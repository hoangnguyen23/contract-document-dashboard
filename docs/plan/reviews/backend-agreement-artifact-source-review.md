# Independent original agreement and artifact source review

Review date: 17 October 2026, Asia/Ho_Chi_Minh. The reviewer did not implement or repair application code. A second independent source reviewer corroborated lease, transaction, uniqueness and retry checks.

## Exact verdict and scope

**GO for the bounded original agreement/PDF source tranche at `ac85ecee4ad6ac668e1cdf57b7257b34fe0a2062`.** Initial implementation `ddd7d8716805f589957456171cbef8ff243273ac` and subsequent repairs were independently inspected. The findings below are closed. No unresolved P1/P2 source blocker was found within this scope. The final independent package passed with every test resource/compilation/execution step skipped.

This supersedes the missing-original-port limitation in the earlier approval review only for this newer source. DOSSIER final APPROVE now has a real transactional OriginalAgreementPort and durable PDF job. Four Round routes remain partial because CHANGE/CONTROL_REQUEST adapters are absent. Contract commands, signing, readiness, timeline/effectivity and ContractVersion history remain absent; two GET routes expose the PREPARED/artifact tranche. This is permission to continue sequential Backend implementation, not full G3 or integrated G1/G2 acceptance.

Authorities: A1.2.0 §§3, 5–6, 10–11, 14 and BR/AC-020/021/053; B1.2.0 §§3–4, 6–7; closed Contract/File/Job schemas; canonical seed hash conventions and original agreement profiles. Canonical A/B and extracted contracts were unchanged by this tranche/review.

## Findings closed by the developer

| Finding group | Concrete problem | Closure in reviewed source |
|---|---|---|
| P2 — persisted Job error code | Ordinary scanner/renderer/storage failure persisted SERVICE_UNAVAILABLE into Job.lastErrorCode. That HTTP Problem code is not in the canonical Job enum and made job responses fail validation. | `920ec9a`: actual scanner failures use SCANNER_UNAVAILABLE; source/render/private-byte integrity failures use STORAGE_INTEGRITY. No new wire enum was introduced. |
| P2 — PDF page clipping | Default PDPage is LETTER, height 792pt, but text started at baseline 785pt. Pinned Noto Sans Ẫ in the first heading has ymax 1048/1000; at 9pt its top was 794.432pt and therefore outside the page. Other Vietnamese accents also exceeded 792pt. | `1e8efec`: both page creation sites use explicit A4 and baseline pageHeight minus 50pt margin minus 12pt ascent allowance. Horizontal wrapping stays inside the page and new pages retain the margin. |
| P2 — generated timestamp | File.generatedAt was copied from final approval/intent time, even when the worker rendered much later. | `1e8efec`: actual generation completion time is captured from the server clock immediately after render and passed to generated File registration. The frozen intent date remains separate deterministic PDF metadata. |
| P2 — composite roles and action hints | An AUDITOR label vetoed otherwise authorized MAKER/CHECKER users. Removing that veto alone allowed an unassigned MAKER+AUDITOR to use auditor read access as maker retry authority. Flat unavailable hints also exposed checker actions to makers and unregistered Contract commands. | `1e8efec` plus `0aa5717` and `af81b7c`: retry requires current source scope and MAKER/CHECKER; when CHECKER is absent, CustomerAccess.authorizeAssignedMaker checks the actual current relationship assignment before replay and again for the new retry. Ordinary AUDITOR cannot mutate. Contract action sets are empty because no Contract command handler is registered in this tranche. |
| P2 — blanket sensitive classification | Marking every original SERVICE_TERMS PDF sensitive blocked baseline business makers/reviewers without SENSITIVE_READ from downloading necessary synthetic business facts, contrary to canonical original profiles. | `ac85ece`: generated source-only original PDF uses sensitive=false, matching SIGNING_ORIGINAL/ACTIVE_AGREEMENT. The renderer embeds facts and evidence references, not IDENTITY_IMAGE bytes. Existing parent authorization and the independent ordinary-auditor FULL restriction remain intact. Raw sensitive evidence keeps its original classification. |

