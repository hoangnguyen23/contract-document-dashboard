# Backend contract analysis — read-only source review

Canonical A/B 1.0.0. This report is extraction/analysis, not application verification or an official third specification. User requested no tests; no tests or application changes were executed.

## All 85 operations

| Phase | Owner | Method/path | Canonical operationId | Request | Success response |
|---|---|---|---|---|---|
| G1 | platform/identity | GET /session | getSession | — | 200 Session |
| G1 | platform/identity | POST /session/login | login | application/json: LoginRequest | 200 Session |
| G1 | platform/identity | POST /session/logout | logout | — | 204 no body |
| G1-G2 | identity admin | GET /users | listUsers | — | 200 UserPage |
| G1-G2 | identity admin | POST /users | createUser | application/json: UserCreate | 201 User |
| G1-G2 | identity admin | GET /users/{id} | getUser | — | 200 User |
| G1-G2 | identity admin | PUT /users/{id} | updateUser | application/json: UserUpdate | 200 User |
| G1-G2 | identity admin | GET /branches | listBranchs | — | 200 BranchPage |
| G1-G2 | identity admin | POST /branches | createBranch | application/json: BranchCreate | 201 Branch |
| G1-G2 | identity admin | GET /branches/{id} | getBranch | — | 200 Branch |
| G1-G2 | identity admin | PUT /branches/{id} | updateBranch | application/json: BranchUpdate | 200 Branch |
| G2 | customer admin | GET /parties | listPartys | — | 200 PartyPage |
| G2 | customer admin | POST /parties | createParty | application/json: PartyCreate | 201 Party |
| G2 | customer admin | GET /parties/{id} | getParty | — | 200 Party |
| G2 | customer admin | PUT /parties/{id} | updateParty | application/json: PartyUpdate | 200 Party |
| G2 | customer | GET /relationships | listRelationships | — | 200 RelationshipPage |
| G2 | customer | POST /relationships | createRelationship | application/json: RelationshipCreate | 201 Relationship |
| G2 | customer | GET /relationships/{id} | getRelationship | — | 200 Relationship |
| G2 | customer | PUT /relationships/{id} | updateRelationship | application/json: RelationshipUpdate | 200 Relationship |
| G2 | customer | GET /contact-requests | listContactRequests | — | 200 ContactRequestPage |
| G2 | customer | POST /contact-requests | createContactRequest | application/json: ContactRequestCreate | 201 ContactRequest |
| G2 | customer | GET /contact-requests/{id} | getContactRequest | — | 200 ContactRequest |
| G2 | customer | POST /contact-requests/{id}/commands | commandContactRequest | application/json: ContactRequestCommand | 200 ContactRequest |
| G2 draft / G3 commands | dossier | GET /dossiers | listDossiers | — | 200 DossierPage |
| G2 draft / G3 commands | dossier | POST /dossiers | createDossier | application/json: DossierCreate | 201 Dossier |
| G2 draft / G3 commands | dossier | GET /dossiers/{id} | getDossier | — | 200 Dossier |
| G2 draft / G3 commands | dossier | PUT /dossiers/{id} | updateDossier | application/json: DossierUpdate | 200 Dossier |
| G2 draft / G3 commands | dossier | POST /dossiers/{id}/commands | commandDossier | application/json: DossierCommand | 200 Dossier, 201 Dossier |
| G3 | approval | GET /rounds | listRounds | — | 200 RoundPage |
| G3 | approval | GET /rounds/{id} | getRound | — | 200 Round |
| G3 | approval | POST /rounds/{id}/commands | commandRound | application/json: RoundCommand | 200 Round |
| G3 | policy | GET /policies | listPolicys | — | 200 PolicyPage |
| G3 | policy | POST /policies | createPolicy | application/json: PolicyCreate | 201 Policy |
| G3 | policy | GET /policies/{id} | getPolicy | — | 200 Policy |
| G3 | policy | PUT /policies/{id} | updatePolicy | application/json: PolicyUpdate | 200 Policy |
| G3 | policy | POST /policies/{id}/commands | commandPolicy | application/json: PolicyCommand | 200 Policy, 201 Policy |
| G3 | policy | POST /policies/{id}/simulation | simulatePolicy | application/json: PolicySimulation | 200 SimulationResponse |
| G3 | contract | GET /contracts | listContracts | — | 200 ContractPage |
| G3 | contract | GET /contracts/{id} | getContract | — | 200 Contract |
| G3 | contract | POST /contracts/{id}/commands | commandContract | application/json: ContractCommand | 200 Contract |
| G3 | contract | GET /contracts/{id}/versions | listContractVersions | — | 200 ContractVersionPage |
| G3 | contract/signing | GET /signing-packets | listSigningPackets | — | 200 SigningPacketPage |
| G3 | contract/signing | POST /signing-packets | createSigningPacket | application/json: SigningPacketCreate | 201 SigningPacket |
| G3 | contract/signing | GET /signing-packets/{id} | getSigningPacket | — | 200 SigningPacket |
| G3 | contract/signing | POST /signing-packets/{id}/commands | commandSigningPacket | application/json: SigningPacketCommand | 200 SigningPacket |
| G4 | change | GET /changes | listChanges | — | 200 ChangePage |
| G4 | change | POST /changes | createChange | application/json: ChangeCreate | 201 Change |
| G4 | change | GET /changes/{id} | getChange | — | 200 Change |
| G4 | change | PUT /changes/{id} | updateChange | application/json: ChangeUpdate | 200 Change |
| G4 | change | POST /changes/{id}/commands | commandChange | application/json: ChangeCommand | 200 Change |
| G4 | notice | GET /notices | listNotices | — | 200 NoticePage |
| G4 | notice | POST /notices | createNotice | application/json: NoticeCreate | 201 Notice |
| G4 | notice | GET /notices/{id} | getNotice | — | 200 Notice |
| G4 | notice | PUT /notices/{id} | updateNotice | application/json: NoticeUpdate | 200 Notice |
| G4 | notice | POST /notices/{id}/commands | commandNotice | application/json: NoticeCommand | 200 Notice |
| G5 | duty | GET /obligations | listObligations | — | 200 ObligationPage |
| G5 | duty | GET /obligations/{id} | getObligation | — | 200 Obligation |
| G5 | duty | POST /obligations/{id}/commands | commandObligation | application/json: ObligationCommand | 200 Obligation |
| G5 | incident | GET /incidents | listIncidents | — | 200 IncidentPage |
| G5 | incident | POST /incidents | createIncident | application/json: IncidentCreate | 201 Incident |
| G5 | incident | GET /incidents/{id} | getIncident | — | 200 Incident |
| G5 | incident | POST /incidents/{id}/commands | commandIncident | application/json: IncidentCommand | 200 Incident |
| G5 | control | GET /control-requests | listControlRequests | — | 200 ControlRequestPage |
| G5 | control | POST /control-requests | createControlRequest | application/json: ControlRequestCreate | 201 ControlRequest |
| G5 | control | GET /control-requests/{id} | getControlRequest | — | 200 ControlRequest |
| G5 | control | POST /control-requests/{id}/commands | commandControlRequest | application/json: ControlRequestCommand | 200 ControlRequest |
| G5 | data governance | GET /data-requests | listDataRequests | — | 200 DataRequestPage |
| G5 | data governance | POST /data-requests | createDataRequest | application/json: DataRequestCreate | 201 DataRequest |
| G5 | data governance | GET /data-requests/{id} | getDataRequest | — | 200 DataRequest |
| G5 | data governance | POST /data-requests/{id}/commands | commandDataRequest | application/json: DataRequestCommand | 200 DataRequest |
| G2 shared all gates | private files | GET /files | listFiles | — | 200 FilePage |
| G2 shared all gates | private files | POST /files | uploadFile | multipart/form-data: FileUpload | 201 File |
| G2 shared all gates | private files | GET /files/{id} | getFile | — | 200 File |
| G2 shared all gates | private files | DELETE /files/{id} | deleteUnattachedFile | application/json: DeleteFileRequest | 204 no body |
| G2 shared all gates | private files | POST /files/{id}/commands | commandFile | application/json: FileCommand | 200 File |
| G2 shared all gates | private files | GET /files/{id}/content | downloadFile | — | 200 string format=binary; string format=binary; string format=binary |
| G2 shared all gates | durable jobs | GET /jobs | listJobs | — | 200 JobPage |
| G2 shared all gates | durable jobs | GET /jobs/{id} | getJob | — | 200 Job |
| G2 shared all gates | durable jobs | POST /jobs/{id}/commands | commandJob | application/json: JobCommand | 200 Job |
| G2 shared / G5 list | audit | GET /audit-events | listAuditEvents | — | 200 AuditEventPage |
| G5 | reporting | GET /dashboard | getDashboard | — | 200 Dashboard |
| G5 | reporting | GET /exports/audit | exportAuditCsv | — | 200 string  |
| G3 | approval/identity | GET /reviewer-options | getReviewerOptions | — | 200 ReviewerOptions |
| G4 | notice | GET /notices/{id}/capture | getNoticeCapture | — | 200 NoticeCapture |
| G3 | approval | GET /rounds/{id}/snapshot | getRoundSnapshot | — | 200 RoundSnapshot |

