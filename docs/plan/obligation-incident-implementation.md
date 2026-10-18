# Obligation and Incident implementation

Canonical A/B1.2.2 remains unchanged. This note describes source implementation in `feature/backend-obligations-incidents`, not runtime/business acceptance. Initial source8926f45 and corrective commits978d3e7,520c395,ffb0c32 are under independent review; final source GO and skipped-test clean/package are pending.

## Operations and ownership

`obligation/ObligationController.java` and `ObligationService.java` implement listObligations, getObligation and commandObligation. List predicates include current Relationship assignment, type, strict currentDueAt/dueBefore, and everBreached from the immutable ledger, scoped before pagination. Projection derives current assignedMakerId and unresolved Incident references. Ordinary AUDITOR free-text evidence/basis projection is redacted.

`incident/IncidentController.java` and `IncidentService.java` implement listIncidents, createIncident, getIncident and commandIncident, with canonical kind/state/contract filters and current Contract→Relationship scope. Original receivedAt/externalDueAt/source do not reset on evidence or resolution.

All nine duty commands have source handlers: SUBMIT_EVIDENCE, APPROVE, RETURN, REQUEST_EXTENSION, DECIDE_EXTENSION, REQUEST_WAIVER, DECIDE_WAIVER, SUPERSEDE and CANCEL_INAPPLICABLE. **commandObligation remains partial for CLOSEOUT APPROVE until the real Control ServiceEndProof producer is integrated.** Missing proof fails closed; there is no successful placeholder.

All four Incident commands have handlers: VERIFY, SUBMIT_RESOLUTION, RESOLVE and CLOSE. Submission preserves VERIFIED/REMEDIATION. Independent RESOLVE accepts exact candidate only after required remediation is closed or validly waived and payment complaints have reviewed external confirmation. CLOSE is separate and preserves original source/receipt.

## Duty types and immutable deadlines

LifecycleObligations owns all factories. TERM_REVIEW derives from exact fixed ContractVersion, end minus30 calendar dates at17:00 Vietnam; closure records RENEW/DECLINE/NEW_AGREEMENT intent and does not apply renewal. CLOSEOUT derives after legal expiry/termination with five configured demo working days and actual ending source. NOTICE_ACK derives once from Notice immutable source revision and tariff effective boundary. REMEDIATION derives once from independent Incident verification and the selected demo trigger+48 elapsed hours; source-specific basis is retained consistently in public history and immutable due rows.

V011 alone adds cg_g5_event (immutable source/candidate/assessment/exception/ending records with server hash and monotonic ordinal), cg_obligation_due (immutable due revisions and safe existing-history backfill), and bounded candidate indexing. V001–V010 are unchanged. The existing cg_lifecycle_consequence and cg_lifecycle_obligation_breach remain authoritative dedupe stores; no competing public Job kind is introduced.

ObligationDeadlines repeatedly scans persisted nonterminal operative due values with strict due<now. Under Dossier→Contract→duty locks, synchronizeBreach inserts only the actual missed dueRevision, preserving earliest breach and all historical events. Late exceptions/closure synchronize before effect; prospective extension appends new operative due and does not fabricate breach at the superseded original deadline. originalDueAt never changes. INTERNAL exceptions require independent MANAGER; CONTRACTUAL requires independently assessed bilateral proof; STATUTORY_EXTERNAL rejects app waiver/extension. Array/history/contributor limits reject overflow instead of truncating evidence.

## Evidence, authorization and Notice integration

DutyEvidence centralizes exact CLEAN private-byte manifests, proper same-parent/same-Contract ancestor association, immutable history, current role/branch/assignment checks and stable source uploader contributors. Evidence association marks File.attached and retains historical references. The current actor is checked before idempotency replay and inside the command transaction. Multiple staged contributor additions accumulate rather than overwrite. Review assessments are not source edits; actual source/copy uploaders remain excluded from independent checks.

Files EvidenceAccess now locks actual INCIDENT/OBLIGATION ancestry for upload/scan callbacks as well as controllers. Incident create requires real same-Contract ancestor receipt evidence before the new Incident exists. Cross-contract/branch/sibling reuse fails. Read-only projection/hints do not freeze evidence, acquire mutation locks or write assessments.

Notice's existing noticeAck, acceptNoticeAck and supersedeNoticeAck hooks stay intact, including the pre-update Notice + independently verified candidate acceptance call. Shared synchronizeBreach replaces the old Notice-only helper. Generic duty approval cannot independently create an ACK assessment or bypass Notice VERIFY_ACK; Notice-owned immutable candidate/assessment remains authoritative. Cleanup retains dispatched/superseded history and historical breach.

VERIFY freezes the actual Incident original source ContractVersion, private trigger evidence and independent human assessment. VERIFICATION_CONFIRMED uses one server instant for verifiedAt and triggerAt. NOTICE_RECEIVED requires explicit nonfuture receipt triggerAt and sourced selected contractual clause/receipt evidence reviewed by CHECKER; code does not infer a universal receipt clause from template name or call48h a national rule. Human assessment is affirmative source review, not malware scanning or a fraud/legal certification.

## Cross-module dependency and verification boundary

IncidentService.requireResumeAllowed(UUID) is an actual same-Contract blockingResume query for upcoming Control confirmation; it blocks unless each flagged Incident is RESOLVED/CLOSED.

ServiceEndProof.verifiedEnded(UUID) is a narrow obligation-owned read interface for the Control-owned immutable STOP/TERMINATE confirmation proof implementation. ObjectProvider absence returns false. Closeout also queries actual unresolved Incidents and applicable nonterminal duties excluding itself, and requires independently assessed external confirmations. Neither observed STOPPED nor NOT_STARTED JSON by itself is adequate proof. This is an explicit remaining business dependency, so seven handlers do not mean all duty types are complete.

Developer compilation succeeded with maven.test.skip=true before final review refinements. Independent source review/final package are pending. V011 has not been run by this tranche. No automated tests, HTTP smoke/scenarios, PDF rendering, Docker startup or business-data operations were performed. FE schemas/permissions/API fields are unchanged; source capabilities and truthful action hints affect the seven operation IDs above. Runtime, business/integration and production acceptance remain unclaimed. Continue Control/Privacy and full85-operation source closure after bounded G5 review.
