# Backend integration handoff — 30 October 2026

Status: prepared for FE counterpart review, not sent externally and not an executed integration verdict. Requirements are canonical A/B1.3.0. Current application/build/runtime identities live in [current status](backend-current-status.md); never infer active source from the folder or feature name.

## Contract package and changes

Use the same canonical B and extracted `contracts/openapi-v1.json`, seed/scenario/protocol/manifest artifacts on both sides. A SHA256 `0dc6885d1b7faf65c6d5adf49389baa37474a2036735ed73e5710c47057e43a1`; B `d89a36ea7ae04750589b654442f690832fd675e074df8b23441578b43291e201`; manifest `cb486182799b6319bc3069aecb7cec3ad22a663f3f219a80f3a7356bf3540d18`. Regeneration/version changes follow shared ownership, not local FE DTO changes to fit an unexpected response.

| Change | Operations / fields | Breaking impact | Approval and verification |
|---|---|---|---|
| Canonical synthetic email validation repair81c3d7f | Shared email format in24 operations; exact list/request-vs-response reach in caller review | No wire/schema/permission change; restores syntax-based canonical validation | Routine BE correction, independent source review; original s07/s12 candidate retest NOT RUN |
| Prepared JSONB SQL repair81c3d7f | listFiles/listJobs scope; getDashboard/listAuditEvents/exportAuditCsv ancestry;17 timeline-lag readers listed in caller review | No wire/schema change; SQL bind preservation reviewed | Routine BE correction; original s18 listFiles candidate retest NOT RUN |
| Per-claim fencing d306166 | All five durable job kinds, internal lease owner only | No public Job field/action/status/schema change | Independent source review GO; stale-worker recovery scenario NOT RUN |
| Approved S060 | getContract → Contract.allowedActions | MAKER no longer receives HOLD_READINESS hint; no new permission/schema | Owner approved; internal shared review GO; external FE acknowledgement PENDING |
| Approved S024 | commandNotice → Notice.allowedActions | ACK recorder does not receive VERIFY_ACK hint; no new permission/schema | Owner approved; internal shared review GO; external FE acknowledgement PENDING |
| Approved termination1.3.0 | commandControlRequest → existing409 STATE_CONFLICT; get/listControlRequests action hints | Business date semantics changed: effectOn cannot precede currentVersion.effectiveFrom; no wire field/enum change | Owner approved; internal shared review GO; external FE acknowledgement PENDING |

Detailed caller map: [independent review](reviews/backend-baseline-caller-review-20261030.md).24 email-reachable operations does not mean all24 previously failed; optional/union response branches may not appear in a request. JSONB reach is source-derived, not a list of measured runtime failures.

## FE integration expectations

- Use exact canonical operation IDs, including existing plural spellings. Backend has85 operations in source inventory; UI should not infer runtime acceptance from that count.
- Session endpoint supplies current session/CSRF protocol. Preserve HttpOnly/Secure host-only cookie, rotate tokens after login and obey expiry/revocation. Do not persist session secret in localStorage.
- Same-origin HTTPS routes SPA and `/api/v1` through a deliberate proxy boundary. Current local API8081 is plain loopback and launcher does not supply TLS proxy. FE HTTPS proxy or shared runtime preparation is required; record actual ports instead of copying B's example8080 as a live fact. No wildcard CORS or insecure-cookie workaround.
- Mutations use canonical Idempotency-Key and If-Match requirements. Preserve intended command and input on conflict; obtain current state/ETag before a new user decision. Do not automatically replay privileged approval/signing/control actions after a409/412.
- Use canonical problem code/status/field errors for UI handling.403 authorization/CSRF,404 scoped non-disclosure,409 domain/idempotency conflict,412 stale version,422 validation and428 missing precondition have different meanings; follow B for each operation rather than this abbreviated list.
- Render allowedActions/blockedActions as server hints, never authorization proof. Permission may change before the next request. Preserve rejection/return/hold history and expose blocking reasons without inventing bypass actions.
- Enqueue success is pending work, not completed PDF, scan, dispatch, effectivity or disposition. Poll canonical Job/resource endpoints within UI lifecycle; display QUEUED/RUNNING/FAILED/CANCELLED truthfully. Retry only when server hints/current source permit it.
- Private download uses authorized endpoint and current representation. SCANNING/SCAN_FAILED/DISPOSED or missing safe copy cannot be treated as a public file link. CLEAN is scan outcome, distinct from human evidence/signing verification.
- Contract PREPARED/READY/SCHEDULED/ACTIVE and service states are distinct. ACTIVE does not prove banking service execution. Notice DISPATCHED/local capture does not prove received/ACK; recorded ACK requires independent verification.
- Termination date rejection must retain original request/source and explain cancel/new reviewed request when needed; FE must not silently clamp effectOn or rewrite timeline.

## Handoff acceptance record

FE counterpart should record its branch/commit, consumed contract hashes, generated-client/toolchain result, acknowledged S024/S060/termination semantics, proxy/origin design and unresolved mismatches. This file deliberately leaves those as PENDING because no external counterpart response or browser/provider/consumer execution has been obtained.

Backend preparation includes source reviews, exact skipped-test build artifact, configuration instructions and known-defect/retest list. Owner's testing deferral remains in force. The next execution scope is V01 original manual workflow first, with real provider/consumer/browser acceptance scheduled as counterpart and owner permissions allow. No tests, browser scenario, external message or FE source edit is performed by creating this handoff.
