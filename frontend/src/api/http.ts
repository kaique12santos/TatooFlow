import { useAuthStore } from '@/stores/auth';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

async function authenticatedRequest(path: string, options: RequestInit = {}): Promise<Response> {
  const baseUrl = ((import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '') as string)
    .replace(/\/$/, '');
  if (!baseUrl) throw new Error('A conexão com a API não está configurada.');

  const headers = new Headers(options.headers);
  headers.set('Authorization', `Bearer ${useAuthStore().token}`);
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');

  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, { ...options, headers });
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Tente novamente.');
  }
  if (!response.ok) {
    const result = await response.json().catch(() => null) as ApiResponse<unknown> | null;
    const fallback = response.status === 401 || response.status === 403
      ? 'Sua sessão expirou ou você não tem permissão. Entre novamente.'
      : 'Não foi possível salvar ou carregar os dados. Tente novamente.';
    throw new Error(result?.message || fallback);
  }
  return response;
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await authenticatedRequest(path, options);
  const result = await response.json().catch(() => null) as ApiResponse<T> | null;
  if (!result?.success) throw new Error(result?.message || 'Não foi possível salvar ou carregar os dados. Tente novamente.');
  return result.data;
}

export async function apiRequestBlob(path: string): Promise<Blob> {
  const response = await authenticatedRequest(path);
  return response.blob();
}
