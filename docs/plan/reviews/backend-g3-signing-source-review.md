# Independent original signing and lifecycle source review

Review date: 17 October 2026, Asia/Ho_Chi_Minh. The reviewer did not implement or repair application code. Authorities: canonical A/B1.2.0, especially A sections 3.1, 6, 7, 9.1-9.2, 10.1 and 11; B sections 3-7; closed extracted schemas and authored scenarios S016/S018/S060; task backend-g3-signing.

## Verdict and scope

**GO for the bounded original CONTRACT signing/readiness/lifecycle source tranche at `6585c4f896d6b201a77bea77380e7b1f9e511812`.** Initial implementation `b4f75fd` and repairs `9f014fa`, `63cd538` and `6585c4f` were inspected independently. No unresolved concrete P1/P2 source finding remains in this bounded review. One final clean package of this exact source succeeded with all test phases skipped.

This is not HTTP workflow, runtime, legal, security/race/recovery or integrated G3 acceptance. CHANGE signing/effectivity and qualifying renewal remain unavailable; real initial G5 consequences are created but public duty/control completion is absent. Canonical A/B, extracted contracts and Frontend files were unchanged. Current coverage is 85 operations: 47 implemented handler paths, 9 explicitly partial and 29 absent. Four signing operations are partial for CONTRACT only; four Round routes remain DOSSIER only; commandJob supports the implemented scan/original artifact/original effectivity kinds.

## Findings closed

| Finding | Reviewed closure |
|---|---|
| P2: EFFECTIVITY persisted HTTP codes READINESS_BLOCKED/SERVICE_UNAVAILABLE/arbitrary errors outside Job.lastErrorCode, breaking closed Job responses and retry | Failures now use canonical GUARD_BLOCKED or actual STORAGE_INTEGRITY. No wire enum was added. |
| P2: initial live FIXED ContractVersion.effectiveTo used contractual endExclusive, and original signing/activation/expiry advanced future-Change scheduleVersion contrary to S060 | Initial current interval remains open with effectiveTo=null; original transitions preserve scheduleVersion. Expiry adds an immutable interval-closure record; Contract.currentVersion and version listing project the same closure without rewriting approved terms/version source. |
| P2: failed current readiness evidence rolled back maintenance and left an apparently READY candidate | Maintenance materializes REVIEW_REQUIRED with an audit when current evidence guards fail; workers record GUARD_BLOCKED without committing an activation. Material source changes still require a new dossier. |
| P2: maintenance before idempotency lookup caused extra timeline effects on replay, or prevented cached success after later guard failure | A narrow CommandExecutor cache-miss hook retains original key arbitration/current authorization, runs REQUIRES_NEW maintenance only after a cache miss, then reauthorizes. Cached replay returns before maintenance. Invalid key/fingerprint/in-progress checks also precede it. Maintenance commits independently before a new command's ETag check, so 412 does not roll it back. Authorization callbacks hold no outer domain row locks. |
| P2: unsupported CHANGE packet returned 503 before parent/branch authorization, exposing cross-scope existence | Current packet branch and actual parent access are checked before the unavailable-adapter response; create also checks current role/parent first. No fake CHANGE adapter was introduced. |
| P2: newly created lifecycle obligations used the historic Contract maker after relationship reassignment | The factory reads the current locked Relationship maker for each new task. Historic tasks/Revision contributors are preserved. |

The developer additionally identified and root authorized a bounded creation-time missed-deadline correction: a lifecycle task whose operative due has already arrived during catch-up records that actual due in breachedAt/deadline history and one immutable `(obligation_id,due_revision)` breach row/audit. Prospective deadlines do not receive a breach. This is not a general G5 threshold scheduler or closure implementation.

## Independently inspected invariants

Signing input rows and separate verification assessments are immutable. Packet fields are checked against the stored hashed input; exact file manifests are re-read and compared. Actual signed copies must be distinct uploaded CLEAN files staged on the same CONTRACT; authority sources may additionally use only evidence frozen into that contract's own dossier Revision. Artifact ID/hash, private bytes and immutable original artifact association must agree. Historical references are retained, never replaced/deleted on a correction.

