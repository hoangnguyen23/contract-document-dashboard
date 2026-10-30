# Developer evidence: canonical launcher JDK selection

Scope: `scripts/start-backend.ps1` and API setup README, based on A§13/B§2 exact Oracle25.0.4.1 pin and verified provisioned vendor/build evidence. No canonical, application Java, schema, migration, FE or dependency change.

Added optional absolute `-JavaHome`. Without it, existing PATH discovery remains, but the actual Java properties must identify Oracle Corporation25.0.4.1 and the verified distribution build25.0.4.1+1-LTS-5. Probe resolves the real JDK path and requires java.exe/javac.exe. Explicit path must equal probe java.home, preventing silent redirection. The parameter is not overwritten by a case-insensitive local variable: resolvedJavaHome is separate.

Probe/validation now occurs before managed-process reuse. Existing metadata javaPath must equal selected executable before reuse; a mismatch rejects with the existing stop/start instruction. No existing process is stopped or replaced by mismatch failure. PID/path/start-time ownership guards and new-process cleanup remain unchanged.

JAVA_HOME/PATH are scoped to the launcher process before Maven, use the selected resolved JDK, and their original values are restored by existing finally on success/failure. System/user environment is never changed. The API still launches the selected executable explicitly. Database/storage/Compose/secrets/CSRF/session behavior is untouched.

README shows the verified side-by-side local toolchain and explains mismatch behavior. Another machine can provision the same exact vendor/version/build at its own absolute path.

Verification performed: PowerShell AST ParseFile on the edited script reported no syntax errors; Git whitespace check passed. These are static syntax/diff checks, not test execution. No launcher, Java probe, Maven build, test, HTTP request, Docker/process operation, data change, commit or push was run. Independent source review is required; Windows runtime behavior remains unexecuted under current owner testing policy.
