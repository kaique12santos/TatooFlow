package com.tattooflow.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegendaSanitizerTest {

    @Test
    @DisplayName("Deve remover negrito com asteriscos (**texto** e ***texto***) e preservar o conteúdo")
    void deveRemoverNegritoComAsteriscos() {
        String input = "**Estudo de Arte** em chiaroscuro. ***Linhas finas*** executadas com perfeição.";
        String expected = "Estudo de Arte em chiaroscuro. Linhas finas executadas com perfeição.";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve remover itálico com asteriscos (*texto*) e underscores (_texto_)")
    void deveRemoverItalico() {
        String input = "Uma obra *visceral* gravada na pele com estilo _FineLine_.";
        String expected = "Uma obra visceral gravada na pele com estilo FineLine.";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve remover marcadores de cabeçalho Markdown (# e ##) mas preservar hashtags (#Hashtag)")
    void deveRemoverCabecalhosEManterHashtags() {
        String input = """
                # Obra de Arte
                ## Curadoria Literária
                Linhas precisas e contraste dramático.
                
                #TattooFlow #FineLine #BlackAndGrey
                """;

        String output = LegendaSanitizer.sanitizar(input);

        assertThat(output).contains("Obra de Arte");
        assertThat(output).contains("Curadoria Literária");
        assertThat(output).doesNotContain("# Obra");
        assertThat(output).doesNotContain("## Curadoria");
        // As hashtags devem ser preservadas
        assertThat(output).contains("#TattooFlow");
        assertThat(output).contains("#FineLine");
        assertThat(output).contains("#BlackAndGrey");
    }

    @Test
    @DisplayName("Deve remover asteriscos soltos e duplos residuais da IA")
    void deveRemoverAsteriscosSoltos() {
        String input = "** Estudo de São Jerônimo ** - Linhas puras **";
        String output = LegendaSanitizer.sanitizar(input);
        assertThat(output).doesNotContain("**");
    }

    @Test
    @DisplayName("Deve remover aspas externas quando o modelo envelopa a legenda inteira")
    void deveRemoverAspasExternas() {
        String input = "\"Legenda poética sobre a tatuagem realizada no estúdio.\"";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo("Legenda poética sobre a tatuagem realizada no estúdio.");
    }

    @Test
    @DisplayName("Deve limpar placeholders artificiais de template [Descrição da Arte: '...']")
    void deveLimparPlaceholdersArtificiais() {
        String input = "A profundidade do claro-escuro. [Descrição da Arte: 'Estudo anatômico de São Jerônimo']. Executado no [Local do Corpo: Antebraço].";
        String expected = "A profundidade do claro-escuro. Estudo anatômico de São Jerônimo. Executado no Antebraço.";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve converter listas markdown (* item, - item) em bullets elegantes (• item)")
    void deveConverterListasEmBullets() {
        String input = """
                Detalhes da sessão:
                * Pigmento Carbon Black puro
                - Agulhas 03RL
                + Assepsia notarial
                """;
        String output = LegendaSanitizer.sanitizar(input);
        assertThat(output).contains("• Pigmento Carbon Black puro");
        assertThat(output).contains("• Agulhas 03RL");
        assertThat(output).contains("• Assepsia notarial");
        assertThat(output).doesNotContain("* Pigmento");
    }

    @Test
    @DisplayName("Deve converter links markdown [Texto](URL) preservando o texto e a URL")
    void deveConverterLinksMarkdown() {
        String input = "Agende via [WhatsApp Oficial](https://wa.me/5511999999999) com nossa equipe.";
        String expected = "Agende via WhatsApp Oficial (https://wa.me/5511999999999) com nossa equipe.";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Deve remover blocos de código e código inline")
    void deveRemoverCodigoMarkdown() {
        String input = "Registro da obra: `NC-2026-001` no acervo.";
        assertThat(LegendaSanitizer.sanitizar(input)).isEqualTo("Registro da obra: NC-2026-001 no acervo.");
    }

    @Test
    @DisplayName("Deve sanitizar título removendo asteriscos, aspas e quebras de linha")
    void deveSanitizarTitulo() {
        String input = "**Estudo de São Jerônimo** · Chiaroscuro\n";
        assertThat(LegendaSanitizer.sanitizarTitulo(input)).isEqualTo("Estudo de São Jerônimo · Chiaroscuro");
    }

    @Test
    @DisplayName("Deve sanitizar hashtags garantindo o prefixo # e removendo pontuações finais")
    void deveSanitizarHashtags() {
        List<String> raw = List.of("**TattooFlow**", "#FineLine,", "Chiaroscuro;", "BlackAndGrey.");
        List<String> cleaned = LegendaSanitizer.sanitizarHashtags(raw);

        assertThat(cleaned).containsExactly("#TattooFlow", "#FineLine", "#Chiaroscuro", "#BlackAndGrey");
    }

    @Test
    @DisplayName("Deve retornar string vazia para entrada nula ou em branco")
    void deveLidarComEntradaNulaOuVazia() {
        assertThat(LegendaSanitizer.sanitizar(null)).isEmpty();
        assertThat(LegendaSanitizer.sanitizar("   ")).isEmpty();
        assertThat(LegendaSanitizer.sanitizarTitulo(null)).isEmpty();
        assertThat(LegendaSanitizer.sanitizarHashtags(null)).isEmpty();
    }
}
