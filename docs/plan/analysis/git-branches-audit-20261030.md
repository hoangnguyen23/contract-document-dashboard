# All-branch Git audit — 30 October 2026

Read-only audit after root fetched every origin head. Snapshot:42 local/remote refs,15 distinct HEAD commits,14 distinct Git trees. Every ref is listed below; equal trees were deduplicated for content inspection. No checkout, merge, reset, rebase, test, HTTP, runtime action or application edit was performed by this reviewer. Only this report was written.

## Principal findings

1. **Remote dev now includes the Backend stack.** `origin/dev` is `df34af6d6a865607d17169535e5129e6f3b04615`; its first-parent history records PR merges #2–#10 after #1. Its tree `8e810a0ab81c80ce5f7f8c88ee09998c7c9452b1` is byte-identical to integrated delivery `ef33bae`. `git diff ef33bae origin/dev` is empty; no new application or canonical behavior arrived with these merge commits. This proves local Git ancestry/tree integration, not current GitHub review/CI status or production deployment.
2. **The advertised FE/setup/docs/test branches contain no implementation.** All19 refs at initial `b536f6e` share one README-only tree:9 FE,4 setup,2 docs,3 test and main. There are no frontend manifests/source or tests hidden in these branch trees. Branch names alone must not be treated as completed work.
3. **Local dev is stale.** Local `dev` is503483c (A/B1.0.0 documents only), while origin/dev is1.3.0 integrated Backend. Preserve its history; if root chooses to move it, use a guarded fast-forward only, not reset/rebase.
4. **Two actual manual-smoke defects remain the next source priority.** The previously dirty `feature/backend-smoke-fixes` was committed concurrently during this audit as81c3d7f, a separately identified email-format/prepared-SQL JSONB repair candidate. It is not in origin/dev/ef33bae and must not be counted verified before independent review, exact build and separately versioned manual retest. The prior85/85 source inventory is not proof of runtime compatibility: actual reserved-domain email create422/seed read503 and file-list SQL503 reopened known defects.
5. **Manual approval/evidence is local-only.** `feature/backend-manual-smoke` adds two documentation commits after ef33bae, including the approved limited smoke record and Clock proposal; no application diff. Runtime evidence/secrets are ignored under var/verification, not part of a branch tree. Owner later explicitly approved the exact isolated +1day/+2day Clock configuration; actual use/results must be checked from run evidence, not inferred from proposal text.

## Per-ref matrix

`dev L/R` = commits reachable only from origin/dev / only from this ref. `base L/R` = commits only from ef33bae / only from this ref. `files vs dev/base` = number of differing tracked paths (two-endpoint diff, not lines). Tree IDs below map to exact tree hashes in the next table.

