package com.tattooflow.modules.auth;

import com.tattooflow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Entrar com PIN e ID do aparelho",
            description = "Valida o PIN com bcrypt e o vínculo do aparelho com um usuário ativo. "
                    + "O JWT é retornado no campo data e pode ser usado em Authorize.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticado; JWT no campo data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "PIN ou ID do aparelho ausente ou com formato inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "PIN inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Aparelho não autorizado ou usuário sem acesso")
    })
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.autenticar(request.pin(), request.aparelhoId());
        return ResponseEntity.ok(ApiResponse.success("Autenticado com sucesso", token));
    }
}
