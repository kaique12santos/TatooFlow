import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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

const defaultDecemberSummary: MonthlyFinancialSummary = {
  monthYear: 'DEZEMBRO / 2026',
  status: 'conciliado',
  grossRevenue: 78400,
  grossRevenueFormatted: 'R$ 78.400,00',
  growthPercentage: '+18.4%',
  completedWorksCount: 46,
  studioTaxRate: 30,
  studioTaxAmount: 23520,
  studioTaxFormatted: 'R$ 23.520,00',
  artistShareRate: 70,
  artistShareAmount: 54880,
  artistShareFormatted: 'R$ 54.880,00',
  activeArtistsCount: 3,
  pixEscrowAmount: 6200,
  pixEscrowFormatted: 'R$ 6.200,00',
  artistsBreakdown: [
      {
        id: 'gabriel',
        name: 'Gabriel Dornelles',
        station: 'Bancada 03',
        specialty: 'Master Chiaroscuro',
        totalMonth: 'R$ 34.200,00',
        liquidArtist: 'R$ 23.940,00',
        studioTax: 'R$ 10.260,00',
        progressPercentage: 43.6,
        sessionsCount: 18,
      },
      {
        id: 'valeria',
        name: 'Valéria Moretti',
        station: 'Bancada 01',
        specialty: 'Fine Line & Botânica',
        totalMonth: 'R$ 28.500,00',
        liquidArtist: 'R$ 19.950,00',
        studioTax: 'R$ 8.550,00',
        progressPercentage: 36.3,
        sessionsCount: 16,
      },
      {
        id: 'marcus',
        name: 'Marcus V.',
        station: 'Bancada 02',
        specialty: 'Blackwork Gravura',
        totalMonth: 'R$ 15.700,00',
        liquidArtist: 'R$ 10.990,00',
        studioTax: 'R$ 4.710,00',
        progressPercentage: 20.0,
        sessionsCount: 12,
      },
    ],
  };

const defaultMonthlyData: Record<string, MonthlyFinancialSummary> = {
  'DEZEMBRO / 2026': defaultDecemberSummary,
  'NOVEMBRO / 2026': {
    monthYear: 'NOVEMBRO / 2026',
    status: 'conciliado',
    grossRevenue: 66200,
    grossRevenueFormatted: 'R$ 66.200,00',
    growthPercentage: '+12.1%',
    completedWorksCount: 39,
    studioTaxRate: 30,
    studioTaxAmount: 19860,
    studioTaxFormatted: 'R$ 19.860,00',
    artistShareRate: 70,
    artistShareAmount: 46340,
    artistShareFormatted: 'R$ 46.340,00',
    activeArtistsCount: 3,
    pixEscrowAmount: 4800,
    pixEscrowFormatted: 'R$ 4.800,00',
    artistsBreakdown: [
      {
        id: 'gabriel',
        name: 'Gabriel Dornelles',
        station: 'Bancada 03',
        specialty: 'Master Chiaroscuro',
        totalMonth: 'R$ 29.800,00',
        liquidArtist: 'R$ 20.860,00',
        studioTax: 'R$ 8.940,00',
        progressPercentage: 45.0,
        sessionsCount: 15,
      },
      {
        id: 'valeria',
        name: 'Valéria Moretti',
        station: 'Bancada 01',
        specialty: 'Fine Line & Botânica',
        totalMonth: 'R$ 24.100,00',
        liquidArtist: 'R$ 16.870,00',
        studioTax: 'R$ 7.230,00',
        progressPercentage: 36.4,
        sessionsCount: 14,
      },
      {
        id: 'marcus',
        name: 'Marcus V.',
        station: 'Bancada 02',
        specialty: 'Blackwork Gravura',
        totalMonth: 'R$ 12.300,00',
        liquidArtist: 'R$ 8.610,00',
        studioTax: 'R$ 3.690,00',
        progressPercentage: 18.6,
        sessionsCount: 10,
      },
    ],
  },
  'JANEIRO / 2027': {
    monthYear: 'JANEIRO / 2027',
    status: 'em_aberto',
    grossRevenue: 42100,
    grossRevenueFormatted: 'R$ 42.100,00',
    growthPercentage: '+5.2%',
    completedWorksCount: 22,
    studioTaxRate: 30,
    studioTaxAmount: 12630,
    studioTaxFormatted: 'R$ 12.630,00',
    artistShareRate: 70,
    artistShareAmount: 29470,
    artistShareFormatted: 'R$ 29.470,00',
    activeArtistsCount: 3,
    pixEscrowAmount: 8900,
    pixEscrowFormatted: 'R$ 8.900,00',
    artistsBreakdown: [
      {
        id: 'gabriel',
        name: 'Gabriel Dornelles',
        station: 'Bancada 03',
        specialty: 'Master Chiaroscuro',
        totalMonth: 'R$ 18.000,00',
        liquidArtist: 'R$ 12.600,00',
        studioTax: 'R$ 5.400,00',
        progressPercentage: 42.7,
        sessionsCount: 9,
      },
      {
        id: 'valeria',
        name: 'Valéria Moretti',
        station: 'Bancada 01',
        specialty: 'Fine Line & Botânica',
        totalMonth: 'R$ 15.600,00',
        liquidArtist: 'R$ 10.920,00',
        studioTax: 'R$ 4.680,00',
        progressPercentage: 37.0,
        sessionsCount: 8,
      },
      {
        id: 'marcus',
        name: 'Marcus V.',
        station: 'Bancada 02',
        specialty: 'Blackwork Gravura',
        totalMonth: 'R$ 8.500,00',
        liquidArtist: 'R$ 5.950,00',
        studioTax: 'R$ 2.550,00',
        progressPercentage: 20.3,
        sessionsCount: 5,
      },
    ],
  },
};

