# Backend delivery documentation

Canonical sources are [A](../Final_Project_Specification.md) and [B](../FE_BE_Shared_Development_Contract.md), currently1.3.0. Implementation plans describe delivery and do not override these specifications.

Current source checkpoint:85 implemented /0 partial /0 missing at appe718ccc5f7f6ba4d3881d924174440bedfbf7db1, Privacy review1595418 and separate final crossmodule repair review. All five Job kinds and three approval/reviewer-option targets are implemented. See [coverage](backend-operation-coverage.md), [execution checkpoints](completion-execution.md) and [Privacy review](reviews/backend-privacy-source-review.md). Business and FE acceptance remain pending.

The work continues through whole-backend source reconciliation, independent review, skipped-test build, exact runtime-source matching and FE handoff. Completed feature branches remain reviewable Draft PRs with explicit dependencies; no merge or main/dev push is authorized.

The owner prohibits writing/running automated tests and unapproved HTTP/business scenarios. Permitted build uses maven.test.skip=true, skipping test resources, compilation and execution. Source review, compilation, operational startup, business verification, FE integrated acceptance and production readiness are separate statuses. The [proposed verification plan](backend-verification-plan.md) is NOT AUTHORIZED and has not been executed.

The active repository is D:/contract-document-dashboard; active feature worktrees are under D:/contract-document-dashboard-worktrees. Earlier C: locations are retained backups. D Docker ownership was independently verified without deleting volumes; see [relocation evidence](reviews/backend-d-relocation-runtime.md). Current integrated application startup must be recorded separately from historical startup reports.