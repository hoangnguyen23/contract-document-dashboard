# Manual smoke Clock proposal — design only

Status: **NOT APPROVED FOR EXECUTION / NOT RUN**. Owner21October2026 authorized proposing a Clock configuration, not activating it. This design uses read-only source/bytecode inspection. No alternate Clock was instantiated, no app launched and no HTTP/test executed for this proposal.

## Baseline and exact change requested

Retain delivery ef33bae18236c9d4d79b49772f0549c9f2e10517, application e718ccc5f7f6ba4d3881d924174440bedfbf7db1, canonical A/B1.3.0 and executable JAR SHA256 fa4c214eac62813bfeb62cc377f65fdd7e64708e9ffab4ff30f246b01f016602. ContractGovernanceApplication.java13 defines Clock.systemUTC(); no existing application Clock override property/profile exists.

Request approval for two **external Spring XML definitions**, two bean-selection properties, and one orphan-maintenance delay below, exclusively in project cg-smoke-20261021-a/database cg_smoke_20261021/own storage/HTTPS18443. No compiled class, baseline source/JAR, main.env, primary launcher, canonical contract, host/container/DB clock or8081 process is changed. No Java agent, custom Clock class, bypass endpoint, direct Policy bootstrap or historical timestamp update is proposed.

Prefer Clock.offset(systemUTC(),1day), then an orderly isolated-process restart with offset2days. Time still progresses normally within each phase, including session expiry, heartbeat, leases and retry backoff. This is synthetic application time, not a claim that real midnight was waited through. Phase2 observes normal late-worker effectivity, not an exact-midnight race.

## Static evidence for this exact bundled Spring version

Read-only javap inspection of Boot4.1.1 and Spring7.0.9 establishes:

1. SpringApplication.prepareContext applies ApplicationProperties.allowBeanDefinitionOverriding to DefaultListableBeanFactory before sources load. getAllSources creates an ordered LinkedHashSet with the primary app first and ApplicationProperties.sources afterward.
2. BeanDefinitionLoader accepts file resource strings and loads XML through XmlBeanDefinitionReader. All sources load before refresh; registering the primary configuration class does not yet instantiate its @Bean methods.
3. ConfigurationClassBeanDefinitionReader.isOverriddenByExistingDefinition subsequently sees the top-level XML clock definition. When overriding is allowed, that application-role definition wins and the @Bean method is skipped. Otherwise BeanDefinitionOverrideException is thrown. This avoids relying on a second @Primary Clock or replacing an instantiated singleton.
4. The XML has one top-level Clock, with anonymous inner JDK factory values; no imports, component scan, SpEL or custom code. Bundled META-INF/spring.schemas maps the exact HTTPS beans XSD below to its local classpath schema.

These observations are **not a startup proof**. After approval, unexpected binding/schema/override behavior must produce BLOCKED; do not silently edit the app or add another override mechanism.

The inspected Maven-cache bytes equal the corresponding libraries nested in the reviewed executable JAR:

| Library | SHA256 |
|---|---|
| spring-boot-4.1.1.jar | 0d92b532b1d4020640e72d78c31af175c23db8e0ddd45b81855b7fdf4c9c70f0 |
| spring-context-7.0.9.jar | 7552a2fcfa30cea53eb14d7a65a7a8e1b1dd82e832a7164fb7e6fb105f438858 |
| spring-beans-7.0.9.jar | ff218b827a25c9e8929b0cd56dfb56916cea9d5b669ed97dc7cb262508ff548b |

## Exact proposed artifacts and launch configuration

Materialize only after owner approval under the ignored run directory D:/contract-document-dashboard/var/verification/manual-smoke-20261021-a/proposals/. UTF8 without BOM, LF endings, exactly one terminal newline. Protect these files against edits between hashing and loading; they contain no secret.

clock-plus-1d.xml, SHA256 `0a95058f752ccca20df2bef54ee8cee8d0316446442b8956ad777b62d0a145e6`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">
  <bean id="clock" class="java.time.Clock" factory-method="offset">
    <constructor-arg index="0">
      <bean class="java.time.Clock" factory-method="systemUTC"/>
    </constructor-arg>
    <constructor-arg index="1">
      <bean class="java.time.Duration" factory-method="parse">
        <constructor-arg value="P1D"/>
      </bean>
    </constructor-arg>
  </bean>
