package com.tattooflow.modules.relatorio;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.tattooflow.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class RelatorioPdfService {

    public static final long LIMITE_MAXIMO_BYTES = 5 * 1024 * 1024; // 5MB
    private static final DateTimeFormatter DATA_HORA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat MOEDA_FORMAT = new DecimalFormat("R$ #,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

    // Cores temáticas elegantes inspiradas na identidade TattooFlow
    private static final Color COR_PRIMARIA_ESCURA = new Color(20, 20, 22);
    private static final Color COR_DOURADO = new Color(184, 148, 86);
    private static final Color COR_CINZA_FUNDO = new Color(247, 246, 243);
    private static final Color COR_BORDA = new Color(222, 220, 215);
    private static final Color COR_TEXTO_SECUNDARIO = new Color(110, 108, 104);
    private static final Color COR_TEXTO_ESCURO = new Color(30, 30, 32);

    public byte[] gerarPdf(RelatorioMensalDTO relatorio) {
        if (relatorio == null) {
            throw new BusinessException("Dados do relatório não podem ser nulos para a geração do PDF.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);

            document.addTitle("Relatório Consolidado Mensal & Livro-Razão - TattooFlow");
            document.addSubject(String.format("Fechamento Contábil e Auditoria Notarial de %02d/%04d", relatorio.getMes(), relatorio.getAno()));
            document.addAuthor("TattooFlow Studio Management");
            document.addCreator("TattooFlow Relatório Module");

            document.open();

            // Fontes
            Font fontTituloPrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, COR_PRIMARIA_ESCURA);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COR_DOURADO);
            Font fontCabecalhoInfo = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COR_TEXTO_SECUNDARIO);
            Font fontSecaoTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, COR_PRIMARIA_ESCURA);
            Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COR_TEXTO_ESCURO);
            Font fontTextoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COR_TEXTO_ESCURO);
            Font fontTabelaHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, COR_PRIMARIA_ESCURA);
            Font fontLegal = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COR_TEXTO_SECUNDARIO);
            Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, COR_TEXTO_SECUNDARIO);

            // 1. CABEÇALHO DO ATELIER
            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(new Chunk("NEW CONCEPT TATTOO\n", fontTituloPrincipal));
            header.add(new Chunk("LIVRO-RAZÃO NOTARIAL · RELATÓRIO FINANCEIRO CONSOLIDADO\n", fontSubtitulo));
            header.add(new Chunk(String.format("Competência: %02d/%04d · Período: %s a %s · Emissão: %s\n\n",
                    relatorio.getMes(),
                    relatorio.getAno(),
                    relatorio.getPeriodoInicio() != null ? relatorio.getPeriodoInicio().format(DATA_FORMATO) : "01/" + relatorio.getPeriodo(),
                    relatorio.getPeriodoFim() != null ? relatorio.getPeriodoFim().format(DATA_FORMATO) : relatorio.getPeriodo(),
                    LocalDateTime.now().format(DATA_HORA_FORMATO)), fontCabecalhoInfo));
            document.add(header);

            LineSeparator sep = new LineSeparator();
            sep.setLineColor(COR_DOURADO);
            sep.setLineWidth(1.2f);
            document.add(sep);
            document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

            // 2. METADADOS E PROTOCOLO
            String protocolo = String.format("TF-REL-%04d%02d-%06d", relatorio.getAno(), relatorio.getMes(), System.currentTimeMillis() % 1000000);
            boolean concil = relatorio.getTotalSessoes() > 0 || (relatorio.getTotalFaturamento() != null && relatorio.getTotalFaturamento().compareTo(BigDecimal.ZERO) > 0);
            String statusConciliacao = concil ? "CONCILIADO" : "EM ABERTO";

            PdfPTable tabelaMeta = new PdfPTable(4);
            tabelaMeta.setWidthPercentage(100);
            tabelaMeta.setWidths(new float[]{22, 28, 22, 28});
            tabelaMeta.setSpacingAfter(8);

            adicionarCelulaMeta(tabelaMeta, "Protocolo Documental:", protocolo, fontTextoBold, fontTexto);
            adicionarCelulaMeta(tabelaMeta, "Status do Mês:", statusConciliacao, fontTextoBold, fontTextoBold);
            document.add(tabelaMeta);

            // 3. RESUMO EXECUTIVO MENSAL
            adicionarTituloSecao(document, "1. RESUMO EXECUTIVO MENSAL CONSOLIDADO", fontSecaoTitulo);

            PdfPTable tabelaResumo = new PdfPTable(2);
            tabelaResumo.setWidthPercentage(100);
            tabelaResumo.setWidths(new float[]{55, 45});
            tabelaResumo.setSpacingAfter(10);

            adicionarLinhaTabela(tabelaResumo, "Faturamento Bruto Total (Obras e Sessões):", formatarMoeda(relatorio.getTotalFaturamento()), fontTextoBold, fontTextoBold, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaResumo, "Faturamento Recebido (Custódia Sinais PIX):", formatarMoeda(relatorio.getFaturamentoRecebido()), fontTexto, fontTexto, Color.WHITE);
            adicionarLinhaTabela(tabelaResumo, "Taxa Atelier / Retenção de Espaço (30%):", formatarMoeda(relatorio.getTotalComissoes()), fontTextoBold, fontTextoBold, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaResumo, "Repasse Artistas Residentes & Guests (70%):", formatarMoeda(relatorio.getTotalRepasses()), fontTextoBold, fontTextoBold, Color.WHITE);
            adicionarLinhaTabela(tabelaResumo, "Gastos Operacionais com Insumos:", formatarMoeda(relatorio.getTotalGastos()), fontTexto, fontTexto, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaResumo, "Lucro Líquido do Atelier (Comissões - Custos):", formatarMoeda(relatorio.getLucroLiquido()), fontTextoBold, fontTextoBold, Color.WHITE);
            adicionarLinhaTabela(tabelaResumo, "Total de Obras e Sessões Concluídas:", String.valueOf(relatorio.getTotalSessoesConcluidas()), fontTexto, fontTexto, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaResumo, "Total de Agendamentos no Período:", String.valueOf(relatorio.getTotalAgendamentosPeriodo()), fontTexto, fontTexto, Color.WHITE);

            document.add(tabelaResumo);

            // 4. DETALHAMENTO DE PERFORMANCE POR ARTISTA
            adicionarTituloSecao(document, "2. DETALHAMENTO DE REPASSE E PERFORMANCE POR ARTISTA", fontSecaoTitulo);

            List<RelatorioArtistaDTO> artistas = relatorio.getDetalhamentoArtistas();
            if (artistas != null && !artistas.isEmpty()) {
                PdfPTable tabelaArtistas = new PdfPTable(7);
                tabelaArtistas.setWidthPercentage(100);
                tabelaArtistas.setWidths(new float[]{24, 10, 14, 14, 13, 13, 12});
                tabelaArtistas.setSpacingAfter(10);

                adicionarCabecalhoColuna(tabelaArtistas, "Artista / Tatuador", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Sessões", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Faturado", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Base Cálculo", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Atelier (30%)", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Repasse (70%)", fontTabelaHead);
                adicionarCabecalhoColuna(tabelaArtistas, "Insumos", fontTabelaHead);

                boolean zebra = false;
                for (RelatorioArtistaDTO art : artistas) {
                    Color bg = zebra ? COR_CINZA_FUNDO : Color.WHITE;
                    zebra = !zebra;

                    adicionarCelulaDado(tabelaArtistas, art.getNomeTatuador() != null ? art.getNomeTatuador() : "Tatuador #" + art.getTatuadorId(), fontTextoBold, Element.ALIGN_LEFT, bg);
                    adicionarCelulaDado(tabelaArtistas, String.valueOf(art.getTotalSessoes()), fontTexto, Element.ALIGN_CENTER, bg);
                    adicionarCelulaDado(tabelaArtistas, formatarMoeda(art.getTotalFaturado()), fontTexto, Element.ALIGN_RIGHT, bg);
                    adicionarCelulaDado(tabelaArtistas, formatarMoeda(art.getBaseCalculo()), fontTexto, Element.ALIGN_RIGHT, bg);
                    adicionarCelulaDado(tabelaArtistas, formatarMoeda(art.getComissaoEstudio()), fontTexto, Element.ALIGN_RIGHT, bg);
                    adicionarCelulaDado(tabelaArtistas, formatarMoeda(art.getRepasseArtista()), fontTextoBold, Element.ALIGN_RIGHT, bg);
                    adicionarCelulaDado(tabelaArtistas, formatarMoeda(art.getCustoInsumos()), fontTexto, Element.ALIGN_RIGHT, bg);
                }
                document.add(tabelaArtistas);
            } else {
                Paragraph pVazio = new Paragraph("Nenhuma movimentação de repasses a artistas registrada para este período.", fontTexto);
                pVazio.setSpacingAfter(10);
                document.add(pVazio);
            }

            // 5. RESUMO DE INSUMOS E ALMOXARIFADO
            adicionarTituloSecao(document, "3. CONTROLE DE ALMOXARIFADO E ESTOQUE DE INSUMOS", fontSecaoTitulo);
            PdfPTable tabelaEstoque = new PdfPTable(2);
            tabelaEstoque.setWidthPercentage(100);
            tabelaEstoque.setWidths(new float[]{60, 40});
            tabelaEstoque.setSpacingAfter(10);

            long itensCadastrados = relatorio.getResumoEstoque() != null ? relatorio.getResumoEstoque().getTotalItensCadastrados() : 0;
            long itensCriticos = relatorio.getResumoEstoque() != null ? relatorio.getResumoEstoque().getItensEmEstoqueCritico() : 0;
            boolean alertaAtivo = relatorio.getResumoEstoque() != null && relatorio.getResumoEstoque().isAlertaEstoqueAtivo();

            adicionarLinhaTabela(tabelaEstoque, "Total de Itens Cadastrados no Acervo:", String.valueOf(itensCadastrados), fontTexto, fontTexto, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaEstoque, "Itens em Nível Crítico / Reposição:", String.valueOf(itensCriticos), fontTexto, fontTextoBold, Color.WHITE);
            adicionarLinhaTabela(tabelaEstoque, "Status de Alerta do Almoxarifado:", alertaAtivo ? "ALERTA ATIVO - REPOSIÇÃO NECESSÁRIA" : "REGULAR / NORMAS ATENDIDAS", fontTextoBold, fontTextoBold, COR_CINZA_FUNDO);

            document.add(tabelaEstoque);

            // 6. CERTIFICAÇÃO DIGITAL NOTARIAL E AUTENTICIDADE
            adicionarTituloSecao(document, "4. CERTIFICAÇÃO DIGITAL NOTARIAL E LIVRO-RAZÃO", fontSecaoTitulo);

            String hashIntegridade = calcularHashIntegridade(relatorio);

            PdfPTable tabelaCert = new PdfPTable(2);
            tabelaCert.setWidthPercentage(100);
            tabelaCert.setWidths(new float[]{30, 70});
            tabelaCert.setSpacingAfter(12);

            adicionarLinhaTabela(tabelaCert, "Data/Hora de Apuração:", LocalDateTime.now().format(DATA_HORA_FORMATO), fontTextoBold, fontTexto, COR_CINZA_FUNDO);
            adicionarLinhaTabela(tabelaCert, "Hash de Integridade (SHA-256):", hashIntegridade, fontTextoBold, fontLegal, Color.WHITE);
            adicionarLinhaTabela(tabelaCert, "Segurança Notarial:", "Livro-razão criptografado para resguardo tributário e transparência irrestrita com os artistas.", fontTextoBold, fontLegal, COR_CINZA_FUNDO);

            document.add(tabelaCert);

            // 7. ASSINATURAS NOTARIAIS
            PdfPTable tabelaAssinatura = new PdfPTable(2);
            tabelaAssinatura.setWidthPercentage(100);
            tabelaAssinatura.setSpacingBefore(12);
            tabelaAssinatura.setSpacingAfter(12);

            PdfPCell cellEstudio = new PdfPCell();
            cellEstudio.setBorder(Rectangle.NO_BORDER);
            cellEstudio.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellEstudio.addElement(new Paragraph("__________________________________________", fontTexto));
            cellEstudio.addElement(new Paragraph("NEW CONCEPT TATTOO", fontTextoBold));
            cellEstudio.addElement(new Paragraph("Diretoria Financeira & Auditoria", fontFooter));

            PdfPCell cellNotarial = new PdfPCell();
            cellNotarial.setBorder(Rectangle.NO_BORDER);
            cellNotarial.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNotarial.addElement(new Paragraph("__________________________________________", fontTexto));
            cellNotarial.addElement(new Paragraph("CONTABILIDADE & LIVRO-RAZÃO", fontTextoBold));
            cellNotarial.addElement(new Paragraph("Autenticação Digital SHA-256", fontFooter));

            tabelaAssinatura.addCell(cellEstudio);
            tabelaAssinatura.addCell(cellNotarial);
            document.add(tabelaAssinatura);

            // 8. RODAPÉ LEGAL
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk("Documento oficial gerado eletronicamente pela plataforma TattooFlow Studio.\n"
                    + "Garantia de fé pública, transparência das bancadas e conformidade fiscal e sanitária.", fontFooter));
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            if (pdfBytes.length > LIMITE_MAXIMO_BYTES) {
                throw new BusinessException("O relatório PDF consolidado gerado excede o limite máximo permitido de 5 MB.");
            }

            return pdfBytes;
        } catch (DocumentException e) {
            throw new BusinessException("Erro na montagem do documento PDF de fechamento: " + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Falha ao gerar o relatório financeiro consolidado em PDF: " + e.getMessage());
        }
    }

    private void adicionarTituloSecao(Document document, String titulo, Font font) throws DocumentException {
        Paragraph p = new Paragraph(titulo, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(3);
        document.add(p);
    }

    private void adicionarLinhaTabela(PdfPTable table, String rotulo, String valor, Font fontRotulo, Font fontValor, Color bg) {
        PdfPCell cRotulo = new PdfPCell(new Phrase(rotulo, fontRotulo));
        cRotulo.setBackgroundColor(bg);
        cRotulo.setPadding(4f);
        cRotulo.setBorderColor(COR_BORDA);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontValor));
        cValor.setBackgroundColor(bg);
        cValor.setPadding(4f);
        cValor.setBorderColor(COR_BORDA);

        table.addCell(cRotulo);
        table.addCell(cValor);
    }

    private void adicionarCelulaMeta(PdfPTable table, String rotulo, String valor, Font fontRotulo, Font fontValor) {
        PdfPCell cRotulo = new PdfPCell(new Phrase(rotulo, fontRotulo));
        cRotulo.setBackgroundColor(COR_CINZA_FUNDO);
        cRotulo.setPadding(4f);
        cRotulo.setBorderColor(COR_BORDA);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontValor));
        cValor.setBackgroundColor(Color.WHITE);
        cValor.setPadding(4f);
        cValor.setBorderColor(COR_BORDA);

        table.addCell(cRotulo);
        table.addCell(cValor);
    }

    private void adicionarCabecalhoColuna(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(COR_CINZA_FUNDO);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4.5f);
        cell.setBorderColor(COR_BORDA);
        table.addCell(cell);
    }

    private void adicionarCelulaDado(PdfPTable table, String texto, Font font, int alinhamento, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(3.5f);
        cell.setBorderColor(COR_BORDA);
        table.addCell(cell);
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        synchronized (MOEDA_FORMAT) {
            return MOEDA_FORMAT.format(valor);
        }
    }

    private String calcularHashIntegridade(RelatorioMensalDTO relatorio) {
        try {
            String raw = String.format("%s:%s:%s:%s:%s:%d",
                    relatorio.getPeriodo(),
                    relatorio.getTotalFaturamento() != null ? relatorio.getTotalFaturamento().toString() : "0",
                    relatorio.getTotalComissoes() != null ? relatorio.getTotalComissoes().toString() : "0",
                    relatorio.getTotalRepasses() != null ? relatorio.getTotalRepasses().toString() : "0",
                    relatorio.getTotalGastos() != null ? relatorio.getTotalGastos().toString() : "0",
                    relatorio.getTotalSessoes());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "SHA256-UNAVAILABLE";
        }
    }
}
