import { httpClient } from './httpClient';

export const midiaApi = {
  listarPorAgendamento: async (agendamentoId: number) => {
    const response = await httpClient.get(`/midias/agendamento/${agendamentoId}`);
    return response.data;
  },
  upload: async (data: any) => {
    const response = await httpClient.post('/midias', data);
    return response.data;
  },
};
