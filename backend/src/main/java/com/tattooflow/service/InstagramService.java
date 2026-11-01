package com.tattooflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.postagem.PublicacaoResultado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Serviço responsável pela integração com a Instagram Graph API [F4-INFRA-02, F4-BACK-03, UC03.2].
 * Suporta teste de conexão e fluxo de publicação em duas etapas (container creation + publish).
 */
@Service
public class InstagramService {

    private static final Logger log = LoggerFactory.getLogger(InstagramService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${instagram.account-id:${INSTAGRAM_ACCOUNT_ID:}}")
    private String accountId;

    @Value("${instagram.access-token:${INSTAGRAM_ACCESS_TOKEN:}}")
    private String accessToken;

    @Value("${instagram.api-version:${INSTAGRAM_API_VERSION:v21.0}}")
    private String apiVersion;

    public InstagramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public InstagramService(RestTemplate restTemplate, String accountId, String accessToken, String apiVersion) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.apiVersion = apiVersion;
    }

    @Autowired
    public InstagramService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
    }

    public InstagramService(RestTemplate restTemplate, ObjectMapper objectMapper, String accountId, String accessToken, String apiVersion) {
        this.restTemplate = restTemplate;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.apiVersion = apiVersion;
    }

    /**
     * Testa a conectividade com a Instagram Graph API consultando os dados básicos da conta configurada (id, username).
     *
     * GET https://graph.facebook.com/{apiVersion}/{accountId}?fields=id,username&access_token={accessToken}
     *
     * @return Corpo da resposta JSON retornado pela Graph API
     */
    public String testarConexao() {
        validarCredenciais();

        try {
            String baseUrl = String.format("https://graph.facebook.com/%s/%s", getVersaoEfetiva(), accountId);

            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("fields", "id,username")
                    .queryParam("access_token", accessToken)
                    .toUriString();

            log.info("Disparando requisição de teste para o Instagram Graph API: id={}", accountId);
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Comunicação com o Instagram Graph API concluída com sucesso. Status: {}", response.getStatusCode());
                return response.getBody();
            } else {
                throw new BusinessException("Resposta inesperada do Instagram Graph API. Status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Erro retornado pelo Instagram Graph API: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Erro na Instagram Graph API [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Falha inesperada ao comunicar com o Instagram Graph API", e);
            throw new BusinessException("Falha inesperada ao conectar com a Instagram Graph API: " + e.getMessage());
        }
    }

    /**
     * Executa a publicação de uma foto no Instagram através do fluxo oficial da Graph API [F4-BACK-03]:
     * 1. Criação do container de mídia (POST /{accountId}/media)
     * 2. Publicação do container (POST /{accountId}/media_publish)
     *
     * @param imageUrl URL pública da imagem (Cloudinary)
     * @param legenda Texto da legenda da postagem (com hashtags e RN03)
     * @return PublicacaoResultado contendo o status, containerId, mediaId e detalhes da resposta
     */
    public PublicacaoResultado publicarFoto(String imageUrl, String legenda) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException("A URL da imagem é obrigatória para publicação no Instagram.");
        }
        if (legenda == null || legenda.isBlank()) {
            throw new BusinessException("A legenda é obrigatória para publicação no Instagram.");
        }

        validarCredenciais();

        try {
            // ===================================================================
            // ETAPA 1: Criação do Container de Mídia (POST /{accountId}/media)
            // ===================================================================
            String mediaContainerBaseUrl = String.format("https://graph.facebook.com/%s/%s/media",
                    getVersaoEfetiva(), accountId);

            URI mediaUri = UriComponentsBuilder.fromHttpUrl(mediaContainerBaseUrl)
                    .queryParam("image_url", imageUrl)
                    .queryParam("caption", legenda)
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUri();

            log.info("Iniciando publicação no Instagram Graph API. Etapa 1: Criar Container. AccountId={}", accountId);
            ResponseEntity<String> containerResponse = restTemplate.postForEntity(mediaUri, null, String.class);

            if (!containerResponse.getStatusCode().is2xxSuccessful() || containerResponse.getBody() == null) {
                log.error("Falha ao criar container no Instagram: Status={}", containerResponse.getStatusCode());
                return PublicacaoResultado.falha("Falha na criação do container no Instagram. HTTP " + containerResponse.getStatusCode(),
                        containerResponse.getBody());
            }

            JsonNode containerJson = objectMapper.readTree(containerResponse.getBody());
            String containerId = containerJson.path("id").asText();

            if (containerId == null || containerId.isBlank()) {
                log.error("Resposta do container não continha ID: {}", containerResponse.getBody());
                return PublicacaoResultado.falha("Container criado não retornou ID válido.", containerResponse.getBody());
            }

            log.info("Container criado com sucesso no Instagram Graph API: containerId={}", containerId);

            // ===================================================================
            // ETAPA 2: Publicação do Container (POST /{accountId}/media_publish)
            // ===================================================================
            String publishBaseUrl = String.format("https://graph.facebook.com/%s/%s/media_publish",
                    getVersaoEfetiva(), accountId);

            URI publishUri = UriComponentsBuilder.fromHttpUrl(publishBaseUrl)
                    .queryParam("creation_id", containerId)
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUri();

            log.info("Etapa 2: Publicando Container no Instagram Feed. ContainerId={}", containerId);
            ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUri, null, String.class);

            if (!publishResponse.getStatusCode().is2xxSuccessful() || publishResponse.getBody() == null) {
                log.error("Falha ao publicar container no Instagram: Status={}", publishResponse.getStatusCode());
                return PublicacaoResultado.falha("Falha ao publicar container no Instagram. HTTP " + publishResponse.getStatusCode(),
                        publishResponse.getBody());
            }

            JsonNode publishJson = objectMapper.readTree(publishResponse.getBody());
            String mediaId = publishJson.path("id").asText();

            log.info("Publicação concluída com sucesso no Instagram Graph API! MediaId={}, ContainerId={}", mediaId, containerId);

            return PublicacaoResultado.sucesso(containerId, mediaId, publishResponse.getBody());

        } catch (HttpStatusCodeException e) {
            String erroDetalhes = e.getResponseBodyAsString();
            log.error("Erro HTTP [{}] da Instagram Graph API durante publicação: {}", e.getStatusCode(), erroDetalhes);
            return PublicacaoResultado.falha("Erro HTTP [" + e.getStatusCode() + "] da Graph API: " + erroDetalhes, erroDetalhes);
        } catch (Exception e) {
            log.error("Exceção inesperada ao publicar foto no Instagram Graph API", e);
            return PublicacaoResultado.falha("Exceção inesperada: " + e.getMessage(), null);
        }
    }

    private void validarCredenciais() {
        if (accountId == null || accountId.isBlank()) {
            throw new BusinessException("O identificador da conta do Instagram (instagram.account-id) não está configurado.");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException("O token de acesso do Instagram (instagram.access-token) não está configurado.");
        }
    }

    private String getVersaoEfetiva() {
        return (apiVersion != null && !apiVersion.isBlank()) ? apiVersion : "v21.0";
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
