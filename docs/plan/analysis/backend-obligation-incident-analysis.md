# Obligation and Incident implementation handoff

Read-only analysis against reviewed G4 source, canonical A/B1.2.2, contracts/openapi-v1.json and current lifecycle/reporting infrastructure. Notice V010 is being implemented separately. This document is outside the repository, not a canonical change or an implementation claim. No tests, HTTP, Docker or data operations were performed for this analysis.

## Ownership and prerequisites

Planned branch: feature/backend-obligations-incidents, based on integrated/reviewed Notice plus G4. Root alone allocates V011. V001–V009 are already applied and immutable; V010 belongs to Notice. Do not create a competing obligation ledger or rewrite historical migrations.

Own obligation/** and incident/** plus necessary files-owned evidence association/locking integration. Coordinate Notice-owned duty creation/ack closure/inapplicability ports before writing. Reporting/** already exists; preserve its SQL formulas and table ownership. Control public APIs come next; implement closeout guards against real persisted controls/service evidence, never a successful no-op callback.

Two G4 defects found during this analysis have already been fixed separately at application commit2af915d: supersession closureOutcome=null (rather than forbidden free text), and factory missed=now.isAfter(due). Independent source/build evidence follows that repair; do not reintroduce >=due or mutate V009.

## Seven operations and exact actions

Obligation: listObligations, getObligation, commandObligation. There is no public create/update/delete obligation operation: duties derive from sources.

commandObligation exact nine actions:

- SUBMIT_EVIDENCE: evidenceFileIds, summary, closureOutcome, externalConfirmations. Maker source submission from OPEN/RETURNED to EVIDENCE_SUBMITTED. closureOutcome request enum is FULFILLED/RENEW/DECLINE/NEW_AGREEMENT; never introduce arbitrary string outcomes.
- APPROVE: reason. Independent CHECKER accepts exact submitted source to CLOSED after all type-specific guards.
- RETURN: reason. Independent CHECKER returns to RETURNED; old evidence/assessment preserved. No invented correctionItems JSON field.
- REQUEST_EXTENSION: requestedDueAt, reason, evidenceFileIds. Assigned MAKER; request does not alter operative due.
- DECIDE_EXTENSION: requestId, decision(APPROVE/REJECT), reason, basis, bilateralAcceptanceFileIds. Independent MANAGER, excluding requester and all source contributors.
- REQUEST_WAIVER: reason, evidenceFileIds. Same request-only principle.
- DECIDE_WAIVER: same decision shape as extension. Approved valid waiver cancels the duty with closureOutcome=WAIVED and immutable decision; it does not erase breach history.
- SUPERSEDE: replacementId(nullable), endingBasis, evidenceFileIds.
- CANCEL_INAPPLICABLE: endingBasis, evidenceFileIds.

The last two are CHECKER decisions limited to TERM_REVIEW or a solely dependent cancelled/superseded NOTICE_ACK. Do not permit them to cancel REMEDIATION/payment complaints/CLOSEOUT. replacementId must reference a real successor duty on the same Contract; null only for ended applicability. Evidence and independent ending assessment remain historical.

Incident: listIncidents, createIncident, getIncident, commandIncident. No public update/delete. Exact four commands:

- VERIFY: reason, triggerEvidenceFileIds, triggerAt(nullable). Independent CHECKER. For VERIFICATION_CONFIRMED, triggerAt must be null and server verification instant is authoritative. NOTICE_RECEIVED requires triggerAt<=now and exact reviewed receipt/clause proof. VERIFIED when no qualifying remediation; VERIFIED→REMEDIATION may occur in the same transaction when explicit verified trigger creates its linked required duty (B§7 and S032 expected REMEDIATION).
- SUBMIT_RESOLUTION: summary, evidenceFileIds. Assigned MAKER records a new immutable resolution candidate. B§7 explicitly says preserve VERIFIED/REMEDIATION state; upload/submission is not RESOLVED.
- RESOLVE: reason, evidenceFileIds, externalComplaintConfirmation(nullable). Independent CHECKER accepts current candidate only when required linked remediation duties are CLOSED or validly waived, and payment-complaint external confirmation is reviewed. Do not infer provider settlement/refund from app state.
- CLOSE: reason. Independent CHECKER, RESOLVED→CLOSED. Keep original receipt/external deadline and all candidates/assessments.

## Closed schemas and immutable source

Obligation has sourceId, type, basisType/basisText, assignedMakerId, originalDueAt/currentDueAt/dueRevision, breachedAt, evidenceFileIds/evidenceSummary/closureOutcome, contributors, extensionRequest/waiverRequest, supersededById, unresolvedIncidentIds, externalConfirmations and deadlineHistory. It has no arbitrary public closedAt/closedBy/sourceVersion/correctionItems additions. Store those in owned history rows if needed. closureOutcome response enum includes WAIVED plus the four submission outcomes or null.

DeadlineRevision fields: number, dueAt, effectiveAt, decisionBy(nullable), basis, breachedAt(nullable). deadlineHistory max100. ExtensionRequest fields: id, requesterId, contributors, requestedDueAt(nullable for waiver), reason, evidenceFileIds, state PENDING/APPROVED/REJECTED, decisionBy/decidedAt nullable. Public current request slot can be replaced only while all older requests and decisions stay immutable internally. Do not lose rejected or returned history.

Incident has no assignedMakerId or public verification/resolution-assessment timestamp fields. Assignment/scope derives through Contract→Relationship; internal creator/contributor ledger and immutable source record preserve requester identity. receivedAt, receiptEvidenceFileIds, externalReference, externalDueAt, externalRuleBasis, remediationTrigger/durationHours, obligationId and current resolution evidence/confirmation must match closed schema. Freeze original receivedAt/externalDueAt; new evidence or review never resets them.

ReceiptEvidence is only fileIds, basis, observedAt, sourceLabel(EXTERNAL_RECORDED/SIMULATED). Human review of exact external basis is authoritative; do not invent a new public confirmation-kind enum or parse legal conclusions from free text.

## V011 proposal (root allocates; adjust to actual V010 tables)

Reuse cg_lifecycle_consequence(kind,source_id,source_version) for exactly-once derived creation and cg_lifecycle_obligation_breach(obligation_id,due_revision,breached_at,recorded_at) for historical events. Dashboard already queries the latter. Do not create a second competing breach store.

Add narrowly scoped immutable tables as needed:

- cg_obligation_source keyed obligation_id: exact source type/id/version, ContractVersion, source snapshot/hash, triggerAt, original due/calendar/basis. Backfill safe metadata from existing factory/Notice rows without altering their public state or inventing proof.
- cg_obligation_due_revision unique(obligation_id,number): due/basis/effectiveAt/decision/source. Breach is appended separately; project deadlineHistory by joining events rather than rewriting immutable due facts.
- cg_obligation_submission unique(obligation_id,number): submitted source, exact file manifest, outcome/summary/external confirmations, submitter and timestamp.
- cg_obligation_assessment: submission identity, decision/reason/actor/current-source manifest/time. Source assessments are not source contributions; actual evidence uploaders are contributors.
- cg_obligation_exception_request and decision: immutable request IDs, contributor/source manifests, original requested due, manager decision/bilateral proof and timestamp. One pending request per type/duty if needed; never silently replace pending source.
- cg_incident_source plus cg_incident_verification unique incident_id: immutable original receipt and assessed exact trigger/clause/ContractVersion/evidence, authoritative verifiedAt/triggerAt and derived obligation link.
- cg_incident_resolution_candidate plus assessment/events: immutable maker candidate revisions, independent accepted resolution and closure assessment.
- A bounded immutable event/threshold ledger only if needed beyond the existing breach key. Do not duplicate the already sufficient OVERDUE dedupe solely for abstraction.

Append-only triggers/appropriate constraints protect historical evidence. Keep cg_entity JSON projections, current-state optimistic versions and exact canonical response validation. Respect max50 contributors, max20 evidence/confirmations, max100 deadline history/unresolved incident IDs; reject growth transactionally, never truncate IDs/history.

## Due, extension, waiver and scheduler authority

Current overdue is nonterminal AND currentDueAt < asOf, strictly. At equality no overdue or breach. historicalBreaches is a distinct obligation with an immutable breach recorded by asOf, including after closure/cancel/extension. Keep earliest body.breachedAt and one event for every missed operative dueRevision.

Before a late extension/waiver/closure decision, synchronize the old operative due under locks using one authoritative instant. A prospective extension replaces only the future operative deadline and must not later manufacture a breach at superseded original due. A late extension first records old missed due, then appends the new due revision; never clear old breach. Do not overwrite originalDueAt. requestedDueAt must be a genuine extension of current due and remain future at decision; preserve the request even when rejected.

INTERNAL can be waived/extended by independent MANAGER with real reason/basis/evidence. CONTRACTUAL needs reviewed bilateral acceptance proof; manager decision may be its independent assessment, but CLEAN bytes alone are insufficient. STATUTORY_EXTERNAL rejects REQUEST/DECIDE waiver/extension with canonical422 NONWAIVABLE_DUTY; only a sourced corrected external basis can be represented as a distinct corrected record. There is no public arbitrary deadline PUT.

No new public Job kind is permitted: canonical Job kinds are FILE_SCAN/ARTIFACT_PDF/NOTICE_DISPATCH/DATA_DISPOSITION/EFFECTIVITY, parents FILE/CONTRACT/CHANGE/NOTICE/DATA_REQUEST. An internal bounded @Scheduled breach poller can use durable operative-due rows and existing immutable dedupe ledger. Select candidate IDs without retaining leaf locks, then transactionally lock Dossier→Contract→duty; recheck state/dueRevision/currentDueAt, insert breach ON CONFLICT DO NOTHING, update projection/audit only for the actual new event. Multiple processes/restarts safely repeat the scan. No fake successful cg_job or new OBLIGATION job parent. Do not invent reminder/escalation thresholds absent canonical definitions.

## Source factories and cross-module ports

- LifecycleObligations currently creates TERM_REVIEW and CLOSEOUT only. Extend owned factory APIs rather than writing its tables from Incident/Control/Notice modules. TERM_REVIEW source uniqueness is ContractVersion/end, due endExclusive−30 calendar dates at17:00 Vietnam. CLOSEOUT due5 configured demo business days excluding ending date, not a legal deadline.
- Incident verification calls an obligation-owned mandatory-transaction remediation factory with immutable Incident verification source/version, exact ContractVersion/clause/evidence, triggerAt, server verifiedAt, basis and contributors. Due is trigger+48 elapsed hours for the selected demo basis. Do not compute30 working days for payment complaints or accept arbitrary durationHours as authoritative policy.
- Notice owns actual ACK candidate/independent verification. Coordinate a duty port for NOTICE_ACK dispatch creation, exact acknowledgement fulfillment and cancelled-Change inapplicability. Public APPROVE must not create a second ACK verification path that bypasses Notice evidence. Source-state/evidence checks must remain exact if generic duty closure is allowed for an already verified ACK.
- Closeout reads actual incident and duty stores under Contract lock. Reject closure while any linked Incident is not CLOSED or any applicable duty other than this CLOSEOUT is nonterminal. Query authoritative rows; do not trust caller-supplied unresolvedIncidentIds alone. CANCELLED inapplicable duties do not block; historical breaches remain.
- External settlement/fee/complaint confirmations or explicitly reviewed not-applicable basis are evidence, not monetary execution. Service must be STOPPED or independently verified never-started. Existing observed NOT_STARTED alone is not human never-started proof. Control-owned confirmation port should expose verified source; until it exists, allow only genuinely provable branches, no successful placeholder. Later Control confirmation consumes Incident-owned blocking-resume query: any blockingResume Incident not RESOLVED/CLOSED prevents RESUME.

## Authorization, files, locks, transactions

Current users/scopes before idempotency replay and again inside TX. Maker must be current assigned Relationship/duty maker, not just a historically saved ID. Reviewers/manager must be active, scoped, independent of creator/requester/all stable source contributors and all actual evidence uploaders, including ancestor-file uploaders. Human review assessments do not automatically become source contributions.

Lock Dossier→Contract before Incident/duty; for a transaction touching both, choose deterministic Incident→sorted duties consistently. Avoid closing one duty while holding a different duty then trying to lock its Incident. Relationship/Party/file/job order must be reconciled with existing paths; root Contract lock serializes cross-aggregate invariants. Upload and scanner callbacks currently lock ancestry only for CHANGE/CONTRACT: extend EvidenceAccess.lockOwner for INCIDENT/OBLIGATION/NOTICE through their real Contract ancestry, not only controllers.

IncidentCreate requires >=1 receiptEvidenceFileId while parent does not yet exist. Use explicit same-Contract CLEAN staged ancestor evidence, same branch/current scope, then retain exact association on Incident creation. Never accept a sibling Incident/other Contract file. This follows A§11 permitted-parent/ancestor rule; do not invent client-selected UUID or empty-parent endpoint. Source purpose/hash/full-read checks and actual uploader contributions still apply. Similar owned/allowed-ancestor support is needed for obligation external confirmations. Preserve all historical associations and File.attached consistency.

Freeze bytes/source manifests before source submission; use current hash/CLEAN/full authorization at actual decisions, not metadata-only success. Read-only action hints may inspect current metadata/roles/dependencies but must not lock files, write associations or assessments. All mutation effects/history/audit/idempotency remain one transaction; errors roll back, except the existing Change-specific intentional409 exception must not be copied to these commands.

## Queries and reporting integration

listObligations supports type/state/contractId/assignedMakerId/dueBefore/everBreached, branch/cursor/limit. Existing ListQueries does not handle type or everBreached and its dueBefore references dueAt rather than currentDueAt. Implement explicit bound predicates and remove dueBefore from the map handed to the generic dueAt filter, or extend a narrowly typed helper. Do not silently ignore these filters. everBreached queries the shared immutable breach ledger, including terminal duties. Match Dashboard strict-current-overdue and recorded historical formulas exactly.

listIncidents supports state/contractId/kind plus branch/cursor/limit. Both domains scope through current Contract→Relationship before pagination. Ordinary AUDITOR projections redact free-text sensitive summary/basis/reasons/external references consistently with B while keeping closed required fields and useful state/history. Full sensitive file reads still require explicit grant. Keep scoped timelineLag method and one asOf where the API promises a snapshot.

## Actual TODO versus canonical decisions

Implementation TODOs above require no new public contract. B§7 resolves apparent Incident state ambiguity: VERIFY can yield VERIFIED/REMEDIATION, SUBMIT_RESOLUTION preserves state, independent RESOLVE follows. Source table/transaction choices and strict equality are technical decisions already authorized.

Potential product decisions only if source evidence cannot establish them during implementation:

1. NOTICE_RECEIVED is conditional on an actual selected clause/receipt source. Current generated TERMS-VA-1 text contains no such remediation trigger. Do not invent a universal notice-receipt clause or make48h a national rule. A sourced external contractual clause can be human verified and frozen with its evidence/version; otherwise reject that requested trigger with a precise guard and ask owner only if the required product path cannot be represented by canonical source fields.
2. Unsupported allegation must not become fraud/guilt. VERIFY is an independent affirmative source/trigger assessment; there is no outcome enum or RETURN/REJECT Incident command. Null remediationTrigger permits a verified record without a remedial duty. If owner requires changing an already-created nonnull trigger to a formal NOT_PROVEN result, that needs exact canonical change review, not free-text parsing or an invented action.
3. ReceiptEvidence has no typed settlement/fee/never-started classifier. Preserve human-assessed explicit basis and file proof; do not infer verified never-started from text keywords or observed NOT_STARTED alone. If generic evidence plus independent existing Control confirmation cannot satisfy a mandatory branch, present the exact schema gap rather than silently bypassing it.

These are conditional guard/decision checks, not reasons to pause all seven operations now. Continue the implementable source while root controls genuine owner decisions.

## Verification boundary

Source/static/schema review and skipped-test compilation only under current permissions. No automated tests, HTTP smoke, concurrency/recovery scenarios, PDF generation or data mutation without separate authorization. Final report must distinguish new handlers, complete action dependencies, source review, build evidence, runtime startup and actual business acceptance. Continue Control/privacy/backend closure automatically after this tranche under the existing master mission.
