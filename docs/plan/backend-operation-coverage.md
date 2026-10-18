# Backend operation coverage

Canonical A/B1.2.2 defines85 operations:58 implemented,6 partial and21 not implemented at reviewed G4 source7e2975c/review2310638. These are semantic source classifications, not runtime/business or FE acceptance. Independent84-source package passed with every test phase skipped.

| Method | Path | Canonical operationId | Source coverage | Limitation |
|---|---|---|---|---|
| GET | `/api/v1/audit-events` | `listAuditEvents` | implemented | Scoped reporting source reviewed;78-source package passed; HTTP/business/FE acceptance pending. |
| GET | `/api/v1/branches` | `listBranchs` | implemented | Runtime verification pending. |
| POST | `/api/v1/branches` | `createBranch` | implemented | Runtime verification pending. |
| GET | `/api/v1/branches/{id}` | `getBranch` | implemented | Runtime verification pending. |
| PUT | `/api/v1/branches/{id}` | `updateBranch` | implemented | Runtime verification pending. |
| GET | `/api/v1/changes` | `listChanges` | implemented | Source implemented; HTTP/business/FE acceptance pending. |
| POST | `/api/v1/changes` | `createChange` | implemented | Source implemented; HTTP/business/FE acceptance pending. |
| GET | `/api/v1/changes/{id}` | `getChange` | implemented | Source implemented; HTTP/business/FE acceptance pending. |
| PUT | `/api/v1/changes/{id}` | `updateChange` | implemented | Source implemented; HTTP/business/FE acceptance pending. |
| POST | `/api/v1/changes/{id}/commands` | `commandChange` | partial | TARIFF_NOTICE readiness and existing Notice cleanup await real Notice module; no business acceptance. |
| GET | `/api/v1/contact-requests` | `listContactRequests` | implemented | Runtime verification pending. |
| POST | `/api/v1/contact-requests` | `createContactRequest` | implemented | Runtime verification pending. |
| GET | `/api/v1/contact-requests/{id}` | `getContactRequest` | implemented | Runtime verification pending. |
| POST | `/api/v1/contact-requests/{id}/commands` | `commandContactRequest` | implemented | Runtime verification pending. |
| GET | `/api/v1/contracts` | `listContracts` | implemented | Original signing/readiness/timeline source implemented; actual business/runtime acceptance pending. |
| GET | `/api/v1/contracts/{id}` | `getContract` | implemented | Original signing/readiness/timeline source implemented; actual business/runtime acceptance pending. |
| POST | `/api/v1/contracts/{id}/commands` | `commandContract` | implemented | Original HOLD/RELEASE/CANCEL; current guards/history; business acceptance pending. |
| GET | `/api/v1/contracts/{id}/versions` | `listContractVersions` | implemented | Immutable original history, number DESC/id DESC with bound cursor; G4 supplies future lineage. |
| GET | `/api/v1/control-requests` | `listControlRequests` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/control-requests` | `createControlRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/control-requests/{id}` | `getControlRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/control-requests/{id}/commands` | `commandControlRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/dashboard` | `getDashboard` | implemented | Scoped reporting source reviewed;78-source package passed; HTTP/business/FE acceptance pending. |
| GET | `/api/v1/data-requests` | `listDataRequests` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/data-requests` | `createDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/data-requests/{id}` | `getDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/data-requests/{id}/commands` | `commandDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/dossiers` | `listDossiers` | implemented | Runtime verification pending. |
| POST | `/api/v1/dossiers` | `createDossier` | implemented | Runtime verification pending. |
| GET | `/api/v1/dossiers/{id}` | `getDossier` | implemented | Runtime verification pending. |
| PUT | `/api/v1/dossiers/{id}` | `updateDossier` | implemented | Runtime verification pending. |
| POST | `/api/v1/dossiers/{id}/commands` | `commandDossier` | implemented | Real dossier submit/frozen Revision and withdrawal; final DOSSIER approval creates real PREPARED original and durable PDF intent. |
| GET | `/api/v1/exports/audit` | `exportAuditCsv` | implemented | Scoped reporting source reviewed;78-source package passed; HTTP/business/FE acceptance pending. |
| GET | `/api/v1/files` | `listFiles` | implemented | Runtime verification pending. |
| POST | `/api/v1/files` | `uploadFile` | implemented | Runtime verification pending. |
| DELETE | `/api/v1/files/{id}` | `deleteUnattachedFile` | implemented | Runtime verification pending. |
| GET | `/api/v1/files/{id}` | `getFile` | implemented | Runtime verification pending. |
| POST | `/api/v1/files/{id}/commands` | `commandFile` | implemented | Runtime verification pending. |
| GET | `/api/v1/files/{id}/content` | `downloadFile` | implemented | FULL checks parent/current grants; REDACTED fails with 409 until a verified safe representation exists. No original fallback. |
| GET | `/api/v1/incidents` | `listIncidents` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/incidents` | `createIncident` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/incidents/{id}` | `getIncident` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/incidents/{id}/commands` | `commandIncident` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/jobs` | `listJobs` | implemented | Runtime verification pending. |
| GET | `/api/v1/jobs/{id}` | `getJob` | implemented | Runtime verification pending. |
| POST | `/api/v1/jobs/{id}/commands` | `commandJob` | partial | Notice artifact/dispatch and data disposition retry dependencies remain missing. |
| GET | `/api/v1/notices` | `listNotices` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/notices` | `createNotice` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/notices/{id}` | `getNotice` | not implemented | Later gate; schema availability alone does not register a handler. |
| PUT | `/api/v1/notices/{id}` | `updateNotice` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/notices/{id}/capture` | `getNoticeCapture` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/notices/{id}/commands` | `commandNotice` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/obligations` | `listObligations` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/obligations/{id}` | `getObligation` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/obligations/{id}/commands` | `commandObligation` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/parties` | `listPartys` | implemented | Runtime verification pending. |
| POST | `/api/v1/parties` | `createParty` | implemented | Runtime verification pending. |
| GET | `/api/v1/parties/{id}` | `getParty` | implemented | Runtime verification pending. |
| PUT | `/api/v1/parties/{id}` | `updateParty` | implemented | Runtime verification pending. |
| GET | `/api/v1/policies` | `listPolicys` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| POST | `/api/v1/policies` | `createPolicy` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| GET | `/api/v1/policies/{id}` | `getPolicy` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| PUT | `/api/v1/policies/{id}` | `updatePolicy` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| POST | `/api/v1/policies/{id}/commands` | `commandPolicy` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| POST | `/api/v1/policies/{id}/simulation` | `simulatePolicy` | implemented | Policy source reviewed; compilation passed; runtime and business acceptance pending. |
| GET | `/api/v1/relationships` | `listRelationships` | implemented | Runtime verification pending. |
| POST | `/api/v1/relationships` | `createRelationship` | implemented | Runtime verification pending. |
| GET | `/api/v1/relationships/{id}` | `getRelationship` | implemented | Runtime verification pending. |
| PUT | `/api/v1/relationships/{id}` | `updateRelationship` | implemented | Runtime verification pending. |
| GET | `/api/v1/reviewer-options` | `getReviewerOptions` | implemented | Current scoped eligible reviewers for an existing target; later aggregate types depend on later modules. |
| GET | `/api/v1/rounds` | `listRounds` | partial | DOSSIER/CHANGE implemented; CONTROL_REQUEST adapter remains absent; business acceptance pending. |
| GET | `/api/v1/rounds/{id}` | `getRound` | partial | DOSSIER/CHANGE implemented; CONTROL_REQUEST adapter remains absent; business acceptance pending. |
| POST | `/api/v1/rounds/{id}/commands` | `commandRound` | partial | DOSSIER/CHANGE implemented; CONTROL_REQUEST adapter remains absent; business acceptance pending. |
| GET | `/api/v1/rounds/{id}/snapshot` | `getRoundSnapshot` | partial | DOSSIER/CHANGE implemented; CONTROL_REQUEST adapter remains absent; business acceptance pending. |
| GET | `/api/v1/session` | `getSession` | implemented | Runtime verification pending. |
| POST | `/api/v1/session/login` | `login` | implemented | Runtime verification pending. |
| POST | `/api/v1/session/logout` | `logout` | implemented | Runtime verification pending. |
| GET | `/api/v1/signing-packets` | `listSigningPackets` | implemented | CONTRACT and negotiated CHANGE implemented; HTTP/business acceptance pending. |
| POST | `/api/v1/signing-packets` | `createSigningPacket` | implemented | CONTRACT and negotiated CHANGE implemented; HTTP/business acceptance pending. |
| GET | `/api/v1/signing-packets/{id}` | `getSigningPacket` | implemented | CONTRACT and negotiated CHANGE implemented; HTTP/business acceptance pending. |
| POST | `/api/v1/signing-packets/{id}/commands` | `commandSigningPacket` | implemented | CONTRACT and negotiated CHANGE implemented; HTTP/business acceptance pending. |
| GET | `/api/v1/users` | `listUsers` | implemented | Runtime verification pending. |
| POST | `/api/v1/users` | `createUser` | implemented | Runtime verification pending. |
| GET | `/api/v1/users/{id}` | `getUser` | implemented | Runtime verification pending. |
| PUT | `/api/v1/users/{id}` | `updateUser` | implemented | Runtime verification pending. |

