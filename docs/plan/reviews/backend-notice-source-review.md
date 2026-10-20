# Notice and tariff integration — independent source review

Verdict: bounded source GO at `5142b83e2cbab4b191b616115b11f3f08d3ba8f3`, branch `feature/backend-notices-renewals`, reviewed against canonical A/B 1.2.2. This is source/build evidence, not runtime, business, FE integration or production acceptance.

## Scope

Reviewed the coherent candidate d6e42cb and repairs 09d7586, 231b524, da1adc5, cadbe48 and 5142b83 independently of implementation. Six operations now have source implementation: listNotices, createNotice, getNotice, updateNotice, getNoticeCapture, commandNotice. All eleven canonical commands were traced: SUBMIT, APPROVE, RETURN, REJECT, DISPATCH, RETRY, RECORD_EXTERNAL_DISPATCH, RECORD_RECEIPT, RECORD_ACK, VERIFY_ACK, CANCEL.

Traced Notice to current Contract/Relationship scope and assignment, source/recipient/contact version, immutable review and PDF binding, private scanned generated File, durable local capture, dispatch retry, manual receipt, immutable ACK candidate/assessment, NOTICE_ACK, Change readiness/COMMIT/application and cleanup. Existing ARTIFACT_PDF remains the sole PDF claimant. Notice writes use Change-owned integration hooks rather than modifying Change-owned persistence directly.

## Findings closed before GO

- Valid independent ACK could roll back when a separate Change-only evidence/readiness guard failed. Promotion now retains the ACK/NOTICE_ACK closure and records the readiness blocker; transaction infrastructure failures still roll back atomically.
- Cancelling/rejecting a tariff Notice left Change.noticeId blocking every replacement. Exact Change-owned detachment permits a new independently submitted/reviewed source and preserves the old Notice/history.
- Source lacked pre-submit attachment evidence. SUBMIT now freezes deterministic existing uploaded Notice attachments with CLEAN/private byte hashes and historical associations; PDF/source and subsequent approval/dispatch/retry checks retain that binding. Later receipt/ACK evidence remains separate.
- Action hints and dispatch retry omitted current source/dependency/intent guards. Relevant hints now check source/dependency/artifact/candidate without attaching evidence; retry/worker check exact persisted payload, source, messageId and artifact binding.
- Receipt history and local capture lacked sufficient evidence/hash checks. Immutable receipt events bind file manifests; readiness checks source hash and historical independence. Capture verifies content_hash and exact frozen payload.
- Derived NOTICE_ACK used recording time as actual source event time. Deadline history now uses actual dispatchAt; created/recorded timestamps remain authoritative server time.
- Readiness retroactively invalidated a valid ACK assessment when its assessor later lost role/scope or became a later contributor. New assessments retain current CHECKER authorization/SoD; historical reuse verifies immutable contributor snapshot, assessor, candidate, assessment hash, decision, reason and exact timestamp without reauthorizing the historical actor.

No remaining concrete P1/P2 finding was identified within this bounded source pass. This does not replace execution of the prohibited acceptance scenarios.

## Invariants inspected

Current mutation authorization is repeated inside commands; ETag/idempotency/schema response validation and domain/audit/job intent share the transaction. Lock ancestry precedes Notice and leaf jobs/files. Notice source, artifact, event and ACK input/assessment tables are protected from update/delete. V010 is additive and reserved to this tranche; V001–V009 are unchanged.

Dispatch completes a fenced durable job, stable UUID messageId capture and DISPATCHED state atomically; no delivered/deemed/ACK inference occurs. Exhausted jobs including expired leases reconcile Notice to FAILED. Retry preserves exact intent/messageId. Cancellation and worker acceptance serialize through the same source locks. Manual dispatch uses actual nonfuture event time and separately assessed receipt. ACK recording remains DISPATCHED; independent verification is candidate-bound. Source bytes, recipient hash and evidence manifests remain checked.

Tariff readiness binds approved Change revision/base/fee-only policy evidence, actual Notice artifact, dispatch provenance and independently accepted ACK. Lead is max(localDate(dispatchAt)+7 calendar dates, localDate(acknowledgedAt)+1). READY does not schedule automatically. COMMIT and timeline retain current guard/base/schedule/slot checks and tariff ContractVersion uses the Notice artifact. Cleanup preserves dispatched/acknowledged superseded history and prior obligation breach history; due equality does not create a breach.

## Static and build evidence

- A/B remain 1.2.2; A file SHA matches manifest; all manifest block digests match extracted contracts and runtime resources. No canonical/API/schema/enum or FE source changes.
- CRLF-aware git diff --check passed; existing V001–V009 show no diff.
- Independent final command in apps/api: `mvnw.cmd -Dmaven.test.skip=true -V clean package`.
- Final source HEAD stayed 5142b83e2cbab4b191b616115b11f3f08d3ba8f3, clean before the review document. BUILD SUCCESS, 88 Java sources, finished 2026-10-19 00:02:19 +07:00; test resources not copied, test sources not compiled, tests skipped.
- Maven 3.10.0, Java 25.0.1, Spring Boot 4.1.1. Requested Java patch 25.0.4.1 remains unavailable locally. Nonfatal repository metadata 401 warnings and existing deprecated API notice remain.
- Final packaged JAR SHA-256: `ac9012ce8908bbeb743c31552c6728f540106d2180ac85d2f35e920d7c481a42`.
- An earlier package of da1adc5 also passed, but was superseded by historical-assessment repairs and is not the final artifact evidence.

## Coverage, FE and acceptance limits

Semantic source delta is six missing Notice operations plus commandChange partial to implemented: aggregate source coverage 65 implemented / 5 partial / 15 missing out of 85. Four Round operations remain partial for CONTROL_REQUEST; commandJob remains partial for DATA_DISPOSITION. Public Obligation/Incident, ControlRequest and Privacy remain later work, so the complete Backend mission is unfinished.

FE consumes existing Notice, NoticeCapture, AckCandidate, File and Job shapes and existing action hints. No breaking canonical change or additional owner decision was introduced; S024 MAKER VERIFY_ACK hint remains absent. External FE acknowledgement/integration is not claimed.

No automated tests were authored or run; no HTTP/smoke, business-data mutation, manual PDF rendering, Docker startup or migration execution was performed by this review. V010 execution, workflow/PDF/scan/download, retry/race/recovery, manual receipt/ACK, tariff boundaries, meaningful data/NFR and FE–BE acceptance remain unverified. Build success does not establish these outcomes.
