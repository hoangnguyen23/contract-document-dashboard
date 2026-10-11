# Identity boundary

Owns `/session`, login/logout, `/users` and `/branches` (11 canonical operations).
Sessions use a random opaque 256-bit cookie; only its SHA-256 digest persists. Anonymous bootstrap is required before login. Every unsafe API request verifies its session CSRF token. Successful login rotates both cookie and CSRF; logout expires the Secure, HttpOnly, SameSite=Lax `__Host-cg-session` cookie. PostgreSQL enforces 30-minute idle and 8-hour absolute lifetimes. Scheduled cleanup removes expired tokens.

`UserDirectory.actor` resolves active users, current roles, supplemental permissions and active explicit branch scopes on every request. User changes increment credential revocation version and delete affected sessions in the same command transaction. BANK_ADMIN grants metadata administration; it never grants all business branches. Passwords use versioned PBKDF2-HMAC-SHA256, random salts and 600,000 iterations. Credential fields never appear in business JSON, audit or idempotency responses.

Login rate counting uses atomic PostgreSQL username+servlet-IP buckets in a separate committed transaction, including unsuccessful attempts. Hashing runs outside the session transaction. Forwarded headers remain disabled until an explicit trusted proxy setup exists. Limit is ten attempts per 60-second bucket; the eleventh returns canonical RATE_LIMITED with Retry-After: 60.

Only `SPRING_PROFILES_ACTIVE=demo` enables `DemoIdentitySeed`; `CG_DEMO_PASSWORD` of 12–128 characters is mandatory. There is no application password default and no public seed/reset/clock endpoint. The seed imports synthetic branches, actors, one Party and its Relationship only, on an empty identity database. It does not load fixture workflow states. Production identity provisioning remains an explicit deployment responsibility; the application does not silently create an administrator.

Mutations use shared contract validation, ETag checks, actor/key idempotency and transaction audit. List responses use signed keyset cursors. Static compilation/package succeeded with Maven test compilation and execution skipped at the user's request; no runtime, security or integration acceptance is claimed.
