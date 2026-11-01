# Canonical API/field reference for designers

Generated from contracts/openapi-v1.json at baseline81d6c17. This is design reference, not a new contract. Resolve conditional schemas in the JSON reference before designing a form; required below is local to its schema, not a claim across oneOf branches. Never design command payload by copying a response resource.

| Operation ID | Method | Canonical path | Rules |
|---|---|---|---|
| listAuditEvents | GET | /audit-events | BR-054 |
| listBranchs | GET | /branches | BR-005 |
| createBranch | POST | /branches | BR-005 |
| getBranch | GET | /branches/{id} | BR-005 |
| updateBranch | PUT | /branches/{id} | BR-005 |
| listChanges | GET | /changes | BR-026 |
| createChange | POST | /changes | BR-026 |
| getChange | GET | /changes/{id} | BR-026 |
| updateChange | PUT | /changes/{id} | BR-026 |
| commandChange | POST | /changes/{id}/commands | BR-026 |
| listContactRequests | GET | /contact-requests | BR-010 |
| createContactRequest | POST | /contact-requests | BR-010 |
| getContactRequest | GET | /contact-requests/{id} | BR-010 |
| commandContactRequest | POST | /contact-requests/{id}/commands | BR-010 |
| listContracts | GET | /contracts | BR-024 |
| getContract | GET | /contracts/{id} | BR-024 |
| commandContract | POST | /contracts/{id}/commands | BR-024 |
| listContractVersions | GET | /contracts/{id}/versions | BR-021 |
| listControlRequests | GET | /control-requests | BR-043 |
| createControlRequest | POST | /control-requests | BR-043 |
| getControlRequest | GET | /control-requests/{id} | BR-043 |
| commandControlRequest | POST | /control-requests/{id}/commands | BR-043 |
| getDashboard | GET | /dashboard | BR-055 |
| listDataRequests | GET | /data-requests | BR-046 |
| createDataRequest | POST | /data-requests | BR-046 |
| getDataRequest | GET | /data-requests/{id} | BR-046 |
| commandDataRequest | POST | /data-requests/{id}/commands | BR-046 |
| listDossiers | GET | /dossiers | BR-016 |
| createDossier | POST | /dossiers | BR-016 |
| getDossier | GET | /dossiers/{id} | BR-016 |
| updateDossier | PUT | /dossiers/{id} | BR-016 |
| commandDossier | POST | /dossiers/{id}/commands | BR-016 |
| exportAuditCsv | GET | /exports/audit | BR-056 |
| listFiles | GET | /files | BR-051 |
| uploadFile | POST | /files | BR-051 |
| deleteUnattachedFile | DELETE | /files/{id} | BR-052 |
| getFile | GET | /files/{id} | BR-051 |
| commandFile | POST | /files/{id}/commands | BR-051 |
| downloadFile | GET | /files/{id}/content | BR-052 |
| listIncidents | GET | /incidents | BR-041 |
| createIncident | POST | /incidents | BR-041 |
| getIncident | GET | /incidents/{id} | BR-041 |
| commandIncident | POST | /incidents/{id}/commands | BR-041 |
| listJobs | GET | /jobs | BR-053 |
| getJob | GET | /jobs/{id} | BR-053 |
| commandJob | POST | /jobs/{id}/commands | BR-053 |
| listNotices | GET | /notices | BR-031 |
| createNotice | POST | /notices | BR-031 |
| getNotice | GET | /notices/{id} | BR-031 |
| updateNotice | PUT | /notices/{id} | BR-031 |
| getNoticeCapture | GET | /notices/{id}/capture | BR-032 |
| commandNotice | POST | /notices/{id}/commands | BR-031 |
| listObligations | GET | /obligations | BR-035 |
| getObligation | GET | /obligations/{id} | BR-035 |
| commandObligation | POST | /obligations/{id}/commands | BR-035 |
| listPartys | GET | /parties | BR-008 |
| createParty | POST | /parties | BR-008 |
| getParty | GET | /parties/{id} | BR-008 |
| updateParty | PUT | /parties/{id} | BR-008 |
| listPolicys | GET | /policies | BR-011 |
| createPolicy | POST | /policies | BR-011 |
| getPolicy | GET | /policies/{id} | BR-011 |
| updatePolicy | PUT | /policies/{id} | BR-011 |
| commandPolicy | POST | /policies/{id}/commands | BR-011 |
| simulatePolicy | POST | /policies/{id}/simulation | BR-013 |
| listRelationships | GET | /relationships | BR-009 |
| createRelationship | POST | /relationships | BR-009 |
| getRelationship | GET | /relationships/{id} | BR-009 |
| updateRelationship | PUT | /relationships/{id} | BR-009 |
| getReviewerOptions | GET | /reviewer-options | BR-009 |
| listRounds | GET | /rounds | BR-018 |
| getRound | GET | /rounds/{id} | BR-018 |
| commandRound | POST | /rounds/{id}/commands | BR-018 |
| getRoundSnapshot | GET | /rounds/{id}/snapshot | BR-019 |
| getSession | GET | /session | BR-003 |
| login | POST | /session/login | BR-003 |
| logout | POST | /session/logout | BR-003 |
| listSigningPackets | GET | /signing-packets | BR-022 |
| createSigningPacket | POST | /signing-packets | BR-022 |
| getSigningPacket | GET | /signing-packets/{id} | BR-022 |
| commandSigningPacket | POST | /signing-packets/{id}/commands | BR-022 |
| listUsers | GET | /users | BR-005 |
| createUser | POST | /users | BR-005 |
| getUser | GET | /users/{id} | BR-005 |
| updateUser | PUT | /users/{id} | BR-005 |

## AckCandidate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| acknowledgedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| noticeId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| recipientSnapshotHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| recordedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| recordedBy | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| verificationReason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| verificationStatus | True | string | {"enum": ["PENDING", "VERIFIED", "RETURNED"]} |
| verifiedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| verifiedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |

## AmendmentTarget

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| endExclusive | False | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| feeVnd | False | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| kind | True | string | {"enum": ["AMENDMENT"]} |

## ApprovalStep

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| decidedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| decisionAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| level | True | string | {"enum": ["L1", "L2"]} |
| reason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| reviewerId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["WAITING", "PENDING", "APPROVED", "RETURNED", "REJECTED", "CANCELLED"]} |

