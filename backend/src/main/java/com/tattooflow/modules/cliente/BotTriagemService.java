package com.tattooflow.modules.cliente;

import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * F2-BACK-02: Fluxo conversacional de coleta de dados via bot do WhatsApp.
 * Gerencia a maquina de estados de perguntas sequenciais (arte, local, data),
 * valida respostas e persiste o pre-agendamento no banco de dados.
 */
@Service
public class BotTriagemService {

    private static final Logger log = LoggerFactory.getLogger(BotTriagemService.class);

    public static final String ETAPA_INICIO = "INICIO";
    public static final String ETAPA_AGUARDANDO_ARTE = "AGUARDANDO_ARTE";
    public static final String ETAPA_AGUARDANDO_LOCAL = "AGUARDANDO_LOCAL";
    public static final String ETAPA_AGUARDANDO_DATA = "AGUARDANDO_DATA";
    public static final String ETAPA_FINALIZADO = "FINALIZADO";

    public static final String STATUS_EM_TRIAGEM = "EM_TRIAGEM";
    public static final String STATUS_PENDENTE = "PENDENTE";

    private static final Set<String> COMANDOS_REINICIAR = Set.of(
            "reiniciar", "cancelar", "recomeçar", "recomecar", "começar de novo", "novo"
    );

    private static final Set<String> SAUDACOES = Set.of(
            "oi", "ola", "olá", "opa", "salve", "bom dia", "boa tarde", "boa noite",
            "e ai", "e aí", "hello", "hi", "quero tatuar", "orçamento", "orcamento",
            "informacao", "informação", "informações", "informacoes", "começar", "comecar"
    );

    private boolean isSaudacao(String texto) {
        String normalizado = texto.toLowerCase().replaceAll("[!?.\\-,]", "").trim();
        return SAUDACOES.contains(normalizado) || normalizado.startsWith("ola ") || normalizado.startsWith("olá ") || normalizado.startsWith("oi ");
    }

    private final ClienteRepository clienteRepository;
    private final PreAgendamentoRepository preAgendamentoRepository;
    private final WhatsAppMessageSender messageSender;

    public record RespostaBot(String textoEnviado, String etapaAtual, Long preAgendamentoId) {}

    public BotTriagemService(ClienteRepository clienteRepository,
                             PreAgendamentoRepository preAgendamentoRepository,
                             WhatsAppMessageSender messageSender) {
        this.clienteRepository = clienteRepository;
        this.preAgendamentoRepository = preAgendamentoRepository;
        this.messageSender = messageSender;
    }

    @Transactional
    public RespostaBot processarMensagem(String telefone, String nomeContato, String mensagemRecebida) {
        String texto = mensagemRecebida != null ? mensagemRecebida.trim() : "";
        log.info("[BOT TRIAGEM] Mensagem recebida de {} ({}): '{}'", telefone, nomeContato, texto);

        // 1. Obter ou criar o cliente
        Cliente cliente = obterOuCriarCliente(telefone, nomeContato);

        // 2. Tratamento de mensagens vazias ou fora de padrão preliminar
        if (texto.isBlank()) {
            String resposta = "Olá! Para iniciar seu atendimento no TattooFlow, envie uma mensagem descrevendo sua ideia de tattoo! 🎨";
            messageSender.enviarMensagem(telefone, resposta);
            return new RespostaBot(resposta, ETAPA_INICIO, null);
        }

        // 3. Comando de reinício/cancelamento
        if (isComandoReiniciar(texto)) {
            PreAgendamento novo = criarNovoPreAgendamento(cliente);
            novo.setEtapaBot(ETAPA_AGUARDANDO_ARTE);
            preAgendamentoRepository.save(novo);
            String resposta = String.format("Tudo bem, %s! Reiniciamos seu atendimento. 🎨\n\nQual é a **ideia ou desenho** que você deseja tatuar? (Ex: estilo, referência, tamanho aproximado)",
                    cliente.getNome());
            messageSender.enviarMensagem(telefone, resposta);
            return new RespostaBot(resposta, ETAPA_AGUARDANDO_ARTE, novo.getId());
        }

        // 4. Buscar pre-agendamento ativo em andamento
        Optional<PreAgendamento> ativoOpt = preAgendamentoRepository
                .findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(cliente, STATUS_EM_TRIAGEM);

        PreAgendamento preAgendamento;
        if (ativoOpt.isEmpty()) {
            // Se nao tem triagem em andamento, verifica se o ultimo ja foi finalizado
            Optional<PreAgendamento> ultimoOpt = preAgendamentoRepository.findFirstByClienteOrderByDataCriacaoDesc(cliente);
            if (ultimoOpt.isPresent() && ETAPA_FINALIZADO.equals(ultimoOpt.get().getEtapaBot())) {
                String resposta = "Seu pré-agendamento anterior já foi recebido e está em análise pela nossa equipe! ⏳\n\nCaso queira solicitar um **novo orçamento**, digite **reiniciar**.";
                messageSender.enviarMensagem(telefone, resposta);
                return new RespostaBot(resposta, ETAPA_FINALIZADO, ultimoOpt.get().getId());
            }

            preAgendamento = criarNovoPreAgendamento(cliente);
        } else {
            preAgendamento = ativoOpt.get();
        }

        // 5. Execução da máquina de estados com perguntas sequenciais e validações
        String etapa = preAgendamento.getEtapaBot() != null ? preAgendamento.getEtapaBot() : ETAPA_AGUARDANDO_ARTE;
        String resposta;

        switch (etapa) {
            case ETAPA_INICIO:
                if (isSaudacao(texto)) {
                    preAgendamento.setEtapaBot(ETAPA_AGUARDANDO_ARTE);
                    resposta = String.format("Olá, %s! Bem-vindo(a) ao TattooFlow! 🎨\n\nVamos iniciar seu pré-agendamento e orçamento. Para começar: qual é a **ideia ou desenho** da sua tattoo? (Ex: estilo, referência, tamanho)", cliente.getNome());
                } else {
                    resposta = processarEtapaArte(preAgendamento, cliente, texto);
                }
                break;

            case ETAPA_AGUARDANDO_ARTE:
                if (isSaudacao(texto)) {
                    resposta = "Olá! 😊 Para darmos andamento ao seu orçamento, por favor descreva a **ideia ou desenho** que você gostaria de tatuar (ex: 'borboleta em fine line', 'dragão oriental no braço').";
                } else {
                    resposta = processarEtapaArte(preAgendamento, cliente, texto);
                }
                break;

            case ETAPA_AGUARDANDO_LOCAL:
                resposta = processarEtapaLocal(preAgendamento, cliente, texto);
                break;

            case ETAPA_AGUARDANDO_DATA:
                resposta = processarEtapaData(preAgendamento, cliente, texto);
                break;

            default:
                resposta = "Seu pré-agendamento já está registrado! Se quiser começar outro, envie **reiniciar**.";
                break;
        }

        preAgendamento.setDataAtualizacao(LocalDateTime.now());
        preAgendamentoRepository.save(preAgendamento);

        messageSender.enviarMensagem(telefone, resposta);
        return new RespostaBot(resposta, preAgendamento.getEtapaBot(), preAgendamento.getId());
    }

