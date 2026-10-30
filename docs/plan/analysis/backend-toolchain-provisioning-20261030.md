# TC01 developer provisioning evidence — 30 October 2026

Downloaded exact official Windows archive `https://download.oracle.com/java/25/archive/jdk-25.0.4.1_windows-x64_bin.zip` and separately retrieved its `.sha256`. Published and actual archive SHA256 agree: `0e9726dc51724f262a32c796705abfe49e41574c0b134827bc72d6cef7a36ff4`.

Archive retained under ignored `D:/contract-document-dashboard/var/toolchains`. Before extraction, all519 entries were checked for absolute paths, backslashes/drive syntax, parent traversal, escaped resolved targets and symlinks. A pre-existing target would have stopped provisioning. Extracted into new task-owned `oracle-jdk-25.0.4.1/jdk-25.0.4.1`; no global installation, PATH/JAVA_HOME change or process replacement.

Explicit executable invocation reports:

```text
java version "25.0.4.1" 2026-08-18 LTS
Java(TM) SE Runtime Environment (build 25.0.4.1+1-LTS-5)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.4.1+1-LTS-5, mixed mode, sharing)
```

`bin/java.exe` SHA256: `65c411a0be023cbe3e96d1f165d4652593b6c62c189eb742efd6fd097e42cf71`. This records Windows x64 downloaded vendor/build identity; no application build has yet used this toolchain.

CI config now downloads exact Oracle Linux x64 archive from `https://download.oracle.com/java/25/archive/jdk-25.0.4.1_linux-x64_bin.tar.gz`, verifies pinned official SHA256 `dd7e7f51d23bcffe85f1a439577ce1c0190a33417b34afdb338f9b3897bdeaec` from independent operations-review retrieval, extracts with Python data filter under RUNNER_TEMP and exports only job environment JAVA_HOME/PATH. The former floating Temurin setup-java step is removed; existing checkout/setup-python action pins, canonical extraction and skipped-test package remain. No replacement caching action/dependency is introduced.

No actual Linux CI execution or Linux archive extraction occurred locally. CI vendor/build log is configured but not yet observed. Source/config independent review pending. No tests, HTTP business calls, build, baseline JAR overwrite, restart8081, database/storage mutation, migration, commit/push or deployment performed. Exact isolated JDK provisioning addresses artifact availability, not runtime acceptance.
