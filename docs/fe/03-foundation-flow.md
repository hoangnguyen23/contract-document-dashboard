# F1 detailed screen specification: session → dossier → approval

Read canonical fields in reference, never copy response fields into request. Every ID below must appear as labelled frame in Figma; each state is a component variant or separate frame linked from state index.

## Common wire behavior for all F1 screens

1. Bootstrap getSession before choosing authenticated shell. Login/logout follow exact CSRF/session protocol.401 clears auth state but retains unsent form in memory only; never auto-replay a command after login. Do not persist PII/draft/password in localStorage by default.
2. Load detail and retain response ETag outside editable payload. Form maps only request schema fields. Save uses exact If-Match; create/new command uses one UUIDv4 idempotency key for one intentional operation. Ambiguous network retry retains same key/body/ETag; user-edited intent gets a new key. No automatic semantic409/412 retries.
3. Commands succeed→invalidate exact affected detail, relevant lists/round/source relation/dashboard. Use server returned resource and reload canonical hints; don't optimistic-promote approval/scan/readiness.
4. Lists show items,nextCursor,asOf,lag as supplied; no total. Poll visible queue/job15s, pause hidden tab, stop/unsubscribe on unmount. Avoid overwriting dirty form with polling response; show newer-version banner.
5. Unknown queryfields never sent; branch change clears query cursor.403,404,410cursor,412,422,428,429,503 distinct copy/actions; use Retry-After only for bounded allowedretry. Problem.violations pointer→exact field and firstinvalid focus. Trace ID shown on systemfailure.

## S01 — Login and expired session

Desktop centered card width420px with product title “Quản lý hợp đồng & hồ sơ”, username/password labelled, password visibility accessible, primary “Đăng nhập”; no rememberpassword or rolepicker. Mobile fullwidth max420. States bootstrap skeleton, anonymous, submitting, invalidcredentials, throttled with serverretry guidance, serverunavailable, expiredsession. GET /session (getSession) → POST /session/login (login); POST /session/logout (logout). These are exact operation IDs. Disable only duplicate in-flight submit, not keyboardaccess. Success→return intended safe route if authorized else home. Logout confirms dirty draft before discarding UI; session logout result not fake.

## S03 — Relationship list

Header title, explanatory scope badge, conditional admin create/link action. Filterbar only canonical querykeys, no fullbank autocomplete. Table: relationship reference/party accessible label, branch, assignedMaker, contact confirmation status when resourceprovides it, updated/source timestamp only if field exists, open action. No contact/fullPII columns bydefault. Cursor next/refresh, no numberedpages. Empty copy distinguishes no scopeddata from appliedfilter noresults. Crossscope404 uses generic “Không tìm thấy hoặc không thể truy cập”, no “khách hàng tồn tại ở chi nhánh khác”. Selectrow→S04. Admin create dialog uses Party/Relationship exact schemas and duplicate422/409 mapping; no taxId existence lookup endpoint invented.

## S04 — Relationship360

Breadcrumb→scopedlist; header accessible Party/relationship identity, branch, assignment, ETag-managed current metadata. Tabs Overview, Contacts, Dossiers, Contracts, History only if data/API available; related lists use canonical relationshipId filter. No synthetic aggregate timeline endpoint. Contacts show confirmed version separately from pending ContactRequest, with requester/contributor/independence blockers. Assignment action only canonical allowed, picker serverauthorized, reason exactschema. Newdossier CTA requires assignedMaker/currentauthorization and forwards relationshipId.

## S06/S07 — Dossier wizard

Use five ordered sections, no state-changing step transitions:

| Step | Fields/content | Validation and source |
|---|---|---|
|1. Bối cảnh | selected relationship, Party identity accessible fields, assignment/scope | read relationship; do not permit arbitrary branchoverride |
|2. Điều khoản | Terms fields including feeVnd, termKind, startOn/endExclusive and canonical bundle identifiers | required/nullability/limits exact DossierCreate/Update/Terms; VND string; keep localdates unchanged; policy outcomes serverauthoritative |
|3. Đại diện | Representation fields/action/validity/sourcefile references per schema | validTo exclusive date visible; knownRevocationAt distinct recorded time; draft nullable evidence vs submissionrequired |
|4. Tài liệu | required-document checklist from current source/evaluation, upload rows and actual file state | allowlist/size exact B; selectedlocalfile not uploaded evidence; CLEAN not humanverified; link actual IDs only |
|5. Rà soát | current persisted facts, policy/rule output if actual endpoint supports target, contributors, planned route and blockers | no pretend approval/route from client; if policy simulation available make explicit advisory, not submitsuccess |

Footer: Back/Next, “Lưu bản nháp”, “Nộp hồ sơ” only at review and actual allowedhints. Stepnavigation checks local syntax; no automatic PUT on stepchange. Save creates or full-replaces mutable draft via correct schema; exclude id/version/audit/hints from request. On initialcreate success route to persisted edit/detail, keep its ETag. Dirty marker visible; routeleave dialog “Tiếp tục chỉnh sửa / Rời đi” and preserve no PII beyond memory.

