# Independent dossier approval source review

Review date: 17 October 2026, Asia/Ho_Chi_Minh. Reviewer: independent Backend contract reviewer; implementation and repairs were made by a separate developer.

## Verdict and exact boundary

**GO for the bounded dossier approval source tranche at `2af9f50aabebd59f2ab5c545b6558bcfca5b9eb0`.** The initial implementation `54ac270a47f86fbebc3006e4c8676cf1ad7f974a` had the three P2 findings below. They are closed by the reviewed repair commit. No unresolved P1/P2 source blocker was found within the declared scope.

This verdict permits continuing sequential Backend implementation. It is not integrated G3 acceptance or a complete original agreement workflow. The four Round operations implement DOSSIER targets only. CHANGE and CONTROL_REQUEST adapters are absent. Final APPROVE deliberately returns HTTP 503 SERVICE_UNAVAILABLE until a genuine OriginalAgreementPort creates the original contract and durable artifact intent in the same transaction. There is no success placeholder or default port bean.

Authorities: canonical A1.2.0 §§3.1, 4.2, 5.1–5.3, 14; B1.2.0 §§4, 6–7; extracted OpenAPI Revision, Round, RoundSnapshot, EvidenceCheck, MandatoryCheck and command schemas; seed-v1.hashConventions. Canonical files were not changed during this review.

## Findings and verified repairs

| ID | Initial finding at 54ac270a | Repair verified at 2af9f50a |
|---|---|---|
| P2-01 | DossierService.project called requireSubmissionReady while ListQueries.page held a read-only transaction. The hint resolved policies through requireApplicable and SELECT FOR UPDATE, which PostgreSQL rejects in that transaction. | PolicyAccess.findApplicableHint performs a plain interval/entity SELECT. It has no transactional annotation, activation or row lock. ApprovalService uses it only for advisory projection; actual SUBMIT retains requireApplicable and its serialization. |
| P2-02 | Before the first ACTIVE policy, the transactional resolver threw POLICY_BLOCKED inside draft create/update. Catching that exception to build blockedActions did not clear the outer transaction's rollback-only marker, so an otherwise valid draft save failed. | Missing hint availability is Optional.empty, converted into an advisory exception outside a transactional proxy boundary and caught by the projection. Draft persistence no longer enters the unavailable-policy resolver. The empty-schedule business SUBMIT still fails closed. |
| P2-03 | A reviewer could introduce a LEGAL_CONDITION or SERVICE_READINESS blocker in Round.mandatoryChecks, then RETURN/WITHDRAW. The dossier retained only its original submission set, so resubmission could omit the new known condition. | RETURN/REJECT and WITHDRAW copy the latest round condition set into mutable Dossier.mandatoryChecks. SUBMIT retainConditions now sees this cumulative applicability. Later VERIFY also retains known codes. Immutable Revision/history is not rewritten. |

Repair locations: PolicyAccess.java:18–23; ApprovalService.java:45–46, 83, 117, 159 and 174. The original read-only-list and rollback-only findings were independently corroborated by a second source reviewer. These are source conclusions; no request/scenario was executed to demonstrate them.

## Source and security checks

- Request bodies retain the canonical discriminated command schemas and closed property sets. Required nullable fields remain present, including l2ReviewerId, authority source/check fields, and undecided step decision fields. Revision deliberately has no mutable Entity version/branch/action fields. Snapshot returns the exact DOSSIER discriminated shape. CommandExecutor validates successful response bodies before durable state/cache commit.
- Current branch/relationship assignment controls dossier and Round access. List scopes are applied in SQL before pagination, including current maker assignment. Ordinary AUDITOR projections mask both mutable and nested historical contact/representation facts; composite business roles use actual assignment and current permissions rather than a MAKER label alone.
- Submission and decisions recheck eligible active reviewer role/branch, distinct L1/L2, stable cumulative contributor IDs and submittedBy exclusion. Role changes or reassignment do not remove those exclusions. Sensitive evidence requires each nominated reviewer's SENSITIVE_READ grant. Reassignment requires an independent current MANAGER and keeps dueAt/history unchanged.
- Round commands authorize with current grants and assignment before idempotency replay and again under the command transaction. Authorization does not require the previous OPEN/PENDING state before replay. New effects require the current ETag, OPEN Round and matching current IN_REVIEW dossier binding. In-progress/fingerprint/replay handling remains the shared CommandExecutor behavior.
- The new service dependencies do not introduce an eager DossierService/ApprovalService bean cycle: DossierService uses ObjectProvider<DossierApprovalBridge>, while access/evidence dependencies resolve through DossierAccess and CustomerAccess. This is static inspection, not a Spring startup assertion.

## Frozen source, evidence and assessment

Submission holds the real dossier lock and locks the current Relationship. It checks active assignment, Vietnam tomorrow-or-later start, supported fixed template/bundle and canonical SubmittedRepresentation. It uses confirmedContact under that Relationship lock, not the stale draft contactSnapshot or a pending contact edit. The locked Party identity and contactVersion are retained internally for the future original agreement boundary.

ApprovalInputs.evidence locks actual File IDs in UUID order, requires same dossier/branch CLEAN metadata, a real association, metadata/blob SHA equality and a private byte integrity read. It produces purpose/fileId/sha256 digests from server metadata. Historical associations are retained when freezing. Current approval rechecks private bytes and the stored evidence projection; it does not trust a simulation flag or a global human-verified File field.