## Exact foundation shapes

All response property lists below are required unless explicitly marked optional. Null is a value: absence is not equivalent. Do not use NON_NULL serialization globally. Schema remains authority for constraints.

### Session

- `authenticated`: boolean 
- `user`: User | null 
- `csrfToken`: string minLength=1, maxLength=200
- `expiresAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 

### LoginRequest

- `username`: string minLength=1, maxLength=40
- `password`: string minLength=1, maxLength=128

### User

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `username`: string pattern=^[a-z][a-z0-9._-]{2,39}$
- `displayName`: string minLength=1, maxLength=120
- `active`: boolean 
- `roles`: [string enum(BANK_ADMIN,MAKER,CHECKER,MANAGER,POLICY_AUTHOR,POLICY_REVIEWER,DATA_OFFICER,AUDITOR)]
- `branchIds`: [string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$]
- `permissions`: [string enum(SENSITIVE_READ,EXPORT)]
- `blockedActions`: [BlockedAction]

### UserCreate

- `username`: string minLength=1, maxLength=40
- `displayName`: string minLength=1, maxLength=120
- `temporaryPassword`: string minLength=12, maxLength=128
- `roles`: [string enum(BANK_ADMIN,MAKER,CHECKER,MANAGER,POLICY_AUTHOR,POLICY_REVIEWER,DATA_OFFICER,AUDITOR)]
- `branchIds`: [string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$]
- `permissions`: [string enum(SENSITIVE_READ,EXPORT)]
- `reason`: string minLength=1, maxLength=1000

### UserUpdate

- `displayName`: string minLength=1, maxLength=120
- `active`: boolean 
- `roles`: [string enum(BANK_ADMIN,MAKER,CHECKER,MANAGER,POLICY_AUTHOR,POLICY_REVIEWER,DATA_OFFICER,AUDITOR)]
- `branchIds`: [string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$]
- `permissions`: [string enum(SENSITIVE_READ,EXPORT)]
- `reason`: string minLength=1, maxLength=1000

### Branch

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `code`: string pattern=^B[0-9]{2}$
- `name`: string minLength=1, maxLength=120
- `active`: boolean 
- `blockedActions`: [BlockedAction]

### BranchCreate

- `code`: string pattern=^B[0-9]{2}$
- `name`: string minLength=1, maxLength=120

### BranchUpdate

- `name`: string minLength=1, maxLength=120
- `active`: boolean 
- `reason`: string minLength=1, maxLength=1000

### Party

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `taxId`: string pattern=^([0-9]{10}|[0-9]{13})$
- `name`: string minLength=1, maxLength=200
- `blockedActions`: [BlockedAction]

### PartyCreate

- `taxId`: string pattern=^([0-9]{10}|[0-9]{13})$
- `name`: string minLength=1, maxLength=200

### PartyUpdate

- `name`: string minLength=1, maxLength=200
- `reason`: string minLength=1, maxLength=1000

### Contact

- `email`: string format=email, maxLength=254
- `phone`: string pattern=^\+?[0-9]{8,15}$
- `personName`: string minLength=1, maxLength=120
- `roleLabel`: string minLength=1, maxLength=100
- `purpose`: string enum(SERVICE_CONTACT,NOTICE_RECIPIENT)

### Relationship

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `partyId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `assignedMakerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `confirmedContact`: Contact
- `contactVersion`: integer minimum=1
- `active`: boolean 
- `blockedActions`: [BlockedAction]

### RelationshipCreate

- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `partyId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `assignedMakerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `confirmedContact`: Contact

### RelationshipUpdate

- `assignedMakerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `active`: boolean 
- `reason`: string minLength=1, maxLength=1000

