package com.tattooflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.midia.GerarLegendaRequest;
import com.tattooflow.modules.midia.LegendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Serviço de integração com a API do Google Gemini [F4-INFRA-01, F4-BACK-02, UC03.1, RN03].
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    @Value("${tattooflow.whatsapp.link:${WHATSAPP_LINK:https://wa.me/5511999999999}}")
    private String defaultWhatsappLink;

    @Value("${tattooflow.studio.location:São Paulo - SP}")
    private String defaultLocation;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public GeminiService(RestTemplate restTemplate, String apiKey, String apiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Autowired
    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
    }

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper, String apiKey, String apiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    /**
     * Gera automaticamente uma legenda para postagem no Instagram com base na imagem e estilo da tatuagem [F4-BACK-02].
     *
     * Regras e Critérios:
     * - Chamada à Gemini API com contexto da imagem/estilo (UC03.1)
     * - Legenda obrigatoriamente contém link do WhatsApp, estilo e localização (RN03)
     * - Fallback robusto caso a IA fique indisponível
     *
     * @param request Dados da requisição contendo contexto, imagem, estilo, localização e tom
     * @return LegendaResponse com texto gerado e garantias da RN03
     */
    public LegendaResponse gerarLegenda(GerarLegendaRequest request) {
        if (request == null) {
            request = new GerarLegendaRequest();
        }

        // 1. Extração e sanitização de parâmetros com valores padrão
        String estilo = extrairEstilo(request);
        String localizacao = extrairLocalizacao(request);
        String whatsappLink = extrairWhatsapp(request);
        String tom = (request.getTom() != null && !request.getTom().isBlank())
                ? request.getTom().trim().toLowerCase() : "chiaroscuro";
        String imageUrl = request.getImageUrl();
        String artista = extrairArtista(request);
        String descricao = extrairDescricao(request);

        // 2. Se a chave da API não estiver configurada, aciona fallback com conformidade RN03
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Chave da Gemini API não configurada. Acionando fallback automático de legenda (RN03).");
            return gerarLegendaFallback(estilo, localizacao, whatsappLink, tom, request.getClient());
        }

        try {
            // 3. Montagem do prompt com contexto da obra e regras estritas da RN03
            String prompt = construirPrompt(estilo, localizacao, whatsappLink, tom, imageUrl, artista, descricao);

            String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("key", apiKey)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("x-goog-api-key", apiKey);

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> parts = Map.of("parts", List.of(textPart));
            Map<String, Object> payload = Map.of("contents", List.of(parts));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            log.info("Chamando Gemini API para geração de legenda. Estilo: {}, Tom: {}", estilo, tom);
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String textoResposta = extrairTextoDaRespostaGemini(response.getBody());
                if (textoResposta != null && !textoResposta.isBlank()) {
                    return formatarRespostaGemini(textoResposta, estilo, localizacao, whatsappLink, tom, request.getClient());
                }
            }

            log.warn("Resposta vazia da Gemini API. Acionando fallback (RN03).");
            return gerarLegendaFallback(estilo, localizacao, whatsappLink, tom, request.getClient());

        } catch (HttpStatusCodeException e) {
            log.warn("Erro HTTP [{}] da Gemini API: {}. Acionando fallback de legenda (RN03).",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return gerarLegendaFallback(estilo, localizacao, whatsappLink, tom, request.getClient());
        } catch (Exception e) {
            log.warn("Falha inesperada ao comunicar com a Gemini API: {}. Acionando fallback automático (RN03).", e.getMessage());
            return gerarLegendaFallback(estilo, localizacao, whatsappLink, tom, request.getClient());
        }
    }

    /**
     * Gera legenda de contingência quando a API de IA estiver inacessível.
     * Atende 100% aos critérios de aceite [F4-BACK-02] e à regra RN03.
     */
    public LegendaResponse gerarLegendaFallback(String estilo, String localizacao, String whatsappLink, String tom, Map<String, Object> client) {
        String tomNormalizado = (tom != null) ? tom.toLowerCase().trim() : "chiaroscuro";
        String title;
        String body;
        List<String> hashtags;

        String tagEstilo = "#" + normalizarHashtag(estilo);

        switch (tomNormalizado) {
            case "solene":
                title = "Registro Notarial de Obra · Acervo " + estilo;
                body = "Lavrado em sessão presencial de alta precisão técnica. A representação clássica traduzida com pigmentos de grau cirúrgico no estilo " + estilo + ".\n\n📍 Localização: " + localizacao + "\n🎨 Estilo: " + estilo + "\n\n📲 Agendamentos e orçamentos via WhatsApp: " + whatsappLink;
                hashtags = List.of("#TattooFlow", tagEstilo, "#NotaryTattoo", "#FineArtTattoo", "#TattooArte");
                break;
            case "story":
                title = "Memória Gravada na Derme · A Expressão em " + estilo;
                body = "Mais do que um projeto na pele, uma narrativa de introspecção e arte viva. Horas de dedicação entre a máquina e a derme para eternizar uma perspectiva humana no estilo " + estilo + ".\n\n📍 Localização: " + localizacao + "\n🎨 Estilo: " + estilo + "\n\n📲 Agendamentos e orçamentos via WhatsApp: " + whatsappLink;
                hashtags = List.of("#TattooFlow", tagEstilo, "#TattooJourney", "#InkedMemories", "#ArtOnSkin");
                break;
            case "chiaroscuro":
            default:
                title = "Estudo de Arte · " + estilo + " Chiaroscuro";
                body = "A profundidade do claro-escuro materializada na pele. Estudo anatômico e iluminação dramática executados com rigor técnico no estilo " + estilo + ".\n\n📍 Localização: " + localizacao + "\n🎨 Estilo: " + estilo + "\n\n📲 Agendamentos e orçamentos via WhatsApp: " + whatsappLink;
                hashtags = List.of("#TattooFlow", tagEstilo, "#ChiaroscuroTattoo", "#FineLineAtelier", "#DarkArt", "#TattooStudio");
                break;
        }

        // Assegura rigorosamente RN03
        body = garantirRN03(body, whatsappLink, estilo, localizacao);
        String legendaCompleta = body + "\n\n" + String.join(" ", hashtags);

        return LegendaResponse.builder()
                .title(title)
                .body(body)
                .hashtags(hashtags)
                .legendaCompleta(legendaCompleta)
                .estilo(estilo)
                .localizacao(localizacao)
                .whatsappLink(whatsappLink)
                .geradoPorIa(false)
                .fallback(true)
                .client(client)
                .build();
    }

    /**
     * Validação e aplicação estrita da Regra de Negócio RN03:
     * A legenda gerada deve obrigatoriamente conter link do WhatsApp, estilo e localização.
     */
    public String garantirRN03(String texto, String whatsappLink, String estilo, String localizacao) {
        if (texto == null) {
            texto = "";
        }
        StringBuilder sb = new StringBuilder(texto);

        // 1. Link do WhatsApp (RN03)
        if (whatsappLink != null && !whatsappLink.isBlank() && !texto.contains(whatsappLink)) {
            sb.append("\n\n📲 Agendamentos via WhatsApp: ").append(whatsappLink.trim());
        }

        // 2. Estilo (RN03)
        if (estilo != null && !estilo.isBlank() && !texto.toLowerCase().contains(estilo.toLowerCase().trim())) {
            sb.append("\n🎨 Estilo: ").append(estilo.trim());
        }

        // 3. Localização (RN03)
        if (localizacao != null && !localizacao.isBlank() && !texto.toLowerCase().contains(localizacao.toLowerCase().trim())) {
            sb.append("\n📍 Localização: ").append(localizacao.trim());
        }

        return sb.toString();
    }

    private String construirPrompt(String estilo, String localizacao, String whatsappLink, String tom,
                                  String imageUrl, String artista, String descricao) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é o curador de arte e redator do conceituado estúdio TattooFlow.\n");
        sb.append("Crie uma legenda marcante para publicação no Instagram sobre uma tatuagem finalizada.\n\n");
        sb.append("Contexto da Obra:\n");
        sb.append("- Estilo: ").append(estilo).append("\n");
        sb.append("- Localização (Anatômica / Estúdio): ").append(localizacao).append("\n");
        sb.append("- WhatsApp para Agendamento: ").append(whatsappLink).append("\n");
        if (imageUrl != null && !imageUrl.isBlank()) {
            sb.append("- Imagem da Obra: ").append(imageUrl).append("\n");
        }
        if (artista != null && !artista.isBlank()) {
            sb.append("- Artista / Tatuador: ").append(artista).append("\n");
        }
        if (descricao != null && !descricao.isBlank()) {
            sb.append("- Detalhes da Sessão: ").append(descricao).append("\n");
        }
        sb.append("- Tom Literário: ").append(tom).append(" (ex: chiaroscuro = artístico & denso; solene = clássico & notarial; story = memória & afeto)\n\n");
        sb.append("REQUISITOS OBRIGATÓRIOS DA REGRA DE NEGÓCIO (RN03):\n");
        sb.append("1. A legenda DEVE obrigatoriamente mencionar o estilo da arte: \"").append(estilo).append("\".\n");
        sb.append("2. A legenda DEVE obrigatoriamente mencionar a localização: \"").append(localizacao).append("\".\n");
        sb.append("3. A legenda DEVE obrigatoriamente incluir a chamada para agendamento com o link exato do WhatsApp: \"").append(whatsappLink).append("\".\n");
        sb.append("4. Adicione ao final entre 5 e 10 hashtags temáticas relevantes (#TattooFlow, etc).\n");
        sb.append("Escreva o texto da legenda pronto para publicação.");
        return sb.toString();
    }

    private LegendaResponse formatarRespostaGemini(String textoResposta, String estilo, String localizacao,
                                                   String whatsappLink, String tom, Map<String, Object> client) {
        List<String> hashtags = new ArrayList<>();
        StringBuilder corpoBuilder = new StringBuilder();

        String[] linhas = textoResposta.split("\n");
        for (String linha : linhas) {
            String linhaTrim = linha.trim();
            if (linhaTrim.startsWith("#") && !linhaTrim.startsWith("##")) {
                String[] tags = linhaTrim.split("\\s+");
                for (String t : tags) {
                    if (t.startsWith("#")) {
                        hashtags.add(t);
                    }
                }
            } else {
                corpoBuilder.append(linha).append("\n");
            }
        }

        String corpo = corpoBuilder.toString().trim();
        if (corpo.isEmpty()) {
            corpo = textoResposta.trim();
        }

        // Garante RN03
        corpo = garantirRN03(corpo, whatsappLink, estilo, localizacao);

        if (hashtags.isEmpty()) {
            hashtags = List.of("#TattooFlow", "#" + normalizarHashtag(estilo), "#TattooArt", "#Inked");
        }

        String title = "Estudo de Arte · " + estilo + " (" + tom.toUpperCase() + ")";
        String legendaCompleta = corpo + "\n\n" + String.join(" ", hashtags);

        return LegendaResponse.builder()
                .title(title)
                .body(corpo)
                .hashtags(hashtags)
                .legendaCompleta(legendaCompleta)
                .estilo(estilo)
                .localizacao(localizacao)
                .whatsappLink(whatsappLink)
                .geradoPorIa(true)
                .fallback(false)
                .client(client)
                .build();
    }

    private String extrairTextoDaRespostaGemini(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao desserializar JSON da resposta Gemini: {}", e.getMessage());
        }
        return null;
    }

    private String extrairEstilo(GerarLegendaRequest req) {
        if (req.getEstilo() != null && !req.getEstilo().isBlank()) {
            return req.getEstilo().trim();
        }
        if (req.getClient() != null) {
            Object style = req.getClient().get("artworkStyle");
            if (style == null) style = req.getClient().get("style");
            if (style != null && !style.toString().isBlank()) {
                return style.toString().trim();
            }
        }
        return "FineLine Chiaroscuro";
    }

    private String extrairLocalizacao(GerarLegendaRequest req) {
        if (req.getLocalizacao() != null && !req.getLocalizacao().isBlank()) {
            return req.getLocalizacao().trim();
        }
        if (req.getClient() != null) {
            Object loc = req.getClient().get("station");
            if (loc == null) loc = req.getClient().get("location");
            if (loc != null && !loc.toString().isBlank()) {
                return loc.toString().trim();
            }
        }
        return (defaultLocation != null && !defaultLocation.isBlank()) ? defaultLocation : "São Paulo - SP";
    }

    private String extrairWhatsapp(GerarLegendaRequest req) {
        if (req.getWhatsappLink() != null && !req.getWhatsappLink().isBlank()) {
            return req.getWhatsappLink().trim();
        }
        return (defaultWhatsappLink != null && !defaultWhatsappLink.isBlank())
                ? defaultWhatsappLink : "https://wa.me/5511999999999";
    }

    private String extrairArtista(GerarLegendaRequest req) {
        if (req.getArtista() != null && !req.getArtista().isBlank()) {
            return req.getArtista().trim();
        }
        if (req.getClient() != null) {
            Object artist = req.getClient().get("artistName");
            if (artist == null) artist = req.getClient().get("artist");
            if (artist != null) return artist.toString().trim();
        }
        return null;
    }

    private String extrairDescricao(GerarLegendaRequest req) {
        if (req.getDescricao() != null && !req.getDescricao().isBlank()) {
            return req.getDescricao().trim();
        }
        if (req.getClient() != null) {
            Object title = req.getClient().get("artworkTitle");
            if (title == null) title = req.getClient().get("sessionInfo");
            if (title != null) return title.toString().trim();
        }
        return null;
    }

    private String normalizarHashtag(String texto) {
        if (texto == null || texto.isBlank()) return "TattooArt";
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String semAcentos = pattern.matcher(nfd).replaceAll("");
        return semAcentos.replaceAll("[^a-zA-Z0-9]", "");
    }

    public String testarComunicacao() {
        return testarComunicacao("Teste de conexao com a API do Google Gemini via TattooFlow");
    }

    public String testarComunicacao(String mensagem) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("A chave da API do Google Gemini (gemini.api-key) não está configurada.");
        }

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("key", apiKey)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("x-goog-api-key", apiKey);

            Map<String, Object> textPart = Map.of("text", (mensagem != null && !mensagem.isBlank()) ? mensagem : "Ping");
            Map<String, Object> parts = Map.of("parts", List.of(textPart));
            Map<String, Object> payload = Map.of("contents", List.of(parts));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            log.info("Disparando chamada para a API do Google Gemini no endpoint: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Comunicação com a API do Google Gemini realizada com sucesso. Status: {}", response.getStatusCode());
                return response.getBody();
            } else {
                throw new BusinessException("Falha ao comunicar com a API do Gemini. Status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Erro retornado pela API do Gemini: HTTP {} - Resposta: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Erro na API do Google Gemini [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado na chamada ao Google Gemini", e);
            throw new BusinessException("Erro inesperado ao conectar com a API do Google Gemini: " + e.getMessage());
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
