export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'TATUADOR';
  ativo: boolean;
}

export interface Cliente {
  id: number;
  nome: string;
  telefone?: string;
  email?: string;
  instagram?: string;
  historicoSaude?: string;
}

export interface Agendamento {
  id: number;
  clienteId: number;
  tatuadorId: number;
  dataHora: string;
  descricaoSessao?: string;
  valorEstimado?: number;
  status: 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';
}

export interface ItemEstoque {
  id: number;
  nome: string;
  categoria?: string;
  quantidade: number;
  quantidadeMinima: number;
  unidadeMedida?: string;
}
