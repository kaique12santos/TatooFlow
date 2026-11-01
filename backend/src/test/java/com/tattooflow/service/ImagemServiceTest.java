package com.tattooflow.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Uploader;
import com.tattooflow.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ImagemServiceTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private ImagemService imagemService;

    @BeforeEach
    void setUp() {
        cloudinary = mock(Cloudinary.class);
        uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        imagemService = new ImagemService(cloudinary);
    }

    @Test
    void deveFazerUploadComSucessoRetornandoSecureUrlEConfiguracoesWebpEQAuto() throws IOException {
        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "tatuagem.jpg",
                "image/jpeg",
                "conteudo-foto-tatuagem".getBytes()
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/test/image/upload/v1234/tatuagem.webp",
                "format", "webp"
        ));

        String url = imagemService.uploadImagem(arquivo);

        assertThat(url).isEqualTo("https://res.cloudinary.com/test/image/upload/v1234/tatuagem.webp");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(eq(arquivo.getBytes()), optionsCaptor.capture());

        Map<String, Object> captorOptions = optionsCaptor.getValue();
        assertThat(captorOptions).containsEntry("format", "webp");
        assertThat(captorOptions).containsEntry("quality", "auto");
        assertThat(captorOptions).containsKey("transformation");
        Object transformation = captorOptions.get("transformation");
        assertThat(transformation).isInstanceOf(Transformation.class);
    }

    @Test
    void deveLancarExcecaoQuandoArquivoForNuloOuVazio() {
        assertThatThrownBy(() -> imagemService.uploadImagem(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");

        MockMultipartFile vazio = new MockMultipartFile("file", "vazio.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> imagemService.uploadImagem(vazio))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void deveLancarBusinessExceptionQuandoCloudinaryFalhar() throws IOException {
        MockMultipartFile arquivo = new MockMultipartFile("file", "teste.png", "image/png", "dados".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Timeout de conexão"));

        assertThatThrownBy(() -> imagemService.uploadImagem(arquivo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Erro ao processar imagem para envio");
    }
}

