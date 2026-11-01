package com.tattooflow.modules.midia;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.termo.TermoRepository;
import com.tattooflow.service.GeminiService;
import com.tattooflow.service.ImagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MidiaServiceTest {

    private MidiaRepository midiaRepository;
    private TermoRepository termoRepository;
    private AgendamentoRepository agendamentoRepository;
    private ImagemService imagemService;
    private GeminiService geminiService;
    private MidiaService midiaService;

    @BeforeEach
    void setUp() {
        midiaRepository = mock(MidiaRepository.class);
        termoRepository = mock(TermoRepository.class);
        agendamentoRepository = mock(AgendamentoRepository.class);
        imagemService = mock(ImagemService.class);
        geminiService = mock(GeminiService.class);

        midiaService = new MidiaService(midiaRepository, termoRepository, agendamentoRepository, imagemService, geminiService);
    }

    @Test
    void deveFazerUploadComSucessoQuandoTermoEstiverAssinado() {
        Long agendamentoId = 10L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "tatoo_finalizada.jpg", "image/jpeg", "bytes-da-imagem".getBytes()
        );

        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(termoRepository.existsByAgendamentoIdAndDataAssinaturaIsNotNull(agendamentoId)).thenReturn(true);
        when(imagemService.uploadImagem(file)).thenReturn("https://res.cloudinary.com/test/image/upload/v1/tatoo.webp");
        when(midiaRepository.save(any(Midia.class))).thenAnswer(invocation -> {
            Midia m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        Midia resultado = midiaService.uploadMidia(file, agendamentoId, "FOTO_DEPOIS");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getUrl()).isEqualTo("https://res.cloudinary.com/test/image/upload/v1/tatoo.webp");
        assertThat(resultado.getTipo()).isEqualTo("FOTO_DEPOIS");
        assertThat(resultado.getAgendamentoId()).isEqualTo(agendamentoId);
        assertThat(resultado.getDataUpload()).isNotNull();

        verify(termoRepository).existsByAgendamentoIdAndDataAssinaturaIsNotNull(agendamentoId);
        verify(imagemService).uploadImagem(file);
        verify(midiaRepository).save(any(Midia.class));
    }

    @Test
    void devePermitirUploadQuandoTermoForAssinadoPorClienteVinculadoAoAgendamento() {
        Long agendamentoId = 15L;
        Long clienteId = 99L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "tatoo_costas.png", "image/png", "bytes-da-foto".getBytes()
        );

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .clienteId(clienteId)
                .build();

        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(termoRepository.existsByAgendamentoIdAndDataAssinaturaIsNotNull(agendamentoId)).thenReturn(false);
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(termoRepository.existsByClienteIdAndDataAssinaturaIsNotNull(clienteId)).thenReturn(true);
        when(imagemService.uploadImagem(file)).thenReturn("https://res.cloudinary.com/test/image/upload/v1/costas.webp");
        when(midiaRepository.save(any(Midia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Midia resultado = midiaService.uploadMidia(file, agendamentoId, null);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUrl()).isEqualTo("https://res.cloudinary.com/test/image/upload/v1/costas.webp");
        assertThat(resultado.getTipo()).isEqualTo("FOTO_DEPOIS"); // Padrão se null

        verify(imagemService).uploadImagem(file);
    }

    @Test
    void deveBloquearUploadE_NUNCA_ChamarBucketQuandoTermoNaoEstiverAssinado() {
        Long agendamentoId = 20L;
        Long clienteId = 55L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "tatoo.jpg", "image/jpeg", "dados".getBytes()
        );

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .clienteId(clienteId)
                .build();

        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(true);
        when(termoRepository.existsByAgendamentoIdAndDataAssinaturaIsNotNull(agendamentoId)).thenReturn(false);
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(termoRepository.existsByClienteIdAndDataAssinaturaIsNotNull(clienteId)).thenReturn(false);

        assertThatThrownBy(() -> midiaService.uploadMidia(file, agendamentoId, "FOTO_DEPOIS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Upload de mídia bloqueado: o cliente deve aceitar digitalmente o termo de consentimento antes de enviar fotos (Fase 4).");

        // CRITÉRIO CRÍTICO: upload no bucket NÃO pode ser executado sem termo aceito
        verify(imagemService, never()).uploadImagem(any());
        verify(midiaRepository, never()).save(any());
    }

    @Test
    void deveBloquearUploadQuandoArquivoExceder10MB() {
        Long agendamentoId = 20L;
        byte[] conteudoGrande = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile fileGrande = new MockMultipartFile(
                "file", "foto_gigante.jpg", "image/jpeg", conteudoGrande
        );

        assertThatThrownBy(() -> midiaService.uploadMidia(fileGrande, agendamentoId, "FOTO_DEPOIS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Arquivo excede o tamanho máximo permitido de 10MB.");

        verify(imagemService, never()).uploadImagem(any());
        verify(midiaRepository, never()).save(any());
    }

    @Test
    void deveBloquearUploadQuandoArquivoForNuloOuVazio() {
        assertThatThrownBy(() -> midiaService.uploadMidia(null, 1L, "FOTO_DEPOIS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");

        MockMultipartFile vazio = new MockMultipartFile("file", "vazio.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> midiaService.uploadMidia(vazio, 1L, "FOTO_DEPOIS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");

        verify(imagemService, never()).uploadImagem(any());
    }

    @Test
    void deveBloquearUploadQuandoFormatoNaoForImagem() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "documento.pdf", "application/pdf", "pdf-data".getBytes()
        );

        assertThatThrownBy(() -> midiaService.uploadMidia(pdf, 1L, "FOTO_DEPOIS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apenas imagens são permitidas");

        verify(imagemService, never()).uploadImagem(any());
    }

    @Test
    void deveBloquearUploadQuandoAgendamentoNaoExistir() {
        Long agendamentoId = 999L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "tatoo.jpg", "image/jpeg", "dados".getBytes()
        );

        when(agendamentoRepository.existsById(agendamentoId)).thenReturn(false);

        assertThatThrownBy(() -> midiaService.uploadMidia(file, agendamentoId, "FOTO_DEPOIS"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agendamento não encontrado com o ID: 999");

        verify(imagemService, never()).uploadImagem(any());
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve enriquecer contexto e delegar para GeminiService ao gerar legenda")
    void deveEnriquecerContextoEDelegarParaGeminiServiceAoGerarLegenda() {
        Long midiaId = 1L;
        Long agendamentoId = 20L;

        Midia midia = Midia.builder()
                .id(midiaId)
                .agendamentoId(agendamentoId)
                .url("https://res.cloudinary.com/demo/image/upload/v1/obra.webp")
                .tipo("FOTO_DEPOIS")
                .build();

        Agendamento agendamento = Agendamento.builder()
                .id(agendamentoId)
                .descricaoSessao("Estudo de São Jerônimo FineLine")
                .build();

        when(midiaRepository.findById(midiaId)).thenReturn(Optional.of(midia));
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

        LegendaResponse mockResponse = LegendaResponse.builder()
                .title("Título Teste")
                .body("Texto com https://wa.me/5511999999999, FineLine e SP")
                .hashtags(List.of("#TattooFlow", "#FineLine"))
                .legendaCompleta("Completa")
                .estilo("FineLine")
                .localizacao("SP")
                .whatsappLink("https://wa.me/5511999999999")
                .geradoPorIa(true)
                .build();

        when(geminiService.gerarLegenda(any(GerarLegendaRequest.class))).thenReturn(mockResponse);

        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .midiaId(midiaId)
                .estilo("FineLine")
                .build();

        LegendaResponse resultado = midiaService.gerarLegenda(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitle()).isEqualTo("Título Teste");
        assertThat(resultado.isGeradoPorIa()).isTrue();

        ArgumentCaptor<GerarLegendaRequest> captor = ArgumentCaptor.forClass(GerarLegendaRequest.class);
        verify(geminiService).gerarLegenda(captor.capture());

        GerarLegendaRequest capturado = captor.getValue();
        assertThat(capturado.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/obra.webp");
        assertThat(capturado.getAgendamentoId()).isEqualTo(agendamentoId);
        assertThat(capturado.getDescricao()).isEqualTo("Estudo de São Jerônimo FineLine");
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve lançar exceção se GeminiService for nulo")
    void deveLancarExcecaoSeGeminiServiceForNulo() {
        MidiaService serviceSemGemini = new MidiaService(midiaRepository, termoRepository, agendamentoRepository, imagemService, null);

        assertThatThrownBy(() -> serviceSemGemini.gerarLegenda(new GerarLegendaRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Serviço de IA Gemini não configurado");
    }
}

