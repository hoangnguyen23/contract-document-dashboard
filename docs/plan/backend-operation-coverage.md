# Backend operation coverage

## Current checkpoint — 30 October 2026

Owner resumed after Docker recovery and explicitly separated testing from implementation. No further automated tests, manual HTTP smoke or business scenarios are run under this continuation. Earlier21October smoke evidence and FAILs remain historical, not erased or relabelled as repaired runtime results.

Current reviewed application repair is81c3d7f66628a397aa9c2d62dd26877448c25d10 on feature/backend-smoke-fixes, Draft PR11. Email format syntax now supports canonical synthetic domains without disabling other formats; ten JDBC JSONB operators use jsonb_exists with scope/argument order preserved. Independent source review and108-source skipped-test package passed30Oct07:51:56+07; JAR SHA256 b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3. This is source/build evidence; the original three failing HTTP cases have NOT been rerun on this candidate.

Remote devdf34af6 contains merged PR2–10 and has the same tree as the old ef33bae delivery; PR11 remains separate, with no automatic merge or main/dev push. Source inventory remains85 implemented/0 partial/0 missing, with business/FE/NFR acceptance pending. FE/setup/docs/test branches audited previously contain no implementation. Local Java25.0.1 still differs from pinned25.0.4.1.

Operational readiness for this exact candidate is recorded separately in reviews/backend-repair-runtime-20261030.md. No Clock XML, synthetic time jump, fault injection, data reset, volume deletion or smoke workflow is part of this continuation. Historical statements below describe their recorded dates and sources.


Canonical A/B1.3.0: **85 implemented / 0 partial / 0 missing** after bounded independent Privacy review1595418 and final crossmodule repair review. Exact application source e718ccc5f7f6ba4d3881d924174440bedfbf7db1. This is semantic source coverage, not executed business or FE acceptance.

Cumulative 107-source clean/package PASS2026-10-20T10:07:55+07:00; all test phases skipped; Java25.0.1/Maven3.10.0; exact app e718ccc5f7f6ba4d3881d924174440bedfbf7db1. JAR SHA256 `fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602`. Final operational startup/migration is recorded separately in [runtime evidence](reviews/backend-final-runtime-startup.md). No automated tests, HTTP scenarios, manual PDF or actual disposition was executed.

