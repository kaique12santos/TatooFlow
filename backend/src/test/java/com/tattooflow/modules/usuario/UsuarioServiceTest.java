package com.tattooflow.modules.usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

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
}
