package vn.contractgovernance.api.change;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.*;
import vn.contractgovernance.api.obligation.LifecycleObligations;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.signing.SigningEvidence;
import static vn.contractgovernance.api.change.ChangeService.id;

/** Single-contract-lock application, called by ContractLifecycle before current-term expiry. */
@Service
public class ChangeTimeline {
    private final EntityStore store;
    private final ChangeService changes;
    private final ChangeApproval approval;
    private final ChangeSigning signing;
    private final ContractLifecycle lifecycle;
    private final SigningEvidence evidence;
    private final LifecycleObligations obligations;
    private final JobQueue jobs;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final ContractRegistry schemas;
    private final AuditService audit;
    private final TransactionTemplate tx;
    public ChangeTimeline(EntityStore store,ChangeService changes,ChangeApproval approval,ChangeSigning signing,
            ContractLifecycle lifecycle,SigningEvidence evidence,LifecycleObligations obligations,JobQueue jobs,
            JsonSupport json,Clock clock,CalendarService calendar,ContractRegistry schemas,AuditService audit,
            PlatformTransactionManager manager) {
        this.store=store; this.changes=changes; this.approval=approval; this.signing=signing; this.lifecycle=lifecycle;
        this.evidence=evidence; this.obligations=obligations; this.jobs=jobs; this.json=json; this.clock=clock;
        this.calendar=calendar; this.schemas=schemas; this.audit=audit; this.tx=new TransactionTemplate(manager);
    }
    public EntityStore.Entity advanceLocked(EntityStore.Entity contract) {
        if(!contract.body().path("state").asString().equals("ACTIVE")||!contract.body().hasNonNull("scheduledChangeId")) return contract;
        var change=store.lock("Change",id(contract.body(),"scheduledChangeId"));
        var boundary=calendar.startOfDay(LocalDate.parse(change.body().path("effectiveOn").asString()));
        if(clock.instant().isBefore(boundary)) return contract;
        String failure=null;
        try {
            if(!change.body().path("state").asString().equals("SCHEDULED")||!change.body().path("contractId").asString().equals(contract.id().toString())) throw ApiException.of("SCHEDULE_CONFLICT","Slot nguồn không khớp.");
            if(!contract.body().path("currentVersion").path("id").equals(change.body().path("baseContractVersionId"))) throw ApiException.of("BASE_CONFLICT","Base version đã đổi.");
            if(!ChangeService.counter(contract.body().path("scheduleVersion"),change.body().path("committedScheduleVersion"))) throw ApiException.of("SCHEDULE_CONFLICT","Committed schedule không khớp.");
            changes.requireCurrentGuard(contract); evidence.requireMaterial(contract); signing.requireReady(change,null);
        } catch(ApiException guard) {
            if(Set.of("STORAGE_INTEGRITY","SERVICE_UNAVAILABLE").contains(guard.code())) throw guard;
            failure=guard.code();
        }
        if(failure!=null) {
            var body=(ObjectNode)change.body().deepCopy(); body.put("state","CONFLICTED").put("conflictReason",failure); changes.save(change,body,null,"APPLY_CONFLICT",failure);
            var cb=(ObjectNode)contract.body().deepCopy(); cb.putNull("scheduledChangeId").put("scheduleVersion",contract.body().path("scheduleVersion").asLong()+1); return lifecycle.save(contract,cb,null,"RELEASE_CONFLICTED_SLOT",failure);
        }
        var frozen=approval.frozen(change); var previous=contract.body().path("currentVersion");
        var version=json.object().put("id",UUID.randomUUID().toString()).put("number",previous.path("number").asInt()+1)
                .put("sourceKind",change.body().path("target").path("kind").asString()).put("sourceId",change.id().toString())
                .put("effectiveFrom",change.body().path("effectiveOn").asString()).put("artifactId",change.body().path("artifactId").asString())
                .put("artifactHash",change.body().path("artifactHash").asString()).put("createdAt",changes.now());
        version.putNull("effectiveTo"); version.set("terms",frozen.revision().path("consolidatedTerms")); schemas.validateSchema("ContractVersion",version);
        store.jdbc().update("insert into cg_contract_version_closure(version_id,effective_to,recorded_at) values(?,?::date,?)",id(previous,"id"),change.body().path("effectiveOn").asString(),Timestamp.from(clock.instant()));
        store.jdbc().update("insert into cg_contract_version(id,contract_id,number,body) values(?,?,?,?::jsonb)",id(version,"id"),contract.id(),version.path("number").asInt(),json.stringify(version));
        store.jdbc().update("insert into cg_change_effect(change_id,revision_id,version_id,effective_at,recorded_at) values(?,?,?,?,?)",change.id(),id(frozen.revision(),"id"),id(version,"id"),Timestamp.from(boundary),Timestamp.from(clock.instant()));
        var cb=(ObjectNode)contract.body().deepCopy(); cb.set("currentVersion",version); cb.putNull("scheduledChangeId").put("scheduleVersion",contract.body().path("scheduleVersion").asLong()+1).put("recordedAt",changes.now());
        contract=lifecycle.save(contract,cb,null,"CHANGE_APPLIED",change.id().toString());
        var body=(ObjectNode)change.body().deepCopy(); body.put("state","APPLIED").put("appliedVersionId",version.path("id").asString()); changes.save(change,body,null,"APPLY",null);
        if(version.path("terms").path("termKind").asString().equals("FIXED")) {
            obligations.termReview(contract,version,frozen.revision().path("contributorIds"),boundary);
            obligations.supersedeUnopenedTermReview(contract,previous,version);
            enqueueExpiry(contract,version);
        }
        return contract;
    }
    private void enqueueExpiry(EntityStore.Entity contract,tools.jackson.databind.JsonNode version) {
        var original=store.jdbc().queryForObject("select source_hash from cg_original_agreement where contract_id=?",String.class,contract.id());
        UUID packetId=id(contract.body(),"signingPacketId"); var input=store.jdbc().queryForObject("select input_hash from cg_signing_input where packet_id=?",String.class,packetId);
        var boundary=calendar.startOfDay(LocalDate.parse(version.path("terms").path("endExclusive").asString()));
        var payload=json.object().put("boundaryKind","EXPIRED").put("boundaryAt",JsonSupport.timestamp(boundary)).put("packetId",packetId.toString()).put("packetInputHash",input).put("originalSourceHash",original);
        var job=jobs.enqueue("EFFECTIVITY","CONTRACT",contract.id(),contract.branchId(),contract.version(),json.hash(payload),"EFFECTIVITY:VERSION:"+version.path("id").asString()+":EXPIRED",payload,AuditService.traceId());
        store.jdbc().update("update cg_job set available_at=? where id=? and attempts=0 and state='QUEUED'",Timestamp.from(boundary),job);
    }
    public void run(JobQueue.Job job) {
        tx.executeWithoutResult(status->{
            var preview=store.require("Change",job.targetId()); var contract=lifecycle.lock(id(preview.body(),"contractId")); var change=store.lock("Change",job.targetId());
            if(!Set.of("SCHEDULED","APPLIED","CONFLICTED").contains(change.body().path("state").asString())||!json.hash(job.payload()).equals(job.expectedHash())||!approval.frozen(change).revision().path("id").equals(job.payload().path("revisionId"))) { jobs.cancel(job,"Change source obsolete"); return; }
            if(!jobs.renew(job)) return;
            lifecycle.advanceLocked(contract); change=store.require("Change",change.id());
            boolean terminal=Set.of("APPLIED","CONFLICTED").contains(change.body().path("state").asString());
            if(terminal) { if(!jobs.succeed(job)) throw new IllegalStateException("Effect lease lost"); }
            else if(!jobs.fail(job,"GUARD_BLOCKED","Current Change boundary not applied")) throw new IllegalStateException("Effect lease lost");
            audit.record(null,"EFFECTIVITY",store.require("Job",job.id()),"Change boundary outcome");
        });
    }
    public void authorize(UUID changeId,Actor actor) { changes.require(changeId,actor); actor.requireRole("MAKER","CHECKER"); if(!actor.hasRole("CHECKER")) changes.maker(changeId,actor); }
    public void requireRetry(EntityStore.Entity job,Actor actor) {
        UUID changeId=id(job.body(),"parentId"); authorize(changeId,actor); var change=store.require("Change",changeId);
        if(!job.body().path("state").asString().equals("FAILED")||!change.body().path("state").asString().equals("SCHEDULED")) throw ApiException.of("STATE_CONFLICT","Change không còn retry effectivity được.");
        var rows=store.jdbc().query("select payload::text,expected_hash from cg_job where id=?",(rs,n)->List.of(rs.getString(1),rs.getString(2)),job.id()); var payload=json.parse(rows.getFirst().getFirst());
        if(!json.hash(payload).equals(rows.getFirst().get(1))||!payload.path("revisionId").equals(approval.frozen(change).revision().path("id"))) throw ApiException.of("STORAGE_INTEGRITY","Nguồn retry không khớp.");
    }
    public EntityStore.Entity retry(UUID id,String tag,String reason,Actor actor) {
        var preview=store.require("Job",id); changes.lock(id(preview.body(),"parentId")); var job=store.lock("Job",id); ETags.check(tag,job.version()); requireRetry(job,actor);
        if(!jobs.retry(id)) throw ApiException.of("STATE_CONFLICT","Job đã thay đổi."); var updated=store.require("Job",id); audit.record(actor,"RETRY",updated,reason); return updated;
    }
}
