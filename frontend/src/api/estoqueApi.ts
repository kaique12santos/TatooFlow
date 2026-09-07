import { httpClient } from './httpClient';
import { ItemEstoque } from '../types';

export const estoqueApi = {
  listarTodos: async () => {
    const response = await httpClient.get<ItemEstoque[]>('/estoque');
    return response.data;
  },
  salvar: async (item: Partial<ItemEstoque>) => {
    const response = await httpClient.post<ItemEstoque>('/estoque', item);
    return response.data;
  },
};
