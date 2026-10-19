package com.tattooflow.modules.cliente;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triagem")
public class TriagemController {

    private final BotTriagemService botTriagemService;
    private final PreAgendamentoRepository preAgendamentoRepository;
    private final ClienteRepository clienteRepository;

    public record MensagemBotRequest(String telefone, String nome, String mensagem) {}

    public TriagemController(BotTriagemService botTriagemService,
                             PreAgendamentoRepository preAgendamentoRepository,
                             ClienteRepository clienteRepository) {
        this.botTriagemService = botTriagemService;
        this.preAgendamentoRepository = preAgendamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    @PostMapping("/bot/simular")
    public ResponseEntity<ApiResponse<BotTriagemService.RespostaBot>> simularMensagem(@RequestBody MensagemBotRequest request) {
        BotTriagemService.RespostaBot resposta = botTriagemService.processarMensagem(
                request.telefone(),
                request.nome(),
                request.mensagem()
        );
        return ResponseEntity.ok(ApiResponse.success("Mensagem processada pelo bot", resposta));
    }

    @GetMapping("/pre-agendamentos")
    public ResponseEntity<ApiResponse<List<PreAgendamento>>> listarPreAgendamentos() {
        return ResponseEntity.ok(ApiResponse.success(preAgendamentoRepository.findAll()));
    }

    @GetMapping("/clientes")
    public ResponseEntity<ApiResponse<List<Cliente>>> listarClientesTriagem() {
        return ResponseEntity.ok(ApiResponse.success(clienteRepository.findAll()));
    }
}