## AuditEvent

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"minLength": 1, "maxLength": 80} |
| actorId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| afterHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| beforeHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| branchId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| occurredAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| reason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| resourceId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| resourceType | True | string | {"enum": ["USER", "BRANCH", "PARTY", "RELATIONSHIP", "CONTACT_REQUEST", "DOSSIER", "ROUND", "POLICY", "CONTRACT", "SIGNING_PACKET", "CHANGE", "NOTICE", "OBLIGATION", "INCIDENT", "CONTROL_REQUEST", "DATA_REQUEST", "FILE", "JOB", "EXPORT"]} |
| resourceVersion | True | integer | {"minimum": 1} |
| traceId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## AuditEventPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/AuditEvent"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## BlockedAction

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"]} |
| code | True | string | {"enum": ["BAD_JSON", "UNAUTHENTICATED", "ROLE_FORBIDDEN", "CSRF_INVALID", "SOD_VIOLATION", "SENSITIVE_FORBIDDEN", "EXPORT_FORBIDDEN", "NOT_FOUND", "STATE_CONFLICT", "IDEMPOTENCY_CONFLICT", "IDEMPOTENCY_IN_PROGRESS", "BASE_CONFLICT", "SCHEDULE_CONFLICT", "POLICY_SCHEDULE_CONFLICT", "AUTHORITY_REVIEW_REQUIRED", "LEGAL_REVIEW_REQUIRED", "ARTIFACT_NOT_READY", "READINESS_BLOCKED", "NOTICE_NOT_READY", "UNRESOLVED_DUTY", "DUPLICATE_PARTY", "DUPLICATE_RELATIONSHIP", "HOLD_ACTIVE", "PRECONDITION_FAILED", "FILE_TOO_LARGE", "MEDIA_UNSUPPORTED", "VALIDATION_FAILED", "POLICY_BLOCKED", "FILE_NOT_CLEAN", "FILE_LIMIT", "AUTHORITY_INVALID", "NOTICE_LEAD_TOO_SHORT", "LATE_SIGNING", "NO_RENEWAL", "EXPORT_LIMIT", "REGRESSION_FAILED", "NONWAIVABLE_DUTY", "PRECONDITION_REQUIRED", "RATE_LIMITED", "SERVICE_UNAVAILABLE", "STORAGE_INTEGRITY", "CURSOR_EXPIRED", "DISPOSITION_COMMITTED"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## Branch

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| code | True | string | {"pattern": "^B[0-9]{2}$"} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| name | True | string | {"minLength": 1, "maxLength": 120} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## BranchCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| code | True | string | {"pattern": "^B[0-9]{2}$"} |
| name | True | string | {"minLength": 1, "maxLength": 120} |

## BranchPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Branch"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## BranchUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| name | True | string | {"minLength": 1, "maxLength": 120} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## Change

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| appliedVersionId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| artifactHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| artifactId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| artifactJobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| baseContractVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| cancelScheduleRequest | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ExtensionRequest"}, {"type": "null"}]} |
| committedScheduleVersion | True | composed | {"anyOf": [{"minimum": 1, "type": "integer"}, {"type": "null"}]} |
| conflictReason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| effectiveOn | True | string | {"format": "date"} |
| evaluation | True | composed | {"anyOf": [{"$ref": "#/components/schemas/Evaluation"}, {"type": "null"}]} |
| expectedScheduleVersion | True | integer | {"minimum": 1} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| noticeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| roundId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| signingPacketId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["DRAFT", "IN_REVIEW", "RETURNED", "APPROVED", "READY", "SCHEDULED", "APPLIED", "REJECTED", "CANCELLED", "CONFLICTED"]} |
| target | True | #/components/schemas/ChangeTarget | {} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## ChangeCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ChangeCommand_APPROVE_CANCEL_SCHEDULE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE_CANCEL_SCHEDULE"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCommand_CANCEL

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCommand_COMMIT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["COMMIT"]} |
| expectedBaseContractVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| expectedScheduleVersion | True | integer | {"minimum": 1} |

## ChangeCommand_REBASE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REBASE"]} |
| baseContractVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| expectedScheduleVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCommand_REJECT_CANCEL_SCHEDULE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT_CANCEL_SCHEDULE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCommand_REQUEST_CANCEL_SCHEDULE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REQUEST_CANCEL_SCHEDULE"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCommand_SUBMIT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT"]} |
| l1ReviewerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| l2ReviewerId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |

## ChangeCommand_WITHDRAW

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["WITHDRAW"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ChangeCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| baseContractVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| effectiveOn | True | string | {"format": "date"} |
| expectedScheduleVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| target | True | #/components/schemas/ChangeTarget | {} |

## ChangePage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Change"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## ChangeRevision

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| baseContractVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| consolidatedTerms | True | #/components/schemas/Terms | {} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| effectiveOn | True | string | {"format": "date"} |
| evaluation | True | #/components/schemas/Evaluation | {} |
| evidenceHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| expectedScheduleVersion | True | integer | {"minimum": 1} |
| factsHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| number | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| representation | True | #/components/schemas/SubmittedRepresentation | {} |
| submittedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| target | True | #/components/schemas/ChangeTarget | {} |

## ChangeRoundSnapshot

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| revision | True | #/components/schemas/ChangeRevision | {} |
| targetKind | True | string | {"enum": ["CHANGE"]} |

## ChangeTarget

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ChangeUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| effectiveOn | True | string | {"format": "date"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| target | True | #/components/schemas/ChangeTarget | {} |

## Contact

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| email | True | string | {"format": "email", "maxLength": 254} |
| personName | True | string | {"minLength": 1, "maxLength": 120} |
| phone | True | string | {"pattern": "^\\+?[0-9]{8,15}$"} |
| purpose | True | string | {"enum": ["SERVICE_CONTACT", "NOTICE_RECIPIENT"]} |
| roleLabel | True | string | {"minLength": 1, "maxLength": 100} |

## ContactRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributors | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| decidedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| decision | True | string | {"enum": ["PENDING", "APPROVED", "REJECTED"]} |
| expectedRelationshipVersion | True | integer | {"minimum": 1} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| requestedContact | True | #/components/schemas/Contact | {} |
| requesterId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## ContactRequestCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ContactRequestCommand_APPROVE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE"]} |
| expectedRelationshipVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ContactRequestCommand_REJECT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ContactRequestCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| expectedRelationshipVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| requestedContact | True | #/components/schemas/Contact | {} |

## ContactRequestPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/ContactRequest"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## Contract

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| approvedArtifactHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| approvedArtifactId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| approvedRevisionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| approvedTerms | True | #/components/schemas/Terms | {} |
| artifactJobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| currentVersion | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ContractVersion"}, {"type": "null"}]} |
| dossierId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| effectiveAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |
| readiness | True | string | {"enum": ["PENDING", "READY", "REVIEW_REQUIRED"]} |
| readinessRisks | True | array | {"items": {"$ref": "#/components/schemas/ReadinessRisk"}} |
| readinessVerifiedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| recordedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| scheduleVersion | True | integer | {"minimum": 1} |
| scheduledChangeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| serviceState | True | string | {"enum": ["NOT_STARTED", "RUNNING", "SUSPENDED", "STOPPED"]} |
| signingPacketId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["PREPARED", "SCHEDULED", "ACTIVE", "EXPIRED", "TERMINATED", "CANCELLED"]} |
| terminationEffectOn | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| terminationObservedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## ContractCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ContractCommand_CANCEL

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL"]} |
| cancellationEvidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ContractCommand_HOLD_READINESS

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["HOLD_READINESS"]} |
| evidenceIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ContractCommand_RELEASE_READINESS

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RELEASE_READINESS"]} |
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| evidenceIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| riskId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## ContractPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Contract"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## ContractVersion

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| artifactId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| effectiveFrom | True | string | {"format": "date"} |
| effectiveTo | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| number | True | integer | {"minimum": 1} |
| sourceId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| sourceKind | True | string | {"enum": ["ORIGINAL", "AMENDMENT", "RENEWAL", "TARIFF_NOTICE"]} |
| terms | True | #/components/schemas/Terms | {} |

## ContractVersionPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/ContractVersion"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## ControlRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| effectOn | True | string | {"format": "date"} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| externalAcknowledgement | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ReceiptEvidence"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| kind | True | string | {"enum": ["ACTIVATE", "SUSPEND", "RESUME", "TERMINATE", "STOP"]} |
| noticeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| observedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 2000} |
| requesterId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| roundId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["DRAFT", "REQUESTED", "IN_REVIEW", "APPROVED", "CONFIRMED", "REJECTED", "CANCELLED"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| urgent | True | boolean | {} |
| version | True | integer | {"minimum": 1} |

## ControlRequestCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ControlRequestCommand_CANCEL

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ControlRequestCommand_CONFIRM

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CONFIRM"]} |
| externalAcknowledgement | True | #/components/schemas/ReceiptEvidence | {} |
| neverStarted | True | boolean | {} |
| observedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ControlRequestCommand_SUBMIT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT"]} |
| l1ReviewerId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| l2ReviewerId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ControlRequestCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| effectOn | True | string | {"format": "date"} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| kind | True | string | {"enum": ["ACTIVATE", "SUSPEND", "RESUME", "TERMINATE", "STOP"]} |
| noticeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 2000} |
| urgent | True | boolean | {} |

## ControlRequestPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/ControlRequest"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## ControlRevision

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| effectOn | True | string | {"format": "date"} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| kind | True | string | {"enum": ["ACTIVATE", "SUSPEND", "RESUME", "TERMINATE", "STOP"]} |
| noticeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 2000} |
| requesterId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| submittedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| urgent | True | boolean | {} |

## ControlRoundSnapshot

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| revision | True | #/components/schemas/ControlRevision | {} |
| targetKind | True | string | {"enum": ["CONTROL_REQUEST"]} |

## Dashboard

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| activeAgreements | True | integer | {"minimum": 0} |
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| branchIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| dossiersByState | True | array | {"items": {"additionalProperties": false, "properties": {"count": {"minimum": 0, "type": "integer"}, "state": {"enum": ["DRAFT", "IN_REVIEW", "RETURNED", "APPROVED", "COMPLETED", "REJECTED", "CANCELLED"], "type": "string"}}, "required": ["state", "count"], "type": "object"}} |
| expiring30 | True | integer | {"minimum": 0} |
| historicalBreaches | True | integer | {"minimum": 0} |
| noticeFailures | True | integer | {"minimum": 0} |
| overdueObligations | True | integer | {"minimum": 0} |
| pendingServiceControls | True | integer | {"minimum": 0} |
| reviewOverdue | True | integer | {"minimum": 0} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |
| unresolvedCloseout | True | integer | {"minimum": 0} |

## DataRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| decision | True | composed | {"anyOf": [{"enum": ["ALLOW", "PARTIAL", "DEFER", "REFUSE"], "type": "string"}, {"type": "null"}]} |
| decisionBasis | True | composed | {"anyOf": [{"maxLength": 2000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| decisionBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| holdIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| holds | True | array | {"items": {"$ref": "#/components/schemas/LegalHold"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| jobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| manifest | True | composed | {"anyOf": [{"$ref": "#/components/schemas/DispositionManifest"}, {"type": "null"}]} |
| purpose | True | string | {"minLength": 1, "maxLength": 500} |
| recordIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| requestType | True | string | {"enum": ["ERASURE", "ACCESS", "CORRECTION"]} |
| requesterId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| state | True | string | {"enum": ["OPEN", "IN_REVIEW", "DECIDED", "EXECUTED", "REJECTED"]} |
| subjectReference | True | string | {"minLength": 1, "maxLength": 200} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## DataRequestCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## DataRequestCommand_DECIDE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["DECIDE"]} |
| basis | True | string | {"minLength": 1, "maxLength": 2000} |
| decision | True | string | {"enum": ["ALLOW", "PARTIAL", "DEFER", "REFUSE"]} |
| manifest | True | composed | {"anyOf": [{"$ref": "#/components/schemas/DispositionManifest"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DataRequestCommand_EXECUTE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["EXECUTE"]} |
| manifestHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| manifestVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DataRequestCommand_PLACE_HOLD

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["PLACE_HOLD"]} |
| basisFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| recordIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |

## DataRequestCommand_REJECT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DataRequestCommand_RELEASE_HOLD

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RELEASE_HOLD"]} |
| basisFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| holdId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DataRequestCommand_START_REVIEW

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["START_REVIEW"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DataRequestCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| purpose | True | string | {"minLength": 1, "maxLength": 500} |
| recordIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| requestType | True | string | {"enum": ["ERASURE", "ACCESS", "CORRECTION"]} |
| subjectReference | True | string | {"minLength": 1, "maxLength": 200} |

## DataRequestPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/DataRequest"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## DeadlineRevision

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| breachedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| decisionBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| dueAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| effectiveAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| number | True | integer | {"minimum": 1} |

## DeleteFileRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DispositionManifest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| hash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| purpose | True | string | {"minLength": 1, "maxLength": 500} |
| records | True | array | {"items": {"$ref": "#/components/schemas/DispositionRecord"}} |
| version | True | integer | {"minimum": 1} |

## DispositionRecord

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REDACT", "DELETE_BYTES", "PROVIDE_REDACTED_COPY", "CORRECT_CURRENT_METADATA"]} |
| correction | True | composed | {"anyOf": [{"$ref": "#/components/schemas/Contact"}, {"type": "null"}]} |
| expectedRecordVersion | True | integer | {"minimum": 1} |
| holdId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| recordClass | True | string | {"enum": ["DEMO_AGREEMENT", "DEMO_ID_COPY", "DEMO_OPERATION"]} |
| recordId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| replacementFileId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |

## DocumentLink

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| fileId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| purpose | True | string | {"enum": ["SERVICE_APPLICATION", "CORPORATE_ID", "REPRESENTATION_BASIS", "SERVICE_TERMS", "IDENTITY_IMAGE", "SIGNING_COPY", "NOTICE_EVIDENCE", "DUTY_EVIDENCE", "CONTROL_EVIDENCE", "DATA_BASIS"]} |

## Dossier

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| bundleVersion | True | string | {"enum": ["TERMS-VA-1"]} |
| contactSnapshot | True | #/components/schemas/Contact | {} |
| contractId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| externalAccountReference | True | string | {"minLength": 1, "maxLength": 100} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| linkedFromDossierId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| representation | True | #/components/schemas/Representation | {} |
| revision | True | composed | {"anyOf": [{"$ref": "#/components/schemas/Revision"}, {"type": "null"}]} |
| roundId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["DRAFT", "IN_REVIEW", "RETURNED", "APPROVED", "COMPLETED", "REJECTED", "CANCELLED"]} |
| templateVersion | True | string | {"enum": ["BASE-VA-1"]} |
| terms | True | #/components/schemas/Terms | {} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## DossierCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## DossierCommand_CANCEL

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DossierCommand_CLONE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CLONE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## DossierCommand_SUBMIT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT"]} |
| l1ReviewerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| l2ReviewerId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |

## DossierCommand_WITHDRAW

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["WITHDRAW"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## DossierCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| externalAccountReference | True | string | {"minLength": 1, "maxLength": 100} |
| linkedFromDossierId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| relationshipId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| representation | True | #/components/schemas/Representation | {} |
| terms | True | #/components/schemas/Terms | {} |

## DossierPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Dossier"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## DossierRoundSnapshot

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| revision | True | #/components/schemas/Revision | {} |
| targetKind | True | string | {"enum": ["DOSSIER"]} |

## DossierUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| externalAccountReference | True | string | {"minLength": 1, "maxLength": 100} |
| representation | True | #/components/schemas/Representation | {} |
| terms | True | #/components/schemas/Terms | {} |

## Evaluation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| configHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| evaluatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| factsHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| policyVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| results | True | array | {"items": {"$ref": "#/components/schemas/RuleResult"}} |
| route | True | string | {"enum": ["L1", "L1_L2", "BLOCKED"]} |

## EvidenceCheck

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basis | True | string | {"minLength": 1, "maxLength": 500} |
| checkedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| fileId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| result | True | string | {"enum": ["PASS", "FAIL", "NEEDS_CLARIFICATION"]} |

## ExtensionRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| contributors | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| decidedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| decisionBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| requestedDueAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| requesterId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| state | True | string | {"enum": ["PENDING", "APPROVED", "REJECTED"]} |

## FactVector

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| candidateL1Id | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| candidateL2Id | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| documents | True | array | {"items": {"additionalProperties": false, "properties": {"clean": {"type": "boolean"}, "humanVerified": {"type": "boolean"}, "purpose": {"enum": ["SERVICE_APPLICATION", "CORPORATE_ID", "REPRESENTATION_BASIS", "SERVICE_TERMS", "IDENTITY_IMAGE", "SIGNING_COPY", "NOTICE_EVIDENCE", "DUTY_EVIDENCE", "CONTROL_EVIDENCE", "DATA_BASIS"], "type": "string"}}, "required": ["purpose", "clean", "humanVerified"], "type": "object"}} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |
| representation | True | #/components/schemas/Representation | {} |
| terms | True | #/components/schemas/Terms | {} |

## FactsSimulation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| facts | True | #/components/schemas/FactVector | {} |
| mode | True | string | {"enum": ["FACTS"]} |

## File

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| attached | True | boolean | {} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| filename | True | string | {"minLength": 1, "maxLength": 150} |
| generatedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| humanVerification | True | string | {"enum": ["NOT_CHECKED", "PASS", "FAIL"]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| mediaType | True | string | {"enum": ["application/pdf", "image/jpeg", "image/png"]} |
| origin | True | string | {"enum": ["UPLOADED", "GENERATED"]} |
| purpose | True | string | {"enum": ["SERVICE_APPLICATION", "CORPORATE_ID", "REPRESENTATION_BASIS", "SERVICE_TERMS", "IDENTITY_IMAGE", "SIGNING_COPY", "NOTICE_EVIDENCE", "DUTY_EVIDENCE", "CONTROL_EVIDENCE", "DATA_BASIS"]} |
| rendererVersion | True | composed | {"anyOf": [{"maxLength": 100, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| scanJobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| scannedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| scannerVersion | True | composed | {"anyOf": [{"maxLength": 80, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| sensitive | True | boolean | {} |
| sha256 | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| signatureDatabaseVersion | True | composed | {"anyOf": [{"maxLength": 80, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| sizeBytes | True | integer | {"minimum": 1, "maximum": 10485760} |
| state | True | string | {"enum": ["SCANNING", "CLEAN", "INFECTED", "SCAN_FAILED", "DISPOSED"]} |
| targetId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| targetKind | True | string | {"enum": ["DOSSIER", "CONTRACT", "CHANGE", "RELATIONSHIP", "INCIDENT", "OBLIGATION", "CONTROL_REQUEST", "DATA_REQUEST", "NOTICE", "POLICY"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| uploadedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| version | True | integer | {"minimum": 1} |

## FileCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## FileCommand_RETRY

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETRY"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## FilePage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/File"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## FileUpload

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| branchId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| file | True | string | {"format": "binary"} |
| purpose | True | string | {"enum": ["SERVICE_APPLICATION", "CORPORATE_ID", "REPRESENTATION_BASIS", "SERVICE_TERMS", "IDENTITY_IMAGE", "SIGNING_COPY", "NOTICE_EVIDENCE", "DUTY_EVIDENCE", "CONTROL_EVIDENCE", "DATA_BASIS"]} |
| targetId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| targetKind | True | string | {"enum": ["DOSSIER", "CONTRACT", "CHANGE", "RELATIONSHIP", "INCIDENT", "OBLIGATION", "CONTROL_REQUEST", "DATA_REQUEST", "NOTICE", "POLICY"]} |

## Incident

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| blockingResume | True | boolean | {} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| durationHours | True | composed | {"anyOf": [{"maximum": 720, "minimum": 1, "type": "integer"}, {"type": "null"}]} |
| externalComplaintConfirmation | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ReceiptEvidence"}, {"type": "null"}]} |
| externalDueAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| externalReference | True | composed | {"anyOf": [{"maxLength": 200, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| externalRuleBasis | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| kind | True | string | {"enum": ["CONTRACT_PERFORMANCE", "PAYMENT_COMPLAINT_REFERENCE"]} |
| obligationId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| receiptEvidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| receivedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| remediationTrigger | True | composed | {"anyOf": [{"enum": ["VERIFICATION_CONFIRMED", "NOTICE_RECEIVED"], "type": "string"}, {"type": "null"}]} |
| resolutionEvidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| state | True | string | {"enum": ["OPEN", "VERIFIED", "REMEDIATION", "RESOLVED", "CLOSED"]} |
| summary | True | string | {"minLength": 1, "maxLength": 2000} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## IncidentCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## IncidentCommand_CLOSE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CLOSE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## IncidentCommand_RESOLVE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RESOLVE"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| externalComplaintConfirmation | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ReceiptEvidence"}, {"type": "null"}]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## IncidentCommand_SUBMIT_RESOLUTION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT_RESOLUTION"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| summary | True | string | {"minLength": 1, "maxLength": 1000} |

## IncidentCommand_VERIFY

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["VERIFY"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| triggerAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| triggerEvidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |

## IncidentCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| blockingResume | True | boolean | {} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| durationHours | True | composed | {"anyOf": [{"maximum": 720, "minimum": 1, "type": "integer"}, {"type": "null"}]} |
| externalDueAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| externalReference | True | composed | {"anyOf": [{"maxLength": 200, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| externalRuleBasis | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| kind | True | string | {"enum": ["CONTRACT_PERFORMANCE", "PAYMENT_COMPLAINT_REFERENCE"]} |
| receiptEvidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| receivedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| remediationTrigger | True | composed | {"anyOf": [{"enum": ["VERIFICATION_CONFIRMED", "NOTICE_RECEIVED"], "type": "string"}, {"type": "null"}]} |
| summary | True | string | {"minLength": 1, "maxLength": 2000} |

## IncidentPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Incident"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## Job

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| attempts | True | integer | {"minimum": 0, "maximum": 5} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| completedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| kind | True | string | {"enum": ["FILE_SCAN", "ARTIFACT_PDF", "NOTICE_DISPATCH", "DATA_DISPOSITION", "EFFECTIVITY"]} |
| lastErrorCode | True | composed | {"anyOf": [{"enum": ["SCANNER_UNAVAILABLE", "TRANSPORT_UNAVAILABLE", "STORAGE_INTEGRITY", "GUARD_BLOCKED", "LEASE_EXPIRED"], "type": "string"}, {"type": "null"}]} |
| nextAttemptAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| parentId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| parentKind | True | string | {"enum": ["FILE", "CONTRACT", "CHANGE", "NOTICE", "DATA_REQUEST"]} |
| resultFileId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| startedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["QUEUED", "RUNNING", "SUCCEEDED", "FAILED", "CANCELLED"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## JobCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## JobCommand_RETRY

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETRY"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## JobPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Job"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## LegalHold

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basisFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| placedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| placedBy | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| recordIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| releasedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| releasedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |

## LoginRequest

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| password | True | string | {"minLength": 1, "maxLength": 128} |
| username | True | string | {"minLength": 1, "maxLength": 40} |

## MandatoryCheck

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| checkedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| code | True | string | {"enum": ["AUTHORITY_CURRENT", "LEGAL_CONDITION", "SERVICE_READINESS"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| result | True | string | {"enum": ["PASS", "BLOCK", "REVIEW_REQUIRED"]} |

## Notice

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| ackCandidate | True | composed | {"anyOf": [{"$ref": "#/components/schemas/AckCandidate"}, {"type": "null"}]} |
| acknowledgedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| artifactHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| artifactId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| artifactJobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| bodyText | True | string | {"minLength": 1, "maxLength": 5000} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| changeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| channel | True | string | {"enum": ["EMAIL"]} |
| clauseVersion | True | string | {"minLength": 1, "maxLength": 80} |
| contactVersion | True | integer | {"minimum": 1} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| deemedReceivedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| deliveredAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| dispatchAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| jobId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| messageId | True | string | {"minLength": 1, "maxLength": 100} |
| purpose | True | string | {"enum": ["TARIFF_CHANGE", "CONTRACTUAL_REMINDER", "TERMINATION", "GENERAL"]} |
| receiptEvidence | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ReceiptEvidence"}, {"type": "null"}]} |
| recipient | True | #/components/schemas/Contact | {} |
| recipientSnapshotHash | True | composed | {"anyOf": [{"pattern": "^[a-f0-9]{64}$", "type": "string"}, {"type": "null"}]} |
| state | True | string | {"enum": ["DRAFT", "IN_REVIEW", "APPROVED", "QUEUED", "DISPATCHED", "FAILED", "ACKNOWLEDGED", "CANCELLED"]} |
| subject | True | string | {"minLength": 1, "maxLength": 200} |
| superseded | True | boolean | {} |
| transport | True | string | {"enum": ["LOCAL_CAPTURE", "MANUAL_EXTERNAL"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## NoticeCapture

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| acceptedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| bodyText | True | string | {"minLength": 1, "maxLength": 5000} |
| messageId | True | string | {"minLength": 1, "maxLength": 100} |
| noticeId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| recipient | True | #/components/schemas/Contact | {} |
| subject | True | string | {"minLength": 1, "maxLength": 200} |

## NoticeCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## NoticeCommand_APPROVE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_CANCEL

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_DISPATCH

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["DISPATCH"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_RECORD_ACK

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| acknowledgedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| action | True | string | {"enum": ["RECORD_ACK"]} |
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| recipientSnapshotHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |

## NoticeCommand_RECORD_EXTERNAL_DISPATCH

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RECORD_EXTERNAL_DISPATCH"]} |
| dispatchAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| receiptEvidence | True | #/components/schemas/ReceiptEvidence | {} |

## NoticeCommand_RECORD_RECEIPT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RECORD_RECEIPT"]} |
| deemedReceivedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| deliveredAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| receiptEvidence | True | #/components/schemas/ReceiptEvidence | {} |

## NoticeCommand_REJECT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_RETRY

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETRY"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_RETURN

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETURN"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_SUBMIT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT"]} |
| expectedContactVersion | True | integer | {"minimum": 1} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCommand_VERIFY_ACK

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["VERIFY_ACK"]} |
| candidateId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| decision | True | string | {"enum": ["ACCEPT", "RETURN"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## NoticeCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| bodyText | True | string | {"minLength": 1, "maxLength": 5000} |
| changeId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| clauseVersion | True | string | {"minLength": 1, "maxLength": 80} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| purpose | True | string | {"enum": ["TARIFF_CHANGE", "CONTRACTUAL_REMINDER", "TERMINATION", "GENERAL"]} |
| subject | True | string | {"minLength": 1, "maxLength": 200} |
| transport | True | string | {"enum": ["LOCAL_CAPTURE", "MANUAL_EXTERNAL"]} |

## NoticePage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Notice"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## NoticeUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| bodyText | True | string | {"minLength": 1, "maxLength": 5000} |
| subject | True | string | {"minLength": 1, "maxLength": 200} |

## Obligation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| basisText | True | string | {"minLength": 1, "maxLength": 1000} |
| basisType | True | string | {"enum": ["INTERNAL", "CONTRACTUAL", "STATUTORY_EXTERNAL"]} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| breachedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| closureOutcome | True | composed | {"anyOf": [{"enum": ["FULFILLED", "RENEW", "DECLINE", "NEW_AGREEMENT", "WAIVED"], "type": "string"}, {"type": "null"}]} |
| contractId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| currentDueAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| deadlineHistory | True | array | {"items": {"$ref": "#/components/schemas/DeadlineRevision"}} |
| dueRevision | True | integer | {"minimum": 1} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| evidenceSummary | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| extensionRequest | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ExtensionRequest"}, {"type": "null"}]} |
| externalConfirmations | True | array | {"items": {"$ref": "#/components/schemas/ReceiptEvidence"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| originalDueAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| sourceId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| state | True | string | {"enum": ["OPEN", "EVIDENCE_SUBMITTED", "RETURNED", "CLOSED", "CANCELLED"]} |
| supersededById | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| type | True | string | {"enum": ["NOTICE_ACK", "REMEDIATION", "TERM_REVIEW", "CLOSEOUT"]} |
| unresolvedIncidentIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |
| waiverRequest | True | composed | {"anyOf": [{"$ref": "#/components/schemas/ExtensionRequest"}, {"type": "null"}]} |

## ObligationCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## ObligationCommand_APPROVE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ObligationCommand_CANCEL_INAPPLICABLE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CANCEL_INAPPLICABLE"]} |
| endingBasis | True | string | {"minLength": 1, "maxLength": 1000} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |

## ObligationCommand_DECIDE_EXTENSION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["DECIDE_EXTENSION"]} |
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| bilateralAcceptanceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| decision | True | string | {"enum": ["APPROVE", "REJECT"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| requestId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## ObligationCommand_DECIDE_WAIVER

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["DECIDE_WAIVER"]} |
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| bilateralAcceptanceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| decision | True | string | {"enum": ["APPROVE", "REJECT"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| requestId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## ObligationCommand_REQUEST_EXTENSION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REQUEST_EXTENSION"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| requestedDueAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |

## ObligationCommand_REQUEST_WAIVER

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REQUEST_WAIVER"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ObligationCommand_RETURN

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETURN"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## ObligationCommand_SUBMIT_EVIDENCE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUBMIT_EVIDENCE"]} |
| closureOutcome | True | string | {"enum": ["FULFILLED", "RENEW", "DECLINE", "NEW_AGREEMENT"]} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| externalConfirmations | True | array | {"items": {"$ref": "#/components/schemas/ReceiptEvidence"}} |
| summary | True | string | {"minLength": 1, "maxLength": 1000} |

## ObligationCommand_SUPERSEDE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["SUPERSEDE"]} |
| endingBasis | True | string | {"minLength": 1, "maxLength": 1000} |
| evidenceFileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| replacementId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |

## ObligationPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Obligation"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## Party

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| name | True | string | {"minLength": 1, "maxLength": 200} |
| taxId | True | string | {"pattern": "^([0-9]{10}\|[0-9]{13})$"} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## PartyCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| name | True | string | {"minLength": 1, "maxLength": 200} |
| taxId | True | string | {"pattern": "^([0-9]{10}\|[0-9]{13})$"} |

## PartyPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Party"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## PartyUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| name | True | string | {"minLength": 1, "maxLength": 200} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## Policy

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| config | True | #/components/schemas/PolicyConfig | {} |
| configHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| effectiveFrom | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| effectiveTo | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| regressionReport | True | composed | {"anyOf": [{"$ref": "#/components/schemas/RegressionReport"}, {"type": "null"}]} |
| scheduleVersion | True | integer | {"minimum": 0} |
| state | True | string | {"enum": ["DRAFT", "IN_REVIEW", "SCHEDULED", "ACTIVE", "RETIRED", "REJECTED"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## PolicyCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## PolicyCommand_APPROVE_PUBLICATION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE_PUBLICATION"]} |
| expectedScheduleVersion | True | integer | {"minimum": 0} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## PolicyCommand_CLONE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["CLONE"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## PolicyCommand_REJECT_PUBLICATION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT_PUBLICATION"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## PolicyCommand_REQUEST_PUBLICATION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REQUEST_PUBLICATION"]} |
| effectiveFrom | True | string | {"format": "date"} |
| expectedScheduleVersion | True | integer | {"minimum": 0} |
| regressionConfigHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |

## PolicyCommand_RUN_REGRESSION

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RUN_REGRESSION"]} |
| corpusVersion | True | string | {"enum": ["POLICY-CORPUS-1"]} |

## PolicyConfig

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedTermKinds | True | array | {"items": {"enum": ["FIXED", "OPEN_ENDED"], "type": "string"}} |
| maxFeeVnd | True | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| maxFixedTermDays | True | integer | {"minimum": 1, "maximum": 3650} |
| minFeeVnd | True | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| minFixedTermDays | True | integer | {"minimum": 1, "maximum": 3650} |
| referenceFeeVnd | True | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| requiredDocumentPurposes | True | array | {"items": {"enum": ["SERVICE_APPLICATION", "CORPORATE_ID", "REPRESENTATION_BASIS", "SERVICE_TERMS", "IDENTITY_IMAGE"], "type": "string"}} |
| reviewDueWorkingDays | True | integer | {"minimum": 1, "maximum": 10} |

## PolicyCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| cloneFromId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| config | True | #/components/schemas/PolicyConfig | {} |

## PolicyPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Policy"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## PolicySimulation

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## PolicyUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| config | True | #/components/schemas/PolicyConfig | {} |

## Problem

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| code | True | string | {"enum": ["BAD_JSON", "UNAUTHENTICATED", "ROLE_FORBIDDEN", "CSRF_INVALID", "SOD_VIOLATION", "SENSITIVE_FORBIDDEN", "EXPORT_FORBIDDEN", "NOT_FOUND", "STATE_CONFLICT", "IDEMPOTENCY_CONFLICT", "IDEMPOTENCY_IN_PROGRESS", "BASE_CONFLICT", "SCHEDULE_CONFLICT", "POLICY_SCHEDULE_CONFLICT", "AUTHORITY_REVIEW_REQUIRED", "LEGAL_REVIEW_REQUIRED", "ARTIFACT_NOT_READY", "READINESS_BLOCKED", "NOTICE_NOT_READY", "UNRESOLVED_DUTY", "DUPLICATE_PARTY", "DUPLICATE_RELATIONSHIP", "HOLD_ACTIVE", "PRECONDITION_FAILED", "FILE_TOO_LARGE", "MEDIA_UNSUPPORTED", "VALIDATION_FAILED", "POLICY_BLOCKED", "FILE_NOT_CLEAN", "FILE_LIMIT", "AUTHORITY_INVALID", "NOTICE_LEAD_TOO_SHORT", "LATE_SIGNING", "NO_RENEWAL", "EXPORT_LIMIT", "REGRESSION_FAILED", "NONWAIVABLE_DUTY", "PRECONDITION_REQUIRED", "RATE_LIMITED", "SERVICE_UNAVAILABLE", "STORAGE_INTEGRITY", "CURSOR_EXPIRED", "DISPOSITION_COMMITTED"]} |
| currentVersion | True | composed | {"anyOf": [{"minimum": 1, "type": "integer"}, {"type": "null"}]} |
| detail | True | string | {"minLength": 1, "maxLength": 2000} |
| instance | True | string | {"minLength": 1, "maxLength": 500} |
| status | True | integer | {"enum": [400, 401, 403, 404, 409, 410, 412, 413, 415, 422, 428, 429, 503]} |
| title | True | string | {"minLength": 1, "maxLength": 120} |
| traceId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| type | True | string | {"format": "uri-reference"} |
| violations | True | array | {"items": {"$ref": "#/components/schemas/Violation"}} |

## ReadinessRisk

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| evidenceIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| raisedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| raisedBy | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| resolutionBasis | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| resolutionBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| resolutionEvidenceIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| resolvedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |

## ReceiptEvidence

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basis | True | string | {"minLength": 1, "maxLength": 1000} |
| fileIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| observedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| sourceLabel | True | string | {"enum": ["EXTERNAL_RECORDED", "SIMULATED"]} |

## RegressionReport

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| configHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| corpusVersion | True | string | {"enum": ["POLICY-CORPUS-1"]} |
| generatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| passed | True | boolean | {} |
| results | True | array | {"items": {"$ref": "#/components/schemas/RegressionResult"}} |

## RegressionResult

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| actualRoute | True | string | {"enum": ["L1", "L1_L2", "BLOCKED"]} |
| expectedRoute | True | string | {"enum": ["L1", "L1_L2", "BLOCKED"]} |
| passed | True | boolean | {} |
| scenarioId | True | string | {"minLength": 1, "maxLength": 60} |

## RegressionSimulation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| corpusVersion | True | string | {"enum": ["POLICY-CORPUS-1"]} |
| mode | True | string | {"enum": ["REGRESSION"]} |

## Relationship

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| confirmedContact | True | #/components/schemas/Contact | {} |
| contactVersion | True | integer | {"minimum": 1} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| partyId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## RelationshipCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| confirmedContact | True | #/components/schemas/Contact | {} |
| partyId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## RelationshipPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Relationship"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## RelationshipUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| assignedMakerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## RenewalTarget

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| endExclusive | True | string | {"format": "date"} |
| feeVnd | False | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| kind | True | string | {"enum": ["RENEWAL"]} |

## Representation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basisType | True | string | {"enum": ["LEGAL_REPRESENTATIVE", "POWER_OF_ATTORNEY"]} |
| checkEvidenceFileId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| checkedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| issuerReference | True | string | {"minLength": 1, "maxLength": 200} |
| knownRevocationAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| personName | True | string | {"minLength": 1, "maxLength": 120} |
| scopeText | True | string | {"minLength": 1, "maxLength": 1000} |
| sourceFileId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| title | True | string | {"minLength": 1, "maxLength": 120} |
| validFrom | True | string | {"format": "date"} |
| validTo | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |

## ReviewerOption

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| displayName | True | string | {"minLength": 1, "maxLength": 120} |
| level | True | string | {"enum": ["L1", "L2"]} |
| userId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## ReviewerOptions

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/ReviewerOption"}} |

## Revision

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| contact | True | #/components/schemas/Contact | {} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| documents | True | array | {"items": {"$ref": "#/components/schemas/DocumentLink"}} |
| evaluation | True | #/components/schemas/Evaluation | {} |
| evidenceHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| externalAccountReference | True | string | {"minLength": 1, "maxLength": 100} |
| factsHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| number | True | integer | {"minimum": 1} |
| representation | True | #/components/schemas/SubmittedRepresentation | {} |
| submittedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| terms | True | #/components/schemas/Terms | {} |

## Round

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| authorityChecked | True | boolean | {} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| dueAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| evidenceChecks | True | array | {"items": {"$ref": "#/components/schemas/EvidenceCheck"}} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |
| policyVersionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| revisionId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| state | True | string | {"enum": ["OPEN", "APPROVED", "RETURNED", "REJECTED", "CANCELLED"]} |
| steps | True | array | {"items": {"$ref": "#/components/schemas/ApprovalStep"}} |
| targetId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| targetKind | True | string | {"enum": ["DOSSIER", "CHANGE", "CONTROL_REQUEST"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| version | True | integer | {"minimum": 1} |

## RoundCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## RoundCommand_APPROVE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["APPROVE"]} |
| level | True | string | {"enum": ["L1", "L2"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## RoundCommand_REASSIGN

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REASSIGN"]} |
| level | True | string | {"enum": ["L1", "L2"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| reviewerId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |

## RoundCommand_REJECT

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["REJECT"]} |
| level | True | string | {"enum": ["L1", "L2"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## RoundCommand_RETURN

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETURN"]} |
| level | True | string | {"enum": ["L1", "L2"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## RoundCommand_VERIFY_EVIDENCE

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["VERIFY_EVIDENCE"]} |
| authorityChecked | True | boolean | {} |
| checks | True | array | {"items": {"$ref": "#/components/schemas/EvidenceCheck"}} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |

## RoundPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/Round"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## RoundSnapshot

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## RuleResult

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| actual | True | string | {"minLength": 0, "maxLength": 200} |
| expected | True | string | {"minLength": 0, "maxLength": 200} |
| message | True | string | {"minLength": 1, "maxLength": 1000} |
| result | True | string | {"enum": ["PASS", "BLOCK", "REVIEW_REQUIRED"]} |
| ruleId | True | string | {"minLength": 1, "maxLength": 60} |
| sourceKind | True | string | {"enum": ["INTERNAL_POLICY", "HARD_INVARIANT", "REVIEWED_EXTERNAL_CONDITION"]} |
| sourceVersion | True | string | {"minLength": 1, "maxLength": 100} |

## Session

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| authenticated | True | boolean | {} |
| csrfToken | True | string | {"minLength": 1, "maxLength": 200} |
| expiresAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| user | True | composed | {"anyOf": [{"$ref": "#/components/schemas/User"}, {"type": "null"}]} |

## SigningAuthority

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| bank | True | #/components/schemas/SubmittedRepresentation | {} |
| beneficiary | True | #/components/schemas/SubmittedRepresentation | {} |

## SigningPacket

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| artifactId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| authority | True | #/components/schemas/SigningAuthority | {} |
| bankSignedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| beneficiarySignedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| contributorIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| signedCopyIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| signingMethod | True | string | {"enum": ["PAPER_SCAN", "EXTERNALLY_SIGNED"]} |
| targetId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| targetKind | True | string | {"enum": ["CONTRACT", "CHANGE"]} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| verificationReason | True | composed | {"anyOf": [{"maxLength": 1000, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| verificationStatus | True | string | {"enum": ["PENDING", "VERIFIED", "RETURNED"]} |
| verifiedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| verifiedBy | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| version | True | integer | {"minimum": 1} |

## SigningPacketCommand

Conditional/composed schema: read exact oneOf/anyOf/allOf in JSON reference; do not merge mutually exclusive fields into one unrestricted form.


## SigningPacketCommand_RETURN_SIGNING

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["RETURN_SIGNING"]} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## SigningPacketCommand_VERIFY_SIGNING

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| action | True | string | {"enum": ["VERIFY_SIGNING"]} |
| checks | True | array | {"items": {"$ref": "#/components/schemas/EvidenceCheck"}} |
| mandatoryChecks | True | array | {"items": {"$ref": "#/components/schemas/MandatoryCheck"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |

## SigningPacketCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| artifactHash | True | string | {"pattern": "^[a-f0-9]{64}$"} |
| artifactId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| authority | True | #/components/schemas/SigningAuthority | {} |
| bankSignedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| beneficiarySignedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| signedCopyIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| signingMethod | True | string | {"enum": ["PAPER_SCAN", "EXTERNALLY_SIGNED"]} |
| targetId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| targetKind | True | string | {"enum": ["CONTRACT", "CHANGE"]} |

## SigningPacketPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/SigningPacket"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## SimulationResponse

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| evaluation | True | composed | {"anyOf": [{"$ref": "#/components/schemas/Evaluation"}, {"type": "null"}]} |
| regressionReport | True | composed | {"anyOf": [{"$ref": "#/components/schemas/RegressionReport"}, {"type": "null"}]} |

## SubmittedRepresentation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| basisType | True | string | {"enum": ["LEGAL_REPRESENTATIVE", "POWER_OF_ATTORNEY"]} |
| checkEvidenceFileId | True | composed | {"anyOf": [{"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}, {"type": "null"}]} |
| checkedAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| issuerReference | True | string | {"minLength": 1, "maxLength": 200} |
| knownRevocationAt | True | composed | {"anyOf": [{"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$", "type": "string"}, {"type": "null"}]} |
| personName | True | string | {"minLength": 1, "maxLength": 120} |
| scopeText | True | string | {"minLength": 1, "maxLength": 1000} |
| sourceFileId | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| title | True | string | {"minLength": 1, "maxLength": 120} |
| validFrom | True | string | {"format": "date"} |
| validTo | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |

## TariffTarget

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| feeVnd | True | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| kind | True | string | {"enum": ["TARIFF_NOTICE"]} |

## Terms

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| clauseVersion | True | string | {"enum": ["NEGOTIATED-VA-1", "TARIFF-ACK-7D-1"]} |
| currency | True | string | {"enum": ["VND"]} |
| endExclusive | True | composed | {"anyOf": [{"format": "date", "type": "string"}, {"type": "null"}]} |
| feePayer | True | string | {"enum": ["BENEFICIARY", "UNAGREED"]} |
| feeVnd | True | string | {"pattern": "^(0\|[1-9][0-9]{0,9})$"} |
| pricingMode | True | string | {"enum": ["NEGOTIATED_FIXED", "STANDARD_TARIFF"]} |
| startOn | True | string | {"format": "date"} |
| termKind | True | string | {"enum": ["FIXED", "OPEN_ENDED"]} |

## User

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| allowedActions | True | array | {"items": {"enum": ["UPDATE", "SUBMIT", "WITHDRAW", "CANCEL", "CLONE", "APPROVE", "RETURN", "REJECT", "VERIFY_EVIDENCE", "REASSIGN", "SIMULATE", "REQUEST_PUBLICATION", "APPROVE_PUBLICATION", "REJECT_PUBLICATION", "SIGN", "VERIFY_SIGNING", "RETURN_SIGNING", "COMMIT", "REBASE", "REQUEST_CANCEL_SCHEDULE", "APPROVE_CANCEL_SCHEDULE", "REJECT_CANCEL_SCHEDULE", "DISPATCH", "RETRY", "RECORD_RECEIPT", "SUBMIT_EVIDENCE", "VERIFY", "REQUEST_EXTENSION", "DECIDE_EXTENSION", "REQUEST_WAIVER", "DECIDE_WAIVER", "RESOLVE", "CLOSE", "CONFIRM", "DECIDE", "EXECUTE", "PLACE_HOLD", "RELEASE_HOLD", "DOWNLOAD_REDACTED", "DOWNLOAD_FULL", "EXPORT", "RUN_REGRESSION", "HOLD_READINESS", "RELEASE_READINESS", "RECORD_ACK", "VERIFY_ACK", "RECORD_EXTERNAL_DISPATCH", "START_REVIEW", "SUPERSEDE", "CANCEL_INAPPLICABLE", "SUBMIT_RESOLUTION"], "type": "string"}} |
| blockedActions | True | array | {"items": {"$ref": "#/components/schemas/BlockedAction"}} |
| branchIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| createdAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| displayName | True | string | {"minLength": 1, "maxLength": 120} |
| id | True | string | {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"} |
| permissions | True | array | {"items": {"enum": ["SENSITIVE_READ", "EXPORT"], "type": "string"}} |
| roles | True | array | {"items": {"enum": ["BANK_ADMIN", "MAKER", "CHECKER", "MANAGER", "POLICY_AUTHOR", "POLICY_REVIEWER", "DATA_OFFICER", "AUDITOR"], "type": "string"}} |
| updatedAt | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| username | True | string | {"pattern": "^[a-z][a-z0-9._-]{2,39}$"} |
| version | True | integer | {"minimum": 1} |

## UserCreate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| branchIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| displayName | True | string | {"minLength": 1, "maxLength": 120} |
| permissions | True | array | {"items": {"enum": ["SENSITIVE_READ", "EXPORT"], "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| roles | True | array | {"items": {"enum": ["BANK_ADMIN", "MAKER", "CHECKER", "MANAGER", "POLICY_AUTHOR", "POLICY_REVIEWER", "DATA_OFFICER", "AUDITOR"], "type": "string"}} |
| temporaryPassword | True | string | {"minLength": 12, "maxLength": 128} |
| username | True | string | {"minLength": 1, "maxLength": 40} |

## UserPage

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| asOf | True | string | {"format": "date-time", "pattern": "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$"} |
| items | True | array | {"items": {"$ref": "#/components/schemas/User"}} |
| nextCursor | True | composed | {"anyOf": [{"maxLength": 2048, "minLength": 1, "type": "string"}, {"type": "null"}]} |
| timelineProcessingLagSeconds | True | integer | {"minimum": 0} |

## UserUpdate

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| active | True | boolean | {} |
| branchIds | True | array | {"items": {"format": "uuid", "pattern": "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", "type": "string"}} |
| displayName | True | string | {"minLength": 1, "maxLength": 120} |
| permissions | True | array | {"items": {"enum": ["SENSITIVE_READ", "EXPORT"], "type": "string"}} |
| reason | True | string | {"minLength": 1, "maxLength": 1000} |
| roles | True | array | {"items": {"enum": ["BANK_ADMIN", "MAKER", "CHECKER", "MANAGER", "POLICY_AUTHOR", "POLICY_REVIEWER", "DATA_OFFICER", "AUDITOR"], "type": "string"}} |

## Violation

| Field | Required locally | Type/reference | Constraints / values |
|---|---|---|---|
| code | True | string | {"minLength": 1, "maxLength": 80} |
| message | True | string | {"minLength": 1, "maxLength": 1000} |
| path | True | string | {"minLength": 1, "maxLength": 200} |
