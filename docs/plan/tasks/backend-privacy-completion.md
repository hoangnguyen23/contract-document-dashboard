---
id: backend-privacy-completion
scope: finite DataRequest, safe file disposition and final Job source closure
status: done
depends-on: [backend-control-completion]
---

# Privacy implementation task

Execution resumed20October in D:/contract-document-dashboard-worktrees/be-records-privacy on feature/backend-records-privacy, based on reviewed Control and relocation checkpoint8d131af. Canonical A/B1.3.0 and actual integrated G5 remain authoritative; earlier backend-privacy-analysis.md is supporting analysis. V013 is allocated exclusively to Privacy; preserve V001–V012. Developer implementation completed at e718ccc; separate independent source review1595418 and final107-source skipped-test package passed. Actual startup/V001–V013 evidence is recorded separately. No tests/HTTP/PDF/byte-disposal/business-data execution is authorized; skipped-test build is permitted.

## Deliverable

Implement DataRequest4 operations/all6commands and final DATA_DISPOSITION JobService worker/retry/hints closure. Finite File actions REDACT/PROVIDE_REDACTED_COPY/DELETE_BYTES and current Relationship CORRECT_CURRENT_METADATA only; no generic entity delete/update, schema/roles/FE change or FULL fallback. Immutable approved manifest hash/version/source proof; independent current scoped DATA_OFFICER, stable source/upload contributors and explicit sensitive grant. All selected records authorized independently, global target-record holds/reservation, durable per-record phase recovery and truthful failure/incomplete states.

## Current source facts and real ports

- Control now implements ServiceEndProof via ControlEndProof/cg_control_confirmation, hashes exact source/assessment/contributors and rereads source/ack bytes. Privacy needs no new successful Control callback. A post-disposition source cannot remain usable proof through forged bytes: retain real tombstone/authorized-disposition marker and fail closed at existing evidence consumers. Agreement/operation retention starts from actual accepted CLOSEOUT, not expiry/termination or STOP. Completed G5 ACCEPT_SUBMISSION immutable event supplies closeout acceptance time; don't add public closedAt or guess createdAt.
- Privacy resolver should expose File record identity/version, actual owner/Contract/Relationship ancestry, current class/source anchor, original+redacted keys/hashes and retained references; Relationship exposes exact current contactVersion/history proof. Lock target identity through a shared record reservation authority before any hold/disposition action, reconciling ancestry/Relationship lock order globally for mixed records. File/Customer module-owned port updates should honor that same reservation, not just privacy controllers.
- PrivateBlobStore.delete is orphan cleanup: silently returns if referenced, REQUIRES_NEW/table reference guard and opaque filesystem guards. Add explicit files-owned disposition cleanup outcome/reconciliation, not an afterCommit callback. First reserve+freeze exact keys/source; commit logical tombstone/pointer intent under shared reference guard; physically delete safe unreferenced keys outside outer business TX; persist cleanup result/fencing. Crash after physical delete before acknowledgement must replay exact missing-key success from durable reserved intent, never classify arbitrary pre-existing missing bytes as disposition. Failure keeps JobFAILED/requestDECIDED. Shared cloned blob references outside approved finite scope prevent claiming bytes erased.
- BlobMaintenance counts original/redacted cg_file_blob pointers only. Durable cleanup intents must be coordinated with maintenance: preserve pending key identity/hash and require verification of actual absence/cleanup phase before overall SUCCEEDED, even if guarded maintenance removed an unreferenced committed key first. No orphan daemon can create an apparent successful privacy execution merely by deleting a key. No new public Job kinds.
- SAFE copies are real uploaded CLEAN DATA_REQUEST files, reviewed independent of uploader; freeze bytes/hash/purpose/association. Files-owned method publishes exact redacted pointer/verifiedBy. REDACT is access minimization; PROVIDE_REDACTED_COPY makes safe representation available. Neither silently adds destructive DELETE_BYTES. Null copy means READINESS_BLOCKED, never original fallback.

