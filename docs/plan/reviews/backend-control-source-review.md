# Control and closeout integration — independent source review

Verdict: bounded source GO at `8f40974b892264493f25d9a74d397f7d4901e5dc`, branch feature/backend-contract-closeout, against owner-approved canonical A/B1.3.0. This is source/build evidence, not runtime, business, integrated or production acceptance.

## Scope and coverage

Independently reviewed initial coherent candidate78b61f2 and repairs c6139e6, f408198, 80ffca8, 6174f9f, e82a875, bb116f9, f07377b and newline-only8f40974. Traced listControlRequests/createControlRequest/getControlRequest/commandControlRequest; SUBMIT/CONFIRM/CANCEL across ACTIVATE/SUSPEND/RESUME/TERMINATE/STOP; all four existing Round routes including CONTROL_REQUEST list SQL, detail, exact snapshot and five commands; actual ControlEndProof implementing ServiceEndProof for Obligation CLOSEOUT.

Semantic source delta: four new Control operations implemented; four Round operations partial-to-implemented for CONTROL_REQUEST; commandObligation partial-to-implemented through the real service-end evidence producer. Aggregate source coverage is80 implemented /1 partial /4 missing of85. commandJob DATA_DISPOSITION and four Privacy operations remain, so full Backend source closure is unfinished.

## Findings closed before GO

- A SYSTEM STOP had no frozen revision until confirmation, but its hint required that revision. System-only prefreeze hints now inspect actual source/current artifact read-only; CONFIRM freezes source under locks.
- Optional Notice references were restricted to TERMINATION purpose for every Control kind. Purpose restriction now applies to TERMINATE only; generic optional references retain same-Contract approved/dispatched source provenance.
- Round pending-state and private-byte preconditions ran before idempotency cache lookup. Current role/scope/assignment/SoD and sensitive-read permission now remain pre-cache; pending/open/source assessment checks remain cache-miss business checks. Approved cancellation replay also retains current independent CHECKER authorization from immutable cancellation provenance after the request becomes CANCELLED; new terminal mutation still fails its state guard.
- L2 review reauthorized a historically approved L1 actor after later role revocation. Historical valid assessment remains historical; current acting/new reviewer still requires current authority. A follow-up caught identity uniqueness skipped with that historical role check: stable reviewer IDs now participate in the L1/L2 duplicate check before skipping historical reauthorization, preventing same dual-role actor taking both approval steps through reassignment.
- Approved/SYSTEM cancellation could assess newly introduced ancestor proof uploaded by its own checker. Cancellation now excludes actual proof uploaders and retains immutable manifest, assessor, contributor snapshot and source-state provenance.
- Canonical1.3.0 retrodate blocker was missing from CONFIRM hints. Existing STATE_CONFLICT now reflects the same lower/upper date condition without changing source dates.
- Action projection called locked material checks inside read-only list transactions. Explicit read-only source/current/checked paths use unlocked material reads; mutation paths keep locked current checks. No hint freezes associations or takes Party/Relationship/File mutation locks.
- Slow private evidence checks could cross a true fixed expiry or due scheduled Change after precommand maintenance. Confirmation now captures an authoritative completed instant after byte checks and fails closed if current temporal authority has expired or a timeline boundary is due. No REQUIRES_NEW maintenance is invoked while holding ancestry locks. Contract-owned termination rechecks the actual fixed interval at that instant.
- Newly introduced acknowledgement uploaders were absent from derived CLOSEOUT source lineage. Confirmed Control now retains the exact contributor union with max50 rejection, and termination passes the updated confirmed request rather than the stale preconfirmation object into the owned factory.

No remaining concrete P1/P2 finding was identified within this bounded source pass. Runtime scenarios were not executed to establish these outcomes.

## Invariants inspected

Create/submit/round/confirm/cancel use current active role, branch and Relationship assignment before replay and under parent-first Contract serialization. Urgent checker SUSPEND retains requester authority; independent confirmation excludes requester/all actual source and receipt uploaders. Existing live-kind uniqueness remains the authority; duplicate pending STOP is reconciled to the existing request instead of rolling true expiry back.

