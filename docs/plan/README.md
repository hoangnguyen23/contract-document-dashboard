# Backend delivery documentation

Canonical sources are [A](../Final_Project_Specification.md) and [B](../FE_BE_Shared_Development_Contract.md), currently1.3.0. Implementation plans describe delivery and do not override these specifications.

Current reviewed source checkpoint: Control8f40974/reviewc96b8ec;80 implemented,1 partial and4 missing canonical operations. Privacy/DataRequest and DATA_DISPOSITION are in progress on feature/backend-records-privacy. Dirty source is not counted as completed. See [coverage](backend-operation-coverage.md), [execution checkpoints](completion-execution.md) and the [Privacy task](tasks/backend-privacy-completion.md).

The work continues through whole-backend source reconciliation, independent review, skipped-test build, exact runtime-source matching and FE handoff. Completed feature branches remain reviewable Draft PRs with explicit dependencies; no merge or main/dev push is authorized.

The owner prohibits writing/running automated tests and unapproved HTTP/business scenarios. Permitted build uses maven.test.skip=true, skipping test resources, compilation and execution. Source review, compilation, operational startup, business verification, FE integrated acceptance and production readiness are separate statuses. The [proposed verification plan](backend-verification-plan.md) is NOT AUTHORIZED and has not been executed.

The active repository is D:/contract-document-dashboard; active feature worktrees are under D:/contract-document-dashboard-worktrees. Earlier C: locations are retained backups. D Docker ownership was independently verified without deleting volumes; see [relocation evidence](reviews/backend-d-relocation-runtime.md). Current integrated application startup must be recorded separately from historical startup reports.