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

interface BackendUser {
  id: number;
  nome: string;
  email: string;
  perfil: string;
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
const TOKEN_STORAGE_KEY = 'tattooflow_auth_token';
const DEVICE_STORAGE_KEY = 'tattooflow_device_id';

const emptyActivationProfile: UserActivationProfile = {
  id: '',
  name: '',
  role: '',
  station: '',
  specialty: '',
  avatar: '',
  status: 'pending',
  statusLabel: '',
  temporaryPin: '',
  remainingSeconds: 0,
  phone: '',
};

export const useAuthStore = defineStore('auth', () => {
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || (import.meta.env.VITE_API_URL as string) || '';
  const getStoredValue = (key: string) => {
    try {
      return localStorage.getItem(key) || '';
    } catch {
      return '';
    }
  };
  const getDeviceId = () => {
    const configured = (import.meta.env.VITE_DEVICE_ID as string) || '';
    if (configured) return configured;
    const stored = getStoredValue(DEVICE_STORAGE_KEY);
    if (stored) return stored;
    const generated = `tattooflow-${crypto.randomUUID()}`;
    try { localStorage.setItem(DEVICE_STORAGE_KEY, generated); } catch { /* ignore */ }
    return generated;
  };

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
  const token = ref(getStoredValue(TOKEN_STORAGE_KEY));
  const deviceId = ref(getDeviceId());
  const profile = ref<UserProfile | null>(null);
  const isLoading = ref(false);
  const activationProfile = ref<UserActivationProfile>({ ...emptyActivationProfile });
  const isPinLoading = ref<boolean>(false);

  const user = computed<UserProfile>(() => profile.value || {
    id: '', name: 'Usuário TattooFlow', email: '', role: currentRole.value,
    station: currentRole.value === 'admin' ? 'GESTÃO & AUDITORIA' : 'BANCADA 01',
  });
  const isAuthenticated = computed(() => Boolean(token.value && profile.value));

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

  function mapBackendUser(backendUser: BackendUser): UserProfile {
    const role: UserRole = backendUser.perfil === 'ADMIN' ? 'admin' : 'tatuador';
    return { id: String(backendUser.id), name: backendUser.nome, email: backendUser.email, role, station: role === 'admin' ? 'GESTÃO & AUDITORIA' : 'BANCADA 01' };
  }

  async function login(pin: string, aparelhoId: string): Promise<{ success: boolean; message: string }> {
    isLoading.value = true;
    try {
      if (!apiBaseUrl) return { success: false, message: 'Configure a URL da API em VITE_API_BASE_URL.' };
      const response = await fetch(`${apiBaseUrl}/api/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ pin, aparelhoId }) });
      const result = await response.json().catch(() => ({}));
      if (!response.ok || !result.data) return { success: false, message: result.message || 'Não foi possível entrar.' };
      const nextToken = result.data as string;
      const profileResponse = await fetch(`${apiBaseUrl}/api/auth/me`, { headers: { Authorization: `Bearer ${nextToken}` } });
      const profileResult = await profileResponse.json().catch(() => ({}));
      if (!profileResponse.ok || !profileResult.data) return { success: false, message: profileResult.message || 'Não foi possível carregar seu perfil.' };
      token.value = nextToken;
      deviceId.value = aparelhoId;
      profile.value = mapBackendUser(profileResult.data as BackendUser);
      setRole(profile.value.role);
      try { localStorage.setItem(TOKEN_STORAGE_KEY, nextToken); localStorage.setItem(DEVICE_STORAGE_KEY, aparelhoId); } catch { /* ignore */ }
      return { success: true, message: result.message || 'Login realizado com sucesso.' };
    } catch {
      return { success: false, message: 'Não foi possível conectar ao backend.' };
    } finally {
      isLoading.value = false;
    }
  }

  async function restoreSession(): Promise<boolean> {
    if (!token.value || !apiBaseUrl) return false;
    try {
      const response = await fetch(`${apiBaseUrl}/api/auth/me`, { headers: { Authorization: `Bearer ${token.value}` } });
      const result = await response.json().catch(() => ({}));
      if (!response.ok || !result.data) { logout(); return false; }
      profile.value = mapBackendUser(result.data as BackendUser);
      setRole(profile.value.role);
      return true;
    } catch { return false; }
  }

  function logout() {
    token.value = '';
    profile.value = null;
    try { localStorage.removeItem(TOKEN_STORAGE_KEY); localStorage.removeItem(STORAGE_KEY); } catch { /* ignore */ }
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

  // Action: Definir PIN definitivo do terminal
  async function definePermanentPin(payload: PinDefinitionPayload): Promise<PinActionResponse> {
    isPinLoading.value = true;

    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/auth/definir-pin`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        if (response.ok) {
          const data = await response.json();
          isPinLoading.value = false;
          return {
            success: true,
            fromBackend: true,
            message: data.message || 'PIN permanente registrado com sucesso no servidor!',
          };
        } else {
          console.warn(`[AuthStore] Backend retornou erro HTTP ${response.status} ao definir PIN.`);
        }
      } catch (err) {
        console.warn('[AuthStore] Falha ao comunicar com endpoint /api/auth/definir-pin:', err);
      }
    }

    isPinLoading.value = false;
    return {
      success: false,
      fromBackend: false,
      message: 'Não foi possível registrar o PIN no servidor.',
    };
  }

  // Action: Reenviar PIN temporário por WhatsApp
  async function resendTemporaryPin(phoneOrToken?: string): Promise<PinActionResponse> {
    if (apiBaseUrl) {
      try {
        const response = await fetch(`${apiBaseUrl}/api/auth/pin-temporario/reenviar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            phone: phoneOrToken || activationProfile.value.phone,
          }),
        });

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

    return {
      success: false,
      fromBackend: false,
      message: 'Não foi possível reenviar o PIN pelo servidor.',
    };
  }

  // Action: Validar PIN de segurança na tela de bloqueio (PinLock) com backend real
  async function verifyPinLock(pin: string, _artistId?: string): Promise<PinAuthResponse> {
    isPinLoading.value = true;
    const loginResult = await login(pin, deviceId.value);
    isPinLoading.value = false;

    if (loginResult.success) {
      return {
        success: true,
        fromBackend: true,
        message: loginResult.message,
        role: currentRole.value,
        user: user.value,
      };
    }

    return {
      success: false,
      fromBackend: true,
      message: loginResult.message || 'PIN incorreto ou aparelho não autorizado no servidor.',
    };
  }

  return {
    currentRole,
    user,
    token,
    deviceId,
    isAuthenticated,
    isLoading,
    isAdmin,
    isTatuador,
    activationProfile,
    isPinLoading,
    setRole,
    toggleRole,
    login,
    restoreSession,
    logout,
    fetchActivationProfile,
    definePermanentPin,
    resendTemporaryPin,
    verifyPinLock,
  };
});
