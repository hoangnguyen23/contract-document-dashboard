# Obligations and Incidents — independent source review

Verdict: bounded G5 source GO at `992946dd3a81c8f64594e03fbabaa8b7e0755ee8`, branch `feature/backend-obligations-incidents`, canonical A/B 1.2.2. This verdict excludes unresolved CLOSEOUT service-end proof and is not runtime/business/integrated acceptance.

## Scope and explicit partial boundary

Reviewed candidate 8926f45 and repairs 978d3e7, 520c395, ffb0c32, 2ad7873, 9a18e71 and 992946d independently of the developer. Seven operations have source handlers: listObligations/getObligation/commandObligation and listIncidents/createIncident/getIncident/commandIncident. Traced all nine duty commands and all four Incident commands, source factories, immutable evidence/event records, due revisions, shared breach synchronization, polling, Notice hooks, ancestry/scope, filters, hints and projections.

Six formerly missing operations are implemented within this tranche. commandObligation remains PARTIAL: CLOSEOUT APPROVE requires a real Control-owned ServiceEndProof provider, which is absent and explicitly fails closed. STOPPED/NOT_STARTED JSON is not accepted as substitute proof. Aggregate semantic source coverage is therefore 71 implemented / 6 partial / 8 missing of 85. Four Round operations still lack CONTROL_REQUEST, and commandJob still lacks DATA_DISPOSITION. Do not label all duty type variants or the full backend complete.

## Concrete findings closed before GO

- Multiple contributor additions within Incident RESOLVE could discard earlier staged uploaders. Contributor accumulation now unions persisted, already staged and new contributor IDs before the max50 guard. Ending-source uploader history is also retained.
- Current duty assignment projection/filter used historical assignment. Both now derive the current Contract Relationship assignment, consistent with mutation authorization.
- Initial deadline history universally claimed the demo calendar, including 48 elapsed-hour remediation. Public and immutable due rows now retain the same source-specific basis.
- VERIFICATION_CONFIRMED used separate trigger and verification instants. One authoritative instant after source/trigger private-byte and SoD checks now supplies both the verification evidence and 48-hour trigger; NOTICE_RECEIVED keeps its sourced actual trigger.
- Known source/dependency failures were omitted from action hints. Duty acceptance and pending decisions now inspect frozen manifests; Incident VERIFY/RESOLVE/CLOSE inspect the applicable source, verification, candidate, accepted and external manifests without attaching/locking files. Read-only linked duty waiver checks now match the mutation guard.
- Breach synchronization only before slow private evidence checks could miss an operative deadline crossed before a late extension/closure. Completion now synchronizes the still-operative persisted due at the authoritative decision instant, refreshes Entity version and merges historical breach fields into the staged projection before changing due/state. An extension inserts the new due only after this synchronization and verifies the requested due remains future. Exact due equality remains nonbreached.
- Commands containing summary but no reason wrote empty AuditEvent.reason, violating the canonical nonempty-or-null shape. Missing reason is now null.

No remaining concrete P1/P2 finding was identified in the bounded implemented source after those repairs. The Control service-end dependency remains an explicit feature limitation rather than a successful placeholder.

## Invariants inspected

Current active role, branch and Contract Relationship assignment apply before replay and again under mutation ancestry locks. Independent reviewers/managers exclude stable source contributors and actual file uploaders, including allowed ancestor evidence. Dossier/Contract locks precede Incident/duty and File ancestry; Incident resolution checks linked duty under the same Contract serialization. Creator/request/receipt/external due source remains immutable. Exact private bytes, CLEAN state, parent/ancestor scope and historical associations are checked at source submission and review; File.attached remains consistent.

Immutable cg_g5_event source/request/candidate/assessment rows retain hashes and earlier versions. Immutable cg_obligation_due retains operative revisions; existing cg_lifecycle_obligation_breach is reused with one deduplicated event per missed dueRevision. No competing ledger/public job kind or arbitrary obligation creation endpoint was added. Polling rechecks persisted operative due/state under ancestry locks and safely retries after restart. Original due and earliest breach are retained; prospective extension does not manufacture an original-due breach; late extension/waiver/closure retains missed history.

