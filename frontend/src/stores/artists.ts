import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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

const defaultArtists: ArtistUser[] = [
  {
    id: 'gabriel',
    name: 'Gabriel Dornelles',
    station: 'Bancada 01',
    specialty: 'Master Chiaroscuro',
    pinLabel: 'PIN #9821',
    sinceLabel: 'Desde 2021',
    statusBadge: 'ATIVO • TOTAL',
    status: 'ativo',
    avatar:
      'https://s3-alpha-sig.figma.com/img/5d0c/dcc0/9526114594d0aed5412f2474cb8e9edd?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=P1mpY0e-3jYcKZZ4V6lJa-wZX15noDjlie3AaNFYXRFtKCc2JtVtcHKTVPPOKZwlKgjkYuYoSskAd3EeVIl1XhQ~4sdzBkuizJhyHXE~nZd24V6mlagidhhwr0cD5wFHqfJCR9YjP5-LiuIczdRpZaKKHT3Fy8W5OwIyLnEIOSyAvXmD8dJOtSilrMNZKm1gchqHZyy8ymyeSU~ufKlI8L5vrIViaV-RPTBLi18xmOTZc1s~UsqtHIjSOcq-dcYtq2bGGF8HHQbKTt1fo-WM-CrvuCbhBoZk9TlFu4SGOn9SIDoK~QlnKyJzCIFBhL-6kuS61CuNvHxZytSNceFVjw__',
    role: 'Tatuador Residente',
    email: 'gabriel@newconcept.art',
  },
  {
    id: 'valeria',
    name: 'Valéria Moretti',
    station: 'Bancada 02',
    specialty: 'Fine Line & Botânica',
    pinLabel: 'PIN #4119',
    sinceLabel: 'Desde 2022',
    statusBadge: 'ATIVO • TOTAL',
    status: 'ativo',
    avatar:
      'https://s3-alpha-sig.figma.com/img/0293/54de/2b9d6df9205d9c45a19a4ab2b6e1cf2b?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=ax7-6ukFtX0NwnJ0iphPZ4~BxB2zL~A40Rl7u3V5LzuSfKI0hmMd06dXbnanHlX1GnQ68tBPUjVdj1DKBt5THDYdJweORRbVNTTUt86jMzq16w77DcW3AYYLQ6HCWmXnJ11eIiKiev3S4iDWMfrPYC5GE0r1tQdBSlOZFmyNj~uPoZY1kF0iPiBepwjiJCtowIDUCBuLoqE3oYqjvfG7obfW1lM~tOB0RosQWYeJcQcdkjv9CK~ysfHLWjtjtVGO96gs4ePFAT~CU~vK6nrX8Eo5Gh2vBVr9b5uoMB523ILTXVfCqO1gMNP3t46KSRQ4lQhkfPlNB2b81iC8FWVluA__',
    role: 'Tatuadora Residente',
    email: 'valeria@newconcept.art',
  },
  {
    id: 'rodrigo',
    name: 'Rodrigo Faust',
    station: 'Sem Bancada Vinculada',
    specialty: 'Old School & Tradicional',
    pinLabel: 'PIN Bloqueado',
    sinceLabel: 'Desligado em 14/Out',
    statusBadge: 'REVOGADO',
    status: 'revogado',
    avatar:
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80',
    role: 'Ex-Artista Convidado',
    email: 'rodrigo.faust@external.art',
  },
];