| Ref | HEAD | Tree | Canonical A/B | dev L/R | base L/R | files vs dev/base |
|---|---|---|---|---|---|---|
| `dev` | `503483c` | T1 | 1.0.0 (docs only) | 160/0 | 151/0 | 219/219 |
| `feature/backend-approval-agreement` | `b8a026e` | T2 | 1.2.0 | 87/0 | 78/0 | 116/116 |
| `feature/backend-approval-policies` | `c0fcd48` | T3 | 1.2.0 | 114/0 | 105/0 | 150/150 |
| `feature/backend-contract-changes` | `5eefa7b` | T4 | 1.2.2 | 52/0 | 43/0 | 95/95 |
| `feature/backend-contract-closeout` | `8d131af` | T5 | 1.3.0 | 16/0 | 7/0 | 36/36 |
| `feature/backend-dashboard-reports` | `4b35c1b` | T6 | 1.2.1 | 74/0 | 65/0 | 107/107 |
| `feature/backend-foundation` | `ebe0691` | T7 | 1.1.0 | 127/0 | 118/0 | 160/160 |
| `feature/backend-manual-smoke` | `83fb401` | T8 | 1.3.0 | 9/2 | 0/2 | 4/4 |
| `feature/backend-notices-renewals` | `1462e14` | T9 | 1.2.2 | 43/0 | 34/0 | 83/83 |
| `feature/backend-obligations-incidents` | `d96db6d` | T10 | 1.2.2 | 33/0 | 24/0 | 67/67 |
| `feature/backend-records-privacy` | `ef33bae` | T11 | 1.3.0 | 9/0 | 0/0 | 0/0 |
| `feature/backend-smoke-fixes` | `81c3d7f` | T12 | 1.3.0 | 9/1 | 0/1 | 5/5 |
| `task/be-evidence` | `eac2d6a` | T13 | 1.1.0 | 158/6 | 149/6 | 178/178 |
| `origin/dev` | `df34af6` | T11 | 1.3.0 | 0/0 | 0/9 | 0/0 |
| `origin/docs/developer-handover` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/docs/project-specifications` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/backend-approval-agreement` | `b8a026e` | T2 | 1.2.0 | 87/0 | 78/0 | 116/116 |
| `origin/feature/backend-approval-policies` | `c0fcd48` | T3 | 1.2.0 | 114/0 | 105/0 | 150/150 |
| `origin/feature/backend-contract-changes` | `5eefa7b` | T4 | 1.2.2 | 52/0 | 43/0 | 95/95 |
| `origin/feature/backend-contract-closeout` | `8d131af` | T5 | 1.3.0 | 16/0 | 7/0 | 36/36 |
| `origin/feature/backend-dashboard-reports` | `4b35c1b` | T6 | 1.2.1 | 74/0 | 65/0 | 107/107 |
| `origin/feature/backend-foundation` | `ebe0691` | T7 | 1.1.0 | 127/0 | 118/0 | 160/160 |
| `origin/feature/backend-notices-renewals` | `1462e14` | T9 | 1.2.2 | 43/0 | 34/0 | 83/83 |
| `origin/feature/backend-obligations-incidents` | `d96db6d` | T10 | 1.2.2 | 33/0 | 24/0 | 67/67 |
| `origin/feature/backend-records-privacy` | `ef33bae` | T11 | 1.3.0 | 9/0 | 0/0 | 0/0 |
| `origin/feature/frontend-approval-agreement` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-approval-policies` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-contract-changes` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-contract-closeout` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-dashboard-reports` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-foundation` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-notices-renewals` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-obligations-incidents` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/feature/frontend-records-privacy` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/main` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/setup/backend` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/setup/frontend` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/setup/integration-ci` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/setup/shared-api-contract` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/test/end-to-end-acceptance` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/test/performance-recovery` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |
| `origin/test/security-concurrency` | `b536f6e` | T14 | none | 161/0 | 152/0 | 219/219 |

## Tree/content groups

| Tree | Exact tree hash | Files | Inspected scope / meaning |
|---|---|---|---|
| T1 | `03dec6b42707127c4cfd20cf3492542de1501809` | 3 | Original A/B1.0 documents only, no extracted contracts or app. |
| T2 | `a7649d75ae58caa0bc637724cb6cbc1571d68789` | 142 | Original approval/agreement/signing lifecycle checkpoint; historical source limitations superseded by later stack. |
| T3 | `79be1a651fb4e69591759f90c9f512a263941831` | 104 | Policy/foundation checkpoint with real configuration/evaluator/publication source, before later modules. |
| T4 | `055a2e61b2fd02d43c02ff09a4e66d4921f83cdc` | 166 | Negotiated Change/renewal checkpoint, earlier Notice dependency superseded by later stack. |
| T5 | `38222b9974380b8c0db17bd61db7b20a07155f94` | 203 | Control/closeout checkpoint plus D runtime ownership evidence, before Privacy. |
| T6 | `88039740067bf8862ee3f5e715fa2fee22a30652` | 153 | Reporting checkpoint plus approved S060 canonical correction. |
| T7 | `8fe999a4ec1364e2c05c7641b44d1edbc0545e6c` | 92 | Foundation/identity/customer/dossier/files/scanner and guarded local runtime source. |
| T8 | `163eace1433d6fe5e1dde01f1af38db07a9d26c0` | 221 | Full integrated source unchanged, plus two manual-smoke/Clock authorization and proposal docs commits. |
| T9 | `61c781329a01fb3991244c98587f9da8897239f2` | 175 | Notice/ACK/tariff source integrated; later G5/Control/Privacy not present. |
| T10 | `1a54829b3e0cdbb08e1dd21370d3d578ff9cbee1` | 191 | Obligation/Incident source checkpoint; later Control supplies CLOSEOUT proof. |
| T11 | `8e810a0ab81c80ce5f7f8c88ee09998c7c9452b1` | 219 | Integrated107-Java-file Backend, V001–V013, canonical1.3.0, final source/runtime docs; also exact remote dev tree. |
| T12 | `e228fa9f89086bc4cf1a6c117126adde6cacf2fa` | 220 | New candidate81c3d7f: narrow email-format syntax and prepared-SQL JSONB repair; independent review/build/retest gates are separate and pending in this audit. |
| T13 | `ba90f0ec554f852ff926fe0c581ba5d05f988a43` | 80 | Old isolated evidence foundation history. Six non-ancestral commits are patch-equivalent to changes already in integrated stack; no unique cherry-picked patch remains. |
| T14 | `291e0fc4260b8f5ea5672b2252f8a2992de7279c` | 1 | Initial README only. All FE/setup/docs/test placeholders and main share this tree. |

## Ancestry, canonical and process checks

- All nine Backend feature heads from foundation through records-privacy are ancestors of origin/dev and match their corresponding origin refs. Their old A/B versions1.1/1.2/1.2.1/1.2.2 are historical checkpoints; do not merge an old checkpoint over canonical1.3.0 or regenerate current resources from it.
- `origin/dev` and ef33bae have identical canonical files, extracted contracts, Java source, migrations, CI and delivery documentation. No canonical drift or application-source merge conflict is introduced merely by basing the pending repair on ef33bae. Its history can be integrated later through a normal PR into current dev; do not rewrite dirty work or rebase without need.
- All tracked trees were checked for AGENTS.md; none exists. No additional branch-local development rule overrides A/B/shared contract or the owner no-automated-tests policy. Existing docs-sprint instructions require separate develop/review, while owner Git constraints prohibit automatic main/dev merge.
- The complete first-parent origin/dev merge chain is: df34af6(#10),781f359(#9),b1d6683(#8),051c2b1(#7),e755baa(#6),cbbb53e(#5),189575e(#4),5715354(#3),b55959a(#2),15e3eb8(#1). These are repository merge messages/ancestry, not a claim this agent merged any PR.
- `task/be-evidence` is not an ancestor by commit identity, but `git log --cherry-pick --right-only ef33bae...task/be-evidence` is empty. Its V003 targetKind constraint correction is already represented in current source; do not blindly cherry-pick/rewrite applied migrations.
- Current root checkout remains detached ef33bae. Feature worktrees preserve separate Change/reporting/Notice/G5/Control/Privacy histories. The fix worktree owns the newly committed81c3d7f candidate and remaining task/analysis; the manual-smoke branch owns docs/proposals. This audit does not clean or archive any worktree.
- Scope remains Backend completion and approved manual synthetic original-contract smoke. Read-only FE branch audit is not permission to implement Frontend or run test/performance/security suites.

The configured remote.origin.fetch remains a selective Backend/dev refspec list. This audit relied on root’s explicit all-heads fetch; a future plain git fetch may not refresh FE/setup/docs/test/main refs. Do not infer their future state from this snapshot without another explicit all-heads fetch.

This is branch/tree/dependency reconciliation and a bounded inspection of meaningful unique source/document changes, not a new exhaustive line-by-line correctness certification of every historical application version. Earlier independent module reviews remain attributed evidence; actual pending candidate review and smoke verification remain separate.

## Recommended continuation

1. Independently review the two narrow P1 repairs candidate81c3d7f in feature/backend-smoke-fixes, preserving baseline ef33bae/JARfa4c214e and original FAIL evidence. Review query scope/parameter order and all email syntax/literal behavior, then compile with every test phase skipped.
2. Reconcile isolated smoke actual process/database/storage/clock/current date before resuming. Ten-day elapsed wall time can invalidate sessions, startOn dates or policy timing; never reuse an old synthetic schedule or restart an advanced dataset backwards without inspecting its chronology. Preserve old project/volumes and API8081.
3. Pin a newly reviewed candidate commit/JAR separately, rerun the specifically failed manual cases plus dependent original-contract workflow within the existing owner scope. Do not change expected results from actual output or silently relabel the old baseline as fixed.
4. Publish reviewable feature PR/evidence updates against current origin/dev only after source/build/review gates. No force push or automatic PR merge. Consolidate branch-state and smoke evidence docs so prior historical source-complete statements are not mistaken for full current acceptance.
5. Actual FE integration, broad domain acceptance, concurrency/fault/recovery/load/NFR and production readiness remain separate pending work/approval. Placeholder FE/test branches do not supply that evidence.
