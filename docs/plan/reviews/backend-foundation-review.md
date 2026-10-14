# Independent Backend foundation source review

**Historical snapshot:** the eight findings below were closed through the concrete fixes recorded in [the final follow-up review](backend-foundation-follow-up.md). Preserve this report as review history; current source verdict and build evidence are in that follow-up.

Reviewed platform committed HEAD `8fadac8` in `work/be-platform`, preserved identity/customer sources in `work/be-identity`, and preserved dossier/files sources in `work/be-evidence`. Canonical A/B1.1.0 (minimal POLICY Job branch-nullability correction) is authority. This is static source inspection; no application code edited, no tests written/run, no runtime or build verification claimed.

## Findings

### [P1] Validate all multipart fields before persistence

Location: `work/be-evidence/apps/api/src/main/java/vn/contractgovernance/api/files/FileController.java:19`, `FileService.java:30–43`; platform `shared/ContractHttpAdvice.java:19–21` only validates JSON request bodies.

`targetKind`, `purpose`, form `branchId`, and `targetId` bypass the FileUpload schema. `purpose=ANY_UNKNOWN_VALUE` is accepted by FileService, written to File JSON and cached as201, before response schema validation fails with503. A missing branch field is treated like explicit null. Unknown multipart fields and repeated values are ignored. Required lowercase UUIDv4 and enum/closed request constraints are not enforced here. An invalid client request must fail422 before bytes/database/audit/job/idempotency success commit. Validate a normalized complete FileUpload projection (binary placeholder for file) and exact form field allowlist/cardinality before calling upload; do not rely on response validation after commit.

### [P1] Serialize scanner completion against its parent lifecycle

Location: `work/be-evidence/apps/api/src/main/java/vn/contractgovernance/api/files/FileScanWorker.java:33–36,48–50`.

The transaction locks only File. `guard` reads the parent through `requireWritableParent` without locking it. A maker can CANCEL the parent after the guard passes but before the worker marks the File CLEAN and job SUCCEEDED. The claimed intent therefore completes after its source was closed, violating current-parent source guards in B§7. Lock the actual parent before File consistently with upload/dossier-save lock order, recheck current source and actor eligibility in that serialized transaction, then finish job+File+audit atomically. Avoid adding parent lock after File because normal draft save already takes parent→File.

### [P2] Successful file mutations cannot replay their saved response

Location: `work/be-evidence/apps/api/src/main/java/vn/contractgovernance/api/files/FileService.java:61–72,75–78`.

`retry` calls `requireRetrySource` in the authorization callback before CommandExecutor cache lookup; that checks SCAN_FAILED. After the first successful retry the file is SCANNING or CLEAN, so the same key returns409 instead of the exact cached200 with Idempotent-Replayed:true. Upload also checks parent draft writability before replay, which is a state guard rather than only authorization. DELETE removes File and then the same-key replay fails at requireFile before it can serve cached204.

Separate current role/scope/ownership authorization from mutation state/If-Match guards. Check state only inside uncached operation callback. DELETE needs retained internal replay authorization context/tombstone linking actor+parent+branch so cached204 is served only after current authorization without requiring a now-deleted live entity. Do not bypass current scope checks merely by returning a cached success.

### [P2] Contact approval reports the wrong conflict class

Location: `work/be-identity/apps/api/src/main/java/vn/contractgovernance/api/customer/CustomerService.java:162–165`.

Stale `expectedRelationshipVersion` throws `ApiException.stale`, mapped to412 PRECONDITION_FAILED. A§AC010 explicitly requires409 BASE_CONFLICT for an otherwise-current ContactRequest whose relationship changed. If-Match on the ContactRequest still independently uses412; the embedded expected relationship version is a semantic base guard. Use BASE_CONFLICT with currentVersion context without mutation/cache. The source creation mismatch at line109 should likewise be consciously classified rather than sharing generic If-Match semantics.

### [P2] Required query parameters are not enforced by canonical parameter validation

Location: `work/be-platform/apps/api/src/main/java/vn/contractgovernance/api/shared/ContractRegistry.java:64–88`; `shared/ProblemAdvice.java:36`.

Only provided query names/values are validated; the required-parameter loop checks header/path and explicitly continues for query. Missing `representation`, `targetKind`, or `targetId` is therefore deferred to Spring's MissingServletRequestParameterException and generic503 mapping, rather than contract422 with `/query/...` violations. Validate absence of every required query parameter. Map missing form parts/type conversion to canonical422 too, especially multipart IDs/form arguments not covered by path validation.

