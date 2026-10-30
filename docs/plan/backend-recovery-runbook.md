# Backend backup, restore and rollback runbook

Status: proposed operational procedure, not executed recovery evidence. Owner has deferred testing. This document does not authorize stopping8081, creating a restore environment, changing data, fault injection or production deployment.

## Required manifest

Record run ID/time/timezone, source application and documentation commits, JAR SHA256, canonical A/B/manifest hashes, Java vendor/build/architecture, Maven/dependency versions, PostgreSQL/ClamAV image digests, migration versions/checksums and extension versions. Record actual Compose project/container/network/volume IDs, database name, storage absolute path, process ownership and ports. Keep credentials/private keys/cookies outside Git; manifest references secret locations rather than values. Use least-privilege access and restrict backup ACLs because even synthetic evidence may contain sensitive-looking data.

## Consistency boundary for backup

The database and private evidence bytes are one recovery unit. A database dump alone cannot recover CLEAN/verified/signed evidence. Blob metadata/hash and disposition intent/tombstone must match the restored bytes.

Before executing a maintenance window, separately approve the target and interruption. Inventory all writers: HTTP application, FILE_SCAN/PDF/EFFECTIVITY/NOTICE_DISPATCH/DATA_DISPOSITION workers, schedulers, orphan maintenance and any other application process using the same database/storage. Quiesce all writers at a documented transaction boundary; merely blocking browser traffic is insufficient. Prove no worker is still writing bytes while the DB snapshot/file copy is taken. If writers cannot be quiesced, use a separately designed consistent snapshot method and do not claim an ordinary live dump plus directory copy is coherent.

While the boundary is held, create PostgreSQL backup and private-storage copy into a new owned backup location. Preserve referenced blob bytes and immutable generated artifacts; retain logical disposition/tombstone state rather than reconstructing erased content from unrelated copies. Hash the backup archives and create a manifest of referenced file IDs, sizes, digests and disposition state without dumping full PII. Record capture start/end and failed reads. If any required byte is missing/corrupt, mark backup incomplete and investigate; do not edit metadata to call it valid. Restore normal writers only after ownership and integrity of the intended source environment are confirmed. A failed backup never justifies deleting the source.

## Isolated restore sequence

1. Select one matching DB+storage backup manifest and verify archive hashes before extraction. Validate archive entries remain within the intended new extraction directory. Never restore onto an existing live volume or storage root.
2. Allocate new project/database/user/volumes/storage/process and unused loopback ports. Inspect resolved mounts, connection URL, namespace and process configuration; the restored process must have no access to the live database/storage. Disable real-person/paid transports and use only synthetic/local capture adapters. Restore exercise scope must be approved before execution.
3. Restore DB and matching private bytes with the application stopped. Retain roles/grants only as deliberately provisioned for the isolated database; do not accidentally restore privileged infrastructure access.
4. Compare migration history/checksums against the selected application. Flyway validation must pass. Do not repair checksums or rewrite historical migrations to force startup. A later schema requires an explicitly compatible application/migration plan.
5. Before enabling workers, inspect queued/running jobs, lease timestamps and clock chronology. Restoring an old snapshot can make due actions immediately eligible. Do not activate the application until the isolated environment and intended recovery effects are approved. Do not move host/DB time or rewind business history to avoid due actions.
6. Check each required referenced blob against metadata size/hash and disposition status. CLEAN metadata alone is insufficient. Missing/corrupt bytes must cause access/approval to fail closed; preserve the observed defect and restore from the coherent source rather than fabricating evidence.
7. Start only the isolated application using the exact candidate configuration; record actual Java/JAR, migration validation, workers and source guards. Execute only the approved acceptance subset. Confirm private download, immutable proof, job dedupe and truthful incomplete disposition behavior with actual results. This step is NOT RUN under the current development instruction.
8. Compare post-restore database/job/file/audit effects with independently authored expectations. Record elapsed restore duration and actual dataset/hardware against A§13.2. No production RPO/availability claim follows from one exercise. Retain the run evidence and infrastructure until cleanup is explicitly scoped; do not reset existing resources.

## Application rollback

Preserve prior JAR/configuration/contract manifest and the candidate as distinct immutable artifacts. Determine backward compatibility of the prior application with the **current** schema and persisted states before selecting rollback. If compatible, a separately approved process replacement may use the prior artifact with recorded exact configuration. Keep the database/storage intact and record before/after identity.

If the prior application cannot safely read/write the current schema or newly persisted states, do not blindly swap JARs or run down migrations. Choose a forward repair or a separately approved coherent DB+file restoration with a stated data-loss window and reconciliation plan. An application restart cannot reverse a dispatched notice, accepted signing proof, physical disposition or external acknowledgement. Never rebuild deleted bytes from a different environment or invent an external undo.

## Operational handoff checklist

- Operator can identify actual project, process, ports, DB, storage, source/JAR and rollback compatibility without reading secrets.
- Queue age/failures, scanner version/signature age, file integrity errors, storage capacity and correlation IDs have a documented observation path; no full request/contact/session logging.
- Secure cookie/CSRF and same-origin HTTPS proxy boundary remain enabled; FE counterpart reviews browser origin and proxy settings before integrated delivery.
- Backup verification, coherent restore, lease recovery, security negatives and NFR remain separate execution records. Writing this runbook closes documentation preparation only.
