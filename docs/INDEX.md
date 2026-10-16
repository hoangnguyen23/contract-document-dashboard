# Project documentation

- [Final Project Specification](Final_Project_Specification.md): canonical product, business rules and acceptance criteria (A1.1.0).
- [FE/BE Shared Development Contract](FE_BE_Shared_Development_Contract.md): canonical HTTP schema, permissions, fixtures and protocol (B1.1.0, policy file job scope correction).
- [Backend delivery plan](plan/backend-implementation.md): implementation sequence and verification scope for the current Backend work.
- [Backend operation coverage](plan/backend-operation-coverage.md): implementation status and limitations for all 85 canonical operations.
- [Independent foundation review](plan/reviews/backend-foundation-follow-up.md): closed source findings and build evidence, with runtime acceptance still pending.
- [Local runtime startup](plan/reviews/backend-runtime-startup.md): actual PostgreSQL/Flyway, scanner and API startup evidence.
- [Approval implementation notes](plan/approval-implementation.md): deferred G3 dependencies and contract questions, distinct from implemented coverage.

Implementation plans and module setup notes are derived operational documentation; they do not override A/B. Schema or business changes require a concrete canonical A/B patch and review before implementation.
