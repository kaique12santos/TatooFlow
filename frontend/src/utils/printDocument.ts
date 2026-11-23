/**
 * Utilitário para impressão e exportação em PDF de documentos do TattooFlow.
 * Renderiza o documento em ambiente isolado (iframe limpo) com estilos A4
 * preto e branco padrão de mercado, acionando o diálogo nativo "Salvar como PDF" / Imprimir.
 */
export interface PrintOptions {
  title?: string;
}

export function printDocument(elementOrHtml: HTMLElement | string, options: PrintOptions = {}): void {
  const title = options.title || 'Documento - TattooFlow';
  const htmlContent = typeof elementOrHtml === 'string'
    ? elementOrHtml
    : (elementOrHtml.outerHTML || elementOrHtml.innerHTML);

  const iframe = document.createElement('iframe');
  iframe.style.position = 'fixed';
  iframe.style.right = '0';
  iframe.style.bottom = '0';
  iframe.style.width = '0';
  iframe.style.height = '0';
  iframe.style.border = '0';
  iframe.style.opacity = '0';
  iframe.style.pointerEvents = 'none';
  document.body.appendChild(iframe);

  const doc = iframe.contentWindow?.document;
  if (!doc) {
    console.error('[printDocument] Não foi possível acessar o contexto do iframe.');
    return;
  }

  doc.open();
  doc.write(`
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
      <meta charset="utf-8">
      <title>${title}</title>
      <style>
        @page {
          size: A4 portrait;
          margin: 12mm 15mm;
        }
        * {
          box-sizing: border-box;
          -webkit-print-color-adjust: exact;
          print-color-adjust: exact;
        }
        body {
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
          color: #111111;
          background: #ffffff;
          margin: 0;
          padding: 0;
          font-size: 10pt;
          line-height: 1.35;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 8px;
        }
        th, td {
          border: 1px solid #333333;
          padding: 4px 6px;
          text-align: left;
          font-size: 9pt;
          vertical-align: top;
        }
        th {
          background-color: #f2f2f2;
          font-weight: 600;
          text-transform: uppercase;
          font-size: 8.5pt;
        }
        .avoid-break {
          page-break-inside: avoid;
          break-inside: avoid;
        }
        @media print {
          body {
            width: 100%;
            margin: 0;
          }
          .no-print {
            display: none !important;
          }
        }
      </style>
    </head>
    <body>
      ${htmlContent}
    </body>
    </html>
  `);
  doc.close();

  setTimeout(() => {
    try {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();
    } catch (err) {
      console.warn('[printDocument] Falha na chamada print() do iframe, usando fallback de janela:', err);
      const printWin = window.open('', '_blank');
      if (printWin) {
        printWin.document.write(doc.documentElement.outerHTML);
        printWin.document.close();
        printWin.focus();
        printWin.print();
      }
    } finally {
      setTimeout(() => {
        if (document.body.contains(iframe)) {
          document.body.removeChild(iframe);
        }
      }, 1500);
    }
  }, 350);
}

export function downloadDocumentHtml(elementOrHtml: HTMLElement | string, filename: string = 'documento.html'): void {
  const content = typeof elementOrHtml === 'string'
    ? elementOrHtml
    : (elementOrHtml.outerHTML || elementOrHtml.innerHTML);

  const fullHtml = `<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <title>${filename.replace(/\.html$/i, '')}</title>
  <style>
    @page { size: A4 portrait; margin: 12mm 15mm; }
    * { box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      color: #111;
      background: #fff;
      padding: 24px;
      max-width: 800px;
      margin: 0 auto;
      font-size: 10pt;
      line-height: 1.35;
    }
    table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
    th, td { border: 1px solid #333; padding: 4px 6px; font-size: 9pt; vertical-align: top; }
    th { background: #f2f2f2; font-weight: 600; text-transform: uppercase; font-size: 8.5pt; }
  </style>
</head>
<body>
  ${content}
</body>
</html>`;

  const blob = new Blob([fullHtml], { type: 'text/html;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename.endsWith('.html') ? filename : `${filename}.html`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
