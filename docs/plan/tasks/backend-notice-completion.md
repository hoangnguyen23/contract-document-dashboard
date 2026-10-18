---
id: backend-notice-completion
scope: real Notice dispatch, independent receipt/ACK and tariff integration
status: ready
depends-on: [backend-change-completion]
---

# Objective

Complete all six canonical Notice operations and eleven actions, real immutable source/PDF/local-capture dispatch, independent receipt and ACK assessments, NOTICE_ACK obligations and the remaining TARIFF_NOTICE readiness/application/cleanup dependencies. This is the next dependency of the whole Backend mission, not final closure.

# Context

Read docs/INDEX.md, full canonical A/B1.2.2 (especially Notice, Change, date and SoD rules), all six contracts, completion-execution.md, backend-execution-inventory.json, change-implementation.md and source reviews. Owner-approved S060/S024 only change maker action hints; do not change schema, roles or API. Canonical wins over plans. Read current Java modules and migrations before edits.

# Path and ownership

Own notice/**, additive V010 exclusively, and necessary real integration ports/adapters in change, files, jobs, contract LifecycleObligations and list/read projections. Preserve V001-V009 and existing workflow/history. Do not write Change-owned tables directly from Notice: add Change-owned source/attach/promote/readiness/cleanup operations. Root owns Git/PR/publication and coverage checkpoint; separate reviewer owns independent verification. No Frontend changes.

# Required integration

- Implement listNotices/createNotice/getNotice/updateNotice/getNoticeCapture/commandNotice with exact canonical shapes, current branch/assignment/sensitivity, ETag/idempotency/audit and hints. Create fields and update whitelist must match contracts; do not invent reviewerId or endpoints.
- All actions: SUBMIT, APPROVE, RETURN, REJECT, DISPATCH, RETRY, RECORD_EXTERNAL_DISPATCH, RECORD_RECEIPT, RECORD_ACK, VERIFY_ACK, CANCEL. SUBMIT binds expectedContactVersion to Relationship.contactVersion (not version), rejecting stale values with BASE_CONFLICT. Freeze confirmed current Relationship contact/recipient and source hashes; immutable review/source/artifact/receipt/ACK history. Independent current noncontributor checker, not same MAKER recorder.
- Extend files-owned explicit NOTICE artifact registration and existing sole ARTIFACT_PDF claimant. Real PDF bytes, scan and private download; no ready=true stub. Stable UUID messageId local DB capture atomic with dispatch state. Capture proves DISPATCHED only, never receipt/deemed/ACK.
- Real NOTICE_DISPATCH worker, bounded attempts/lease/fencing/obsolete source and authorized retry, using JobService source projection. Lock Contract -> Change -> Notice -> Job consistently with existing paths. No external real email service.
- Manual evidence timestamps <= authoritative now, canonical nullable deliveredAt/deemedReceivedAt and ReceiptEvidence; no fabricated receipt. RECORD_ACK creates exact immutable candidate while DISPATCHED; VERIFY_ACK ACCEPT/RETURN independent candidate-bound assessment.
- Valid ACK remains recorded/NOTICE_ACK accepted even if tariff lead dates too short. Earliest tariff effective date is max(localDate(dispatchAt)+7 calendar dates, localDate(ackAt)+1), not 168h. No rollback of valid ACK merely to force READY.
- TARIFF_NOTICE must bind exact approved Change revision/base/fee-only source, actual Notice artifact, recipient, canonical transport provenance (DB local capture is authoritative dispatch; external manual dispatch requires independent receipt assessment), independent ACK and date guards. Current material, policy, scope, schedule and source required at readiness/commit/application; no premature activation or automatic scheduling.
- Change rebase/cancel/supersession preserves dispatched/ack history; unissued notice cleanup and solely inapplicable obligation cancellation preserve prior breach. Add LifecycleObligations-owned NOTICE_ACK create/accept/supersede operations, retaining cg_lifecycle_obligation_breach history for public G5 completion. Preserve strict due < asOf breach semantics at exact equality.
- Review existing Change/Round/Signing/Job/Contract reads and hints after connecting dependencies. No default no-op integration or false success. Source-specific timeline artifact binding must use tariff Notice evidence correctly.

# Verification

No automated tests/test dependencies, HTTP scenarios, smoke, manual PDF generation, business-data mutation or isolated Docker. Permitted static/source/schema/hash review and clean/package with all test phases skipped. Developer commits coherent source, independent reviewer repairs/GO before final package. Record exact build/source evidence separately from runtime/business acceptance. No canonical change without owner approval. Root continues Obligation/Incident, Control and Privacy automatically after GO.