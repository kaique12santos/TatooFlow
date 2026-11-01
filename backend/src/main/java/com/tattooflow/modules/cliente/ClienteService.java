package com.tattooflow.modules.cliente;

import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.midia.Midia;
import com.tattooflow.modules.midia.MidiaRepository;
import com.tattooflow.modules.termo.TermoAceite;
import com.tattooflow.modules.termo.TermoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PreAgendamentoRepository preAgendamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final TermoRepository termoRepository;
    private final MidiaRepository midiaRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this(clienteRepository, null, null, null, null);
    }

    public ClienteService(ClienteRepository clienteRepository, TermoRepository termoRepository) {
        this(clienteRepository, null, null, termoRepository, null);
    }

    @Autowired
    public ClienteService(ClienteRepository clienteRepository,
                          @Autowired(required = false) PreAgendamentoRepository preAgendamentoRepository,
                          @Autowired(required = false) AgendamentoRepository agendamentoRepository,
                          @Autowired(required = false) TermoRepository termoRepository,
                          @Autowired(required = false) MidiaRepository midiaRepository) {
        this.clienteRepository = clienteRepository;
        this.preAgendamentoRepository = preAgendamentoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.termoRepository = termoRepository;
        this.midiaRepository = midiaRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Consulta os termos de consentimento vinculados ao histórico do cliente [F3-BACK-02].
     */
    public List<TermoAceite> buscarHistoricoTermos(Long clienteId) {
        buscarPorId(clienteId);
        if (termoRepository == null) {
            return List.of();
        }
        return termoRepository.findByClienteIdOrderByDataAssinaturaDesc(clienteId);
    }

    /**
     * Busca e filtra clientes por nome ou tipo de trabalho [F5-BACK-02, UC04.1].
     * Retorna os clientes correspondentes com seus orçamentos e sessões consolidados.
     * Conforme RNF08, a resposta é otimizada para ser rápida.
     */
    public List<ClienteBuscaResponse> buscarClientes(String termo) {
        List<Cliente> clientes;
        if (termo == null || termo.trim().isEmpty()) {
            clientes = clienteRepository.findAllByOrderByNomeAsc();
        } else {
            clientes = clienteRepository.buscarPorNomeOuTipoTrabalho(termo.trim());
        }

        return clientes.stream().map(cliente -> {
            List<PreAgendamento> orcamentosEnt = preAgendamentoRepository != null
                    ? preAgendamentoRepository.findByClienteOrderByDataCriacaoDesc(cliente)
                    : List.of();

            List<Agendamento> sessoesEnt = agendamentoRepository != null
                    ? agendamentoRepository.findByClienteIdOrderByDataHoraDesc(cliente.getId())
                    : List.of();

            LinkedHashSet<String> tiposSet = new LinkedHashSet<>();
            for (Agendamento s : sessoesEnt) {
                if (s.getDescricaoSessao() != null && !s.getDescricaoSessao().isBlank()) {
                    tiposSet.add(s.getDescricaoSessao().trim());
                }
            }
            for (PreAgendamento o : orcamentosEnt) {
                if (o.getIdeiasDescricao() != null && !o.getIdeiasDescricao().isBlank()) {
                    tiposSet.add(o.getIdeiasDescricao().trim());
                }
                if (o.getReferencia() != null && !o.getReferencia().isBlank()) {
                    tiposSet.add(o.getReferencia().trim());
                }
            }

            List<String> tiposTrabalho = new ArrayList<>(tiposSet);
            String tipoTrabalho = tiposTrabalho.isEmpty() ? null : tiposTrabalho.get(0);

            List<ClienteBuscaResponse.OrcamentoResumoDTO> orcamentos = orcamentosEnt.stream()
                    .map(o -> ClienteBuscaResponse.OrcamentoResumoDTO.builder()
                            .id(o.getId())
                            .ideiasDescricao(o.getIdeiasDescricao())
                            .referencia(o.getReferencia())
                            .localCorpo(o.getLocalCorpo())
                            .dataDesejada(o.getDataDesejada())
                            .statusTriagem(o.getStatusTriagem())
                            .dataCriacao(o.getDataCriacao())
                            .build())
                    .collect(Collectors.toList());

            List<ClienteBuscaResponse.SessaoResumoDTO> sessoes = sessoesEnt.stream()
                    .map(s -> ClienteBuscaResponse.SessaoResumoDTO.builder()
                            .id(s.getId())
                            .tatuadorId(s.getTatuadorId())
                            .dataHora(s.getDataHora())
                            .duracaoMinutos(s.getDuracaoMinutos())
                            .descricaoSessao(s.getDescricaoSessao())
                            .valorEstimado(s.getValorEstimado())
                            .status(s.getStatus())
                            .build())
                    .collect(Collectors.toList());

            return ClienteBuscaResponse.builder()
                    .id(cliente.getId())
                    .nome(cliente.getNome())
                    .telefone(cliente.getTelefone())
                    .email(cliente.getEmail())
                    .instagram(cliente.getInstagram())
                    .historicoSaude(cliente.getHistoricoSaude())
                    .tipoTrabalho(tipoTrabalho)
                    .tiposTrabalho(tiposTrabalho)
                    .orcamentos(orcamentos)
                    .sessoes(sessoes)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Consulta o histórico completo do cliente [F5-BACK-03].
     * Retorna os dados cadastrais, sessões (agendamentos), termos de aceite assinados e mídias vinculadas.
     */
    public ClienteHistoricoResponse buscarHistoricoCompleto(Long clienteId) {
        Cliente cliente = buscarPorId(clienteId);

        List<Agendamento> sessoesEnt = agendamentoRepository != null
                ? agendamentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                : List.of();

        List<TermoAceite> termosEnt = termoRepository != null
                ? termoRepository.findByClienteIdOrderByDataAssinaturaDesc(clienteId)
                : List.of();

        List<Long> agendamentoIds = sessoesEnt.stream().map(Agendamento::getId).collect(Collectors.toList());
        List<Midia> midiasEnt = (!agendamentoIds.isEmpty() && midiaRepository != null)
                ? midiaRepository.findByAgendamentoIdInOrderByDataUploadDesc(agendamentoIds)
                : List.of();

        Map<Long, List<ClienteHistoricoResponse.MidiaDTO>> midiasPorAgendamento = midiasEnt.stream()
                .map(m -> ClienteHistoricoResponse.MidiaDTO.builder()
                        .id(m.getId())
                        .agendamentoId(m.getAgendamentoId())
                        .url(m.getUrl())
                        .tipo(m.getTipo())
                        .dataUpload(m.getDataUpload())
                        .build())
                .collect(Collectors.groupingBy(ClienteHistoricoResponse.MidiaDTO::getAgendamentoId));

        List<ClienteHistoricoResponse.MidiaDTO> todasMidiasDTO = midiasEnt.stream()
                .map(m -> ClienteHistoricoResponse.MidiaDTO.builder()
                        .id(m.getId())
                        .agendamentoId(m.getAgendamentoId())
                        .url(m.getUrl())
                        .tipo(m.getTipo())
                        .dataUpload(m.getDataUpload())
                        .build())
                .collect(Collectors.toList());

        List<ClienteHistoricoResponse.SessaoHistoricoDTO> sessoesDTO = sessoesEnt.stream()
                .map(s -> ClienteHistoricoResponse.SessaoHistoricoDTO.builder()
                        .id(s.getId())
                        .tatuadorId(s.getTatuadorId())
                        .dataHora(s.getDataHora())
                        .duracaoMinutos(s.getDuracaoMinutos())
                        .descricaoSessao(s.getDescricaoSessao())
                        .valorEstimado(s.getValorEstimado())
                        .status(s.getStatus())
                        .midias(midiasPorAgendamento.getOrDefault(s.getId(), List.of()))
                        .build())
                .collect(Collectors.toList());

        List<ClienteHistoricoResponse.TermoAceiteDTO> termosDTO = termosEnt.stream()
                .map(t -> ClienteHistoricoResponse.TermoAceiteDTO.builder()
                        .id(t.getId())
                        .agendamentoId(t.getAgendamentoId())
                        .urlPdfAssinado(t.getUrlPdfAssinado())
                        .dataAssinatura(t.getDataAssinatura())
                        .ipAssinatura(t.getIpAssinatura())
                        .dataExpiracaoRetencao(t.getDataExpiracaoRetencao())
                        .build())
                .collect(Collectors.toList());

        ClienteHistoricoResponse.ClienteResumoDTO clienteDTO = ClienteHistoricoResponse.ClienteResumoDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .telefone(cliente.getTelefone())
                .email(cliente.getEmail())
                .instagram(cliente.getInstagram())
                .historicoSaude(cliente.getHistoricoSaude())
                .build();

        return ClienteHistoricoResponse.builder()
                .cliente(clienteDTO)
                .sessoes(sessoesDTO)
                .termos(termosDTO)
                .midias(todasMidiasDTO)
                .totalSessoes(sessoesDTO.size())
                .totalTermos(termosDTO.size())
                .totalMidias(todasMidiasDTO.size())
                .build();
    }
}
