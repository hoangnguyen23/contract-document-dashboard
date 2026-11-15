import { apiClient } from '@/lib/api';
import type { SessionData } from '@/types/auth';

export const authApi = {
  getSession: async (): Promise<SessionData> => {
    const response = await apiClient.get<SessionData>('/api/v1/session');
    return response.data;
  },

  login: async (username: string, password: string): Promise<SessionData> => {
    const response = await apiClient.post<SessionData>('/api/v1/session/login', {
      username,
      password,
    });
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/v1/session/logout');
  },
};
