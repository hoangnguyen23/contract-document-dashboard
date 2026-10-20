# Privacy independent source review

**Bounded semantic source GO at `e718ccc5f7f6ba4d3881d924174440bedfbf7db1`.** This includes Privacy implementation d736824, line-ending preservation 8977c32, cross-module repair 1ae46a3 and NFC source repair e718ccc5. Final exact clean/package passed with all test phases skipped. This verdict concerns source implementation, not executed business correctness or Frontend acceptance.

## Actual reviewed workflows

The four DataRequest operations have real implementations and all six command branches were traced: START_REVIEW, REJECT, DECIDE, EXECUTE, PLACE_HOLD and RELEASE_HOLD. ALLOW/PARTIAL binds an exact nonempty finite manifest; ALLOW covers every requested record. DEFER/REFUSE cannot execute. Unsupported record/action/type combinations fail closed. commandJob has a real DATA_DISPOSITION worker/retry path.

- Current directory roles/scopes are reloaded, every selected File/Relationship is authorized and officers are excluded from requester and stable actual selected-source/copy contributors. Read hints do not reserve intent or take record locks. DataRequest/File/Job SQL predicates authorize every selected-record ancestor before pagination. AUDITOR redaction preserves the closed response shape. A global hold is omitted entirely if any member is outside reader scope.
- Canonical manifest hashing NFC-normalizes JSON, omits only the top-level hash and retains array order. Duplicates are rejected and versions use exact integer conversion without truncation/overflow. Immutable decision/intent proof binds exact target/replacement versions and private bytes/hash. At e718ccc5 the command is normalized once before authorization/fingerprinting/guards/storage, preventing an NFD live manifest from differing from its NFC immutable event.
- REDACT/PROVIDE_REDACTED_COPY require an independently reviewed actual CLEAN upload on the exact DATA_REQUEST. Files publishes its verified redacted pointer and the authenticated content endpoint returns the real bytes without FULL fallback. REDACT marks the original sensitive so FULL needs explicit SENSITIVE_READ; it does not silently erase originals.
- CORRECT_CURRENT_METADATA resolves an actual approved normal ContactRequest, its independent CHECKER and current immutable contact history. It freezes the actual post-approval Relationship version and unchanged purpose. Execution confirms that already applied correction without reapplying contact, granting CHECKER to DATA_OFFICER or rewriting Party/agreement/recipient history.
- Retention derives DEMO_ID_COPY 30 days from an actual selected-source original/Change signing assessment; DEMO_AGREEMENT 3650 days and DEMO_OPERATION 180 days from immutable ACCEPT_SUBMISSION of a real CLOSED CLOSEOUT. Missing anchor is not eligibility. This internal synthetic review flag does not autoerase or establish national statutory retention; explicit reviewed basis/manifest remains required.
- Global active hold membership blocks reservation with HOLD_ACTIVE; committed reservation blocks later hold with DISPOSITION_COMMITTED even after cleanup failure. Release excludes placer and requester/source contributors. Immutable events preserve placement/release evidence. Hidden holds still enforce globally.
- DELETE_BYTES reserves exact keys/hash, then commits a Files-owned DISPOSED limited tombstone/opaque pointer before physical cleanup. Cleanup requires exact committed LOGICAL authorization, no retained references, matching existing bytes/hash and verified absence. Already absent bytes reconcile only against that committed intent. Shared retained owners block erasure rather than being damaged.
- One bounded worker uses an independent 20-second heartbeat and shared 60-second lease/requeue/backoff. Read-only ownership/attempt/expiry checks avoid holding the Job row during slow proof work; final renewal fences logical commit and cleanup acknowledgement. Physical IO occurs outside the domain transaction. Every record must reach DONE before fenced SUCCEEDED/EXECUTED; failure remains DECIDED/incomplete. Audited manual retry binds the actual current retry executor while preserving original execution history and exact immutable manifest. Current grants are checked before new physical effects; revocation does not discard irreversible reservation.

## Concurrency and storage

A shared transaction advisory guard precedes selected financial ancestors, records, request and replacement File locks in both Privacy and DATA_REQUEST evidence upload/scan/retry. This deliberately serializes the bounded Privacy workflow and resolves request-to-File versus File-to-request inversion, including older DataRequest source Files. Normal financial operations do not acquire that guard after taking a source File. Financial ancestry remains Dossier before Contract before leaf records.

