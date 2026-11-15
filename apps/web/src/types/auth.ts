export interface User {
  id: string;
  username: string;
  fullName: string;
  email: string;
  active: boolean;
  branchIds: string[];
  roles: string[];
  createdAt: string;
}

export interface SessionData {
  authenticated: boolean;
  csrfToken: string;
  user: User | null;
  expiresAt: string | null;
}

export interface ApiProblem {
  code: string;
  message: string;
  status?: number;
  details?: Record<string, unknown>;
}
