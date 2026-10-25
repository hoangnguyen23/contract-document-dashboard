# Backend operational startup evidence

Latest result, 17 October 2026 local time: integrated source `3577f5ce` built successfully and the reviewed launcher started the real API JVM as PID `23100`, listening on `127.0.0.1:8081`. PostgreSQL on `127.0.0.1:56432` and ClamAV on `127.0.0.1:3310` are healthy. The final section records the resolved Windows blockers and precise evidence. This is operational startup readiness, not business/FE acceptance.

The initial observations below were recorded on 11 October 2026, with a read-only snapshot at `2026-10-11T16:40:15Z` (`Asia/Ho_Chi_Minh` local time: 23:40). The later 17 October continuation is recorded at the end of this report and supersedes the initial process/port status. This records local process startup, migrations and infrastructure readiness. It does not replace the [source review](backend-foundation-follow-up.md) or establish G1/G2 business acceptance.

## Source and executable provenance

- Repository: `work/project-document-publish`, source/document HEAD `8dee3582c33390c444326b4236ff5b33557808bd`.
- Application source last changed at `f9a495a`; the existing packaged JAR was used without another build or source modification during this startup task.
- JAR: `apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar`.
- JAR SHA-256: `711B3418DDCEA8A763335F8EC83A9929739894F7FA25835BEEC6C4D02D500CE9`.
- Actual Java: Oracle JDK `25.0.1+8-LTS-27`; Spring Boot `4.1.1`, embedded Tomcat `11.0.24`. This workstation is not the specification's exact Java patch baseline.
- Docker Engine `29.8.0`, Docker Compose `v5.5.1`.

## Isolated local infrastructure

The existing native PostgreSQL process used port `5432`; `MTAgentService` used port `8080`; unrelated container `signvoice_mongodb` used port `27017`. These were preserved. The project was started under Compose project name `contract-governance` using `compose.yml` and the ignored local override `var/operations/compose-local.override.yml`.

| Component | Actual binding | Observed status |
| --- | --- | --- |
| PostgreSQL container `contract-governance-postgres-1` | `127.0.0.1:55432` → container `5432` | Healthy; PostgreSQL `18.6` |
| ClamAV container `contract-governance-scanner-1` | `127.0.0.1:3310` → container `3310` | Healthy; ClamAV `1.5.4`; TCP `PING` returned `PONG` |
| API Java process | `127.0.0.1:8081`, PID `32148` | Running; actual TCP listener matched the PID |

The images used the repository's pinned digests:

```text
postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336
clamav/clamav:1.5.4@sha256:ebec5bc138401b36ae987caa1a3fa3c3b2a21ed3d51f0bfa5852825e663e67b0
```

Scanner `VERSION` reported `ClamAV 1.5.4/28150/Sun Oct 11 06:26:01 2026`. Its startup log reported an activated signature database containing 3,628,164 signatures. Only protocol health commands were sent; no file scan was performed.

## Startup failure and local resolution

The first API process, PID `8780`, exited before migrations. Java inherited workstation timezone `Asia/Saigon`. PostgreSQL rejected the JDBC connection with:

```text
SessionFilter → SessionService → JdbcTemplate → flywayInitializer
FlywaySqlUnableToConnectToDbException
PSQLException: FATAL: invalid value for parameter "TimeZone": "Asia/Saigon"
```

A read-only timezone query confirmed that the pinned PostgreSQL image recognizes `Asia/Ho_Chi_Minh` and `UTC`, but does not expose `Asia/Saigon`. The approved restart supplied JVM argument `-Duser.timezone=UTC` before `-jar`; this is limited to the application process. Explicit Vietnam business-calendar calculations were not changed, and neither global Java settings nor PostgreSQL configuration was altered.

The restarted API connected successfully. Flyway validated and applied all three migrations against the previously empty project database:

| Version | Description | Persisted success |
| --- | --- | --- |
| `001` | platform | true |
| `002` | identity customer | true |
| `003` | dossier files | true |

Tomcat started at `2026-10-11T16:37:19.996Z`; application startup completed at `16:37:20.038Z` in 3.785 seconds. A later log read showed no post-startup `ERROR`/exception/failure entry, and stderr remained empty. This confirms the observed dependency injection, migration SQL and startup configuration path for this executable.

## Local configuration and process handoff

The pre-existing `.gitignore` was checked before local state writes. `.env` did not exist and was created once with cryptographically random database, cursor and demo-password secrets. Secret values were not included in commands, reports or Git. Existing `.env` contents must be preserved on subsequent starts.

The opt-in `demo` profile was active. A read-only count showed 11 synthetic User rows, 2 Branch rows, 1 Party and 1 Relationship; no File metadata and no jobs existed. These are startup seed counts, not business workflow evidence. No direct business-data SQL mutation was made.

Process-local settings pointed JDBC at `127.0.0.1:55432`, scanner at `127.0.0.1:3310`, API at port `8081`, and private file storage at the absolute repository path `var/private-files`. `SERVER_ADDRESS=127.0.0.1` produced the confirmed loopback listener. Java was launched with `Start-Process -WindowStyle Hidden`.

