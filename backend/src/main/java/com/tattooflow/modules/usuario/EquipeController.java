package com.tattooflow.modules.usuario;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipe")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class EquipeController {

    private final UsuarioService usuarioService;

    public EquipeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/artistas")
    public ResponseEntity<ApiResponse<List<ArtistaEquipeResponse>>> listarArtistas() {
        return ResponseEntity.ok(ApiResponse.success(usuarioService.listarArtistasEquipe()));
    }

    @PatchMapping("/artistas/{id}/revogar")
    public ResponseEntity<ApiResponse<ArtistaEquipeResponse>> revogarArtista(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Acesso do artista revogado", usuarioService.alterarAtivo(id, false)));
    }

    @PatchMapping("/artistas/{id}/reativar")
    public ResponseEntity<ApiResponse<ArtistaEquipeResponse>> reativarArtista(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Acesso do artista reativado", usuarioService.alterarAtivo(id, true)));
    }

    @DeleteMapping("/artistas/{id}")
    public ResponseEntity<ApiResponse<Void>> excluirArtista(@PathVariable Long id) {
        usuarioService.excluirArtista(id);
        return ResponseEntity.ok(ApiResponse.success("Artista removido com sucesso", null));
    }
}
