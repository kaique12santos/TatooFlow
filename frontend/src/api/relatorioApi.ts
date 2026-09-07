import { httpClient } from './httpClient';

export const relatorioApi = {
  obterRelatorioMensal: async (mes: number, ano: number) => {
    const response = await httpClient.get('/relatorios/mensal', { params: { mes, ano } });
    return response.data;
  },
};
