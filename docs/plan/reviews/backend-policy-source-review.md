# Independent Policy source review

Reviewed on 17 October 2026 at `29d1993d8576bd8f1cbfd561777c92b88fd3ae48`, including implementation `6243f6b`, reviewed foundation merge `deb84acc`, and repairs `2a43627` / `f8f3a6f` / `29d1993`. Business/wire authority is canonical A/B `1.2.0`, with the independent canonical review approved through `bf1f871`.

## Verdict and boundaries

No remaining concrete P1/P2 finding was identified in this bounded Policy tranche after the repairs below. Source review and an isolated build support proceeding with integration and the next authorized Backend module. They do not establish G2/G3 integrated acceptance, real publication/effectivity behavior or completion of the full Backend.

This reviewer read the Java, canonical schemas/prose, authored corpus, shared transaction/validation code and migrations. A separate read-only reviewer independently checked schedule serialization and atomicity. No application source was edited by reviewers. No tests, HTTP request, product regression execution, database migration, policy activation, deletion/race scenario or FE acceptance was performed. The running primary foundation API/database were not disturbed.

## Findings closed

| Finding | Concrete defect | Verified repair |
| --- | --- | --- |
| P1 — publication requester self-review | A draft created by A could be requested and approved by dual-role B because only contributors were excluded. | `V005__policy_publication_request.sql` adds an internal requester record, inserted in the same request transaction and protected against update/delete. `PolicyService.isIndependent()` excludes both requester and contributors in new decisions, action projections and authorization before cache replay. No wire field was invented. |
| P1/P2 — authority time boundaries | `validTo` was inclusive, and any nonnull revocation timestamp was treated as already effective. | `PolicyEvaluator` uses `[validFrom local midnight, validTo local midnight)` in `Asia/Ho_Chi_Minh`, and blocks revocation at/after its effective instant. Business evaluation defaults to server action time; the Java-only explicit-time port supports actual signing/effect instants. FACTS/corpus use hypothetical terms-start midnight. Human `checkedAt` remains bounded by server time. |
| P2 — duplicate simulation ID sets | FACTS accepted repeated contributor/evidence IDs despite B's uniqueness rule. | `validateSimulationIds()` rejects duplicate contributor IDs and each mandatory-check evidence ID set with the exact duplicate-index field path before evaluation/hash response. |
| P2 — narrowing schedule counters | `expectedScheduleVersion.asInt()` could wrap a valid large integer and falsely match the empty counter. | Exact decimal-to-integer conversion compares against the stored integer through `BigInteger`, including integer-valued decimal/exponent spellings. No narrowing or schema restriction was added. |

The requester exclusion is stable after role changes and survives publication/retirement. Its internal immutable row does not add undeclared properties to the closed Policy response. The initial Policy implementation had not been deployed to the primary database during this review; V004/V005 runtime execution remains pending.

## Six canonical operations

| Operation ID | HTTP route | Reviewed behavior |
| --- | --- | --- |
| `listPolicys` | `GET /api/v1/policies` | Canonical spelling retained; current Policy role, validated state/cursor/limit, bank-wide Policy namespace and closed `PolicyPage`. |
| `createPolicy` | `POST /api/v1/policies` | Typed validated config, fresh DRAFT/contributor set, exact config hash, optional terminal clone source, transactional audit/cache, `201` and relative Location. |
| `getPolicy` | `GET /api/v1/policies/{id}` | Current Policy role, real stored version/state/config, ETag and refreshed action/schedule projection. |
| `updatePolicy` | `PUT /api/v1/policies/{id}` | DRAFT only; If-Match, contributor accumulation, full config replacement and regression invalidation. |
| `commandPolicy` | `POST /api/v1/policies/{id}/commands` | Finite RUN_REGRESSION, REQUEST/APPROVE/REJECT_PUBLICATION and terminal CLONE; current authorization before replay, current-state guards for new effects, transactional schedule/audit/cache. CLONE returns a new `201` resource. |
| `simulatePolicy` | `POST /api/v1/policies/{id}/simulation` | Read-only FACTS/REGRESSION; mutually exclusive nullable response members; no persistent report, audit mutation, round or job. Session-bound CSRF remains required for this POST. |

Controller bodies match the declared closed response structures. Required nulls, version/config/hash fields, allowed/blocked actions and regression fields are constructed explicitly. Shared request advice validates the canonical request shape; shared parameter validation enforces required headers and closed query vocabulary. `CommandExecutor` validates successful mutation responses inside the transaction before cache/state commit; response advice also validates transport bodies. No extra route, enum, Job kind or public evaluator DSL was introduced.

Policy roles authorize only the global Policy namespace. They confer no business branch scope. Mutations lock/reload the current acting User before effects; reviewer eligibility reads active current roles and active explicit branch grants. A cache hit retains original status/body/ETag after current authorization instead of incorrectly rechecking the old resource state/version. The requester/contributor exclusion also applies before that replay.