### ContactRequest

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `relationshipId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `requestedContact`: Contact
- `requesterId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `contributors`: [string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$]
- `decision`: string enum(PENDING,APPROVED,REJECTED)
- `reason`: string minLength=1, maxLength=1000 | null 
- `decidedBy`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `expectedRelationshipVersion`: integer minimum=1
- `blockedActions`: [BlockedAction]

### ContactRequestCreate

- `relationshipId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `requestedContact`: Contact
- `reason`: string minLength=1, maxLength=1000
- `expectedRelationshipVersion`: integer minimum=1

### ContactRequestCommand_APPROVE

- `action`: string enum(APPROVE)
- `reason`: string minLength=1, maxLength=1000
- `expectedRelationshipVersion`: integer minimum=1

### ContactRequestCommand_REJECT

- `action`: string enum(REJECT)
- `reason`: string minLength=1, maxLength=1000

### Representation

- `basisType`: string enum(LEGAL_REPRESENTATIVE,POWER_OF_ATTORNEY)
- `personName`: string minLength=1, maxLength=120
- `scopeText`: string minLength=1, maxLength=1000
- `validFrom`: string format=date
- `validTo`: string format=date | null 
- `sourceFileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `issuerReference`: string minLength=1, maxLength=200
- `checkedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `checkEvidenceFileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `title`: string minLength=1, maxLength=120
- `knownRevocationAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 

### SubmittedRepresentation

- `basisType`: string enum(LEGAL_REPRESENTATIVE,POWER_OF_ATTORNEY)
- `personName`: string minLength=1, maxLength=120
- `scopeText`: string minLength=1, maxLength=1000
- `validFrom`: string format=date
- `validTo`: string format=date | null 
- `sourceFileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `issuerReference`: string minLength=1, maxLength=200
- `checkedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `checkEvidenceFileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `title`: string minLength=1, maxLength=120
- `knownRevocationAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 

### SigningAuthority

- `bank`: SubmittedRepresentation
- `beneficiary`: SubmittedRepresentation

### Terms

- `currency`: string enum(VND)
- `feeVnd`: string pattern=^(0|[1-9][0-9]{0,9})$
- `feePayer`: string enum(BENEFICIARY,UNAGREED)
- `pricingMode`: string enum(NEGOTIATED_FIXED,STANDARD_TARIFF)
- `termKind`: string enum(FIXED,OPEN_ENDED)
- `startOn`: string format=date
- `endExclusive`: string format=date | null 
- `clauseVersion`: string enum(NEGOTIATED-VA-1,TARIFF-ACK-7D-1)

### DocumentLink

- `fileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `purpose`: string enum(SERVICE_APPLICATION,CORPORATE_ID,REPRESENTATION_BASIS,SERVICE_TERMS,IDENTITY_IMAGE,SIGNING_COPY,NOTICE_EVIDENCE,DUTY_EVIDENCE,CONTROL_EVIDENCE,DATA_BASIS)

