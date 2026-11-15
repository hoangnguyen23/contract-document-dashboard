import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { generateIdempotencyKey, etagCache } from '@/lib/apiUtils';
import type { ApiProblem } from '@/lib/apiUtils';

// ─── CSRF token store (memory-only, never localStorage) ───────────────────
let currentCsrfToken: string | null = null;

export const setCsrfToken = (token: string | null) => {
  currentCsrfToken = token;
};

export const getCsrfToken = (): string | null => currentCsrfToken;

// ─── 401 redirect callback ────────────────────────────────────────────────
// Set by AuthProvider so the interceptor can trigger logout/redirect
// without depending on React context (avoids circular deps)
let onUnauthenticated: (() => void) | null = null;

export const setUnauthenticatedHandler = (handler: () => void) => {
  onUnauthenticated = handler;
};

// ─── Operations that do NOT need Idempotency-Key ─────────────────────────
// (session bootstrap, login, logout, file download, simulation reads)
const IDEMPOTENCY_EXEMPT = new Set([
  '/api/v1/session',
  '/api/v1/session/login',
  '/api/v1/session/logout',
]);

function isIdempotencyExempt(url: string | undefined): boolean {
  if (!url) return false;
  return IDEMPOTENCY_EXEMPT.has(url) || url.startsWith('/api/v1/exports/');
}

// ─── Axios instance ───────────────────────────────────────────────────────
export const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  withCredentials: true, // Send CG_SESSION cookie on all requests
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Request interceptor ──────────────────────────────────────────────────
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const method = config.method?.toUpperCase();
    const isMutation = method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method);

    // 1. Inject CSRF token for all unsafe methods (B §3)
    if (isMutation && currentCsrfToken) {
      config.headers.set('X-CSRF-Token', currentCsrfToken);
    }

    // 2. Inject Idempotency-Key for all business mutations (B §4)
    if (isMutation && !isIdempotencyExempt(config.url)) {
      // Only add if not already set (allows caller to pass a stable key for retries)
      if (!config.headers.get('Idempotency-Key')) {
        config.headers.set('Idempotency-Key', generateIdempotencyKey());
      }
    }

    // 3. Inject If-Match for existing-resource mutations (B §4)
    // Mutations (PUT, DELETE, and POST /{collection}/{id}/commands) require If-Match
    if (isMutation && config.url) {
      const existingIfMatch = config.headers.get('If-Match');
      if (existingIfMatch) {
        // Ensure canonical quotation ^"r-[1-9][0-9]*"$
        const raw = String(existingIfMatch).replace(/^"|"$/g, '');
        config.headers.set('If-Match', `"${raw}"`);
      } else {
        // Auto-lookup ETag from resource ID in URL: /api/v1/{collection}/{id} or /api/v1/{collection}/{id}/commands
        const match = config.url.match(/\/api\/v1\/[^/]+\/([0-9a-f-]{36})(?:\/|$)/i);
        if (match) {
          const resourceId = match[1];
          const cached = etagCache.get(resourceId);
          if (cached) {
            config.headers.set('If-Match', `"${cached.replace(/^"|"$/g, '')}"`);
          }
        }
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response interceptor ─────────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => {
    // Update CSRF token when server rotates it (after login/logout)
    if (response.data && typeof response.data.csrfToken === 'string') {
      setCsrfToken(response.data.csrfToken);
    }

    // Cache ETag from resource responses (B §4: resource detail/create/update/command)
    const etag = response.headers['etag'];
    const match = response.config.url?.match(/\/api\/v1\/[^/]+\/([0-9a-f-]{36})/i);
    const resourceId = match ? match[1] : response.data?.id;

    if (resourceId) {
      if (etag) {
        etagCache.set(resourceId, etag.replace(/^"|"$/g, ''));
      } else if (typeof response.data?.version === 'number' && response.data.version > 0) {
        etagCache.set(resourceId, `r-${response.data.version}`);
      }
    }

    return response;
  },
  (error) => {
    const status: number = error.response?.status;
    const data = error.response?.data;

    // Build normalized ApiProblem from RFC9457 response (B §4)
    const problem: ApiProblem = {
      type: data?.type,
      title: data?.title,
      status: status || 0,
      detail: data?.detail || data?.message || error.message || 'Lỗi kết nối máy chủ',
      instance: data?.instance,
      code: data?.code ?? (status === 0 ? 'NETWORK_ERROR' : 'UNKNOWN_ERROR'),
      traceId: data?.traceId,
      violations: data?.violations || [],
      currentVersion: data?.currentVersion,
      retryAfterSeconds: parseRetryAfter(error.response?.headers?.['retry-after']),
    };

    // ─── Global side-effects by status ──────────────────────────────────
    if (status === 401) {
      // Session expired or unauthenticated — trigger redirect to /login
      onUnauthenticated?.();
    }

    // 412 / 428 / 429 / 503 → let caller handle via formatApiError()
    // We only do redirect for 401 globally; all others bubble up.

    return Promise.reject(problem);
  }
);

// ─── Helpers ──────────────────────────────────────────────────────────────
function parseRetryAfter(header: string | undefined): number | undefined {
  if (!header) return undefined;
  const n = parseInt(header, 10);
  return isNaN(n) ? undefined : n;
}
