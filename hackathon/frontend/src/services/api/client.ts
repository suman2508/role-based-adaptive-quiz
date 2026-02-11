import axios from 'axios';
import { API_BASE_URL } from '@config/env';
import { getAccessToken, getRefreshToken, setAuthTokens, clearAuth } from '@app/store/auth';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
});

// Example request/response interceptors (extend for auth/JWT when added)
api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    if (config.headers && typeof (config.headers as any).set === 'function') {
      (config.headers as any).set('Authorization', `Bearer ${token}`);
    } else {
      config.headers = {
        ...(config.headers as any),
        Authorization: `Bearer ${token}`
      } as any;
    }
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const original = err?.config || {};
    const status = err?.response?.status;

    if (status === 401 && !original._retry) {
      original._retry = true;

      try {
        const refreshToken = getRefreshToken();
        if (!refreshToken) throw new Error('Missing refresh token');

        const { data } = await api.post('/auth/refresh', { refreshToken });
        if (data?.accessToken) {
          setAuthTokens({
            accessToken: data.accessToken,
            refreshToken: data.refreshToken ?? refreshToken
          });
          if (original.headers && typeof (original.headers as any).set === 'function') {
            (original.headers as any).set('Authorization', `Bearer ${data.accessToken}`);
          } else {
            original.headers = {
              ...(original.headers as any),
              Authorization: `Bearer ${data.accessToken}`
            };
          }
          return api.request(original);
        }
      } catch (refreshErr) {
        clearAuth();
      }
    }

    // eslint-disable-next-line no-console
    console.error('API error:', status, err?.response?.data || err?.message);
    return Promise.reject(err);
  }
);