### Dossier

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `state`: string enum(DRAFT,IN_REVIEW,RETURNED,APPROVED,COMPLETED,REJECTED,CANCELLED)
- `relationshipId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `assignedMakerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `linkedFromDossierId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `terms`: Terms
- `representation`: Representation
- `contactSnapshot`: Contact
- `templateVersion`: string enum(BASE-VA-1)
- `bundleVersion`: string enum(TERMS-VA-1)
- `documents`: [DocumentLink]
- `contributorIds`: [string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$]
- `revision`: Revision | null 
- `roundId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `contractId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `mandatoryChecks`: [MandatoryCheck]
- `externalAccountReference`: string minLength=1, maxLength=100
- `blockedActions`: [BlockedAction]

### DossierCreate

- `relationshipId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `linkedFromDossierId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `terms`: Terms
- `representation`: Representation
- `documents`: [DocumentLink]
- `externalAccountReference`: string minLength=1, maxLength=100

### DossierUpdate

- `terms`: Terms
- `representation`: Representation
- `documents`: [DocumentLink]
- `externalAccountReference`: string minLength=1, maxLength=100

### DossierCommand_SUBMIT

- `action`: string enum(SUBMIT)
- `mandatoryChecks`: [MandatoryCheck]
- `l1ReviewerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `l2ReviewerId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 

### File

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `state`: string enum(SCANNING,CLEAN,INFECTED,SCAN_FAILED,DISPOSED)
- `targetKind`: string enum(DOSSIER,CONTRACT,CHANGE,RELATIONSHIP,INCIDENT,OBLIGATION,CONTROL_REQUEST,DATA_REQUEST,NOTICE,POLICY)
- `targetId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `purpose`: string enum(SERVICE_APPLICATION,CORPORATE_ID,REPRESENTATION_BASIS,SERVICE_TERMS,IDENTITY_IMAGE,SIGNING_COPY,NOTICE_EVIDENCE,DUTY_EVIDENCE,CONTROL_EVIDENCE,DATA_BASIS)
- `mediaType`: string enum(application/pdf,image/jpeg,image/png)
- `filename`: string minLength=1, maxLength=150
- `sizeBytes`: integer minimum=1, maximum=10485760
- `sha256`: string pattern=^[a-f0-9]{64}$
- `scanJobId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `scannerVersion`: string minLength=1, maxLength=80 | null 
- `signatureDatabaseVersion`: string minLength=1, maxLength=80 | null 
- `scannedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `humanVerification`: string enum(NOT_CHECKED,PASS,FAIL)
- `sensitive`: boolean 
- `attached`: boolean 
- `uploadedBy`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `origin`: string enum(UPLOADED,GENERATED)
- `rendererVersion`: string minLength=1, maxLength=100 | null 
- `generatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `blockedActions`: [BlockedAction]

### FileUpload

- `file`: string format=binary
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `targetKind`: string enum(DOSSIER,CONTRACT,CHANGE,RELATIONSHIP,INCIDENT,OBLIGATION,CONTROL_REQUEST,DATA_REQUEST,NOTICE,POLICY)
- `targetId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `purpose`: string enum(SERVICE_APPLICATION,CORPORATE_ID,REPRESENTATION_BASIS,SERVICE_TERMS,IDENTITY_IMAGE,SIGNING_COPY,NOTICE_EVIDENCE,DUTY_EVIDENCE,CONTROL_EVIDENCE,DATA_BASIS)