RevisionStore persists a separate immutable row. Facts hashing uses exactly terms, representation, contact, externalAccountReference, templateVersion and bundleVersion. Evidence hashing sorts purpose then fileId; document reference order stays in the Revision. JsonSupport applies NFC normalization and canonical JSON hashing. Internal source metadata is not added to the closed wire Revision. Each Round snapshot resolves its own immutable revisionId/dossier pair, so later draft edits and resubmissions cannot substitute their source.

VERIFY_EVIDENCE is assigned, independent, pending L1 only. It records a separate immutable assessment, validates exact frozen file membership, duplicate IDs/codes and nonfuture timestamps. It preserves known condition applicability without inventing an unconditional three-code checklist: the canonical min1 condition set and evidenceFileIds=[] remain valid shapes. BLOCK/REVIEW_REQUIRED values cannot pass policy evaluation.

Current authority evaluation clears maker-authored checkedAt/checkEvidenceFileId from its temporary input. Only a current L1 PASS EvidenceCheck for the exact frozen representation.sourceFileId plus authorityChecked=true supplies the assessment overlay. The latest assessment actor must still be the assigned L1. Reassigning L1 clears review checks/authority flag. The frozen source and its factsHash remain unchanged. Current validFrom/validTo use the reviewed inclusive/exclusive interval and knownRevocationAt action instant semantics. Every required evidence document must have a human PASS independently of scanner CLEAN.

## Transaction, locking and history checks

CommandExecutor's TransactionTemplate owns new effects, response validation, audit and idempotency cache as one unit. Submission enters with Dossier locked, then Relationship; Round mutations lock Dossier → Relationship → Round before source effects, and File locks use a stable UUID order. WITHDRAW starts with the caller-held Dossier and locks its current Round. Relationship/Party administrative paths introduce no reversed dossier lock dependency in the inspected source. No @Transactional self-invocation or autocommit row-lock illusion was found in these command paths.

V003 retains the unique OPEN DOSSIER Round index using targetKind/targetId. V006 adds unique revision numbers and (round_id, level) decisions, immutable Revision/assessment/evaluation/reassignment/decision records, immutable closed Rounds, immutable source bindings and protection for already decided steps. L1 APPROVED promotes L2 WAITING to PENDING. RETURN/REJECT close the whole Round and cancel only undecided steps; WITHDRAW preserves prior APPROVED decisions and returns the dossier to DRAFT. Closed Rounds are not reopened; resubmission creates a new numbered Revision and current-policy Round.

Current decisions revalidate the pinned published policy, roles/SoD, Party identity, files, mandatory conditions and assessment. Later policy retirement does not replace the submitted policy/config. Review dueAt uses the pinned reviewDueWorkingDays and demo business calendar without resetting on reassignment.

For the currently absent OriginalAgreementPort, final APPROVE throws inside the command transaction before a decision or target approval is persisted. The preceding attempted current evaluation insert is also rolled back; audit and idempotency success are not cached. The future real port must retain this transaction and bind one PREPARED original, frozen Party/template/bundle source and real durable PDF intent. That implementation is outside this verdict.

## Operation coverage

| Operation | Source status after this tranche |
|---|---|
| commandDossier | All canonical dossier actions have handlers: existing CANCEL/CLONE plus real SUBMIT/WITHDRAW. Final approval belongs to commandRound and remains unavailable as described above. |
| listRounds | Partial: DOSSIER only, SQL scope/filter/cursor behavior inherited from ListQueries. |
| getRound | Partial: DOSSIER only, current parent access and ETag representation. |
| getRoundSnapshot | Partial: exact historical DOSSIER Revision with current parent access/redaction. |
| commandRound | Partial: DOSSIER VERIFY_EVIDENCE, REASSIGN, RETURN, REJECT and nonfinal L1 APPROVE; final APPROVE requires the genuine original contract/PDF port. |

The full 85-row coverage matrix currently contains 43 implemented operations, 5 partial operations and 37 not implemented. The five partial entries are the four Round routes and commandJob. Source coverage does not mean runtime/business acceptance.

## Independent package evidence

Final reviewed application source: `2af9f50aabebd59f2ab5c545b6558bcfca5b9eb0`. Worktree was clean before this report was written.

PowerShell command from apps/api:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

BUILD SUCCESS, completed `2026-10-17T13:11:21+07:00`, Maven 3.10.0, Oracle JDK 25.0.1, Windows 11 amd64. Javac compiled 59 source files with release 25. Test resources were not copied, test sources were not compiled, and Surefire reported Tests are skipped. The build emitted nonblocking optional-feed metadata HTTP 401 warnings and an existing deprecated-API notice; it completed without an application compiler error.

Packaged JAR SHA-256: `0ca1e6b2be52f43f9f6a04491e15d9ce79bdd49b34fc7db29b2ce56c8509e747` for apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar. This report-only commit does not change packaged application source.

No tests, HTTP business requests, product regression execution, Docker actions, SQL execution, file-deletion/race scenarios or application startup were performed in this review. V006 PostgreSQL execution, actual DI/startup, security/session/ETag/replay behavior, concurrency/recovery, PDF integration and real FE/BE acceptance remain unverified. Prior runtime evidence is separate; G1/G2/G3 acceptance is not claimed.

