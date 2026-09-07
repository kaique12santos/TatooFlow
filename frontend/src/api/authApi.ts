import { httpClient } from './httpClient';

export const authApi = {
  login: async (email: string, senha: string) => {
    const response = await httpClient.post('/auth/login', null, { params: { email, senha } });
    return response.data;
  },
};
