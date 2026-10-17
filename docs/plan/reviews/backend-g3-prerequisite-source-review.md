# Existing original workflow prerequisite review

Reviewed snapshot: af7d109eec49e7e6d438afc30dde522f87cda15a. Independent reviewer: g3_existing_flow_review. Read-only canonical/dependency review for task backend-g3-signing.

## Bounded conclusion

No concrete P1/P2 source blocker was identified within Dossier -> immutable Revision -> DOSSIER Approval Round -> original PREPARED -> ARTIFACT_PDF -> ClamAV -> private authenticated download. This permits further source implementation; it is not workflow runtime acceptance.

- ApprovalService 69-103 captures actual Relationship/Party, exact evidence and immutable Revision/history; 135-151 joins final approval and real mandatory-transaction original preparation; 178-207 rechecks current source/reviewers/policy.
- OriginalAgreementService 17-29 creates one real Contract, immutable source and durable intent atomically.
- V006 protects immutable revisions/assessments/decisions and closed rounds. V007 protects unique original/source/artifact bindings.
- ArtifactJobs 24-31 renders and actually scans, keeps guarded pending bytes through completion, fences each lease and atomically binds File/artifact/Contract/job/audit.
- GeneratedArtifactFiles 21-27 records actual GENERATED metadata, null uploader, scanner outcome and NOT_CHECKED human status.
- FileService 98-107 and FileController 35 enforce current parent/FULL permission, expected-byte integrity, safe redacted refusal and authenticated attachment/audit.
- CommandExecutor 35-54 rechecks authorization before cached replay and validates canonical responses before transaction commit.

Line anchors describe the reviewed snapshot, not future rewritten files.

## Evidence boundary

Prior independent package: 64 Java sources, tests fully skipped; application ac85ece; JAR SHA256 056256308037ed47c8dff01d30b553d510a5667a857331e2c0a1d2621dd9acca. Prior actual operational report: V001-V007 successful, ApplicationReady/renderer/worker bean initialization, zero workflow rows. Fresh passive review observed listener127.0.0.1:8081/PID5292 and healthy project PostgreSQL/ClamAV on56432/3310.

No successful final-approval business request, rendered/scanned artifact or authenticated download has been observed. No tests, HTTP calls, manual rendering, scanner exercise, SQL business mutation or Docker change was executed by this review. Synthetic smoke requires a separately presented concrete scope and owner approval.

## Next tranche prerequisites

Immutable packet input and separate assessment; exact source ancestry/hash; cumulative upload/source/packet contributors; current independent verification; two authority instants; strict server readiness cutoff; current material/mandatory/hold guards; real effectivity and G5 creation consequences; maintenance before stale commands with correct transaction boundaries. The six-operation bounded task describes these integrations. No canonical API/schema change is planned.