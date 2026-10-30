# C07 operational/toolchain readiness review — 30 October 2026

Bounded independent source/configuration review. No tests, build, application HTTP, startup, infrastructure mutation or Git mutation performed. Only this review file is authored by the reviewer. This is a readiness assessment, not production certification.

## Findings

### TC01 — P2 / blocks exact-baseline reproducibility

A §13/B §2 pin Oracle JDK25.0.4.1. `.github/workflows/backend-build.yml` selects Temurin and `java-version: '25'`, while the recorded local candidate build uses25.0.1. `apps/api/pom.xml` fixes release25, which does not constrain compiler vendor/patch. `scripts/start-backend.ps1` checks major25 only. Existing build evidence remains valid for its actual environment, not the exact canonical runtime.

The official [Oracle JDK25 archive](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html) observed during this review explicitly lists25.0.4.1 Windows x64 ZIP and Linux x64 tar.gz, with checksum links. Thus the pin is not shown to be unavailable by this review. Direct checksum retrieval through web tooling failed; no artifact/checksum was obtained, downloaded or installed. The page is a current upstream observation, not proof of what was downloadable at an earlier build date.

Routine next action: fetch exact official archives and checksum files, verify bytes, unpack into task-owned toolchain directories, record vendor/build/architecture/hash, and use explicit JAVA_HOME/PATH only for the build process. Do not change system Java, stop8081 or overwrite the baseline JAR. Align CI with exact verified Oracle25.0.4.1, preserving skipped tests. No canonical change is needed to implement its existing pin. If actual artifact retrieval or CI availability fails, prepare a concrete alternative baseline patch for owner approval; do not silently choose another vendor/patch.

### TC02 — P2 / blocks stated Maven checksum requirement

`apps/api/.mvn/wrapper/maven-wrapper.properties` pins Maven3.10.0 URL but has no `distributionSha256Sum`. Both `mvnw` and `mvnw.cmd` support checksum verification only when this optional property is set. The SHA256 of the URL used in `mvnw.cmd` for directory naming is not a downloaded-binary integrity check. A §13 requires a wrapper with checksum.

Routine technical repair: obtain official Maven3.10.0 distribution plus independently published authoritative checksum, verify it, calculate/pin exact SHA256 in wrapper properties. Preserve version and wrapper scripts. A warm existing Maven cache does not prove fresh-download checking; fresh-bootstrap execution is deferred to the separate verification stage. This is a concrete missing configuration, not a reason to upgrade Maven. Maven Central directory retrieval through the browser tool failed in this review; no checksum value is asserted.

### TC03 — delivery documentation gap / blocks operational handoff

`apps/api/README.md` explains paired DB/private-file backup only in one paragraph. It still describes107-source20October delivery and says no HTTP workflow has been performed, while later evidence records partial21October smoke and a108-source30October repair. Add a current checkpoint and label historic statements. Preserve old FAIL evidence and exact old/new JAR identities.

No standalone coherent backup/restore or deployment/rollback runbook was found by the bounded tracked-path search for runbook/backup/deploy/restore. Write a procedure covering consistent DB+file snapshot, immutable hashes, ownership, temporary isolated restore targets, missing/corrupt bytes and schema-compatible rollback. Document-only preparation is permitted; executing restore/fault scenarios is deferred. Do not represent an unexecuted procedure as recovery proof.

### TC04 — integration dependency, not a source defect verdict

README explicitly says local launcher does not provide TLS proxy. `application.yml` preserves Secure/HttpOnly/SameSite host-only session cookie, loopback binding and ignored forwarded headers. Browser integration requires FE HTTPS proxy or shared same-origin TLS configuration. Prepare BE proxy/network configuration with explicit trusted boundary and FE review. Do not weaken cookie/CSRF/CORS or indiscriminately trust forwarded IPs to enable login. Browser acceptance belongs to the separate stage.

## Existing controls observed

- CI exists, runs canonical extraction plus derived-resource diff and packages with `maven.test.skip=true`; actions are commit-SHA pinned. There is no need to create a duplicate build pipeline.
- `compose.yml` pins PostgreSQL18.6 and ClamAV1.5.4 by digest and binds published service ports to loopback. This does not establish measured capacity or restore success.
- Launcher validates ownership, secrets, process/JAR identity and distinct ports; starts hidden Java and uses readiness marker/listener rather than HTTP test. Config default DB password is development-only; the guarded launcher rejects invalid placeholders. Direct manual/Compose setup still requires deliberate credentials. No external deployment profile is certified by this review.
- `ClamAvScanner` requests and parses engine/signature version. Signature freshness/outage behavior and real job timings still require separately authorized verification.
- Flyway validate-on-migrate and bounded datasource pool are configured. V001–V013 must not be rewritten to simplify deployment.

## Recommended sequence

1. Commit/read-review the current documentation baseline coherently; reconcile C01 statuses.
2. Fix TC02 and provision exact isolated JDK for TC01 through a dedicated implementation task and separate review; skipped-test build only when source/config changes justify it. Keep candidate and baseline packages distinct.
3. Update current API handoff and produce paired-backup/restore/rollback procedure, with unknown deployment values explicit.
4. Prepare TLS integration contract with FE; collect counterpart acknowledgement before shared-config integration. No unsolicited external message is sent.
5. Hand fresh-bootstrap/restore/browser/security/NFR execution to the separately approved verification phase. Keep NOT RUN statuses.

