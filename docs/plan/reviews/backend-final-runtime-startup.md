# Final Backend operational startup evidence

## Preflight — 2026-10-20 10:08 +07:00

Status: **PREFLIGHT READY — FINAL API NOT STARTED**. This note records operational read-only checks, not HTTP/workflow or business acceptance. Final reviewed source/JAR transfer and startup remain separate steps. Privacy repair/review/package was still pending when these checks were made.

- Main checkout `D:/contract-document-dashboard` was clean, detached at `4b2ceeabfe86b414e1102dd6fb2b74c8622562c2`. This old source/target is not the final integrated Backend. No checkout or JAR replacement was performed in preflight.
- Existing `.env` was parsed with the repository's launcher helper without printing credentials. Storage resolves to `D:/contract-document-dashboard/var/private-files`, already present. No environment or private-file configuration was changed.
- No `api-process.json` managed-process metadata, no managed API process and no listener on8081 were present. No unrelated process was stopped.
- Unmodified `Assert-BackendComposeOwnership` passed for the same `contract-governance` project and D repository path. Existing container identities and owned named volumes:
  - PostgreSQL: `7c2ab59fe52041994069e7d6b390e2da39d88fe8b0abddb74814c8930e336273`, service postgres, healthy, `contract-governance_postgres_data`.
  - Scanner: `a5ebfb0d0d8f108888321a97c6e4a3c30959ccf99d81df7dd353f52b00ced551`, service scanner, healthy, `contract-governance_scanner_signatures`.
- Used `docker exec ... psql -X -v ON_ERROR_STOP=1` with explicit `BEGIN READ ONLY ... COMMIT`. Flyway V001–V009 are successful; V010 onward have not yet been applied. Entity counts:2 Branch,1 Party,1 Relationship,11 User; no other cg_entity workflow types. cg_job contains0 rows and0 active leases. Therefore no existing queued/running business jobs or existing Contract/duty/Notice/Privacy aggregates were observed for startup to process at this checkpoint.

No API startup, migrations, HTTP calls, automated tests, manual PDF, byte disposition, business-data writes, volume reset, new database/project or guard bypass occurred. The preflight did not alter app source, `.env`, private files or container configuration. Operational Docker/container startup was performed by root before this read-only pass and is not a business-workflow test.

After final source/review/package GO, the existing launcher command is `./scripts/start-backend.ps1 -DatabasePort 56432 -Demo -SkipBuild`, only after verifying that main checkout and packaged JAR match the reviewed cumulative source. Current supported local Java is25.0.1; canonical exact patch25.0.4.1 remains a reported environment gap. Startup/migration/bean/log evidence must be appended below if and when observed; this preflight alone establishes none of them.

## Final reviewed API startup — 2026-10-20 10:10 +07:00

Status: **OPERATIONAL STARTUP PASS — BUSINESS/FE ACCEPTANCE PENDING**. Root transferred the independently reviewed cumulative source and exact packaged JAR after Privacy source GO. Main checkout remained clean at detached `e718ccc5f7f6ba4d3881d924174440bedfbf7db1`; the package SHA-256 was independently rechecked against launcher metadata as `fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602`.

- Ran only the existing launcher `./scripts/start-backend.ps1 -DatabasePort 56432 -Demo -SkipBuild` from `D:/contract-document-dashboard`. Maven was not repeated. The prior package evidence is107 source files, all test phases skipped, reported BUILD SUCCESS 2026-10-20 10:07:55 +07:00.
- Managed real JVM PID **14772**, start time **2026-10-20 10:10:24.1463200 +07:00**, Java25.0.1 at `C:/Program Files/Java/jdk-25/bin/java.exe`. Log reports Spring application started **10:10:29.951 +07:00**, in5.189s. Listener was independently observed as `127.0.0.1:8081` owned by14772; repository/JAR identity was checked using Get-ManagedBackendProcess.
- Flyway actually applied V010 notices, V011 obligations/incidents, V012 control requests and V013 privacy disposition at startup, reporting four migrations successful at10:10:27.874 +07:00. A separate explicit READ ONLY PostgreSQL transaction confirmed **V001–V013 success**. This establishes migration/startup compatibility on this retained local V009 database, not a fresh-install or restore test.
- Existing PostgreSQL/scanner container IDs, healthy status, project and named volumes stayed the same as preflight. Storage metadata resolves to `D:/contract-document-dashboard/var/private-files`. No credentials were printed or changed.
- Poststartup read-only counts matched the preflight business entity counts:2 Branch,1 Party,1 Relationship,11 User, no other workflow entity types;0 jobs,0 leases. Additional checks found0 cg_file_blob rows,0 disposition records,0 Privacy events and0 private stored files. No pending business work was present to execute; no disposal or PDF generation was triggered. This is an observed empty workflow state, not evidence of successfully exercising those workflows.
- At10:10:54 +07:00 stdout contained no ERROR/application-failed entries and stderr length was0. Logs: `D:/contract-document-dashboard/var/local-runtime/api-b800c16f7ba2408d81d64b9134f10522.stdout.log` and corresponding `.stderr.log`. API was left running as the managed local service.

No HTTP request, automated test, business workflow, manual PDF/render/download, real external-bank/provider action, private-byte deletion or FE integration was executed. Current startup demonstrates actual dependency injection, Flyway application, server startup and healthy local infrastructure. It does not establish scenario correctness, races/lease recovery, byte-disposition recovery, PDF/scan delivery, browser HTTPS/session/CSRF integration, coherent restore or NFR acceptance. Local JDK25.0.1 versus canonical25.0.4.1 remains an explicit environment gap.
