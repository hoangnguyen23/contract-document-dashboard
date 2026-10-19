# Termination boundary 1.3.0 — independent canonical compatibility review

Verdict: internal shared review GO for candidate `96c198ec05ccedcc82a05739055ab25a09bd5da1`, branch task/termination-contract-boundary, compared with d96db6d. Owner approval covers the current-version lower date boundary and its AC-045/catalog propagation. This review does not claim external Frontend acknowledgement, runtime acceptance or application implementation.

## Behavioral scope and temporal authority

A10.1/B6 require TERMINATE CONFIRM current ContractVersion.effectiveFrom <= effectOn <= today. An earlier effectOn returns existing409 STATE_CONFLICT as ordinary prevalidation, preserving the rejected request/source and existing materialized intervals/APPLIED history. No date clamping, automatic source rewrite, successful confirmation or success cache is permitted; corrected source follows cancel/new review. BR-045 and AC-045 describe the same conditional behavior. AC-045's normalized acceptanceOracle entry propagates that exact approved guard, rather than retaining an unconditional success assertion.

The no-business-effects statement applies to the rejected CONFIRM operation. Existing A10.1 explicitly requires independently committed authoritative timeline maintenance before the guard; that maintenance can materialize a qualifying Change/true expiry before a subsequent confirmation is rejected. The new guard does not authorize skipping, rolling back or disabling that maintenance to manufacture global zero effects. The boundary is evaluated against the resulting actual current materialized version. This is consistent with existing timeline precedence, not an additional rule or a waiver of it.

## BE wire and FE consumer compatibility

The existing commandControlRequest response catalog already includes409; STATE_CONFLICT exists. No operation/path/request/response schema, field, required/nullability rule, enum, role or permission changes. The source effectOn remains an existing immutable request fact; confirmation receives the existing command shape. getControlRequest/listControlRequests action hints use existing allowedActions/blockedActions and STATE_CONFLICT.

This is an owner-approved behavioral/date-semantics change, recorded as minor A/B1.3.0 under B9. Frontend must retain the source date, display the existing conflict/blocker, and allow cancel/new review; it must not silently clamp or retry a corrected date against the old source. It is not a breaking wire/schema change, and no Frontend source was modified. Actual FE acknowledgement/generation/consumer execution remains pending and is not replaced by this internal compatibility review.

## Independent static evidence

Parsed all six embedded blocks with duplicate-key rejection and compared them byte-for-byte with canonical sorted UTF-8 extracted files and runtime resources. A SHA-256 is `0dc6885d1b7faf65c6d5adf49389baa37474a2036735ed73e5710c47057e43a1`, matching the manifest. All five manifest block digests match. Both canonical documents decode as UTF-8 without BOM and retain CRLF; CRLF-aware git diff --check passed.

Recursive semantic diff against d96db6d found only:

- openapi-v1: info.version and version-description metadata.
- seed-v1: version metadata.
- scenarios-v1: version and seedVersion metadata only; all63 concrete scenarios unchanged, including S024/S060 allowedActions=[].
- protocol-rules-v1: version metadata only.
- business-catalog-v1: version and acceptanceOracle[44].then (AC-045) exact conditional propagation only.
- contract-manifest-v1: A/B versions, A file digest and regenerated block digests only.

No application, migration or FE source changes. Source documentation changes stay within approved A10/B6/BR045/AC045, version metadata, index and proposal status. No unapproved endpoint, role or alternative success behavior was introduced.

## Limits

No build, automated tests, HTTP/smoke scenarios, manual PDF, business-data mutation, Docker or migration execution occurred in this review. Control implementation, exact runtime409/no-success-cache behavior, concurrent timeline/confirmation effects, action hints and FE integration require their separate source/runtime evidence. This document is a review record only; root may update owner/internal-review status references after this commit and integrate the reviewed canonical candidate before the final Control build.