### FileCommand_RETRY

- `action`: string enum(RETRY)
- `reason`: string minLength=1, maxLength=1000

### DeleteFileRequest

- `reason`: string minLength=1, maxLength=1000

### Job

- `id`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `version`: integer minimum=1
- `createdAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `updatedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `allowedActions`: [string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)]
- `branchId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `state`: string enum(QUEUED,RUNNING,SUCCEEDED,FAILED,CANCELLED)
- `kind`: string enum(FILE_SCAN,ARTIFACT_PDF,NOTICE_DISPATCH,DATA_DISPOSITION,EFFECTIVITY)
- `parentKind`: string enum(FILE,CONTRACT,CHANGE,NOTICE,DATA_REQUEST)
- `parentId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `attempts`: integer minimum=0, maximum=5
- `nextAttemptAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `startedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `completedAt`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$ | null 
- `resultFileId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ | null 
- `lastErrorCode`: string enum(SCANNER_UNAVAILABLE,TRANSPORT_UNAVAILABLE,STORAGE_INTEGRITY,GUARD_BLOCKED,LEASE_EXPIRED) | null 
- `blockedActions`: [BlockedAction]

### JobCommand_RETRY

- `action`: string enum(RETRY)
- `reason`: string minLength=1, maxLength=1000

### RelationshipPage

- `items`: [Relationship]
- `nextCursor`: string minLength=1, maxLength=2048 | null 
- `asOf`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `timelineProcessingLagSeconds`: integer minimum=0

### DossierPage

- `items`: [Dossier]
- `nextCursor`: string minLength=1, maxLength=2048 | null 
- `asOf`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `timelineProcessingLagSeconds`: integer minimum=0

### FilePage

- `items`: [File]
- `nextCursor`: string minLength=1, maxLength=2048 | null 
- `asOf`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `timelineProcessingLagSeconds`: integer minimum=0

### JobPage

- `items`: [Job]
- `nextCursor`: string minLength=1, maxLength=2048 | null 
- `asOf`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$
- `timelineProcessingLagSeconds`: integer minimum=0

### ReviewerOption

- `userId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `displayName`: string minLength=1, maxLength=120
- `level`: string enum(L1,L2)

### ReviewerOptions

- `items`: [ReviewerOption]
- `asOf`: string format=date-time, pattern=^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$

