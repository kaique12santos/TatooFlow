package com.tattooflow.modules.usuario;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Usuario>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(usuarioService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(usuarioService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastrar usuário e autorizar seu aparelho",
            description = "Requer JWT de ADMIN. O PIN é armazenado como bcrypt e não é retornado na resposta.")
    public ResponseEntity<ApiResponse<Usuario>> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.pin())
                .aparelhoId(request.aparelhoId())
                .perfil(request.perfil())
                .ativo(true)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Usuário criado com sucesso", usuarioService.salvar(usuario)));
    }
}
