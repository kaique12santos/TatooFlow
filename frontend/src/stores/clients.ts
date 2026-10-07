import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface ClientItem {
  id: string;
  name: string;
  initials: string;
  dossierNumber: string;
  phone: string;
  cpf: string;
  projectTitle: string;
  tattooLocation: string;
  style: string;
  sessionsCount: number;
  status: 'valid' | 'pending' | 'finished';
  statusLabel: string;
}

export interface ClientArtwork {
  id: string;
  badgeStatus: string;
  dateAndDuration: string;
  title: string;
  conceptDescription: string;
  image: string;
  tags: string[];
  rawLabel: string;
  artistResponsible: string;
  signedTermAvailable: boolean;
  rawPhotosCount: number;
  healingStatus?: string;
}

export interface ClientDossier {
  id: string;
  name: string;
  initials: string;
  phone: string;
  cpf: string;
  dossierNumber: string;
  worksCount: number;
  scheduledWorksCount: number;
  dermalProfile: {
    fitzpatrick: string;
    description: string;
  };
  anamneseStatus: {
    title: string;
    notes: string;
    badge: string;
  };
  artworks: ClientArtwork[];
  preMessageText: string;
  posMessageText: string;
}

const defaultClients: ClientItem[] = [
  {
    id: 'c-0482',
    name: 'Camila Albuquerque',
    initials: 'CA',
    dossierNumber: 'DOSSIÊ #0482',
    phone: '+55 (11) 98844-3321',
    cpf: '341.890.112-09',
    projectTitle: 'Estudo de São Jerônimo',
    tattooLocation: 'Antebraço Direito',
    style: 'Gravura & Chiaroscuro',
    sessionsCount: 3,
    status: 'valid',
    statusLabel: 'ANAMNESE VÁLIDA',
  },
  {
    id: 'c-0483',
    name: 'Marcos Vinícius Silva',
    initials: 'MS',
    dossierNumber: 'DOSSIÊ #0483',
    phone: '+55 (11) 97722-1144',
    cpf: '218.441.982-45',
    projectTitle: 'Fechamento Costas Oriental (Ryū)',
    tattooLocation: 'Dorso Completo',
    style: 'Irezumi Contemporâneo',
    sessionsCount: 5,
    status: 'pending',
    statusLabel: 'TERMO PENDENTE',
  },
  {
    id: 'c-0484',
    name: 'Mariana Rios Barreto',
    initials: 'MR',
    dossierNumber: 'DOSSIÊ #0484',
    phone: '+55 (21) 99133-8820',
    cpf: '452.109.873-71',
    projectTitle: 'Micro-realismo Botânico',
    tattooLocation: 'Costela Esquerda',
    style: 'Fineline Botânico',
    sessionsCount: 1,
    status: 'valid',
    statusLabel: 'ANAMNESE VÁLIDA',
  },
  {
    id: 'c-0485',
    name: 'Rodrigo Santoro de Souza',
    initials: 'RS',
    dossierNumber: 'DOSSIÊ #0485',
    phone: '+55 (11) 96655-4433',
    cpf: '119.782.341-88',
    projectTitle: 'Blackwork Ornamental & Geometria Sagrada',
    tattooLocation: 'Manga Completa',
    style: 'Blackwork Pesado',
    sessionsCount: 4,
    status: 'finished',
    statusLabel: 'CONCLUÍDO',
  },
];

const defaultCamilaDossier: ClientDossier = {
  id: 'c-0482',
  name: 'Camila Albuquerque',
  initials: 'CA',
  phone: '+55 11 98844-3321',
  cpf: '***.382.918-**',
  dossierNumber: '#0482',
  worksCount: 3,
  scheduledWorksCount: 1,
  dermalProfile: {
    fitzpatrick: 'FITZPATRICK II',
    description: 'Boa retenção de cinzas',
  },
  anamneseStatus: {
    title: 'ANAMNESE VÁLIDA',
    notes: 'Sem alergias a pigmentos minerais / orgânicos',
    badge: 'APROVADA',
  },
  artworks: [
    {
      id: 'art-01',
      badgeStatus: 'OBRA SELADA & CURADA',
      dateAndDuration: '16/OUT/2024 • 4H EXEC.',
      title: 'Estudo de São Jerônimo',
      conceptDescription: 'Manga Chiaroscuro Renascentista com gradiente profundo de sombras e contraste dramático.',
      image: '/assets/sao_jeronimo_tattoo.jpg',
      tags: ['#CHIAROSCURO', '#RENASCENTISTA'],
      rawLabel: 'Foto RAW 4K',
      artistResponsible: 'Gabriel D. (Master)',
      signedTermAvailable: true,
      rawPhotosCount: 3,
      healingStatus: 'Curada 100%',
    },
    {
      id: 'art-02',
      badgeStatus: 'PROTOCOLO D+30 CONCLUÍDO',
      dateAndDuration: '22/AGO/2024 • 2H30 EXEC.',
      title: 'Peônia Imperial Fine Line',
      conceptDescription: 'Linha única contínua com técnica de micro-pontilhismo nos pistilos e pétalas fluidas.',
      image: '/assets/tattoo_healing_check.jpg',
      tags: ['#FINELINE', '#BOTANICO'],
      rawLabel: 'Foto RAW 4K',
      artistResponsible: 'Gabriel D. (Master)',
      signedTermAvailable: true,
      rawPhotosCount: 2,
      healingStatus: 'Perfeita e uniforme',
    },
  ],
  preMessageText:
    'Olá! Seu horário no New Concept Tattoo está próximo. Recomendamos boa hidratação da pele, evitar exposição solar direta e não ingerir bebidas alcoólicas 24h antes.',
  posMessageText:
    'Lembrete D+15: Solicitação de envio de foto em luz natural para avaliação de regeneração epitelial da sessão.',
};