### Problem

- `type`: string format=uri-reference
- `title`: string minLength=1, maxLength=120
- `status`: integer enum(400,401,403,404,409,410,412,413,415,422,428,429,503)
- `detail`: string minLength=1, maxLength=2000
- `instance`: string minLength=1, maxLength=500
- `code`: string enum(BAD_JSON,UNAUTHENTICATED,ROLE_FORBIDDEN,CSRF_INVALID,SOD_VIOLATION,SENSITIVE_FORBIDDEN,EXPORT_FORBIDDEN,NOT_FOUND,STATE_CONFLICT,IDEMPOTENCY_CONFLICT,IDEMPOTENCY_IN_PROGRESS,BASE_CONFLICT,SCHEDULE_CONFLICT,POLICY_SCHEDULE_CONFLICT,AUTHORITY_REVIEW_REQUIRED,LEGAL_REVIEW_REQUIRED,ARTIFACT_NOT_READY,READINESS_BLOCKED,NOTICE_NOT_READY,UNRESOLVED_DUTY,DUPLICATE_PARTY,DUPLICATE_RELATIONSHIP,HOLD_ACTIVE,PRECONDITION_FAILED,FILE_TOO_LARGE,MEDIA_UNSUPPORTED,VALIDATION_FAILED,POLICY_BLOCKED,FILE_NOT_CLEAN,FILE_LIMIT,AUTHORITY_INVALID,NOTICE_LEAD_TOO_SHORT,LATE_SIGNING,NO_RENEWAL,EXPORT_LIMIT,REGRESSION_FAILED,NONWAIVABLE_DUTY,PRECONDITION_REQUIRED,RATE_LIMITED,SERVICE_UNAVAILABLE,STORAGE_INTEGRITY,CURSOR_EXPIRED,DISPOSITION_COMMITTED)
- `traceId`: string format=uuid, pattern=^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$
- `violations`: [Violation]
- `currentVersion`: integer minimum=1 | null 

### Violation

- `path`: string minLength=1, maxLength=200
- `code`: string minLength=1, maxLength=80
- `message`: string minLength=1, maxLength=1000

### BlockedAction

- `action`: string enum(UPDATE,SUBMIT,WITHDRAW,CANCEL,CLONE,APPROVE,RETURN,REJECT,VERIFY_EVIDENCE,REASSIGN,SIMULATE,REQUEST_PUBLICATION,APPROVE_PUBLICATION,REJECT_PUBLICATION,SIGN,VERIFY_SIGNING,RETURN_SIGNING,COMMIT,REBASE,REQUEST_CANCEL_SCHEDULE,APPROVE_CANCEL_SCHEDULE,REJECT_CANCEL_SCHEDULE,DISPATCH,RETRY,RECORD_RECEIPT,SUBMIT_EVIDENCE,VERIFY,REQUEST_EXTENSION,DECIDE_EXTENSION,REQUEST_WAIVER,DECIDE_WAIVER,RESOLVE,CLOSE,CONFIRM,DECIDE,EXECUTE,PLACE_HOLD,RELEASE_HOLD,DOWNLOAD_REDACTED,DOWNLOAD_FULL,EXPORT,RUN_REGRESSION,HOLD_READINESS,RELEASE_READINESS,RECORD_ACK,VERIFY_ACK,RECORD_EXTERNAL_DISPATCH,START_REVIEW,SUPERSEDE,CANCEL_INAPPLICABLE,SUBMIT_RESOLUTION)
- `code`: string enum(BAD_JSON,UNAUTHENTICATED,ROLE_FORBIDDEN,CSRF_INVALID,SOD_VIOLATION,SENSITIVE_FORBIDDEN,EXPORT_FORBIDDEN,NOT_FOUND,STATE_CONFLICT,IDEMPOTENCY_CONFLICT,IDEMPOTENCY_IN_PROGRESS,BASE_CONFLICT,SCHEDULE_CONFLICT,POLICY_SCHEDULE_CONFLICT,AUTHORITY_REVIEW_REQUIRED,LEGAL_REVIEW_REQUIRED,ARTIFACT_NOT_READY,READINESS_BLOCKED,NOTICE_NOT_READY,UNRESOLVED_DUTY,DUPLICATE_PARTY,DUPLICATE_RELATIONSHIP,HOLD_ACTIVE,PRECONDITION_FAILED,FILE_TOO_LARGE,MEDIA_UNSUPPORTED,VALIDATION_FAILED,POLICY_BLOCKED,FILE_NOT_CLEAN,FILE_LIMIT,AUTHORITY_INVALID,NOTICE_LEAD_TOO_SHORT,LATE_SIGNING,NO_RENEWAL,EXPORT_LIMIT,REGRESSION_FAILED,NONWAIVABLE_DUTY,PRECONDITION_REQUIRED,RATE_LIMITED,SERVICE_UNAVAILABLE,STORAGE_INTEGRITY,CURSOR_EXPIRED,DISPOSITION_COMMITTED)
- `reason`: string minLength=1, maxLength=1000

