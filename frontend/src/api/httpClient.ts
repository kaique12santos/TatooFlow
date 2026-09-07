import axios from 'axios';

export const httpClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

httpClient.interceptors.request.use(
  async (config) => {
    // Interceptor para injetar o JWT token nos cabeçalhos HTTP
    return config;
  },
  (error) => Promise.reject(error)
);
