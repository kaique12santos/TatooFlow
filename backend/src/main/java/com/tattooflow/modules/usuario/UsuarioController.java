package com.tattooflow.modules.usuario;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
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
    public ResponseEntity<ApiResponse<Usuario>> criar(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(ApiResponse.success("Usuário criado com sucesso", usuarioService.salvar(usuario)));
    }
}
