# Independent negotiated Change source review

Review date: 18 October 2026, Asia/Ho_Chi_Minh. Final reviewed application/source HEAD: `7e2975c64e030f6c956a94ff844ad3c5fad92518`, branch `feature/backend-contract-changes`, isolated `work/be-contract-changes`. Authorities: canonical A/B1.2.2, especially A sections3.1/5/6/7/9 and B sections3-8; closed Change/ChangeRevision/Round/SigningPacket/Job/ContractVersion schemas; authored Change profiles and hash projections. Independent reviewer did not implement or repair application code.

## Verdict and exact scope

GO for the bounded negotiated AMENDMENT/RENEWAL source implementation and its integrated reporting read models. No unresolved concrete P1/P2 finding remains in this bounded source review. This is not full G4, whole Backend, executed PostgreSQL/HTTP workflow, FE integration, concurrency/recovery or production acceptance.

The five Change routes have real source paths. `commandChange` remains partial for TARIFF_NOTICE readiness/Notice cleanup until the actual Notice adapter is delivered. Four Round operations now support DOSSIER and CHANGE; CONTROL_REQUEST remains missing. Four SigningPacket operations support CONTRACT and negotiated CHANGE; tariff uses Notice instead of a fake packet. Job commands support real FILE_SCAN, CONTRACT/CHANGE ARTIFACT_PDF and EFFECTIVITY paths; NOTICE_DISPATCH/DATA_DISPOSITION remain future scope. No canonical permission/schema expansion or Frontend source change was introduced by implementation. Owner-approved S060/S024 compatibility corrections are separately reviewed and merged as A/B1.2.2.

## Findings repaired and independently inspected

| Source finding | Closure inspected |
|---|---|
| Scheduled renewal storage/current guard errors rolled back maintenance and could leave original ACTIVE after its unchanged end | `37470a3`: current ApiException guard failures produce CONFLICTED and release slot, then existing-term expiry continues; raw database/transaction failures still roll back. Immutable revision/artifact source digests are rechecked. |
| Human evidence evaluation accepted one PASS per required purpose despite another required file FAIL/unassessed | `290ecfb`: each required-purpose document needs its own PASS before approval and in read-only approval hints. |
| Linked signing/assessment files retained associations but still exposed attached=false; upload could bypass contributor50 limit | `290ecfb`, final compile repair `7e2975c`: locked File metadata is updated with real historical association; cumulative upload contributors and body limits reject transactionally rather than truncate stable IDs. |
| Known-current blockers absent from Change/Round/Signing action hints | `290ecfb`, `9e27e66`, `6481c7f`: read-only hints check current source/base/schedule/slot/role/SoD/readiness/material/policy/document metadata as applicable; no byte-lock/history/association writes occur in list projections. Actual mutation still rechecks exact bytes. |
| Old expiry intent marked SUCCEEDED after delayed catchup past renewed end | `9e27e66`: payload boundary mismatch cancels the old intent for ACTIVE or EXPIRED current state; the actual current-version expiry intent owns its boundary. |
| Material identity changed after signing READY but COMMIT still reserved a slot | `2acfb59`: actual COMMIT requires material source under locks, and its read hint checks material without writes. |
| Manual Change effectivity retry accepted obsolete slot/base or current readiness/hold/material failures | `24a0df7`: current parent/base/committed counter/slot/readiness/material/packet/source guards run before retry; actual transaction verifies exact readiness bytes. On-time verified readiness remains usable for delayed workers. |
| Verification final byte pass could cross boundary after cutoff check; REBASE could omit known mandatory codes after clearing roundId | `ba570e5`: final authoritative instant is sampled after all byte/association work and used for both strict cutoff and persisted assessment/verifiedAt. All same-Change Round history supplies required codes for fresh re-assessment, never reused historical PASS. |

Developer self-review also repaired committed409 Problem media type, real draft limits/reference replacement, RETURN/WITHDRAW condition retention and V009 immutable artifact/revision constraints before final review. The first final package at `ba570e5` failed compilation at ChangeService278/279 because the attached-metadata patch referenced undefined `body`; the two-line locked-file correction `7e2975c` was independently inspected, then the justified final package passed.

## Invariants inspected

