package com.tattooflow.modules.usuario;

import com.tattooflow.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {
        Usuario mockUsuario = Usuario.builder().id(1L).nome("João Tatuador").email("joao@tattooflow.com").build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(mockUsuario));

        Usuario usuario = usuarioService.buscarPorId(1L);

        assertNotNull(usuario);
        assertEquals("João Tatuador", usuario.getNome());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void deveSalvarPinComBcryptPreservandoZerosIniciais() {
        Usuario usuario = novoUsuario();
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario salvo = usuarioService.salvar(usuario);

        assertNotEquals("012345", salvo.getSenha());
        assertTrue(passwordEncoder.matches("012345", salvo.getSenha()));
        assertTrue(salvo.getAtivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRejeitarPinForaDoFormatoSemSalvar() {
        Usuario usuario = novoUsuario();
        usuario.setSenha("abc123");

        assertThrows(BusinessException.class, () -> usuarioService.salvar(usuario));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAparelhoJaVinculadoSemSalvar() {
        Usuario usuario = novoUsuario();
        when(usuarioRepository.findByAparelhoId(usuario.getAparelhoId()))
                .thenReturn(Optional.of(Usuario.builder().id(10L).build()));

        assertThrows(BusinessException.class, () -> usuarioService.salvar(usuario));

        verify(usuarioRepository, never()).save(any());
    }

    private Usuario novoUsuario() {
        return Usuario.builder()
                .nome("João Tatuador")
                .email("joao@tattooflow.com")
                .senha("012345")
                .aparelhoId("aparelho-joao")
                .perfil("TATUADOR")
                .build();
    }
}
