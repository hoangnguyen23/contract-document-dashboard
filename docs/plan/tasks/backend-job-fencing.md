---
id: BE-C02-JOB-FENCE-01
scope: Durable job claim ownership
status: done
depends-on: [BE-C01]
---

# Per-claim fencing repair

## Objective

Close independent P1 JOB-FENCE-01 in `reviews/backend-completion-invariants-20261030.md`: a stale FileScanWorker attempt must not renew/complete/fail a newer lease even when both attempts originate in the same process.

## Context and path

Read A BR-053/§13 and B§7; JobQueue, FileScanWorker and PrivacyJobs lease predicates. Source change is limited to `apps/api/src/main/java/vn/contractgovernance/api/shared/JobQueue.java`. No canonical/schema/migration/FE change. Claim signature remains compatible; actual lease_owner becomes unique per claim, independent of the caller worker ID.

## Delivery

Developer implementation committed `d306166`: generate one UUID for a selected claim and persist/return that exact token. Existing state/owner/live-lease predicates protect all mutations. See developer evidence in `analysis/backend-job-fence-repair-20261030.md`. Independent exact-commit review and skipped-test build pending at task creation. Do not reuse old81c3d7f JAR as evidence for new source.

## Verification and completion

Independent source review must inspect all token consumers, stale-claim/retry-reset identity and unchanged transaction/state/attempt behavior. Build after source review with all test phases skipped; record actual source/toolchain/JAR hash. No HTTP/race/fault injection or runtime restart. Race acceptance remains a separate NOT RUN item, even after source review/build pass. Existing process8081 and old/new package identities are preserved. Root manages Git; no automatic merge/main/dev change.

## Closure

Independent exact-source GO atd306166; cumulative2487adb clean/package passed with Oracle25.0.4.1 on30October08:25:08+07. All test phases skipped. New JAR99e695f8a28f998eafe9264ea9c5883e2317f1ac2ae4593dc9caa6bc1c0f74ed remains in feature worktree; no deployment/restart. Source task is done; runtime lease-race/recovery acceptance remains NOT RUN. See independent review and exact build evidence.
