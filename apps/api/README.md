# Contract Governance API

Java 25 LTS · Spring Boot 4.1.1 · Spring Modulith 2.1.1 · Maven Wrapper 3.10.0 · PostgreSQL 18.6 · ClamAV 1.5.4.

Canonical documents remain `docs/Final_Project_Specification.md` and `docs/FE_BE_Shared_Development_Contract.md`. Run `python scripts/extract_contracts.py` from repository root after an approved contract change. The extractor checks A file SHA, all block digests, duplicate keys/blocks and local-only refs, then writes byte-identical derived resources.

## Local setup

On Windows, install a Java 25 JDK on PATH, Docker Desktop with the engine running and Docker Compose >= 2.24.4. From repository root:

```powershell
.\scripts\start-backend.ps1 -Demo
```

The launcher packages the API with `maven.test.skip=true`, starts this project's PostgreSQL/ClamAV containers, then starts hidden Java on `127.0.0.1:8081`. Defaults are database `127.0.0.1:55432`, scanner `127.0.0.1:3310` and Compose project `contract-governance`. Override using `-DatabasePort`, `-ApiPort`, `-ScannerPort` or `-ComposeProjectName` when occupied. It refuses a Compose project owned by another repository, including retained project volumes without matching containers or previous launcher ownership metadata, and never stops an unrelated process. For an already packaged jar, `-SkipBuild` skips packaging. Readiness is bounded by `-StartupTimeoutSeconds` (default 240, excluding build); ClamAV's first signature download can require a longer value. Scanner failures keep files quarantined.

If required secrets are absent from both the process environment and root `.env`, cryptographically random values are appended once to ignored `.env`. Existing values are preserved, including a password already used to initialize PostgreSQL's volume. Placeholder or invalid values cause an actionable failure instead of silent replacement. Inherited process values take precedence. The launcher reads only `CG_DATABASE_PASSWORD`, `CG_CURSOR_SECRET`, `CG_DEMO_PASSWORD`, `SPRING_PROFILES_ACTIVE` and `CG_STORAGE_ROOT` from `.env`; other keys are ignored. Values are literal, optionally enclosed in single/double quotes, with no command, variable or escape expansion; `#` inside a value is literal. Do not put inline comments after a value. Secrets are never printed or put on the Java command line. Process environment changes are restored after launch.

`-Demo` enables only the synthetic core branches/users/Party/Relationship seed and generates a missing `CG_DEMO_PASSWORD` privately in `.env`. An explicit `SPRING_PROFILES_ACTIVE=demo` also opts in and requires a configured password (12–128 characters). Omit the demo profile outside local development. There is no default application password. Inspect your local ignored `.env` privately when you need the demo password.

Transport settings in the local child process match the selected ports and loopback address; local data goes to absolute repository `var/private-files`, or the configured `CG_STORAGE_ROOT` resolved relative to repository root. The process uses `--enable-native-access=ALL-UNNAMED` for the Windows JNA storage identity adapter and `-Duser.timezone=UTC` so a Windows JVM alias such as `Asia/Saigon` cannot make PostgreSQL reject the JDBC session before Flyway. Business calendar logic explicitly uses `Asia/Ho_Chi_Minh` and is unchanged. The API binds loopback by default via `CG_SERVER_ADDRESS`; configure a deployment bind address explicitly for a private container/proxy network. Unused Boot-generated security credentials are excluded because authentication belongs to the database SessionFilter.

Logs, port override and process metadata live in ignored `var/local-runtime`. Port overrides use [Compose `!override`](https://docs.docker.com/reference/compose-file/merge/#replace-value) so the occupied original database port is replaced rather than also published. Container health, the `CG_BACKEND_READY` marker from `ApplicationReadyEvent` after startup runners, and the exact Java PID's loopback listener determine operational readiness; each launch reads its own unique logs and makes no HTTP requests. Reuse/stop verifies repository, exact jar path, resolved executable and process start time as well as PID. Stop then start after changing secrets/environment or rebuilding the jar:

```powershell
.\scripts\stop-backend.ps1
# Optional: stop this project's containers, retaining their volumes and private files.
.\scripts\stop-backend.ps1 -StopServices
```

For manual setup on other platforms, configure `.env` and export the same settings into the API process. Start containers with `docker compose up -d`, build from `apps/api` with `./mvnw -Dmaven.test.skip=true package` (PowerShell: `.\mvnw.cmd '-Dmaven.test.skip=true' package`), then run `java --enable-native-access=ALL-UNNAMED -Duser.timezone=UTC -jar target/contract-governance-api-0.1.0-SNAPSHOT.jar`. Manual default database/API ports remain 5432/8080; use explicit matching overrides if occupied.

Browser access still needs the future FE HTTPS localhost:5173 proxy or a same-origin TLS proxy, targeting the selected private API port (8081 with the Windows launcher). Session cookies remain Secure and host-only; an ordinary HTTP browser origin cannot authenticate this API. Local startup does not provide a TLS proxy or relax CSRF/CORS/session guards.

Flyway migrates a real PostgreSQL database at startup. Files live under `CG_STORAGE_ROOT`, default repository `var/private-files`, outside public web paths. Backups must pair PostgreSQL and that private directory; do not independently restore one without source/file hash reconciliation.

## Delivered scope and verification

Current integrated source covers85/85 canonical operations, including all Round targets, signing targets and five durable Job kinds. See [current coverage](../../docs/plan/backend-operation-coverage.md), [Privacy source review](../../docs/plan/reviews/backend-privacy-source-review.md) and [crossmodule review](../../docs/plan/reviews/backend-final-crossmodule-source-review.md). The final107-source clean/package passed20October10:07:55+07 with all test resources, compilation and execution skipped. Source correctness has bounded independent reviews, not executed business acceptance.

The active project is D:/contract-document-dashboard. On this machine use `.\scripts\start-backend.ps1 -DatabasePort 56432 -Demo -SkipBuild` only with the recorded reviewed package; port55432 is excluded locally. Real startup/migration evidence is recorded separately in [the final runtime report](../../docs/plan/reviews/backend-final-runtime-startup.md). Earlier startup reports describe older packages.

No automated tests, HTTP workflow, actual PDF/signing/disposition, race/recovery, FE E2E, fresh isolated migration/restore or NFR acceptance has been performed for the final implementation. The [verification plan](../../docs/plan/backend-verification-plan.md) requires explicit owner approval before those scenarios run. Local JDK25.0.1 differs from pinned25.0.4.1.

Container references are pinned by manifest digest; CI actions are pinned by commit SHA. Configured forwarded headers are ignored until a trusted proxy boundary is established, preserving the username+source-IP rate-limit boundary.
