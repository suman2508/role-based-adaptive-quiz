import create from 'zustand';

/** Derive numeric userId from JWT access token if backend doesn't return it */
function parseUserIdFromToken(token?: string | null): number | null {
  try {
    if (!token) return null;
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payloadJson = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    const payload: any = JSON.parse(payloadJson);
    const cand = payload.userId ?? payload.uid ?? payload.user_id ?? payload.sub ?? null;
    const n = typeof cand === 'string' ? Number(cand) : (typeof cand === 'number' ? cand : null);
    return Number.isFinite(n as number) ? (n as number) : null;
  } catch {
    return null;
  }
}

type AuthState = {
  accessToken?: string | null;
  refreshToken?: string | null;
  userEmail?: string | null;
  userId?: number | null;
  isAuthenticated: boolean;

  setTokens: (params: { accessToken: string; refreshToken?: string | null; userEmail?: string | null; userId?: number | null }) => void;
  clear: () => void;
};

function loadFromStorage(): Partial<AuthState> {
  try {
    const raw = localStorage.getItem('auth');
    if (!raw) return {};
    const parsed = JSON.parse(raw);
    const derivedId = parseUserIdFromToken(parsed.accessToken);
    return {
      accessToken: parsed.accessToken ?? null,
      refreshToken: parsed.refreshToken ?? null,
      userEmail: parsed.userEmail ?? null,
      userId: (typeof parsed.userId === 'number' ? parsed.userId : (parsed.userId ?? derivedId ?? null)),
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
  userId: undefined,
  isAuthenticated: false,
  ...loadFromStorage(),

  setTokens: (params: { accessToken: string; refreshToken?: string | null; userEmail?: string | null; userId?: number | null }) => {
    set((prev) => {
      const computedFromToken = parseUserIdFromToken(params.accessToken);
      const next = {
        accessToken: params.accessToken,
        refreshToken: params.refreshToken ?? prev.refreshToken ?? null,
        userEmail: params.userEmail ?? prev.userEmail ?? null,
        // Preserve existing userId unless explicitly provided; otherwise derive from token payload
        userId: (params.userId !== undefined ? params.userId : (prev.userId ?? computedFromToken ?? null)),
        isAuthenticated: !!params.accessToken
      };
      localStorage.setItem('auth', JSON.stringify(next));
      return next;
    });
  },

  clear: () => {
    localStorage.removeItem('auth');
    set({ accessToken: null, refreshToken: null, userEmail: null, userId: null, isAuthenticated: false });
  }
}));

// Helpers for non-react modules (e.g., axios interceptors)
export const getAccessToken = (): string | null | undefined => useAuthStore.getState().accessToken;
export const getRefreshToken = (): string | null | undefined => useAuthStore.getState().refreshToken;
export const getUserId = (): number | null | undefined => useAuthStore.getState().userId;
export const setAuthTokens = (params: { accessToken: string; refreshToken?: string | null; userEmail?: string | null; userId?: number | null }) =>
  useAuthStore.getState().setTokens(params);
export const clearAuth = () => useAuthStore.getState().clear();
