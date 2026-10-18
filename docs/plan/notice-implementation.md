# Notice implementation — source checkpoint

Status: implementation candidate under independent review; not runtime or business acceptance. Branch `feature/backend-notices-renewals`; source candidate `231b524` (18 October 2026). Canonical A/B 1.2.2 unchanged.

## API and ownership

`notice/NoticeController` and `NoticeService` implement `listNotices`, `createNotice`, `getNotice`, `updateNotice`, `getNoticeCapture`, `commandNotice`. Commands cover SUBMIT, APPROVE, RETURN, REJECT, DISPATCH, RETRY, RECORD_EXTERNAL_DISPATCH, RECORD_RECEIPT, RECORD_ACK, VERIFY_ACK, CANCEL. Mutable update fields remain subject/bodyText only. ETag, idempotency, schema validation, current parent branch/relationship scope, independent CHECKER and contributor exclusions use existing shared authorities.

Notice source is server-owned; SUBMIT compares expectedContactVersion with current confirmed Relationship.contactVersion. It freezes text, contact, recipient hash, deterministic pre-submit uploaded attachments (IDs/purposes/byte hashes), renderer manifest and exact approved Change revision where relevant. Later receipt/ACK uploads are separate evidence and do not rewrite the source. RETURN preserves the prior source and review events; resubmission creates a new immutable source. CANCEL/REJECT detach only the matching active Change-owned Notice association so a replacement may be independently reviewed.

## Historical evidence and transactions

V010 exclusively adds immutable Notice source, PDF binding, review/event, ACK input and ACK assessment tables; it also protects existing local capture history against update/delete. V001–V009 are unchanged. Receipt events bind evidence manifests; ACK input binds immutable candidate, file manifest and exact artifact/recipient hashes. Private bytes and metadata are checked again at independent assessment and readiness. Source attachments become historical file associations and cannot be staging-deleted.

Mutation lock order is existing Dossier/Contract, optional Change, Notice, then leaf File/Job/Obligation as required. Source changes, audit, idempotency and durable job intent share the command transaction. Files own artifact registration/storage metadata. Change owns attachment/detachment, source/current guard, promotion, slot and effectivity writes; Notice does not write Change tables directly.

Valid independent ACK closes NOTICE_ACK and remains recorded even if a later Change-only business/evidence guard blocks tariff readiness; the blocker is retained and Change stays fail-closed. SQL transaction failures still roll back atomically. Breach synchronization uses strict due < now and retains immutable breach/deadline history before ACK closure or supersession.

## Durable jobs and actual artifacts

The existing sole ARTIFACT_PDF claimant delegates NOTICE to `NoticeJobs`; no competing PDF claimant is introduced. Frozen source renders real PDF bytes with existing pinned renderer assets, scan happens outside the reference transaction, and files-owned registration creates CLEAN private generated NOTICE_EVIDENCE metadata only after scan success. Registration/source/result binding and fenced queue success commit together. Failed artifact retry requires exact current IN_REVIEW source/job/hash and no artifact.

NOTICE_DISPATCH is a separate durable local adapter. Its transaction checks current Notice/dependency/source/artifact and persisted intent, fences the lease, then inserts one stable UUID messageId capture and commits DISPATCHED together. No delivery, deemed receipt or ACK is inferred. Shared queue reclaim/backoff/5-attempt handling is retained; reconciliation projects exhausted jobs to Notice FAILED even after lease expiry, and RETRY reuses exact payload/messageId after current guards. Manual external dispatch records source evidence and actual nonfuture event time; independent receipt remains separate from dispatch recording.

`JobService` supports NOTICE_DISPATCH retries and NOTICE ARTIFACT_PDF through existing operations and hints. No new endpoint, canonical field or enum is added. Explicit current source/candidate/dependency guards are used in Notice hints without attaching/writing evidence merely to project actions.

## Tariff and remaining backend dependencies

The Notice bridge checks exact approved tariff Change revision, base, current material/readiness, immutable approval/evidence/policy, actual scanned Notice artifact, dispatch provenance and independent ACK. Local capture is authoritative app dispatch; external recording requires independently assessed receipt. Earliest effective date is max(localDate(dispatchAt)+7 calendar dates, localDate(acknowledgedAt)+1). READY does not schedule automatically; existing explicit COMMIT and future-slot transaction still control activation. Timeline uses the tariff Notice artifact in ContractVersion and rechecks readiness before application.

Change cleanup cancels unissued Notice jobs/sources, preserves dispatched/ACK history as superseded, and cancels solely inapplicable NOTICE_ACK without erasing prior breaches. Other public Obligation/Incident, ControlRequest and Privacy modules remain separate later tranches. This candidate does not claim all 85 operations complete.

## FE impact and evidence limits

All six Notice operation IDs and existing Notice/NoticeCapture/AckCandidate schemas retain canonical shape. Frontend gains actual responses, jobs/private-artifact progression, transport-specific action hints and independent ACK behavior; canonical owner-approved S024 MAKER hints remain []. No breaking schema change, no FE edit, no new product decision. External FE acknowledgement/integration has not been performed here.

Developer compile of 88 Java sources passed with `-Dmaven.test.skip=true` before the final small source repairs; it is historical compile evidence only. Independent exact-HEAD source review and final skipped-test clean/package remain pending at this checkpoint. No automated tests, HTTP scenarios, manual PDF rendering, business-data mutation, Docker start or migration execution occurred in this worktree. Runtime migration, dispatch/retry/races, PDF/scan/download, tariff/ACK workflow and FE–BE acceptance are unverified and require the separately authorized acceptance scope.