export const useClientsStore = defineStore('clients', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const clients = ref<ClientItem[]>([...defaultClients]);
  const activeDossier = ref<ClientDossier>({ ...defaultCamilaDossier });
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Action com IF: Buscar clientes no backend com fallback para o catálogo mockado
  async function fetchClients(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    // IF 1: Verifica se há backend configurado
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/clientes`);

        // IF 2: Se a resposta foi bem-sucedida
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou uma lista não vazia de clientes
          if (Array.isArray(data) && data.length > 0) {
            console.log('[ClientsStore] Clientes carregados do backend com sucesso:', data);
            clients.value = data;
            isLoading.value = false;
            return;
          } else {
            console.info('[ClientsStore] Backend retornou lista vazia. Mantendo catálogo mockado.');
          }
        } else {
          console.warn(`[ClientsStore] Backend HTTP ${response.status}. Usando fallback mock local.`);
        }
      } catch (err: any) {
        console.warn('[ClientsStore] Erro ao conectar ao endpoint /api/clientes. Ativando fallback mock:', err);
        error.value = err?.message || 'Falha na conexão com backend';
      }
    } else {
      console.info('[ClientsStore] VITE_API_URL não configurada. Catálogo mantido no mock reativo local.');
    }

    isLoading.value = false;
  }

  // Action com IF: Buscar Dossiê completo do cliente pelo ID (backend vs mock fallback)
  async function fetchClientDossier(clientId: string): Promise<ClientDossier> {
    isLoading.value = true;

    // IF 1: Verifica backend configurado
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/clientes/${clientId}/dossie`);

        // IF 2: Se o backend respondeu com sucesso
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se os dados do dossiê são válidos
          if (data && data.name) {
            console.log(`[ClientsStore] Dossiê do cliente ${clientId} carregado do backend:`, data);
            activeDossier.value = data;
            isLoading.value = false;
            return data;
          }
        } else {
          console.warn(`[ClientsStore] Backend HTTP ${response.status} para dossiê. Usando fallback mock.`);
        }
      } catch (err) {
        console.warn('[ClientsStore] Erro ao obter dossiê do backend. Ativando fallback mock:', err);
      }
    }

    // Fallback Mock: localiza dados do cliente na lista ou fallback para Camila
    const foundClient = clients.value.find((c) => c.id === clientId);
    const client = foundClient || defaultClients[0]!;
    const mockDossier: ClientDossier = {
      ...defaultCamilaDossier,
      id: client.id,
      name: client.name,
      initials: client.initials,
      phone: client.phone,
      cpf: client.cpf,
      dossierNumber: client.dossierNumber,
      worksCount: client.sessionsCount || 3,
    };

    activeDossier.value = mockDossier;
    isLoading.value = false;
    return mockDossier;
  }

  // Action com IF: Atualizar mensagem de pré ou pós-cuidado
  async function updateDossierMessage(
    clientId: string,
    type: 'pre' | 'pos',
    message: string
  ): Promise<{ success: boolean; fromBackend: boolean; message: string }> {
    if (type === 'pre') {
      activeDossier.value.preMessageText = message;
    } else {
      activeDossier.value.posMessageText = message;
    }

    // IF 1: Tenta atualizar no backend se disponível
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/clientes/${clientId}/mensagens`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ type, message }),
        });

        if (response.ok) {
          return {
            success: true,
            fromBackend: true,
            message: 'Mensagem atualizada com sucesso no servidor!',
          };
        }
      } catch (err) {
        console.warn('[ClientsStore] Erro ao sincronizar mensagem com backend:', err);
      }
    }

    // Fallback Mock
    return {
      success: true,
      fromBackend: false,
      message: 'Mensagem atualizada localmente com sucesso (Modo Simulação).',
    };
  }

  // Action com IF: Disparar termo por WhatsApp
  async function dispatchTermoWhatsapp(
    clientId: string
  ): Promise<{ success: boolean; fromBackend: boolean; message: string }> {
    const client = activeDossier.value;

    // IF 1: Envia disparo via backend se disponível
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/clientes/${clientId}/disparo-termo`, {
          method: 'POST',
        });

        if (response.ok) {
          return {
            success: true,
            fromBackend: true,
            message: `Termo de consentimento disparado com sucesso via servidor para ${client.name} (${client.phone}).`,
          };
        }
      } catch (err) {
        console.warn('[ClientsStore] Erro ao disparar termo via backend:', err);
      }
    }

    // Fallback Mock
    return {
      success: true,
      fromBackend: false,
      message: `Termo de consentimento disparado via WhatsApp para ${client.name} (${client.phone}) (Modo Simulação).`,
    };
  }

  function getClientById(id: string): ClientItem | undefined {
    return clients.value.find((c) => c.id === id);
  }

  return {
    clients,
    activeDossier,
    isLoading,
    error,
    fetchClients,
    fetchClientDossier,
    updateDossierMessage,
    dispatchTermoWhatsapp,
    getClientById,
  };
});
