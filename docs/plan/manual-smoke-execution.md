# Manual synthetic original-contract smoke — authorization and execution record

Owner approval:21 October2026, limited to item1 of backend-verification-plan.md. This approval does not authorize automated tests, load/fault injection, other domain acceptance, real-person delivery, paid services, production deployment, existing-data destruction or PR merges/main/dev changes.

## Baseline and separation

Reviewed application source e718ccc5f7f6ba4d3881d924174440bedfbf7db1; documentation delivery ef33bae18236c9d4d79b49772f0549c9f2e10517; JAR SHA256 fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602. Canonical A/B1.3.0 unchanged. Source/JAR/contracts must be rehashed for the run. New verification branch feature/backend-manual-smoke is documentation/configuration evidence only; the reviewed application baseline remains unchanged.

Initial21October passive observation: Docker Engine was not responding, ports8081/56432/3310 had no listeners, and the earlier managed API was not observed. Installed Docker Desktop was started without issuing commands to the old project. Existing contract-governance containers remained stopped, with their original IDs and volumes. No claim that the historical PID14772 remains running is made.

Planned isolated namespace: runId manual-smoke-20261021-a, Docker project cg-smoke-20261021-a, fresh PostgreSQL database/user and own new PostgreSQL/ClamAV volumes/bridge. PostgreSQL56433, ClamAV13310, application HTTPS18443 bind loopback. Run files live only under ignored D:/contract-document-dashboard/var/verification/manual-smoke-20261021-a, with separate storage, logs, environment, process metadata, cookies and TLS material. Native Spring SSL uses a local certificate trusted by curl --cacert only, with no system trust modification or --insecure bypass. No main.env, main launcher metadata, existing volume or main API process is reused.

Design was independently reviewed before environment creation. Before first HTTP request the operator must record actual project/container/volume/network ownership, mounts/ports, database/storage targets, exact JAR hash and process configuration; the separate reviewer must confirm isolation. Secrets, cookies, CSRF/session credentials, signing material and private keys are excluded from tracked evidence. Existing resource/state snapshots allow before/after comparison without modifying business data.

## Approved manual sequence and expected sources

1. Actual session/login, cookie/CSRF rotation, proper authorization, ETag and idempotency. Selected invalid/missing CSRF, stale/missing If-Match, duplicate/conflicting idempotency and cross-scope operations must have canonical error/no forbidden effect.
2. Create new synthetic Party and Relationship through real authorized HTTP, assign Maker; create Dossier DRAFT.
3. Upload synthetic evidence through HTTP, real FILE_SCAN/ClamAV to CLEAN, then link exact files. Scanner CLEAN is distinct from independent human evidence verification.
4. Supply the necessary first Policy via its ordinary manual product workflow; RUN_REGRESSION is the required application business command invoked by one manual HTTP request, not an automated test harness or broader Policy acceptance. No direct database ACTIVE-policy or fixture-result seeding.
5. Submit immutable Revision, inspect real L1/L2 order/SoD; final approval creates exactly one PREPARED original and durable ARTIFACT_PDF intent. Generate/scan/hash/authenticated-download the real PDF.
6. Real immutable SigningPacket, independent verification after byte guards, strict on-time readiness and original effectivity. Before startOn midnight the original remains SCHEDULED/READY with no premature ContractVersion; actual effectivity creates the initial version and ACTIVE while observed service stays NOT_STARTED.
7. Inspect selected denial/no-effect outcomes, immutable evidence and actual entity/job/file/audit changes. Do not expand to other business modules, fault injection or erasure.

Expected outcomes come from A/B and source-independent canonical oracles before requests. Actual IDs may be used as later references; actual results must never generate expected outcomes. Each step records sanitized request/response, before/after state, database/job/file effects, PASS/FAIL/BLOCKED/NOT RUN and any defect/severity. HTTP calls are individual, manually assessed actions, not a generated/looped assertion suite. Bounded observation of asynchronous workers is evidence gathering, not load or fault injection.

## Time gate and owner decision

Baseline Clock.systemUTC() has no reviewed configurable verification clock. First Policy publication on21October can become effective no earlier than22October00:00 Vietnam. Submission after that requires startOn at least23October, so original effectivity cannot truthfully be completed immediately under real time. No host clock, database timestamps, canonical dates or baseline JAR may be silently changed.

Owner chose21October: allow a PROPOSAL of separate verification-only Clock configuration. This authorizes design and independent review only, not activation/execution. The exact proposal, artifact hash, baseline integrity, bean-loading mechanism, session/job consequences and activation/reversion commands must be presented for a separate owner decision. Until approved, use the real baseline clock and mark gated steps BLOCKED/NOT RUN as applicable. Independent earlier HTTP steps may proceed after the isolation gate.

## Current result

IN PROGRESS. Isolation design reviewed; actual creation/manifest and HTTP evidence pending. No workflow PASS or Backend acceptance is declared by this record. Actual defects must be reported against the pinned baseline; source repairs require a separate coherent commit, independent review and separately identified JAR before retesting. The original baseline evidence must remain intact.

After item1 completes, present the next bounded acceptance plan for separate approval. Manual smoke alone never constitutes full Backend acceptance.