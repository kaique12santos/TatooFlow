package com.tattooflow.modules.auth;

import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtUtil jwtUtil, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public String autenticar(String pin, String aparelhoId) {
        Usuario usuario = usuarioRepository.findByAparelhoId(aparelhoId)
                .orElseThrow(() -> new AccessDeniedException("Aparelho não autorizado"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new AccessDeniedException("Usuário inativo");
        }
        if (!"ADMIN".equals(usuario.getPerfil()) && !"TATUADOR".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Usuário não autorizado");
        }
        if (!passwordEncoder.matches(pin, usuario.getSenha())) {
            throw new BadCredentialsException("PIN inválido");
        }

        return jwtUtil.generateToken(usuario.getEmail());
    }
}
