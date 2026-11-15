// ─── Canonical B1.3.0 Business Types ──────────────────────────────────────────

export interface BlockedAction {
  action: string;
  code: string;
  reason: string;
}

// ─── Terms & Representation (B1.3.0) ─────────────────────────────────────────

export interface Terms {
  currency: 'VND';
  feeVnd: string; // pattern: ^(0|[1-9][0-9]{0,9})$
  feePayer: 'BENEFICIARY' | 'UNAGREED';
  pricingMode: 'NEGOTIATED_FIXED' | 'STANDARD_TARIFF';
  termKind: 'FIXED' | 'OPEN_ENDED';
  startOn: string; // YYYY-MM-DD
  endExclusive: string | null; // YYYY-MM-DD or null
  clauseVersion: 'NEGOTIATED-VA-1' | 'TARIFF-ACK-7D-1';
}

export interface Representation {
  basisType: 'LEGAL_REPRESENTATIVE' | 'POWER_OF_ATTORNEY';
  personName: string;
  title: string;
  scopeText: string;
  validFrom: string; // YYYY-MM-DD
  validTo: string | null; // YYYY-MM-DD or null
  issuerReference: string;
  sourceFileId: string | null; // UUID or null
  checkedAt: string | null; // ISO datetime or null
  checkEvidenceFileId: string | null; // UUID or null
  knownRevocationAt: string | null; // ISO datetime or null
}

export interface DocumentLink {
  fileId: string;
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
    | 'DATA_BASIS';
}

export interface Contact {
  personName: string;
  roleLabel: string;
  email: string;
  phone: string;
  purpose: 'SERVICE_CONTACT' | 'NOTICE_RECIPIENT';
}

export interface MandatoryCheck {
  code: 'AUTHORITY_CURRENT' | 'LEGAL_CONDITION' | 'SERVICE_READINESS';
  result: 'PASS' | 'BLOCK' | 'REVIEW_REQUIRED';
  basis: string;
  evidenceFileIds: string[];
  checkedAt: string; // ISO datetime UTC
}

// ─── Dossier ─────────────────────────────────────────────────────────────────

export interface DossierCreate {
  relationshipId: string;
  externalAccountReference: string;
  linkedFromDossierId: string | null;
  terms: Terms;
  representation: Representation;
  documents: DocumentLink[];
}

export interface DossierUpdate {
  terms: Terms;
  representation: Representation;
  documents: DocumentLink[];
  externalAccountReference: string;
}

export interface Dossier {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  branchId: string;
  state: 'DRAFT' | 'IN_REVIEW' | 'RETURNED' | 'APPROVED' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';
  relationshipId: string;
  assignedMakerId: string;
  linkedFromDossierId: string | null;
  terms: Terms;
  representation: Representation;
  contactSnapshot: Contact;
  templateVersion: string;
  bundleVersion: string;
  documents: DocumentLink[];
  contributorIds: string[];
  revision: number;
  roundId: string | null;
  contractId: string | null;
  mandatoryChecks: MandatoryCheck[];
  externalAccountReference: string;
}

// ─── Dossier Commands ────────────────────────────────────────────────────────

export interface DossierCommandSubmit {
  action: 'SUBMIT';
  l1ReviewerId: string;
  l2ReviewerId: string | null;
  mandatoryChecks: MandatoryCheck[];
}

export interface DossierCommandWithdraw {
  action: 'WITHDRAW';
  reason: string;
}

export interface DossierCommandCancel {
  action: 'CANCEL';
  reason: string;
}

export interface DossierCommandClone {
  action: 'CLONE';
  relationshipId: string;
  reason: string;
}

export type DossierCommand =
  | DossierCommandSubmit
  | DossierCommandWithdraw
  | DossierCommandCancel
  | DossierCommandClone;

// ─── Contract ────────────────────────────────────────────────────────────────

export interface ContractVersion {
  id: string;
  contractId: string;
  version: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  terms: Terms;
  changeId: string | null;
  recordedAt: string;
}

export interface Contract {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  branchId: string;
  state: 'PREPARED' | 'ACTIVE' | 'TERMINATED' | 'CLOSED';
  dossierId: string;
  relationshipId: string;
  assignedMakerId: string;
  currentVersion: ContractVersion | null;
  scheduleVersion: number;
  scheduledChangeId: string | null;
  artifactJobId: string | null;
  readiness: 'READY' | 'NOT_READY' | 'EXPIRED';
  signingPacketId: string | null;
  serviceState: 'NORMAL' | 'SUSPENDED' | 'DISCONTINUED';
  mandatoryChecks: MandatoryCheck[];
  terminationEffectOn: string | null;
  terminationObservedAt: string | null;
  approvedTerms: Terms | null;
  approvedArtifactId: string | null;
  approvedArtifactHash: string | null;
  readinessVerifiedAt: string | null;
  readinessRisks: string[];
  effectiveAt: string | null;
  recordedAt: string;
  approvedRevisionId: string | null;
}

// ─── Contract Commands ───────────────────────────────────────────────────────

export interface ContractCommandHoldReadiness {
  action: 'HOLD_READINESS';
  reason: string;
  evidenceIds: string[];
}

export interface ContractCommandReleaseReadiness {
  action: 'RELEASE_READINESS';
  riskId: string;
  basis: string;
  evidenceIds: string[];
}

export interface ContractCommandCancel {
  action: 'CANCEL';
  reason: string;
  cancellationEvidenceFileIds: string[];
}

export type ContractCommand =
  | ContractCommandHoldReadiness
  | ContractCommandReleaseReadiness
  | ContractCommandCancel;

// ─── Files ───────────────────────────────────────────────────────────────────

export interface StoredFile {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
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
  scannedAt: string | null;
  scanJobId: string | null;
  scannerVersion: string | null;
  signatureDatabaseVersion: string | null;
  origin: string;
  targetId: string | null;
  targetKind: string | null;
  rendererVersion: string | null;
  generatedAt: string | null;
  humanVerification: unknown | null;
  allowedActions: string[];
  blockedActions: BlockedAction[];
}

// ─── Relationship & Party ────────────────────────────────────────────────────

export interface Relationship {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  branchId: string;
  partyId: string;
  active: boolean;
  assignedMakerId: string;
  confirmedContact: Contact;
  contactVersion: number;
  allowedActions: string[];
  blockedActions: BlockedAction[];
  // Optional enriched name if mapped from Party
  counterpartyName?: string;
}

export interface Party {
  id: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  name: string;
  taxId: string;
  allowedActions: string[];
  blockedActions: BlockedAction[];
}

// ─── Reviewer Options ────────────────────────────────────────────────────────

export interface ReviewerOption {
  userId: string;
  displayName: string;
  level: 'L1' | 'L2';
}

export interface ReviewerOptions {
  asOf: string;
  items: ReviewerOption[];
}

// ─── Dashboard Metrics ───────────────────────────────────────────────────────

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

// ─── Common Page Response (B §5: items, nextCursor, asOf, timelineProcessingLagSeconds) ──

export interface PageResponse<T> {
  items: T[];
  nextCursor: string | null;
  asOf: string;
  timelineProcessingLagSeconds: number;
}

export type DossierPage = PageResponse<Dossier>;
export type ContractPage = PageResponse<Contract>;
export type RelationshipPage = PageResponse<Relationship>;
export type FilePage = PageResponse<StoredFile>;

// ─── Branch (simple reference) ────────────────────────────────────────────────
export interface BranchRef {
  id: string;
  code: string;
  name: string;
  active?: boolean;
}