The cumulative internal contributor ledger captures every successful Contract evidence uploader and packet submitter, including deleted staging uploads; original contributors/requester and actual pre-migration uploaders are included. Current eligible scoped independent CHECKER guards run before replay and again under source locks for a new decision. Composite business roles do not remove contributor exclusions. Human assessments are not source edits. Signed cancellation requires an independent checker and real evidence; unsigned cancellation requires the current assigned maker. Dossier/Contract/job cancellation commits atomically and preserves history.

Each party's authority interval and known revocation are evaluated at that party's own reported signing instant, with local-date exclusive validTo and no future signing. Human signed-copy PASS attests comparison to the exact artifact, and AUTHORITY_CURRENT evidence must cover both source/check references; CLEAN and maker checkedAt are not independent verification. This follows S016 without inventing additional mandatory EvidenceCheck entries. Beneficiary material representation must match the approved source apart from assessment metadata, blocking S018's changed scope without a free-text legal parser. Applicable prior mandatory codes cannot be omitted; SERVICE_READINESS requires a real human assessment with actual evidence. Server readiness completion is sampled strictly before original local midnight; reported old signing times cannot backdate it.

Existing aggregate paths lock Dossier then Contract, followed by current Relationship/Party and evidence files before contribution/job changes. Upload owns Contract before adding contribution; no ledger-to-parent inversion was found. Party/Relationship locks serialize the material comparison with supported customer updates. HOLD can be raised by a contributor; RELEASE requires another noncontributor checker, actual evidence and current guards. It cannot legalize altered material inputs. Historical reviewer authorization is not arbitrarily revoked because a later role changes; current authorization is required for each new human action.

One timeline authority serves detail, signing maintenance, new time-sensitive commands and workers. First ACTIVE creates one immutable ORIGINAL version/effect, completes the dossier and preserves NOT_STARTED service. Real TERM_REVIEW and CLOSEOUT creation use SQL dedupe identities and complete canonical initial payloads. TERM_REVIEW due is local endExclusive minus 30 calendar dates at 17:00; CLOSEOUT uses five demo working days excluding the end date. Observed RUNNING/SUSPENDED expiry creates a real SYSTEM STOP REQUESTED with requester=null, contributors=[] and actual original artifact provenance; it does not fabricate acknowledgement or STOPPED service. OPEN_ENDED does not expire. Catch-up activation/expiry and required consequences share a transaction.

Actual READY/SCHEDULED Change rows and scheduledChangeId fail closed while the G4 adapter is absent. Qualifying renewal behavior is not implemented or accepted. Durable original EFFECTIVITY intents are available at their actual start/end boundaries and bind packet input/original source/boundary hashes. Per-claim lease tokens, current source checks and conditional completion fence domain effects; lost ownership rolls back effects. Recovery and bounded retry use the existing queue. Failed original jobs expose genuine source/current-guard retry through Job APIs. Version history uses number DESC/id DESC and a signed actor/scope/query-bound cursor, with the append-only closure projection. Ordinary auditor projections redact personal/basis/risk content.

## Exact final build evidence

From isolated `work/be-g3-signing/apps/api`, one final independent command after all repairs:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

BUILD SUCCESS completed `2026-10-17T21:57:22+07:00`; Maven 3.10.0, Oracle JDK25.0.1, Windows11 amd64, javac release25, **73 Java sources**. Test resources were not copied, test sources were not compiled and Surefire reported Tests are skipped. Optional-feed metadata HTTP401 warnings and existing deprecated-API notice were nonblocking.

Packaged JAR SHA-256: `97bfb98cf9e21fbb309fae0201d7189ae1bdf67a813e1e926c0429de48921068` for `apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar`. This report commit changes no application source. Installed JDK is not certification against the specification's exact25.0.4.1 patch.

## Unperformed verification and remaining scope

No tests, product regression execution, HTTP requests, PDF generation, scanner exercise, synthetic business data, SQL/migration execution, Docker changes or application startup were performed for this tranche review. **V008 has not been run here**, and actual bean initialization is unverified. Earlier startup of V001-V007 with zero workflow rows does not verify this newer package or a successful dossier-to-download flow.

No browser/FE integration, authority/SoD negative scenario, late-midnight case, cancellation/hold race, replay/412 transaction scenario, lease/restart/recovery, PDF scan/download, actual lifecycle/task outcome, NFR or backup/restore acceptance is claimed. Public G5 duty/control completion, service acknowledgement, termination, G4 changes/renewals/notices and other remaining operations are outside this source tranche. G3 remains unaccepted as an integrated gate.