## Config, finite regression and hashes

`PolicyConfig` validates the canonical closed schema and semantic ordering: `0 ≤ min ≤ reference ≤ max ≤ 9999999999`, nonempty unique allowed-term subset, `1 ≤ minDays ≤ maxDays ≤ 3650`, bounded review days and unique required purposes retaining the mandatory trio. Decimal-string fee values fit the chosen long arithmetic without truncation.

`PolicyRegression` iterates all 20 authored `POLICY-CORPUS-1` vectors. Materialization is a finite switch over the frozen fee/duration labels; it does not interpret expressions from HTTP. Equal min/reference, zero/global-maximum fee boundaries, FIXED-only/OPEN_ENDED-only configs, forced FIXED vectors, duration 0/3651, required documents, inactive/out-of-scope reviewer, contributor and same-L1/L2 cases were traced in source. Out-of-wire boundaries are materialized and blocked rather than skipped or clamped.

Expected routes are independently computed from authored literals and the approved small conditional oracle; they do not call the evaluator or copy its actual route. Ordinary below-reference vectors receive an independent eligible manager; L1 vectors retain null L2 except the deliberate equality/SoD case. Synthetic candidate/scope overrides are confined to the corpus.

`configHash` covers the complete NFC-normalized canonical config. FACTS `factsHash` covers exactly the complete seven-field NFC-normalized FactVector, retaining array order. RUN_REGRESSION stores a server-produced corpus/config-bound report. Publication recomputes the report and compares all substantive fields/results/config hash while excluding only the regenerated timestamp. PUT clears the previous report; the client cannot submit its own passing report through the closed request schema.

FACTS lacks a target branch by contract. It therefore adds `TARGET_BRANCH_SCOPE=REVIEW_REQUIRED` and offers only a hypothetical route; no branch is inferred from the Policy actor or candidate IDs. The real `PolicyAccess` port rejects an absent branch and unpublished policy, retains the selected ACTIVE/RETIRED config by ID, and checks live candidate roles/scope. Its owning source module must still lock and validate real scoped source facts/files/current mandatory evidence and enforce a blocked result. Simulation flags cannot be reused as real evidence. The source port does not apply the simulation-only 20-contributor cap to real cumulative source contributors.

## Real transaction and schedule boundaries

The row locks are reached under real transaction entrypoints:

- Policy create/update/commands run through `CommandExecutor`'s `TransactionTemplate`; authorization, schedule changes, Policy state, audits, requester record, validated response and idempotency cache share its connection/transaction.
- The scheduler invokes public nonfinal `PolicySchedule.activateDue()` on the managed bean with `@Transactional` already enclosing its internal `activateLocked()` call.
- External business resolution enters `PolicyAccess.requireApplicable()` and calls the separate `PolicySchedule` bean's transactional entrypoint; normal REQUIRED propagation joins the caller's transaction.

This does not rely on a self-invoked annotation or an autocommit `SELECT ... FOR UPDATE` illusion. Current publication and activation paths acquire the shared `POLICY_PUBLICATION` row and singleton schedule row before Policy row locks. Their differing subsequent old/new Policy order is serialized by those common locks.

V004 initializes the real schedule counter at 0 with no fabricated active policy. The singleton has one future slot; partial indexes prevent multiple ACTIVE/SCHEDULED policies; `[effective_from,effective_to)` GiST range exclusion prevents interval overlap. Both request and approval require tomorrow-or-later local effectivity and exact current schedule revision. Closing the active interval, reserving its successor, saving publication and recording audits share the transaction. Immediate successor boundaries prevent a timeline gap.

The persisted due reservation is durable activation intent. The worker atomically retires the previous policy, activates its successor, writes both audits, clears the future slot and increments the counter. A rollback retains the prior slot/state; a later poll can retry. A committed activation leaves no future slot for duplicate activation. Catch-up resolution uses the same lock/transaction. Before the first real ACTIVE policy, business resolution fails closed. Retired config remains immutable through the public API and available for frozen historical evaluation while current eligibility/authority conditions are rechecked.

These are source conclusions. PostgreSQL constraints, scheduler proxy behavior, races, clock-boundary recovery and exact durable effect/audit counts have not been exercised at runtime for this Policy revision.

## Build evidence

One final isolated clean/package ran after the repairs, from this worktree's `apps/api`:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

`BUILD SUCCESS` completed at `2026-10-17T12:54:18+07:00`. Maven `3.10.0` compiled 54 Java source files with `javac release 25` using Oracle JDK `25.0.1`. Maven explicitly reported no test-resource copying, no test-source compilation and skipped tests. This compiler patch differs from the document's exact JDK baseline; no toolchain/global runtime was changed. The source diff whitespace check passed.

Approval rounds, immutable dossier revisions, actual agreement/artifact/signing/readiness and their integration remain the next Backend tranche. This review does not mark G3 or deferred FE/integrated acceptance complete.
