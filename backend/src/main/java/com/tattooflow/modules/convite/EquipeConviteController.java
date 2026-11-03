package com.tattooflow.modules.convite;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipe/convites")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class EquipeConviteController {

    private final ConviteService conviteService;

    public EquipeConviteController(ConviteService conviteService) {
        this.conviteService = conviteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipeConviteResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(conviteService.listarConvitesEquipe()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EquipeConviteResponse>> criar(@Valid @RequestBody CriarConviteRequest request) {
        Convite convite = conviteService.criarConvite(Convite.builder()
                .emailConvidado(request.emailConvidado())
                .build());
        return ResponseEntity.ok(ApiResponse.success("Convite criado com sucesso", EquipeConviteResponse.from(convite)));
    }

    @PostMapping("/{id}/reenviar")
    public ResponseEntity<ApiResponse<EquipeConviteResponse>> reenviar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Convite reenviado com sucesso",
                EquipeConviteResponse.from(conviteService.reenviarConvite(id))));
    }
}
