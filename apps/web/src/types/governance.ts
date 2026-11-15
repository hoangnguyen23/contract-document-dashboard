import type { BlockedAction, Contact, MandatoryCheck, PageResponse, Terms } from './business';

// ─── Approval Step & Round ───────────────────────────────────────────────────

export interface ApprovalStep {
  level: 'L1' | 'L2';
  state: 'WAITING' | 'PENDING' | 'APPROVED' | 'RETURNED' | 'REJECTED' | 'CANCELLED';
  reviewerId: string | null;
  decidedBy: string | null;
  decisionAt: string | null;
  reason: string | null;
}

export interface EvidenceCheck {
  fileId: string;
  result: 'PASS' | 'FAIL' | 'NEEDS_CLARIFICATION';
  reason: string;
  basis: string;
  checkedAt: string;
}

export interface Round {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  branchId: string;
  targetKind: 'DOSSIER' | 'CHANGE' | 'CONTROL_REQUEST';
  targetId: string;
  revisionId: string;
  state: 'OPEN' | 'APPROVED' | 'RETURNED' | 'REJECTED' | 'CANCELLED';
  policyVersionId: string;
  dueAt: string;
  steps: ApprovalStep[];
  evidenceChecks: EvidenceCheck[];
  authorityChecked: boolean;
  mandatoryChecks: MandatoryCheck[];
}

export type RoundPage = PageResponse<Round>;

// ─── Round Commands ──────────────────────────────────────────────────────────

export interface RoundCommandApprove {
  action: 'APPROVE';
  level: 'L1' | 'L2';
  reason: string;
}

export interface RoundCommandReturn {
  action: 'RETURN';
  level: 'L1' | 'L2';
  reason: string;
}

export interface RoundCommandReject {
  action: 'REJECT';
  level: 'L1' | 'L2';
  reason: string;
}

export interface RoundCommandVerifyEvidence {
  action: 'VERIFY_EVIDENCE';
  authorityChecked: boolean;
  checks: EvidenceCheck[];
  mandatoryChecks: MandatoryCheck[];
}

export interface RoundCommandReassign {
  action: 'REASSIGN';
  level: 'L1' | 'L2';
  reviewerId: string;
  reason: string;
}

export type RoundCommand =
  | RoundCommandApprove
  | RoundCommandReturn
  | RoundCommandReject
  | RoundCommandVerifyEvidence
  | RoundCommandReassign;

// ─── Round Snapshot ──────────────────────────────────────────────────────────

export interface DossierRoundSnapshot {
  targetKind: 'DOSSIER';
  revision: {
    id: string;
    number: number;
    terms: Terms;
    representation: unknown;
    contact: Contact;
    documents: unknown[];
    contributorIds: string[];
    factsHash: string;
    evidenceHash: string;
    evaluation: unknown;
    submittedAt: string;
    externalAccountReference: string;
  };
}

export interface ChangeRoundSnapshot {
  targetKind: 'CHANGE';
  revision: unknown;
}

export interface ControlRoundSnapshot {
  targetKind: 'CONTROL_REQUEST';
  revision: unknown;
}

export type RoundSnapshot = DossierRoundSnapshot | ChangeRoundSnapshot | ControlRoundSnapshot;

// ─── Notice ──────────────────────────────────────────────────────────────────

export interface Notice {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  branchId: string;
  state:
    | 'DRAFT'
    | 'IN_REVIEW'
    | 'APPROVED'
    | 'QUEUED'
    | 'DISPATCHED'
    | 'FAILED'
    | 'ACKNOWLEDGED'
    | 'CANCELLED';
  contractId: string;
  changeId: string | null;
  purpose: 'TARIFF_CHANGE' | 'CONTRACTUAL_REMINDER' | 'TERMINATION' | 'GENERAL';
  clauseVersion: string;
  subject: string;
  bodyText: string;
  recipient: Contact;
  contactVersion: number;
  channel: 'EMAIL';
  transport: 'LOCAL_CAPTURE' | 'MANUAL_EXTERNAL';
  contributorIds: string[];
  messageId: string | null;
  jobId: string | null;
  dispatchAt: string | null;
  deliveredAt: string | null;
  deemedReceivedAt: string | null;
  acknowledgedAt: string | null;
  receiptEvidence: unknown | null;
  superseded: boolean;
  artifactId: string | null;
  artifactHash: string | null;
  artifactJobId: string | null;
  recipientSnapshotHash: string | null;
  ackCandidate: unknown | null;
}

export type NoticePage = PageResponse<Notice>;

export interface NoticeCapture {
  noticeId: string;
  messageId: string;
  recipient: Contact;
  subject: string;
  bodyText: string;
  artifactHash: string;
  acceptedAt: string;
}

// ─── Identity (Users & Branches) ─────────────────────────────────────────────

export interface SystemUser {
  id: string;
  version: number;
  username: string;
  displayName: string;
  active: boolean;
  branchIds: string[];
  roles: string[];
  permissions: string[];
  allowedActions: string[];
  blockedActions: BlockedAction[];
  createdAt: string;
  updatedAt: string;
}

export type UserPage = PageResponse<SystemUser>;

export interface Branch {
  id: string;
  version: number;
  code: string;
  name: string;
  active: boolean;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  createdAt: string;
  updatedAt: string;
}

export type BranchPage = PageResponse<Branch>;

// ─── Audit ───────────────────────────────────────────────────────────────────

export interface AuditEvent {
  id: string;
  action: string;
  actorId: string;
  branchId: string | null;
  resourceType: string;
  resourceId: string;
  resourceVersion: number | null;
  reason: string | null;
  occurredAt: string;
  traceId: string | null;
  beforeHash: string | null;
  afterHash: string | null;
}

export type AuditEventPage = PageResponse<AuditEvent>;
