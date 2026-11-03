import { useAuthStore } from '@/stores/auth';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const baseUrl = ((import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '') as string)
    .replace(/\/$/, '');
  if (!baseUrl) throw new Error('A conexão com a API não está configurada.');

  const headers = new Headers(options.headers);
  headers.set('Authorization', `Bearer ${useAuthStore().token}`);
  if (options.body) headers.set('Content-Type', 'application/json');

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, { ...options, headers });
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Tente novamente.');
  }
  const result = await response.json().catch(() => null) as ApiResponse<T> | null;
  if (!response.ok || !result?.success) {
    const fallback = response.status === 401 || response.status === 403
      ? 'Sua sessão expirou ou você não tem permissão. Entre novamente.'
      : 'Não foi possível salvar ou carregar os dados. Tente novamente.';
    throw new Error(result?.message || fallback);
  }
  return result.data;
}
