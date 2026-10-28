package com.tattooflow.modules.termo;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

@Service
public class TermoStorageService {

    public static final long LIMITE_MAXIMO_BYTES = 5 * 1024 * 1024; // 5MB (RES02)
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{'%', 'P', 'D', 'F', '-'};

    private final Path baseStoragePath;

    public TermoStorageService(@Value("${termo.storage.dir:storage/termos}") String storageDir) {
        this.baseStoragePath = Paths.get(storageDir).toAbsolutePath().normalize();
        inicializarDiretorio();
    }

    private void inicializarDiretorio() {
        try {
            if (!Files.exists(baseStoragePath)) {
                Files.createDirectories(baseStoragePath);
            }
        } catch (IOException e) {
            throw new BusinessException("Falha ao inicializar diretório de armazenamento seguro de termos: " + e.getMessage());
        }
    }

    /**
     * Armazena com segurança o arquivo PDF do termo de consentimento.
     * Valida tamanho máximo de 5MB (RES02) e assinatura do arquivo PDF.
     *
     * @param termoId     Identificador do termo
     * @param conteudoPdf Conteúdo em bytes do PDF
     * @return Caminho relativo seguro do arquivo armazenado
     */
    public String armazenar(Long termoId, byte[] conteudoPdf) {
        validarConteudoPdf(conteudoPdf);

        String hash = calcularSha256(conteudoPdf).substring(0, 12);
        String nomeArquivo = String.format("termo_%d_%s.pdf", termoId != null ? termoId : 0L, hash);

        Path caminhoAlvo = resolverCaminhoSeguro(nomeArquivo);

        try {
            Files.write(caminhoAlvo, conteudoPdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return baseStoragePath.relativize(caminhoAlvo).toString().replace('\\', '/');
        } catch (IOException e) {
            throw new BusinessException("Erro ao armazenar arquivo PDF do termo com segurança: " + e.getMessage());
        }
    }

    /**
     * Carrega os bytes do arquivo PDF com validação rigorosa de segurança contra Path Traversal.
     *
     * @param caminho Caminho relativo ou nome do arquivo
     * @return Bytes do arquivo PDF
     */
    public byte[] carregar(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            throw new BusinessException("Caminho do arquivo não pode ser vazio.");
        }

        Path caminhoArquivo = resolverCaminhoSeguro(caminho);

        if (!Files.exists(caminhoArquivo) || !Files.isRegularFile(caminhoArquivo)) {
            throw new ResourceNotFoundException("Arquivo do termo não encontrado no armazenamento: " + caminho);
        }

        try {
            byte[] bytes = Files.readAllBytes(caminhoArquivo);
            validarConteudoPdf(bytes);
            return bytes;
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler arquivo do termo armazenado: " + e.getMessage());
        }
    }

    public boolean existe(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return false;
        }
        try {
            Path caminhoArquivo = resolverCaminhoSeguro(caminho);
            return Files.exists(caminhoArquivo) && Files.isRegularFile(caminhoArquivo);
        } catch (Exception e) {
            return false;
        }
    }

    public void excluir(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }
        try {
            Path caminhoArquivo = resolverCaminhoSeguro(caminho);
            Files.deleteIfExists(caminhoArquivo);
        } catch (IOException ignored) {
        }
    }

    /**
     * Valida os critérios de aceite:
     * - Tamanho máximo de 5MB (RES02)
     * - Arquivo não nulo e não vazio
     * - Arquivo com assinatura válida de PDF
     */
    public void validarConteudoPdf(byte[] conteudoPdf) {
        if (conteudoPdf == null || conteudoPdf.length == 0) {
            throw new BusinessException("O conteúdo do documento PDF é obrigatório.");
        }

        // RES02: Validação de tamanho máximo de 5MB
        if (conteudoPdf.length > LIMITE_MAXIMO_BYTES) {
            throw new BusinessException("RES02: O arquivo do termo de consentimento excede o limite máximo permitido de 5 MB ("
                    + conteudoPdf.length + " bytes).");
        }

        if (!eFormatoPdfValido(conteudoPdf)) {
            throw new BusinessException("O arquivo informado não é um documento PDF válido.");
        }
    }

    private boolean eFormatoPdfValido(byte[] conteudo) {
        if (conteudo.length < PDF_MAGIC_BYTES.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOf(conteudo, PDF_MAGIC_BYTES.length), PDF_MAGIC_BYTES);
    }

    /**
     * Prevenção de Path Traversal: Garante que o arquivo acessado está estritamente
     * contido no diretório base de armazenamento configurado.
     */
    public Path resolverCaminhoSeguro(String caminhoInformado) {
        String nomeSanitizado = caminhoInformado.replace('\\', '/').trim();
        // Remove possíveis prefixos como storage/termos/ se passados
        if (nomeSanitizado.startsWith("storage/termos/")) {
            nomeSanitizado = nomeSanitizado.substring("storage/termos/".length());
        }

        Path caminhoResolvido = baseStoragePath.resolve(nomeSanitizado).normalize();

        if (!caminhoResolvido.startsWith(baseStoragePath)) {
            throw new BusinessException("Tentativa de acesso a caminho não autorizado (Path Traversal detectado): " + caminhoInformado);
        }

        return caminhoResolvido;
    }

    /**
     * Calcula o hash criptográfico SHA-256 do arquivo para comprovação de integridade e validade jurídica (RN04).
     */
    public String calcularSha256(byte[] conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(conteudo);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao instanciar algoritmo SHA-256", e);
        }
    }

    public Path getBaseStoragePath() {
        return baseStoragePath;
    }
}

