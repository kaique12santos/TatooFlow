import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useAuthStore } from './auth';

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
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const auth = useAuthStore();
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

  interface UsuarioResponse { id: number; nome: string; email: string; perfil: string; ativo: boolean }

  async function fetchArtists(): Promise<void> {
    isLoading.value = true;
    error.value = null;
    artists.value = [];
    try {
      const response = await fetch(`${apiBaseUrl.replace(/\/$/, '')}/api/usuarios`, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      const result = await response.json().catch(() => ({}));
      if (!response.ok || !result.success || !Array.isArray(result.data)) {
        throw new Error(result.message || 'N?o foi poss?vel carregar a equipe.');
      }
      artists.value = (result.data as UsuarioResponse[])
        .filter(usuario => usuario.perfil === 'TATUADOR')
        .map(usuario => ({
          id: String(usuario.id), name: usuario.nome, email: usuario.email,
          role: 'Tatuador', station: 'Sem bancada vinculada', specialty: '',
          pinLabel: usuario.ativo ? 'PIN cadastrado' : 'Acesso inativo', sinceLabel: '',
          statusBadge: usuario.ativo ? 'ATIVO' : 'INATIVO',
          status: usuario.ativo ? 'ativo' : 'revogado', avatar: '',
        }));
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Falha de conex?o com o servidor.';
    } finally { isLoading.value = false; }
  }

  // These operations have no corresponding backend endpoints yet.
  async function unavailable(_artistId: string): Promise<ArtistActionResponse> {
    const message = 'Esta a??o ainda n?o est? dispon?vel no servidor.';
    error.value = message;
    return { success: false, fromBackend: false, message };
  }
  const revokeAccess = unavailable;
  const reactivateProfile = unavailable;
  const deletePermanently = unavailable;

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
