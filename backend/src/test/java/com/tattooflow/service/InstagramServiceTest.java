package com.tattooflow.service;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.postagem.PublicacaoResultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InstagramServiceTest {

    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;
    private InstagramService instagramService;

    private static final String ACCOUNT_ID = "17841416931904965";
    private static final String ACCESS_TOKEN = "EAAB_TEST_TOKEN";
    private static final String API_VERSION = "v21.0";

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        instagramService = new InstagramService(restTemplate, ACCOUNT_ID, ACCESS_TOKEN, API_VERSION);
    }

    @Test
    void deveTestarConexaoComSucesso() {
        String mockResponse = """
                {
                  "id": "17841416931904965",
                  "username": "tattooflow_estudio"
                }
                """;

        String expectedUrl = "https://graph.facebook.com/v21.0/" + ACCOUNT_ID + "?fields=id,username&access_token=" + ACCESS_TOKEN;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        String resposta = instagramService.testarConexao();

        assertThat(resposta).isNotNull();
        assertThat(resposta).contains("tattooflow_estudio");
        assertThat(resposta).contains("17841416931904965");
        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoQuandoAccountIdEstiverAusente() {
        InstagramService serviceSemId = new InstagramService(restTemplate, "", ACCESS_TOKEN, API_VERSION);

        assertThatThrownBy(serviceSemId::testarConexao)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("instagram.account-id");
    }

    @Test
    void deveLancarExcecaoQuandoAccessTokenEstiverAusente() {
        InstagramService serviceSemToken = new InstagramService(restTemplate, ACCOUNT_ID, "", API_VERSION);

        assertThatThrownBy(serviceSemToken::testarConexao)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("instagram.access-token");
    }

    @Test
    void deveLancarExcecaoQuandoInstagramRetornarErroHttp() {
        String expectedUrl = "https://graph.facebook.com/v21.0/" + ACCOUNT_ID + "?fields=id,username&access_token=" + ACCESS_TOKEN;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":{\"message\":\"Invalid OAuth access token.\",\"type\":\"OAuthException\",\"code\":190}}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> instagramService.testarConexao())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("400");

        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve publicar foto no Instagram executando container creation e media_publish")
    void devePublicarFotoComSucessoEmDuasEtapas() {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1/tatoo.webp";
        String legenda = "Tatuagem FineLine Chiaroscuro #TattooFlow";

        String expectedContainerUrl = "https://graph.facebook.com/v21.0/" + ACCOUNT_ID + "/media?image_url=" + imageUrl +
                "&caption=Tatuagem%20FineLine%20Chiaroscuro%20%23TattooFlow&access_token=" + ACCESS_TOKEN;

        // Etapa 1: Container Creation
        mockServer.expect(requestTo(expectedContainerUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\": \"container_998877\"}", MediaType.APPLICATION_JSON));

        // Etapa 2: Media Publish
        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/" + ACCOUNT_ID + "/media_publish?creation_id=container_998877&access_token=" + ACCESS_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\": \"media_112233\"}", MediaType.APPLICATION_JSON));

        PublicacaoResultado resultado = instagramService.publicarFoto(imageUrl, legenda);

        assertThat(resultado).isNotNull();
        assertThat(resultado.isSucesso()).isTrue();
        assertThat(resultado.getContainerId()).isEqualTo("container_998877");
        assertThat(resultado.getMediaId()).isEqualTo("media_112233");
        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve retornar falha quando Instagram Graph API rejeitar criacao do container")
    void deveRetornarFalhaQuandoCriacaoDoContainerFalhar() {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1/tatoo.webp";
        String legenda = "Legenda";

        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/" + ACCOUNT_ID + "/media?image_url=" + imageUrl + "&caption=" + legenda + "&access_token=" + ACCESS_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":{\"message\":\"Invalid image URL\",\"type\":\"OAuthException\",\"code\":100}}")
                        .contentType(MediaType.APPLICATION_JSON));

        PublicacaoResultado resultado = instagramService.publicarFoto(imageUrl, legenda);

        assertThat(resultado).isNotNull();
        assertThat(resultado.isSucesso()).isFalse();
        assertThat(resultado.getMensagemErro()).contains("400");
        assertThat(resultado.getMensagemErro()).contains("Invalid image URL");
        mockServer.verify();
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve validar campos obrigatorios antes de chamar API")
    void deveValidarCamposObrigatorios() {
        assertThatThrownBy(() -> instagramService.publicarFoto("", "Legenda"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("URL da imagem é obrigatória");

        assertThatThrownBy(() -> instagramService.publicarFoto("https://imagem.jpg", ""))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("legenda é obrigatória");
    }
}

