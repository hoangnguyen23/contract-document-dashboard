# D-drive Docker ownership relocation — operational evidence

Date:19 October2026, completed before10:33 +07:00. This is an infrastructure ownership rehome only. No API/JAR startup, build, Flyway migration, HTTP/business scenario, SQL data mutation, physical evidence-byte cleanup or automated test was performed.

## Ownership proven before mutation

Read the retained handoff-notes/relocation-checkpoint.md, ignored D compose-project.json/override, relocation map and unchanged launcher ownership verifier. The old owner was C:/Users/PC/Documents/Codex/2026-10-09/files-mentioned-by-the-user-dac/work/project-document-publish; copied metadata/relocation map agreed. Main D and retained C compose.yml and .env had equal hashes (secret values were not output). No listener on8081 or D api-process.json existed; no process was stopped.

Exactly two containers carried project contract-governance, both stopped after the engine restart:

- postgres old ID19889e9311710a3e1c0c055a196abb73de6e318a50d0252a54dd0c9d27598dbd, old C working_dir, port127.0.0.1:56432->5432, volume contract-governance_postgres_data at/var/lib/postgresql.
- scanner old ID3887e71eacb58d3bebd2ac9ca3a0cb0c39dd2dcf62cd8c37b7ca906e56157f90, old C working_dir, port127.0.0.1:3310->3310, volume contract-governance_scanner_signatures at/var/lib/clamav.

Images were exactly the pinned postgres18.6 digest74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336 and clamav1.5.4 digestebec5bc138401b36ae987caa1a3fa3c3b2a21ed3d51f0bfa5852825e663e67b0. Both named volumes retained CreatedAt2026-10-11T16:34:22Z and contract-governance ownership labels.

## Controlled action and postconditions

Held the existing D launcher lock. Parsed the allowlisted copied .env as data, supplied its database password only in process memory, compared it with the retained database container and new composed configuration, and restored the prior process variable afterward. Compared composed service images, host ports, exact named volumes and destinations against the retained containers before acting. An ignored sanitized pre-rehome resource snapshot was retained at D:/contract-document-dashboard/var/local-runtime/relocation-before-20261019.json; it contains no environment secrets.

Used the existing D compose.yml, ignored compose.override.yml and compose.env, same project contract-governance and explicit project directory D:/contract-document-dashboard; recreated only postgres/scanner using up -d --force-recreate --pull never --no-build. No down, volume deletion, reset, new project or empty replacement volume was used. No source/C backup/private evidence files were edited.

After verifying actual D working_dir labels, same image IDs and exact volume names, updated only ignored D compose-project.json to schemaVersion1/projectName contract-governance/repositoryPath D:/contract-document-dashboard. The unmodified Assert-BackendComposeOwnership then passed; the verifier was neither disabled nor weakened.

New resources:

- postgres7c2ab59fe52041994069e7d6b390e2da39d88fe8b0abddb74814c8930e336273, running/healthy, D working_dir.
- scannera5ebfb0d0d8f108888321a97c6e4a3c30959ccf99d81df7dd353f52b00ced551, running/healthy, D working_dir.

Same volume names/creation timestamps and same image IDs were verified after recreation. Same loopback ports56432/3310 are listening through Docker Desktop;8081 remains absent. Unrelated Docker containers and processes were untouched. Docker engine reported29.8.0.

## Read-only database snapshot and limits

Executed psql with BEGIN TRANSACTION READ ONLY and SELECTs only: Flyway001–009 all success; core entity counts User11, Branch2, Party1, Relationship1; jobs0. No workflow entities were present. No migration010–013 was run. PostgreSQL normal startup/recovery is infrastructure operation, not evidence of application workflow acceptance.

The main D checkout still contains its historical package/source; no application was started from it. Later startup must use an independently reviewed exact D package/source and existing guarded launcher on56432. This relocation proves retained-container/volume ownership and passive service health only; it does not prove Backend HTTP, migrations beyond009, PDF/scan workflow, signing, Control, Privacy, FE integration or business acceptance.
