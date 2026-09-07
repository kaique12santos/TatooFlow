import { httpClient } from './httpClient';

export const financeiroApi = {
  listarPagamentos: async () => {
    const response = await httpClient.get('/financeiro/pagamentos');
    return response.data;
  },
  registrarPagamento: async (pagamento: any) => {
    const response = await httpClient.post('/financeiro/pagamentos', pagamento);
    return response.data;
  },
};
