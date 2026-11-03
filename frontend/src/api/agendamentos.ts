import { apiRequest, apiRequestBlob } from './http';

export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export interface ClienteAgenda {
  id: number;
  nome: string;
  telefone?: string | null;
}

export interface UsuarioAgenda {
  id: number;
  nome: string;
  perfil: string;
  ativo: boolean;
}

export interface Agendamento {
  id: number;
  clienteId: number;
  tatuadorId: number;
  dataHora: string;
  duracaoMinutos: number;
  descricaoSessao?: string | null;
  valorEstimado?: number | null;
  status: StatusAgendamento;
}

export type NovoAgendamento = Omit<Agendamento, 'id'>;

export const listarAgendamentos = () => apiRequest<Agendamento[]>('/api/agendamentos');
export const listarClientesAgenda = () => apiRequest<ClienteAgenda[]>('/api/clientes');
export const listarUsuariosAgenda = () => apiRequest<UsuarioAgenda[]>('/api/usuarios');
export const buscarComprovanteSinal = (id: number) => apiRequestBlob(`/api/agendamentos/${id}/sinal/comprovante`);

export interface PagamentoSinal {
  id: number;
  agendamentoId: number;
  valor: number;
  tipo: string;
  status: string;
  comprovanteNome?: string;
  dataPagamento?: string;
}

export const registrarSinalAgendamento = (id: number, valor: number, comprovante: File) => {
  const body = new FormData();
  body.set('valor', valor.toFixed(2));
  body.set('formaPagamento', 'PIX');
  body.set('comprovante', comprovante);
  return apiRequest<PagamentoSinal>(`/api/agendamentos/${id}/sinal`, { method: 'POST', body });
};

/**
 * Consulta a agenda filtrando por período (dia/semana/mês) e filtros opcionais [F5-BACK-01].
 */
export const consultarAgenda = (params?: { periodo?: 'dia' | 'semana' | 'mes'; data?: string; status?: string; tatuadorId?: number }) => {
  const query = new URLSearchParams();
  if (params?.periodo) query.set('periodo', params.periodo);
  if (params?.data) query.set('data', params.data);
  if (params?.status) query.set('status', params.status);
  if (params?.tatuadorId) query.set('tatuadorId', params.tatuadorId.toString());
  const qs = query.toString();
  return apiRequest<Agendamento[]>(`/api/agenda${qs ? `?${qs}` : ''}`);
};

// O endpoint existente salva um novo registro ou atualiza o registro identificado por id.
export const salvarAgendamento = (agendamento: NovoAgendamento | Agendamento) =>
  apiRequest<Agendamento>('/api/agendamentos', { method: 'POST', body: JSON.stringify(agendamento) });

export const criarClienteAgenda = (nome: string, telefone: string) =>
  apiRequest<ClienteAgenda>('/api/clientes', {
    method: 'POST', body: JSON.stringify({ nome, telefone }),
  });

export const criarTatuadorAgenda = (dados: { nome: string; email: string; pin: string; aparelhoId: string }) =>
  apiRequest<UsuarioAgenda>('/api/usuarios', {
    method: 'POST', body: JSON.stringify({ ...dados, perfil: 'TATUADOR' }),
  });
