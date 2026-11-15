export interface RoundStep {
  stepIndex: number;
  role: string;
  assignedActorId?: string;
  decidedByActorId?: string;
  decision?: 'APPROVED' | 'REJECTED' | 'REQUEST_CHANGES';
  notes?: string;
  decidedAt?: string;
}

export interface Round {
  id: string;
  targetId: string;
  targetKind: 'DOSSIER' | 'CHANGE' | 'CONTROL_REQUEST';
  branchId: string;
  state: 'OPEN' | 'APPROVED' | 'REJECTED' | 'EXPIRED';
  steps: RoundStep[];
  dueAt: string;
  version: number;
  allowedActions: string[];
  blockedActions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface RoundPage {
  items: Round[];
  page: number;
  pageSize: number;
  total: number;
}

export interface Notice {
  id: string;
  contractId: string;
  branchId: string;
  subject: string;
  bodyText: string;
  purpose: string;
  channel: string;
  state: 'DRAFT' | 'DISPATCHED' | 'DELIVERED' | 'ACKNOWLEDGED' | 'FAILED' | 'CANCELLED';
  recipient: {
    name?: string;
    email?: string;
    phone?: string;
  };
  deliveredAt?: string;
  acknowledgedAt?: string;
  allowedActions: string[];
  blockedActions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NoticePage {
  items: Notice[];
  page: number;
  pageSize: number;
  total: number;
}

export interface SystemUser {
  id: string;
  username: string;
  displayName?: string;
  active: boolean;
  branchIds: string[];
  roles: string[];
  permissions: string[];
  createdAt: string;
  updatedAt: string;
}

export interface UserPage {
  items: SystemUser[];
  page: number;
  pageSize: number;
  total: number;
}

export interface Branch {
  id: string;
  code: string;
  name: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BranchPage {
  items: Branch[];
  page: number;
  pageSize: number;
  total: number;
}

export interface AuditEvent {
  id: string;
  action: string;
  actorId: string;
  branchId?: string;
  resourceType: string;
  resourceId: string;
  resourceVersion?: number;
  reason?: string;
  occurredAt: string;
  traceId?: string;
}

export interface AuditEventPage {
  items: AuditEvent[];
  page: number;
  pageSize: number;
  total: number;
}
