package com.tattooflow.modules.usuario;

import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        if (usuario.getSenha() == null || !usuario.getSenha().matches("[0-9]{4,6}")) {
            throw new BusinessException("PIN deve conter de 4 a 6 dígitos");
        }
        if (usuario.getAparelhoId() == null || usuario.getAparelhoId().isBlank()
                || usuario.getAparelhoId().length() > 255) {
            throw new BusinessException("ID do aparelho é obrigatório e deve ter no máximo 255 caracteres");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new BusinessException("E-mail já cadastrado");
        }
        if (usuarioRepository.findByAparelhoId(usuario.getAparelhoId()).isPresent()) {
            throw new BusinessException("Aparelho já vinculado a outro usuário");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }
        return usuarioRepository.save(usuario);
    }
}
