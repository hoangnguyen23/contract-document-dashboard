# Independent Backend foundation follow-up review

Final application source reviewed at integration commit `f9a495a`, including identity fixes `124a724`, core/setup fixes `869081f`, and evidence fixes `803da706` / `eac2d6a`. This review closes the eight findings in [the historical initial review](backend-foundation-review.md). It is static source review plus compilation and canonical extraction; no tests, HTTP calls, application startup or database runtime verification were performed.

## Final static verdict

The reviewed foundation source has no remaining P1/P2 findings from this review. All eight initial findings and the subsequent multipart, scope, redaction and replay-cleanup findings were closed by reading the concrete repaired source. This verdict is limited to the delivered foundation scope; it does not establish G1/G2 integrated acceptance or completion of the full Backend.

## Identity and customer

ContactRequest `expectedRelationshipVersion` mismatches now return `409 BASE_CONFLICT`; the request's own stale `If-Match` still returns `412 PRECONDITION_FAILED`. Required query parameters fail with field-specific `422` before controller binding. User create/update validate branch IDs, roles and permissions as unique sets.

Contact redaction checks the current maker assignment. An `AUDITOR` who also has an unassigned `MAKER` role cannot unmask a scoped Relationship or ContactRequest. Supplemental `SENSITIVE_READ` is evaluated explicitly. Current role, branch scope and assignment checks remain before cached replay; active-state mutation guards apply only when creating a new effect.

## Core, response atomicity and setup

CommandExecutor resolves the exact canonical operation by method/path and validates both callback results and cached replay bodies inside its transaction, before idempotency insertion or commit. Provider response defects therefore roll back domain, audit and durable job effects. ContractRegistry rejects undeclared response status and nonempty `204` payloads. Response advice remains a final defensive check. Missing query/form arguments, missing multipart parts and argument conversion/binding errors map to canonical `422` responses.

CI actions are pinned by commit SHA. CI runs canonical extraction and Maven packaging with tests skipped. The Unix Maven wrapper is tracked executable (`100755`). Compose pins PostgreSQL 18.6 and ClamAV 1.5.4 by manifest digest. Setup instructions distinguish Compose `.env` loading from the environment needed by a separately launched Java process. Explicit demo seeding requires a profile and password; no default application credentials exist. Source constructors/imports show no obvious dependency cycle. SQL/migration definitions were read, but Flyway execution and dependency injection at startup remain unverified.

The future Change COMMIT implementation must also restrict the persisted `409` cache exception to `operationId=commandChange` and `action=COMMIT`, as required by protocol-rules-v1. Current foundation callbacks throw semantic conflicts rather than return a `409` ApiResult, so this is not a reachable defect in the delivered foundation handlers.

## Dossiers, private evidence and jobs

Multipart upload validates the canonical closed FileUpload shape before persistence. Unknown/repeated fields and invalid enums/UUIDs fail validation; an absent required `branchId` part stays absent rather than becoming null. An explicitly null branch is reserved for a policy parent. Files/jobs list queries explicitly reject a requested unauthorized branch with `404` before paging.

Scanner completion and failure reconciliation lock parent before file, matching draft/upload lock order. The worker rechecks current source version/hash, parent state and authorization, then commits lease-owned job completion, File state and audit together. Fifth expired scan leases are reconciled to a visible `SCAN_FAILED` File. Retry eligibility requires the current matching failed scan job and bytes hash; cancelled or obsolete intents are not presented as retryable. The live dossier Round uniqueness index now uses canonical `targetKind`.

File retry authorization is separated from state guards, so successful same-key retries can serve their saved response. Own unattached deletion retains an internal parent/actor authorization tombstone, allowing cached `204` replay after current authorization even though the live File was deleted. Current scope is never bypassed by a cache hit.

The dossier projection respects the current maker assignment and supplemental sensitive grant. Ordinary auditor metadata masks filenames. Full sensitive download uses current parent role/scope and supplemental permission. Redacted download requires a verified safe private representation and refuses with `409 READINESS_BLOCKED` when absent; it never substitutes original bytes.

Best-effort cleanup is confined to a newly written opaque blob whose database reference count is zero. Cleanup failure preserves the original cached upload response or operation error and records a maintenance warning. Referenced-byte reads/hash checks, scanner results and business authorization remain fail closed. Scheduled 24-hour orphan reconciliation and coherent database/blob backup/restore remain operational follow-up work before full acceptance.

## Exact operation coverage

[The complete 85-operation matrix](../backend-operation-coverage.md) distinguishes source coverage:

- 36 operations have implemented foundation handler paths.
- 2 command operations have partial coverage: Dossier `CANCEL`/`CLONE`, and Job `RETRY` for current failed `FILE_SCAN` only.
- 47 later-gate operations have no handler implementation yet.

Dossier `SUBMIT`/`WITHDRAW` and unsupported Job worker kinds return `503 SERVICE_UNAVAILABLE` without effects until their real modules exist. Schema availability does not imply handler implementation or successful business execution.

## Integrated build and canonical evidence

From `apps/api` in PowerShell:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' clean package
```

The final integrated build completed successfully at 22:50:54 on 11 October 2026 (`Asia/Saigon`). Maven 3.10.0 compiled 44 source files with `javac release 25` and produced the executable Spring Boot JAR. The actual compiler runtime was Oracle JDK `25.0.1+8-LTS-27`, not the specification's `25.0.4.1` baseline. This is accurate compile evidence, not exact-baseline runtime certification. Maven reported “Not copying test resources”, “Not compiling test sources” and “Tests are skipped”. No tests were created or executed.

Canonical extraction re-read A/B 1.1.0, verified the specification SHA and six frozen block digests, and regenerated the derived contract resources. `git diff --exit-code -- contracts apps/api/src/main/resources/contracts` returned zero with no differences.

The Docker daemon was unavailable on the authoring workstation. PostgreSQL/Flyway execution, ClamAV connectivity, HTTP session/scope behavior, FE browser integration, concurrency, recovery and measured performance remain unverified. G1/G2 integrated acceptance is not claimed.
