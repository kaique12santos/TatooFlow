import { httpClient } from './httpClient';

export const termoApi = {
  listarTodos: async () => {
    const response = await httpClient.get('/termos');
    return response.data;
  },
  registrar: async (termo: any) => {
    const response = await httpClient.post('/termos', termo);
    return response.data;
  },
};
