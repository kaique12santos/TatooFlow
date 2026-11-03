import { defineStore } from 'pinia';
import { ref } from 'vue';
import { apiRequest } from '@/api/http';

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

export const useInvitesStore = defineStore('invites', () => {
  const invites = ref<InviteItem[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  async function fetchInvites(): Promise<void> {
    isLoading.value = true;
    error.value = null;
    try {
      invites.value = await apiRequest<InviteItem[]>('/api/equipe/convites');
    } catch (err) {
      invites.value = [];
      error.value = err instanceof Error ? err.message : 'Não foi possível carregar os convites.';
      console.error('[InvitesStore] Falha ao carregar convites do backend:', err);
    }
    isLoading.value = false;
  }

  async function createInvite(payload: CreateInvitePayload): Promise<InviteActionResponse> {
    isLoading.value = true;
    try {
      const newInvite = await apiRequest<InviteItem>('/api/equipe/convites', {
        method: 'POST',
        body: JSON.stringify({ emailConvidado: payload.email }),
      });
      invites.value.unshift(newInvite);
      return {
        success: true,
        fromBackend: true,
        invite: newInvite,
        message: `Convite enviado com sucesso para ${payload.email}.`,
      };
    } catch (err) {
      return {
        success: false,
        fromBackend: true,
        message: err instanceof Error ? err.message : 'Não foi possível criar o convite.',
      };
    } finally {
      isLoading.value = false;
    }
  }

  async function resendInvite(inviteIdOrName: string): Promise<InviteActionResponse> {
    const invite = invites.value.find(
      (inv) => inv.id === inviteIdOrName || inv.name.toLowerCase() === inviteIdOrName.toLowerCase()
    );

    if (invite) {
      try {
        const updatedInvite = await apiRequest<InviteItem>(`/api/equipe/convites/${invite.id}/reenviar`, {
          method: 'POST',
        });
        const index = invites.value.findIndex(item => item.id === invite.id);
        if (index >= 0) invites.value[index] = updatedInvite;
        return {
          success: true,
          fromBackend: true,
          invite: updatedInvite,
          message: `Convite para ${invite.email} reenviado com sucesso.`,
        };
      } catch (err) {
        return {
          success: false,
          fromBackend: true,
          message: err instanceof Error ? err.message : 'Não foi possível reenviar o convite.',
        };
      }
    }

    return {
      success: false,
      fromBackend: true,
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
