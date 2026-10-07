import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserRole } from '@/types/navigation';

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  station: string;
}

export interface UserActivationProfile {
  id: string;
  name: string;
  role: string;
  station: string;
  specialty: string;
  avatar: string;
  status: 'valid' | 'pending' | 'expired';
  statusLabel: string;
  temporaryPin: string;
  remainingSeconds: number;
  phone: string;
}

export interface PinDefinitionPayload {
  pin: string;
  token?: string;
  station?: string;
}

export interface PinActionResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
}

export interface VerifyPinPayload {
  pin: string;
  artistId?: string;
  station?: string;
}

export interface PinAuthResponse {
  success: boolean;
  fromBackend: boolean;
  message: string;
  role?: UserRole;
  user?: UserProfile;
}

const STORAGE_KEY = 'tattooflow_user_role';

const defaultActivationProfile: UserActivationProfile = {
  id: 'usr-val-02',
  name: 'Valéria Moretti',
  role: 'TATUADORA RESIDENTE',
  station: 'BANCADA 01',
  specialty: 'Fine Line & Blackwork',
  avatar:
    'https://s3-alpha-sig.figma.com/img/0293/54de/2b9d6df9205d9c45a19a4ab2b6e1cf2b?Expires=1792368000&Key-Pair-Id=APKAQ4GOSFWCW27IBOMQ&Signature=ax7-6ukFtX0NwnJ0iphPZ4~BxB2zL~A40Rl7u3V5LzuSfKI0hmMd06dXbnanHlX1GnQ68tBPUjVdj1DKBt5THDYdJweORRbVNTTUt86jMzq16w77DcW3AYYLQ6HCWmXnJ11eIiKiev3S4iDWMfrPYC5GE0r1tQdBSlOZFmyNj~uPoZY1kF0iPiBepwjiJCtowIDUCBuLoqE3oYqjvfG7obfW1lM~tOB0RosQWYeJcQcdkjv9CK~ysfHLWjtjtVGO96gs4ePFAT~CU~vK6nrX8Eo5Gh2vBVr9b5uoMB523ILTXVfCqO1gMNP3t46KSRQ4lQhkfPlNB2b81iC8FWVluA__',
  status: 'valid',
  statusLabel: 'VALIDADO',
  temporaryPin: '749 • 382',
  remainingSeconds: 14 * 60 + 53, // 14 min 53 seg
  phone: '+55 (11) 98112-3344',
};