Immutable ControlRevision and internal source metadata/hash bind current ContractVersion and actual current artifact, including CHANGE/NOTICE ownership; no original artifact fallback is used. Source/evidence checks retain CLEAN private bytes, exact hashes, current permission and historical File associations. V012 adds immutable source/confirmation/event ledgers and extends only the Contract effect kind constraint for TERMINATED; V001–V011 remain unchanged.

ACTIVATE/RESUME use L1; TERMINATE uses sequential distinct L1/L2; SUSPEND/STOP use REQUESTED without formal Round. Each Control source file requires independent human PASS and required authority/legal conditions. RETURN retains history and parent DRAFT; no invented PUT/withdraw endpoint. Round cancellation changes only undecided steps. Actual observed service changes only at separate confirmation, never at request/approval. RESUME consults real Incident-owned blocking source and last confirmed suspension acknowledgement. Required timestamps and explicit ACTIVATE/RESUME lower bounds remain distinct from server recording time.

TERMINATE checks current ACTIVE, reviewed source, stop/independently assessed never-started proof and currentVersion.effectiveFrom<=effectOn<=today. Earlier effectOn uses ordinary409 STATE_CONFLICT, no CONFIRM effects/success cache/date clamp/history rewrite. Prior independently committed timeline maintenance is still authoritative and may advance a qualifying Change/true expiry before rejection; the new no-effects condition does not roll it back or skip it. Same-transaction termination closes the current version interval, records TERMINATED/STOPPED, cleans unapplied inapplicable Change/Notice/jobs/slot/other controls, preserves APPLIED versions/complaints/remediation/applicable obligations/breaches, and creates deduplicated CLOSEOUT through its owner.

STOP retains EXPIRED/TERMINATED. Terminal NOT_STARTED can be represented only with affirmative independent never-started acknowledgement evidence and no actual start history; mutable NOT_STARTED alone is not proof. Historical ControlEndProof verifies immutable source/confirmation/hash/assessor/contributors/private manifests and stopped-vs-never-started consistency without reauthorizing historical reviewers. Its real producer closes the previously explicit CLOSEOUT dependency; source absence fails closed.

## Static and build evidence

- Canonical1.3.0 owner patch was independently reviewed separately at c5120f5, integrated before this build. A digest and all frozen contract/resource copies match manifest; A/B remain1.3.0. S024/S060 corrections retained. No unapproved API/schema/enum/permission or FE source change.
- CRLF-aware git diff --check passed. V001–V011 have no changes against reviewed canonical-integrated baseline1ecf6c3; V012 has not been executed in this review.
- Independent final command in apps/api: `mvnw.cmd -Dmaven.test.skip=true -V clean package`.
- Exact source HEAD stayed8f40974b892264493f25d9a74d397f7d4901e5dc, clean before this report. BUILD SUCCESS,100 Java sources, finished2026-10-19 10:26:49 +07:00. Test resources not copied, test sources not compiled, tests skipped.
- Packaged JAR SHA-256: `1d027fb1a0123ea2e1731766c8c4a1a67f108b0018ad1cd4f9ab854c7e8eea89`.
- Maven3.10.0 / Java25.0.1 / Spring Boot4.1.1. Requested Java patch25.0.4.1 unavailable locally; nonfatal dependency-repository metadata401 warnings and existing deprecated API notice remain.

## FE and acceptance limits

Existing ControlRequest, ControlRevision, ControlRoundSnapshot, Round, ReceiptEvidence and Obligation shapes are retained. FE gains actual Control commands/service observations, complete CONTROL_REQUEST Round source/actions and CLOSEOUT acceptance against real proof. Owner-approved1.3.0 adds existing409 STATE_CONFLICT for the retroactive-date collision and corresponding hints; source dates must be preserved and corrected via cancel/new review. No breaking wire/schema change, FE source modification or external FE acknowledgement is claimed.

No automated tests were authored/run, no HTTP/smoke, actual bank/provider execution, business-data mutation, manual PDF, Docker start or migration execution occurred in this review. V012 migration, meaningful workflows, expiry/confirmation/hold/concurrency/idempotency recovery, private artifact delivery, Control-to-CLOSEOUT workflow, NFR and FE–BE acceptance remain unverified at runtime. Source/build success does not establish them. Continue Privacy and final whole-backend source/dependency review before any85-operation closure claim.
