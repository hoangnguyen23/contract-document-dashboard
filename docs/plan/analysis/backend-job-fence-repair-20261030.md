# JOB-FENCE-01 developer evidence — 30 October 2026

Independent invariants reviewer confirmed a claim-token reuse defect: a fixed worker UUID could be reused when a lease expires and the same worker process claims the job again. Old Job handles could then match the newly persisted lease_owner, making owner equality insufficient for fencing stale work.

Implementation changes only JobQueue.java. claim/claimForKind method signatures retain workerId for caller compatibility. The select row mapper creates a fresh UUID for the single selected claim; an empty select creates none. The returned Job carries that token, and the subsequent RUNNING update persists exactly job.leaseOwner(). No second token is generated. Query selection, transaction/row locks, attempts, timestamps, state recovery, available_at, retry/backoff and wire/schema remain unchanged.

All existing renew/finish/fail predicates use Job.leaseOwner and therefore consume the per-claim token without worker changes. FileScanWorker can retain its fixed workerId argument; it is no longer the fencing identity. PrivacyJobs.lease compares id/state/lease_owner/attempts/lease_until against the returned Job; the new token is coherent with this predicate and does not require a Privacy source change.

This is developer implementation evidence, not independent review or executed correctness proof. No tests, build, HTTP, runtime, data changes, migrations, commits or pushes performed for this repair. Independent source review must follow; runtime stale-claim recovery verification remains in the separately authorized testing stage. Earlier candidate81c3d7f/JARb29274dc remains the prior baseline, not this unbuilt source change.
