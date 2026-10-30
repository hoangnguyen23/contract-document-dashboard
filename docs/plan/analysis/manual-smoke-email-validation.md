# Manual smoke email validation finding — analysis only

Date:21 October2026. No application/source/JAR changes, build, automated tests or HTTP requests were performed by this analyst. Baseline remains delivery ef33bae/app e718ccc, reviewed JAR fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602. This document reports a real observed failure and a proposed repair; it is not a repair or retest verdict.

## Observed failure and canonical expectation

The operator's predeclared case s07 POST /api/v1/relationships used a valid authenticated BANK_ADMIN, current CSRF, new UUIDv4 idempotency key and synthetic Contact.email smoke-a@example.invalid. Expected201 came from the canonical request schema. Actual422 VALIDATION_FAILED at /confirmedContact/email reported format mismatch, with the message “must be a valid RFC 5321 Mailbox”. Operator evidence: ignored var/verification/manual-smoke-20261021-a/evidence/s07-create-relationship.http.json and s07-after-relationship.database.txt. No Relationship/audit/idempotency/job effects were created. This FAIL must remain in the baseline report even when a separate documented example.com input succeeds.

Canonical contracts/openapi-v1.json Contact.properties.email is exactly type:string, format:email, maxLength:254. There is no business allowlist of registered domains or live delivery requirement. The frozen seed contains54 occurrences of @example.invalid, including the core confirmed Contact. Redacted source projections also use this domain. The initial source-wide application of this validator can therefore affect reads as well as writes.

JSON Schema2020-12§7.3.2 defines email by the RFC5321§4.1.2 Mailbox syntax; the syntax does not require membership in a maintained TLD table. Reserved invalid/test names are useful for safe synthetic data and must not be interpreted as an instruction to attempt delivery. See [JSON Schema email semantics](https://json-schema.org/draft/2020-12/json-schema-validation#section-7.3.2), [RFC5321 grammar](https://www.rfc-editor.org/rfc/rfc5321#section-4.1.2), and [RFC6761 reserved invalid domain](https://www.rfc-editor.org/rfc/rfc6761#section-6.4).

## Exact root cause from source and bundled dependency bytecode

- apps/api/src/main/java/vn/contractgovernance/api/shared/ContractRegistry.java29 creates one Draft2020-12 SchemaRegistry with formatAssertionsEnabled(true). Its validateRequest/validateSchema/validateResponse share that registry.
- apps/api/pom.xml pins com.networknt:json-schema-validator:3.0.8. Read-only javap inspection of that actual cached dependency (the application dependency version, no alternate validator execution) confirms the following chain.
- com.networknt.schema.format.EmailFormat() constructs IPv6AwareEmailValidator(true,true). EmailFormat.matches first rejects non-ASCII whitespace, then delegates to its EmailValidator. It exposes a public EmailFormat(EmailValidator) constructor.
- IPv6AwareEmailValidator only normalizes the IPv6: address-literal prefix before delegating.
- Shaded com.networknt.org.apache.commons.validator.routines.EmailValidator.isValid validates the overall email/local part then calls protected isValidDomain. Domain branches call DomainValidator.isValid and isValidTld; DomainValidator matches labels then checks bundled infrastructure/generic/country/local TLD arrays. This is an embedded registration-table constraint, not DNS verification or SMTP delivery.
- allowLocal/allowTld do not bypass membership for multi-label example.invalid: allowLocal addresses a single host label, and allowTld accepts a whole known TLD. Neither makes the final invalid suffix valid.

Severity: P1 after the separately authorized ordinary GET of the caller-owned seeded Relationship also returned503. Operator evidence s12-read-owned-canonical-seed-relationship.http.json confirms a provider read failure, in addition to request422. The same global validator rejects the frozen seeded Contact. This is actual smoke evidence, not only a predicted blast radius; no business write was caused by either failure.

## Proposed narrow repair

Keep the canonical JSON and formatAssertionsEnabled(true), every other format, closed-schema rules, maxLength and all domain authorization/state guards unchanged. Do not disable formats globally, special-case this request, seed a success result or replace .invalid fixture expectations.

Use the dependency's existing supported custom-format port:

1. Clone the supplied Draft2020-12 dialect with Dialect.builder(Dialects.getDraft202012()).
2. Replace only its format named email using new EmailFormat(a small EmailValidator subclass).
3. Build the same SchemaRegistry through SchemaRegistry.withDefaultDialect(customDialect, existingConfigConsumer). The public overload and Dialect.Builder.format(Format) were confirmed in3.0.8; format replaces one map entry by name.
4. Reuse the existing EmailFormat non-ASCII-whitespace guard and EmailValidator overall/local-part validation, including quoted local parts and the64-character local limit. Override only protected isValidDomain.
5. For IPv4/IPv6 address literals preserve the existing literal handling, including the IPv6: normalization; do not allow arbitrary bracket contents or call name resolution.
6. For ordinary domains perform syntax-only validation: IDN.toASCII with USE_STD3_ASCII_RULES, catch illegal conversion; enforce nonempty domain, total<=253ASCII bytes, nonempty labels<=63, ASCII letters/digits/hyphen only, no leading/trailing hyphen, no empty/trailing labels. No registered-TLD lookup, DNS, network or delivery check. Preserve the existing library's IDNA conversion behavior where compatible; do not broaden local-part behavior in this repair.

This is a bounded removal of an unintended domain-registration constraint, not a blanket email regex or new mail subsystem. It requires no new dependency, public field/route/enum/role or A/B/schema change. In a dedicated reviewed repair commit, both provider response and request validators must use the same repaired format.

An alternative instance-local DomainValidator TLD override for invalid/test/example would fix those fixtures only, but leave the same canonical-format defect for other syntactically valid unregistered suffixes. Global static TLD table mutation also affects other consumers and has initialization-order concerns. Neither is preferred over the syntax-only email-format replacement.

## Review and manual recheck requirements

Developer and independent reviewer must inspect actual custom-dialect construction and ensure the format keyword uses the replacement rather than retaining the old map. Check IPv6 literals, quoted local part, empty/doubled labels, leading/trailing hyphen, whitespace/control characters, local-part length, total schema length and other format validators. This is source-review scope, not authorization for an automated test suite.

Build only with maven.test.skip=true under existing policy. Preserve exact baseline JAR/evidence and create a separately identified reviewed repair JAR/source commit before any retest. Manual recheck should include original s07 .invalid case with fresh key, another reserved .test address, malformed-address rejection/no effects, and the observed seeded/redacted GET failure if confirmed. Use only the approved isolated smoke namespace or an explicitly identified isolated repair namespace; do not replace baseline evidence or touch the main8081 process/data.