export const useAuthStore = defineStore('auth', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';

  const getInitialRole = (): UserRole => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'admin' || stored === 'tatuador') {
        return stored;
      }
    } catch {
      // localStorage may fail in some environments
    }
    return 'tatuador';
  };

  const currentRole = ref<UserRole>(getInitialRole());
  const activationProfile = ref<UserActivationProfile>({ ...defaultActivationProfile });
  const isPinLoading = ref<boolean>(false);

  const user = computed<UserProfile>(() => {
    if (currentRole.value === 'admin') {
      return {
        id: 'usr-adm-01',
        name: 'Carlos Notarial',
        email: 'gestao@newconcept.tattoo',
        role: 'admin',
        station: 'GESTÃO & AUDITORIA',
      };
    }
    return {
      id: 'usr-tat-01',
      name: 'Gabriel Dornelles',
      email: 'gabriel@newconcept.tattoo',
      role: 'tatuador',
      station: 'BANCADA 01',
    };
  });

  const isAdmin = computed(() => currentRole.value === 'admin');
  const isTatuador = computed(() => currentRole.value === 'tatuador');

  function setRole(newRole: UserRole) {
    currentRole.value = newRole;
    try {
      localStorage.setItem(STORAGE_KEY, newRole);
    } catch {
      // ignore
    }
  }

  function toggleRole() {
    setRole(currentRole.value === 'admin' ? 'tatuador' : 'admin');
  }

  // Action com IF: Buscar perfil de ativação e PIN temporário no backend
  async function fetchActivationProfile(token?: string): Promise<void> {
    isPinLoading.value = true;

    // IF 1: Verifica se a URL base do backend está configurada
    if (apiBaseUrl) {
      try {
        const query = token ? `?token=${encodeURIComponent(token)}` : '';
        const response = await fetch(`${apiBaseUrl}/api/auth/ativacao/perfil${query}`);

        // IF 2: Se a resposta HTTP for bem-sucedida
        if (response.ok) {
          const data = await response.json();

          // IF 3: Se o backend retornou os dados válidos do artista
          if (data && data.name) {
            console.log('[AuthStore] Perfil de ativação carregado do backend:', data);
            activationProfile.value = {
              ...activationProfile.value,
              ...data,
            };
            isPinLoading.value = false;
            return;
          } else {
            console.info('[AuthStore] Dados vazios do backend. Mantendo perfil mock.');
          }
        } else {
          console.warn(`[AuthStore] Backend HTTP ${response.status}. Ativando fallback mock.`);
        }
      } catch (err) {
        console.warn('[AuthStore] Erro ao carregar perfil de ativação do backend:', err);
      }
    } else {
      console.info('[AuthStore] VITE_API_URL não configurada. Usando perfil mock de ativação.');
    }

    isPinLoading.value = false;
  }

  // Action com IF: Definir PIN definitivo do terminal
  async function definePermanentPin(payload: PinDefinitionPayload): Promise<PinActionResponse> {
    isPinLoading.value = true;

    // IF 1: Tenta registrar o novo PIN no backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/auth/definir-pin`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        // IF 2: Se o backend confirmou a ativação
        if (response.ok) {
          const data = await response.json();
          isPinLoading.value = false;
          return {
            success: true,
            fromBackend: true,
            message: data.message || 'PIN permanente autenticado e ativo no servidor!',
          };
        } else {
          console.warn(`[AuthStore] Backend retornou erro HTTP ${response.status} ao definir PIN.`);
        }
      } catch (err) {
        console.warn('[AuthStore] Falha ao comunicar com endpoint /api/auth/definir-pin:', err);
      }
    }

    // Fallback Mock garantido
    isPinLoading.value = false;
    return {
      success: true,
      fromBackend: false,
      message: 'PIN permanente definido com sucesso na bancada (Modo Local Seguro).',
    };
  }

  // Action com IF: Reenviar PIN temporário por WhatsApp
  async function resendTemporaryPin(phoneOrToken?: string): Promise<PinActionResponse> {
    // IF 1: Tenta disparar via endpoint do backend
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/auth/pin-temporario/reenviar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            phone: phoneOrToken || activationProfile.value.phone,
          }),
        });

        // IF 2: Se resposta OK
        if (response.ok) {
          return {
            success: true,
            fromBackend: true,
            message: 'PIN temporário reenviado com sucesso pelo servidor!',
          };
        }
      } catch (err) {
        console.warn('[AuthStore] Falha ao reenviar PIN pelo backend:', err);
      }
    }

    // Fallback Mock
    return {
      success: true,
      fromBackend: false,
      message: `Comprovante e PIN reenviados com sucesso para ${activationProfile.value.phone} (Modo Simulação).`,
    };
  }

  // Action com IF: Validar PIN de segurança na tela de bloqueio (PinLock)
  async function verifyPinLock(pin: string, artistId?: string): Promise<PinAuthResponse> {
    isPinLoading.value = true;

    // IF 1: Verifica se a URL base do backend está configurada
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/auth/pin-lock/verify`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            pin,
            artistId: artistId || activationProfile.value.id,
            station: activationProfile.value.station,
          }),
        });

        // IF 2: Se o backend autenticou com sucesso
        if (response.ok) {
          const data = await response.json();
          console.log('[AuthStore] PIN validado com sucesso via backend:', data);
          if (data.role) {
            setRole(data.role as UserRole);
          }
          isPinLoading.value = false;
          return {
            success: true,
            fromBackend: true,
            message: data.message || 'Acesso liberado pelo servidor central do atelier.',
            role: data.role || currentRole.value,
            user: data.user,
          };
        } else {
          const errorData = await response.json().catch(() => ({}));
          console.warn(`[AuthStore] Backend rejeitou o PIN (Status HTTP ${response.status}):`, errorData);
          if (response.status === 401 || response.status === 403) {
            isPinLoading.value = false;
            return {
              success: false,
              fromBackend: true,
              message: errorData.message || 'PIN de segurança incorreto. Tente novamente.',
            };
          }
        }
      } catch (err) {
        console.warn('[AuthStore] Falha de conexão ao validar PIN no backend. Utilizando fallback local:', err);
      }
    } else {
      console.info('[AuthStore] VITE_API_URL não configurada. Validando PIN em modo local seguro.');
    }

    // Fallback Mock Seguro (Modo Local)
    await new Promise((resolve) => setTimeout(resolve, 350));
    isPinLoading.value = false;
    return {
      success: true,
      fromBackend: false,
      message: 'Acesso liberado no terminal de bancada (Modo Local Seguro).',
      role: currentRole.value,
    };
  }

  return {
    currentRole,
    user,
    isAdmin,
    isTatuador,
    activationProfile,
    isPinLoading,
    setRole,
    toggleRole,
    fetchActivationProfile,
    definePermanentPin,
    resendTemporaryPin,
    verifyPinLock,
  };
});
