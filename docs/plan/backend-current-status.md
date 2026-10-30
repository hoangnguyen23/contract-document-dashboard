# Backend current delivery status — 30 October 2026

This is the current operational status entry point. A/B remain the canonical requirements. Historical reports retain their original date, source and scope; the status below does not retroactively change their results.

## Source and delivery identity

| Item | Current recorded identity |
|---|---|
| Current candidate source/config | `2487adb9ea26b1020c9a448ded56be77c2e9e87d`; application fencing repair `d306166cfc9dc6dff45eee80685d5d05f86cd077` |
| Prior email/JSONB application repair | `81c3d7f66628a397aa9c2d62dd26877448c25d10`, retained runtime baseline |
| Delivery branch | `feature/backend-smoke-fixes`; pre-completion-plan HEAD `60e4f3e308a2b103abda793bf31cc0730078d994` |
| PR | [11](https://github.com/hoangnguyen23/contract-document-dashboard/pull/11), Owner approved merge before testing; merged30October at54af6f1b4b59c30ee6e9f01c40cde95368a79ecd |
| Remote dev | `54af6f1b4b59c30ee6e9f01c40cde95368a79ecd`, PR11 merged after owner authorization; prior devdf34af6 retained in history |
| Remote main | `b536f6ec4afb572ff4c807849dbaf0e4baf2e607`, observed30October; initial README tree |
| Current candidate JAR SHA256 | `99e695f8a28f998eafe9264ea9c5883e2317f1ac2ae4593dc9caa6bc1c0f74ed` |
| Prior packaged/runtime baseline JAR | `b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3`, main package freshly rehashed and preserved |
| Canonical | A/B1.3.0; exact freshly read hashes in [baseline](analysis/backend-baseline-20261030.json) |
| Historical manual-smoke docs | `feature/backend-manual-smoke@d352f0dd41c03f428103ba133d9a53377ca8c96d` |

Later completion-plan/status/review documentation changes do not alter the application revision above. A future delivery must report its own exact HEAD and confirm no application changes before reusing the JAR identity.

## Independent completion dimensions

| Dimension | Evidence | Current conclusion |
|---|---|---|
| Source coverage | Existing per-module reviews/inventory; fresh canonical/inventory reconciliation85/85 and all referenced paths exist | Source inventory complete; path existence is not handler/command correctness certification |
| Latest repair source | Independent bounded review of email format and ten prepared-SQL JSONB substitutions | GO within reported scope; no canonical/schema/authorization change |
| Current candidate build |108Java sources clean/package30October08:25:08+07, Oracle25.0.4.1+1-LTS-5/Maven3.10.0; all test resources/compile/run skipped | PASS with exact pinned JDK; Linux CI/fresh wrapper bootstrap NOT RUN |
| Prior baseline operational startup |81c3d7f/JARb29274dc ready30October08:03:03.223+07 at8081;13migrations validated; seed counts preserved | Historical startup only. New2487adb/JAR99e695f8 has NOT been started/deployed; no current-PID guarantee |
| Candidate HTTP defect retest | s07 Relationship email422, s12 seeded Relationship503, s18 File list503 occurred on old baseline | NOT RUN on repair; original FAILs preserved |
| Original contract workflow | Prior run throughs31: Dossier DRAFTv5,3CLEANfiles, Policy DRAFTv2 after product RUN_REGRESSION | PARTIALLY EXECUTED; no Revision/Round/Contract/artifact/signing/readiness/effectivity evidence in run |
| Other domain acceptance | Source reviews exist; no corresponding executed acceptance package claimed | PENDING |
| FE integration | Approved S024/S060/termination1.3.0 internal compatibility reviews | External FE acknowledgement and real consumer/browser execution PENDING |
| Races/recovery/restore/NFR | Requirements in A§13.2 and B; no completed measurement package | NOT RUN / PENDING; production readiness not established |

## Owner decisions in force

- 30 October: continue implementation carefully; testing is a separate stage. No automated tests, HTTP smoke or business scenarios are run in the current development continuation.
- 21 October: limited isolated original-contract manual smoke and separately reviewed external Clock configuration were approved. Smoke ran partially; Clock configuration was not activated. Those approvals/results are historical and do not silently override the later testing deferral.
- S060 and S024 Maker allowedActions corrections and termination interval guard were owner-approved. Do not reopen them as pending product decisions or silently change them.
- No automatic PR merge, main/dev push, force/reset/destructive rebase, real-person delivery, paid services, production deployment or destructive infrastructure/data operations.

## Next work and owners

Use [completion ledger](backend-completion-ledger.md) and [plan](backend-completion-plan-20261030.md). C01 reconciliation and bounded C02–C06 source reviews are delivered. JOB-FENCE-01 source repair and C07 toolchain configuration passed independent review; exact-JDK skipped-test build passed. Recovery runbook and FE handoff are prepared and independently read. C09 reviewable package has independent delivery-evidence GO (reviews/backend-completion-delivery-review-20261030.md); feature publication is recorded by the delivery Git HEAD, and PR11 is merged under owner approval before testing; FE acknowledgement and all deferred runtime acceptance remain open. No newly established source blocker remains in the recorded review scopes.

BE owns API/domain/SQL/runtime preparation. FE owns apps/web and consumer/browser implementation; shared configuration/contract changes follow A/B ownership and acknowledgement. Only owner decides unresolved product/canonical baseline changes and PR integration. External messages are not sent by creating this document.

Testing handoff follows V01–V05 in the completion plan when the owner opens that stage. The old verification proposal is an acceptance backlog, not current execution permission. Individual results must keep source/review/build/startup/runtime-business distinctions and expected outcomes authored from A/B.

## Owner-approved postmerge continuation

Owner explicitly authorized merging PR11 into dev and completing Backend development before testing. Merge54af6f1 contains the reviewed repairs. Follow-up branch feature/backend-release-readiness starts from this merge to close RD01 exact launcher build/runtime JDK selection, RD02 concrete secure TLS preparation and RD03 documentation provenance. This authorization changes execution/integration order, not A/B business semantics or the meaning of acceptance. No automated/manual workflow test is run. The new runtime tooling has not replaced the main process or its JAR.

Postmerge development closure: [Backend development completion](backend-development-completion.md). RD01 pinned launcher and RD02 concrete optional HTTPS preparation are delivered in5e58768/7bfbeaa, with independent review recorded in reviews/backend-postmerge-gap-review-20261030.md. Final feature integration is recorded in Git; all testing and runtime activation remain deferred.
