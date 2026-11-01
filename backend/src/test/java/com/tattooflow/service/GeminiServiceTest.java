package com.tattooflow.service;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.midia.GerarLegendaRequest;
import com.tattooflow.modules.midia.LegendaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiServiceTest {

    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;
    private GeminiService geminiService;

    private static final String API_KEY = "chave-gemini-teste-123";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        geminiService = new GeminiService(restTemplate, API_KEY, API_URL);
    }

    @Test
    void deveTestarComunicacaoComSucesso() {
        String mockResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Conexão com Gemini estabelecida com sucesso!"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contents[0].parts[0].text").value("Teste de prompt"))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        String resposta = geminiService.testarComunicacao("Teste de prompt");

        assertThat(resposta).isNotNull();
        assertThat(resposta).contains("Conexão com Gemini estabelecida");
        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoSeApiKeyNaoEstiverConfigurada() {
        GeminiService serviceSemChave = new GeminiService(restTemplate, "", API_URL);

        assertThatThrownBy(serviceSemChave::testarComunicacao)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chave da API do Google Gemini");
    }

    @Test
    void deveLancarExcecaoQuandoGeminiRetornarErroHttp() {
        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\": \"API_KEY_INVALID\"}"));

        assertThatThrownBy(() -> geminiService.testarComunicacao("Teste"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("401");

        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve gerar legenda com sucesso via Gemini API cumprindo rigorosamente a RN03")
    void deveGerarLegendaComSucessoViaGeminiComRN03() {
        String mockResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Estudo de São Jerônimo\\nLinhas puras e contraste denso traduzidos na derme.\\nEstilo: FineLine\\nLocalização: Antebraço\\nAgendamentos: https://wa.me/5511999999999\\n\\n#TattooFlow #FineLine #ArtOnSkin"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .estilo("FineLine")
                .localizacao("Antebraço")
                .whatsappLink("https://wa.me/5511999999999")
                .tom("chiaroscuro")
                .imageUrl("https://res.cloudinary.com/demo/image/upload/v1/tatoo.webp")
                .artista("Gabriel Dornelles")
                .descricao("Estudo anatômico com agulhas 03RL")
                .build();

        LegendaResponse response = geminiService.gerarLegenda(request);

        assertThat(response).isNotNull();
        assertThat(response.isGeradoPorIa()).isTrue();
        assertThat(response.isFallback()).isFalse();
        assertThat(response.getEstilo()).isEqualTo("FineLine");
        assertThat(response.getLocalizacao()).isEqualTo("Antebraço");
        assertThat(response.getWhatsappLink()).isEqualTo("https://wa.me/5511999999999");
        assertThat(response.getBody()).contains("FineLine");
        assertThat(response.getLegendaCompleta()).contains("https://wa.me/5511999999999");
        assertThat(response.getLegendaCompleta()).contains("Antebraço");
        assertThat(response.getHashtags()).contains("#TattooFlow", "#FineLine", "#ArtOnSkin");

        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve garantir RN03 adicionando campos faltantes caso a IA os omita")
    void deveGarantirRN03InserindoCamposObrigatoriosQuandoOmitidos() {
        String mockResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Arte incrível finalizada no estúdio hoje.\\n\\n#TattooStudio #Ink"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .estilo("Blackwork")
                .localizacao("Costas")
                .whatsappLink("https://wa.me/5511988887777")
                .tom("solene")
                .build();

        LegendaResponse response = geminiService.gerarLegenda(request);

        assertThat(response).isNotNull();
        // Garantia da RN03
        assertThat(response.getBody()).contains("https://wa.me/5511988887777");
        assertThat(response.getBody()).contains("Blackwork");
        assertThat(response.getBody()).contains("Costas");

        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve acionar fallback automático com RN03 quando a chave da API estiver vazia")
    void deveAcionarFallbackQuandoApiKeyEstiverVazia() {
        GeminiService serviceSemApiKey = new GeminiService(restTemplate, "", API_URL);

        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .estilo("Realismo")
                .localizacao("Braço")
                .whatsappLink("https://wa.me/5511977776666")
                .tom("chiaroscuro")
                .build();

        LegendaResponse response = serviceSemApiKey.gerarLegenda(request);

        assertThat(response).isNotNull();
        assertThat(response.isGeradoPorIa()).isFalse();
        assertThat(response.isFallback()).isTrue();
        assertThat(response.getTitle()).contains("Chiaroscuro");
        assertThat(response.getBody()).contains("Realismo");
        assertThat(response.getBody()).contains("Braço");
        assertThat(response.getBody()).contains("https://wa.me/5511977776666");
        assertThat(response.getHashtags()).isNotEmpty();
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve acionar fallback com RN03 quando Gemini retornar erro 500 sem quebrar a requisição")
    void deveAcionarFallbackQuandoGeminiRetornarErroServidor() {
        mockServer.expect(requestTo(API_URL + "?key=" + API_KEY))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"SERVER_OVERLOADED\"}"));

        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .estilo("OldSchool")
                .localizacao("Perna")
                .whatsappLink("https://wa.me/5511955554444")
                .tom("story")
                .build();

        LegendaResponse response = geminiService.gerarLegenda(request);

        assertThat(response).isNotNull();
        assertThat(response.isFallback()).isTrue();
        assertThat(response.getBody()).contains("OldSchool");
        assertThat(response.getBody()).contains("Perna");
        assertThat(response.getBody()).contains("https://wa.me/5511955554444");

        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve respeitar os tons solene e story no fallback")
    void deveRespeitarDiferentesTonsNoFallback() {
        GeminiService serviceSemApiKey = new GeminiService(restTemplate, "", API_URL);

        // Teste tom Solene
        LegendaResponse solene = serviceSemApiKey.gerarLegenda(GerarLegendaRequest.builder()
                .tom("solene")
                .estilo("Oriental")
                .localizacao("Ombro")
                .whatsappLink("https://wa.me/5511944443333")
                .build());

        assertThat(solene.getTitle()).contains("Registro Notarial");
        assertThat(solene.getBody()).contains("Oriental");
        assertThat(solene.getBody()).contains("Ombro");
        assertThat(solene.getBody()).contains("https://wa.me/5511944443333");

        // Teste tom Story
        LegendaResponse story = serviceSemApiKey.gerarLegenda(GerarLegendaRequest.builder()
                .tom("story")
                .estilo("Aquarela")
                .localizacao("Costela")
                .whatsappLink("https://wa.me/5511933332222")
                .build());

        assertThat(story.getTitle()).contains("Memória Gravada na Derme");
        assertThat(story.getBody()).contains("Aquarela");
        assertThat(story.getBody()).contains("Costela");
        assertThat(story.getBody()).contains("https://wa.me/5511933332222");
    }
}