export const useFinancialStore = defineStore('financial', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const currentMonth = ref<string>('DEZEMBRO / 2026');
  const summary = ref<MonthlyFinancialSummary>({ ...defaultDecemberSummary });
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  const monthsSequence = ['NOVEMBRO / 2026', 'DEZEMBRO / 2026', 'JANEIRO / 2027'];

  // Action com IF: Carrega fechamento consolidado mensal do backend ou ativa fallback mock
  async function fetchMonthlyClosing(targetMonth: string = currentMonth.value): Promise<void> {
    isLoading.value = true;
    error.value = null;
    currentMonth.value = targetMonth;

    // IF 1: Verifica se a URL do backend está configurada
    if (apiBaseUrl) {
      try {
        const query = encodeURIComponent(targetMonth);
        const response = await fetch(`${apiBaseUrl}/api/gestao/fechamento?mes=${query}`);

        // IF 2: Se a requisição HTTP foi bem-sucedida
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou dados consistentes de fechamento
          if (data && (data.grossRevenue !== undefined || data.grossRevenueFormatted)) {
            console.log('[FinancialStore] Fechamento mensal carregado do servidor:', data);
            summary.value = {
              ...summary.value,
              ...data,
            };
            isLoading.value = false;
            return;
          } else {
            console.info('[FinancialStore] Resposta do backend incompleta. Mantendo base mock.');
          }
        } else {
          console.warn(`[FinancialStore] Backend retornou HTTP ${response.status}. Utilizando fallback mock.`);
        }
      } catch (err) {
        console.warn('[FinancialStore] Falha ao comunicar com endpoint de fechamento financeiro:', err);
      }
    } else {
      console.info('[FinancialStore] VITE_API_URL não configurada. Consumindo fechamento do livro-razão mock.');
    }

    // Fallback Mock com suporte a múltiplos meses
    const mock = defaultMonthlyData[targetMonth] ?? defaultDecemberSummary;
    summary.value = { ...mock, monthYear: targetMonth };
    isLoading.value = false;
  }

  // Action com IF: Solicita geração ou download do relatório mensal em PDF
  async function downloadMonthlyReportPdf(targetMonth: string = currentMonth.value): Promise<FinancialActionResponse> {
    // IF 1: Verifica se o backend de auditoria notarial está disponível
    if (apiBaseUrl) {
      try {
        const query = encodeURIComponent(targetMonth);
        const response = await fetch(`${apiBaseUrl}/api/gestao/fechamento/pdf?mes=${query}`);

        // IF 2: Se o backend retornou o PDF ou link assinado
        if (response.ok) {
          const data = await response.json();
          return {
            success: true,
            fromBackend: true,
            downloadUrl: data.downloadUrl || `${apiBaseUrl}/api/gestao/fechamento/pdf/download?mes=${query}`,
            message: data.message || `Relatório contábil de ${targetMonth} gerado com certificação notarial!`,
          };
        }
      } catch (err) {
        console.warn('[FinancialStore] Falha ao emitir PDF no backend. Simulando localmente:', err);
      }
    }

    // Fallback Mock
    return {
      success: true,
      fromBackend: false,
      downloadUrl: '#',
      message: `Relatório consolidado de ${targetMonth} exportado com sucesso (Modo Simulação Notarial).`,
    };
  }

  // Navegação de meses
  function prevMonth() {
    const idx = monthsSequence.indexOf(currentMonth.value);
    if (idx > 0) {
      fetchMonthlyClosing(monthsSequence[idx - 1]);
    } else {
      fetchMonthlyClosing('NOVEMBRO / 2026');
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