Existing financial mutations lock Dossier then Contract before Change and its dependent Round/SigningPacket/files/jobs. Upload and scanner callbacks now acquire matching financial ancestry. Current actor/branch/Relationship assignment and independent reviewer/contributor/requester checks precede idempotency replay and are repeated for new effects. Contributors are cumulative across uploads, returned rounds and explicit rebase. L1/L2 remain distinct and ordered; immutable prior decisions/assessments/reassignments are not reused as new approvals.

Change source has only canonical target whitelist fields. AMENDMENT uses consolidated original-start duration; RENEWAL uses old-end to new-end extension duration through a Java-only evaluator port while immutable consolidated terms keep original startOn. Full ChangeRevision factsHash and consolidated policy-input evaluation hash remain separate projections; NFC and evidence ordering are preserved. Exact current base/schedule is checked without asInt truncation: BigDecimal.toBigIntegerExact handles integral decimal/exponent inputs and rejects unequal oversized counters. Stored schedule increments are server-owned rather than copied from untrusted expected values.

COMMIT locks the actual Contract/Change, checks strict future boundary/base/expected counter/empty slot, verifies current material/exact readiness, reserves committedScheduleVersion=s+1 and persists a real durable intent. Losing BASE_CONFLICT/SCHEDULE_CONFLICT intentionally commits CONFLICTED/audit plus the exact cacheable409, with problem media type; ordinary prevalidation failures roll back. Replay retains original response after current authorization and performs no extra timeline effects. Apply/cancel/conflict release increments the counter again. SQL additionally enforces one SCHEDULED Change per Contract and one OPEN CHANGE Round.

Appendix rendering uses immutable revision/source/template hashes, the shared single ARTIFACT_PDF claimant and bounded executor with lease heartbeat. Actual PDF bytes go through ClamAV and the private pending-write lifecycle before reference publication. Source/revision/current job binding and conditional lease completion fence callbacks; no successful placeholder artifact or scan exists. Signing input and independent assessment are separate immutable rows. Uploaded copies are distinct files on the exact Change; canonical authored profiles also place Change authority source on that same Change. Other contract files do not gain access by ID. Historical hashes and file associations are retained.

The same locked Contract timeline applies due qualifying renewal before expiry and then rereads currentVersion terms. A failed current renewal guard does not extend the original agreement. Applied changes create one immutable effect/version and append-only interval closure; approvedTerms remain original. The old boundary worker cannot fabricate a second version. Effective boundary and actual recordedAt stay distinct, and service state is not promoted to RUNNING or STOPPED by agreement effects. TERM_REVIEW creation is real; prospective supersession only affects untouched future-due tasks and links a real successor, preserving prior human actions/breaches.

Reviewed reporting composition continues to use currentVersion terms, current Round target binding, canonical Change parent ancestry and the unchanged authoritative lifecycle breach history. Domain-specific public obligation/control/notice drilldowns and their writer paths still need review as those modules are added.

## Final independent package

From the isolated final HEAD after approved A/B1.2.2 resources and all source repairs:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

BUILD SUCCESS completed `2026-10-18T18:53:59+07:00`. Maven3.10.0, OracleJDK25.0.1, Windows11 amd64, javac release25,84Java sources. Test resources were not copied, test sources were not compiled, and Surefire reported Tests are skipped. Existing optional-feed metadata HTTP401 warnings and deprecated-API notice were nonblocking. JDK25.0.1 is not certification against the specification's exact25.0.4.1 patch.

JAR SHA-256: `3b7d6d127f7162cc559237e7d91632c5e34f76ae10d3617b7064ed2ced556e35`.

## Verification limits and next dependency

No automated application tests, HTTP workflow/scenario requests, SQL/migration execution, runtime startup, actual PDF generation/scan/download, Docker operation, synthetic business-data mutation, external communication, FE browser integration, race/recovery/restore or NFR measurement occurred. V009 exists in source and has not been applied by this review. The current runtime is not evidence for this package. No branch was force-pushed or merged into dev/main, and no application source was edited by reviewer.

Next implement actual Notice revision/artifact/approval, local capture dispatch/receipt/ACK, NOTICE_ACK and tariff readiness/cleanup before claiming full Change support. Later Control must admit the exact current-version approved artifact as a permitted ancestor for SYSTEM STOP, including a CHANGE-owned appendix after application. Later Obligation must preserve currentDueAt/breach history and strict dueBefore reporting semantics. These are explicit remaining mission dependencies, not a reason to stop Backend completion at this tranche.