export const useArtistsStore = defineStore('artists', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const artists = ref<ArtistUser[]>([...defaultArtists]);
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

  // Action com IF: Carrega corpo de artistas do backend ou ativa fallback mock
  async function fetchArtists(): Promise<void> {
    isLoading.value = true;
    error.value = null;

    // IF 1: Verifica se a URL base do backend está configurada
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/artistas`);

        // IF 2: Se a resposta HTTP for bem-sucedida
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou uma lista não vazia
          if (Array.isArray(data) && data.length > 0) {
            console.log('[ArtistsStore] Artistas carregados do backend:', data);
            artists.value = data;
            isLoading.value = false;
            return;
          } else {
            console.info('[ArtistsStore] Backend retornou lista vazia. Mantendo mock do atelier.');
          }
        } else {
          console.warn(`[ArtistsStore] Backend retornou HTTP ${response.status}. Usando fallback mock.`);
        }
      } catch (err) {
        console.warn('[ArtistsStore] Falha ao comunicar com backend de artistas:', err);
      }
    } else {
      console.info('[ArtistsStore] VITE_API_URL não configurada. Utilizando corpo de artistas mockado.');
    }

    isLoading.value = false;
  }

  // Action com IF: Revogar credenciais e acesso do artista
  async function revokeAccess(artistId: string): Promise<ArtistActionResponse> {
    const artist = artists.value.find(a => a.id === artistId);
    if (!artist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    // IF 1: Tenta revogação via endpoint do backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/artistas/${artistId}/revogar`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: 'revogado' }),
        });

        // IF 2: Se o backend confirmou a revogação
        if (response.ok) {
          const data = await response.json();
          artist.status = 'revogado';
          artist.statusBadge = 'REVOGADO';
          artist.pinLabel = 'PIN Bloqueado';
          artist.sinceLabel = 'Desligado Hoje';

          return {
            success: true,
            fromBackend: true,
            artist,
            message: data.message || `Acesso de ${artist.name} revogado com sucesso no servidor.`,
          };
        } else {
          console.warn(`[ArtistsStore] Falha HTTP ${response.status} ao revogar no backend.`);
        }
      } catch (err) {
        console.warn('[ArtistsStore] Falha ao conectar endpoint de revogação. Aplicando fallback mock:', err);
      }
    }

    // Fallback Mock garantido
    artist.status = 'revogado';
    artist.statusBadge = 'REVOGADO';
    artist.pinLabel = 'PIN Bloqueado';
    artist.sinceLabel = 'Desligado Hoje';

    return {
      success: true,
      fromBackend: false,
      artist,
      message: `Credenciais de ${artist.name} revogadas localmente (Modo Contingência).`,
    };
  }

  // Action com IF: Reativar perfil de artista
  async function reactivateProfile(artistId: string): Promise<ArtistActionResponse> {
    const artist = artists.value.find(a => a.id === artistId);
    if (!artist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }

    // IF 1: Tenta reativar via backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/artistas/${artistId}/reativar`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: 'ativo' }),
        });

        // IF 2: Se o backend confirmou a reativação
        if (response.ok) {
          const data = await response.json();
          artist.status = 'ativo';
          artist.statusBadge = 'ATIVO • TOTAL';
          artist.pinLabel = data.pinLabel || 'PIN #8820';
          artist.sinceLabel = 'Reativado Hoje';

          return {
            success: true,
            fromBackend: true,
            artist,
            message: data.message || `Perfil de ${artist.name} reativado com sucesso no servidor.`,
          };
        }
      } catch (err) {
        console.warn('[ArtistsStore] Falha ao comunicar com endpoint de reativação:', err);
      }
    }

    // Fallback Mock garantido
    artist.status = 'ativo';
    artist.statusBadge = 'ATIVO • TOTAL';
    artist.pinLabel = 'PIN #8820';
    artist.sinceLabel = 'Reativado Hoje';

    return {
      success: true,
      fromBackend: false,
      artist,
      message: `Perfil de ${artist.name} reativado com sucesso (Modo Mock).`,
    };
  }

  // Action com IF: Excluir definitivamente artista do acervo
  async function deletePermanently(artistId: string): Promise<ArtistActionResponse> {
    const index = artists.value.findIndex(a => a.id === artistId);
    if (index === -1) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }
    const targetArtist = artists.value[index];
    if (!targetArtist) {
      return { success: false, fromBackend: false, message: 'Artista não encontrado.' };
    }
    const removedName = targetArtist.name;

    // IF 1: Tenta deletar no backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/equipe/artistas/${artistId}`, {
          method: 'DELETE',
        });

        // IF 2: Se o backend confirmou a deleção
        if (response.ok) {
          artists.value.splice(index, 1);
          return {
            success: true,
            fromBackend: true,
            message: `Registro de ${removedName} removido definitivamente do servidor.`,
          };
        }
      } catch (err) {
        console.warn('[ArtistsStore] Falha ao comunicar com endpoint de exclusão:', err);
      }
    }

    // Fallback Mock garantido
    artists.value.splice(index, 1);
    return {
      success: true,
      fromBackend: false,
      message: `Registro de ${removedName} removido da base local.`,
    };
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