Upload: select→local validation→uploadFile multipart fields according FileUpload→resourceQUARANTINED/SCANNING→pollgetFile/listJobs→CLEAN/INFECTED/SCAN_FAILED. Fileupload does not autoattach unless canonical command/body creates thatassociation; link only supported DossierUpdate documents field. Retry UI follows commandFile allowedaction; deletion must use canonical delete semantics and historical restrictions. Each row shows filename/size/type/sha short/state and parent; fullhash inmetadata drawer. Permissionfailedfile must not be accessible by retaining olddownloadURL.

Submit opens summarydialog of persisted serverfacts and blockers; unpersisted edits must be saved first. commandDossier SUBMIT uses latest detailETag and newintentkey; successnavigateS08 and refreshRound/list. Missing file, representation, stalecontact, reviewerunavailable, policynotactive render exact serverviolations; never mark “approved” aftersubmit. If maintenancemovedversion→412, preserve memoryedits and offer reload/diff; user decides saveagain with newETag, no blindforceoverwrite.

Required frames: S07-default; S07-invalidmoney; S07-missingauthority; S07-uploadscanning; S07-clean-unverified; S07-scanfailed; S07-dirtynewerversion; S07-412comparison; S07-submitblocked; S07-submitsuccess. Do not manually name canonical filestates from localuploadstates.

## S08 — Dossier detail / historical Revision

Header recorded state, relationship, ID and relevant serverdates. Tabs Overview, Documents, Revisions, Review/history. Actionpanel current allowed/blocked with reason; edit/cancel/withdraw/clone exact state. Revisionselectedview labelled “Bản đã nộp số…” immutable; never bind historicalvalue to editable inputs. Currentdraft vs selectedRevision explicitly separated. Roundlink uses actualroundID; if noRound show truthfulempty. RETURN→show returnedreason and newdraft/newround lifecycle onresubmit; rejectterminal uses clone ifallowed, not fake reopen. Finalapproval→link PREPARED Contract and showartifactpending, noteffectiveversion.

## S09 — Review queue

Filter targetkind DOSSIER/CHANGE/CONTROL_REQUEST and branch/state/duedate only canonical allowlist; showtargetlabel,currentstep,assignedreviewer,dueAt,sourceID. Currentactorassignment restriction derived from API, not fallback admin. L1/L2 stepnot justrolebadge. Due display uses serverdueAt/asOf; FE may display duration but cannot calculate newlegaldeadline. No aggregatecounts invented from first25rows. RowopensS10 with source kind.

## S10 — Review workbench

Desktop main content + right320px decisionpanel. Main sections: frozen facts/terms, party/contact/representation, source revision/hash, policy decision/rules, evidence checklist+fileviewer, assessmenthistory/contributors, roundstep timeline. Snapshot comes getRoundSnapshot; current Round comes getRound; never review editable draft facts instead. Both IDs/revision binding visible.

Evidence verification: select actualfile and mandatorycheck inputs per RoundCommand_VERIFY_EVIDENCE. Upload contributor cannot independently assess ownsource. Assessment PASS is not inferred from scanCLEAN or buttonpressed. Review of authority requires document and two-sided action/timefields where applicable; no legalvalidityclaim. Picker revieweroptions from getReviewerOptions not listUsers, candidates current eligible; changing role doesn't erase contributors.

Actions VERIFY_EVIDENCE, APPROVE, RETURN, REJECT, REASSIGN shown only when currenthint permits; all payloads drawn from their exact command branch. Decisiondialog includes exactrequiredreason and targetrevision/currentstep; approval must block if unresolved mandatory evidence/SoD/sourceguards. Disabled reason visible without hover. Success refresh Round/snapshothistory/dossier/contract/queues; finalL2 can create PREPARED+artifactjob pending. Losingauth/scope whileopen closes protectedviewer and handles nextrequestfailure; not stalebuttonsuccess.

Required frames: S10-L1pendingverification; selfcontributorblocked; evidenceverified; approvaldialog; returned; rejected; L2pending; sourcechanged412; noaccess404; finalapprovalPREPARED-jobqueued. No allgreen fabrication in assessmentdefaults.

## S29/S30 — File/job drawers reused in F1

Filemetadata shows parent/state/scannedAt/engine/signatureversion whenprovided, hash and actualrepresentation. Preview loaded by authorized request; revoke objectURL on close. PDF inaccessible fallback is metadata+authorizeddownload, never originalfullbytes fallback. Jobpanel state/attempt/source/currenterror and serverallowedretry; retry uses jobETag/currentauthorization. Fileworker success→refreshFile, not assumeRoundhumanverification. No canceljobbutton unless canonical command permits it.

## Planner stop conditions for implementing agent

Missing canonical field/query/action, snapshot mismatch, undocumentedpermission, need newenum/API, uncertainty about parsing conditionalrequest, or missing sourceevidence must be reported with operation/schema/BR, expectedbehavior and proposedminimalpatch. Do not solve by any/typeassertions, weakeningvalidation, invisibleAPIerror, automaticapprove/rebase or fake data. Routine spacing/componentcopy follows design system without askingowner.