## Common interfaces and requirements

- ActorContext: immutable actorId + request traceId, current active roles, branchIds and supplemental permissions resolved from database per request. An inactive actor returns401 next request; never trust browser role/scope or cache stale User in session.
- ParentScopeResolver / AggregateAccess: authorize object before replay. Scope outside or guessed nested object404; wrong role within current scope403. MAKER assignedRelationship restriction is narrower than merely sharing branch. BANK_ADMIN directory power does not confer ordinary business read/review. POLICY_AUTHOR/REVIEWER global namespace applies only policy and non-PII policy files.
- RequestContract: derive exact path/method schema and parameter allowlist from B; reject unknown JSON/query/header-dependent invalid values with422 and JSON-pointer violations. Bad JSON400. UUID lowercase v4, money decimal string, dates strict ISO, timestamps exactly milliseconds UTC, closed request objects, semantic unique ID arrays. Do not change canonical operationId spellings.
- WireResponse: Cache-Control:no-store, X-Trace-Id; ETag only resource detail/create/update/command, format `"r-<version>"`; no ETag on session/lists/simulation/dashboard/CSV.201 Location relative.204 absent body. RFC9457 Problem has all required fields including violations[] and currentVersion:null when absent.
- MutationCoordinator: same database transaction updates aggregate/state/version + audit + durable job intent + idempotency completion. Creates require Idempotency-Key UUID, all existing mutations also If-Match; missing If-Match428, stale412 with currentVersion. Simulation/login/logout exclude idempotency and If-Match but unsafe CSRF remains mandatory.
- IdempotencyStore: key scoped(actorId,key), 24h, fingerprint method+normalized path+RFC8785 canonical JSON+If-Match; multipart fingerprint canonical fields and server-derived byte digest. Current auth/CSRF/scope precede cache replay, replay preserves original response status/body/ETag even aggregate changed, adds Idempotent-Replayed:true. Same key/different fingerprint409; running409+Retry-After:1. Cache success only except persisted Change COMMIT BASE_CONFLICT/SCHEDULE_CONFLICT409. All other prevalidation4xx no state/cache.
- CanonicalHasher: separate RFC8785 HTTP body hash from sorted-key block/manifest and named business projections. NFC input before freeze. Hashes bind correct projection and artifact hashes bind actual bytes; never treat Dossier/Change evaluation facts hash and full revision hash as interchangeable.
- BusinessClock / Calendar: production system time, no public clock/reset endpoint; Asia/Ho_Chi_Minh dates; date interval [start,end). Demo work calendar weekdays plus simulated2027-01-04 holiday; N working days exclude receipt day, deadline17:00 local. External law deadlines source-recorded externalDueAt with rule basis, not generic calculated calendar.
- SecureBlobStore: opaque private/quarantine paths, upload MIME sniff and allowed PDF/JPEG/PNG, max10MiB,20/draft,100MiB/draft; finalized bytes before usable association, server SHA256. No parent-less upload and no cross-parent reuse.201 SCANNING is quarantine, CLEAN scanner only, human check separate. Full sensitive download supplemental grant; redacted representation unavailable/unsafe409 with no original fallback. Content-Length/hash refer actual returned representation.
- DurableJobDispatcher: leases60s renew20s, SKIP LOCKED,5 attempts with min(900,5*2^(attempt-1)) seconds delay, expired lease requeue. Store semantic dedupeKey/source version/source hash. Run scanner/PDF outside DB lock, recheck source and parent guards transactionally when completing. Obsolete source => CANCELLED/GUARD_BLOCKED. Retry source still-authorized failed only; no guessed nested job leakage.
- QueryPage: SQL scope/filter before results; AND filters; strict dueBefore<, createdFrom inclusive, createdTo exclusive. Defaultlimit25 range1..100. createdAt DESC,id DESC (contractversions number DESC,id DESC). Signed15min cursor binds query+actor+scopes+order+first-page max+last key. mismatch422, expiry410. items,nextCursor(nullable),asOf,timelineProcessingLagSeconds; no undeclared total.
- AuditWriter: immutable resource effects within transaction; reason required per command/admin update; no password/full files or PII leak through audit/export; separate security denial records allowed without business success. CSV supplemental EXPORT + scoped role, max1000, BOM/CRLF/RFC4180, all reason redacted and spreadsheet formula neutralization.
- ActionProjector: allowedActions and blockedActions disjoint, only role/workflow-relevant union, block precise guards. Runtime rechecks every action; UI hint cannot grant permission.