Sanitized handoff state is in ignored files:

- `var/operations/api-process.json`: current PID, executable/JAR paths and hash, timezone, addresses, storage and log paths; no credentials.
- `var/operations/api-attempt-01.json`: first failed launch metadata.
- `var/operations/api-startup.stdout.log` and `.stderr.log`: first attempt.
- `var/operations/api-startup-02.stdout.log` and `.stderr.log`: current process.

The current API and project containers were left running for continuation. A future launcher must verify the project executable/JAR identity before stopping an adopted process. It must not act on unrelated services or delete volumes.

Spring Boot also initialized an unused default `inMemoryUserDetailsManager` and logged a generated security password. All reported output redacted that value; raw ignored startup logs remain local. The session-based security chain is present, but no HTTP authentication claim is made here. Disabling the unused default-user auto-configuration is a follow-up source cleanup.

## Verification boundaries

No tests were created or run. No HTTP acceptance request, session/login scenario, file upload/download, EICAR or synthetic scan scenario, concurrent business operation, recovery exercise, FE integration or performance measurement was performed. The read-only database diagnostics and ClamAV health protocol confirm infrastructure/startup readiness only. G1/G2 and later gate acceptance remain unclaimed; [the operation coverage matrix](../backend-operation-coverage.md) still governs implementation scope.

## Integrated continuation — 17 October 2026 local time

The reviewed source at `3b6f5fe0c4c218b0ef6d0dd69309057e1a75e3d7` includes storage maintenance commit `0d1e672302da1305b6ffba4119f3cd10fb914b1c` and launcher commit `dd1f8d5cd02c3bf9e25cd174b66b500518ef0f5b`. Static review closed the earlier Java discovery, pre-runner readiness and retained-volume ownership findings. The launcher selects one executable, checks the actual child identity, waits for a unique launch log's post-runner `ApplicationReadyEvent` marker, and guards project containers plus labeled/unlabeled named-volume collisions before Compose writes. Storage review verified both original/redacted references, short database table locks, separate writer guards, 24-hour age and identity checks, and bounded nonrecursive enumeration. These were source conclusions; the subsequent real startup exposed Windows provider/runtime defects described below.

The fresh process inventory found old PID `32148` absent and API port `8081` free. Existing project containers were stopped following the workstation interruption, with their original pinned images, primary-repository labels and persistent volumes intact. Unrelated services were left untouched. No PID was killed.

Canonical extraction verified six frozen A/B `1.1.0` blocks and produced no derived-resource differences. From `apps/api` the integrated build ran:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

Maven `3.10.0` and Oracle JDK `25.0.1` compiled 46 Java source files with `release 25`. `BUILD SUCCESS` completed at `2026-10-17T01:07:11+07:00`; Maven explicitly skipped test resources, test compilation and tests. The packaged JAR SHA-256 was `764F37F531A4486C82C9C624D59FCA7C628C7219EE0CE9F3CB35662713018926`.

The first real launcher invocation used `-Demo -SkipBuild`. Docker could not publish PostgreSQL on `55432`: Windows' current TCP exclusion range `55372–55471` includes that port. This was diagnosed through read-only `netsh ... show excludedportrange`, without changing system reservations. Configuration-only retry used `-DatabasePort 56432`, retaining the same database volume, project and secrets.

PostgreSQL then became healthy at `127.0.0.1:56432`; ClamAV remained healthy at `127.0.0.1:3310`. Flyway connected, validated all three migrations and found schema `003` already current. Persisted migration success flags remained true, with zero jobs and zero private-file metadata rows. The `.env` SHA comparison across launch returned unchanged; its last-write time remained 11 October. No secret value was reported.

That API launch did not become ready. It revealed two implementation blockers:

- Oracle's `javapath/java.exe` spawned the actual JVM: launcher metadata recorded shim PID `35624`, while Spring reported actual JVM PID `28208`. Both subsequently exited. Tracking the shim cannot safely establish the API's readiness/stop identity; executable discovery must resolve the real JDK binary before the long-running launch.
- `DossierService → FileService → PrivateBlobStore` failed at bean construction with `IllegalStateException: Private file store unavailable or unsafe`. Read-only inspection of the installed `sun.nio.fs.WindowsFileAttributes` bytecode confirmed `fileKey()` returns null. The new constructor rejected that null for every ordinary directory, and therefore blocked Windows startup. A real Windows file-identity implementation is required rather than accepting shared null identities or weakening symlink guards.

Failure reporting also threw a Windows sharing violation while `File.ReadAllLines` opened the live redirected stdout log. It masked the initiating launcher exception and could bypass the retained-process cleanup. Both failure diagnostics and the readiness poll's similar `ReadAllText` pattern need a shared-read stream, with cleanup protected from diagnostic failures. Shared-handle read-only inspection recovered the sanitized application exception chain; no application code was edited by the reviewer.

