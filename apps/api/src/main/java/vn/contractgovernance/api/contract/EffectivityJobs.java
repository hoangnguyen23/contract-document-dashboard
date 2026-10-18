package vn.contractgovernance.api.contract;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.signing.SigningEvidence;

/** Durable original EFFECTIVITY with action-boundary availability and per-claim lease fencing. */
@Service
public class EffectivityJobs {
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeTimeline> changes;
    private final JobQueue queue;
    private final EntityStore store;
    private final ContractLifecycle lifecycle;
    private final OriginalAgreementService originals;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final AuditService audit;
    private final TransactionTemplate tx;
    public EffectivityJobs(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeTimeline> changes,JobQueue queue, EntityStore store, ContractLifecycle lifecycle,
            OriginalAgreementService originals, JsonSupport json, Clock clock, CalendarService calendar,
            AuditService audit, PlatformTransactionManager manager) {
        this.changes=changes;this.queue=queue; this.store=store; this.lifecycle=lifecycle; this.originals=originals;
        this.json=json; this.clock=clock; this.calendar=calendar; this.audit=audit;
        this.tx=new TransactionTemplate(manager);
    }

    public static void enqueue(JobQueue queue, EntityStore store, JsonSupport json, Clock clock,
            EntityStore.Entity contract, JsonNode packet, Instant start) {
        enqueueBoundary(queue,store,json,clock,contract,packet,"ACTIVE",start);
        var terms=contract.body().path("approvedTerms");
        if (terms.path("termKind").asString().equals("FIXED")) {
            enqueueBoundary(queue,store,json,clock,contract,packet,"EXPIRED",
                    LocalDate.parse(terms.path("endExclusive").asString()).atStartOfDay(CalendarService.ZONE).toInstant());
        }
    }

    private static void enqueueBoundary(JobQueue queue, EntityStore store, JsonSupport json, Clock clock,
            EntityStore.Entity contract, JsonNode packet, String boundaryKind, Instant boundary) {
        var inputHash=store.jdbc().queryForObject("select input_hash from cg_signing_input where packet_id=?",String.class,SigningEvidence.uuid(packet,"id"));
        var sourceHash=store.jdbc().queryForObject("select source_hash from cg_original_agreement where contract_id=?",String.class,contract.id());
        var payload=json.object().put("boundaryKind",boundaryKind).put("boundaryAt",JsonSupport.timestamp(boundary))
                .put("packetId",packet.path("id").asString()).put("packetInputHash",inputHash).put("originalSourceHash",sourceHash);
        var id=queue.enqueue("EFFECTIVITY","CONTRACT",contract.id(),contract.branchId(),contract.version(),json.hash(payload),
                "EFFECTIVITY:ORIGINAL:"+contract.id()+":"+packet.path("id").asString()+":"+boundaryKind,payload,AuditService.traceId());
        store.jdbc().update("update cg_job set available_at=?,updated_at=? where id=? and state='QUEUED' and attempts=0",
                Timestamp.from(boundary),Timestamp.from(clock.instant()),id);
    }

    @Scheduled(fixedDelayString="${cg.effectivity.poll-ms:1500}")
    public void poll() {
        var claimed=queue.claimForKind("EFFECTIVITY",UUID.randomUUID());
        if (claimed.isEmpty()) return;
        var job=claimed.get();
        try {
            if(job.targetType().equals("CHANGE")){changes.getObject().run(job);return;}
            tx.executeWithoutResult(status -> {
                if (!job.targetType().equals("CONTRACT")) {
                    queue.fail(job,"GUARD_BLOCKED","CHANGE effectivity adapter unavailable"); return;
                }
                var contract=lifecycle.lock(job.targetId());
                if (!validSource(job,contract)) {
                    queue.cancel(job,"Immutable original source or selected packet is obsolete"); return;
                }
                if (!queue.renew(job)) return;
                var updated=lifecycle.advanceLocked(contract);
                if(job.payload().path("boundaryKind").asString().equals("EXPIRED")
                        && updated.body().path("state").asString().equals("ACTIVE")
                        && !job.payload().path("boundaryAt").asString().equals(JsonSupport.timestamp(calendar.startOfDay(LocalDate.parse(updated.body().path("currentVersion").path("terms").path("endExclusive").asString()))))) {
                    if(!queue.cancel(job,"Old expiry boundary superseded by current version"))throw new IllegalStateException("Effectivity lease lost");
                    return;
                }
                boolean due= !clock.instant().isBefore(Instant.parse(job.payload().path("boundaryAt").asString()));
                boolean completed=job.payload().path("boundaryKind").asString().equals("ACTIVE")
                        ? updated.body().hasNonNull("currentVersion")
                        : updated.body().path("state").asString().equals("EXPIRED");
                if (!due || !completed) {
                    if (!queue.fail(job,"GUARD_BLOCKED","Current readiness/risk/material guard prevents the due original effect")) {
                        throw new IllegalStateException("Effectivity lease lost before blocked outcome");
                    }
                } else if (!queue.succeed(job)) {
                    throw new IllegalStateException("Effectivity lease lost before completion");
                }
                audit.record(null,"EFFECTIVITY",store.require("Job",job.id()),"Original boundary outcome");
            });
        } catch (RuntimeException failure) {
            tx.executeWithoutResult(status -> {
                var code=failure instanceof ApiException api && api.code().equals("STORAGE_INTEGRITY")
                        ? "STORAGE_INTEGRITY" : "GUARD_BLOCKED";
                queue.fail(job,code,"Original effectivity rolled back; current dependency or guard blocked");
            });
        }
    }

