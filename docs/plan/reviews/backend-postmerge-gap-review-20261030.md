# Post-merge Backend delivery gap review — 30 October 2026

Scope: read-only configuration/source/document audit of reviewed delivery merged by owner approval as dev54af6f1. No tests, build, HTTP business request, runtime/process/container/data mutation or source edit. Only this report is authored. This is not another full domain implementation review; existing module/current crossdomain reports retain their scope.

## RD01 — P2, local launcher does not select one pinned JDK for build and runtime

Locations: `scripts/start-backend.ps1:75–94`; `apps/api/README.md:11–15`. Canonical basis: A§13/B§2 exact Oracle25.0.4.1 and C07 same baseline/reproducibility.

The launcher selects `java.exe` using PATH, accepts any Java25 patch, resolves java.home and launches that runtime. It then invokes `mvnw.cmd` without setting JAVA_HOME/PATH to that same resolved JDK. Maven's wrapper prefers JAVA_HOME when supplied, so the runtime probe and actual compiler may select different installed JDKs. The newly provisioned exact Oracle JDK and exact CI do not close this local-launcher selection gap. The documented default still tells users only to install a Java25 JDK on PATH.

Routine repair: allow an explicit JDK home, or deliberately honor a documented JAVA_HOME with strict canonical vendor/version validation; use that same resolved home for the scoped Maven process and launched executable. Preserve existing global environment and running process. Record actual vendor/version/home in process metadata and make already-running reuse compare the requested toolchain identity. Do not stop the old process to enforce the new setting; mismatched identity should give an actionable refusal. No canonical, role, migration or public API change is required. The exact JDK is already available, so no owner version exception is needed.

## RD02 — delivery preparation gap, concrete HTTPS consumption recipe still missing

Locations: `apps/api/README.md:35`, `docs/plan/backend-fe-integration-handoff.md` HTTPS entry, A§13/B§1.1. Current code keeps Secure host-only cookies and server.forward-headers-strategy=none; local API8081 is plain loopback and launcher intentionally does not supply a TLS proxy. Handoff correctly states FE HTTPS proxy or shared TLS proxy is required, but the tracked package contains no concrete TLS runtime profile/recipe or shared proxy preparation for the counterpart to consume.

This is not evidence that authentication source is missing, and no new proxy product is mandatory. Spring's native SSL configuration already exists through Boot environment/property support. A small documented external/private native-SSL profile (keystore location/type/password supplied privately, loopback bind and explicit port) plus a clear FE HTTPS-origin/proxy contract is an acceptable BE preparation route. Alternatively provide a root proxy template within existing ownership. Keep Secure cookie/CSRF, private API binding and current source-IP trust boundary. Do not add CORS wildcard, insecure cookie toggles or trust all forwarded headers. Do not create/activate certificates or start a new listener under this audit. Browser execution and counterpart FE code remain a separate later phase.

## RD03 — P3 documentation provenance wording

`apps/api/README.md:3` says “The repaired candidate has source/build/startup evidence,” while current candidate2487adb/JAR99e695 has never started. The central checkpoint correctly distinguishes prior81c3d7f/JARb29274 startup. Make the README lead equally precise, e.g. current candidate source/build evidence; prior baseline startup evidence. Its historical implementation section also states local25.0.1/no workflow at its old date; keep a labelled historical heading instead of allowing those lines to appear as live toolchain facts.

Current-status/ledger still say PR11 owner merge authorization pending. Owner has now approved and GitHub reports merge54af6f1; update the operational checkpoint and ledger with the approval and merge SHA while preserving test deferral and prior evidence. This is routine documentation reconciliation, not a canonical product change.

## Items not reopened as development gaps

- CI exact Oracle artifact/checksum and Maven distributionSha256Sum are delivered at2487adb and independently reviewed. Actual Linux CI/fresh bootstrap execution remains later evidence, not a missing build pipeline.
- Coherent DB+private-byte backup/restore/rollback runbook is delivered and independently read. A backup/restore automation framework is not required by the source contract; executing recovery is deferred testing. Do not create scripts solely to claim more completion.
-85 operations,14 command action sets and current producer/consumer reviews exist. This bounded audit establishes no new domain API/transaction defect and cannot certify every guard. No remaining concrete business/schema decision is requested.
-FE acknowledgement, generated client/browser tests, negative authorization, PDF/signing/lease recovery, restore and measured NFR remain required later acceptance; do not count them as missing BE source implementations or execute them now.

## Recommended next tranche

Implement RD01 with a separate developer and review; close RD02 with a minimal concrete secure runtime recipe/configuration and FE-facing connection contract; reconcile RD03/current merge status. Build only if application/tooling changes justify it, always with all test phases skipped. Preserve current8081 and all data/volumes. These are within existing owner-approved Backend development scope and do not need a new product decision. After their independent review, report development readiness with exact source/config/build identities and explicit unexecuted acceptance rather than an unconditional100% correctness claim.

