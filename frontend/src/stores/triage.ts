import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface AnamneseQuestionResponse {
  id: string;
  label: string;
  sublabel?: string;
  value: boolean;
  valueText?: string;
  isWarningIfTrue?: boolean;
}

export interface AnamneseData {
  allergies: boolean;
  allergiesDetail?: string;
  anticoagulants: boolean;
  keloids: boolean;
  diabetesOrBleeding: boolean;
  pregnantOrLactating: boolean;
  recentMeal: boolean;
  recentMealDetail: string;
  source: 'whatsapp_bot';
  receivedAt: string;
  phoneSender: string;
  botVerificationId: string;
}

export interface TriageClient {
  id: string;
  name: string;
  avatar: string;
  phone: string;
  cpf: string;
  projectTitle: string;
  style: string;
  conceptDescription: string;
  status: 'aguardando' | 'sinal' | 'confirmados';
  statusLabel: string;
  priceTotal: number;
  depositAmount: number;
  depositPaid: boolean;
  depositReceipt?: string;
  timeAgo: string;
  priority: 'alta' | 'media' | 'normal';
  assignedArtist: string;
  station: string;
  estimatedDuration: string;
  lotId: string;
  termNumber: string;
  isTermSigned?: boolean;
  termSignedAt?: string;
  termPdfUrl?: string;
  anamnese: AnamneseData;
  scheduledSlot?: {
    day: string;
    dayLabel: string;
    shift: string;
    shiftLabel: string;
    time: string;
  };
}

export interface SignTermoPayload {
  clientId: string;
  signatureBase64: string;
  termNumber: string;
  agreedAt: string;
  botVerificationId?: string;
  clientCpf?: string;
}

export interface TermoActionResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
  protocol: string;
  pdfUrl?: string;
}

export interface DepositPayload {
  clientId: string;
  receiptFileName: string;
  depositAmount?: number;
  notes?: string;
}

export interface DepositActionResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
  protocol: string;
  statusLabel: string;
}

export interface PreAttendanceLinkResponse {
  success: boolean;
  fromBackend: boolean;
  link: string;
  token: string;
}

