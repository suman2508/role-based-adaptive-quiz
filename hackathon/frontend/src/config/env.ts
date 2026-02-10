export const API_BASE_URL: string =
  (import.meta as any).env?.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const APP_ENV: string = (import.meta as any).env?.MODE ?? 'development';