## Foundation business traps

- Party taxId bankwide unique, Relationship unique(branchId,partyId); Maker must not get global Party lookup or taxId enumeration. Admin assignment target must be active eligible MAKER in branch; reassignment preserves contributors and history.
- Contact update uses immutable PENDING ContactRequest and independent CHECKER. Both request If-Match and expectedRelationshipVersion checked; stale relationship =>409 BASE_CONFLICT (not silent overwrite), confirmedContact/contactVersion only change on approval. Never edit past Notice recipient or immutable revision. Source edit/upload appends stable contributors; assessment does not.
- Draft representation.sourceFileId/null review fields are legitimate bootstrap. Create Dossier parent first, upload+scan, then PUT DocumentLink IDs. Assigned MAKER only DRAFT/RETURNED. Submitted representation.sourceFileId is nonnull, complete terms and authority/check evidence mandatory, start from next local day. Required trio SERVICE_APPLICATION/CORPORATE_ID/REPRESENTATION_BASIS always CLEAN before submit. Fee payer UNAGREED allowed draft only.
- Original approved Contract PREPARED/SCHEDULED keeps currentVersion=null until firstACTIVE; approval is not effectiveness. Contract final APPROVE and artifact intent atomic, immutable round snapshot preserved. No artificial active/approved defaults in generic CRUD or seeded artifacts masquerading as worker output.
- File target POLICY branchId=null, policy parent-scoped author/reviewer only and non-PII; all other targets branchId nonnull matches parent. Note Job schema still requires nonnull branchId; POLICY FILE_SCAN job representation needs an explicit technical resolution if exposed through canonical Job, rather than silently inventing field nullability.
- File delete is only own unattached staging, not arbitrary DOSSIER/approved association; inherited parent permissions/version/history must be checked. Download unauthorized cross-scope404. SENSITIVE_READ augments relevant role/scope; plain ADMIN never full image.
- Job/file retries require FAILED/SCAN_FAILED and current parent source authorization; source hash/version obsolete cannot replay old intended effect. Scan completion must not overwrite newer file/disposition state after worker releases DB lock.
- Security session cookie `__Host-cg-session; Secure; HttpOnly; Path=/; SameSite=Lax`, opaque ID; GET/session establishes anonymous CSRF, login validates then rotates both sessionID+CSRF, logout204 expires. Idle30min/absolute8h. Login10/min username+IP =>429 Retry-After60.

## Later phase invariants not optional when declaring full Backend

G3 finite policy typed config/regression/current source, independent publication, one future schedule slot, immutable rounds/revisions, separate reviewer roles and stable SoD, approved artifact exact hash, independent immutable signing packets, readiness before start strict. G4 Change dual CAS against base version+schedule counter; semantic loser409 persisted CONFLICTED; approved signed AMENDMENT/tariff receipt+verified exact ACK+7 DATEdays/ack+1; cancellation retains notices and sole inapplicable NOTICE_ACK; timeline qualifying renewal before expiry. G5 nonwaivable STATUTORY_EXTERNAL tasks; late extension preserves accrued breach; incident resolution candidate independently verified and linked duty/externalcomplaint confirmation; external controls recorded confirmation only, no bank execution; disposal independent officer exact manifest+record locks against holds and reservations, incomplete byte cleanup remains FAILED/DECIDED; snapshot dashboard same formulas as drilldown. These cannot be replaced by generic state setters while claiming85 operations implemented.
