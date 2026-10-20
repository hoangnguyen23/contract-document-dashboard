---
id: backend-final-invariant-repair
scope: historical Round eligibility, completed original signing timestamp and Notice retry audit
status: done
depends-on: [backend-control-completion]
---

# Final crossmodule invariant repair

## Objective

Close the independently identified source blockers before whole-backend closure. Read canonical A3.1/A6.1 and B6, the final crossmodule review, the existing Control historical eligibility repair, original SigningService and ChangeSigning completed-time pattern.

1. DOSSIER and CHANGE: a valid APPROVED step is historical evidence; later L2 decisions/reassignment must not invalidate it because the earlier actor changed role/active/scope/grants. Preserve all reviewer IDs when enforcing L1/L2 distinctness. Every new action still requires current caller eligibility/assignment/scope/SoD and every undecided reviewer required by the workflow is checked currently. Policy evaluation must preserve the same historical boundary through a narrow source-authorized internal port; ordinary simulation and current evaluation behavior remain unchanged. A caller-supplied historical flag or filtering a BLOCKED result is not proof. No approved-step reassignment, source rewriting, role widening or cached replay bypass.
2. Original signing: complete every slow evidence manifest/private-byte/association guard before capturing the authoritative verification instant. Recheck strict start boundary using that completed instant, and use the same instant for packet verifiedAt, contract readinessVerifiedAt and immutable assessment assessed_at. No backdated readiness or premature activation. Preserve existing current auth, transaction, idempotency, lifecycle maintenance and durable effectivity intent behavior.

3. Notice ARTIFACT_PDF manual retry: ensure the delegated NoticeJobs.retry path records mutation audit with the canonical reason in the same transaction. A local Notice event alone does not replace canonical audit. Preserve current authorization, idempotency and retry source guards.

4. Audit/list/export ancestry must include DataRequest finite recordIds and its File/Job children, using the actual per-record scoped Privacy predicate before pagination. Authorized assigned MAKER must see its own scoped audit; never replace selected-record scope with branch-only DATA_OFFICER/AUDITOR access. Coordinate SQL helper ownership with Privacy developer.

## Ownership

Implementation owns only approval/ApprovalService.java, change/ChangeApproval.java signing/SigningService.java notice/NoticeJobs.java and the necessary policy/PolicyAccess.java + policy/PolicyEvaluator.java internal port and reporting/ReportingScope.java plus a concise implementation note if necessary. Privacy developer separately owns Privacy/Files/Customer/V013 and must not overlap. No canonical/FE/migration change expected. Separate reviewer owns the final crossmodule review report. Root manages documentation/Git/publication.

## Verification

Independent static source re-review against the exact repair commit, then coordinated final cumulative Maven clean/package with maven.test.skip=true after Privacy fixes. No tests written/run, HTTP/smoke/PDF/runtime/byte-disposal/business-data mutation. Do not run concurrent Maven builds. No85-complete verdict until both repairs and Privacy semantic review close. No main/dev push or PR merge.
Source task completed20October: final appe718ccc5f7f6ba4d3881d924174440bedfbf7db1, Privacy review1595418 plus separate crossmodule review; 107-source clean/package PASS2026-10-20T10:07:55+07:00; all test phases skipped; Java25.0.1/Maven3.10.0; exact app e718ccc5f7f6ba4d3881d924174440bedfbf7db1. No runtime/business/FE acceptance implied.
