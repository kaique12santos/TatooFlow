import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { apiRequest } from '@/api/http';

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

export const useArtistsStore = defineStore('artists', () => {
  const artists = ref<ArtistUser[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // Estatísticas computadas dinamicamente
  const stats = computed<ArtistStats>(() => {
    const ativos = artists.value.filter(a => a.status === 'ativo').length;
    const inativos = artists.value.filter(a => a.status === 'revogado').length;
    return {
      ativos,
      inativos,
      total: artists.value.length,
    };
  });

  async function fetchArtists(): Promise<void> {
    isLoading.value = true;
    error.value = null;
    try {
      artists.value = await apiRequest<ArtistUser[]>('/api/equipe/artistas');
    } catch (err) {
      artists.value = [];
      error.value = err instanceof Error ? err.message : 'Não foi possível carregar a equipe.';
      console.error('[ArtistsStore] Falha ao carregar artistas do backend:', err);
    }
    isLoading.value = false;
  }

  async function revokeAccess(artistId: string): Promise<ArtistActionResponse> {
    if (!artists.value.some(artist => artist.id === artistId)) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    try {
      const updatedArtist = await apiRequest<ArtistUser>(`/api/equipe/artistas/${artistId}/revogar`, {
        method: 'PATCH',
      });
      const index = artists.value.findIndex(artist => artist.id === artistId);
      if (index >= 0) artists.value[index] = updatedArtist;
      return {
        success: true,
        fromBackend: true,
        artist: updatedArtist,
        message: 'Acesso do artista revogado com sucesso no servidor.',
      };
    } catch (err) {
      return {
        success: false,
        fromBackend: true,
        message: err instanceof Error ? err.message : 'Não foi possível revogar o acesso.',
      };
    }
  }

  async function reactivateProfile(artistId: string): Promise<ArtistActionResponse> {
    if (!artists.value.some(artist => artist.id === artistId)) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    try {
      const updatedArtist = await apiRequest<ArtistUser>(`/api/equipe/artistas/${artistId}/reativar`, {
        method: 'PATCH',
      });
      const index = artists.value.findIndex(artist => artist.id === artistId);
      if (index >= 0) artists.value[index] = updatedArtist;
      return {
        success: true,
        fromBackend: true,
        artist: updatedArtist,
        message: 'Perfil do artista reativado com sucesso no servidor.',
      };
    } catch (err) {
      return {
        success: false,
        fromBackend: true,
        message: err instanceof Error ? err.message : 'Não foi possível reativar o artista.',
      };
    }
  }

  async function deletePermanently(artistId: string): Promise<ArtistActionResponse> {
    const index = artists.value.findIndex(a => a.id === artistId);
    if (index === -1) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }
    const targetArtist = artists.value[index];
    if (!targetArtist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }
    try {
      await apiRequest<void>(`/api/equipe/artistas/${artistId}`, { method: 'DELETE' });
      artists.value.splice(index, 1);
      return {
        success: true,
        fromBackend: true,
        message: `Registro de ${targetArtist.name} removido definitivamente do servidor.`,
      };
    } catch (err) {
      return {
        success: false,
        fromBackend: true,
        message: err instanceof Error ? err.message : 'Não foi possível remover o artista.',
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
