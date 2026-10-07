import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { SessionCardItem } from '../components/SessionCard.vue';
import { Syringe, CheckCircle2, AlertCircle, FileText, MessageSquare, Pause, Play } from '@lucide/vue';

export type { SessionCardItem };

export interface AgendaFilterTab {
  id: string;
  label: string;
  count: number;
  dotColor?: string;
}

export interface CalendarDay {
  dayName: string;
  dayNum: number;
  status: string;
  badgeColor: string;
}

const figmaAssetBase = 'https://s3-alpha-sig.figma.com/img';
const clientImg1 = `${figmaAssetBase}/3cf6/36d9/7bad3958736403c58cf36020a928d6e1?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=PKq2wjmGhVsDNbpzhPVcftEnWeyZqyeGvGYDqAsq3R0v3JCiXveFRpfI~9AFZD-RWPUx1H8WMnXO6Wt8-VyYVD4rl8X7GhdG6K9esx90B36IeHBKCQR5Kbw2U7HtkGBQT-nCNlaRUpA4cfNZjo8v33yfk6NhBp~TR9iVVORmFhRJpxdQR2KhkC~aK7ByXSFZtp5tdN6GYfdiLwm2k5Y96-fcWLqRjIu3Xbo-M7RyLJ0lidNTQ8LqveXYg64iiDupGkVLx2N2ctk6jxiRMr5awtwvm0cehhDauCGLepw7NhaB5dvZhwFy1QAdBx~lHD1Fu6f--aIyaQ6a93NL2-CUfQ__`;
const clientImg2 = `${figmaAssetBase}/da92/b8b5/59eceba032c310088964928b7aea3c8b?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=OqSwHmcW9VIXkJfAHOFT5MGfx7YlwMxNm3Ms1PimRaDtCQwohl2MHJHN38J0rLOYAP58z9HolIToUV3izicZD5wfGqH4iPDmRti3ZqBCveK1i7JYDdFCMmCZ7btYoPUiVFCYbu22mB0x268ATYA0nsjoA5U6GvxGKeQgwH504~Hm~wWfXEcRbVG5fNe7EooLawSw6wwxZdjF8ZEYDNGb8zHpEA3BxzHT3E~vYYWwOSHvmR0ByH8Ar8vqaHEIfPYSmeW5D3P~N8aN5T22C~YV2H3bGMpb~aoyE8w-y3VZhT8kKXyCD70rRk3pERz-ZnmS~RAPktoVsQVfV3OxIn3lSA__`;
const clientImg3 = `${figmaAssetBase}/9961/f0f2/bc1a212219da123c5166270ee50e5f94?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=POHUW~rdT1I4TVFY3mO4JhRBluahXCMA8iOEeZBSiU0Oq0kn1OuEZHlU4blhLOk39dO0R8crlSY4qjnj2rwVovpiTLfyR-rgHAOjYPyPILsBqwX1GCwYy5BOTk2PHK0KtvYvWahwYP~hoddVmf5qL5CSqPXgxQ-pFwqlDMcauLDUN9dISbzIxJ~lCq71iMudRsJSpfHObC5loHP7HsrQn~djwxDeooHrSv1ynIwKoJF~G9JfQAWjTIQ0z1zaZLu8cEpy98eqRZcL3WYtlDxhCpnLkSNwVEAyLTEA-6xL46ygnK~JXIEUhvdW0XTmdT-xy-fzbkJMMGd5r8anak3Gww__`;

