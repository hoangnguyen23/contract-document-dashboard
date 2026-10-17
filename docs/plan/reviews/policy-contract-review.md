# G3 independent canonical review — approved at bf1f871

FACTS context follow-up: **GO at `bf1f871`.** The single B §8 prose delta matches root's approved decision: no inferred target branch; observable current active/role/contributor-SoD checks; existing `RuleResult.result=REVIEW_REQUIRED` for unknown target scope; route is a conditional hint only. Live business evaluation requires a real explicit target branch and current evidence, while REGRESSION retains isolated authored context. All six JSON blocks and derived/runtime copies are unchanged from `2593f0c` and still match their digests; A hash and CRLF remain valid. No further blocking canonical finding in this bounded review. Untracked implementation files observed in the developer worktree were outside this review and were not inspected, edited or approved.

Final narrow re-review: commit `2593f0c` closes the sole P2 identified below. **GO for the reviewed canonical patch.** B §6 and `seed-v1.hashConventions.simulationFactsHash` now explicitly hash the complete seven-field NFC-normalized FactVector, preserve array order and do not invent absent dossier source fields. The seed semantic delta from `f6b7526` is exactly that one convention; A is unchanged. All six canonical blocks, extracted files, runtime copies and manifest digests match. A SHA remains `e20ccb7b68cf22c2912bf5e689cf275679c7e635ec0bedee90453f6a7b7e2bdc`; B retains CRLF only. The working tree was clean. No tests, HTTP, app edits or runtime acceptance were performed. A separately raised FACTS target-branch context question has been forwarded to root; this hash review does not invent a branch absent from FactVector or certify business authorization.

Reviewed on 17 October 2026: policy branch `work/be-approval-policies`, commits `f18f647`, `6c36405`, `f6b7526`, against merged foundation `15e3eb8`. HEAD was `f6b7526`; working tree was clean. This review only read source/documents and performed static JSON/digest comparisons. No application edits, compile, tests, HTTP calls, business scenario execution or FE acceptance were performed.

## Gate decision

At `f6b7526`, all submitted finite-policy, schedule and generated-file corrections were approved, conditional on the P2 hash clarification below. That P2 is now closed by `2593f0c`. No P1 finding was identified in this patch.

### P2 — Give simulation its own explicit facts-hash projection — CLOSED

B §6 and `seed-v1.hashConventions.factsHash` describe the dossier source projection `{terms,representation,contact,externalAccountReference,templateVersion,bundleVersion}`. However, `PolicySimulation`/`FactVector` supplies seven different fields: `terms`, `representation`, `documents`, `contributorIds`, `candidateL1Id`, `candidateL2Id`, `mandatoryChecks`. `SimulationResponse.evaluation` reuses `Evaluation.factsHash`. The caller cannot know what hash to expect without an undocumented default or another projection.

Minimal correction: state in B and `seed-v1.hashConventions` that the existing `SimulationResponse.evaluation.factsHash` hashes the complete NFC-normalized FactVector, using canonical JSON object-key ordering and existing array-order convention. A separately named `simulationFactsHash` convention can describe it without adding a wire field. Preserve dossier/change revision projections, config hashes, wire schemas and business scope. Do not invent missing contact/account/template/bundle facts, and do not derive expected hashes or expected routes from the evaluator's actual result. Regenerate the canonical manifest and both extracted copies after that clarification.

## Approved semantic content

- The fee formula stays finite: within bounds, fee below reference requires L1_L2; at/above reference requires L1. Equality min=reference is permitted and now yields L1 at the minimum. FEE_BELOW_REF becomes BLOCKED if its substituted fee falls below min.
- Expected corpus outcomes remain independently authored conditional rules; no actual-to-expected copying, skipped boundary, clamping, DSL or additional approval step is introduced. Internal out-of-domain fee/duration vectors remain BLOCKED; this does not relax wire validation.
- Base term selects FIXED when allowed, otherwise OPEN_ENDED with null end. Each FIXED_* vector explicitly forces FIXED and computes end from substituted days. Conditional FIXED/OPEN expectations respect allowedTermKinds subsets. Ordinary below-reference vectors supply eligible independent manager L2; L1 uses null L2 except the deliberate SAME_L1_L2 override. Equal L1/L2 remains a hard BLOCK even for an L1 fee route.
- `Policy.scheduleVersion`, REQUEST_PUBLICATION.expectedScheduleVersion and APPROVE_PUBLICATION.expectedScheduleVersion all allow actual empty counter 0. A §4.2 explicitly requires a future first publication from tomorrow, fail-closed business submission before an applicable policy, and no import of an ACTIVE test fixture as live state.
- GENERATED uploadedBy=null is corrected everywhere: 11 seed occurrences plus 3 scenario/worker-overlay occurrences, 14 total. No generated occurrence retains an uploader. Semantic diffs show no file bytes/hash changes and no unrelated uploaded-file metadata change.

## Static evidence at f6b7526

All six canonical JSON blocks matched their extracted `contracts/*.json` files exactly after canonical serialization; all six runtime resource copies matched those files byte for byte. Every nonmanifest block digest matched the manifest. The complete A file SHA-256 matched `specificationFileSha256`: `e20ccb7b68cf22c2912bf5e689cf275679c7e635ec0bedee90453f6a7b7e2bdc`.

A retained 1,510 CRLF endings; B retained 40,896 CRLF endings, with no bare LF or BOM. JSON semantic changes were limited to version metadata, three schedule minima, the finite corpus semantics, 14 generated uploader nulls and dependent manifest hashes. The two document diffs were narrow. This evidence establishes document/extraction consistency only; it is not runtime verification or G3 completion.