</beans>
```

clock-plus-2d.xml is the **exact same bytes except the single literal P1D becomes P2D**, SHA256 `9f3057289cd06bd9f92a62da622b2c8cd73d0b64903ea6cea3bca5598be83420`. No other substitution is allowed. Neither XML has been loaded or executed.

Additional arguments for phase1:

```text
--spring.main.sources=file:/D:/contract-document-dashboard/var/verification/manual-smoke-20261021-a/proposals/clock-plus-1d.xml
--spring.main.allow-bean-definition-overriding=true
--cg.storage-maintenance.initial-delay-ms=604800000
```

Phase2 selects clock-plus-2d.xml; all other arguments remain the isolated launcher configuration. Hash/path allowlisting is essential because the Spring override switch is broader than Clock itself. Allow exactly the approved XML with one top-level bean id=clock, no wildcard/import. Never add these arguments to the primary environment, CI or production.

## Why the maintenance delay is included

BlobMaintenance.java50 compares injected Clock.now-minus24hours with actual filesystem lastModifiedTime. Advancing only application time would artificially age unreferenced staging files. Reference/hash/guard protection remains, but unrelated orphan deletion should not become part of scope1 accidentally.

The existing supported cg.storage-maintenance.initial-delay-ms is therefore set to604800000:7 real days after each isolated startup. Finish or stop this exact smoke process well before7days. Do not disable scheduling globally: FILE_SCAN, ARTIFACT_PDF, Policy scheduling and EFFECTIVITY must remain real. No filesystem timestamp is rewritten; no other namespace is affected. Orphan/disposition cleanup is NOT RUN under this proposal.

## Predeclared manual phases and expected outcomes

1. **Ordinary Clock.** Continue authorized session/CSRF/ETag/idempotency and synthetic Party/Relationship/Dossier/upload/scan checks. Author and independently publish the first Policy through ordinary manual product HTTP requests, no business-state SQL seed. Its earliest effectiveFrom is next local day. RUN_REGRESSION is the product prerequisite command, not an external automated test harness or Policy acceptance claim. Record requested policy effective date and expected due transition before any Clock change.
2. **Offset+1day.** First wait for all current scan/artifact jobs to finish normally, with no RUNNING lease or intentionally carried transient failure. Snapshot counts/state/file hashes. Verify and stop only the smoke PID by executable, JAR/path/hash, start time, run identity and18443 ownership. Retain its database/storage. Restart exact JAR with XML1day and the delay above. Record real UTC and expected application instant interval realUTC+24hours; reject backwards time. The real Policy worker, not SQL, must activate the due Policy. Then perform Dossier submission, immutable Revision, L1/L2, PREPARED/PDF/scan/download and independent signing/readiness. Choose and record startOn=verified application local date+1day and a safely later endOn before requests. Expect SCHEDULED/READY, serviceNOT_STARTED and no initial ContractVersion before start.
3. **Offset+2days.** Wait for scan/PDF work to finish; snapshot proves only the intended future EFFECTIVITY job is queued and no RUNNING jobs exist. Stop/restart only the smoke process with XML2days. Verify realUTC+48hours is at/after the recorded startOn but before endOn. Let the ordinary worker act without force-effective endpoint or DB timestamp edit. Expect one initial ContractVersion with effectiveFrom=startOn, ContractACTIVE and DossierCOMPLETED; service remainsNOT_STARTED. Record earlier effectiveAt versus later recordedAt honestly and inspect no duplicate version/intent.

Dates follow recorded phase time and preapproved offsets, not actual success output. If elapsed wall time makes any proposed interval unsuitable, stop and review request inputs before submitting. Never run a synthetic dataset under a backwards application clock.

## Positive proof and limits

Record unchanged JAR/contract hashes, XML hashes, exact arguments, smoke PID/18443 binding, real host UTC and read-only PostgreSQL clock_timestamp() before/after. Host/DB clocks remain real. A temporary ConfigurationClassBeanDefinitionReader DEBUG logger may record the top-level clock override; no new actuator/Clock endpoint is added.

After each restart obtain a fresh anonymous session and login. Compare server expiresAt to application-now+30minutes, and isolated cg_identity_session created_at/last_seen_at/absolute_expires_at to application-now/+8hours without exposing cookies, CSRF or token hashes. EntityStore/JobQueue server timestamps provide additional Clock consumers. HTTP Date/TLS validity use real wall time and are not business-Clock proof.

Discard old cookie files and reauthenticate independent actors after each jump; old8hour sessions should naturally expire. Do not relax expiry/auth/CSRF. Idempotency24hour expiry may also occur, so perform same-window replay checks in the ordinary phase with fresh keys. Offset Clock keeps time progressing, unlike fixed Clock, but crash/lease faults, exact timing races, wall-time scheduling, NFR and production readiness remain outside this evidence.

## Alternatives, finish and approval gate

- Real-time waiting is the unmodified-config alternative: first Policy scheduled21Oct may activate22Oct; submission22Oct can start no earlier23Oct. Until then, dependent outcomes are BLOCKED/NOT RUN.
- Clock.fixed XML is technically possible but not recommended: frozen time stalls retry available_at/backoff and lease expiry and can make a transient normal failure unrecoverable without another change. It is not included in the requested approval.
- No Java bootstrap class, agent, privileged time change, libfaketime or database chronology rewrite is proposed.

Retain the dedicated project/volumes/synthetic data/evidence; no implicit deletion/reset cleanup. Deactivation means omitting the XML flags for a fresh run, or waiting until real time catches up before resuming this dataset. Do not demonstrate deactivation by restarting this advanced dataset backwards.

Independent reviewer must inspect exact XML bytes, bundled-bytecode reasoning, flags, maintenance exception, isolation allowlist and expected outcomes. Root then requests owner approval to execute this concrete proposal. Until that approval, only the previously authorized ordinary-Clock partial smoke may run.

## Independent design review —21October2026

Separate reviewer confirmed static design GO only: independently recomputed both XML hashes, parsed the single top-level Clock bean, checked bundled Spring bytecode/library hashes and local XSD mapping, and confirmed the specific BlobMaintenance real-filesystem/time interaction. This is not a runtime proof or owner execution approval. Exact isolated process identity, XML allowlist, no-running-job gates, monotonic offsets and no backward restart remain mandatory. Owner execution decision is pending.