The clipping finding was established by reading the pinned TTF tables and PDFBox constructor bytecode, without rendering a PDF. No request, scanner scenario, race or runtime assertion was executed to establish these findings or repairs.

## Real original agreement boundary

OriginalAgreementService implements OriginalAgreementPort with Propagation.MANDATORY. Final approval enters through CommandExecutor's transaction, with the actual dossier/Round locked and current approval guards already checked. The port validates the dossier/Round/Revision binding, requires the supported frozen template/bundle and creates the real PREPARED Contract, immutable original source row, stable ARTIFACT_PDF intent and audit in that same transaction. The approval service then records the dossier/round decision and idempotency result under the existing precommit response validation. Exceptions roll back the whole decision/intent/audit/cache unit.

V007 enforces one original per dossier and revision, one job per original, one artifact per Contract and unique artifact file. Original source and artifact history reject updates/deletes. These constraints complement the dossier lock and existing unique step decision/open-round guards; a controller-only precheck is not the sole duplicate prevention.

The full source hash binds dossier/Round identity, the exact immutable Revision, frozen Party/contact/contactVersion/evidence/config/template/bundle metadata, latest approved mandatory checks and the resource/font manifest. It is not merely the dossier factsHash, which intentionally omits several rendering inputs. Rendering never loads current customer/policy facts to replace the approved source.

The new Contract starts with approvedTerms and approvedRevisionId, scheduleVersion=1, PREPARED/PENDING/NOT_STARTED, and null currentVersion/artifact/signing/effectivity/readiness timestamps. Artifact completion changes only its artifact binding/resource version. No effective ContractVersion, signing verification, readiness or ACTIVE success is fabricated.

## PDF, resources and private File

AgreementRenderer uses actual PDFBox 3.0.6 text rendering and embedded Noto Sans Regular. It appends the frozen source, metadata and approved checks to the authored synthetic BASE-VA-1/TERMS-VA-1 text and signature lines. It wraps by measured glyph width, iterates code points, paginates within explicit margins and fails on unsupported glyphs instead of silently dropping approved text. No OS font lookup or runtime font download is required. The result is an unsigned product template, not an issued bank document or legal certification.

Both font assets and the complete copyright/SIL OFL1.1 text are packaged. Renderer construction checks their SHA-256 pins; the source manifest also binds both template resources and renderer version. Local resource hashes inspected:

| Resource | SHA-256 |
|---|---|
| NotoSans-Regular.ttf | `b85c38ecea8a7cfb39c24e395a4007474fa5a4fc864f6ee33309eb4948d232d5` |
| NotoSans-Bold.ttf | `c976e4b1b99edc88775377fcc21692ca4bfa46b6d6ca6522bfda505b28ff9d6a` |
| OFL.txt | `0dab92d0544f7b233403f14b84a663bdbfa746982eda629e7f4f9ffe1b036feb` |
| BASE-VA-1.txt | `dab26840fd2108205888cb63484e9411ece00bd094fc65f4e7d57e6417afd509` |
| TERMS-VA-1.txt | `0ceee8eb46b6597cbcc48f292169c3e0ae1046c98bb540c7a1586cfac0a7f531` |

The manifest is rechecked before rendering. PDF document dates/IDs derive from the immutable source rather than the workstation clock/customer state; File.generatedAt separately records actual server rendering completion. A source/template mismatch fails closed. This inspection does not certify visual output, accessibility, byte determinism across library/runtime changes or complete glyph coverage for every schema-valid input.

The worker performs a real ClamAV INSTREAM scan before publishing the generated File. Scanner outage cannot yield CLEAN; infected output cannot be registered. GeneratedArtifactFiles joins the caller transaction with MANDATORY and writes CONTRACT/SERVICE_TERMS, GENERATED, uploadedBy=null, NOT_CHECKED human status, actual byte hash/size, actual scanner versions and a retained historical association. SERVICE_TERMS is the semantic original-PDF purpose used by the canonical generated approved-original.pdf profiles; CONTRACT_ARTIFACT would be an invented enum.

