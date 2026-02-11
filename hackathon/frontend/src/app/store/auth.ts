import create from 'zustand';

type AuthState = {
  accessToken?: string | null;
  refreshToken?: string | null;
  userEmail?: string | null;
  isAuthenticated: boolean;

  setTokens: (params: { accessToken: string; refreshToken?: string | null; userEmail?: string | null }) => void;
  clear: () => void;
};

function loadFromStorage(): Partial<AuthState> {
  try {
    const raw = localStorage.getItem('auth');
    if (!raw) return {};
    const parsed = JSON.parse(raw);
    return {
      accessToken: parsed.accessToken ?? null,
      refreshToken: parsed.refreshToken ?? null,
      userEmail: parsed.userEmail ?? null,
      isAuthenticated: !!parsed.accessToken
    };
  } catch {
    return {};
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: undefined,
  refreshToken: undefined,
  userEmail: undefined,
  isAuthenticated: false,
  ...loadFromStorage(),

  setTokens: ({ accessToken, refreshToken = null, userEmail = null }) => {
    const next = {
      accessToken,
      refreshToken,
      userEmail,
      isAuthenticated: !!accessToken
    };
    localStorage.setItem('auth', JSON.stringify(next));
    set(next);
  },

  clear: () => {
    localStorage.removeItem('auth');
    set({ accessToken: null, refreshToken: null, userEmail: null, isAuthenticated: false });
  }
}));

// Helpers for non-react modules (e.g., axios interceptors)
export const getAccessToken = (): string | null | undefined => useAuthStore.getState().accessToken;
export const getRefreshToken = (): string | null | undefined => useAuthStore.getState().refreshToken;
export const setAuthTokens = (params: { accessToken: string; refreshToken?: string | null; userEmail?: string | null }) =>
  useAuthStore.getState().setTokens(params);
export const clearAuth = () => useAuthStore.getState().clear();
