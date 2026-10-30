# Independent manual-smoke repair source review — 30 October 2026

Verdict: **bounded source GO** for application commit `81c3d7f66628a397aa9c2d62dd26877448c25d10`, subject to separately identified candidate runtime/manual retest. This is not a claim that the original smoke failures passed, or that Backend acceptance is complete. The reviewer did not implement this repair.

## Baseline, actual defects and review boundary

Original delivery `ef33bae`, application `e718ccc`, JAR SHA256 `fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602` remain historical evidence. Existing isolated manual evidence under ignored `var/verification/manual-smoke-20261021-a/evidence` records: s07 canonical `@example.invalid` Relationship creation422, s12 authorized seeded Relationship read503, and s18 scoped File list503 with PostgreSQL JDBC unbound parameter13. The later `@example.com` input and direct-File GET continuation do not erase those FAILs.

Reviewed the exact five-file repair diff, enclosing ContractRegistry request/response validation, recursive scope callers and networknt3.0.8 dependency bytecode. No FE, canonical contract, dependency, migration, worker, business state or authentication code changed. No automatic tests, HTTP requests, runtime startup, database or private-file mutations were performed by this reviewer.

## EMAIL-RFC-SYNTHETIC — source repair accepted

- ContractRegistry clones the existing Draft2020-12 dialect and replaces only the format named `email` with EmailFormat(CanonicalEmailValidator). `formatAssertionsEnabled(true)` remains. The shared registry still validates requests and provider responses.
- Independently inspected actual networknt3.0.8 Dialect/Dialect.Builder bytecode: clone copies keywords, formats, vocabularies and specification version; format(Format) replaces the map entry by getName; the format keyword is rebuilt with that map. This is not an unused helper or a global format-disable change. Other format implementations and schema constraints remain in force.
- The helper inherits EmailValidator.isValid and isValidUser, retaining overall/local-part pattern, quoted local handling and64-character local-part limit. EmailFormat retains its non-ASCII-whitespace guard; canonical maxLength254 is unchanged.
- Ordinary domain validation uses IDN.toASCII with USE_STD3_ASCII_RULES, catches illegal conversion, checks nonempty ASCII length<=253, nonempty labels<=63, LDH characters, and no leading/trailing hyphen. It no longer requires live/registered TLD membership or performs DNS/delivery lookup. `.invalid` and other syntactically valid reserved/unregistered suffixes are no longer tied to a static registration table. Empty/doubled/trailing labels, invalid ASCII domain characters and illegal IDNA conversion remain rejected by source logic.
- Bracketed address literals delegate to the existing validator, retaining actual IPv4/IPv6 numeric-literal validation. The networknt package-private helper normalizes the leading `[IPv6:` before delegation; the repair reproduces this bounded normalization without broadening arbitrary bracket contents. Existing library behavior beyond this repair remains inherited, not newly certified as exhaustive RFC compliance.

No remaining concrete P1/P2 source finding was identified in this narrow repair. Runtime proof that request and provider response paths use the new dialect is still required on the separately labelled candidate.

## JDBC-JSONB-EXISTENCE — source repair accepted

Ten raw JSONB `?` existence operators in prepared Java SQL were replaced: four EvidenceAccess, four ReportingScope and two ListQueries timeline-lag occurrences. PostgreSQL `jsonb_exists(body,'key')` preserves top-level key-existence semantics, including a present key whose value is JSON null. The SQL substitutions add no bind marker or value parameter.

All actual positional placeholders, argument list insertion order, recursive ancestor paths/depth limits, branch matching, role/assignment checks and exclusive DataRequest per-record scope predicates remain unchanged. Source search found no remaining equivalent raw `body ?` Java expression. Historical migration SQL was not edited; its valid database-side JSONB operators do not require a JDBC workaround.

This closes the identified phantom-placeholder source cause. Other possible SQL/runtime/data-shape defects are not excluded by this static review; actual authorized File-list retest remains necessary. Source-identified reporting/timeline reach is not represented as a previously executed reporting failure.

## Build evidence and candidate identity

Developer reported exact-source clean/package SUCCESS30October2026 07:51:56+07,108 Java files, Java25.0.1/Maven3.10.0,24.593seconds. Test resources, test compilation and execution were explicitly skipped. Reviewer did not rerun Maven; independently read the packaged candidate SHA256 as `b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3`, matching the supplied evidence, and checked that current application source is unchanged from the reviewed commit. Installed JDK25.0.1 remains different from canonical25.0.4.1.

Candidate deployment/retest must preserve the old JAR and baseline FAIL records, record new source/JAR/contract identity, and revalidate isolated project/database/storage/process/ports before HTTP. No main8081 interruption, existing-volume destruction, PR merge or main/dev change is authorized by this review.

## Retained smoke checkpoint and next manual evidence

The retained21October run extends through s31, not merely s24. Its last database snapshot records Dossierv5 DRAFT, three linked CLEAN Files and three SUCCEEDED FILE_SCAN jobs, Policy v2 DRAFT after the ordinary product RUN_REGRESSION command,12 audit rows,9 idempotency rows,3 blobs and3 associations. There is no retained evidence of submitted Revision/Round/Contract/PDF/signing/readiness/effectivity. Session/CSRF, role/idempotency/ETag/cross-branch observations and scan/link successes are bounded to their actual recorded requests.

Retained metadata refers to PID22728 and ordinary Clock.systemUTC from21October; passive30October inspection did not observe that PID or listeners18443/8081/56433/13310. No Clock proposal files or executed offset phase evidence were present. Historical metadata is not live-process proof. Resume must use current real date and current DB state, reauthenticate, and record future business inputs before requests. Owner-approved Clock+1day/+2days configuration and orphan-maintenance delay remain a separate explicitly recorded verification configuration; never silently restore an advanced dataset under a backwards clock.

Authorized candidate retest should independently predeclare outcomes for original `.invalid` creation, caller-owned canonical seed GET, malformed email rejection/no effects, scoped File list and ordinary same-scope Dossier continuation. Broader reporting, load/fault injection, other business modules and automated tests remain outside this review/run permission. The remaining approved original-contract smoke sequence still needs actual runtime/business evidence. No full acceptance verdict is granted here.