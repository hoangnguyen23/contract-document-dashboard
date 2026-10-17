# Backend implementation

Sources: `docs/Final_Project_Specification.md` (A1.2.0), `docs/FE_BE_Shared_Development_Contract.md` (B1.2.0). These remain the only canonical product and wire specifications. Compatibility patch 1.1.0 closes the documented POLICY file/job branch scope contradiction; business scope is unchanged.

The user requested Backend implementation and explicitly waived writing/running tests on 11 October 2026. Compilation, dependency resolution and independent static code review remain required. No automated test suites or manual HTTP acceptance scenarios will be run in this delivery. Build success does not establish G1–G6 integrated acceptance.

The foundation tranche implements the Backend part of G1/G2: platform, identity/customer, dossier drafts and private evidence/scanning. On 17 October 2026 the user confirmed sequential Backend G3 development while deferring FE/integrated acceptance. Complete the observed Windows startup fixes, then implement policy before approval/agreement using the latest accepted `dev`; keep the shared gate acceptance pending. G3–G5 operations are not implemented merely by this authorization and must remain explicit in the coverage report, with no simulated success.

## Delivery sequence

1. Platform: Java 25 / Spring Boot 4.1.1 / Modulith 2.1.1; Maven wrapper; PostgreSQL/Flyway; canonical contract extraction; HTTP validation/errors/concurrency/idempotency; session/CSRF; jobs/audit and private storage boundaries.
2. Foundation: branches/users/parties/relationships/contact verification; scoped dossier draft and evidence uploads/scanning.
3. Approval and agreements: finite policy evaluation/publication; immutable revisions; SoD/round decisions; artifact/signing/readiness and effective timeline.
4. Changes/notices: signed amendments, tariff notices, acknowledgement, renewal and durable processing.
5. Lifecycle/data: obligations, incidents, control/closeout, holds/disposition, scoped dashboards and exports.
6. Static integration review, build with tests skipped, setup instructions and an explicit operation coverage report.

Independent develop worktrees own distinct module paths. Shared interfaces are established by the platform task before dependent module implementation; no duplicate shared DTO/error/schema meanings. Integration occurs on `feature/backend-foundation`, based on current `dev`. Neither `main` nor `dev` is merged automatically. Publication is reviewable; FE integration and runtime acceptance remain pending.

## Required connections

HTTP contract → session/current actor → object scope → request validation → ETag/idempotency → domain transaction → relational state + audit + durable intent → exact documented response.

Customer scope supplies dossier/file access. Submission freezes policy/facts/evidence; final approval creates one contract and artifact job. Signing/readiness precede timeline activation. Change/notice jobs recheck immutable source plus current guards. Lifecycle transitions create/control duties and closeout. Record holds serialize against disposition. Reports/files/jobs reuse object scope and redact per permission.

No success placeholders, fixture-as-live-business-output, in-memory persistence, silent fallback for invalid states, or blanket admin bypass. Operations not yet implemented must be listed honestly rather than returning synthetic success.

Policy compatibility patch 1.2.0 clarifies finite corpus boundaries, empty schedule version zero, generated-file uploader metadata and full simulation facts hashing. Six Policy operations passed independent source review at 29d1993; operational migration/startup and business acceptance remain separate gates.
