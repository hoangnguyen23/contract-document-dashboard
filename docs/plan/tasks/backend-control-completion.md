---
id: backend-control-completion
scope: real service acknowledgement, CONTROL_REQUEST Round and termination/closeout
status: done
depends-on: [backend-obligation-incident-completion]
---

# Objective

Complete four ControlRequest operations, three commands SUBMIT/CONFIRM/CANCEL, all five kinds ACTIVATE/SUSPEND/RESUME/TERMINATE/STOP, all four existing Round operations with CONTROL_REQUEST adapter and the real ServiceEndProof producer closing the G5 CLOSEOUT dependency. No service success inferred from request/approval or fake bank provider.

# Context

Read full canonical A/B (verify current owner-approved version), all contracts/fixtures especially S031/S036/S057, docs/INDEX.md, actual G5/Notice/Change/Approval/Contract/Files source and reviews, backend-control-analysis/preparation handoffs. Current baseline1.2.2; owner question is pending for effectOn before current materialized ContractVersion.effectiveFrom. Do not implement this ambiguous confirmation rule until owner decision/canonical review; other representable paths continue. User no-tests instruction persists.

# Path

Own control/** and additive V012 exclusively after root allocation. Extend real approval adapters/list filters/snapshot, contract-owned effect/interval transition, Change/Notice-owned inapplicability cleanup, files-owned exact current-version evidence association/ancestry and Obligation ServiceEndProof implementation. Preserve applied/source-owned V001-V011, all immutable history and source boundaries. No FE or unapproved canonical edits. Root Git/PR/docs, separate reviewer.

# Required behavior

- Source create requires exact same-Contract CLEAN staged ancestor evidence and immutable source/manifest/contributor/ContractVersion binding; no invented PUT/withdraw endpoint. Freeze ControlRevision closed shape; internal metadata separate. All source uploaders/requester excluded from independent review; new assessment checks current active scope/roles, historical valid assessment remains immutable.
- ACTIVATE/RESUME L1-only, TERMINATE sequential L1/L2; SUSPEND/STOP REQUESTED with reviewer null. Actual Round VERIFY_EVIDENCE/per-file PASS/APPROVE/RETURN/REJECT/REASSIGN, exact historical snapshots, current hints and source lineage. RETURN parent DRAFT/new immutable round; changes require cancel/new request. Existing live-kind unique index reused.
- Separate CONFIRM event with exact source/receipt/actual observedAt/neverStarted proof; approval does not change service. L1 may later confirm ACTIVATE/RESUME when independent/current eligible. Never infer verified stopped/never-started solely from mutable service state or a boolean.
- ACTIVATE current ACTIVE/READY/NOT_STARTED and acknowledgement>=start; RESUME ACTIVE/READY/SUSPENDED, exact last suspension source and IncidentService.requireResumeAllowed. S031 blocking guard applies create as well as later transitions. SUSPEND urgent requester and independent checker, no formal Round; STOP terminal contract only retains EXPIRED/TERMINATED.
- Before commands run committed ContractLifecycle maintenance before leaf locks/ETag; do not hold parent locks while REQUIRES_NEW advances timeline. Recheck under parent-first Contract serialization. Confirmation after true expiry cannot force termination. Keep reported effectOn, external observedAt and server recordedAt distinct.
- TERMINATE requires actual reviewed ground/appropriate Notice source/stop acknowledgement or independently verified neverStarted. Do not impose tariff ACK/+7 on all termination notices (S036). One transaction contract-owned TERMINATED/STOPPED/version closure/effect, cleanup other pending Control/Rounds and unapplied inapplicable Change/Notice/jobs/slot, preserve incidents/applicable duties/versions/breaches, create deduped CLOSEOUT. Pending retroactive boundary needs owner decision as above.
- Current ContractVersion artifact may be CONTRACT, CHANGE or NOTICE; source-specific exact retained association/proof, never original fallback or silent HistoricalContractFiles.retain no-op. CONTROL_REQUEST file upload/scanner lock ancestry must include Contract/Dossier.
- Real immutable Control confirmation ledger and source/evidence binding implement ServiceEndProof.verifiedEnded used by Obligation CLOSEOUT. Verify exact historical assessor/contributor/bytes/manifest/timestamps and stop-vs-neverStarted; source absence fails closed. This closes commandObligation partial only after actual provider is reviewed.
- CANCEL requester/current maker before confirmation with reason; APPROVED/SYSTEM independent checker plus actual evidence; SYSTEM STOP cancellation requires verified inapplicability. Preserve approval/source/observed service. Serialize duplicate STOP/current live request with expiry instead of letting unique collision roll back true expiry.
- All four Round read/list/command/snapshot routes accept CONTROL_REQUEST including list SQL filters. Current role/branch/Relationship assignment before pagination/replay; redaction/schema/action hints/date/nullability exact; read hints do not mutate evidence.

# Verification

No automated tests/dependencies, HTTP/smoke/manual PDF/business-data operations or isolated Docker. Static/source/schema and skipped-test compilation only. Commit coherent source, independent review/repair then final skipped-test clean/package. No PR merge/main/dev push. Report truthful partial paths/owner decisions and actual FE impact. Continue Privacy and final85-source closure automatically after GO.
Owner approved boundary guard19October; A/B1.3.0 candidate96c198e/internal reviewc5120f5 merged1ecf6c3. Earlier pending-decision wording is historical. Bounded source task completed8f40974/reviewc96b8ec with100-source skipped-test package PASS; next Privacy/source closure continues automatically. No runtime acceptance.
