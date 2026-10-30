# Independent baseline caller review — 30 October 2026

Scope: static read of the repaired SQL definitions and direct caller chains, ListQueries page dispatch, canonical inventory operation IDs, ApprovalService and SigningService target routing. Application baseline81c3d7f. Reviewer did not implement the repair or baseline documents. No tests, build, HTTP, runtime, database or Git mutations were performed. This report alone does not close every BE-C01 requirement.

## Exact JSONB repair reach

| Repaired SQL | Direct callers and actual operation IDs | Limit |
|---|---|---|
| EvidenceAccess.sqlScope, line112 onward; recursive ancestor expression at121 | FileService.list at32 → FileController → `listFiles`; JobService.list at19 → JobController → `listJobs` | These are the only sqlScope call sites found in current Java source. Other EvidenceAccess users do not execute this particular SQL merely because they inject EvidenceAccess. |
| ReportingScope.PARENT_TYPE/PARENT_ID at19–33, via business/audit predicates | DashboardService.dashboard → `getDashboard`; AuditReportingService.list/export → `listAuditEvents`, `exportAuditCsv` | Audit includes ordinary, staging-deletion and null-branch policy ancestry branches. Repair preserves their existing bind arguments. |
| ListQueries.timelineLag at100–149 | `listRelationships`, `listContactRequests`, `listDossiers`, `listRounds`, `listContracts`, `listSigningPackets`, `listChanges`, `listNotices`, `listObligations`, `listIncidents`, `listControlRequests`, `listDataRequests`, `listFiles`, `listJobs`; also `listContractVersions`, `getDashboard`, `listAuditEvents` | Every listed ordinary page uses the lag SQL even when items are empty. `listContractVersions` goes through ContractLifecycle. Dashboard/Audit supply their snapshot instant. |

The generic page helper also serves `listUsers`, `listBranchs`, `listPartys`, `listPolicys`, but its line51 conditional returns lag0 for User/Branch/Party/Policy, so these do not execute the repaired timeline SQL. Keep these canonical spellings; do not rename operation IDs to improve English. `exportAuditCsv` uses repaired ReportingScope ancestry, not ListQueries.timelineLag.

No canonical public field, status, scope, permission, action or schema changed in this repair. `listFiles` is the known executed SQL failure; the other operations are source-derived reach, not asserted historical failures or new runtime PASS results. Existing independent repair review covers query semantics/argument preservation. This review adds the precise affected endpoint map.

## Email repair reach

ContractRegistry replaces only the email format in its shared request/response schema dialect. Recursive traversal of every canonical requestBody/parameters/responses, resolving local refs with per-path cycle protection, identifies 24 operations with at least one reachable email-format schema branch:

- ContactRequests: `listContactRequests`, `createContactRequest`, `getContactRequest`, `commandContactRequest`.
- DataRequests: `listDataRequests`, `createDataRequest`, `getDataRequest`, `commandDataRequest`.
- Dossiers: `listDossiers`, `createDossier`, `getDossier`, `updateDossier`, `commandDossier`.
- Notices: `listNotices`, `createNotice`, `getNotice`, `updateNotice`, `getNoticeCapture`, `commandNotice`.
- Relationships: `listRelationships`, `createRelationship`, `getRelationship`, `updateRelationship`.
- Frozen snapshots: `getRoundSnapshot`.

Request-side reach exists for `createContactRequest`, `commandDataRequest`, `createRelationship`; all24 have response-side reach. This is a schema reach map: optional/union branches need not be present in every actual response. The original observed FAILs remain s07 create422 and s12 authorized read503. No public schema change occurred; no new operation failure or runtime PASS is inferred.

## Action and target inventory — bounded review

ApprovalService.list explicitly selects DOSSIER/CHANGE/CONTROL_REQUEST (line123); target detection/delegation exists at263–267. SigningService.list explicitly selects CONTRACT/CHANGE (line184). These agree with the corresponding inventory target lists; this is routing evidence, not approval/signing semantic acceptance.

Inventory still labels `listObligations`/`getObligation` CLOSEOUT as `CLOSEOUT:read/source only`, whereas the final handoff says the Control tranche supplies the actual producer and commandObligation lists CLOSEOUT. This wording should be reconciled with the current source-review evidence rather than copied as a current missing business dependency. Severity P3, non-blocking documentation ambiguity; owner decision not required. Do not remove it blindly without retaining that source/history provenance.

Canonical top-level command action schema sets were recursively resolved through local refs and oneOf/anyOf/allOf, without descending into nested disposition action properties. All14 `command*` operations with supportedActions exactly match their inventory sets; all their inventory action literals occur in the inventory-referenced implementation files. Nested DataRequest disposition actions are correctly separate from commandDataRequest actions. This is schema/list/literal parity, not semantic guard certification. `simulatePolicy` records FACTS/REGRESSION modes in supportedActions rather than an action property; that inventory terminology is historical and does not mean a canonical command action is missing.

The complete target semantics and every command branch were not independently re-reviewed here. Presence of a string or route is not proof that every current guard/side effect is correct. Prior module reviews remain attributed evidence. No new concrete blocking source defect was identified within this caller-map scope, and no canonical/product decision is requested.

The two stale inventory CLOSEOUT labels were changed to plain CLOSEOUT, using the existing Control source review's explicit real-producer proof (backend-control-source-review.md, lines35/48). Only those two byte strings changed; no counts, coverage claim, source, canonical or historical review was rewritten. This resolves the P3 wording ambiguity above, not runtime acceptance.

## Verdict and remaining work

Caller mapping for the ten JSONB replacements and email schema reach: **GO at static source scope**. HTTP compatibility/retest: **NOT RUN**. BE-C01 overall: full action/target reconciliation remains bounded by prior module evidence. Reviewed `backend-current-status.md` and baseline JSON: correctly distinguish inventory/path presence from semantic certification; hashes match recorded canonical values and current-status preserves prior FAILs and testing deferral. No test authorization or production-readiness implication.