### [P2] Response validity is checked after mutation commits

Location: `work/be-platform/apps/api/src/main/java/vn/contractgovernance/api/shared/CommandExecutor.java:43–48`, `shared/ContractHttpAdvice.java:24–25`.

CommandExecutor writes cached success and commits before response advice validates its body. Any programmer/client input issue that produces an invalid success body thus yields503 to the caller after domain/audit/job/idempotency have committed. The multipart invalid-purpose case above demonstrates a reachable instance. Validate the exact operation success response inside the transaction before cache insertion/commit; keep HTTP advice as a final defensive check. This is a contract atomicity issue, not an argument to skip response schema checks.

### [P2] Duplicate role and supplemental permission sets are persisted

Location: `work/be-identity/apps/api/src/main/java/vn/contractgovernance/api/identity/IdentityAdministration.java:43–45,61–63,80–88`.

Only branchIds uniqueness is enforced. Canonical UserCreate/UserUpdate array schemas do not include uniqueItems, while B§4 explicitly requires ID/set arrays unique. `roles=[MAKER,MAKER]` or duplicate EXPORT passes schema and is preserved in response JSON, while Actor converts it to a set. Validate duplicate roles and permissions with field-specific422 to keep wire and effective authorization state consistent.

### [P2] Lease exhaustion leaves scanner File stuck without its retry path

Location: `work/be-platform/apps/api/src/main/java/vn/contractgovernance/api/shared/JobQueue.java:28`; evidence `files/FileScanWorker.java:43–44` and `FileService.java:71`.

Expired RUNNING lease on attempt5 is moved directly to FAILED by JobQueue; the worker no longer receives that job and never sets corresponding SCANNING File to SCAN_FAILED. Its file RETRY remains unavailable because it requires SCAN_FAILED. Ensure exhausted FILE_SCAN lease reconciliation atomically moves the still-matching file to SCAN_FAILED+audit, or let the job retry use a correct current-source path supporting this state. Source hash/version and parent checks remain necessary.

## Incomplete foundation reachability in inspected snapshot

- `DossierController` lacks GET `/dossiers`.
- `FileController` lacks GET `/files`.
- No Job controller exists for GET `/jobs`, GET `/jobs/{id}`, POST `/jobs/{id}/commands`.
- Dossier SUBMIT/WITHDRAW intentionally returns503 without a DossierApprovalBridge implementation. This is a truthful fail-closed G3 placeholder, not completed approval behavior. Draft projection always reports SUBMIT blocked even if a future bridge exists; when that bridge is implemented the projection must compute real guards.
- No provider/browser/race/NFR verification was performed, by user instruction. G1/G2 integration cannot be declared accepted solely from this source review or a compile.

These are scope/completion facts. Treat them as remaining work before claiming the corresponding operations/gates complete, without disguising them as successful generic CRUD implementations.

## Positive implementation evidence from source

- Opaque hashed DB sessions, secure host cookie, CSRF before unsafe operations, login session+CSRF rotation, idle/absolute expiry, current actor grants and credential revocation are present.
- Bank-admin directory rights are separated from business read/write; maker assignment restriction, scoped parent access, reviewer eligibility, stable contact contributor SoD, taxId/relationship uniqueness and frozen history tables are present.
- Transaction-bound advisory idempotency lock and cached status/body/ETag, strict JSON schema validation, no-store/trace headers, canonical RFC9457 shape, signed scope-bound cursor and SQL scope filtering are present.
- Private finalized byte storage, server SHA, allowlist magic sniff, real clamd with fail-closed outage, bounded scan workers, lease heartbeats, File association history protection, safe redacted download refusal, and scoped full-content grants are present.

No finding implies source behavior was runtime-tested. Follow-up review should read the concrete repaired source and check that fixes preserve existing authorization and rollback behavior.

## Follow-up source reading — identity commit9e7d3b2

Confirmed source repairs: ContactRequest creation/approval expectedRelationshipVersion mismatches use409 BASE_CONFLICT; ContactRequest own ETag remains412. ContractRegistry now iterates allowed required query parameters before validating provided values, returning field-specific422 on absence. CustomerAccess introduces authorizeAssignedMaker, separating current role/scope/assignment from active-state guard for cached replay.

The original finding headings are retained as the initial reviewed snapshot history; the Contact409 and required-query defects are closed in this identity commit. Multipart argument/missing-part error mapping remains part of evidence/core review. Duplicate roles/permissions remained at this reading and was sent directly to identity owner. Evidence repairs and transactional response validation still require source follow-up after their concrete patches.
