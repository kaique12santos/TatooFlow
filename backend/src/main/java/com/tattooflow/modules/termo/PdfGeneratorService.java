package com.tattooflow.modules.termo;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public static final long LIMITE_MAXIMO_BYTES = 5 * 1024 * 1024; // 5MB (RES02)
    private static final DateTimeFormatter DATA_FORMATADA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TermoRepository termoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public PdfGeneratorService(TermoRepository termoRepository,
                               ClienteRepository clienteRepository,
                               AgendamentoRepository agendamentoRepository) {
        this.termoRepository = termoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Gera o documento PDF do Termo de Consentimento para o ID informado.
     * Atende ao Critério 1 (RN04) com dados do cliente e data/hora do aceite.
     * Atende ao Critério 2 (RES02) validando o tamanho máximo de 5MB.
     *
     * @param termoId ID do Termo de Aceite
     * @return Array de bytes do PDF gerado
     */
    public byte[] gerarPdfTermo(Long termoId) {
        if (termoId == null) {
            throw new BusinessException("ID do termo é obrigatório para geração do PDF.");
        }

        TermoAceite termo = termoRepository.findById(termoId)
                .orElseThrow(() -> new ResourceNotFoundException("Termo de aceite não encontrado com ID: " + termoId));

        Cliente cliente = clienteRepository.findById(termo.getClienteId()).orElse(null);
        Agendamento agendamento = agendamentoRepository.findById(termo.getAgendamentoId()).orElse(null);

        TermoDadosPdf dados = TermoDadosPdf.de(termo, cliente, agendamento);
        return gerarPdf(dados);
    }

    /**
     * Gera o PDF completo com dados estruturados.
     *
     * @param dados Informações cadastrais, contratuais e data/hora do aceite
     * @return Array de bytes do documento PDF
     */
    public byte[] gerarPdf(TermoDadosPdf dados) {
        if (dados == null) {
            throw new BusinessException("Dados do termo não podem ser nulos para a geração do PDF.");
        }
        if (dados.nomeCliente() == null || dados.nomeCliente().isBlank()) {
            throw new BusinessException("Nome do cliente é obrigatório para a geração do PDF do termo.");
        }
        if (dados.dataAceite() == null) {
            throw new BusinessException("Data e hora do aceite são obrigatórias para validação legal (RN04).");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Metadados do PDF (RN04)
            document.addTitle("Termo de Consentimento Livre e Esclarecido - TattooFlow");
            document.addSubject("Documento Legal de Consentimento e Responsabilidade (RN04)");
            document.addAuthor("TattooFlow Studio Management");
            document.addCreator("TattooFlow Security Module");

            document.open();

            // Fontes
            Font fontHeaderTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(20, 20, 20));
            Font fontHeaderSub = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 100, 100));
            Font fontSectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 30, 30));
            Font fontText = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(40, 40, 40));
            Font fontTextBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(20, 20, 20));
            Font fontLegal = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(70, 70, 70));
            Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(130, 130, 130));

            // CABEÇALHO DO ESTÚDIO
            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(new Chunk("TATTOOFLOW STUDIO\n", fontHeaderTitle));
            header.add(new Chunk("TERMO DE CONSENTIMENTO LIVRE E ESCLARECIDO & ANAMNESE\n", fontSectionTitle));
            header.add(new Chunk("Documento Legal Eletrônico para Fins Sanitários e Civis (RN04)\n\n", fontHeaderSub));
            document.add(header);

            // LINHA SEPARADORA
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(new Color(210, 210, 210));
            separator.setLineWidth(1);
            document.add(separator);
            document.add(new Paragraph(" "));

            // PROTOCOLO E REGISTRO
            PdfPTable tabelaMeta = new PdfPTable(2);
            tabelaMeta.setWidthPercentage(100);
            tabelaMeta.setSpacingAfter(10);

            adicionarLinhaTabela(tabelaMeta, "Protocolo Documental:",
                    dados.protocolo() != null ? dados.protocolo() : "TF-TERMO-" + (dados.termoId() != null ? dados.termoId() : "NOVO"),
                    fontTextBold, fontText);
            adicionarLinhaTabela(tabelaMeta, "Data/Hora de Geração:",
                    LocalDateTime.now().format(DATA_FORMATADA), fontTextBold, fontText);
            document.add(tabelaMeta);

            // SEÇÃO 1: DADOS DO CLIENTE (Critério 1)
            adicionarSecaoTitulo(document, "1. DADOS CADASTRAIS DO CLIENTE", fontSectionTitle);
            PdfPTable tabelaCliente = new PdfPTable(2);
            tabelaCliente.setWidthPercentage(100);
            tabelaCliente.setSpacingAfter(10);

            adicionarLinhaTabela(tabelaCliente, "Nome do Cliente:", dados.nomeCliente(), fontTextBold, fontText);
            adicionarLinhaTabela(tabelaCliente, "Telefone / WhatsApp:",
                    dados.telefoneCliente() != null ? dados.telefoneCliente() : "Não informado", fontTextBold, fontText);
            adicionarLinhaTabela(tabelaCliente, "E-mail:",
                    dados.emailCliente() != null ? dados.emailCliente() : "Não informado", fontTextBold, fontText);
            adicionarLinhaTabela(tabelaCliente, "CPF / Documento:",
                    dados.cpfCliente() != null ? dados.cpfCliente() : "Não informado", fontTextBold, fontText);

            if (dados.historicoSaude() != null && !dados.historicoSaude().isBlank()) {
                adicionarLinhaTabela(tabelaCliente, "Declaração de Saúde / Anamnese:",
                        dados.historicoSaude(), fontTextBold, fontText);
            }
            document.add(tabelaCliente);

            // SEÇÃO 2: DADOS DO AGENDAMENTO / PROCEDIMENTO
            adicionarSecaoTitulo(document, "2. INFORMAÇÕES DO PROCEDIMENTO E SESSÃO", fontSectionTitle);
            PdfPTable tabelaAgendamento = new PdfPTable(2);
            tabelaAgendamento.setWidthPercentage(100);
            tabelaAgendamento.setSpacingAfter(10);

            adicionarLinhaTabela(tabelaAgendamento, "Código do Agendamento:",
                    dados.agendamentoId() != null ? String.valueOf(dados.agendamentoId()) : "Vinculado", fontTextBold, fontText);
            adicionarLinhaTabela(tabelaAgendamento, "Data/Hora da Sessão:",
                    dados.dataHoraAgendamento() != null ? dados.dataHoraAgendamento().format(DATA_FORMATADA) : "A confirmar",
                    fontTextBold, fontText);
            adicionarLinhaTabela(tabelaAgendamento, "Duração Prevista:",
                    dados.duracaoMinutos() != null ? dados.duracaoMinutos() + " minutos" : "Padrão da sessão",
                    fontTextBold, fontText);
            adicionarLinhaTabela(tabelaAgendamento, "Descrição do Procedimento:",
                    dados.descricaoSessao() != null ? dados.descricaoSessao() : "Procedimento de tatuagem artística / body art",
                    fontTextBold, fontText);
            document.add(tabelaAgendamento);

            // SEÇÃO 3: CLÁUSULAS JURÍDICAS E RESPONSABILIDADE (RN04)
            adicionarSecaoTitulo(document, "3. TERMOS DE CIÊNCIA, RISCOS E CONSENTIMENTO (RN04)", fontSectionTitle);
            Paragraph pClausulas = new Paragraph();
            pClausulas.setAlignment(Element.ALIGN_JUSTIFIED);
            pClausulas.setSpacingAfter(10);

            pClausulas.add(new Paragraph("3.1. Declaro ser maior de 18 anos completos ou legalmente emancipado, em pleno gozo de minhas capacidades civis, e que tomei a decisão de realizar o procedimento de livre e espontânea vontade.\n", fontLegal));
            pClausulas.add(new Paragraph("3.2. Fui plenamente esclarecido(a) de que o procedimento de tatuagem é invasivo e permanente, havendo dor moderada inerente à perfuração cutânea, riscos naturais de inflamação local, sensibilidade e necessidade de assepsia rigorosa no período de cicatrização.\n", fontLegal));
            pClausulas.add(new Paragraph("3.3. Confirmo a veracidade de todas as informações prestadas durante a triagem e ficha de anamnese médica, afirmando não omitir histórico de alergias, hemofilia, hepatites, cardiopatias, diabetes descompensada ou uso de anticoagulantes.\n", fontLegal));
            pClausulas.add(new Paragraph("3.4. Comprometo-me a seguir rigorosamente as diretrizes e instruções de cuidados pré e pós-procedimento fornecidas pela equipe do TattooFlow, responsabilizando-me pelos cuidados de higiene e preservação do curativo e da derme.\n", fontLegal));
            pClausulas.add(new Paragraph("3.5. Autorizo expressamente o atelier TattooFlow a realizar o procedimento conforme planejado e acordado previamente.\n", fontLegal));
            document.add(pClausulas);

            // SEÇÃO 4: ACEITE DIGITAL E RETENÇÃO LEGAL (Critério 1 - data/hora do aceite & RN06)
            adicionarSecaoTitulo(document, "4. REGISTRO DE ACEITE DIGITAL E RETENÇÃO LEGAL (RN04 & RN06)", fontSectionTitle);
            PdfPTable tabelaAceite = new PdfPTable(2);
            tabelaAceite.setWidthPercentage(100);
            tabelaAceite.setSpacingAfter(15);

            adicionarLinhaTabela(tabelaAceite, "Data e Hora do Aceite:",
                    dados.dataAceite().format(DATA_FORMATADA), fontTextBold, fontText);
            adicionarLinhaTabela(tabelaAceite, "Endereço IP do Aceite:",
                    dados.ipAceite() != null ? dados.ipAceite() : "127.0.0.1", fontTextBold, fontText);

            LocalDateTime expiracao = dados.dataExpiracaoRetencao() != null
                    ? dados.dataExpiracaoRetencao()
                    : dados.dataAceite().plusYears(5);
            adicionarLinhaTabela(tabelaAceite, "Prazo de Retenção Legal (RN06):",
                    expiracao.format(DATA_FORMATADA) + " (Mínimo de 5 anos)", fontTextBold, fontText);

            String hashIntegridade = calcularHashIntegridade(dados);
            adicionarLinhaTabela(tabelaAceite, "Hash de Autenticidade (SHA-256):",
                    hashIntegridade, fontTextBold, fontLegal);
            document.add(tabelaAceite);

            // ÁREA DE ASSINATURA ELETRÔNICA
            PdfPTable tabelaAssinatura = new PdfPTable(2);
            tabelaAssinatura.setWidthPercentage(100);
            tabelaAssinatura.setSpacingBefore(10);
            tabelaAssinatura.setSpacingAfter(15);

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setBorder(Rectangle.NO_BORDER);
            cellCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellCliente.addElement(new Paragraph("__________________________________________", fontText));
            cellCliente.addElement(new Paragraph(dados.nomeCliente(), fontTextBold));
            cellCliente.addElement(new Paragraph("Assinatura Eletrônica do(a) Cliente\nAceite registrado em: "
                    + dados.dataAceite().format(DATA_FORMATADA), fontFooter));

            PdfPCell cellEstudio = new PdfPCell();
            cellEstudio.setBorder(Rectangle.NO_BORDER);
            cellEstudio.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellEstudio.addElement(new Paragraph("__________________________________________", fontText));
            cellEstudio.addElement(new Paragraph("TATTOOFLOW STUDIO", fontTextBold));
            cellEstudio.addElement(new Paragraph("Certificação Notarial de Bancada\nAutenticação Digital SHA-256", fontFooter));

            tabelaAssinatura.addCell(cellCliente);
            tabelaAssinatura.addCell(cellEstudio);
            document.add(tabelaAssinatura);

            // RODAPÉ
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Chunk("Este documento eletrônico é protegido pela legislação vigente e possui fé jurídica.\n"
                    + "Armazenamento seguro auditável pelo sistema TattooFlow conforme RN04 e RN06.", fontFooter));
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            // Critério 2: Validação de tamanho máximo de 5MB (RES02)
            if (pdfBytes.length > LIMITE_MAXIMO_BYTES) {
                throw new BusinessException("RES02: O documento PDF gerado excede o limite máximo de 5 MB ("
                        + pdfBytes.length + " bytes).");
            }

            return pdfBytes;
        } catch (DocumentException e) {
            throw new BusinessException("Erro na montagem do documento PDF: " + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Falha ao gerar o documento PDF do termo de consentimento: " + e.getMessage());
        }
    }

    private void adicionarSecaoTitulo(Document document, String titulo, Font font) throws DocumentException {
        Paragraph p = new Paragraph(titulo, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        document.add(p);
    }

    private void adicionarLinhaTabela(PdfPTable table, String rotulo, String valor, Font fontRotulo, Font fontValor) {
        PdfPCell cRotulo = new PdfPCell(new Phrase(rotulo, fontRotulo));
        cRotulo.setBackgroundColor(new Color(245, 245, 245));
        cRotulo.setPadding(4);
        cRotulo.setBorderColor(new Color(220, 220, 220));

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontValor));
        cValor.setPadding(4);
        cValor.setBorderColor(new Color(220, 220, 220));

        table.addCell(cRotulo);
        table.addCell(cValor);
    }

    private String calcularHashIntegridade(TermoDadosPdf dados) {
        try {
            String raw = String.format("%s:%s:%s:%s",
                    dados.nomeCliente(),
                    dados.dataAceite() != null ? dados.dataAceite().toString() : "",
                    dados.ipAceite(),
                    dados.agendamentoId());
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
