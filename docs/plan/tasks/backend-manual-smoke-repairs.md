---
id: backend-manual-smoke-repairs
scope: canonical email syntax and PostgreSQL JDBC JSONB existence queries
status: done
depends-on: []
---

# Repair actual manual-smoke defects

## Context and objective

Reviewed baseline app e718ccc/delivery ef33bae/JAR fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602 stays immutable. Owner authorized limited manual synthetic scope1 and normal review/Git defect handling; no automated tests, load/fault injection, main/dev changes or silent baseline replacement. Root has reported the defects/proposed repair before implementation.

Read A/B1.3.0, docs/plan/analysis/manual-smoke-email-validation.md, ContractRegistry and exact bundled networknt3.0.8. Actual sanitized evidence lives under ignored D:/contract-document-dashboard/var/verification/manual-smoke-20261021-a/evidence; do not copy secrets/raw sessions into Git.

- EMAIL-RFC-SYNTHETIC P1: s07 RelationshipCreate with canonical smoke-a@example.invalid returned422 rather than201; s12 authorized GET own canonical seed Relationship returned503 rather than200. Networknt adds registered-TLD membership checking to email syntax; canonical seed/redacted email uses .invalid. Both cases left business data unchanged. Repair ONLY email-format syntax validation in cloned Draft2020-12 dialect, retaining all other formats/assertions, inherited local-part/length behavior, IPv4 and IPv6 domain-literal semantics, and proper IDNA/LDH/label/domain length checks without DNS/TLD lookup. No blanket validation disable or arbitrary registered TLD exception list, schema change, new dependency or unrelated behavior.
- JDBC-JSONB-EXISTENCE P1: s18 scoped GET files returned503, real PostgreSQL JDBC reported unbound parameter13. Four raw JSONB question-mark operators in EvidenceAccess SQL are interpreted as positional binds. Same source pattern exists in ReportingScope and ListQueries timeline lag. Replace only such prepared-SQL operators with PostgreSQL jsonb_exists(body,'key'), preserving null/key-existence behavior, recursive ancestry, predicate grouping, bind order, authorization and scope. Inspect Java sources for equivalent occurrences. Do not change historical migration SQL, where raw operators are valid and not JDBC binds.

## Ownership and Git

Work only D:/contract-document-dashboard-worktrees/be-smoke-fixes, feature/backend-smoke-fixes based on ef33bae. Implementation owns shared/ContractRegistry.java, one narrowly scoped email-format helper as necessary, files/EvidenceAccess.java, reporting/ReportingScope.java and shared/ListQueries.java. No FE/canonical/migration/runtime/main.env edits. Root owns task/analysis/Git publication; separate reviewer owns review verdict. Commit only owned application paths after self-review; preserve baseline and current HTTP run unchanged. Do not deploy/copy/replace the running JAR yourself.

## Verification and handoff

Allowed source/static review and Maven clean/package with -Dmaven.test.skip=true, skipping test resources, compilation and execution. No automated test code/run, HTTP or runtime from developer; operator alone runs individual manual requests after separate review and explicit versioned handoff. Record exact fix commit/JAR SHA/build evidence. Independent reviewer must inspect syntax edge behavior and SQL scope/parameter invariants. New JAR is a separately identified candidate, never relabelled as original baseline. Original failures remain FAIL evidence; a later successful candidate rerun is a separate result. No PR merge or main/dev push.
## Source checkpoint30October2026

Application repair81c3d7f66628a397aa9c2d62dd26877448c25d10 independently source-reviewed.108-source clean/package passed30Oct07:51:56+07, all test phases skipped; JARb29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3. Source task done; candidate runtime/manual retest remains pending and original baseline failures remain FAIL. No full acceptance claimed.