Exception requests do not change operative due. Current independent manager decisions retain exact source/evidence and bilateral proof where CONTRACTUAL; STATUTORY_EXTERNAL is nonwaivable. Supersession/inapplicability is restricted to eligible TERM_REVIEW/sole Notice duties with evidence and real same-Contract successor where provided. Remediation/complaints/closeout cannot be cancelled through that bypass.

Incident create uses authorized same-Contract ancestor receipt evidence before a public parent exists. Verification freezes the original source/ContractVersion and exact assessed trigger evidence. NOTICE_RECEIVED is an affirmative independent human assessment of sourced contractual clause/receipt proof with nonfuture actual trigger, not keyword inference, a universal template clause or a statutory deadline. VERIFICATION_CONFIRMED uses server verification time and requires null caller triggerAt. Source uniqueness produces one remediation duty with 48 elapsed hours. SUBMIT_RESOLUTION retains VERIFIED/REMEDIATION; independent resolution/closure checks exact current candidate/history, valid closed/waived remediation and complaint confirmation. No payment/refund or fraud finding is inferred.

Notice direct verified ACK closure still receives the pre-update Notice/candidate in its owned transaction; generic duty acceptance cannot replace Notice VERIFY_ACK. Shared breach synchronization is mandatory-transaction and preserves strict cutoff/history. Real unresolved Incident and duty stores guard closeout; absent Control proof blocks service-end acceptance. Incident-owned requireResumeAllowed reads actual unresolved blocking incidents for the next Control tranche.

Idempotency source review: successful DECIDE_EXTENSION/WAIVER replay still performs current scope/role/SoD authorization, while current request ID/state/terminal/deadline checks occur only in the cache-miss callback. Replacing the current request slot does not reapply a cached decision or add duplicate history/breach. No scenario execution was performed.

## Static and build evidence

- Canonical A/B stay 1.2.2. A SHA matches manifest; frozen contract block hashes and runtime resource copies match. No API/schema/enum or FE source change.
- CRLF-aware git diff --check passed. V001–V010 are unchanged; V011 is this tranche's additive migration and has not been executed by this review.
- Final independent command in apps/api: `mvnw.cmd -Dmaven.test.skip=true -V clean package`.
- Exact HEAD remained 992946dd3a81c8f64594e03fbabaa8b7e0755ee8, clean before this review document. BUILD SUCCESS, 95 Java sources; finished 2026-10-19 00:18:26 +07:00. Test resources were not copied, test sources not compiled and tests skipped.
- Maven 3.10.0 / Java 25.0.1 / Spring Boot 4.1.1. Requested Java patch 25.0.4.1 remains unavailable locally. Nonfatal repository metadata401 warnings and existing deprecated-API notice remain.
- Packaged JAR SHA-256: `87c6db685098ac1acd4c7522ccf7d76907520968a06560358f9232b5675fab5a`.

## FE and acceptance limits

Existing Obligation, Incident, ExtensionRequest, DeadlineRevision and ReceiptEvidence shapes/actions remain canonical. FE gains actual list/detail/command behavior, current assignment and action hints, immutable exception/deadline projection and separate resolution submission/acceptance. CLOSEOUT acceptance stays blocked until Control provides real proof. No breaking change or additional product decision was introduced; external FE acknowledgement/integration is unclaimed.

No automated tests were written or run, no HTTP/smoke/business-data mutations, manual PDF generation, Docker startup or migration execution occurred in this review. Deadline boundary, race/recovery/idempotency, meaningful workflow evidence, V011 migration, Control proof integration and FE–BE acceptance remain unverified at runtime. Source/build success does not establish those outcomes. Continue Control, Privacy and complete-backend review; do not declare G5 integrated acceptance or all85 closure.
