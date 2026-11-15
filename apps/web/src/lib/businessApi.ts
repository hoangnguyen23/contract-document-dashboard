import { apiClient } from '@/lib/api';
import type {
  DashboardMetrics,
  Contract,
  ContractPage,
  Dossier,
  DossierPage,
  StoredFile,
  FilePage,
  RelationshipPage,
  BranchRef,
} from '@/types/business';

export const dashboardApi = {
  getMetrics: async (): Promise<DashboardMetrics> => {
    const res = await apiClient.get<DashboardMetrics>('/api/v1/dashboard');
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

  getVersions: async (id: string): Promise<Contract['currentVersion'][]> => {
    const res = await apiClient.get<Contract['currentVersion'][]>(`/api/v1/contracts/${id}/versions`);
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

  create: async (data: {
    relationshipId: string;
    branchId: string;
    externalAccountReference: string;
    terms: Record<string, unknown>;
  }): Promise<Dossier> => {
    const res = await apiClient.post<Dossier>('/api/v1/dossiers', data);
    return res.data;
  },

  submit: async (id: string): Promise<Dossier> => {
    const res = await apiClient.post<Dossier>(`/api/v1/dossiers/${id}/commands`, {
      action: 'SUBMIT',
    });
    return res.data;
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

  upload: async (file: File, branchId: string, purpose = 'EVIDENCE'): Promise<StoredFile> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('branchId', branchId);
    formData.append('purpose', purpose);

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
};

export const branchRefApi = {
  list: async (): Promise<BranchRef[]> => {
    // Branches endpoint from identity — returns BranchPage; we map to simple refs
    const res = await apiClient.get<{ items: BranchRef[] }>('/api/v1/branches', {
      params: { pageSize: '100' },
    });
    return res.data.items || [];
  },
};
