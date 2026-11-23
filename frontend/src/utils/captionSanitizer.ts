/**
 * Utilitário auxiliar de sanitização e filtragem de legendas geradas por IA (Gemini).
 *
 * Garante que a legenda esteja em texto puro (plain text), em estrita conformidade com
 * os padrões do Instagram, eliminando formatações Markdown (negrito **, itálico *,
 * cabeçalhos ##, blocos de código, tags de template, etc.) que o Instagram não suporta.
 *
 * Princípio de Engenharia: Defesa em Profundidade (Prompt Refinement + Backend Sanitization + Frontend Sanitization).
 */

// 1. Remove blocos de código Markdown (```código```)
const CODE_BLOCK_REGEX = /```(?:[a-zA-Z0-9_-]+)?\s*\n?([\s\S]*?)```/g;

// 2. Remove código inline (`código`)
const INLINE_CODE_REGEX = /`([^`\n]+)`/g;

// 3. Remove cabeçalhos Markdown (# Título, ## Seção, etc.), preservando hashtags (#Hashtag)
const MARKDOWN_HEADING_REGEX = /^#{1,6}\s+(.+)$/gm;

// 4. Remove citações Markdown (> citação)
const BLOCKQUOTE_REGEX = /^>\s*/gm;

// 5. Remove tags de template artificiais (ex: [Descrição da Arte: '...'], [Local do Corpo: '...'])
const TEMPLATE_PLACEHOLDER_REGEX = /\[(?:Descrição da Arte|Local do Corpo|Estilo|Artista|Observações|Detalhes)[\s:]*'?([^'\]]+)'?\]/gi;

// 6. Converte links [texto](url) -> texto (url)
const MARKDOWN_LINK_REGEX = /\[([^\]]+)\]\(([^)]+)\)/g;

// 7. Remove negrito e itálico combinados (***texto*** ou ___texto___)
const BOLD_ITALIC_REGEX = /(\*\*\*|___)(.+?)\1/g;

// 8. Remove negrito (**texto** ou __texto__)
const BOLD_REGEX = /(\*\*|__)(.+?)\1/g;

// 9. Remove itálico (*texto* ou _texto_)
const ITALIC_ASTERISK_REGEX = /(?<!\*)\*(?!\s)([^*\n]+?)(?<!\s)\*(?!\*)/g;
const ITALIC_UNDERSCORE_REGEX = /(?<=\s|^)_(?!\s)([^_\n]+?)(?<!\s)_(?=\s|[.,!?;:]|$)/g;

// 10. Remove tachado (~~texto~~)
const STRIKETHROUGH_REGEX = /~~(.+?)~~/g;

// 11. Converte marcadores de lista (* item, - item, + item) em bullets (• item)
const LIST_BULLET_REGEX = /^[\*\-\+]\s+/gm;

// 12. Remove tags HTML residuais (<br>, <b>, <i>, <p>, etc.)
const HTML_BR_REGEX = /<br\s*\/?>/gi;
const HTML_TAG_REGEX = /<[^>]+>/g;

// 13. Remove múltiplos asteriscos soltos (ex: **, ***)
const STRAY_ASTERISKS_REGEX = /\*{2,}/g;

// 14. Normaliza quebras de linha consecutivas excessivas (mais de 2)
const EXCESSIVE_NEWLINES_REGEX = /\n{3,}/g;

/**
 * Remove aspas externas caso a IA tenha envolvido toda a resposta entre aspas.
 */
function removeOuterQuotes(str: string): string {
  if (!str || str.length < 2) return str;
  const trimmed = str.trim();
  if (
    (trimmed.startsWith('"') && trimmed.endsWith('"')) ||
    (trimmed.startsWith("'") && trimmed.endsWith("'")) ||
    (trimmed.startsWith('“') && trimmed.endsWith('”')) ||
    (trimmed.startsWith('«') && trimmed.endsWith('»'))
  ) {
    return trimmed.substring(1, trimmed.length - 1).trim();
  }
  return trimmed;
}

/**
 * Sanitiza o corpo da legenda gerada por IA, garantindo texto puro para o Instagram.
 */
export function sanitizeCaption(rawText: string | null | undefined): string {
  if (!rawText || !rawText.trim()) {
    return '';
  }

  let text = rawText;

  // 1. Normaliza quebras de linha Windows/Mac para Unix
  text = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n');

  // 2. Remove aspas externas se toda a legenda foi envelopada
  text = removeOuterQuotes(text);

  // 3. Remove blocos de código e código inline
  text = text.replace(CODE_BLOCK_REGEX, '$1');
  text = text.replace(INLINE_CODE_REGEX, '$1');

  // 4. Remove cabeçalhos Markdown (# Título -> Título), preservando hashtags (#Hashtag)
  text = text.replace(MARKDOWN_HEADING_REGEX, '$1');

  // 5. Remove citações Markdown (> citação -> citação)
  text = text.replace(BLOCKQUOTE_REGEX, '');

  // 6. Limpa placeholders artificiais [Descrição da Arte: 'XYZ'] -> XYZ
  text = text.replace(TEMPLATE_PLACEHOLDER_REGEX, '$1');

  // 7. Converte links [texto](url) -> texto (url)
  text = text.replace(MARKDOWN_LINK_REGEX, (_, linkText: string, linkUrl: string) => {
    const t = linkText.trim();
    const u = linkUrl.trim();
    return t.toLowerCase() === u.toLowerCase() ? u : `${t} (${u})`;
  });

  // 8. Remove negrito e itálico (Markdown)
  text = text.replace(BOLD_ITALIC_REGEX, '$2');
  text = text.replace(BOLD_REGEX, '$2');
  text = text.replace(ITALIC_ASTERISK_REGEX, '$1');
  text = text.replace(ITALIC_UNDERSCORE_REGEX, '$1');
  text = text.replace(STRIKETHROUGH_REGEX, '$1');

  // 9. Remove asteriscos soltos restantes
  text = text.replace(STRAY_ASTERISKS_REGEX, '');

  // 10. Converte listas com marcadores (* item, - item) em bullets (• item)
  text = text.replace(LIST_BULLET_REGEX, '• ');

  // 11. Limpa tags HTML
  text = text.replace(HTML_BR_REGEX, '\n');
  text = text.replace(HTML_TAG_REGEX, '');

  // 12. Limpa colchetes soltos desnecessários de prompts (ex: [Antebraço] -> Antebraço)
  text = text.replace(/\[([^\]\n]{1,60})\]/g, '$1');

  // 13. Normaliza quebras de linha e espaçamentos por linha
  const lines = text.split('\n').map((line) => line.replace(/[ \t]+$/, ''));
  text = lines.join('\n');

  // 14. Reduz 3 ou mais quebras de linha consecutivas para no máximo 2
  text = text.replace(EXCESSIVE_NEWLINES_REGEX, '\n\n');

  return text.trim();
}

/**
 * Sanitiza o título da obra/legenda gerado pela IA.
 */
export function sanitizeTitle(rawTitle: string | null | undefined): string {
  if (!rawTitle || !rawTitle.trim()) {
    return '';
  }
  let title = sanitizeCaption(rawTitle);
  title = title.replace(/\n+/g, ' ').replace(/\s{2,}/g, ' ');
  title = removeOuterQuotes(title);
  title = title.replace(/\*/g, '');
  return title.trim();
}

/**
 * Sanitiza a lista de hashtags.
 */
export function sanitizeHashtags(rawHashtags: string[] | null | undefined): string[] {
  if (!rawHashtags || !rawHashtags.length) {
    return [];
  }
  const cleaned: string[] = [];
  for (const raw of rawHashtags) {
    if (!raw || !raw.trim()) continue;
    let tag = raw.trim().replace(/[*`]/g, '');
    if (!tag.startsWith('#')) {
      tag = `#${tag}`;
    }
    tag = tag.replace(/[.,;:!?]+$/, '');
    if (tag.length > 1 && !cleaned.includes(tag)) {
      cleaned.push(tag);
    }
  }
  return cleaned;
}
