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

  // Sincroniza convites reais cadastrados no banco via /api/convites
  async function fetchInvites(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    try {
      const data = await apiRequest<any[]>('/api/convites');
      if (Array.isArray(data)) {
        invites.value = data.map((c: any) => {
          const email = c.emailConvidado || '';
          const name = email.split('@')[0] || `Convite #${c.id}`;
          const isExpired = Boolean(c.dataExpiracao && new Date(c.dataExpiracao).getTime() < Date.now());
          const status: 'pending' | 'active' | 'expired' = c.utilizado
            ? 'active'
            : (isExpired ? 'expired' : 'pending');

          const statusText = c.utilizado
            ? 'Convite ativado'
            : (isExpired ? 'Link expirado' : 'Aguardando ativação');

          return {
            id: String(c.id),
            name,
            email,
            phone: '',
            role: 'Artista Convidado',
            station: 'Bancada',
            pin: c.codigo,
            validity: c.dataExpiracao ? new Date(c.dataExpiracao).toLocaleDateString('pt-BR') : '48h',
            status,
            statusText,
            createdAt: '',
            token: c.codigo,
          };
        });
      } else {
        invites.value = [];
      }
    } catch (err: any) {
      invites.value = [];
      error.value = err?.message || 'Erro ao sincronizar convites do banco.';
      console.warn('[InvitesStore] Erro ao carregar convites:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // Disparar novo convite para artista/equipe no banco
  async function createInvite(payload: CreateInvitePayload): Promise<InviteActionResponse> {
    isLoading.value = true;

    try {
      const created = await apiRequest<any>('/api/convites', {
        method: 'POST',
        body: JSON.stringify({ emailConvidado: payload.email }),
      });

      const newInvite: InviteItem = {
        id: String(created.id),
        name: payload.name || payload.email.split('@')[0] || `Convite #${created.id}`,
        email: created.emailConvidado || payload.email,
        phone: payload.phone,
        role: payload.role || 'Artista Convidado',
        station: payload.station || 'Bancada 01',
        pin: created.codigo || payload.pin,
        validity: payload.validity || '48h',
        status: 'pending',
        statusText: 'Aguardando ativação',
        createdAt: new Date().toISOString(),
        token: created.codigo || '',
      };

      invites.value.unshift(newInvite);
      return {
        success: true,
        fromBackend: true,
        invite: newInvite,
        message: `Convite criado no banco com código ${created.codigo} para ${payload.email}.`,
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        message: err?.message || 'Erro ao emitir convite no servidor.',
      };
    } finally {
      isLoading.value = false;
    }
  }

  // Reenviar link de convite
  async function resendInvite(inviteIdOrName: string): Promise<InviteActionResponse> {
    const invite = invites.value.find((i) => i.id === inviteIdOrName || i.name === inviteIdOrName);
    if (!invite) {
      return { success: false, fromBackend: false, message: 'Convite não encontrado.' };
    }

    return {
      success: true,
      fromBackend: true,
      message: `Link com o código ${invite.token} pronto para reenvio para ${invite.email}.`,
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
