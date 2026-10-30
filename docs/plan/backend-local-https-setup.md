# Optional local Backend HTTPS preparation

Status: preparation only, no certificate creation, configuration activation or browser/API scenario executed. Owner separates testing from development. Do not launch this recipe onto8081 or point it at existing data. FE owns its proxy implementation; this document supplies the BE connection contract without editing FE.

## Private certificate preparation

Choose a new task-owned ignored directory such as `D:/contract-document-dashboard/var/integration-tls/<runId>`. Confirm it is new and the keystore target does not exist before any command. Generate a strong private password outside command history and supply process environment `CG_TLS_KEYSTORE_PASSWORD`; do not commit it, place it on a command line or print it. The following example is for a separately authorized future operator action, not an instruction already executed:

```powershell
$jdkBin = 'D:/contract-document-dashboard/var/toolchains/oracle-jdk-25.0.4.1/jdk-25.0.4.1/bin'
$tlsDirectory = 'D:/contract-document-dashboard/var/integration-tls/<newRunId>'
# Create/secure only the verified new task directory; never overwrite an existing keystore.
& "$jdkBin/keytool.exe" -genkeypair -alias cg-local -keyalg RSA -keysize 3072 `
  -validity 30 -dname 'CN=localhost,OU=Local Synthetic Development' `
  -ext 'SAN=dns:localhost,ip:127.0.0.1' -storetype PKCS12 `
  -keystore "$tlsDirectory/cg-local.p12" `
  -storepass:env CG_TLS_KEYSTORE_PASSWORD -keypass:env CG_TLS_KEYSTORE_PASSWORD
& "$jdkBin/keytool.exe" -exportcert -rfc -alias cg-local `
  -keystore "$tlsDirectory/cg-local.p12" -storepass:env CG_TLS_KEYSTORE_PASSWORD `
  -file "$tlsDirectory/cg-local.crt"
```

Restrict directory/keystore ACLs to the operator/application identity. The self-signed certificate is for local synthetic integration, not production identity. Inspect certificate identity/SAN/expiry/fingerprint before explicitly trusting this certificate in the intended client. Do not modify global trust stores automatically, use trust-all callbacks, browser ignore-certificate switches or curl insecure flags. Public certificate may be supplied to a scoped client trust configuration; private key/password stay private.

## Backend connection configuration

`infra/backend-local-tls.properties` is an explicit optional Spring additional-location file. It has no automatic profile activation. Supply `CG_TLS_KEYSTORE` as an absolute Spring `file:` URI, the private keystore password, a free loopback `CG_TLS_PORT` (example18443) and distinct isolated `CG_DATABASE_URL`, `CG_DATABASE_USER`, `CG_DATABASE_PASSWORD`, `CG_CURSOR_SECRET`, `CG_STORAGE_ROOT`, `CG_SCANNER_HOST` and `CG_SCANNER_PORT`. Required placeholders have no development-secret or live-data defaults. Confirm actual ports, process, DB, storage and scanner ownership first.

The external TLS configuration forces loopback and leaves forwarded headers ignored. Canonical Secure/HttpOnly/SameSite cookies and CSRF remain active. No CORS permission is added. Application workers still execute normally against the selected environment; TLS does not authorize a business workflow or disable job effects.

For a future separately authorized isolated launch, use the exact verified Java executable/JAR and an absolute additional-location URI:

```powershell
# Set private process environment deliberately before launch; do not echo secret values.
& "$jdkBin/java.exe" --enable-native-access=ALL-UNNAMED -Duser.timezone=UTC `
  -jar '<absolute-reviewed-candidate.jar>' `
  '--spring.config.additional-location=file:/D:/<release-checkout>/infra/backend-local-tls.properties'
```

This direct invocation intentionally bypasses the Windows guarded launcher. It must have its own process identity/log/ownership manifest and must not overwrite `var/local-runtime` metadata or be stopped through the main launcher. It is a template, not a production service manager. The main8081 process/package remains separate. Before activation, record source/JAR/contract hashes, actual Java build, all private target paths and process ownership. No process was launched by preparing these files.

## Frontend boundary

Preferred local browser origin remains FE-owned `https://localhost:5173`. Its same-origin `/api/v1` proxy targets the selected Backend TLS origin, trusts the explicit local certificate, and preserves canonical headers/cookies. FE review must confirm backend-certificate validation, CSRF/session behavior and target port; do not set TLS verification false to make the proxy connect. A direct browser call from5173 to18443 is cross-origin and is not a replacement for that proxy. A browser/provider/consumer result requires later authorized execution and counterpart acknowledgement.
