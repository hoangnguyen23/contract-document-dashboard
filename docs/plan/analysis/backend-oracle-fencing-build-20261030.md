# Exact Oracle fencing candidate build — 30 October 2026

Application fencing commit `d306166`; cumulative source/build configuration commit `2487adb9ea26b1020c9a448ded56be77c2e9e87d`. Independent fencing source GO and tooling GO were received before execution. Source/config paths were unchanged from that commit before/after build; concurrent documentation changes are not compiled application changes.

Executed once from `D:/contract-document-dashboard-worktrees/be-smoke-fixes/apps/api`:

```powershell
.\mvnw.cmd -B -Dmaven.test.skip=true clean package
```

Before Maven clean, resolved working directory and target were checked against the exact expected worktree paths; target was a normal directory, not a reparse point. Clean operated only on this worktree target. JAVA_HOME/PATH were scoped to the single PowerShell build process, selecting ignored isolated Oracle toolchain; no system environment was modified.

- Oracle Corporation Java25.0.4.1, Runtime/HotSpot build25.0.4.1+1-LTS-5, Windows11 amd64.
- Apache Maven3.10.0, revision `c43a36b8d67be7e0805a411bc0898af1a51f5472`.
- Compiled108 Java sources, release25.
- BUILD SUCCESS, exit0, finished2026-10-30T08:25:08+07:00, Maven reported21.421seconds.
- Test resources: `Not copying test resources`; test compile: `Not compiling test sources`; Surefire: `Tests are skipped.` No test phases executed application tests.
- Candidate JAR `apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar` SHA256 `99e695f8a28f998eafe9264ea9c5883e2317f1ac2ae4593dc9caa6bc1c0f74ed`.
- Full retained log: `D:/contract-document-dashboard/var/delivery-builds/20261030-oracle-fencing/package.log`, SHA256 `2248ca4ab783d61c15be1a6eca8a7049ebbe8e2c71535db4405e59a0a95e7e50`. Same folder contains actual java-version.log and maven-version.log.

Observed warnings: optional Maven prefix metadata retrieval to Flyway GitHub/Redgate feeds returned401; build still succeeded. JsonSupport deprecated API warning remains. Neither is represented as a test result or hidden dependency resolution failure. Maven used its existing wrapper cache; this build does not prove fresh wrapper bootstrap checksum enforcement or actual Linux CI execution.

Main checkout packaged JAR remains SHA256 `b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3`, freshly hash-read after this build. No candidate deployment, main JAR replacement, runtime/process restart, HTTP smoke, business scenario, database/storage mutation, migration, commit/push or test run occurred. The running application was not inspected or claimed to use the new build. Lease race/recovery and original failure retests remain pending the separate testing stage.
