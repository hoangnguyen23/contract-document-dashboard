# Backend repair operational startup —30 October2026

Status: PREFLIGHT READY; candidate application NOT STARTED at this checkpoint. Latest owner requested careful continuation without tests; no automated tests, HTTP smoke, business workflows or Clock XML activation is performed here.

## Preflight and preserved ownership

Primary D:/contract-document-dashboard was clean, detached ef33bae. No current managed API process or listener8081 was observed; retained metadata is historical only. Private storage resolves to D:/contract-document-dashboard/var/private-files. Existing baseline JAR is retained until root's separately guarded candidate transfer.

Verified existing contract-governance container IDs7c2ab59fe52041994069e7d6b390e2da39d88fe8b0abddb74814c8930e336273(postgres) and a5ebfb0d0d8f108888321a97c6e4a3c30959ccf99d81df7dd353f52b00ced551(scanner), exact D working directory/project/service labels, pinned PostgreSQL18.6/ClamAV1.5.4 images, loopback56432/3310 and original volumes contract-governance_postgres_data / contract-governance_scanner_signatures. Started only these existing containers; no recreate/down/reset/new volume or unrelated-container action. Smoke project cg-smoke-20261021-a containers remain stopped with unchanged IDs.

The first manual ownership-helper invocation omitted its required deadline, producing parameter-binding diagnostics; it was not treated as positive proof. Ownership had been inspected explicitly before container start. The unchanged Assert-BackendComposeOwnership was then rerun with an explicit40-second deadline and passed. No verifier was edited or weakened.

BEGIN TRANSACTION READ ONLY snapshot after dependency start: Flyway001–013 all success; entity counts Branch2/Party1/Relationship1/User11, no workflow types; cg_job has0 rows. No pending business aggregate/job was found that could execute on application startup. SQL SELECTs only; no business data/private bytes were changed.

## Candidate gate

Reviewed application81c3d7f66628a397aa9c2d62dd26877448c25d10; final candidate JAR SHA256 b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3, source review recorded separately. Root will transfer exact candidate and source into the main checkout, retaining old JAR, before this operator uses the unchanged launcher. No candidate process or HTTP acceptance is claimed yet.
## Operational startup result

PASS for infrastructure/application startup only,30October2026. Root guarded transfer moved clean main checkout to236ba8af1c80d95bc6a38f4768a69d76e3c30b36 (application81c3d7f) and verified candidate JAR SHA256 b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3. The prior fa4c214e baseline package was retained under ignored var/local-runtime/retained-packages, not overwritten without retention.

Executed unchanged scripts/start-backend.ps1 -DatabasePort56432 -Demo -SkipBuild. It completed successfully without HTTP requests. Actual JVM PID29280 uses C:/Program Files/Java/jdk-25/bin/java.exe; process start2026-10-30T01:02:59.0241723Z (08:02:59+07), startup ready marker2026-10-30T01:03:03.223Z (08:03:03+07). Listener127.0.0.1:8081 belongs to29280. Launcher metadata start ticks match the real process start, candidate JAR hash and D private-storage path. No unrelated process was killed; prior stale metadata was handled by the original ownership/PID identity checks.

Current stdout var/local-runtime/api-7e80e09c30b0478795ee4147347793bb.stdout.log reports13 migrations successfully validated, schema013 up to date/no migration necessary, and CG_BACKEND_READY. At observation stderr length0/stdout ERROR lines0. Both original main PostgreSQL/scanner containers are healthy with the same IDs/volumes/ports.

Post-start BEGIN TRANSACTION READ ONLY snapshot confirms V001–V013 all success, unchanged Branch2/Party1/Relationship1/User11, jobs0 and file blobs0. No workflow entities or effects were created. Smoke containers0a68b9d4d58b/d462c432f200 remain stopped with their retained volumes/data; no smoke process/HTTP/Clock override was started. Main worktree remains clean at236ba8a and API is left running.

This is source/JAR matching, bean initialization, migration validation and operational readiness evidence only. It does not establish that the original email/list SQL HTTP failures now pass, or business, FE, concurrency/recovery, performance/NFR or production acceptance. Tests and HTTP smoke were not run, per the latest owner instruction. Actual JDK25.0.1 remains below canonical pinned25.0.4.1.
