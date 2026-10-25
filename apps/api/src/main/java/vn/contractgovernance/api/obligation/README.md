# Obligations and Incidents

Owns source-derived duties, immutable event/assessment/candidate records, operative due revisions and the shared immutable breach ledger. Seven canonical operations; no arbitrary duty creation or source history update endpoint. Source factories remain LifecycleObligations, including Notice direct verified ACK acceptance and exact-source supersession.

Every command holds Dossier/Contract ancestry before its leaf; Incident resolution then locks its linked duty. File upload/scanner follows the same new Incident/Obligation ancestry. Ordinary read/action-hint paths do not take mutation locks or freeze associations. Contract scope and current assignment/roles apply before idempotency replay and in the transaction; source uploaders remain stable contributors, reviewers remain assessments.

ObligationDeadlines scans persisted operative due records with strict due < now and dedupes on (obligation,dueRevision). Extensions synchronize old missed due first and append immutable revisions, preserving original deadline and earliest breach. Polling is recoverable after restart; it has no noncanonical public Job kind.

Incident source freezes the original receipt and ContractVersion. VERIFY is an independent affirmative human source/trigger assessment, not a fraud finding or byte scanner assertion. VERIFICATION_CONFIRMED uses server verification time; NOTICE_RECEIVED needs actual sourced contractual clause/receipt evidence and a nonfuture triggerAt. The checker explicitly assesses that evidence; no universal notice clause or statutory 48-hour deadline is inferred. The supported selected demo duration is 48 elapsed hours. Resolution source submission preserves VERIFIED/REMEDIATION; separate independent acceptance and closure retain all earlier receipt/deadline facts.

TERM_REVIEW records only a reviewed renewal/decline/new-agreement intent; closure does not apply a Change. NOTICE_ACK can only close through real Notice VERIFY_ACK; the generic duty path cannot waive a missing Notice readiness guard. Supersession does not erase breach or dispose complaints/remediation/closeout.

CLOSEOUT acceptance requires real unresolved Incident/duty queries and independently assessed external confirmation evidence. ServiceEndProof is a narrow read port for the upcoming Control-owned immutable STOP/TERMINATE confirmation producer. Its absence returns false and blocks closeout acceptance; this type-specific dependency remains partial at the G5 checkpoint. Service STOPPED/NOT_STARTED JSON alone never satisfies that proof. IncidentService.requireResumeAllowed is the real same-Contract blocking Incident guard for Control.

Source implementation and skipped-test compilation do not constitute HTTP/business/runtime acceptance. No automated tests, smoke scenarios or data operations are part of this implementation tranche.
