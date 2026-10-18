# Controlled Change implementation

Authority: canonical A/B1.2.2, A§7, B§4/6/7 and the existing closed OpenAPI schemas. This is source implementation evidence, not business acceptance.

## Implemented paths

`change/ChangeService` owns the five Change operations, mutable drafts, finite target whitelist, current base/schedule checks, explicit rebase, cancellation and reserved future slot. `ChangeApproval` owns immutable ChangeRevision and CHANGE Round commands/snapshots. `ChangeSigning` owns CHANGE packet source and independent assessment. `ChangeArtifacts` is dispatched by the existing ARTIFACT_PDF claimant, renders exact immutable source, scans actual bytes and commits private file references under the pending-write guard. `ChangeTimeline` runs under the existing Contract lock before expiry.

Negotiated AMENDMENT/RENEWAL flows are implemented through approval, artifact, independent signing, READY, COMMIT, apply and version history. TARIFF_NOTICE can draft/submit/review/approve, but readiness and existing Notice cleanup remain explicitly blocked until the next real Notice adapter. No ready=true callback or invented notice success exists.

## Invariants

- Existing-contract mutations lock Dossier → Contract before Change; source review checks file upload/scanner paths use the same ancestry.
- V009 is additive. V001–V008 are unchanged. Immutable ChangeRevision, render source, artifact binding, signing input/assessment and effect/event history have database protections. One SCHEDULED Change per Contract and one OPEN CHANGE Round are database constrained.
- Full ChangeRevision source hash and consolidated policy-input evaluation hash use their distinct canonical projections. Renewal checks extension duration from old end to new end through a Java-only evaluator input; stored terms retain original startOn.
- Every required document needs its own human PASS. Assessment source contributors, actual uploaders and stable contributors retained across rebase cannot independently approve or verify their source. The 51st contributor fails transactionally; IDs are never truncated.
- Packet source is immutable, binds exact approved revision/artifact/hash, same-Change uploaded signed copies and signer authority at each reported signing instant. Server verification must finish before effectiveOn local midnight; an on-time assessment remains usable by a delayed worker.
- COMMIT uses the current base, expected schedule counter and empty future slot. Semantic BASE_CONFLICT/SCHEDULE_CONFLICT intentionally persists CONFLICTED/audit and caches its exact canonical409; ordinary prevalidation errors roll back and are not cached. Cached replay still verifies current authorization first.
- The Contract schedule counter increments at reservation and again at apply/cancel/conflict release. Application uses committedScheduleVersion, not the earlier expectedScheduleVersion.
- Due qualifying renewal is examined before unchanged-term expiry under one Contract lock. Failed current business/evidence guards conflict/release the renewal and allow expiry; database failures roll back. Applied renewal reloads current-version terms before checking the new end. Old boundary intents become CANCELLED, including catchup beyond both boundaries.
- Each Change creates at most one immutable effect/version. Version interval closure and actual recordedAt are distinct from contractual end/effectiveAt. Current terms never overwrite the original approvedTerms.
- TERM_REVIEW supersession is restricted to untouched, future-due duties without human audit, evidence, exceptions or breach. The successor duty link is explicit; prior breach/action history is preserved.
- Read projections use bounded read-only current guards for action hints. They do not lock files, freeze associations or write review history. Actual commands recheck bytes and source after locks.

## FE handoff

No schema, request, enum, permission, timestamp meaning or FE file changed. The five Change operations are newly available; commandChange remains partial for the pending tariff/Notice dependency. Existing Round operation IDs now support CHANGE as well as DOSSIER; CONTROL_REQUEST remains pending. All four SigningPacket operation IDs support CONTRACT and negotiated CHANGE. Job retry adds CHANGE artifact/effectivity source handling; other future job kinds remain partial.

Change has no public revision/readiness/mandatoryChecks additions. Use `getRoundSnapshot` with targetKind CHANGE for ChangeRevision. `target.kind`, `baseContractVersionId`, `expectedScheduleVersion`, `committedScheduleVersion`, artifact/signing references, appliedVersionId and conflictReason retain their canonical meanings. Existing If-Match, Idempotency-Key, current auth and cached committed409 rules apply.

## Verification boundary and next dependency

Independent bounded source GO at7e2975c/review2310638. Final84-source clean/package passed18October18:53:59+07 with test resources, compilation and execution skipped. Prior ba570e5 compile failed and was repaired in7e2975c; see the review report. Build does not verify runtime/business behavior. No tests, HTTP workflows, manual PDF rendering/scanning/download, Docker, database migration or business data mutation has been performed for this tranche. Root records final exact reviewed-source package evidence separately.

Next implement real Notice dispatch/capture/acknowledgement and NOTICE_ACK, then replace the explicit tariff readiness/cleanup blockers and review both sides atomically. Continue remaining obligations, incidents, controls, privacy and source closure under the existing master mission.

Current corrected G4 source GO2af915d/review86035f9;84-source skipped-test package PASS18October18:59:20+07, JAR SHA2568bc3554e12edfe0c4bf180dec6b8c109c8135227ce6c994ce1aa2a18356b493d. The earlier review missed an enum violation in TERM_REVIEW supersession, now repaired and explicitly recorded. V009/startup was observed on prior7e2975c package only; this corrected package has not been started.
