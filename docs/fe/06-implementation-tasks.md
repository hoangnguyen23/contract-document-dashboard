# FE implementation task graph and handoff

Planning only. No apps/web scaffold/code/packageinstallation/test authored or executed in this blueprint. Implementingagent owns actualfiles. Owner has separated testing; taskverification below defines later evidence, not permission torun it now. Codeagent must distinguish static/buildreview from tests; no fixture-as-runtimePASS.

## Task graph

| Task | Depends | Scope / owned paths (future) | Required deliverable |
|---|---|---|---|
| FE00 design | none | Figmafile + docs/fe/handoff references | FIGMA-01 accepted with actualframe/prototypelinks |
| FE01 scaffold | FE00 foundations | apps/web package/lock, TS/Vite/runtimeconfig, src/app | exactpinnedstack, appentry, routing/shell scaffold, envexample withoutsecrets; no domainimplementationmixed |
| FE02 contractclient | FE01 | apps/web/src/api/generated + sharedprotocoladapters; contracts ownership shared | clientfromcanonicalB, frozenoperationIDs/schemafields; no parallelmanualDTOs |
| FE03 session/protocol | FE02 | features/session, api/session/csrf/errors/concurrency | sessionbootstrap/login/logout, CSRFrotation, ETag/idempotency semantics, memoryonlydirtydraft; no automaticprivilegedreplay |
| FE04 components | FE01 + FE00components | src/shared/ui and tokens | designsystemcomponents/variants/accessibilitycontracts, tables/cursor/filter/errors/evidence/jobpanels |
| FE05 foundation | FE03+FE04 | features/relationships,dossiers,files,jobs | S03–S08/S29/S30 includingall03exceptionframes; canonicalupload/scan/link workflow |
| FE06 review | FE05 | features/approval | S09/S10 frozenRoundSnapshot/assessment/L1/L2/reassign and readonlysourceviews |
| FE07 policy/admin | FE03+FE04 | features/policy,administration | S11/S12/S31–33; finiteconfig, simulation/regression/publication and leastprivilegeadmin |
| FE08 originalcontract | FE06+FE07 | features/contracts,signing | S13–15, version/nullcurrentVersion, actualartifact/jobs, authority/independentchecks/holds/readiness |
| FE09 changes/notices | FE08 | features/changes,notices | S16–19, immutablebaselines/diffs/commit409/rebase, receipt/ACKindependence |
| FE10 duty/control | FE09 | features/obligations,incidents,controls | S20–25, source/duehistory/evidence/controlack/closeoutguards |
| FE11 privacy/report | FE10 | features/privacy,audit,dashboard | S26–28/S02 exactmanifest/holds/executionstatus/KPIasOf/redactedexport |
| FE12 integrationprep | FE05–11 | docs/fe handoff + approvedsharedruntimeconfig | scopeoperationmatrix, sameoriginHTTPS/certtrust, knownlimitations and separateacceptancerunplan |

Some component/design work can run concurrently in separateworktrees; no twoagents modify sameAPIadapter/generatedclient or globaltokens concurrently. Domainagent consumes committedsharedbaseline. Do not createallfeaturebranches beforedependencies exist; branchfromcurrentrevieweddev withtaskname/basecommit, neverREADME-onlymain. Preserve existingbranches/history; noforcepush/reset/mergewithoutauthorization.

## FE01 scaffold exact plan

Read A13/B2 runtimepins first: Node24.21.0/npm12.2.0, React19.3.0/TS7.0.2/Vite8.3.4/plugin-react6.1.2/Orval8.41.0 percanonical. Verifyactualupstreamavailability/integrity beforeinstall; if unavailable/unsupported, reportconcretecompatibilitypatch ratherthaninstalllatest. Do not treatdocumentationpins as alreadyverifiedonmachine. Browsergeneratorstrictcompatibility is a buildgate, not excuseforany/as assertions.