export const useTriageStore = defineStore('triage', () => {
  const clients = ref<TriageClient[]>([
    {
      id: 'cli-beatriz',
      name: 'Beatriz Mendes',
      avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=200&q=80',
      phone: '+55 (11) 98765-4321',
      cpf: '***.492.108-**',
      projectTitle: 'Cover-up Old School',
      style: 'Old School Tradicional',
      conceptDescription: 'Pantera Negra & Rosa com sombreamento profundo',
      status: 'aguardando',
      statusLabel: 'AGUARDANDO ORÇAMENTO',
      priceTotal: 560,
      depositAmount: 280,
      depositPaid: false,
      timeAgo: 'Enviado há 3h via WhatsApp Bot',
      priority: 'media',
      assignedArtist: 'Gabriel Dornelles',
      station: 'BANCADA 01',
      estimatedDuration: '3h30',
      lotId: '#NC-8930',
      termNumber: '#TF-8942',
      anamnese: {
        allergies: false,
        anticoagulants: false,
        keloids: false,
        diabetesOrBleeding: false,
        pregnantOrLactating: false,
        recentMeal: true,
        recentMealDetail: 'Alimentação sólida há 2h (refeição leve)',
        source: 'whatsapp_bot',
        receivedAt: 'Hoje às 11:24',
        phoneSender: '+55 11 98765-4321',
        botVerificationId: 'WA-BOT-78419',
      },
      scheduledSlot: {
        day: '29',
        dayLabel: 'QUARTA • 29 MAI',
        shift: 'tarde',
        shiftLabel: 'TURNO TARDE',
        time: '14:30 — 19:00',
      },
    },
    {
      id: 'cli-lucas',
      name: 'Lucas Peixoto',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
      phone: '+55 (11) 97123-8899',
      cpf: '***.721.439-**',
      projectTitle: 'Fine Line Oriental',
      style: 'Fine Line Oriental',
      conceptDescription: 'Dragão ancestral com escamas em traço único',
      status: 'confirmados',
      statusLabel: 'CONFIRMADO',
      priceTotal: 800,
      depositAmount: 400,
      depositPaid: true,
      depositReceipt: 'PIX_LUCAS_PEIXOTO_400.PDF',
      timeAgo: 'Sinal recebido há 25 min',
      priority: 'alta',
      assignedArtist: 'Gabriel Dornelles',
      station: 'BANCADA 02',
      estimatedDuration: '4h00',
      lotId: '#NC-8928',
      termNumber: '#TF-8939',
      anamnese: {
        allergies: false,
        anticoagulants: false,
        keloids: false,
        diabetesOrBleeding: false,
        pregnantOrLactating: false,
        recentMeal: true,
        recentMealDetail: 'Alimentação sólida há 1h30',
        source: 'whatsapp_bot',
        receivedAt: 'Hoje às 13:40',
        phoneSender: '+55 11 97123-8899',
        botVerificationId: 'WA-BOT-81920',
      },
      scheduledSlot: {
        day: '29',
        dayLabel: 'QUARTA • 29 MAI',
        shift: 'tarde',
        shiftLabel: 'TURNO TARDE',
        time: '14:30 — 19:00',
      },
    },
    {
      id: 'cli-helena',
      name: 'Helena Vasco',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
      phone: '+55 (11) 99554-1221',
      cpf: '***.519.803-**',
      projectTitle: 'Fine Line Botânico',
      style: 'Fine Line Botânico',
      conceptDescription: 'Peônia Imperial & Folhagens • Antebraço Direito',
      status: 'sinal',
      statusLabel: 'SINAL PENDENTE',
      priceTotal: 900,
      depositAmount: 450,
      depositPaid: false,
      depositReceipt: '',
      timeAgo: 'Há 8 min via WhatsApp API',
      priority: 'alta',
      assignedArtist: 'Valéria Moretti',
      station: 'BANCADA 01',
      estimatedDuration: '4h30',
      lotId: '#NC-8941',
      termNumber: '#TF-8941',
      anamnese: {
        allergies: false,
        anticoagulants: false,
        keloids: false,
        diabetesOrBleeding: false,
        pregnantOrLactating: false,
        recentMeal: true,
        recentMealDetail: 'Alimentação sólida há 1h',
        source: 'whatsapp_bot',
        receivedAt: 'Hoje às 14:02',
        phoneSender: '+55 11 99554-1221',
        botVerificationId: 'WA-BOT-90214',
      },
      scheduledSlot: {
        day: '29',
        dayLabel: 'QUARTA • 29 MAI',
        shift: 'tarde',
        shiftLabel: 'TURNO TARDE',
        time: '14:30 — 19:00',
      },
    },
    {
      id: 'cli-camila',
      name: 'Camila Albuquerque',
      avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80',
      phone: '+55 (11) 98844-3321',
      cpf: '***.382.918-**',
      projectTitle: 'Estudo de São Jerônimo',
      style: 'FineLine Chiaroscuro',
      conceptDescription: 'Gravura & Chiaroscuro anatômico de antebraço',
      status: 'confirmados',
      statusLabel: 'CONFIRMADO',
      priceTotal: 700,
      depositAmount: 350,
      depositPaid: true,
      depositReceipt: 'PIX_CAMILA_350.PDF',
      timeAgo: 'Confirmado ontem às 18h',
      priority: 'normal',
      assignedArtist: 'Gabriel Dornelles',
      station: 'BANCADA 01',
      estimatedDuration: '4h30',
      lotId: '#NC-8924',
      termNumber: '#TC-2024-0482',
      anamnese: {
        allergies: false,
        anticoagulants: false,
        keloids: false,
        diabetesOrBleeding: false,
        pregnantOrLactating: false,
        recentMeal: true,
        recentMealDetail: 'Alimentação regular há 1h',
        source: 'whatsapp_bot',
        receivedAt: 'Ontem às 17:30',
        phoneSender: '+55 11 98844-3321',
        botVerificationId: 'WA-BOT-67210',
      },
      scheduledSlot: {
        day: '29',
        dayLabel: 'QUARTA • 29 MAI',
        shift: 'tarde',
        shiftLabel: 'TURNO TARDE',
        time: '14:30 — 19:00',
      },
    },
    {
      id: 'cli-mateus',
      name: 'Mateus Silveira Prado',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
      phone: '+55 (11) 98112-9900',
      cpf: '***.382.918-**',
      projectTitle: 'Fine Line Botânico',
      style: 'Fine Line Botânico',
      conceptDescription: 'Peônia Imperial • Antebraço Direito',
      status: 'confirmados',
      statusLabel: 'CONFIRMADO',
      priceTotal: 650,
      depositAmount: 325,
      depositPaid: true,
      depositReceipt: 'PIX_MATEUS_325.PDF',
      timeAgo: 'Confirmado há 1h',
      priority: 'normal',
      assignedArtist: 'Valéria Moretti',
      station: 'BANCADA 01',
      estimatedDuration: '3h30',
      lotId: '#NC-8919',
      termNumber: '#TF-8941',
      anamnese: {
        allergies: false,
        anticoagulants: false,
        keloids: false,
        diabetesOrBleeding: false,
        pregnantOrLactating: false,
        recentMeal: true,
        recentMealDetail: 'Alimentação sólida há 1h',
        source: 'whatsapp_bot',
        receivedAt: 'Hoje às 10:15',
        phoneSender: '+55 11 98112-9900',
        botVerificationId: 'WA-BOT-55198',
      },
      scheduledSlot: {
        day: '29',
        dayLabel: 'QUARTA • 29 MAI',
        shift: 'tarde',
        shiftLabel: 'TURNO TARDE',
        time: '14:30 — 19:00',
      },
    },
  ]);

  const selectedClientId = ref<string>('cli-beatriz');

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const selectedClient = computed<TriageClient>(() => {
    const found = clients.value.find((c) => c.id === selectedClientId.value);
    if (found) return found;
    return clients.value[0] as TriageClient;
  });

  function getClientById(id?: string | null): TriageClient {
    if (!id) return selectedClient.value;
    const found = clients.value.find((c) => c.id === id);
    return found ? found : selectedClient.value;
  }

  function setSelectedClient(id: string) {
    if (clients.value.some((c) => c.id === id)) {
      selectedClientId.value = id;
    }
  }

  function registerDeposit(clientId: string, receiptName = 'COMPROVANTE_PIX.PDF') {
    const client = clients.value.find((c) => c.id === clientId);
    if (client) {
      client.depositPaid = true;
      client.depositReceipt = receiptName;
      client.status = 'confirmados';
      client.statusLabel = 'CONFIRMADO';
    }
  }

  function updateSchedule(
    clientId: string,
    slot: { day: string; dayLabel: string; shift: string; shiftLabel: string; time: string }
  ) {
    const client = clients.value.find((c) => c.id === clientId);
    if (client) {
      client.scheduledSlot = slot;
    }
  }

  // Action com IF: Buscar agendamento e detalhes do cliente no backend (com fallback mock)
  async function fetchClientSchedule(clientId: string): Promise<void> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/agendamentos/${clientId}`);
        if (response.ok) {
          const data = await response.json();
          if (data && data.scheduledSlot) {
            console.log(`[TriageStore] Agendamento do cliente ${clientId} carregado do backend:`, data.scheduledSlot);
            updateSchedule(clientId, data.scheduledSlot);
            return;
          }
        } else {
          console.warn(`[TriageStore] Backend HTTP ${response.status} para agendamento. Mantendo dados mockados.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Backend indisponível para consulta de agendamento. Mantendo mock:', err);
      }
    }
  }

  // Action com IF: Persistir agendamento de sessão (backend vs fallback mock)
  async function saveScheduleSlot(
    clientId: string,
    slot: { day: string; dayLabel: string; shift: string; shiftLabel: string; time: string }
  ): Promise<{ success: boolean; fromBackend: boolean; protocol: string }> {
    // Atualiza o estado local reativo imediatamente
    updateSchedule(clientId, slot);

    // IF 1: Verifica se há endpoint configurado
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/agendamentos/reservar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ clientId, slot }),
        });

        // IF 2: Se o backend respondeu com sucesso
        if (response.ok) {
          const data = await response.json();
          console.log('[TriageStore] Reserva confirmada pelo backend:', data);
          return {
            success: true,
            fromBackend: true,
            protocol: data.protocol || `SCH-SRV-${Date.now().toString().slice(-6)}`,
          };
        } else {
          console.warn(`[TriageStore] Erro HTTP ${response.status} ao salvar reserva. Usando fallback mock.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Falha ao comunicar com backend para reservar sessão. Fallback mock ativado:', err);
      }
    } else {
      console.info('[TriageStore] VITE_API_URL não definida. Reserva efetuada localmente no mock reativo.');
    }

    // Fallback Mock garantido
    return {
      success: true,
      fromBackend: false,
      protocol: `SCH-MOCK-${Date.now().toString().slice(-6)}`,
    };
  }

  // Action com IF: Buscar ficha de anamnese e termo do cliente no backend (com fallback mock)
  async function fetchTermoAnamnese(clientId: string): Promise<void> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/termos/${clientId}`);
        if (response.ok) {
          const data = await response.json();
          if (data && data.anamnese) {
            const client = clients.value.find((c) => c.id === clientId);
            if (client) {
              client.anamnese = { ...client.anamnese, ...data.anamnese };
              if (data.termNumber) client.termNumber = data.termNumber;
              if (data.isTermSigned !== undefined) client.isTermSigned = data.isTermSigned;
            }
            console.log(`[TriageStore] Termo e anamnese do cliente ${clientId} sincronizados do backend:`, data);
            return;
          }
        } else {
          console.warn(`[TriageStore] Backend HTTP ${response.status} para termo. Mantendo ficha mockada.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Backend indisponível para consulta de termo/anamnese:', err);
      }
    } else {
      console.info('[TriageStore] VITE_API_URL não definida. Mantendo ficha de anamnese mockada do bot.');
    }
  }

  // Action com IF: Autenticar e assinar termo notarial de consentimento
  async function signTermo(payload: SignTermoPayload): Promise<TermoActionResponse> {
    const client = clients.value.find((c) => c.id === payload.clientId);
    if (client) {
      client.isTermSigned = true;
      client.termSignedAt = payload.agreedAt;
    }

    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/termos/assinar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        if (response.ok) {
          const data = await response.json();
          console.log('[TriageStore] Termo assinado e autenticado via backend:', data);
          return {
            success: true,
            fromBackend: true,
            protocol: data.protocol || `TERM-AUTH-${Date.now().toString().slice(-6)}`,
            message: data.message || 'Termo autenticado e arquivado no servidor central com sucesso!',
            pdfUrl: data.pdfUrl,
          };
        } else {
          console.warn(`[TriageStore] Erro HTTP ${response.status} ao assinar termo no backend. Fallback ativado.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Falha ao comunicar com backend para autenticação do termo:', err);
      }
    }

    // Fallback Mock seguro
    return {
      success: true,
      fromBackend: false,
      protocol: `TERM-MOCK-${Date.now().toString().slice(-6)}`,
      message: 'Termo autenticado localmente com certificado da bancada (Modo Simulação).',
    };
  }

  // Action com IF: Enviar cópia criptografada do termo assinado via WhatsApp
  async function sendTermoWhatsapp(clientId: string): Promise<TermoActionResponse> {
    const client = clients.value.find((c) => c.id === clientId);

    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/termos/enviar-whatsapp`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            clientId,
            phone: client?.phone,
            termNumber: client?.termNumber,
          }),
        });

        if (response.ok) {
          const data = await response.json();
          return {
            success: true,
            fromBackend: true,
            protocol: data.protocol || `WA-DISPATCH-${Date.now().toString().slice(-6)}`,
            message: data.message || 'Cópia assinada enviada ao WhatsApp do cliente pelo servidor!',
          };
        }
      } catch (err) {
        console.warn('[TriageStore] Falha ao enviar termo via WhatsApp pelo backend:', err);
      }
    }

    // Fallback Mock seguro
    return {
      success: true,
      fromBackend: false,
      protocol: `WA-MOCK-${Date.now().toString().slice(-6)}`,
      message: `Cópia do termo enviada com sucesso para ${client?.phone || 'o WhatsApp do cliente'} (Modo Simulação).`,
    };
  }

  // Action com IF: Buscar fila de triagem de clientes no backend ou manter fallback local
  async function fetchTriageQueue(): Promise<void> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/triagem/clientes`);
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data) && data.length > 0) {
            console.log('[TriageStore] Fila de triagem carregada do backend:', data);
            clients.value = data;
            return;
          } else {
            console.info('[TriageStore] Backend retornou fila vazia. Mantendo dados mockados.');
          }
        } else {
          console.warn(`[TriageStore] Backend HTTP ${response.status} para fila de triagem. Mantendo mock.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Backend indisponível para fila de triagem. Mantendo mock:', err);
      }
    } else {
      console.info('[TriageStore] VITE_API_URL não definida. Consumindo triagem mock local.');
    }
  }

  // Action com IF: Registrar pagamento de sinal com comprovante anexado
  async function registerDepositWithReceipt(payload: DepositPayload): Promise<DepositActionResponse> {
    const client = clients.value.find((c) => c.id === payload.clientId);
    if (client) {
      client.depositPaid = true;
      client.depositReceipt = payload.receiptFileName;
      client.status = 'confirmados';
      client.statusLabel = 'CONFIRMADO';
    }

    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/triagem/sinal`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        if (response.ok) {
          const data = await response.json();
          console.log('[TriageStore] Sinal registrado com sucesso no backend:', data);
          return {
            success: true,
            fromBackend: true,
            message: data.message || 'Sinal validado e registrado no servidor central do atelier!',
            protocol: data.protocol || `PIX-REC-${Date.now().toString().slice(-6)}`,
            statusLabel: 'CONFIRMADO',
          };
        } else {
          console.warn(`[TriageStore] Erro HTTP ${response.status} ao registrar sinal no backend.`);
        }
      } catch (err) {
        console.warn('[TriageStore] Falha ao comunicar com backend para sinal:', err);
      }
    }

    // Fallback Mock seguro
    return {
      success: true,
      fromBackend: false,
      message: 'Sinal de bancada registrado com sucesso (Modo Simulação Local).',
      protocol: `PIX-MOCK-${Date.now().toString().slice(-6)}`,
      statusLabel: 'CONFIRMADO',
    };
  }

  // Action com IF: Gerar link de pré-atendimento WhatsApp
  async function generatePreAttendanceLink(referenceId = 'nct-8941'): Promise<PreAttendanceLinkResponse> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/triagem/link-pre-atendimento`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ referenceId }),
        });

        if (response.ok) {
          const data = await response.json();
          return {
            success: true,
            fromBackend: true,
            link: data.link || `https://tattooflow.app/triagem/novo?ref=${referenceId}`,
            token: data.token || `tok-${Date.now()}`,
          };
        }
      } catch (err) {
        console.warn('[TriageStore] Falha ao gerar link via backend:', err);
      }
    }

    // Fallback Mock
    return {
      success: true,
      fromBackend: false,
      link: `https://tattooflow.app/triagem/novo?ref=${referenceId}`,
      token: `mock-tok-${Date.now()}`,
    };
  }

  return {
    clients,
    selectedClientId,
    selectedClient,
    getClientById,
    setSelectedClient,
    registerDeposit,
    updateSchedule,
    fetchClientSchedule,
    saveScheduleSlot,
    fetchTermoAnamnese,
    signTermo,
    sendTermoWhatsapp,
    fetchTriageQueue,
    registerDepositWithReceipt,
    generatePreAttendanceLink,
  };
});
