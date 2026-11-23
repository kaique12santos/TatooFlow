package com.tattooflow.common.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário auxiliar de sanitização e filtragem de legendas geradas por IA (Gemini).
 *
 * Garante que a legenda esteja em texto puro (plain text), em estrita conformidade com
 * os padrões do Instagram Graph API, eliminando formatações Markdown (negrito **, itálico *,
 * cabeçalhos ##, blocos de código, tags de template, etc.) que o Instagram não renderiza.
 *
 * Princípio de Engenharia: Defesa em Profundidade (Prompt Refinement + Backend Sanitization + Frontend Sanitization).
 */
public final class LegendaSanitizer {

    // 1. Remove blocos de código Markdown (```código```)
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:[a-zA-Z0-9_-]+)?\\s*\\n?([\\s\\S]*?)```");

    // 2. Remove código inline (`código`)
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`\\n]+)`");

    // 3. Remove cabeçalhos Markdown (# Título, ## Seção, etc.), preservando hashtags (#Hashtag)
    // Uma hashtag não tem espaço logo após o '#' (#TattooFlow), enquanto cabeçalho tem espaço (# Título).
    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("(?m)^#{1,6}\\s+(.+)");

    // 4. Remove citações Markdown (> citação)
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("(?m)^>\\s*");

    // 5. Remove tags de template ou placeholders artificiais (ex: [Descrição da Arte: '...'], [Local do Corpo: '...'])
    private static final Pattern TEMPLATE_PLACEHOLDER_PATTERN = Pattern.compile("\\[(?:Descrição da Arte|Local do Corpo|Estilo|Artista|Observações|Detalhes)[\\s:]*'?([^'\\]]+)'?\\]", Pattern.CASE_INSENSITIVE);

    // 6. Remove colchetes de links Markdown [texto](url) mantendo 'texto (url)' ou apenas 'url' se texto == url
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");

    // 7. Remove negrito e itálico combinados (***texto*** ou ___texto___)
    private static final Pattern BOLD_ITALIC_PATTERN = Pattern.compile("(\\*\\*\\*|___)(.+?)\\1");

    // 8. Remove negrito (**texto** ou __texto__)
    private static final Pattern BOLD_PATTERN = Pattern.compile("(\\*\\*|__)(.+?)\\1");

    // 9. Remove itálico (*texto* ou _texto_)
    private static final Pattern ITALIC_ASTERISK_PATTERN = Pattern.compile("(?<!\\*)\\*(?!\\s)([^*\\n]+?)(?<!\\s)\\*(?!\\*)");
    private static final Pattern ITALIC_UNDERSCORE_PATTERN = Pattern.compile("(?<=\\s|^)_(?!\\s)([^_\\n]+?)(?<!\\s)_(?=\\s|[.,!?;:]|$)");

    // 10. Remove tachado (~~texto~~)
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(.+?)~~");

    // 11. Converte marcadores de lista (* item, - item, + item) em bullets elegantes (• item)
    private static final Pattern LIST_BULLET_PATTERN = Pattern.compile("(?m)^[\\*\\-\\+]\\s+");

    // 12. Remove tags HTML residuais (<br>, <b>, <i>, <p>, etc.)
    private static final Pattern HTML_BR_PATTERN = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    // 13. Remove múltiplos asteriscos soltos (ex: **, ***)
    private static final Pattern STRAY_ASTERISKS_PATTERN = Pattern.compile("\\*{2,}");

    // 14. Normaliza quebras de linha consecutivas excessivas (mais de 2)
    private static final Pattern EXCESSIVE_NEWLINES_PATTERN = Pattern.compile("\\n{3,}");

    private LegendaSanitizer() {
        // Construtor privado para classe utilitária
    }

    /**
     * Sanitiza o corpo da legenda gerada por IA, garantindo texto puro para o Instagram.
     *
     * @param rawText Texto bruto recebido da API de IA ou do usuário
     * @return Texto sanitizado, limpo de marcações Markdown e caracteres especiais indesejados
     */
    public static String sanitizar(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String text = rawText;

        // 1. Normaliza quebras de linha Windows/Mac para Unix
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 2. Remove aspas externas se o modelo colocou toda a resposta entre aspas ("...")
        text = removerAspasExternas(text.trim());

        // 3. Remove blocos de código e código inline
        text = CODE_BLOCK_PATTERN.matcher(text).replaceAll("$1");
        text = INLINE_CODE_PATTERN.matcher(text).replaceAll("$1");

        // 4. Remove cabeçalhos Markdown (# Título -> Título), preservando hashtags
        text = MARKDOWN_HEADING_PATTERN.matcher(text).replaceAll("$1");

        // 5. Remove citações (> citação -> citação)
        text = BLOCKQUOTE_PATTERN.matcher(text).replaceAll("");

        // 6. Limpa placeholders de template [Descrição da Arte: 'XYZ'] -> XYZ
        text = TEMPLATE_PLACEHOLDER_PATTERN.matcher(text).replaceAll("$1");

        // 7. Converte links [texto](url) -> texto (url)
        Matcher linkMatcher = MARKDOWN_LINK_PATTERN.matcher(text);
        StringBuffer linkBuffer = new StringBuffer();
        while (linkMatcher.find()) {
            String linkText = linkMatcher.group(1).trim();
            String linkUrl = linkMatcher.group(2).trim();
            String replacement = linkText.equalsIgnoreCase(linkUrl) ? linkUrl : linkText + " (" + linkUrl + ")";
            linkMatcher.appendReplacement(linkBuffer, Matcher.quoteReplacement(replacement));
        }
        linkMatcher.appendTail(linkBuffer);
        text = linkBuffer.toString();

        // 8. Remove negrito e itálico (Markdown)
        text = BOLD_ITALIC_PATTERN.matcher(text).replaceAll("$2");
        text = BOLD_PATTERN.matcher(text).replaceAll("$2");
        text = ITALIC_ASTERISK_PATTERN.matcher(text).replaceAll("$1");
        text = ITALIC_UNDERSCORE_PATTERN.matcher(text).replaceAll("$1");
        text = STRIKETHROUGH_PATTERN.matcher(text).replaceAll("$1");

        // 9. Remove asteriscos soltos restantes
        text = STRAY_ASTERISKS_PATTERN.matcher(text).replaceAll("");

        // 10. Converte listas com marcadores (* item, - item) em bullets (• item)
        text = LIST_BULLET_PATTERN.matcher(text).replaceAll("• ");

        // 11. Limpa tags HTML
        text = HTML_BR_PATTERN.matcher(text).replaceAll("\n");
        text = HTML_TAG_PATTERN.matcher(text).replaceAll("");

        // 12. Limpa colchetes soltos desnecessários de prompts (ex: [Antebraço] -> Antebraço)
        text = text.replaceAll("\\[([^\\]\\n]{1,60})\\]", "$1");

        // 13. Normaliza quebras de linha e espaçamentos por linha
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("[ \\t]+$", ""); // rtrim
            result.append(line);
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        text = result.toString();

        // 14. Reduz 3 ou mais quebras de linha consecutivas para no máximo 2
        text = EXCESSIVE_NEWLINES_PATTERN.matcher(text).replaceAll("\n\n");

        return text.trim();
    }

    /**
     * Sanitiza o título do post gerado pela IA.
     */
    public static String sanitizarTitulo(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) {
            return "";
        }
        String title = sanitizar(rawTitle);
        // Remove quebras de linha de título
        title = title.replace("\n", " ").replaceAll("\\s{2,}", " ");
        // Remove aspas
        title = removerAspasExternas(title);
        // Remove asteriscos soltos
        title = title.replace("*", "");
        return title.trim();
    }

    /**
     * Sanitiza e valida a lista de hashtags geradas pela IA.
     */
    public static List<String> sanitizarHashtags(List<String> rawHashtags) {
        if (rawHashtags == null || rawHashtags.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> cleaned = new ArrayList<>();
        for (String raw : rawHashtags) {
            if (raw == null || raw.isBlank()) continue;
            String tag = raw.trim().replace("*", "").replace("`", "");
            if (!tag.startsWith("#")) {
                tag = "#" + tag;
            }
            // Remove pontuação indevida do final da hashtag (ex: #TattooFlow, -> #TattooFlow)
            tag = tag.replaceAll("[.,;:!?]+$", "");
            if (tag.length() > 1 && !cleaned.contains(tag)) {
                cleaned.add(tag);
            }
        }
        return cleaned;
    }

    private static String removerAspasExternas(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }
        if ((str.startsWith("\"") && str.endsWith("\"")) ||
            (str.startsWith("'") && str.endsWith("'")) ||
            (str.startsWith("“") && str.endsWith("”")) ||
            (str.startsWith("«") && str.endsWith("»"))) {
            return str.substring(1, str.length() - 1).trim();
        }
        return str;
    }
}
