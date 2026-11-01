# Screen → canonical operation matrix

Derived from canonical paths, not new API definitions. All85 operations must have a screen/component owner. Reference gives exact params/request/response and conditional branch fields. All errors and refresh dependencies follow03/06. Resolve target-specific detail semantics in04; sharing one operation does not permit mixing snapshots or permissions.

| Screen/component owner | Operation | Method/path | Mutation coordination |
|---|---|---|---|
| S28 | listAuditEvents | GET /audit-events | Read; scoped query only; no invented ETag on lists |
| S32 | listBranchs | GET /branches | Read; scoped query only; no invented ETag on lists |
| S32 | createBranch | POST /branches | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S32 | getBranch | GET /branches/{id} | Read; scoped query only; no invented ETag on lists |
| S32 | updateBranch | PUT /branches/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S16/S17 | listChanges | GET /changes | Read; scoped query only; no invented ETag on lists |
| S16/S17 | createChange | POST /changes | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S16/S17 | getChange | GET /changes/{id} | Read; scoped query only; no invented ETag on lists |
| S16/S17 | updateChange | PUT /changes/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S16/S17 | commandChange | POST /changes/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S04 | listContactRequests | GET /contact-requests | Read; scoped query only; no invented ETag on lists |
| S04 | createContactRequest | POST /contact-requests | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S04 | getContactRequest | GET /contact-requests/{id} | Read; scoped query only; no invented ETag on lists |
| S04 | commandContactRequest | POST /contact-requests/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S13/S14 | listContracts | GET /contracts | Read; scoped query only; no invented ETag on lists |
| S13/S14 | getContract | GET /contracts/{id} | Read; scoped query only; no invented ETag on lists |
| S13/S14 | commandContract | POST /contracts/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S13/S14 | listContractVersions | GET /contracts/{id}/versions | Read; scoped query only; no invented ETag on lists |
| S24/S25 | listControlRequests | GET /control-requests | Read; scoped query only; no invented ETag on lists |
| S24/S25 | createControlRequest | POST /control-requests | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S24/S25 | getControlRequest | GET /control-requests/{id} | Read; scoped query only; no invented ETag on lists |
| S24/S25 | commandControlRequest | POST /control-requests/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S02 | getDashboard | GET /dashboard | Read; scoped query only; no invented ETag on lists |
| S26/S27 | listDataRequests | GET /data-requests | Read; scoped query only; no invented ETag on lists |
| S26/S27 | createDataRequest | POST /data-requests | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S26/S27 | getDataRequest | GET /data-requests/{id} | Read; scoped query only; no invented ETag on lists |
| S26/S27 | commandDataRequest | POST /data-requests/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S05–S08 | listDossiers | GET /dossiers | Read; scoped query only; no invented ETag on lists |
| S05–S08 | createDossier | POST /dossiers | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S05–S08 | getDossier | GET /dossiers/{id} | Read; scoped query only; no invented ETag on lists |
| S05–S08 | updateDossier | PUT /dossiers/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S05–S08 | commandDossier | POST /dossiers/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S28 | exportAuditCsv | GET /exports/audit | Read; scoped query only; no invented ETag on lists |
| S29 + all evidence workbenches | listFiles | GET /files | Read; scoped query only; no invented ETag on lists |
| S29 + all evidence workbenches | uploadFile | POST /files | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S29 + all evidence workbenches | deleteUnattachedFile | DELETE /files/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S29 + all evidence workbenches | getFile | GET /files/{id} | Read; scoped query only; no invented ETag on lists |
| S29 + all evidence workbenches | commandFile | POST /files/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S29 + all evidence workbenches | downloadFile | GET /files/{id}/content | Read; scoped query only; no invented ETag on lists |
| S22/S23 | listIncidents | GET /incidents | Read; scoped query only; no invented ETag on lists |
| S22/S23 | createIncident | POST /incidents | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S22/S23 | getIncident | GET /incidents/{id} | Read; scoped query only; no invented ETag on lists |
| S22/S23 | commandIncident | POST /incidents/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S30 + async panels | listJobs | GET /jobs | Read; scoped query only; no invented ETag on lists |
| S30 + async panels | getJob | GET /jobs/{id} | Read; scoped query only; no invented ETag on lists |
| S30 + async panels | commandJob | POST /jobs/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S18/S19 | listNotices | GET /notices | Read; scoped query only; no invented ETag on lists |
| S18/S19 | createNotice | POST /notices | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S18/S19 | getNotice | GET /notices/{id} | Read; scoped query only; no invented ETag on lists |
| S18/S19 | updateNotice | PUT /notices/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S18/S19 | getNoticeCapture | GET /notices/{id}/capture | Read; scoped query only; no invented ETag on lists |
| S18/S19 | commandNotice | POST /notices/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S20/S21 | listObligations | GET /obligations | Read; scoped query only; no invented ETag on lists |
| S20/S21 | getObligation | GET /obligations/{id} | Read; scoped query only; no invented ETag on lists |
| S20/S21 | commandObligation | POST /obligations/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S33 | listPartys | GET /parties | Read; scoped query only; no invented ETag on lists |
| S33 | createParty | POST /parties | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S33 | getParty | GET /parties/{id} | Read; scoped query only; no invented ETag on lists |
| S33 | updateParty | PUT /parties/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S11/S12 | listPolicys | GET /policies | Read; scoped query only; no invented ETag on lists |
| S11/S12 | createPolicy | POST /policies | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S11/S12 | getPolicy | GET /policies/{id} | Read; scoped query only; no invented ETag on lists |
| S11/S12 | updatePolicy | PUT /policies/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S11/S12 | commandPolicy | POST /policies/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S11/S12 | simulatePolicy | POST /policies/{id}/simulation | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S03/S04/S06 | listRelationships | GET /relationships | Read; scoped query only; no invented ETag on lists |
| S03/S04/S06 | createRelationship | POST /relationships | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S03/S04/S06 | getRelationship | GET /relationships/{id} | Read; scoped query only; no invented ETag on lists |
| S03/S04/S06 | updateRelationship | PUT /relationships/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S10/assignment dialogs | getReviewerOptions | GET /reviewer-options | Read; scoped query only; no invented ETag on lists |
| S09/S10 | listRounds | GET /rounds | Read; scoped query only; no invented ETag on lists |
| S09/S10 | getRound | GET /rounds/{id} | Read; scoped query only; no invented ETag on lists |
| S09/S10 | commandRound | POST /rounds/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S09/S10 | getRoundSnapshot | GET /rounds/{id}/snapshot | Read; scoped query only; no invented ETag on lists |
| S01/AppShell | getSession | GET /session | Read; scoped query only; no invented ETag on lists |
| S01/AppShell | login | POST /session/login | CSRF/session rotation, no business idempotency assumption |
| S01/AppShell | logout | POST /session/logout | CSRF/session rotation, no business idempotency assumption |
| S15/S14/S17 | listSigningPackets | GET /signing-packets | Read; scoped query only; no invented ETag on lists |
| S15/S14/S17 | createSigningPacket | POST /signing-packets | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S15/S14/S17 | getSigningPacket | GET /signing-packets/{id} | Read; scoped query only; no invented ETag on lists |
| S15/S14/S17 | commandSigningPacket | POST /signing-packets/{id}/commands | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S31 | listUsers | GET /users | Read; scoped query only; no invented ETag on lists |
| S31 | createUser | POST /users | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
| S31 | getUser | GET /users/{id} | Read; scoped query only; no invented ETag on lists |
| S31 | updateUser | PUT /users/{id} | Exact canonical CSRF/Idempotency/If-Match headers; refresh related resource/queues; inspect exception semantics |
