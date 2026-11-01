package com.tattooflow.modules.midia;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/midias", "/midias"})
@Tag(name = "Mídias", description = "Endpoints para gerenciamento e upload de fotos das tatuagens (F4-BACK-01)")
public class MidiaController {

    private final MidiaService midiaService;

    public MidiaController(MidiaService midiaService) {
        this.midiaService = midiaService;
    }

    @GetMapping("/agendamento/{agendamentoId}")
    @Operation(summary = "Listar mídias por agendamento")
    public ResponseEntity<ApiResponse<List<Midia>>> listarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(ApiResponse.success(midiaService.listarPorAgendamento(agendamentoId)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Endpoint de upload de foto da tatuagem para Cloudinary com validação de termo e limite de 10MB [F4-BACK-01]")
    public ResponseEntity<ApiResponse<Midia>> uploadMidia(
            @Parameter(description = "Arquivo da imagem (máx 10MB)")
            @RequestParam(value = "file", required = false) MultipartFile file,
            @Parameter(description = "Arquivo da imagem alternativo")
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @Parameter(description = "ID do agendamento vinculado", required = true)
            @RequestParam("agendamentoId") Long agendamentoId,
            @Parameter(description = "Tipo da mídia (ex: FOTO_DEPOIS, FOTO_ANTES, REFERENCIA)")
            @RequestParam(value = "tipo", required = false, defaultValue = "FOTO_DEPOIS") String tipo
    ) {
        MultipartFile multipartFile = (file != null) ? file : arquivo;
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("O arquivo de imagem é obrigatório e não pode estar vazio.");
        }

        Midia midiaSalva = midiaService.uploadMidia(multipartFile, agendamentoId, tipo);
        return ResponseEntity.ok(ApiResponse.success("Mídia enviada com sucesso", midiaSalva));
    }

    @PostMapping
    @Operation(summary = "Registrar mídia existente")
    public ResponseEntity<ApiResponse<Midia>> salvarMidia(@RequestBody Midia midia) {
        return ResponseEntity.ok(ApiResponse.success("Mídia enviada com sucesso", midiaService.salvar(midia)));
    }

    @PostMapping({"/legenda", "/gerar-legenda"})
    @Operation(summary = "Gerar legenda de foto de tatuagem via Gemini API com conformidade RN03 [F4-BACK-02, UC03.1]")
    public ResponseEntity<ApiResponse<LegendaResponse>> gerarLegenda(
            @RequestBody(required = false) GerarLegendaRequest request
    ) {
        if (request == null) {
            request = new GerarLegendaRequest();
        }
        LegendaResponse response = midiaService.gerarLegenda(request);
        return ResponseEntity.ok(ApiResponse.success("Legenda gerada com sucesso", response));
    }

    @PostMapping({"/{id}/legenda", "/{id}/gerar-legenda"})
    @Operation(summary = "Gerar legenda para uma mídia específica salva [F4-BACK-02, UC03.1]")
    public ResponseEntity<ApiResponse<LegendaResponse>> gerarLegendaPorMidia(
            @PathVariable("id") Long id,
            @RequestBody(required = false) GerarLegendaRequest request
    ) {
        if (request == null) {
            request = new GerarLegendaRequest();
        }
        request.setMidiaId(id);
        LegendaResponse response = midiaService.gerarLegenda(request);
        return ResponseEntity.ok(ApiResponse.success("Legenda gerada com sucesso", response));
    }
}
