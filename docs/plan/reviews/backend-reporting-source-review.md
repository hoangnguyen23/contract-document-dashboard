# Independent dashboard and audit reporting source review

Review date: 18 October 2026, Asia/Ho_Chi_Minh. Reviewed source: `1e721aee3c1e4686a0095521a4cc6ec4e0b0ffbf`; final merged canonical-resource provenance: `c5d53a77237471927a1d5b8e12e725bd0441d0c8`. Reviewer did not implement or repair application code. Authority: canonical A/B1.2.1, A section12.2, B sections3-7 and closed Dashboard/AuditEvent/page schemas and three operation definitions.

## Verdict and boundary

GO for the bounded source implementation of `getDashboard`, `listAuditEvents`, `exportAuditCsv` and the scoped lag snapshot-instant overload. No unresolved concrete P1/P2 finding remains in this diff. This is source/build review, not executed SQL, HTTP, metric/export correctness, performance, security scenario or integrated Backend acceptance. Required future G4/G5 composition and drilldowns remain subject to review as their writers/handlers are delivered.

The prior BASELINE-LAG-SCOPE P2 was independently reviewed closed at `cb878681f6e4bf25ff1a1d9e39fb552fc44d10f8`: the filtered collection and real parent ancestry restrict effectivity lag by current branch and Relationship assignment/business role. File/Job pages retain scoped lag; actual administration/policy pages expose no business lag; version pages select their own Contract. The later four-argument port preserves those predicates and accepts the reporting transaction's one asOf instant. No global business lag fallback remains.

## Source observations

Dashboard is a read-only REPEATABLE_READ transaction with one asOf and canonical branch-only query. It always returns seven dossier-state entries. Review overdue requires an OPEN current Round bound by its current target roundId and IN_REVIEW state, counting distinct target identity. Recorded ACTIVE is independent of serviceState. Expiring30 uses currentVersion terms and local exclusive-end date arithmetic. Overdue duties use currentDueAt and only nonterminal states; historical breaches use actual immutable breach records recorded by asOf and remain counted after closure/cancellation. Closeout, notice failure and pending control predicates follow the finite canonical states. Policy/admin-only roles do not gain business dashboard access.

Audit maps stored CamelCase resource types to canonical UPPER_SNAKE without rewriting immutable rows. Query values are bound; SQL identifiers/ancestry expressions are server owned. Role/branch/current Relationship scope is applied before paging and export. Source-specific ancestry covers Round/SigningPacket/File/Job and contract/relationship parents. Deleted staging files require the retained deletion tombstone plus real currently authorized parent. Global Policy, administrative User/Branch, Party relationships and export provenance have explicit separate predicates. There is no generic branch-null or orphan-resource grant. User-authored reasons are redacted in JSON and CSV, regardless of supplemental sensitive grant.

Audit cursor is HMAC signed and bound to operation, order, actor, current roles/permissions/branches and query; it retains first-page anchor, last position and original15-minute expiry. Current object/assignment scope remains in every page query. Filter timestamps use occurred_at with inclusive createdFrom/exclusive createdTo; sorting is occurred_at DESC,id DESC. Counts do not claim a frozen result across page requests.

CSV uses exactly the canonical nine columns, UTF-8 BOM, CRLF, RFC4180 quoting and an apostrophe for formula prefixes after whitespace. Export requires supplemental EXPORT and ordinary scope; selects at most1001rows and rejects above1000 before recording success. Selection and successful scoped export audit provenance share a non-read-only REPEATABLE_READ transaction. Read-only list/dashboard paths create no audit/domain writes. Controller preserves attachment/nosniff behavior; shared interceptors retain the canonical parameter allowlist and no-store/trace behavior.

## Independent final build

One independent final clean/package in the isolated reporting checkout after the A/B1.2.1 metadata merge:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' -V clean package
```

BUILD SUCCESS completed `2026-10-18T13:21:04+07:00`, Maven3.10.0, OracleJDK25.0.1, Windows11 amd64, javac release25,78Java sources. Test resources were not copied, test sources were not compiled and Surefire reported Tests are skipped. Existing optional Flyway-feed HTTP401 metadata warnings and the deprecated-API notice were nonblocking. The installed JDK is not certification against the specification's exact25.0.4.1 patch.

Packaged JAR SHA-256: `9bdd1328a805849eab35f713ced61c83eaad125f5f46f96cafb898359d7cb30a`.

## Unperformed verification and composition obligations

No automated application tests, HTTP requests, SQL execution, schema/scenario harness execution, runtime startup, migration execution, Docker operation, actual export, KPI drilldown, concurrency/recovery/NFR or Frontend integration occurred. No live data or existing runtime was changed. This tranche introduces no migration, public schema, request, enum or permission change and edits no Frontend code. Owner-approved S060 metadata is reviewed separately; separate external FE developer acknowledgement is not asserted.

Later obligation writers must preserve the authoritative breach table/history, and public obligation filters must use currentDueAt for strict dueBefore. Later Change/control/notice writers must preserve canonical parent bindings/current-round states and currentVersion terms used here. Their actual end-to-end metrics/drilldowns and source composition need independent review; this bounded GO does not claim those absent workflows are complete.
