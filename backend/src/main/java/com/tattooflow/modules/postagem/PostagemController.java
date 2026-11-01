package com.tattooflow.modules.postagem;

import com.tattooflow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para agendamento de postagens no Instagram e histórico de logs [F4-BACK-03, UC03.2].
 */
@RestController
@RequestMapping({"/api/midias/agendamento-postagem", "/midias/agendamento-postagem"})
@Tag(name = "Agendamento de Postagem Instagram", description = "Endpoints para agendamento de postagens e logs de publicação no Instagram [F4-BACK-03]")
public class PostagemController {

    private final PostagemService postagemService;

    public PostagemController(PostagemService postagemService) {
        this.postagemService = postagemService;
    }

    @PostMapping
    @Operation(summary = "Agendar nova postagem no Instagram [F4-BACK-03, Critério 1]")
    public ResponseEntity<ApiResponse<PostagemAgendadaResponse>> agendarPostagem(@RequestBody AgendarPostagemRequest request) {
        PostagemAgendadaResponse response = postagemService.agendarPostagem(request);
        return ResponseEntity.ok(ApiResponse.success("Postagem agendada com sucesso", response));
    }

    @GetMapping
    @Operation(summary = "Listar todas as postagens agendadas [F4-BACK-03]")
    public ResponseEntity<ApiResponse<List<PostagemAgendadaResponse>>> listarPostagens() {
        List<PostagemAgendadaResponse> lista = postagemService.listarTodas().stream()
                .map(PostagemAgendadaResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(lista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalhes de uma postagem por ID [F4-BACK-03]")
    public ResponseEntity<ApiResponse<PostagemAgendadaResponse>> buscarPorId(@PathVariable Long id) {
        PostagemAgendada postagem = postagemService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(PostagemAgendadaResponse.fromEntity(postagem)));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Consultar logs de auditoria de uma postagem [F4-BACK-03, Critério 3]")
    public ResponseEntity<ApiResponse<List<PostagemLog>>> listarLogs(@PathVariable Long id) {
        List<PostagemLog> logs = postagemService.listarLogs(id);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/{id}/publicar-agora")
    @Operation(summary = "Disparar publicação imediatamente ignorando agendamento [F4-BACK-03]")
    public ResponseEntity<ApiResponse<PostagemAgendadaResponse>> publicarAgora(@PathVariable Long id) {
        PostagemAgendada postagem = postagemService.buscarPorId(id);
        postagem = postagemService.executarPublicacao(postagem);
        return ResponseEntity.ok(ApiResponse.success("Publicação executada", PostagemAgendadaResponse.fromEntity(postagem)));
    }
}

