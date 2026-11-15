import { useAuth } from '@/context/AuthContext';

// ─── Role constants (from B §3) ────────────────────────────────────────────
export const ROLES = {
  BANK_ADMIN:      'BANK_ADMIN',
  MAKER:           'MAKER',
  CHECKER:         'CHECKER',
  MANAGER:         'MANAGER',
  POLICY_AUTHOR:   'POLICY_AUTHOR',
  POLICY_REVIEWER: 'POLICY_REVIEWER',
  DATA_OFFICER:    'DATA_OFFICER',
  AUDITOR:         'AUDITOR',
} as const;

export type AppRole = (typeof ROLES)[keyof typeof ROLES];

// ─── Hook ──────────────────────────────────────────────────────────────────
/**
 * Returns permission helpers derived from the current session.
 * Always use allowedActions[] from API responses for action-level guards;
 * use this hook only for menu visibility and page-level access hints.
 */
export function usePermission() {
  const { user } = useAuth();
  const roles: string[] = user?.roles ?? [];

  const hasRole = (...required: AppRole[]): boolean =>
    required.some((r) => roles.includes(r));

  return {
    isAdmin:        hasRole('BANK_ADMIN'),
    isMaker:        hasRole('MAKER'),
    isChecker:      hasRole('CHECKER'),
    isManager:      hasRole('MANAGER'),
    isPolicyAuthor: hasRole('POLICY_AUTHOR'),
    isPolicyReviewer: hasRole('POLICY_REVIEWER'),
    isDataOfficer:  hasRole('DATA_OFFICER'),
    isAuditor:      hasRole('AUDITOR'),

    // Composite helpers
    canManageUsers:   hasRole('BANK_ADMIN'),
    canViewAuditLogs: hasRole('BANK_ADMIN', 'AUDITOR'),
    canApprove:       hasRole('CHECKER', 'MANAGER'),
    canCreateDossier: hasRole('MAKER'),
    canViewDossiers:  hasRole('MAKER', 'CHECKER', 'MANAGER', 'BANK_ADMIN'),

    hasRole,
    roles,
  };
}
