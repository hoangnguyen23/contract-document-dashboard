# Private evidence and scan jobs

Implemented HTTP operations: `listFiles`, `uploadFile`, `getFile`, `deleteUnattachedFile`, `commandFile` (RETRY), `downloadFile`, `listJobs`, `getJob`, `commandJob` (FILE_SCAN retries). Other job kinds retain explicit unavailable retry behavior until their real workers exist.

Multipart upload validates the exact canonical closed `FileUpload` shape, rejects unknown/repeated parts, derives MIME from bytes, enforces 10 MiB per file and 20 files/100 MiB per parent staging budget. Upload requires an existing authorized parent. A private opaque blob key and server SHA-256 are finalized before the transaction creates metadata, audit and a durable FILE_SCAN intent. There are no public storage URLs. `CG_STORAGE_ROOT`, `CG_SCANNER_HOST` and `CG_SCANNER_PORT` configure the actual adapters.

ClamAV uses the real clamd VERSION/INSTREAM socket protocol with bounded connection/read/whole-command deadlines, two scan slots and 20-second lease renewal. External scan work runs outside the domain transaction. Completion locks parent then file, rechecks current authorization/state/source version/hash, verifies lease ownership and updates file+audit+job atomically. Outages remain quarantined during at most five bounded backoff attempts and finish SCAN_FAILED; fifth expired crash leases are reconciled to SCAN_FAILED. No outage or synthetic fixture marks a live file CLEAN. Human verification remains NOT_CHECKED after scanning.

Private metadata/content/job authorization inherits the actual parent. Sensitive full downloads require SENSITIVE_READ; auditor compound roles do not bypass actual assignment. REDACTED requires an independently verified private replacement record and returns READINESS_BLOCKED when absent, with no original fallback. Returned hash and length describe the returned bytes. Redaction/disposition production APIs are future G5 work, so this module does not fabricate replacement files.

PUT draft references require CLEAN same-parent/same-branch files and preserve historical associations. Own unattached uploads can be deleted; internal deletion authorization tombstones preserve authenticated/scoped cached 204 replay. Bytes are removed only after commit and only when no other file references the private key. A failed physical cleanup safely leaves an unreferenced blob for later maintenance, rather than corrupting referenced history. Scheduled orphan reconciliation and complete coherent DB/blob backup/restore remain explicit operational follow-up work.

Job wire snapshots/version/timestamps are maintained by SQL triggers from the durable queue. Retry checks current ownership/scope before cache replay and new-effect state/source guards afterwards. Semantic trigger version changes exclude lease heartbeat updates.

No tests or HTTP acceptance calls were run. The module was compiled and packaged with `mvnw.cmd '-Dmaven.test.skip=true' package`; runtime/scanner/browser/recovery acceptance remains pending.
