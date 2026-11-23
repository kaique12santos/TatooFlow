import { apiRequest } from './http';

export interface NotificacaoItem {
  id: string;
  titulo: string;
  mensagem: string;
  tipo: 'estoque' | 'sinal' | 'anamnese' | 'agendamento' | 'sistema';
  criadoEm: string;
  tempoAtras: string;
  lida: boolean;
  link?: string;
  meta?: Record<string, any>;
}

export const NOTIFICACOES_MOCK_INICIAIS: NotificacaoItem[] = [
  {
    id: 'notif-1',
    titulo: 'Alerta de Estoque Mínimo',
    mensagem: 'Agulha 05 RL descartável atingiu a quantidade mínima crítica (3 unidades). Recomenda-se reposição imediata.',
    tipo: 'estoque',
    criadoEm: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    tempoAtras: '12 min atrás',
    lida: false,
    link: '/estoque',
  },
  {
    id: 'notif-2',
    titulo: 'Sinal Registrado (50%)',
    mensagem: 'Comprovante PIX de R$ 600,00 vinculado à sessão de Camila Albuquerque. Maca liberada para procedimento.',
    tipo: 'sinal',
    criadoEm: new Date(Date.now() - 1000 * 60 * 35).toISOString(),
    tempoAtras: '35 min atrás',
    lida: false,
    link: '/agenda',
  },
  {
    id: 'notif-3',
    titulo: 'Anamnese Digital Recebida',
    mensagem: 'Rodrigo Santos respondeu aos questionamentos de saúde via WhatsApp. Ficha pré-preenchida no sistema.',
    tipo: 'anamnese',
    criadoEm: new Date(Date.now() - 1000 * 60 * 75).toISOString(),
    tempoAtras: '1h atrás',
    lida: false,
    link: '/termo',
  },
  {
    id: 'notif-4',
    titulo: 'Novo Agendamento Confirmado',
    mensagem: 'Sessão Fine Line reservada para amanhã às 14:00 na Bancada 01 com o tatuador responsável.',
    tipo: 'agendamento',
    criadoEm: new Date(Date.now() - 1000 * 60 * 180).toISOString(),
    tempoAtras: '3h atrás',
    lida: true,
    link: '/agenda',
  },
  {
    id: 'notif-5',
    titulo: 'Cuidados Pós-Sessão Enviados',
    mensagem: 'Disparo automático de orientações D+15 enviado com sucesso para acompanhamento da cicatrização.',
    tipo: 'sistema',
    criadoEm: new Date(Date.now() - 1000 * 60 * 360).toISOString(),
    tempoAtras: '6h atrás',
    lida: true,
    link: '/clientes',
  },
];

/**
 * Tenta buscar notificações do backend em /api/notificacoes.
 * Se o endpoint ainda não estiver implementado no backend, usa a lista mockada com eventos reais do estúdio.
 */
export async function listarNotificacoes(): Promise<NotificacaoItem[]> {
  try {
    const data = await apiRequest<NotificacaoItem[]>('/api/notificacoes');
    if (Array.isArray(data) && data.length > 0) {
      return data;
    }
    return NOTIFICACOES_MOCK_INICIAIS;
  } catch {
    return NOTIFICACOES_MOCK_INICIAIS;
  }
}

/**
 * Marca uma notificação como lida no backend ou retorna sucesso local
 */
export async function marcarNotificacaoLidaApi(id: string): Promise<boolean> {
  try {
    await apiRequest(`/api/notificacoes/${id}/lida`, { method: 'PATCH' });
    return true;
  } catch {
    return true;
  }
}

/**
 * Marca todas as notificações como lidas no backend ou retorna sucesso local
 */
export async function marcarTodasNotificacoesLidasApi(): Promise<boolean> {
  try {
    await apiRequest('/api/notificacoes/ler-todas', { method: 'POST' });
    return true;
  } catch {
    return true;
  }
}
