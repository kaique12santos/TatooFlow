package com.tattooflow.service;

import com.tattooflow.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de teste temporário para validação de infraestrutura (Gemini, Cloudinary e Instagram).
 */
@RestController
@RequestMapping("/api/infra")
public class InfraTesteController {

    private static final Logger log = LoggerFactory.getLogger(InfraTesteController.class);

    private final GeminiService geminiService;
    private final ImagemService imagemService;
    private final InstagramService instagramService;

    public InfraTesteController(GeminiService geminiService, ImagemService imagemService, InstagramService instagramService) {
        this.geminiService = geminiService;
        this.imagemService = imagemService;
        this.instagramService = instagramService;
    }

    @GetMapping("/teste")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executarTeste() {
        Map<String, Object> resultados = new LinkedHashMap<>();

        // 1. Chamada ao GeminiService
        try {
            String geminiResposta = geminiService.testarComunicacao("Ping de teste da infraestrutura");
            resultados.put("geminiStatus", "200 OK");
            resultados.put("geminiResposta", geminiResposta);
        } catch (Exception e) {
            resultados.put("geminiStatus", "ERRO: " + e.getMessage());
        }

        // 2. Upload dummy no Cloudinary
        try {
            byte[] dummyBytes = new byte[] {
                (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61,
                (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x80, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x21, (byte) 0xF9, (byte) 0x04, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0x02, (byte) 0x02, (byte) 0x44, (byte) 0x01, (byte) 0x00,
                (byte) 0x3B
            };
            MultipartFile dummyFile = new DummyMultipartFile("file", "teste_dummy.gif", "image/gif", dummyBytes);
            String secureUrl = imagemService.uploadImagem(dummyFile);
            resultados.put("cloudinaryStatus", "UPLOAD REALIZADO COM SUCESSO");
            resultados.put("cloudinarySecureUrl", secureUrl);
        } catch (Exception e) {
            resultados.put("cloudinaryStatus", "ERRO: " + e.getMessage());
        }

        // 3. Chamada ao Instagram Graph API
        try {
            String instagramResposta = instagramService.testarConexao();
            resultados.put("instagramStatus", "200 OK");
            resultados.put("instagramResposta", instagramResposta);
        } catch (Exception e) {
            resultados.put("instagramStatus", "ERRO: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Validações de infraestrutura executadas.", resultados));
    }

    @GetMapping("/instagram")
    public ResponseEntity<ApiResponse<String>> testarInstagram() {
        String resposta = instagramService.testarConexao();
        return ResponseEntity.ok(ApiResponse.success("Conexão com Instagram Graph API realizada com sucesso!", resposta));
    }
}
