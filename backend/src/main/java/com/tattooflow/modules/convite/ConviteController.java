package com.tattooflow.modules.convite;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convites")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class ConviteController {

    private final ConviteService conviteService;

    public ConviteController(ConviteService conviteService) {
        this.conviteService = conviteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Convite>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(conviteService.listarTodos()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Convite>> criarConvite(@Valid @RequestBody CriarConviteRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Convite criado com sucesso", conviteService.criarConvite(Convite.builder().emailConvidado(request.emailConvidado()).build())));
    }
}
