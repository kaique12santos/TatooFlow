package com.tattooflow.modules.auth;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestParam String email, @RequestParam String senha) {
        String token = authService.autenticar(email, senha);
        return ResponseEntity.ok(ApiResponse.success("Autenticado com sucesso", token));
    }
}
