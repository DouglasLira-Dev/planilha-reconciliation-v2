import axios from 'axios';

export const api = axios.create({
  baseURL: '/api', // ou 'http://localhost:8080/api'
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para refresh token (opcional)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Implementar refresh token se necessário
    return Promise.reject(error);
  }
);