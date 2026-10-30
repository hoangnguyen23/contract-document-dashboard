> **Current status — 30 October 2026:** testing is deferred by the owner's latest instruction. Item 1 was previously authorized and partially executed on 21 October; its FAIL evidence remains valid. The candidate repair has source review/build/startup evidence but no HTTP retest. The historical proposal below does not describe current authorization. See backend-completion-plan-20261030.md and the manual-smoke record on feature/backend-manual-smoke@d352f0d. No automated tests or further HTTP scenarios are authorized by this document.

# Proposed final backend verification — NOT AUTHORIZED / NOT EXECUTED

Owner master mission18October preserves the no-automated-tests decision. This plan requests future explicit permission only after source closure. It authorizes nothing by itself. Source Complete, Build Verified, Runtime Started, Business Verified, Integrated Acceptance and Production Ready remain separate verdicts.

## Isolation and safeguards

Proposed meaningful isolated acceptance environment, never the working database: separate project/database/storage namespace under ignored var/verification/<runId>, explicitly checked unused loopback ports, synthetic data only, paired DB/private-file backup. No wipe/reset/delete of current volumes or files; no real external contact or paid service. Local capture transport only. Seed core identities plus one authored profile/overrides per scenario, never import the whole fixture catalogue as live business. A fixed injected server test clock may exist only in approved private verification profile, no public reset/clock/allocator API. Pin the reviewed source/JAR/contracts and record actual Java version.

## Separate permission scopes

1. Manual synthetic HTTP workflow smoke: real session/CSRF/ETag/idempotency, relationship/dossier/upload/scan/save/submit/humanreview/PREPARED, real PDF hash+scan+authenticated download, immutable packet/independent verification/readiness and original timeline. Observe both success and required no-effect errors; no fixture-as-output acceptance.
2. Automated provider/consumer and denied-scope/SoD scenarios: requires separately lifting automated-test restriction. Exact canonical fixtures with real PostgreSQL, strict requests/responses/headers, same FE contract commit, no generation of expected outcomes from actual output.
3. Renewal/change/notice boundaries: approved AMENDMENT/RENEWAL/TARIFF_NOTICE, committed409 cache semantics, slot conflict/rebase, local-date lead times, exact receipt/ACK, renewal-before-expiry and late-worker effectiveAt/recordedAt distinction.
4. Obligation/incident/control/termination: current operative due vs retained historical breach, before/afterdue extensions, statutory nonwaivable basis, incident source triggers, actual external/simulated acknowledgements, never fake stopped service/closed closeout.
5. Concurrency/recovery: double decisions/commit, stale ETag and roles/scopes, 24h idempotency, scanner failure, crash/lease expiry/fencing, PDF/capture/effect dedupe, current-source cancellation; exact durable outcome/audit assertions.
6. Privacy/holds/disposition: actual reviewed safe copy, manifest hash/version, independent officer decision, hold/reservation races, failed byte cleanup visible, no FULL fallback, historical tombstone and current-contact correction rules.
7. Reporting: same scoped snapshot/formulas/drilldown/asOf/lag, branch and reassignment negatives, historical breach, cursor/grant expiry, CSV <=1000/BOM/CRLF/formula protection and redaction.
8. Fresh migration/coherent restore/NFR: isolated all-migration setup and backup+exactblobhash restore, process/dependency restart, failure observations; hardware/load and measurements exactly from A13.2. No passing NFR claim without measured report. Exact pinned runtime patch remains a distinct environment gap if unavailable.

## Evidence and approvals

Each run must name owner approval/scope, source+JAR+contract hashes, environment/data isolation, actual steps/results, allowed/forbidden DB/audit/job effects and unresolved findings. Only executed checks may be PASS; not-run is NOT RUN, never N/A to hide missing verification. No production deployment, paid provider, external delivery, real personal data or existing-data destruction is included in any proposed scope.

Before execution root will present a concrete run subset and side effects to owner. Full shared FE integration/consumer acknowledgement requires the separate FE owner; internal source compatibility review does not substitute actual FE execution.