| Method | Path | Canonical operationId | Source coverage | Acceptance limitation |
|---|---|---|---|---|
| GET | `/api/v1/session` | `getSession` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/session/login` | `login` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/session/logout` | `logout` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/users` | `listUsers` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/users` | `createUser` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/users/{id}` | `getUser` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/users/{id}` | `updateUser` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/branches` | `listBranchs` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/branches` | `createBranch` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/branches/{id}` | `getBranch` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/branches/{id}` | `updateBranch` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/parties` | `listPartys` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/parties` | `createParty` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/parties/{id}` | `getParty` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/parties/{id}` | `updateParty` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/relationships` | `listRelationships` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/relationships` | `createRelationship` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/relationships/{id}` | `getRelationship` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/relationships/{id}` | `updateRelationship` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/contact-requests` | `listContactRequests` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/contact-requests` | `createContactRequest` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/contact-requests/{id}` | `getContactRequest` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/contact-requests/{id}/commands` | `commandContactRequest` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/dossiers` | `listDossiers` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/dossiers` | `createDossier` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/dossiers/{id}` | `getDossier` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/dossiers/{id}` | `updateDossier` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/dossiers/{id}/commands` | `commandDossier` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/rounds` | `listRounds` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/rounds/{id}` | `getRound` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/rounds/{id}/commands` | `commandRound` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/policies` | `listPolicys` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/policies` | `createPolicy` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/policies/{id}` | `getPolicy` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/policies/{id}` | `updatePolicy` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/policies/{id}/commands` | `commandPolicy` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/policies/{id}/simulation` | `simulatePolicy` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/contracts` | `listContracts` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/contracts/{id}` | `getContract` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/contracts/{id}/commands` | `commandContract` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/contracts/{id}/versions` | `listContractVersions` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/signing-packets` | `listSigningPackets` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/signing-packets` | `createSigningPacket` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/signing-packets/{id}` | `getSigningPacket` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/signing-packets/{id}/commands` | `commandSigningPacket` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/changes` | `listChanges` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/changes` | `createChange` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/changes/{id}` | `getChange` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/changes/{id}` | `updateChange` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/changes/{id}/commands` | `commandChange` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/notices` | `listNotices` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/notices` | `createNotice` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/notices/{id}` | `getNotice` | implemented | Business/HTTP/FE acceptance pending. |
| PUT | `/api/v1/notices/{id}` | `updateNotice` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/notices/{id}/commands` | `commandNotice` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/obligations` | `listObligations` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/obligations/{id}` | `getObligation` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/obligations/{id}/commands` | `commandObligation` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/incidents` | `listIncidents` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/incidents` | `createIncident` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/incidents/{id}` | `getIncident` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/incidents/{id}/commands` | `commandIncident` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/control-requests` | `listControlRequests` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/control-requests` | `createControlRequest` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/control-requests/{id}` | `getControlRequest` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/control-requests/{id}/commands` | `commandControlRequest` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/data-requests` | `listDataRequests` | implemented | Finite workflows and recovery source reviewed; actual disposition/hold races unexecuted. |
| POST | `/api/v1/data-requests` | `createDataRequest` | implemented | Finite workflows and recovery source reviewed; actual disposition/hold races unexecuted. |
| GET | `/api/v1/data-requests/{id}` | `getDataRequest` | implemented | Finite workflows and recovery source reviewed; actual disposition/hold races unexecuted. |
| POST | `/api/v1/data-requests/{id}/commands` | `commandDataRequest` | implemented | Finite workflows and recovery source reviewed; actual disposition/hold races unexecuted. |
| GET | `/api/v1/files` | `listFiles` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/files` | `uploadFile` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/files/{id}` | `getFile` | implemented | Business/HTTP/FE acceptance pending. |
| DELETE | `/api/v1/files/{id}` | `deleteUnattachedFile` | implemented | Business/HTTP/FE acceptance pending. |
| POST | `/api/v1/files/{id}/commands` | `commandFile` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/files/{id}/content` | `downloadFile` | implemented | Real reviewed REDACTED binding; no FULL fallback; download acceptance unexecuted. |
| GET | `/api/v1/jobs` | `listJobs` | implemented | All5 Job kinds traced; crash/retry/race acceptance unexecuted. |
| GET | `/api/v1/jobs/{id}` | `getJob` | implemented | All5 Job kinds traced; crash/retry/race acceptance unexecuted. |
| POST | `/api/v1/jobs/{id}/commands` | `commandJob` | implemented | All5 Job kinds traced; crash/retry/race acceptance unexecuted. |
| GET | `/api/v1/audit-events` | `listAuditEvents` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/dashboard` | `getDashboard` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/exports/audit` | `exportAuditCsv` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/reviewer-options` | `getReviewerOptions` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/notices/{id}/capture` | `getNoticeCapture` | implemented | Business/HTTP/FE acceptance pending. |
| GET | `/api/v1/rounds/{id}/snapshot` | `getRoundSnapshot` | implemented | Business/HTTP/FE acceptance pending. |

## Evidence and limitations

- [Privacy review](reviews/backend-privacy-source-review.md): four operations, six commands, finite File/Relationship actions, actual durable effects and guards.
- [Crossmodule review](reviews/backend-final-crossmodule-source-review.md): repaired original signing completion time, historical Round authority, Notice retry audit and per-record DataRequest audit ancestry; initial findings retained.
- Earlier module reviews remain bounded evidence for foundation, policy, dossier/approval, original contract, signing/timeline, Change, Notice, Obligation/Incident, Control and reporting. Prior staged omissions are superseded only by the later source that implements them.
- All5 Job kinds and all3 Round/reviewer-option targets were traced. Registration alone is not counted as completeness. Inventory retains exact actions, targets, sources, dependencies, permissions and canonical acceptance IDs.
- S024/S060 hints and termination date guard are owner-approved/internal-reviewed canonical1.3.0; actual external FE acknowledgement/consumer execution remains pending. No new API/schema/role/permission/FE change in this final tranche.
- Java25.0.1 used locally differs from pinned25.0.4.1. Fresh isolated migration, actual workflow/PDF/signing/disposition, negative permissions/SoD, concurrency, crash recovery, backup/restore and NFR remain unexecuted.
- [Proposed verification plan](backend-verification-plan.md) is NOT AUTHORIZED / NOT EXECUTED. Source Complete, Build Verified, Runtime Started, Business Verified, Integrated Acceptance and Production Ready are separate verdicts.