    private boolean validSource(JobQueue.Job job, EntityStore.Entity contract) {
        if (contract.body().path("state").asString().equals("CANCELLED")) return false;
        var payload=job.payload();
        if (!json.hash(payload).equals(job.expectedHash())
                || !payload.path("packetId").equals(contract.body().path("signingPacketId"))) return false;
        var source=originals.source(contract.id());
        if (!source.hash().equals(payload.path("originalSourceHash").asString())
                || !source.hash().equals(json.hash(source.body()))) return false;
        var hashes=store.jdbc().query("select input_hash from cg_signing_input where packet_id=? and contract_id=?",
                (rs,row)->rs.getString(1),SigningEvidence.uuid(payload,"packetId"),contract.id());
        return hashes.size()==1 && hashes.getFirst().equals(payload.path("packetInputHash").asString());
    }

    public void authorize(UUID id, Actor actor) {if(store.find("Change",id).isPresent()){changes.getObject().authorize(id,actor);return;} originals.authorizeArtifactMutation(id,actor); }

    public void requireRetry(EntityStore.Entity job, Actor actor) {
        if(job.body().path("parentKind").asString().equals("CHANGE")){changes.getObject().requireRetry(job,actor);return;}
        var id=SigningEvidence.uuid(job.body(),"parentId");
        authorize(id,actor);
        var contract=store.require("Contract",id);
        if (!job.body().path("kind").asString().equals("EFFECTIVITY")
                || !job.body().path("state").asString().equals("FAILED")
                || !java.util.List.of("SCHEDULED","ACTIVE").contains(contract.body().path("state").asString())) {
            throw ApiException.of("STATE_CONFLICT","Job không còn đủ điều kiện retry effectivity.");
        }
        var rows=store.jdbc().query("select payload::text,expected_hash from cg_job where id=?",
                (rs,row)->new String[]{rs.getString(1),rs.getString(2)},job.id());
        var payload=json.parse(rows.getFirst()[0]);
        if (!rows.getFirst()[1].equals(json.hash(payload))
                || !payload.path("packetId").equals(contract.body().path("signingPacketId"))
                || !payload.path("originalSourceHash").asString().equals(originals.source(id).hash())) {
            throw ApiException.of("STORAGE_INTEGRITY","Nguồn effectivity không khớp.");
        }
        lifecycle.pendingChangeGuard(contract);
        if (contract.body().path("state").asString().equals("SCHEDULED")) {
            if (!contract.body().path("readiness").asString().equals("READY") || ContractLifecycle.unresolvedRisk(contract.body())) {
                throw ApiException.of("READINESS_BLOCKED","Cần giải quyết readiness trước retry.");
            }
            if (!lifecycleMaterial(contract)) throw ApiException.of("READINESS_BLOCKED","Material source đã đổi.");
        }
    }

    private boolean lifecycleMaterial(EntityStore.Entity contract) {
        // Projection is read-only; exact lock/byte guards run in the actual retry transaction.
        return store.require("Dossier",SigningEvidence.uuid(contract.body(),"dossierId")).body().path("state").asString().equals("APPROVED");
    }
    public EntityStore.Entity retry(UUID id, String tag, String reason, Actor actor) {
        if(store.require("Job",id).body().path("parentKind").asString().equals("CHANGE"))return changes.getObject().retry(id,tag,reason,actor);
        var preview=store.require("Job",id);
        lifecycle.lock(SigningEvidence.uuid(preview.body(),"parentId"));
        var job=store.lock("Job",id);
        ETags.check(tag,job.version()); requireRetry(job,actor);
        var contract=store.require("Contract",SigningEvidence.uuid(preview.body(),"parentId"));
        if (contract.body().path("state").asString().equals("SCHEDULED")) lifecycle.requireReadyEvidence(contract);
        if (!queue.retry(id)) throw ApiException.of("STATE_CONFLICT","Job đã đổi.");
        var updated=store.require("Job",id); audit.record(actor,"RETRY",updated,reason); return updated;
    }
}
