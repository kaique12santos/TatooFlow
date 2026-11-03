import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface InviteItem {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: string;
  station: string;
  pin: string;
  validity: string;
  status: 'pending' | 'active' | 'expired';
  statusText: string;
  createdAt: string;
  token: string;
}

export interface CreateInvitePayload {
  name: string;
  email: string;
  phone: string;
  role?: string;
  station?: string;
  autoPin: boolean;
  pin: string;
  validity: string;
}

export interface InviteActionResponse {
  success: boolean;
  fromBackend: boolean;
  invite?: InviteItem;
  message: string;
}

const defaultInvites: InviteItem[] = [
  {
    id: 'inv-001',
    name: 'Lucas Peixoto',
    email: 'lucas.peixoto@newconcept.art',
    phone: '+55 (11) 97123-8899',
    role: 'Tatuador Convidado',
    station: 'Bancada 03',
    pin: '831 • 402',
    validity: '48h',
    status: 'pending',
    statusText: 'Enviado há 2h • Aguardando ativação',
    createdAt: new Date(Date.now() - 2 * 3600 * 1000).toISOString(),
    token: 'tk-lucas-831402',
  },
  {
    id: 'inv-002',
    name: 'Dra. Clarice V.',
    email: 'clarice.dermato@newconcept.art',
    phone: '+55 (11) 98344-1290',
    role: 'Dermatologista Consultora',
    station: 'Consultório Clínico',
    pin: '902 • 115',
    validity: '24h',
    status: 'pending',
    statusText: 'Enviado há 14h • Link expirando',
    createdAt: new Date(Date.now() - 14 * 3600 * 1000).toISOString(),
    token: 'tk-clarice-902115',
  },
];

export const useInvitesStore = defineStore('invites', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const invites = ref<InviteItem[]>([...defaultInvites]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Action com IF: Sincroniza convites pendentes do backend ou mantém fallback mock
  async function fetchInvites(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    // IF 1: Verifica se a URL base do backend está configurada
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/convites`);

        // IF 2: Se a requisição HTTP foi bem-sucedida
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou uma lista não vazia
          if (Array.isArray(data) && data.length > 0) {
            console.log('[InvitesStore] Convites carregados do backend:', data);
            invites.value = data;
            isLoading.value = false;
            return;
          } else {
            console.info('[InvitesStore] Backend retornou lista vazia. Mantendo mock local.');
          }
        } else {
          console.warn(`[InvitesStore] Backend retornou status HTTP ${response.status}. Utilizando fallback mock.`);
        }
      } catch (err: any) {
        console.warn('[InvitesStore] Falha de conexão com /api/equipe/convites. Ativando fallback mock:', err);
        error.value = err?.message || 'Falha de comunicação';
      }
    } else {
      console.info('[InvitesStore] VITE_API_URL não configurada. Operando com dados mockados de convites.');
    }

    isLoading.value = false;
  }

  // Action com IF: Disparar novo convite para artista/equipe
  async function createInvite(payload: CreateInvitePayload): Promise<InviteActionResponse> {
    isLoading.value = true;

    // IF 1: Envia para o backend se disponível
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/convites`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        // IF 2: Resposta OK do backend
        if (response.ok) {
          const newInvite: InviteItem = await response.json();
          invites.value.unshift(newInvite);
          isLoading.value = false;
          return {
            success: true,
            fromBackend: true,
            invite: newInvite,
            message: `Convite enviado com sucesso pelo servidor para ${payload.name}.`,
          };
        } else {
          console.warn(`[InvitesStore] Falha HTTP ${response.status} ao criar convite. Aplicando fallback.`);
        }
      } catch (err) {
        console.warn('[InvitesStore] Erro ao conectar ao endpoint de convites. Aplicando fallback mock:', err);
      }
    }

    // Fallback Mock Local: cria convite reativo no estado
    const mockInvite: InviteItem = {
      id: `inv-${Date.now().toString().slice(-4)}`,
      name: payload.name,
      email: payload.email,
      phone: payload.phone,
      role: payload.role || 'Artista Convidado',
      station: payload.station || 'Bancada Livre',
      pin: payload.pin,
      validity: payload.validity,
      status: 'pending',
      statusText: 'Enviado agora • Aguardando ativação',
      createdAt: new Date().toISOString(),
      token: `tk-${Math.random().toString(36).substring(2, 9)}`,
    };

    invites.value.unshift(mockInvite);
    isLoading.value = false;

    return {
      success: true,
      fromBackend: false,
      invite: mockInvite,
      message: `Convite gerado e registrado localmente para ${payload.name} (Modo Simulação).`,
    };
  }

  // Action com IF: Reenviar convite pendente
  async function resendInvite(inviteIdOrName: string): Promise<InviteActionResponse> {
    const invite = invites.value.find(
      (inv) => inv.id === inviteIdOrName || inv.name.toLowerCase() === inviteIdOrName.toLowerCase()
    );

    // IF 1: Tenta reenviar no backend se configurado
    if (apiBaseUrl && invite) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/convites/${invite.id}/reenviar`, {
          method: 'POST',
        });

        if (response.ok) {
          invite.statusText = 'Reenviado agora • Aguardando ativação';
          return {
            success: true,
            fromBackend: true,
            invite,
            message: `Convite reenviado pelo backend para ${invite.name}.`,
          };
        }
      } catch (err) {
        console.warn('[InvitesStore] Erro ao reenviar convite via backend:', err);
      }
    }

    // Fallback Mock
    if (invite) {
      invite.statusText = 'Reenviado agora • Link renovado';
      return {
        success: true,
        fromBackend: false,
        invite,
        message: `Convite para ${invite.name} reenviado com sucesso (Modo Simulação).`,
      };
    }

    return {
      success: false,
      fromBackend: false,
      message: 'Convite não localizado.',
    };
  }

  return {
    invites,
    isLoading,
    error,
    fetchInvites,
    createInvite,
    resendInvite,
  };
});