    private String processarEtapaArte(PreAgendamento preAgendamento, Cliente cliente, String texto) {
        // Validação da resposta: deve ter no mínimo 3 caracteres e não ser apenas pontuação/números
        if (texto.length() < 3 || texto.matches("^[0-9\\W]+$")) {
            return "Não consegui entender a ideia da sua tattoo. 🤔\nPor favor, conte um pouco sobre o desenho, estilo ou referência que você gostaria de tatuar (ex: 'borboleta em fine line', 'dragão oriental no braço').";
        }

        preAgendamento.setIdeiasDescricao(texto);
        preAgendamento.setReferencia(texto);
        preAgendamento.setEtapaBot(ETAPA_AGUARDANDO_LOCAL);

        return "Excelente ideia! Já anotei aqui. ✍️\n\nAgora me diga: em qual **local do corpo** você pretende fazer a tattoo? (Ex: antebraço, costela, ombro, panturrilha)";
    }

    private String processarEtapaLocal(PreAgendamento preAgendamento, Cliente cliente, String texto) {
        // Validação da resposta do local: no mínimo 2 caracteres válidos
        if (texto.length() < 2 || texto.matches("^[0-9\\W]+$")) {
            return "Por favor, indique uma região do corpo válida para a tatuagem (ex: 'braço', 'antebraço', 'costela', 'perna').";
        }

        preAgendamento.setLocalCorpo(texto);
        preAgendamento.setEtapaBot(ETAPA_AGUARDANDO_DATA);

        return "Perfeito! Local registrado. 📍\n\nPor último: para **quando você gostaria de agendar** sua sessão? (Ex: 'próximo sábado', 'segunda quinzena de novembro', 'qualquer dia à tarde')";
    }

    private String processarEtapaData(PreAgendamento preAgendamento, Cliente cliente, String texto) {
        // Validação da resposta de data: no mínimo 2 caracteres
        if (texto.length() < 2 || texto.matches("^[\\W]+$")) {
            return "Por favor, informe uma data ou período aproximado de sua preferência (ex: 'próxima semana', '15/11/2026', 'sábado à tarde').";
        }

        preAgendamento.setDataDesejada(texto);
        preAgendamento.setEtapaBot(ETAPA_FINALIZADO);
        preAgendamento.setStatusTriagem(STATUS_PENDENTE);

        return String.format(
                "Show de bola, %s! Seu pré-agendamento foi concluído com sucesso! 🎉\n\n" +
                "📋 **Resumo da sua solicitação:**\n" +
                "• **Arte/Ideia:** %s\n" +
                "• **Local do corpo:** %s\n" +
                "• **Data de preferência:** %s\n\n" +
                "Nossa equipe de tatuadores irá analisar sua proposta e entraremos em contato em breve para confirmar o orçamento e o horário! Caso deseje alterar algo, digite **reiniciar**.",
                cliente.getNome(),
                preAgendamento.getIdeiasDescricao(),
                preAgendamento.getLocalCorpo(),
                preAgendamento.getDataDesejada()
        );
    }

    private Cliente obterOuCriarCliente(String telefone, String nomeContato) {
        return clienteRepository.findByTelefone(telefone).orElseGet(() -> {
            String nome = (nomeContato != null && !nomeContato.isBlank()) ? nomeContato : "Cliente WhatsApp";
            Cliente novo = Cliente.builder()
                    .telefone(telefone)
                    .nome(nome)
                    .build();
            return clienteRepository.save(novo);
        });
    }

    private PreAgendamento criarNovoPreAgendamento(Cliente cliente) {
        PreAgendamento preAgendamento = PreAgendamento.builder()
                .cliente(cliente)
                .statusTriagem(STATUS_EM_TRIAGEM)
                .etapaBot(ETAPA_INICIO)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
        return preAgendamentoRepository.save(preAgendamento);
    }

    private boolean isComandoReiniciar(String texto) {
        return COMANDOS_REINICIAR.contains(texto.toLowerCase().trim());
    }
}