PrivateBlobStore.writePending finalizes opaque private bytes before any usable reference and retains its guard through the reference transaction's commit/rollback. A rollback/lost lease can leave an unreferenced orphan for the existing guarded 24-hour reconciliation; the code never compensates by deleting referenced bytes. Normal File download continues to enforce current parent scope, sensitive/auditor grants and byte integrity. CLEAN does not imply human artifact/sign verification.

## Durable job, retry and atomic publication

The PDF worker has its own bounded single execution slot/pool and renews leases every 20 seconds through the existing 60-second queue protocol. Each claim supplies a fresh UUID lease owner, avoiding stale-attempt ABA when the same process reclaims a job. Rendering, scanning and private byte preparation happen outside domain transactions.

Final publication locks the Contract, rechecks current PREPARED state, absence of a prior artifact, current job/revision binding and the full immutable source hash, then conditionally succeeds the still-owned unexpired job before domain writes. A false fence produces no File/Contract/audit effects. The same TransactionTemplate commits generated File/blob/association, immutable artifact row, Contract approvedArtifactId/hash, Job SUCCEEDED/resultFileId and audit. A later registration/update failure rolls all of these back. Job wire snapshots remain owned by the V003 SQL trigger.

Failed work uses the queue's bounded attempt/backoff and crash-lease recovery. Current cancelled/obsolete parent guards produce cancellation rather than publishing old bytes. Manual ARTIFACT_PDF retry is implemented in the existing commandJob path: current authorization precedes replay, If-Match is required, and new retry locks Contract before Job, checks FAILED/PREPARED/current intent/null artifact/full source hash, resets the same job attempts and audits the supplied reason. It neither creates another Contract nor changes the semantic dedupe/source identity. Other unimplemented job kinds still fail closed.

## GET coverage and integration boundary

GET /api/v1/contracts and GET /api/v1/contracts/{id} use current relationship/branch access. Lists apply scope and current maker assignment before pagination and share canonical query validation/cursor/order behavior. expiring30 is ACTIVE plus FIXED plus local endExclusive strictly after today and at most today+30; PREPARED rows are not treated as effective contracts. The DTO exposes approved terms and artifact status, not the internal unredacted source envelope. Sensitive content remains behind File access rules.

The controller registers no Contract commands or version-history route. Both action hint sets are empty accordingly. The canonical advanceTimeline/readiness/signing behavior must be added before claiming complete Contract detail/time-sensitive workflow acceptance. Static dependency inspection found no eager bean cycle in these additions; actual Spring startup was not observed here.

Current operation matrix totals 85: 45 implemented handler paths, 5 partial and 35 absent. The four Round routes remain partial for non-DOSSIER targets; commandJob remains partial but now covers FILE_SCAN and original ARTIFACT_PDF retry. The prior missing-port/FILE_SCAN-only descriptions belong to earlier source snapshots and do not describe this commit.

## Independent package evidence and limits

Final reviewed application source: `ac85ecee4ad6ac668e1cdf57b7257b34fe0a2062`.

PowerShell command from apps/api:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

BUILD SUCCESS completed `2026-10-17T20:21:21+07:00`, Maven 3.10.0, Oracle JDK 25.0.1, Windows 11 amd64. Javac compiled 64 sources with release 25. Test resources were not copied, test sources were not compiled, and Surefire reported Tests are skipped. Nonblocking optional-feed metadata HTTP 401 warnings and the existing deprecated-API notice did not prevent packaging.

Packaged JAR SHA-256: `056256308037ed47c8dff01d30b553d510a5667a857331e2c0a1d2621dd9acca` for apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar. This report-only commit changes no packaged application source. Concurrent root delivery-document edits are preserved separately.

No application tests, product regression execution, HTTP business requests, manual PDF rendering, scanner exercise, Docker changes, SQL/migration execution, business-data mutation or application startup was performed during this review. V006/V007 execution, actual DI/startup, rendering/scan/download behavior, failure recovery/races, signing/readiness/effectivity and FE integration remain unverified. The separately recorded Docker Desktop host failure was not repaired or reset here. No integrated G1/G2/G3 gate is declared accepted.