Reserved-key SQL guards prevent new clone/blob/association references under common PostgreSQL key serialization. Different File IDs sharing a key are checked as actual owners; ambiguous shared erasure is refused. Filesystem guards are nonblocking and database reference-table locks bounded. Explicit cleanup runs outside any enclosing business transaction holding blob rows. Orphan cleanup and BlobMaintenance exclude disposition-owned keys. No persistent filesystem/PostgreSQL wait cycle was found in these reviewed paths; races/crashes/faults have not been executed.

## Repaired findings

1. P1: initial requester-only SoD omitted actual source/copy contributors; dynamic plus stable retained contributors now enforce independence.
2. P1: the old branch-level File/Job SQL OR shortcut bypassed DataRequest selected-record scope; all DataRequest ancestry now uses the exclusive helper.
3. P1: DataRequest evidence/request and selected-File/copy lock ordering inverted; shared serialization now precedes both paths.
4. P1: physical cleanup after LOGICAL lacked current sensitive grants; current-grant checks now precede new effects and execution/retry guards.
5. P2: Privacy retry lacked canonical audit and enqueue used a random correlation; actual retry audit and originating enqueue trace are present.
6. P2: inline scheduled processing lacked bounded work/20-second renewal; separate bounded executor/heartbeat now cover attempts.
7. P1: early Job renewal blocked its own heartbeat during the logical transaction; read-only fencing plus final renewal now protect commit.
8. P1: new-officer retry retained a revoked original executor; durable current retry binding now preserves original history while enabling real recovery.

Two additional cross-module P2 repairs were independently checked at 1ae46a3: Notice artifact retry now audits the actual Job RETRY/reason in the same transaction, and ReportingScope includes an exclusive all-selected-record DataRequest ancestry guard plus the MAKER assigned-record path before audit pagination/export. These bounded closures do not replace the separate cross-module review.

A final suspected CHECKER/MANAGER-only DataRequest File/Job list leak was explicitly retracted: exact `git show e718ccc5:.../EvidenceAccess.java` already contains the required MAKER/DATA_OFFICER/AUDITOR role gate around the recursive fallback, and the old shortcut excludes every DataRequest ancestor. The initial suspicion came from omitting the final return line while reading an excerpt. No additional code change or build was necessary.

## Canonical and operation reconciliation

Static inspection found 85 unique canonical operationIds. The four DataRequest workflows and final DATA_DISPOSITION commandJob variant have real effects; the separate integrated inventory review may now classify source coverage 85/0/0. No claim is made that85 operations were exercised.

All six fenced JSON blocks parse and equal extracted contract objects; all six extracted/resource copies are byte-identical. All five declared block digests match the manifest. A SHA-256 is 0dc6885d1b7faf65c6d5adf49389baa37474a2036735ed73e5710c47057e43a1. A/B versions are 1.3.0, all 63 scenarios remain, and S024/S060 expected.body.allowedActions are exactly []. No canonical API/schema was changed.

Detail/list Job inspection confirms all five kinds have real current parent authorization, source-aware retry and hints: FILE_SCAN; ARTIFACT_PDF across CONTRACT/CHANGE/NOTICE; EFFECTIVITY; NOTICE_DISPATCH; DATA_DISPOSITION. CustomerService.reviewerOptions supports DOSSIER, CHANGE and CONTROL_REQUEST with actual target/Contract/Relationship scope, stable contributors/requester exclusions and current active scoped candidate roles. Old inventory missingTargets/late-aggregate limitations can be removed. No unsupported canonical Job or reviewer target remains in these inspected paths.

## Build and acceptance boundaries

At 8977c32 the developer's exact clean/package passed 20October 10:06:35+07, 107 Java sources, all test resources/compilation/execution skipped. JAR SHA-256: 2a94c41656f49dad16fd688f62ce4c40122f7fcd6bfd9498b34ca1b4ed691e62. The Unicode repair e718ccc5 clean/package passed 20October 10:07:55+07 with all test phases skipped; JAR SHA-256 fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602. No application edits followed this exact final package. No repeated unchanged build was run by the reviewer.

No automated tests, HTTP/workflow scenarios, PDF rendering, runtime startup, actual byte disposition or business-data mutations were performed by this review. V013 was not applied during source review. Local JDK 25.0.1 does not establish the canonical exact 25.0.4.1 compatibility.

FE impact is nonbreaking implementation of four DataRequest operations, DATA_DISPOSITION Job detail/list/retry hints, actual verified REDACTED/visible DISPOSED File behavior and existing ContactRequest proof integration. No FE files were edited. External FE acknowledgement, browser/consumer execution, HTTP authorization/state flows, signing/PDF/scan/download, hold races, lease/crash recovery, byte cleanup, coherent backup/restore and NFR/production acceptance remain pending owner-authorized verification.
