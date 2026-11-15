import { apiClient } from '@/lib/api';
import type {
  Round,
  RoundPage,
  Notice,
  NoticePage,
  UserPage,
  BranchPage,
  AuditEventPage,
} from '@/types/governance';

export const approvalApi = {
  listRounds: async (params?: Record<string, string>): Promise<RoundPage> => {
    const res = await apiClient.get<RoundPage>('/api/v1/rounds', { params });
    return res.data;
  },

  getRound: async (id: string): Promise<Round> => {
    const res = await apiClient.get<Round>(`/api/v1/rounds/${id}`);
    return res.data;
  },

  decide: async (
    roundId: string,
    action: 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES',
    notes = ''
  ): Promise<Round> => {
    const res = await apiClient.post<Round>(`/api/v1/rounds/${roundId}/commands`, {
      action,
      notes,
    });
    return res.data;
  },
};

export const noticeApi = {
  list: async (params?: Record<string, string>): Promise<NoticePage> => {
    const res = await apiClient.get<NoticePage>('/api/v1/notices', { params });
    return res.data;
  },

  getById: async (id: string): Promise<Notice> => {
    const res = await apiClient.get<Notice>(`/api/v1/notices/${id}`);
    return res.data;
  },

  acknowledge: async (id: string): Promise<Notice> => {
    const res = await apiClient.post<Notice>(`/api/v1/notices/${id}/commands`, {
      action: 'ACKNOWLEDGE',
    });
    return res.data;
  },
};

export const identityApi = {
  listUsers: async (params?: Record<string, string>): Promise<UserPage> => {
    const res = await apiClient.get<UserPage>('/api/v1/users', { params });
    return res.data;
  },

  listBranches: async (params?: Record<string, string>): Promise<BranchPage> => {
    const res = await apiClient.get<BranchPage>('/api/v1/branches', { params });
    return res.data;
  },
};

export const auditApi = {
  listEvents: async (params?: Record<string, string>): Promise<AuditEventPage> => {
    const res = await apiClient.get<AuditEventPage>('/api/v1/audit-events', { params });
    return res.data;
  },

  exportCsv: async (): Promise<void> => {
    const res = await apiClient.get('/api/v1/exports/audit', {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `audit-export-${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
