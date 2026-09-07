package com.tattooflow.modules.auth;

import com.tattooflow.common.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String autenticar(String email, String senha) {
        // Validação e geração de JWT token
        return jwtUtil.generateToken(email);
    }
}
