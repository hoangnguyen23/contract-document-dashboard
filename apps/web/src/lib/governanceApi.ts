import { apiClient } from '@/lib/api';
import type {
  Round,
  RoundPage,
  RoundSnapshot,
  RoundCommand,
  EvidenceCheck,
  Notice,
  NoticePage,
  NoticeCapture,
  UserPage,
  BranchPage,
  AuditEventPage,
} from '@/types/governance';
import type { MandatoryCheck } from '@/types/business';

export const approvalApi = {
  listRounds: async (params?: Record<string, string>): Promise<RoundPage> => {
    const res = await apiClient.get<RoundPage>('/api/v1/rounds', { params });
    return res.data;
  },

  getRound: async (id: string): Promise<Round> => {
    const res = await apiClient.get<Round>(`/api/v1/rounds/${id}`);
    return res.data;
  },

  getRoundSnapshot: async (id: string): Promise<RoundSnapshot> => {
    const res = await apiClient.get<RoundSnapshot>(`/api/v1/rounds/${id}/snapshot`);
    return res.data;
  },

  command: async (id: string, cmd: RoundCommand): Promise<Round> => {
    const res = await apiClient.post<Round>(`/api/v1/rounds/${id}/commands`, cmd);
    return res.data;
  },

  approve: async (id: string, level: 'L1' | 'L2', reason: string): Promise<Round> => {
    return approvalApi.command(id, {
      action: 'APPROVE',
      level,
      reason,
    });
  },

  returnRound: async (id: string, level: 'L1' | 'L2', reason: string): Promise<Round> => {
    return approvalApi.command(id, {
      action: 'RETURN',
      level,
      reason,
    });
  },

  reject: async (id: string, level: 'L1' | 'L2', reason: string): Promise<Round> => {
    return approvalApi.command(id, {
      action: 'REJECT',
      level,
      reason,
    });
  },

  verifyEvidence: async (
    id: string,
    checks: EvidenceCheck[],
    mandatoryChecks: MandatoryCheck[],
    authorityChecked = true
  ): Promise<Round> => {
    return approvalApi.command(id, {
      action: 'VERIFY_EVIDENCE',
      authorityChecked,
      checks,
      mandatoryChecks,
    });
  },

  reassign: async (
    id: string,
    level: 'L1' | 'L2',
    reviewerId: string,
    reason: string
  ): Promise<Round> => {
    return approvalApi.command(id, {
      action: 'REASSIGN',
      level,
      reviewerId,
      reason,
    });
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

  getCapture: async (id: string): Promise<NoticeCapture> => {
    const res = await apiClient.get<NoticeCapture>(`/api/v1/notices/${id}/capture`);
    return res.data;
  },

  command: async (id: string, cmd: Record<string, unknown>): Promise<Notice> => {
    const res = await apiClient.post<Notice>(`/api/v1/notices/${id}/commands`, cmd);
    return res.data;
  },

  dispatch: async (id: string): Promise<Notice> => {
    return noticeApi.command(id, {
      action: 'DISPATCH',
    });
  },

  recordAck: async (
    id: string,
    data: {
      artifactHash: string;
      recipientSnapshotHash: string;
      acknowledgedAt: string;
      evidenceFileIds: string[];
      basis: string;
    }
  ): Promise<Notice> => {
    return noticeApi.command(id, {
      action: 'RECORD_ACK',
      ...data,
    });
  },

  verifyAck: async (
    id: string,
    candidateId: string,
    decision: 'ACCEPT' | 'RETURN',
    reason: string
  ): Promise<Notice> => {
    return noticeApi.command(id, {
      action: 'VERIFY_ACK',
      candidateId,
      decision,
      reason,
    });
  },

  cancel: async (id: string, reason: string): Promise<Notice> => {
    return noticeApi.command(id, {
      action: 'CANCEL',
      reason,
    });
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

  exportCsv: async (params?: Record<string, string>): Promise<void> => {
    const res = await apiClient.get('/api/v1/exports/audit', {
      params,
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