The failed launch's unique logs are `api-11947f52287e483db25485df82bb1bc8.stdout.log` and `.stderr.log` in `var/local-runtime`. At that failed attempt there was no API listener on `8081`, although both project infrastructure containers were healthy. The Java/Windows fixes were sent to their owners for repair and independent re-review. No business request, test or file-deletion scenario was run, and operational readiness was not claimed for that revision. The subsequent successful launch replaced `var/local-runtime/api-process.json` with the current process metadata below.

## Resolved Windows startup — 17 October 2026 local time

Final integrated source HEAD: `3577f5ce75a75a2384b531f1350328c495cfac63`. Independent review of storage fix `76a98652aed7553793f39919ef317b5f8cc81c4e` and launcher fix `bbb87ebfd56cfe4f83a85db18143694b9a5cb6b7` found no remaining concrete P1/P2 blocker in their startup scope.

The storage adapter now obtains Windows identity through a read-only Unicode `CreateFile` handle with `OPEN_EXISTING`, `FILE_READ_ATTRIBUTES`, no-reparse and directory flags. It checks handle-level disk/type attributes, rejects all reparse points, reads volume serial plus the 128-bit file ID into an immutable value, and closes the native handle in `finally`. Other providers still require a nonnull stable file key. Both packaged `jna` and `jna-platform` dependencies are version `5.19.1`; there is no null-identity equality fallback. Existing original/redacted reference guards, writer lifecycle, grace period and read hash checks remain in place.

The launcher uses a bounded `java.home`/version probe to resolve `C:\Program Files\Java\jdk-25\bin\java.exe` rather than launching the spawning Oracle shim for the application. It reads live logs with `FileShare.ReadWrite | FileShare.Delete`. On failure it attempts cleanup through its retained child handle before guarded diagnostics and preserves the initiating failure. The process-local JVM arguments include `--enable-native-access=ALL-UNNAMED` for JNA and `-Duser.timezone=UTC`; no global settings changed. The unused Boot default-user auto-configuration remains excluded.

A new build was required by the added identity class and dependency. The actual command from repository root was:

```powershell
.\apps\api\mvnw.cmd '-Dmaven.test.skip=true' -V -f apps/api/pom.xml clean package
```

Maven `3.10.0` and Oracle JDK `25.0.1` compiled 47 Java source files with `release 25`. `BUILD SUCCESS` completed at `2026-10-17T01:17:24+07:00`. Test resources, test sources and tests were all skipped. The final JAR SHA-256 is `0A0DC0087C4584A5B85D0FE9DFAC1D34E787C773783A34ECF8BDD6218FC016BD`.

The reviewed primary launcher then ran:

```powershell
.\scripts\start-backend.ps1 -DatabasePort 56432 -Demo -SkipBuild
```

It completed successfully. The `.env` comparison remained byte-identical. No absent/old PID was killed, no unrelated service was changed, and existing project volumes/private data were preserved. Current sanitized process metadata is `var/local-runtime/api-process.json`:

| Fact | Observed value |
| --- | --- |
| Actual API PID | `23100` |
| Process start | `2026-10-16T18:17:36.8255902Z` = 17 October, 01:17:36 local |
| Executable | `C:\Program Files\Java\jdk-25\bin\java.exe` |
| Application ready marker | `CG_BACKEND_READY` at `2026-10-16T18:17:40.968Z` |
| Actual listener | `127.0.0.1:8081`, owned by PID `23100` |
| PostgreSQL | Healthy; `18.6`; `127.0.0.1:56432` |
| ClamAV | Healthy; `127.0.0.1:3310`; `PING` → `PONG` |
| Scanner version | `ClamAV 1.5.4/28155/Fri Oct 16 06:24:17 2026` |
| Flyway | `001`, `002`, `003` success; validated and already current at `003` |
| Synthetic core counts | 11 Users, 2 Branches, 1 Party, 1 Relationship |
| Workflow/file counts | 0 jobs; 0 private-file metadata rows |

The process metadata's PID, start-time ticks, actual executable and exact `-jar` path passed the launcher's identity verifier against the live process. The ready marker was emitted after the explicitly enabled startup runner completed. This real startup also initialized the Windows native identity adapter and the private storage/maintenance beans successfully.

Current unique logs are `var/local-runtime/api-ebcab89cdbd248e3ab924640b5e089e6.stdout.log` and `.stderr.log`. The final read-only snapshot at `2026-10-16T18:19:13Z` (17 October, 01:19:13 local) verified the same PID and loopback listener after 96 seconds of uptime, beyond the maintenance worker's initial delay. There were zero `ERROR` entries, zero exception entries, no generated security password and no `inMemoryUserDetailsManager` initialization; stderr remained empty. The private storage root and `.guards` directory existed. This is passive startup observation with an empty file/job store, not an orphan-deletion exercise. PostgreSQL validation did not add or rewrite migration history. Scanner health commands transmitted no file content.

This closes the observed startup blockers for the final integrated revision. It does not demonstrate file scanning outcomes, GC deletion/race safety, HTTP session/role/scope behavior, signing/approval workflows, recovery, concurrency, performance or FE acceptance. Those scenarios were not run. G1/G2 integrated business acceptance remains pending while the user has explicitly chosen to continue Backend work sequentially.
