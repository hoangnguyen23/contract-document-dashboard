# Contract Governance API

Java 25 LTS · Spring Boot 4.1.1 · Spring Modulith 2.1.1 · Maven Wrapper 3.10.0 · PostgreSQL 18.6 · ClamAV 1.5.4.

Canonical documents remain `docs/Final_Project_Specification.md` and `docs/FE_BE_Shared_Development_Contract.md`. Run `python scripts/extract_contracts.py` from repository root after an approved contract change. The extractor checks A file SHA, all block digests, duplicate keys/blocks and local-only refs, then writes byte-identical derived resources.

## Local setup

1. Install a Java 25 JDK and Docker Compose; copy `.env.example` to `.env` and supply a random `CG_CURSOR_SECRET` of at least 32 characters.
2. Start PostgreSQL and ClamAV with `docker compose up -d`. ClamAV must finish downloading its signatures before uploads can become CLEAN. Scanner failures keep files quarantined.
3. Export `CG_CURSOR_SECRET` into the API process environment, plus `CG_DATABASE_PASSWORD` if changed. `CG_LOCAL_SEED_ENABLED=true` opts into synthetic local users; leave it false outside local development.
4. From `apps/api`, build with `./mvnw -Dmaven.test.skip=true package` (Windows: `mvnw.cmd`) and launch with `java -jar target/contract-governance-api-0.1.0-SNAPSHOT.jar`.
5. Access API through the future FE HTTPS localhost:5173 proxy or a same-origin TLS proxy. Session cookies are Secure and host-only; an ordinary HTTP browser origin cannot authenticate this API. Direct Java API port8080 is intended for the private proxy network.

Flyway migrates a real PostgreSQL database at startup. Files live under `CG_STORAGE_ROOT`, default repository `var/private-files`, outside public web paths. Backups must pair PostgreSQL and that private directory; do not independently restore one without source/file hash reconciliation.

## Delivered scope and verification

The current foundation tranche provides infrastructure and identity/customer/dossier/file implementations described by the implementation coverage document. Approval, effectivity, changes, notices and privacy workflows are later gates. Schema availability does not imply an operation is implemented.

The user requested no tests. Maven compilation/package with `maven.test.skip=true` checks Java/dependency assembly only. No HTTP acceptance, browser integration, database/scanner execution, races or performance measurement is claimed. Docker daemon availability and runtime dependency setup must be verified in the deployment environment.
