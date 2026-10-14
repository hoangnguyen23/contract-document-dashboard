# Dossier foundation

Canonical authority: A/B 1.1.0. The user waived writing and running tests. This first delivery tranche implements the G1/G2 Backend foundation; later gates remain on the delivery plan.

Implemented operations: `listDossiers`, `createDossier`, `getDossier`, `updateDossier`. `commandDossier` currently implements `CANCEL` and `CLONE`; `SUBMIT` and `WITHDRAW` return canonical `503 SERVICE_UNAVAILABLE` without effects until the approval module supplies `DossierApprovalBridge`. This is partial command coverage, not completed G3 approval.

Draft creation derives branch, assigned maker and confirmed contact from the real relationship. PUT replaces only canonical mutable fields, validates terms/authority intervals and exact CLEAN same-parent evidence, preserves contributor history and serializes writes with the dossier parent lock. Current relationship assignment is checked before idempotency replay; mutable state and active-parent checks apply only to new effects. Auditor projections also account for actual maker assignment when a user has multiple roles.

Clone creates a new DRAFT parent and new evidence IDs/associations, retains immutable bytes internally, resets human verification/current authority check, keeps lineage and does not copy rounds/approvals. Required signing/approval work remains in G3.

`DossierAccess` supplies authorized scoped aggregate access. `DossierApprovalBridge` is an optional transaction port: future approval must freeze exact revisions/rounds and cancel waiting steps atomically while the dossier lock is held. `EvidenceAccess.freezeReferences` protects historical associations; there is no reopen or generic approved-state setter.

Lists use shared signed cursors and SQL branch/relationship scope before paging. No application tests or HTTP acceptance scenarios were written or run. Maven compilation/package with test compilation and execution skipped is the available build evidence; it does not establish integrated G2 acceptance.
