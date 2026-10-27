import { apiRequest } from './http';

export type StatusAgendamento = 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

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
