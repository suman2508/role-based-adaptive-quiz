import axios from 'axios';
import { API_BASE_URL } from '@config/env';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
});

// Example request/response interceptors (extend for auth/JWT when added)
api.interceptors.request.use((config) => {
  // const token = getAuthToken(); // wire JWT later
  // if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    // Centralized error logging/handling
    // eslint-disable-next-line no-console
    console.error('API error:', err?.response?.status, err?.response?.data || err?.message);
    return Promise.reject(err);
  }
);