## Real contact correction without role bypass/version paradox

Current CustomerService.decide is actual CHECKER approval and immediately applies requestedContact, increments Relationship.version/contactVersion and writes cg_customer_contact_history. DATA_OFFICER does not gain CHECKER. DataRequest schema has no contactRequestId.

Representable internal proof path: normal existing MAKER→CHECKER ContactRequest approval occurs before privacy DECIDE; resolver finds exact APPROVED ContactRequest and current cg_customer_contact_history row matching recordId, full requestedContact/current confirmedContact, confirmed_by and source contributors/independence. Freeze current post-approval Relationship version as DispositionRecord.expectedRecordVersion, exact Contact correction and immutable proof metadata. EXECUTE/worker rechecks that exact current version, unchanged purpose and exact approved contact outcome and records its idempotent privacy resolution without reapplying contact or inventing a second approval. Report that contact correction was performed by linked normal contact approval; privacy execution confirms the already approved exact correction, not a new arbitrary write. Need customer-owned exact-proof reader/resolution port, no mutable ContactRequest-only trust or generic updater. This is ordinary idempotent fulfillment from real evidence, not a successful placeholder.

Do not support a pending request by silently accepting expectedRecordVersion+1 after DECIDE: canonical exact record-version guards would be bypassed. If product demands contact mutation occur only inside privacy worker or privacy officer alone approve it, that is a genuine owner/API/SoD decision. Existing canonical before-execution confirmed-contact wording supports the completed-proof path above; no new decision needed for that path.

## Retention and final integration checks

DEMO_ID_COPY anchor must be actual independent sign verification for the selected source, not scan/upload timestamp. DEMO_AGREEMENT3650days/DEMO_OPERATION180days from real accepted CLOSEOUT; DEMO_ID_COPY30days from sign verification. Review eligibility only, not autodelete, arbitrary record-class selection or legal timetable. Global active holds checked regardless caller-supplied holdId; new hold after committed reservation409DISPOSITION_COMMITTED, before reservation409HOLD_ACTIVE. Retry exact manifest/intent only and current grants/source guards; committed irreversible phases never reset on generic retry.

Read-only hints mirror known manifest/copy/source/version/hold guards without locks/writes. Full closed schema/reason nullability and AUDITOR redaction, list scope before pagination, atomic mutation+audit+idempotency+intent. File download/approval/signing/current Control proof retain truthful authorized-disposition behavior and cannot regenerate or return disposed originals. Final review reconciles all85 operations plus each target/action/job variant; expected end85full/0partial/0missing only after real source dependencies implemented/reviewed. No claim runtime/business/E2E acceptance. Independent reviewer and final skipped-test package required; no owner-forbidden automated/HTTP/byte-disposal execution.

## Context and ownership

Read full current canonical A/B1.3.0, all contracts, docs/INDEX.md, tracked analysis/current source/reviews before editing. Own privacy/** and additive V013 exclusively; root manages Git/PR/docs, separate independent reviewer. Source-owned Files/Customer transaction ports and actual guards may change, no canonical/FE edits or unapproved new product behavior. No invented retention hard rule without exact canonical basis.

## Verification

No automated tests/dependencies, HTTP/smoke/manual PDF/byte disposal/business-data mutations or isolated Docker. Source/static/schema and skipped-test compilation only; coherent source commit -> independent review/repair -> final exact skipped-test clean/package. Record source/build/runtime/business separately; no85/0/0 claim before every action/target/job is real and reviewed. Continue whole-backend source/acceptance-matrix/handoff closure under existing mission; no PR merge/main/dev push.

Source task completed20October: final appe718ccc5f7f6ba4d3881d924174440bedfbf7db1, Privacy review1595418 plus separate crossmodule review; 107-source clean/package PASS2026-10-20T10:07:55+07:00; all test phases skipped; Java25.0.1/Maven3.10.0; exact app e718ccc5f7f6ba4d3881d924174440bedfbf7db1. No runtime/business/FE acceptance implied.