const defaultAppointments: SessionCardItem[] = [
  {
    id: 1,
    category: 'andamento',
    time: '10:00 — 13:30',
    typeIndicatorColor: 'bg-[#c9a86a]',
    statusText: 'EM AGULHAMENTO',
    statusBadgeStyle: 'bg-[#3d301b] text-[#eedaa2] border border-[rgba(238,218,162,0.2)]',
    statusIcon: Syringe,
    image: clientImg1,
    clientName: 'Rodrigo Sampaio',
    tattooTitle: 'Arcanjo Miguel · Chiaroscuro',
    tattooArea: 'Fechamento Braço',
    sessionMeta: 'SESSÃO 03/05 • AGULHA 07RL / 15M1',
    ledgerLabels: ['HONORÁRIO TOTAL', 'SINAL PIX RETIDO', 'SALDO RESTANTE'],
    ledgerValues: ['R$ 2.400,00', 'R$ 800,00 (100%)', 'R$ 1.600,00'],
    ledgerValueColors: ['', 'text-[#c8e6c9]', 'text-[#e6c383]'],
    actions: [
      { label: 'ANAMNESE', icon: FileText, primary: false },
      { label: 'WHATSAPP', icon: MessageSquare, primary: false },
      { label: 'PAUSAR', icon: Pause, primary: true },
    ],
  },
  {
    id: 2,
    category: 'confirmados',
    time: '14:00 — 18:00',
    typeIndicatorColor: 'bg-[#c8e6c9]',
    statusText: 'SINAL PIX LIQUIDADO',
    statusBadgeStyle: 'bg-[#2a3b2e] text-[#c8e6c9] border border-[rgba(200,230,201,0.2)]',
    statusIcon: CheckCircle2,
    image: clientImg2,
    clientName: 'Helena Brandão',
    tattooTitle: 'Botânica & Serpente · Gravura Cobre',
    tattooArea: 'SESSÃO ÚNICA • ANTEBRAÇO EXT.',
    sessionMeta: 'PROJETO FINALIZADO E APROVADO',
    ledgerLabels: ['VALOR ACORDADO', 'SINAL CAUÇÃO', 'A COBRAR'],
    ledgerValues: ['R$ 1.500,00', 'R$ 500,00', 'R$ 1.000,00'],
    ledgerValueColors: ['', 'text-[#c8e6c9]', 'text-[#e6c383]'],
    actions: [
      { label: 'PREPARAR BANCADA', icon: Syringe, primary: false },
      { label: 'VER TERMO', icon: FileText, primary: false },
      { label: 'INICIAR', icon: Play, primary: true },
    ],
  },
  {
    id: 3,
    category: 'orcamentos',
    time: '18:30 — 19:15',
    typeIndicatorColor: 'bg-[#b8d5f5]',
    statusText: 'AVALIAÇÃO / DECALQUE',
    statusBadgeStyle: 'bg-[#182232] text-[#b8d5f5] border border-[rgba(184,213,245,0.2)]',
    statusIcon: AlertCircle,
    image: clientImg3,
    clientName: 'Matheus Falk',
    tattooTitle: 'Vanitas Costela · Ensaio Anatômico',
    tattooArea: 'PRIMEIRA CONSULTA • MEDIÇÃO DE ANATOMIA',
    sessionMeta: 'AVALIAÇÃO PRESENCIAL',
    ledgerLabels: ['ESTIMATIVA PRÉVIA', 'STATUS DO SINAL', 'SESSÕES EST.'],
    ledgerValues: ['R$ 1.800 — R$ 2.200', 'Pendente Decisão', '02 SESSÕES'],
    ledgerValueColors: ['', 'text-[#fadcd9]', 'text-[#a39e93]'],
    actions: [
      { label: 'VER TERMO', icon: FileText, primary: false },
      { label: 'ORÇAMENTO', icon: FileText, primary: false },
      { label: 'CONFIRMAR', icon: CheckCircle2, primary: true },
    ],
  },
];

export const useAgendaStore = defineStore('agenda', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const appointments = ref<SessionCardItem[]>([...defaultAppointments]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Action: Buscar sessões na API com fallback para dados mockados
  async function fetchAppointments(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    // IF 1: Verifica se existe URL base configurada para comunicação com o backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/agenda/sessoes`);

        // IF 2: Se a resposta do backend foi bem-sucedida (HTTP 200)
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou um array com dados reais
          if (Array.isArray(data) && data.length > 0) {
            console.log('[AgendaStore] Dados da agenda carregados com sucesso do backend:', data);
            appointments.value = data;
            isLoading.value = false;
            return;
          } else {
            console.info('[AgendaStore] Backend retornou lista vazia. Mantendo dados mockados locais.');
          }
        } else {
          console.warn(`[AgendaStore] Backend retornou status HTTP ${response.status}. Utilizando fallback mock.`);
        }
      } catch (err: any) {
        console.warn('[AgendaStore] Falha ao comunicar com endpoint /api/agenda/sessoes. Ativando fallback mock:', err);
        error.value = err?.message || 'Falha na conexão';
      }
    } else {
      console.info('[AgendaStore] VITE_API_URL não definida. Executando agenda com dados mockados reativos.');
    }

    // Fallback Mock garantido
    isLoading.value = false;
  }

  // Action: Atualizar status de uma sessão
  async function updateSessionStatus(sessionId: number | string, newStatus: string): Promise<boolean> {
    const session = appointments.value.find(s => String(s.id) === String(sessionId));
    if (session) {
      session.statusText = newStatus;
    }

    // IF 1: Envia atualização ao backend caso disponível
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/agenda/sessoes/${sessionId}/status`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: newStatus }),
        });
        if (response.ok) {
          console.log(`[AgendaStore] Status da sessão #${sessionId} atualizado no backend.`);
          return true;
        }
      } catch (err) {
        console.warn('[AgendaStore] Erro ao sincronizar status com backend:', err);
      }
    }

    return true;
  }

  return {
    appointments,
    isLoading,
    error,
    fetchAppointments,
    updateSessionStatus,
  };
});