Proposedlayout: src/app/{providers,routes,layout}; src/api/{generated,transport,session,errors,query-keys}; src/shared/{ui,formatting}; src/features/{session,relationships,dossiers,approval,policy,contracts,signing,changes,notices,obligations,incidents,controls,privacy,audit,dashboard,administration,files,jobs}; src/styles. Eachfeature routes/pages/components/adapters only as needed; no globalmiscbusinesslogic. Query-cache/formlibrary is an implementationdecision topropose with version/license before adding if not alreadycanonical; no dependencysprawl or inventedmicrofrontend.

Use typedrouteparams and runtimecontractresponsevalidation where B requires. Generatedclientfolder nothandedited. Auth/ETag/idempotency wrapperscentralized, nofeature-specificfetch bypass. No SENSITIVE_READ-by-role assumption. Scaffold includesrouteerrorboundaryandunknownstate render; does not include dummybusiness-successhandlers masqueradingasBEintegration.

## Per-task implementation procedure

1. Inspectcleanworktree/baseline/designframeversion/A/Bhash andtaskdependencies. List exactlyfilesowned; nofrontendagent edits BE/domainmigrations.
2. Readassignedscreens andreferencerequest/responses/parameters/errors/commands. Produce operation→view→field→mutation→invalidation map beforecode.
3. Implementstate/dataadapter first, thencomponents/form andallrequiredstates; nohiddenbranch computingauthority/policy/legaldeadline locally.
4. Keepclientdraftvsserverresource/historicalsnapshot separate. Clone requestallowlist fields, preserve nullable/omitted semantics; no spreading resourcebody intoPUT.
5. Recordcodechanges andstatic/build evidence allowed by owner. Do not write/runautomatedtests or HTTP/browserworkflows under currentrestriction; testtasksremainpreparedonly.
6. Independentreviewer checks actualdiff againsttask, catchesfake/unconnectedpaths/any/permissionleak/missingstate, recordsblockingfindings. Fixthenreviewagain; no self-certifiedcomplete.
7. SubmitfeaturePR withexactbaseline/operation/frame/schemaimpact/review/build/notrunlimits. Noautomaticmergeor publicdeployment.

## Protocol cases agent must implement explicitly

- Newintent key distinctfromretry; exactreplayusesoriginalETag/body. Conflict409semanticCOMMIT may legitimatelyleaveChangeCONFLICTED; do notrollbackoptimisticstatewithoutrefresh. Other4xx notassumedsuccess.
-412/428 requirecorrectpreconditionhandling, not requestwithoutETag.410cursor clearscursorandrefresheswithnotice.429/503 boundedRetry-After handling andcancelonunmount; noautoretryapprove/sign/execute/refusalafterreauth.
- SessionCSRFbeforeunsafeincludinglogin/logout. No JWTlocalStorage/no passwordpersist/no logrequestpayloadPII.
- Resourceupdateinvalidatesownedkeys andaffectedqueues/dashboard; pollinghiddenpaused15svisible. Dirtyformnotreplacedbybackgroundfetch.
- Filecontent actualauthorizedrepresentation, hashheaders ifdisplayed; BlobURLrevoked; redactedmissingneverfullfallback. CSVcanonicalendpoint/EXPORTpermission, noCSVclientbypass.

## Independent review submission template

TaskID; branch/base/head; designfileversion/frameIDs; operations/schemas/actionsimplemented; filenames; allowed/blocked/empty/pending/errorstateshandled; expectedsourceandcanonicalconstraints; actualbuild/staticreviewevidence; testsNOTRUN; FE/BEcontractdelta(noneorownerapprovedpatch); unresolvedfindingsanddependencies. A screenshotisnotproofAPIcallworks; mockfixtureisnotrealbackendacceptance.

## Later verification packages (not authorized by this plan)

V-FE1 foundationrealbrowserlogin→relationship→draft/upload/scan/save anddeniedscope; V-FE2 immutableRevision/review/PDF/sign/readiness/effectivity; V-FE3 Change/Notice/ACKandboundary; V-FE4 duties/control/privacy/report; V-FE5 keyboard/200%zoom/mobile, providerconsumer/schema, recoveryandNFRaccordingA. EachpackageexpectedoutcomefromA/Bbeforeexecution; syntheticisolatedenvironmentandexacthashes, noactualPII/paidmessage/production. Owneropensexecutionstage separately; codecompletenessdoesnotsetthesetoPASS.
