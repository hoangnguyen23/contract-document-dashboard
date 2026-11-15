export interface DashboardMetrics {
  asOf: string;
  branchIds: string[];
  activeAgreements: number;
  expiring30: number;
  reviewOverdue: number;
  overdueObligations: number;
  historicalBreaches: number;
  noticeFailures: number;
  pendingServiceControls: number;
  timelineProcessingLagSeconds: number;
  unresolvedCloseout: number;
  dossiersByState: Array<{
    state: 'DRAFT' | 'IN_REVIEW' | 'RETURNED' | 'APPROVED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';
    count: number;
  }>;
}

export interface ContractVersion {
  version: number;
  effectiveFrom: string;
  effectiveTo?: string;
  terms: {
    termKind: 'FIXED' | 'OPEN_ENDED';
    feeStructure?: Record<string, unknown>;
    collectionLimit?: number;
    [key: string]: unknown;
  };
  changeId?: string;
  recordedAt: string;
}

export interface Contract {
  id: string;
  dossierId: string;
  relationshipId: string;
  branchId: string;
  state: 'PREPARED' | 'ACTIVE' | 'TERMINATED' | 'CLOSED';
  serviceState: 'NORMAL' | 'SUSPENDED' | 'DISCONTINUED';
  assignedMakerId: string;
  version: number;
  effectiveAt?: string;
  currentVersion?: ContractVersion;
  readiness: 'READY' | 'NOT_READY' | 'EXPIRED';
  readinessRisks: string[];
  readinessVerifiedAt?: string;
  approvedArtifactId?: string;
  approvedArtifactHash?: string;
  signingPacketId?: string;
  allowedActions: string[];
  blockedActions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ContractPage {
  items: Contract[];
  page: number;
  pageSize: number;
  total: number;
}

export interface Dossier {
  id: string;
  relationshipId: string;
  branchId: string;
  assignedMakerId: string;
  state: 'DRAFT' | 'IN_REVIEW' | 'RETURNED' | 'APPROVED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';
  roundId?: string;
  contractId?: string;
  version: number;
  externalAccountReference: string;
  terms: {
    serviceType: string;
    settlementCycle: string;
    maxCollectionAmount: number;
    [key: string]: unknown;
  };
  contactSnapshot: {
    contactName: string;
    contactEmail: string;
    contactPhone: string;
  };
  documents: Array<{
    fileId: string;
    documentType: string;
    uploadedAt: string;
  }>;
  allowedActions: string[];
  blockedActions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface DossierPage {
  items: Dossier[];
  page: number;
  pageSize: number;
  total: number;
}

export interface StoredFile {
  id: string;
  branchId: string;
  filename: string;
  mediaType: string;
  sizeBytes: number;
  sha256: string;
  state: 'PENDING_SCAN' | 'CLEAN' | 'INFECTED' | 'REDACTED' | 'DISPOSED' | 'SCAN_FAILED';
  purpose: string;
  attached: boolean;
  sensitive: boolean;
  uploadedBy: string;
  uploadedAt: string;
  scannedAt?: string;
  allowedActions: string[];
  blockedActions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface FilePage {
  items: StoredFile[];
  page: number;
  pageSize: number;
  total: number;
}

// ─── Relationship ─────────────────────────────────────────────────────────────
export interface Relationship {
  id: string;
  branchId: string;
  counterpartyName: string;
  counterpartyTaxId?: string;
  state: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

export interface RelationshipPage {
  items: Relationship[];
  page: number;
  pageSize: number;
  total: number;
}

// ─── Branch (simple reference) ────────────────────────────────────────────────
export interface BranchRef {
  id: string;
  code: string;
  name: string;
  region?: string;
}