No owner product decision is presently required merely to implement the existing pins, checksum requirement or runbook. A decision becomes necessary if exact artifacts cannot actually be retrieved/used, an alternative canonical runtime is proposed, or an external deployment/product boundary is changed.

## Follow-up read-only artifact validation

At root's request, actual HTTP HEAD and checksum GET through PowerShell succeeded after the browser parser failed. No binary was downloaded, installed or executed. These observations supersede the earlier retrieval-tool limitation only:

| Official artifact URL | Actual HEAD | Official checksum GET |
|---|---|---|
| `https://download.oracle.com/java/25/archive/jdk-25.0.4.1_windows-x64_bin.zip` | 200, application/zip,215813704 bytes | Same URL + `.sha256`:200, `0e9726dc51724f262a32c796705abfe49e41574c0b134827bc72d6cef7a36ff4` |
| `https://download.oracle.com/java/25/archive/jdk-25.0.4.1_linux-x64_bin.tar.gz` | 200, application/x-gzip,216995794 bytes | Same URL + `.sha256`:200, `dd7e7f51d23bcffe85f1a439577ce1c0190a33417b34afdb338f9b3897bdeaec` |
| `https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.10.0/apache-maven-3.10.0-bin.zip` | 200, application/zip,10102082 bytes | Same URL + `.sha512`:200, `22d31676d5b92ed53308c19ecded3245d0efd0cddfec51ec7bab62f4f13ad70ddddceff8e62940e0e8e2421f3cde8106737f4becec0aa034ffeff2a754e88337` |

Maven same URL + `.sha256` actually returned404. Developer should verify downloaded ZIP against the official SHA512 first, then derive its SHA256 for `distributionSha256Sum`. HEAD and published checksums establish retrieval readiness, not that a downloaded binary has passed integrity verification. Root received these exact URLs/metadata for separate developer implementation.

## Independent repair verdict — exact tooling commit2487adb

**Bounded source/configuration GO** for `2487adb9ea26b1020c9a448ded56be77c2e9e87d`. Developer and reviewer are separate. Reviewed exact two-file Git diff and developer evidence; independently hashed retained local archives without executing them.

- Maven ZIP actual SHA512 exactly matches the official value above; actual SHA256 is `1f6d9909266510f039f59aa0e13dcd2c66da85f043e56276e41f2918f8bddaff`, equal to new wrapper `distributionSha256Sum`. Both existing wrapper implementations consume this property; URL/version unchanged. TC02 configuration gap is source-closed, fresh-download bootstrap remains NOT RUN.
- Retained Windows JDK ZIP actual SHA256 exactly matches the official value above. Extracted java.exe SHA256 independently equals `65c411a0be023cbe3e96d1f165d4652593b6c62c189eb742efd6fd097e42cf71`; extracted `release` identifies Oracle Corporation,25.0.4.1 and x86_64. Developer's explicit java-version output is attributed provisioning evidence; reviewer did not execute Java.
- CI downloads exact official Linux archive with HTTPS-only curl and fail/retry flags; pinned SHA256 agrees with independently retrieved official metadata. Hash is checked before extraction. Python3.13 was configured by the retained setup-python step; `tarfile.extractall(filter='data')` extracts into a new RUNNER_TEMP directory. Shell heredoc indentation is valid after YAML block indentation removal. Expected archive root is `jdk-25.0.4.1`; Java executable invocation fails the step if absent. JAVA_HOME and bin PATH are exported through GITHUB_ENV/GITHUB_PATH for subsequent Maven step; no global runner system installation required.
- Existing canonical extraction/diff checks and exact skipped-test Maven command are retained. No tests introduced. Removal of Maven cache changes build speed only, not correctness.

No concrete blocking source finding identified in this tooling repair. TC01 configuration/vendor provisioning gap is source-closed; exact-JDK candidate build is the next operational evidence step. Linux archive extraction/CI job execution and fresh wrapper bootstrap remain NOT RUN. This verdict permits proceeding with the separately authorized skipped-test build, not HTTP/business verification or deployment. No source-contract/schema/business behavior changed by this tooling commit.

## Bounded operational-document review

Read current root-authored `backend-recovery-runbook.md` and `backend-fe-integration-handoff.md` independently. No concrete blocking operational error identified in this bounded reading. The runbook explicitly quiesces HTTP plus workers/schedulers, pairs DB and private bytes, verifies manifests, isolates restore targets, checks due-job chronology and requires current-schema compatibility before JAR rollback. It does not provide or claim executed recovery proof. Deployment-specific backup tooling, proxy configuration and execution remain future work.

The FE handoff preserves same-origin HTTPS, Secure cookies/CSRF, current authorization, ETag/idempotency conflict handling, private file representation and truthful async-job/business states. External acknowledgement, provider/consumer/browser execution and candidate HTTP retests remain PENDING/NOT RUN. Caller counts are attributed to the separate caller review; this reviewer did not independently rederive every24-email/17-lag operation count. This verdict does not close C01's full inventory/action/target review or application business acceptance.
