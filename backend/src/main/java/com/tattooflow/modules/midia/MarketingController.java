package com.tattooflow.modules.midia;

import com.tattooflow.modules.postagem.AgendarPostagemRequest;
import com.tattooflow.modules.postagem.PostagemAgendadaResponse;
import com.tattooflow.modules.postagem.PostagemService;
import com.tattooflow.service.InstagramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller de Marketing e Curadoria IA [F4-BACK-02, F4-BACK-03, UC03.1, UC03.2].
 * Atende diretamente os fluxos de geração de legenda e distribuição no Instagram do frontend (useMarketingStore).
 */
@RestController
@RequestMapping({"/api/marketing", "/marketing"})
@Tag(name = "Marketing", description = "Endpoints para curadoria de arte via IA e distribuição no Instagram [F4-BACK-02, F4-BACK-03]")
public class MarketingController {

    private final MidiaService midiaService;
    private final PostagemService postagemService;
    private final InstagramService instagramService;

    public MarketingController(MidiaService midiaService,
                               PostagemService postagemService,
                               InstagramService instagramService) {
        this.midiaService = midiaService;
        this.postagemService = postagemService;
        this.instagramService = instagramService;
    }

    @PostMapping("/curadoria-ia")
    @Operation(summary = "Geração ou refinamento de legenda via IA Gemini com conformidade RN03 [F4-BACK-02, UC03.1]")
    public ResponseEntity<LegendaResponse> curadoriaIa(
            @RequestBody(required = false) GerarLegendaRequest request
    ) {
        if (request == null) {
            request = new GerarLegendaRequest();
        }
        LegendaResponse response = midiaService.gerarLegenda(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/curadoria")
    @Operation(summary = "Sincroniza rascunho de curadoria da obra com IA [F4-BACK-02]")
    public ResponseEntity<LegendaResponse> obterCuradoria(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "agendamentoId", required = false) Long agendamentoId
    ) {
        GerarLegendaRequest request = new GerarLegendaRequest();
        if (agendamentoId != null) {
            request.setAgendamentoId(agendamentoId);
        } else if (sessionId != null && sessionId.matches("\\d+")) {
            request.setAgendamentoId(Long.valueOf(sessionId));
        }
        LegendaResponse response = midiaService.gerarLegenda(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule")
    @Operation(summary = "Agendar ou publicar postagem no Instagram [F4-BACK-03, UC03.2]")
    public ResponseEntity<PostagemAgendadaResponse> agendarPublicacao(
            @RequestBody AgendarPostagemRequest request
    ) {
        PostagemAgendadaResponse response = postagemService.agendarPostagem(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/distribuicao/settings")
    @Operation(summary = "Obter configurações e contas de distribuição do Instagram [F4-BACK-03]")
    public ResponseEntity<Map<String, Object>> obterConfiguracoesDistribuicao() {
        String accountId = (instagramService.getAccountId() != null && !instagramService.getAccountId().isBlank())
                ? instagramService.getAccountId() : "acc-inst-01";

        List<Map<String, Object>> accounts = List.of(
                Map.of(
                        "id", accountId,
                        "handle", "@atelier.dornelles",
                        "monogram", "GD",
                        "platform", "instagram",
                        "statusToken", "ATIVO (TOKEN VÁLIDO)",
                        "isValid", true,
                        "engagementPeakTime", "21:30",
                        "engagementPeakDay", "Sexta-feira"
                )
        );

        Map<String, Object> settings = Map.of(
                "accounts", accounts,
                "activeAccountId", accountId,
                "protocolNumber", "TATTOO-MKT-2025-0523",
                "channels", Map.of(
                        "feed", true,
                        "reels", false,
                        "stories", false,
                        "dossie", false
                )
        );

        return ResponseEntity.ok(settings);
    }

    @PostMapping("/instagram/switch")
    @Operation(summary = "Alternar conta social vinculada [F4-BACK-03]")
    public ResponseEntity<Map<String, Object>> alternarContaInstagram(@RequestBody Map<String, String> body) {
        String accountId = body.getOrDefault("accountId", "acc-inst-01");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "activeAccountId", accountId
        ));
    }
}
