import { apiClient } from '@/lib/api';
import type {
  DashboardMetrics,
  Contract,
  ContractPage,
  ContractCommand,
  ContractVersion,
  Dossier,
  DossierPage,
  DossierCreate,
  DossierUpdate,
  DossierCommand,
  DossierCommandSubmit,
  StoredFile,
  FilePage,
  Relationship,
  RelationshipPage,
  BranchRef,
  ReviewerOptions,
} from '@/types/business';

export const dashboardApi = {
  getMetrics: async (): Promise<DashboardMetrics> => {
    const res = await apiClient.get<DashboardMetrics>('/api/v1/dashboard');
    return res.data;
  },
};

export const reviewerApi = {
  getOptions: async (
    targetKind: 'DOSSIER' | 'CHANGE' | 'CONTROL_REQUEST',
    targetId: string
  ): Promise<ReviewerOptions> => {
    const res = await apiClient.get<ReviewerOptions>('/api/v1/reviewer-options', {
      params: { targetKind, targetId },
    });
    return res.data;
  },
};

export const dossierApi = {
  list: async (params?: Record<string, string>): Promise<DossierPage> => {
    const res = await apiClient.get<DossierPage>('/api/v1/dossiers', { params });
    return res.data;
  },

  getById: async (id: string): Promise<Dossier> => {
    const res = await apiClient.get<Dossier>(`/api/v1/dossiers/${id}`);
    return res.data;
  },

  create: async (data: DossierCreate): Promise<Dossier> => {
    const res = await apiClient.post<Dossier>('/api/v1/dossiers', data);
    return res.data;
  },

  update: async (id: string, data: DossierUpdate): Promise<Dossier> => {
    const res = await apiClient.put<Dossier>(`/api/v1/dossiers/${id}`, data);
    return res.data;
  },

  command: async (id: string, cmd: DossierCommand): Promise<Dossier> => {
    const res = await apiClient.post<Dossier>(`/api/v1/dossiers/${id}/commands`, cmd);
    return res.data;
  },

  submit: async (
    id: string,
    submitData: Omit<DossierCommandSubmit, 'action'>
  ): Promise<Dossier> => {
    return dossierApi.command(id, {
      action: 'SUBMIT',
      ...submitData,
    });
  },

  withdraw: async (id: string, reason: string): Promise<Dossier> => {
    return dossierApi.command(id, {
      action: 'WITHDRAW',
      reason,
    });
  },

  cancel: async (id: string, reason: string): Promise<Dossier> => {
    return dossierApi.command(id, {
      action: 'CANCEL',
      reason,
    });
  },

  clone: async (id: string, relationshipId: string, reason: string): Promise<Dossier> => {
    const res = await apiClient.post<Dossier>(`/api/v1/dossiers/${id}/commands`, {
      action: 'CLONE',
      relationshipId,
      reason,
    });
    return res.data;
  },
};

export const contractApi = {
  list: async (params?: Record<string, string>): Promise<ContractPage> => {
    const res = await apiClient.get<ContractPage>('/api/v1/contracts', { params });
    return res.data;
  },

  getById: async (id: string): Promise<Contract> => {
    const res = await apiClient.get<Contract>(`/api/v1/contracts/${id}`);
    return res.data;
  },

  getVersions: async (id: string): Promise<ContractVersion[]> => {
    const res = await apiClient.get<ContractVersion[]>(`/api/v1/contracts/${id}/versions`);
    return res.data;
  },

  command: async (id: string, cmd: ContractCommand): Promise<Contract> => {
    const res = await apiClient.post<Contract>(`/api/v1/contracts/${id}/commands`, cmd);
    return res.data;
  },

  holdReadiness: async (id: string, reason: string, evidenceIds: string[]): Promise<Contract> => {
    return contractApi.command(id, {
      action: 'HOLD_READINESS',
      reason,
      evidenceIds,
    });
  },

  releaseReadiness: async (
    id: string,
    riskId: string,
    basis: string,
    evidenceIds: string[]
  ): Promise<Contract> => {
    return contractApi.command(id, {
      action: 'RELEASE_READINESS',
      riskId,
      basis,
      evidenceIds,
    });
  },

  cancel: async (
    id: string,
    reason: string,
    cancellationEvidenceFileIds: string[] = []
  ): Promise<Contract> => {
    return contractApi.command(id, {
      action: 'CANCEL',
      reason,
      cancellationEvidenceFileIds,
    });
  },
};

export const fileApi = {
  list: async (params?: Record<string, string>): Promise<FilePage> => {
    const res = await apiClient.get<FilePage>('/api/v1/files', { params });
    return res.data;
  },

  getById: async (id: string): Promise<StoredFile> => {
    const res = await apiClient.get<StoredFile>(`/api/v1/files/${id}`);
    return res.data;
  },

  upload: async (
    file: File,
    branchId: string,
    purpose:
      | 'SERVICE_APPLICATION'
      | 'CORPORATE_ID'
      | 'REPRESENTATION_BASIS'
      | 'SERVICE_TERMS'
      | 'IDENTITY_IMAGE'
      | 'SIGNING_COPY'
      | 'NOTICE_EVIDENCE'
      | 'DUTY_EVIDENCE'
      | 'CONTROL_EVIDENCE'
      | 'DATA_BASIS',
    targetKind: string,
    targetId: string
  ): Promise<StoredFile> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('branchId', branchId);
    formData.append('purpose', purpose);
    formData.append('targetKind', targetKind);
    formData.append('targetId', targetId);

    const res = await apiClient.post<StoredFile>('/api/v1/files', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },

  download: async (id: string, filename: string): Promise<void> => {
    const res = await apiClient.get(`/api/v1/files/${id}/content`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};

export const relationshipApi = {
  list: async (params?: Record<string, string>): Promise<RelationshipPage> => {
    const res = await apiClient.get<RelationshipPage>('/api/v1/relationships', { params });
    return res.data;
  },

  getById: async (id: string): Promise<Relationship> => {
    const res = await apiClient.get<Relationship>(`/api/v1/relationships/${id}`);
    return res.data;
  },
};

export const branchRefApi = {
  list: async (): Promise<BranchRef[]> => {
    const res = await apiClient.get<{ items: BranchRef[] }>('/api/v1/branches', {
      params: { limit: '100' },
    });
    return res.data.items || [];
  },
};
