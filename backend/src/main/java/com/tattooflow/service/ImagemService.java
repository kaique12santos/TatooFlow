package com.tattooflow.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.tattooflow.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Serviço responsável pelo upload e processamento de imagens no Cloudinary [F4-INFRA-01].
 */
@Service
public class ImagemService {

    private static final Logger log = LoggerFactory.getLogger(ImagemService.class);

    private final Cloudinary cloudinary;

    public ImagemService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Realiza o upload de um MultipartFile para o Cloudinary, convertendo a imagem
     * automaticamente para o formato WebP e aplicando otimização de qualidade (q_auto),
     * preparando a imagem leve para ser lida pelo Gemini.
     *
     * @param file Arquivo recebido da requisição
     * @return URL segura (secure_url) da imagem salva no Cloudinary
     */
    public String uploadImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("O arquivo de imagem é obrigatório e não pode estar vazio.");
        }

        try {
            Map<?, ?> uploadOptions = ObjectUtils.asMap(
                    "format", "webp",
                    "quality", "auto",
                    "transformation", new Transformation().fetchFormat("webp").quality("auto")
            );

            log.info("Iniciando upload de imagem para o Cloudinary (Tamanho: {} bytes, Nome original: {})...",
                    file.getSize(), file.getOriginalFilename());

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new BusinessException("Resposta do Cloudinary não conteve uma URL válida para a imagem.");
            }

            log.info("Upload concluído com sucesso. URL segura gerada: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Erro de I/O ao ler bytes da imagem para upload no Cloudinary", e);
            throw new BusinessException("Erro ao processar imagem para envio: " + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado durante upload de imagem no Cloudinary", e);
            throw new BusinessException("Falha na comunicação com o Cloudinary: " + e.getMessage());
        }
    }
}
