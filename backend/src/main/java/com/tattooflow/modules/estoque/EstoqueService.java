package com.tattooflow.modules.estoque;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);

    private final EstoqueRepository estoqueRepository;
    private final UsuarioRepository usuarioRepository;
    private final WhatsAppMessageSender whatsAppMessageSender;
    private final List<AlertaEstoqueDTO> historicoAlertas = new CopyOnWriteArrayList<>();

    public EstoqueService(EstoqueRepository estoqueRepository,
                          UsuarioRepository usuarioRepository,
                          @Autowired(required = false) WhatsAppMessageSender whatsAppMessageSender) {
        this.estoqueRepository = estoqueRepository;
        this.usuarioRepository = usuarioRepository;
        this.whatsAppMessageSender = whatsAppMessageSender;
    }

    public List<ItemEstoque> listarTodos() {
        return estoqueRepository.findAll();
    }

    public List<ItemEstoque> listar(String termo, String categoria, Boolean apenasMinimo) {
        if (Boolean.TRUE.equals(apenasMinimo)) {
            return estoqueRepository.buscarItensEmEstoqueMinimo();
        }
        if (termo != null && !termo.isBlank()) {
            return estoqueRepository.buscarPorTermo(termo.trim());
        }
        if (categoria != null && !categoria.isBlank()) {
            return estoqueRepository.findByCategoriaIgnoreCase(categoria.trim());
        }
        return estoqueRepository.findAll();
    }

    public ItemEstoque buscarPorId(Long id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado com ID: " + id));
    }

    /**
     * [F7-BACK-01] Cadastrar item de estoque com validação de quantidade não-negativa.
     */
    @Transactional
    public ItemEstoque criar(ItemEstoque item) {
        validarItem(item);
        item.setId(null);
        if (item.getQuantidade() == null) {
            item.setQuantidade(0);
        }
        if (item.getQuantidadeMinima() == null) {
            item.setQuantidadeMinima(0);
        }
        ItemEstoque salvo = estoqueRepository.save(item);
        if (salvo.getQuantidade() <= salvo.getQuantidadeMinima()) {
            dispararAlertaEstoqueMinimo(salvo, salvo.getQuantidade(), salvo.getQuantidade(), "Cadastro inicial no limite mínimo");
        }
        return salvo;
    }

    /**
     * [F7-BACK-01] Salvar (compatibilidade com chamadas existentes).
     */
    @Transactional
    public ItemEstoque salvar(ItemEstoque item) {
        if (item.getId() == null) {
            return criar(item);
        }
        return atualizar(item.getId(), item);
    }

    /**
     * [F7-BACK-01] Editar item de estoque com validação de quantidade não-negativa.
     */
    @Transactional
    public ItemEstoque atualizar(Long id, ItemEstoque atualizacao) {
        ItemEstoque existente = buscarPorId(id);
        validarItem(atualizacao);

        int saldoAnterior = existente.getQuantidade() != null ? existente.getQuantidade() : 0;
        int novoSaldo = atualizacao.getQuantidade() != null ? atualizacao.getQuantidade() : 0;
        int novoMinimo = atualizacao.getQuantidadeMinima() != null ? atualizacao.getQuantidadeMinima() : 0;

        existente.setNome(atualizacao.getNome().trim());
        existente.setCategoria(atualizacao.getCategoria());
        existente.setQuantidade(novoSaldo);
        existente.setQuantidadeMinima(novoMinimo);
        existente.setUnidadeMedida(atualizacao.getUnidadeMedida());

        ItemEstoque salvo = estoqueRepository.save(existente);

        if (novoSaldo <= novoMinimo && novoSaldo < saldoAnterior) {
            dispararAlertaEstoqueMinimo(salvo, saldoAnterior, novoSaldo, "Atualização manual de saldo");
        }

        return salvo;
    }

    /**
     * [F7-BACK-01] Remover item de estoque.
     */
    @Transactional
    public void deletar(Long id) {
        ItemEstoque existente = buscarPorId(id);
        estoqueRepository.delete(existente);
    }

    /**
     * [F7-BACK-02] Trigger ao dar baixa no estoque:
     * - Verifica se o saldo é suficiente.
     * - Se a quantidade final atingir o limite mínimo, dispara notificação ao administrador.
     */
    @Transactional
    public BaixaEstoqueResponse darBaixa(Long id, Integer quantidade, String motivo, Long agendamentoId) {
        ItemEstoque item = buscarPorId(id);

        if (quantidade == null || quantidade <= 0) {
            throw new BusinessException("A quantidade para baixa de estoque deve ser maior que zero");
        }

        int saldoAtual = item.getQuantidade() != null ? item.getQuantidade() : 0;
        if (quantidade > saldoAtual) {
            throw new BusinessException(String.format("Saldo insuficiente em estoque para o item '%s'. Saldo disponível: %d, solicitado: %d",
                    item.getNome(), saldoAtual, quantidade));
        }

        int saldoAposBaixa = saldoAtual - quantidade;
        item.setQuantidade(saldoAposBaixa);
        ItemEstoque salvo = estoqueRepository.save(item);

        int minimo = salvo.getQuantidadeMinima() != null ? salvo.getQuantidadeMinima() : 0;
        boolean atingiuMinimo = saldoAposBaixa <= minimo;
        AlertaEstoqueDTO alerta = null;

        if (atingiuMinimo) {
            alerta = dispararAlertaEstoqueMinimo(salvo, saldoAtual, saldoAposBaixa, motivo);
        }

        String mensagem = atingiuMinimo
                ? String.format("Baixa de %d realizada. ALERTA: O item '%s' atingiu a quantidade mínima (%d)!", quantidade, salvo.getNome(), minimo)
                : String.format("Baixa de %d realizada com sucesso. Saldo restante: %d", quantidade, saldoAposBaixa);

        return BaixaEstoqueResponse.builder()
                .item(salvo)
                .quantidadeBaixada(quantidade)
                .saldoAnterior(saldoAtual)
                .saldoAtual(saldoAposBaixa)
                .atingiuEstoqueMinimo(atingiuMinimo)
                .alertaDisparado(atingiuMinimo)
                .mensagem(mensagem)
                .alerta(alerta)
                .build();
    }

    /**
     * [F7-BACK-02] Alocação/baixa de múltiplos itens em lote (compatível com frontend).
     */
    @Transactional
    public List<BaixaEstoqueResponse> alocarMateriais(AlocacaoEstoqueRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("A lista de insumos para alocação não pode estar vazia");
        }

        List<BaixaEstoqueResponse> respostas = new ArrayList<>();
        for (AlocacaoItemPayload payload : request.getItems()) {
            if (payload.getId() == null) {
                continue;
            }
            int qtd = payload.getQuantity() != null ? payload.getQuantity() : 1;
            respostas.add(darBaixa(payload.getId(), qtd, "Alocação para sessão " + request.getSessionId(), null));
        }
        return respostas;
    }

    /**
     * [F7-BACK-02] Disparo de notificação ao administrador quando estoque atinge a quantidade mínima.
     */
    public AlertaEstoqueDTO dispararAlertaEstoqueMinimo(ItemEstoque item, int saldoAnterior, int saldoAtual, String motivo) {
        List<Usuario> administradores = usuarioRepository.findByPerfilAndAtivoTrue("ADMIN");
        if (administradores.isEmpty()) {
            administradores = usuarioRepository.findByPerfil("ADMIN");
        }

        List<String> adminsNotificados = administradores.stream()
                .map(u -> u.getNome() + " (" + u.getEmail() + ")")
                .toList();

        String unidade = item.getUnidadeMedida() != null ? item.getUnidadeMedida() : "unidades";
        int minimo = item.getQuantidadeMinima() != null ? item.getQuantidadeMinima() : 0;

        String mensagem = String.format(
                "[ALERTA DE ESTOQUE MÍNIMO] O insumo '%s' (categoria: %s) atingiu a quantidade crítica! Saldo atual: %d %s (Mínimo estipulado: %d %s). Motivo da baixa: %s. Providenciar reposição imediatamente.",
                item.getNome(),
                item.getCategoria() != null ? item.getCategoria() : "GERAL",
                saldoAtual, unidade,
                minimo, unidade,
                motivo != null ? motivo : "Consumo operacional"
        );

        log.warn("{}", mensagem);

        // Se o WhatsAppMessageSender estiver configurado, envia a mensagem aos canais de apoio
        if (whatsAppMessageSender != null && !administradores.isEmpty()) {
            try {
                // Notificação administrativa via canal de suporte
                whatsAppMessageSender.enviarMensagem("11999999999", mensagem);
            } catch (Exception ex) {
                log.info("Canal WhatsApp não ativo para envio administrativo direto: {}", ex.getMessage());
            }
        }

        AlertaEstoqueDTO alerta = AlertaEstoqueDTO.builder()
                .itemId(item.getId())
                .nomeItem(item.getNome())
                .categoria(item.getCategoria())
                .quantidadeAtual(saldoAtual)
                .quantidadeMinima(minimo)
                .unidadeMedida(unidade)
                .dataAlerta(LocalDateTime.now())
                .mensagem(mensagem)
                .administradoresNotificados(adminsNotificados)
                .build();

        historicoAlertas.add(0, alerta);
        return alerta;
    }

    public List<ItemEstoque> listarItensCriticos() {
        return estoqueRepository.buscarItensEmEstoqueMinimo();
    }

    public List<AlertaEstoqueDTO> listarHistoricoAlertas() {
        return Collections.unmodifiableList(historicoAlertas);
    }

    public void limparHistoricoAlertas() {
        historicoAlertas.clear();
    }

    private void validarItem(ItemEstoque item) {
        if (item.getNome() == null || item.getNome().trim().isEmpty()) {
            throw new BusinessException("O nome do item é obrigatório");
        }
        if (item.getQuantidade() != null && item.getQuantidade() < 0) {
            throw new BusinessException("A quantidade em estoque não pode ser negativa");
        }
        if (item.getQuantidadeMinima() != null && item.getQuantidadeMinima() < 0) {
            throw new BusinessException("A quantidade mínima não pode ser negativa");
        }
    }
}
