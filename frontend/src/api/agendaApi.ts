import { httpClient } from './httpClient';
import { Agendamento } from '../types';

export const agendaApi = {
  listarTodos: async () => {
    const response = await httpClient.get<Agendamento[]>('/agendamentos');
    return response.data;
  },
  criar: async (agendamento: Partial<Agendamento>) => {
    const response = await httpClient.post<Agendamento>('/agendamentos', agendamento);
    return response.data;
  },
};