## Verification boundary

Source review covers role/scope/assignment, stable contributor SoD, closed schema/nullability, current auth before idempotency replay, transactional response validation, private finalized byte references, scanner source/lease guards and migration definitions. Build and extraction evidence appears in docs/plan/reviews/backend-foundation-follow-up.md. Operational startup, scanner connectivity and Flyway001–008 have been observed separately in the primary runtime report. HTTP scope/session behavior, FE browser integration, actual PDF/signing/activation workflows, races/recovery and integrated acceptance remain pending.

PowerShell package from `apps/api`: `.\mvnw.cmd '-Dmaven.test.skip=true' package`; quote the full Maven `-D` argument. This skips both test compilation and execution.


Original agreement artifact tranche: final dossier APPROVE now invokes a real mandatory-transaction original-contract port, persists immutable render source and durable PDF intent. FILE_SCAN and original CONTRACT ARTIFACT_PDF/EFFECTIVITY retry paths are implemented; other job kinds remain partial. Original signing, readiness commands and immutable version/effectivity source are implemented, with real creation-only G5 consequence factories. Negotiated CHANGE signing/renewal/apply are now implemented; tariff Notice, public G5 and termination remain incomplete. Compilation is not artifact rendering, scanner/provider or business acceptance.

Current source review and 73-source package passed at 6585c4f; primary V008/bean initialization also passed. These do not establish business acceptance. S060 and S024 maker hint corrections are owner-approved and internally reviewed at A/B1.2.2; actual external FE acknowledgement remains pending.

Current G4: negotiated Change CRUD/approval/signing/commit/rebase/cancel/timeline source reviewed,84-source package PASS. V009 has not been applied at this checkpoint. Notice/tariff readiness remains partial; CONTROL_REQUEST Round and future Notice/Data jobs remain partial. See reviews/backend-change-source-review.md.

Current corrected G4 source GO2af915d/review86035f9;84-source skipped-test package PASS18October18:59:20+07, JAR SHA2568bc3554e12edfe0c4bf180dec6b8c109c8135227ce6c994ce1aa2a18356b493d. The earlier review missed an enum violation in TERM_REVIEW supersession, now repaired and explicitly recorded. V009/startup was observed on prior7e2975c package only; this corrected package has not been started.