## Independent RD01 repair verdict — exact commit5e58768

**Bounded source GO** for `5e5876832c1dd73479e0100a0dc33587f158d30e`. Developer and reviewer are separate. Inspected exact one-script commit, its enclosing control flow and backend-runtime metadata/cleanup helpers. Developer's static AST evidence is attributed; reviewer did not execute the launcher, Java probe, parser, Maven, test or runtime operation.

- Optional `-JavaHome` is a separate parameter from local `resolvedJavaHome`; PowerShell's case-insensitive variable names do not overwrite the requested path. Both fallback PATH discovery and explicit absolute home are checked against actual probe properties: Oracle Corporation,25.0.4.1 and verified build25.0.4.1+1-LTS-5. Explicit requested/probed homes must agree, and the resolved home must contain both java.exe and javac.exe.
- Before Maven, the launcher saves process JAVA_HOME/PATH and sets both to the same resolved JDK used by its explicit Java executable. Existing finally restores original values on success/failure, including null/unset values. System/user environment remains untouched. No changed callback bypasses that finally.
- Probe/pin validation occurs before managed-process reuse. Existing metadata.javaPath is a schema1 property already required/consumed by Get-ManagedBackendProcess; the new comparison does not introduce a missing-property assumption for valid old metadata. Selected-executable mismatch rejects without killing/replacing the existing process. Existing retained-handle/PID/start-time/JAR identity checks remain intact.
- On mismatch before Start-Process, startedProcess remains null; catch cleanup cannot kill the old managedProcess. Its handle is disposed by the reuse block's finally. New-process cleanup continues to own only the exact newly created retained handle.
- No public API/schema/role/migration/Java domain change. Existing SkipBuild, container ownership/ports/storage, secret handling and no-test Maven command remain unchanged. The former compile/runtime selection mismatch is closed in source; actual Windows launcher behavior remains NOT RUN. This source-only script change does not require claiming a new application JAR or startup result.

RD01 is source-closed in this bounded review. RD02 secure connection preparation and RD03 documentation reconciliation remain separately tracked; future acceptance and FE ownership remain unchanged.

## Independent RD02 preparation verdict — exact commit7bfbeaa

**Bounded configuration/documentation GO** for `7bfbeaa4714eb2d773a56b27a069f1b778c46afd`. Inspected optional `infra/backend-local-tls.properties`, its activation recipe and application defaults. It is outside automatically loaded resources and only activates through an explicit additional-location URI. Required DB/user/password/cursor/storage/scanner/keystore placeholders avoid silently selecting the existing data namespace. Loopback binding and ignored forwarded headers preserve the intended local trust boundary; no public CORS/cookie/CSRF relaxation is introduced.

The certificate recipe uses the verified JDK path, PKCS12, localhost/loopback SAN and env-supplied passwords, explicitly requires a new owned target directory, restricts private material and avoids global trust changes. It does not automatically generate or activate a certificate. The future direct Java invocation is clearly separate from the guarded launcher, requires its own process/log/ownership manifest and must not overwrite main runtime metadata or use8081/live data. Application workers remain active only if an operator later launches the separately authorized environment; the document does not present TLS as disabling business effects.

FE boundary correctly uses same-origin FE HTTPS `/api/v1` proxy with explicit backend certificate trust. Direct cross-origin calls or disabled TLS verification are not offered as workarounds. This delivers a concrete BE-native-SSL recipe without adding a proxy dependency or editing FE-owned source.

RD02 preparation is closed within this bounded configuration review. Certificate creation, actual property loading/TLS handshake, HTTPS process startup, browser/session flow and FE acknowledgement are NOT RUN/PENDING, as intended. No new build is needed to certify this non-packaged explicit template, and none was run by the reviewer. RD03 live status/README lead reconciliation is still required separately; the underlying candidate JAR remains2487adb/JAR99e695 with no new startup observation.

## RD03 reconciliation follow-up

Read the updated API README lead/setup and root-authored current status/ledger after the two fixes. README now assigns source/build to current candidate and startup only to the prior baseline; local setup names exact Oracle vendor/version/build and explicit JavaHome path/reuse behavior. Current status and ledger record owner approval and PR11 merge54af6f1, retaining test deferral and undeployed candidate identity. The follow-up branch is explicitly rooted at that merge. This closes the identified provenance ambiguity; no new executed acceptance is claimed.

RD01/RD02/RD03 have no remaining concrete source/configuration/documentation blocker within this review. Final delivery should record its own follow-up HEAD separately from the unchanged compiled application/JAR2487adb/99e695, and keep the original prior runtime81c3d7f/b29274 identity. Owner-approved integration order is an execution exception, not a change to canonical business rules or an executed G3–G6 verdict.
