import { defineStore } from 'pinia';
import { ref } from 'vue';
import { apiRequest } from '@/api/http';

export interface ArtistPerformanceBreakdown {
  id?: string;
  name: string;
  station: string;
  specialty: string;
  totalMonth: string;
  liquidArtist: string;
  studioTax: string;
  progressPercentage: number;
  sessionsCount?: number;
}

export interface MonthlyFinancialSummary {
  monthYear: string;
  status: 'conciliado' | 'em_aberto' | 'processando';
  grossRevenue: number;
  grossRevenueFormatted: string;
  growthPercentage: string;
  completedWorksCount: number;
  studioTaxRate: number; // Ex: 30%
  studioTaxAmount: number;
  studioTaxFormatted: string;
  artistShareRate: number; // Ex: 70%
  artistShareAmount: number;
  artistShareFormatted: string;
  activeArtistsCount: number;
  pixEscrowAmount: number;
  pixEscrowFormatted: string;
  artistsBreakdown: ArtistPerformanceBreakdown[];
}

export interface FinancialActionResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
  downloadUrl?: string;
}

function createEmptySummary(monthYear: string): MonthlyFinancialSummary {
  return {
    monthYear,
    status: 'em_aberto',
    grossRevenue: 0,
    grossRevenueFormatted: 'R$ 0,00',
    growthPercentage: '0%',
    completedWorksCount: 0,
    studioTaxRate: 30,
    studioTaxAmount: 0,
    studioTaxFormatted: 'R$ 0,00',
    artistShareRate: 70,
    artistShareAmount: 0,
    artistShareFormatted: 'R$ 0,00',
    activeArtistsCount: 0,
    pixEscrowAmount: 0,
    pixEscrowFormatted: 'R$ 0,00',
    artistsBreakdown: [],
  };
}

export const useFinancialStore = defineStore('financial', () => {
  const currentMonth = ref<string>('NOVEMBRO / 2026');
  const summary = ref<MonthlyFinancialSummary>(createEmptySummary(currentMonth.value));
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  const monthsSequence = ['OUTUBRO / 2026', 'NOVEMBRO / 2026', 'DEZEMBRO / 2026', 'JANEIRO / 2027'];

  // Carrega fechamento consolidado mensal real do backend (/api/relatorios/mensal)
  async function fetchMonthlyClosing(targetMonth: string = currentMonth.value): Promise<void> {
    isLoading.value = true;
    error.value = null;
    currentMonth.value = targetMonth;

    const monthMap: Record<string, number> = {
      JANEIRO: 1,
      FEVEREIRO: 2,
      MARÇO: 3,
      MARCO: 3,
      ABRIL: 4,
      MAIO: 5,
      JUNHO: 6,
      JULHO: 7,
      AGOSTO: 8,
      SETEMBRO: 9,
      OUTUBRO: 10,
      NOVEMBRO: 11,
      DEZEMBRO: 12,
    };

    const parts = targetMonth.split('/');
    const monthName = parts[0]?.trim().toUpperCase() || '';
    const yearNum = parseInt(parts[1]?.trim() || '2026', 10);
    const monthNum = monthMap[monthName] || 11;

    try {
      const data = await apiRequest<any>(`/api/relatorios/mensal?mes=${monthNum}&ano=${yearNum}`);

      if (data) {
        const gross = Number(data.faturamentoBruto ?? data.totalFaturamento ?? 0);
        const studio = Number(data.comissaoEstudio ?? data.totalComissoes ?? 0);
        const artist = Number(data.totalRepasseArtistas ?? data.totalRepasses ?? 0);
        const works = Number(data.totalSessoesConcluidas ?? 0);
        const rawArtists = Array.isArray(data.detalhamentoArtistas) ? data.detalhamentoArtistas : [];

        const artistsList: ArtistPerformanceBreakdown[] = rawArtists.map((art: any, idx: number) => ({
          id: String(art.tatuadorId || idx),
          name: art.nomeTatuador || `Tatuador #${art.tatuadorId}`,
          station: `Bancada 0${idx + 1}`,
          specialty: 'Residente',
          totalMonth: `R$ ${Number(art.totalFaturado || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          liquidArtist: `R$ ${Number(art.repasseArtista || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          studioTax: `R$ ${Number(art.comissaoEstudio || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          progressPercentage: gross > 0 ? Math.round((Number(art.totalFaturado || 0) / gross) * 100) : 0,
          sessionsCount: art.totalSessoes || 0,
        }));

        summary.value = {
          monthYear: targetMonth,
          status: works > 0 ? 'conciliado' : 'em_aberto',
          grossRevenue: gross,
          grossRevenueFormatted: `R$ ${gross.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          growthPercentage: '0%',
          completedWorksCount: works,
          studioTaxRate: 30,
          studioTaxAmount: studio,
          studioTaxFormatted: `R$ ${studio.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          artistShareRate: 70,
          artistShareAmount: artist,
          artistShareFormatted: `R$ ${artist.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          activeArtistsCount: artistsList.length,
          pixEscrowAmount: Number(data.faturamentoRecebido ?? 0),
          pixEscrowFormatted: `R$ ${Number(data.faturamentoRecebido ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
          artistsBreakdown: artistsList,
        };
      } else {
        summary.value = createEmptySummary(targetMonth);
      }
    } catch (err: any) {
      summary.value = createEmptySummary(targetMonth);
      error.value = err?.message || 'Erro ao carregar relatório mensal do banco.';
      console.warn('[FinancialStore] Erro no relatório mensal:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // Solicita geração ou download do relatório mensal
  async function downloadMonthlyReportPdf(targetMonth: string = currentMonth.value): Promise<FinancialActionResponse> {
    try {
      const query = encodeURIComponent(targetMonth);
      const data = await apiRequest<any>(`/api/relatorios/mensal/pdf?mes=${query}`);
      return {
        success: true,
        fromBackend: true,
        downloadUrl: data?.downloadUrl,
        message: data?.message || `Relatório de ${targetMonth} exportado com sucesso.`,
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        message: err?.message || `Não foi possível gerar o PDF de ${targetMonth} no servidor.`,
      };
    }
  }

  // Navegação de meses
  function prevMonth() {
    const idx = monthsSequence.indexOf(currentMonth.value);
    if (idx > 0) {
      fetchMonthlyClosing(monthsSequence[idx - 1]);
    } else {
      fetchMonthlyClosing('OUTUBRO / 2026');
    }
  }

  function nextMonth() {
    const idx = monthsSequence.indexOf(currentMonth.value);
    if (idx !== -1 && idx < monthsSequence.length - 1) {
      fetchMonthlyClosing(monthsSequence[idx + 1]);
    } else {
      fetchMonthlyClosing('JANEIRO / 2027');
    }
  }

  return {
    currentMonth,
    summary,
    isLoading,
    error,
    fetchMonthlyClosing,
    downloadMonthlyReportPdf,
    prevMonth,
    nextMonth,
  };
});
