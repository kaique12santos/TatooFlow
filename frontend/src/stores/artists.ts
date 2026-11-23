import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apiRequest } from '@/api/http';
import { getMockAvatar } from '@/components/AnexarImagem.vue';

export interface ArtistUser {
  id: string;
  name: string;
  station: string;
  specialty: string;
  pinLabel: string;
  sinceLabel: string;
  statusBadge: string;
  status: 'ativo' | 'revogado';
  avatar: string;
  role?: string;
  email?: string;
}

export interface ArtistStats {
  ativos: number;
  inativos: number;
  total: number;
}

export interface ArtistActionResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
  artist?: ArtistUser;
}

interface BackendUsuario {
  id: number;
  nome: string;
  email: string;
  perfil: string;
  ativo?: boolean;
  aparelhoId?: string;
}

export const useArtistsStore = defineStore('artists', () => {
  const artists = ref<ArtistUser[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Estatísticas computadas dinamicamente com base nos dados do banco
  const stats = computed<ArtistStats>(() => {
    const ativos = artists.value.filter(a => a.status === 'ativo').length;
    const inativos = artists.value.filter(a => a.status === 'revogado').length;
    return {
      ativos,
      inativos,
      total: artists.value.length,
    };
  });

  // Carrega corpo de artistas diretamente da tabela de usuários do banco
  async function fetchArtists(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    try {
      const data = await apiRequest<BackendUsuario[]>('/api/usuarios');
      if (Array.isArray(data)) {
        artists.value = data.map((u: BackendUsuario) => {
          const isAtivo = u.ativo !== false;
          let specialty = 'Tatuador Residente';
          if (u.perfil === 'ADMIN') {
            specialty = 'Gestão & Auditoria';
          } else if (u.id === 1) {
            specialty = 'Master Chiaroscuro';
          } else if (u.id === 2) {
            specialty = 'Fine Line & Botânica';
          } else if (u.id === 3) {
            specialty = 'Blackwork Gravura';
          }

          const avatar = getMockAvatar(u.id || u.nome);

          return {
            id: String(u.id),
            name: u.nome,
            station: isAtivo ? (u.perfil === 'ADMIN' ? 'Gestão & Auditoria' : `Bancada 0${u.id}`) : 'Sem Bancada Vinculada',
            specialty,
            pinLabel: isAtivo ? `PIN Ativo` : 'PIN Bloqueado',
            sinceLabel: isAtivo ? 'Ativo' : 'Desligado',
            statusBadge: isAtivo ? 'ATIVO • TOTAL' : 'REVOGADO',
            status: isAtivo ? 'ativo' : 'revogado',
            avatar,
            role: u.perfil === 'ADMIN' ? 'Administrador Geral' : 'Tatuador Residente',
            email: u.email,
          };
        });
      } else {
        artists.value = [];
      }
    } catch (err: any) {
      artists.value = [];
      error.value = err?.message || 'Erro ao carregar artistas do banco.';
      console.warn('[ArtistsStore] Erro ao sincronizar artistas do banco:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // Revogar credenciais e acesso do artista
  async function revokeAccess(artistId: string): Promise<ArtistActionResponse> {
    const artist = artists.value.find(a => a.id === artistId);
    if (!artist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    try {
      await apiRequest<any>(`/api/usuarios/${artistId}/revogar`, {
        method: 'PATCH',
        body: JSON.stringify({ status: 'revogado' }),
      });
      artist.status = 'revogado';
      artist.statusBadge = 'REVOGADO';
      artist.pinLabel = 'PIN Bloqueado';
      artist.sinceLabel = 'Desligado';
      return {
        success: true,
        fromBackend: true,
        artist,
        message: `Acesso de ${artist.name} revogado com sucesso.`,
      };
    } catch (err: any) {
      // Se endpoint não implementado no backend, reflete erro real
      return {
        success: false,
        fromBackend: false,
        artist,
        message: err?.message || `Não foi possível revogar acesso no servidor.`,
      };
    }
  }

  // Reativar perfil de artista
  async function reactivateProfile(artistId: string): Promise<ArtistActionResponse> {
    const artist = artists.value.find(a => a.id === artistId);
    if (!artist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    try {
      await apiRequest<any>(`/api/usuarios/${artistId}/reativar`, {
        method: 'PATCH',
        body: JSON.stringify({ status: 'ativo' }),
      });
      artist.status = 'ativo';
      artist.statusBadge = 'ATIVO • TOTAL';
      artist.pinLabel = 'PIN Ativo';
      artist.sinceLabel = 'Ativo';
      return {
        success: true,
        fromBackend: true,
        artist,
        message: `Perfil de ${artist.name} reativado com sucesso.`,
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        artist,
        message: err?.message || 'Não foi possível reativar o perfil no servidor.',
      };
    }
  }

  // Excluir definitivamente artista
  async function deletePermanently(artistId: string): Promise<ArtistActionResponse> {
    const index = artists.value.findIndex(a => a.id === artistId);
    if (index === -1) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }
    const targetArtist = artists.value[index]!;
    const removedName = targetArtist.name;

    try {
      await apiRequest<any>(`/api/usuarios/${artistId}`, { method: 'DELETE' });
      artists.value.splice(index, 1);
      return {
        success: true,
        fromBackend: true,
        message: `Registro de ${removedName} removido com sucesso.`,
      };
    } catch (err: any) {
      return {
        success: false,
        fromBackend: false,
        message: err?.message || `Não foi possível excluir no servidor.`,
      };
    }
  }

  return {
    artists,
    isLoading,
    error,
    stats,
    fetchArtists,
    revokeAccess,
    reactivateProfile,
    deletePermanently,
  };
});
