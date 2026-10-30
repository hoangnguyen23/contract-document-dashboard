# Backend delivery documentation

Next delivery work: [completion plan](backend-completion-plan-20261030.md); [coherent recovery procedure](backend-recovery-runbook.md).

> Current status: [30 October delivery checkpoint](backend-current-status.md). Historical source/build/startup reports below retain their recorded scope; testing remains deferred.

## Current checkpoint — 30 October 2026

Owner resumed after Docker recovery and explicitly separated testing from implementation. No further automated tests, manual HTTP smoke or business scenarios are run under this continuation. Earlier21October smoke evidence and FAILs remain historical, not erased or relabelled as repaired runtime results.

Current reviewed application repair is81c3d7f66628a397aa9c2d62dd26877448c25d10 on feature/backend-smoke-fixes, Draft PR11. Email format syntax now supports canonical synthetic domains without disabling other formats; ten JDBC JSONB operators use jsonb_exists with scope/argument order preserved. Independent source review and108-source skipped-test package passed30Oct07:51:56+07; JAR SHA256 b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3. This is source/build evidence; the original three failing HTTP cases have NOT been rerun on this candidate.

Remote devdf34af6 contains merged PR2–10 and has the same tree as the old ef33bae delivery; PR11 remains separate, with no automatic merge or main/dev push. Source inventory remains85 implemented/0 partial/0 missing, with business/FE/NFR acceptance pending. FE/setup/docs/test branches audited previously contain no implementation. Local Java25.0.1 still differs from pinned25.0.4.1.

Operational readiness for this exact candidate is recorded separately in reviews/backend-repair-runtime-20261030.md. No Clock XML, synthetic time jump, fault injection, data reset, volume deletion or smoke workflow is part of this continuation. Historical statements below describe their recorded dates and sources.


Canonical sources are [A](../Final_Project_Specification.md) and [B](../FE_BE_Shared_Development_Contract.md), currently1.3.0. Implementation plans describe delivery and do not override these specifications.

Current source checkpoint:85 implemented /0 partial /0 missing at appe718ccc5f7f6ba4d3881d924174440bedfbf7db1, Privacy review1595418 and separate final crossmodule repair review. All five Job kinds and three approval/reviewer-option targets are implemented. See [coverage](backend-operation-coverage.md), [execution checkpoints](completion-execution.md) and [Privacy review](reviews/backend-privacy-source-review.md). Business and FE acceptance remain pending.

The work continues through whole-backend source reconciliation, independent review, skipped-test build, exact runtime-source matching and FE handoff. Completed feature branches remain reviewable Draft PRs with explicit dependencies; no merge or main/dev push is authorized.

The owner prohibits writing/running automated tests and unapproved HTTP/business scenarios. Permitted build uses maven.test.skip=true, skipping test resources, compilation and execution. Source review, compilation, operational startup, business verification, FE integrated acceptance and production readiness are separate statuses. The [proposed verification plan](backend-verification-plan.md) is NOT AUTHORIZED and has not been executed.

The active repository is D:/contract-document-dashboard; active feature worktrees are under D:/contract-document-dashboard-worktrees. Earlier C: locations are retained backups. D Docker ownership was independently verified without deleting volumes; see [relocation evidence](reviews/backend-d-relocation-runtime.md). Current integrated application startup must be recorded separately from historical startup reports.