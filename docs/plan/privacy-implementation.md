# Privacy/DataRequest implementation

Canonical A/B 1.3.0. Source implementation handoff, not runtime or business acceptance. Owner excluded automated tests, HTTP workflows, PDF rendering and actual byte disposition. V013 is additive and has not been applied by this implementation task.

## Operations and finite scope

listDataRequests, createDataRequest, getDataRequest and commandDataRequest implement ERASURE/ACCESS/CORRECTION with START_REVIEW, REJECT, DECIDE, EXECUTE, PLACE_HOLD and RELEASE_HOLD. The current independently scoped DATA_OFFICER is excluded from requester and stable actual selected-source/copy upload contributors. Creation and later evidence uploads retain those contributor IDs. Every selected record is authorized independently; DataRequest/associated File and Job list predicates enforce the complete ancestry before pagination. Global hold projections suppress a whole hold when any member is outside the current reader scope; enforcement never ignores hidden active holds.

ALLOW/PARTIAL freezes canonical NFC manifest JSON/hash, exact integer versions and immutable internal source proofs. DEFER/REFUSE cannot execute. Manifest actions are limited to Files REDACT/PROVIDE_REDACTED_COPY/DELETE_BYTES or current Relationship CORRECT_CURRENT_METADATA. Unsupported entity/history writes are rejected. No route, wire field, enum, role or canonical specification changed.

## Actual effects and evidence

A safe copy is an independently reviewed actual CLEAN upload on this DATA_REQUEST, bound to original and replacement versions/private bytes/hash. Files publishes the verified redacted representation for the existing authenticated content endpoint. REDACT marks the original sensitive, requiring explicit SENSITIVE_READ for FULL; REDACTED never falls back to FULL. This minimizes access and does not silently erase original evidence. PROVIDE_REDACTED_COPY exposes the same actual reviewed representation without introducing a public URL.

Contact correction uses an actual normal MAKER -> independent CHECKER ContactRequest approval. The customer-owned reader resolves the current exact confirmed-contact history and approved request, unchanged contact purpose and actual post-approval Relationship version. Privacy execution validates and records fulfillment of that already applied exact correction; it neither reapplies contact nor gives a DATA_OFFICER CHECKER authority. Later metadata changes invalidate the unexecuted frozen version. Approved terms, Party identity and historical contact/decision snapshots remain immutable.

Retention review derives DEMO_ID_COPY 30 days from an actual selected-source signing assessment, and DEMO_AGREEMENT 3650 days / DEMO_OPERATION 180 days from immutable ACCEPT_SUBMISSION of a real CLOSED CLOSEOUT. Missing anchor is not eligibility. This is an internal synthetic review flag, not automatic deletion or a statement of statutory retention. Reviewed decision/manifest basis remains required.

## Durable disposition and concurrency

EXECUTE atomically writes exact immutable per-record reservations, cleanup-key hashes, a DATA_DISPOSITION job, audit and idempotency result. The request remains DECIDED. Global target-record hold membership is serialized with reservation; earlier hold blocks with HOLD_ACTIVE, later hold with DISPOSITION_COMMITTED, including failed cleanup. Hold placement/release events retain original evidence/reason; release excludes the placing officer and request/source contributors.

One bounded worker uses a 60-second lease and independent 20-second heartbeat. Expired attempts are recovered by the shared queue. Logical effects run in a transaction with a final ownership/lease fence; no long transaction holds the Job row while waiting for heartbeat. The immutable intent progresses RESERVED -> LOGICAL -> DONE for erasure, or RESERVED -> DONE for actual safe-copy/contact fulfillment. Only completion of every record and actual physical cleanup can publish EXECUTED / SUCCEEDED. Failure leaves DECIDED/incomplete; bounded queue retry preserves the exact intent. Manual retry is audited and binds the current eligible retry officer for future work without rewriting original EXECUTE history or manifest.

Privacy and DATA_REQUEST evidence use one shared transaction advisory guard before financial/record/request locks. This intentionally serializes this bounded module to prevent target/request/replacement inversion, including older DataRequest source files. Financial locks remain Dossier -> Contract -> current record, then request/copy. Normal financial operations do not acquire this privacy guard after holding a source File. Shared-key SQL guards reject new clone/blob/association references to reserved erasure keys. Shared retained byte owners are rejected rather than falsely claiming erasure.

Files first commits a DISPOSED limited visible tombstone and replacement opaque blob pointer while durable intent retains exact original/redacted cleanup keys/hash. Guarded physical deletion occurs outside the business transaction. It validates committed LOGICAL authorization, no retained references and actual bytes/hash before deletion. An already absent key is reconciled only against that exact committed intent. Final cleanup acknowledgement is fenced. Orphan and maintenance cleanup skip disposition-owned keys; they cannot manufacture execution success. Filesystem guards are nonblocking, reference-table locks bounded, and no REQUIRES_NEW byte cleanup runs while an outer business transaction holds blob rows. Source evidence consumers fail closed when a historical original is disposed.

Current roles, every affected record scope and supplemental sensitive grants are rechecked before execution, retry and new physical effects. Revocation after reservation leaves the immutable authorized intent incomplete/recoverable; it does not discard irreversible reservation or fabricate success. Retry may use another currently eligible independent officer.

## Frontend impact and verification limits

No breaking API/schema change and no Frontend edits. Four DataRequest operations become real; list/get/commandJob gains DATA_DISPOSITION source/retry/action hints; File metadata/content honors actual verified REDACTED and DISPOSED states. Current Customer ContactRequest workflow supplies the correction proof through existing APIs. External FE acknowledgement and consumer/browser integration remain pending.

Source review and exact final skipped-test build evidence are recorded separately in the review report. Compilation proves Java/source/dependency compatibility only. V013 migration, actual holds/reservation races, byte cleanup/crash recovery, safe-copy download, contact workflow, HTTP authorization, real PDF, FE E2E and NFR acceptance have not been executed in this task and require owner-authorized verification scope. Local JDK 25.0.1 differs from the canonical pinned 25.0.4.1.

Final independent bounded source GO at e718ccc, review1595418.107-source skipped-test clean/package PASS20Oct10:07:55+07; JARfa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602. V013 subsequently applied successfully during exact-JAR operational startup at20Oct10:10:29.951+07; earlier unexecuted-migration statements describe this implementation task before the separate runtime action. No Privacy workflow/byte-disposal/HTTP/FE acceptance.
