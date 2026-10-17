# Backend operation coverage

Canonical A/B1.2.0 defines 85 operations. This foundation, Policy and dossier-approval tranche registers 56 operations: 47 have their current handler paths implemented and 9 have explicit partial scope. 29 later-gate operations are not implemented. “Implemented” is source coverage, not runtime/provider/business acceptance. No tests were requested or executed; G1/G2 integrated acceptance is not claimed.

| Method | Path | Canonical operationId | Source coverage | Limitation |
|---|---|---|---|---|
| GET | `/api/v1/audit-events` | `listAuditEvents` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/branches` | `listBranchs` | implemented | Runtime verification pending. |
| POST | `/api/v1/branches` | `createBranch` | implemented | Runtime verification pending. |
| GET | `/api/v1/branches/{id}` | `getBranch` | implemented | Runtime verification pending. |
| PUT | `/api/v1/branches/{id}` | `updateBranch` | implemented | Runtime verification pending. |
| GET | `/api/v1/changes` | `listChanges` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/changes` | `createChange` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/changes/{id}` | `getChange` | not implemented | Later gate; schema availability alone does not register a handler. |
| PUT | `/api/v1/changes/{id}` | `updateChange` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/changes/{id}/commands` | `commandChange` | not implemented | Later gate; schema availability alone does not register a handler. |
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
| GET | `/api/v1/dashboard` | `getDashboard` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/data-requests` | `listDataRequests` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/data-requests` | `createDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/data-requests/{id}` | `getDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| POST | `/api/v1/data-requests/{id}/commands` | `commandDataRequest` | not implemented | Later gate; schema availability alone does not register a handler. |
| GET | `/api/v1/dossiers` | `listDossiers` | implemented | Runtime verification pending. |
| POST | `/api/v1/dossiers` | `createDossier` | implemented | Runtime verification pending. |
| GET | `/api/v1/dossiers/{id}` | `getDossier` | implemented | Runtime verification pending. |
| PUT | `/api/v1/dossiers/{id}` | `updateDossier` | implemented | Runtime verification pending. |
| POST | `/api/v1/dossiers/{id}/commands` | `commandDossier` | implemented | Real dossier submit/frozen Revision and withdrawal; final DOSSIER approval creates real PREPARED original and durable PDF intent. |
| GET | `/api/v1/exports/audit` | `exportAuditCsv` | not implemented | Later gate; schema availability alone does not register a handler. |
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
| POST | `/api/v1/jobs/{id}/commands` | `commandJob` | partial | RETRY for current FAILED FILE_SCAN, original CONTRACT ARTIFACT_PDF and EFFECTIVITY sources; other job kinds fail closed. |
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
| GET | `/api/v1/rounds` | `listRounds` | partial | DOSSIER adapter implemented; CHANGE/CONTROL_REQUEST adapters absent. Final DOSSIER APPROVE creates the real PREPARED original and durable artifact intent. |
| GET | `/api/v1/rounds/{id}` | `getRound` | partial | DOSSIER adapter implemented; CHANGE/CONTROL_REQUEST adapters absent. Final DOSSIER APPROVE creates the real PREPARED original and durable artifact intent. |
| POST | `/api/v1/rounds/{id}/commands` | `commandRound` | partial | DOSSIER adapter implemented; CHANGE/CONTROL_REQUEST adapters absent. Final DOSSIER APPROVE creates the real PREPARED original and durable artifact intent. |
| GET | `/api/v1/rounds/{id}/snapshot` | `getRoundSnapshot` | partial | DOSSIER adapter implemented; CHANGE/CONTROL_REQUEST adapters absent. Final DOSSIER APPROVE creates the real PREPARED original and durable artifact intent. |
| GET | `/api/v1/session` | `getSession` | implemented | Runtime verification pending. |
| POST | `/api/v1/session/login` | `login` | implemented | Runtime verification pending. |
| POST | `/api/v1/session/logout` | `logout` | implemented | Runtime verification pending. |
| GET | `/api/v1/signing-packets` | `listSigningPackets` | partial | CONTRACT adapter implemented with real signing/readiness effects; CHANGE adapter absent; business acceptance pending. |
| POST | `/api/v1/signing-packets` | `createSigningPacket` | partial | CONTRACT adapter implemented with real signing/readiness effects; CHANGE adapter absent; business acceptance pending. |
| GET | `/api/v1/signing-packets/{id}` | `getSigningPacket` | partial | CONTRACT adapter implemented with real signing/readiness effects; CHANGE adapter absent; business acceptance pending. |
| POST | `/api/v1/signing-packets/{id}/commands` | `commandSigningPacket` | partial | CONTRACT adapter implemented with real signing/readiness effects; CHANGE adapter absent; business acceptance pending. |
| GET | `/api/v1/users` | `listUsers` | implemented | Runtime verification pending. |
| POST | `/api/v1/users` | `createUser` | implemented | Runtime verification pending. |
| GET | `/api/v1/users/{id}` | `getUser` | implemented | Runtime verification pending. |
| PUT | `/api/v1/users/{id}` | `updateUser` | implemented | Runtime verification pending. |

## Verification boundary

Source review covers role/scope/assignment, stable contributor SoD, closed schema/nullability, current auth before idempotency replay, transactional response validation, private finalized byte references, scanner source/lease guards and migration definitions. Build and extraction evidence appears in docs/plan/reviews/backend-foundation-follow-up.md. Operational startup, scanner connectivity and Flyway001–007 have been observed separately in the primary runtime report. V008 migration execution, HTTP scope/session behavior, FE browser integration, actual PDF/signing/activation workflows, races/recovery and integrated acceptance remain pending.

PowerShell package from `apps/api`: `.\mvnw.cmd '-Dmaven.test.skip=true' package`; quote the full Maven `-D` argument. This skips both test compilation and execution.


Original agreement artifact tranche: final dossier APPROVE now invokes a real mandatory-transaction original-contract port, persists immutable render source and durable PDF intent. FILE_SCAN and original CONTRACT ARTIFACT_PDF/EFFECTIVITY retry paths are implemented; other job kinds remain partial. Original signing, readiness commands and immutable version/effectivity source are implemented, with real creation-only G5 consequence factories. CHANGE signing/renewal/apply, public G5 completion and termination remain absent. Compilation is not artifact rendering, scanner/provider or business acceptance.
