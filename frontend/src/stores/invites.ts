import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from './auth';
export interface InviteItem { id: number; emailConvidado: string; codigo: string; utilizado: boolean; dataExpiracao: string }
export const useInvitesStore = defineStore('invites', () => {
  const auth = useAuthStore();
  const base = (import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '').replace(/\/$/, '');
  const invites = ref<InviteItem[]>([]);
  const isLoading = ref(false);
  const error = ref('');
  async function request(method: string, email?: string) {
    const response = await fetch(`${base}/api/convites`, {
      method, headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${auth.token}` },
      body: email ? JSON.stringify({ emailConvidado: email }) : undefined,
    });
    const result = await response.json().catch(() => ({}));
    if (!response.ok || !result.success) throw new Error(result.message || 'N?o foi poss?vel acessar os convites.');
    return result.data;
  }
  async function fetchInvites() {
    isLoading.value = true; error.value = '';
    try { invites.value = await request('GET'); }
    catch (err) { error.value = err instanceof Error ? err.message : 'Falha de conex?o.'; }
    finally { isLoading.value = false; }
  }
  async function createInvite(email: string): Promise<InviteItem | null> {
    isLoading.value = true; error.value = '';
    try { const invite: InviteItem = await request('POST', email); invites.value.unshift(invite); return invite; }
    catch (err) { error.value = err instanceof Error ? err.message : 'Falha de conex?o.'; return null; }
    finally { isLoading.value = false; }
  }
  return { invites, isLoading, error, fetchInvites, createInvite };
});
