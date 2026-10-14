# Contract Governance API

Java 25 LTS · Spring Boot 4.1.1 · Spring Modulith 2.1.1 · Maven Wrapper 3.10.0 · PostgreSQL 18.6 · ClamAV 1.5.4.

Canonical documents remain `docs/Final_Project_Specification.md` and `docs/FE_BE_Shared_Development_Contract.md`. Run `python scripts/extract_contracts.py` from repository root after an approved contract change. The extractor checks A file SHA, all block digests, duplicate keys/blocks and local-only refs, then writes byte-identical derived resources.

## Local setup

1. Install a Java 25 JDK and Docker Compose; copy `.env.example` to `.env` and supply a random `CG_CURSOR_SECRET` of at least 32 characters.
2. Start PostgreSQL and ClamAV with `docker compose up -d`. ClamAV must finish downloading its signatures before uploads can become CLEAN. Scanner failures keep files quarantined.
3. Export `CG_CURSOR_SECRET` into the API process environment, plus `CG_DATABASE_PASSWORD` if changed. Docker Compose reads root `.env`; the separately launched Java process needs those variables exported explicitly. For the synthetic local seed, export `SPRING_PROFILES_ACTIVE=demo` and `CG_DEMO_PASSWORD` (12–128 characters). The demo profile imports only core branches/users/Party/Relationship and never fixture workflow states; there is no default password. Omit the demo profile outside local development.
4. From `apps/api`, build with `./mvnw -Dmaven.test.skip=true package` (PowerShell: `.\mvnw.cmd '-Dmaven.test.skip=true' package`) and launch with `java -jar target/contract-governance-api-0.1.0-SNAPSHOT.jar`.
5. Access API through the future FE HTTPS localhost:5173 proxy or a same-origin TLS proxy. Session cookies are Secure and host-only; an ordinary HTTP browser origin cannot authenticate this API. Direct Java API port8080 is intended for the private proxy network.

Flyway migrates a real PostgreSQL database at startup. Files live under `CG_STORAGE_ROOT`, default repository `var/private-files`, outside public web paths. Backups must pair PostgreSQL and that private directory; do not independently restore one without source/file hash reconciliation.

## Delivered scope and verification

The current foundation tranche provides infrastructure and the Backend side of G1/G2: session/users/branches, parties/relationships/contact review, dossier drafts and private evidence/scanning. The [operation coverage](../../docs/plan/backend-operation-coverage.md) lists all 85 operations and their concrete limitations. Approval, effectivity, changes, notices and privacy workflows are later gates. Schema availability does not imply an operation is implemented; G1/G2 integrated acceptance remains pending.

The user requested no tests. Maven compilation/package with `maven.test.skip=true` checks Java/dependency assembly only. CI runs canonical extraction and this same build with tests skipped. No HTTP acceptance, browser integration, database/scanner execution, races or performance measurement is claimed. The Docker daemon was not running on the authoring workstation; Compose services and deployment connectivity have not been exercised.

Container references are pinned by manifest digest; CI actions are pinned by commit SHA. Configured forwarded headers are ignored until a trusted proxy boundary is established, preserving the username+source-IP rate-limit boundary.
