# Negotiated Change operational startup evidence

Observed 18 October 2026, Asia/Ho_Chi_Minh. Operational startup only, under the owner's existing Docker/backend continuation authorization. No HTTP/business acceptance requests or automated tests were executed.

Primary checkout: `work/project-document-publish`, clean detached HEAD `4b2ceeabfe86b414e1102dd6fb2b74c8622562c2`. Application/resources diff against reviewed source `7e2975c64e030f6c956a94ff844ad3c5fad92518` was empty. Root first stopped only the identity-checked managed old API PID25204 and transferred the exact independently reviewed JAR; this verification did not rebuild it.

JAR SHA-256 before/after startup: `3b7d6d127f7162cc559237e7d91632c5e34f76ae10d3617b7064ed2ced556e35`.

Command run in primary:

```powershell
.\scripts\start-backend.ps1 -DatabasePort 56432 -Demo -SkipBuild
```

Launcher completed successfully. Actual Java executable was `C:\Program Files\Java\jdk-25\bin\java.exe`, PID32628, start `2026-10-18T11:56:53.5607207Z` (18:56:53 local). API bound only `127.0.0.1:8081`. `CG_BACKEND_READY` appeared at `2026-10-18T11:56:58.189Z` after application initialization/runners. No matching ERROR/Application-run-failed line was found in its stdout, and its stderr was0bytes at the final observation. No bean-cycle/startup exception was observed.

Existing Docker project `contract-governance` was reused. PostgreSQL18.6 at `127.0.0.1:56432` and ClamAV at `127.0.0.1:3310` remained healthy. Existing volumes/private-storage paths were retained; no volume deletion, database reset, destructive data change, global Java change or isolated-worktree Compose startup occurred. The reviewer made no manual environment/credential change and did not print credentials.

Flyway log validated9migrations, identified prior schema008, applied exactly `009 - contract changes` successfully and reachedv009. A read-only SQL check confirmed V001–V009 all success=true. Read-only counts were Branch2, Party1, Relationship1, User11; no other cg_entity types existed. `cg_change_revision=0`, `cg_change_effect=0`, `cg_job=0`.

These empty workflow counts are an explicit limit: no Dossier→Round→Contract→PDF→Signing→Change→renewal/expiry workflow was exercised. Startup and migration success do not prove API SQL predicates, rendered PDF/scanner behavior, SoD/concurrency/retry/recovery, dashboard/export consistency, FE integration or G3/G4 acceptance. Fresh-database/restore/NFR verification remains unperformed. V010/Notice is not part of this runtime package.

Subsequent source review found and repaired a canonical closureOutcome enum error in prospective task supersession, plus the strict breach boundary, at2af915d. That corrected source was built separately and is NOT the package started above. This file remains historical operational evidence for7e2975c only; zero workflow rows do not establish supersession correctness. See backend-change-source-review.md final addendum for exact corrected build/JAR provenance.
