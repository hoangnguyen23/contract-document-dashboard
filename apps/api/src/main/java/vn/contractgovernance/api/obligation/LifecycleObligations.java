package vn.contractgovernance.api.obligation;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

/** Actual durable G5 creation only. No duty is submitted/closed or externally confirmed here. */
@Service
public class LifecycleObligations {
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final AuditService audit;
    private final ContractRegistry schemas;
    public LifecycleObligations(EntityStore store, JsonSupport json, Clock clock,
            CalendarService calendar, AuditService audit, ContractRegistry schemas) {
        this.store = store; this.json = json; this.clock = clock;
        this.calendar = calendar; this.audit = audit; this.schemas = schemas;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void termReview(EntityStore.Entity contract, JsonNode version, JsonNode sourceContributors, Instant effectiveAt) {
        var end = LocalDate.parse(version.path("terms").path("endExclusive").asString());
        var due = end.minusDays(30).atTime(17, 0).atZone(CalendarService.ZONE).toInstant();
        create(contract, "TERM_REVIEW", UUID.fromString(version.path("id").asString()),
                version.path("number").asInt(), due, effectiveAt, sourceContributors);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void closeout(EntityStore.Entity contract, JsonNode version, JsonNode sourceContributors, Instant expiredAt) {
        create(contract, "CLOSEOUT", UUID.fromString(version.path("id").asString()),
                version.path("number").asInt(), calendar.dueByBusinessDays(expiredAt, 5), expiredAt, sourceContributors);
    }

    /** Only prospective, untouched duties are superseded; human actions/breaches remain operative. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void supersedeUnopenedTermReview(EntityStore.Entity contract,JsonNode previous,JsonNode replacement) {
        var ids=store.jdbc().query("select e.id from cg_entity e where e.entity_type='Obligation' and e.body->>'contractId'=? and e.body->>'type'='TERM_REVIEW' and e.body->>'sourceId'=? order by e.id",
                (rs,n)->rs.getObject(1,UUID.class),contract.id().toString(),previous.path("id").asString());
        for(var id:ids) {
            var duty=store.lock("Obligation",id); var body=duty.body();
            boolean untouched=body.path("state").asString().equals("OPEN")&&!body.hasNonNull("breachedAt")
                    &&body.path("evidenceFileIds").isEmpty()&&!body.hasNonNull("evidenceSummary")
                    &&!body.hasNonNull("extensionRequest")&&!body.hasNonNull("waiverRequest")
                    &&clock.instant().isBefore(Instant.parse(body.path("currentDueAt").asString()))
                    &&store.jdbc().queryForObject("select count(*) from cg_audit where resource_type='Obligation' and resource_id=? and actor_id is not null",Long.class,id)==0L;
            if(!untouched) continue;
            var next=(tools.jackson.databind.node.ObjectNode)body.deepCopy(); next.put("state","CANCELLED").putNull("closureOutcome").put("updatedAt",JsonSupport.timestamp(clock.instant()));
            var successors=store.jdbc().query("select entity_id from cg_lifecycle_consequence where kind='TERM_REVIEW' and source_id=? and source_version=?",(rs,n)->rs.getObject(1,UUID.class),UUID.fromString(replacement.path("id").asString()),replacement.path("number").asInt());
            if(successors.size()!=1) throw ApiException.of("STATE_CONFLICT","Thiếu successor TERM_REVIEW.");
            next.put("supersededById",successors.getFirst().toString());
            var updated=store.update(duty,next); schemas.validateSchema("Obligation",updated.body()); audit.record(null,"SUPERSEDE",updated,"Untouched future TERM_REVIEW replaced by new fixed version");
        }
    }

    /** Incident owns verification; this factory derives one actual contractual 48-hour duty. */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID remediation(EntityStore.Entity contract,EntityStore.Entity incident,Instant triggerAt,JsonNode contributors) {
        create(contract,"REMEDIATION",incident.id(),1,triggerAt.plus(Duration.ofHours(48)),triggerAt,contributors,"CONTRACTUAL","DEMO_REMEDIATION_48H; verified source trigger, not a statutory deadline");
        return store.jdbc().queryForObject("select entity_id from cg_lifecycle_consequence where kind='REMEDIATION' and source_id=? and source_version=1",UUID.class,incident.id());
    }
    private void create(EntityStore.Entity contract, String type, UUID sourceId, int sourceVersion,
            Instant due, Instant eventAt, JsonNode sourceContributors) {
        create(contract,type,sourceId,sourceVersion,due,eventAt,sourceContributors,"INTERNAL",type.equals("TERM_REVIEW")?"A §9.1: rà soát kỳ hạn phiên bản FIXED":"A §9.1: closeout sau kết thúc kỳ hạn");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void noticeAck(EntityStore.Entity contract,UUID noticeId,int sourceVersion,String effectiveOn,Instant dispatchedAt,JsonNode contributors) {
        create(contract,"NOTICE_ACK",noticeId,sourceVersion,calendar.startOfDay(LocalDate.parse(effectiveOn)),dispatchedAt,contributors,"CONTRACTUAL","TARIFF-ACK-7D-1; explicit beneficiary acknowledgement before effective boundary");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void acceptNoticeAck(EntityStore.Entity notice,JsonNode candidate,Actor actor) {
        for(var duty:noticeDuties(notice)) {
            synchronizeBreach(duty);
            duty=store.require("Obligation",duty.id());
            if(Set.of("CLOSED","CANCELLED").contains(duty.body().path("state").asString()))continue;
            var body=(tools.jackson.databind.node.ObjectNode)duty.body().deepCopy();
            body.put("state","CLOSED").put("closureOutcome","FULFILLED").put("evidenceSummary","Independent Notice ACK assessment "+candidate.path("id").asString()).put("updatedAt",JsonSupport.timestamp(clock.instant()));
            body.set("evidenceFileIds",candidate.path("evidenceFileIds"));
            var updated=store.update(duty,body);schemas.validateSchema("Obligation",updated.body());audit.record(actor,"APPROVE",updated,"Verified exact Notice ACK; original due and historical breaches retained");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void supersedeNoticeAck(EntityStore.Entity notice,String reason,Actor actor) {
        for(var duty:noticeDuties(notice)) {
            synchronizeBreach(duty);duty=store.require("Obligation",duty.id());
            if(Set.of("CLOSED","CANCELLED").contains(duty.body().path("state").asString()))continue;
            var body=(tools.jackson.databind.node.ObjectNode)duty.body().deepCopy();body.put("state","CANCELLED").put("updatedAt",JsonSupport.timestamp(clock.instant()));
            var updated=store.update(duty,body);schemas.validateSchema("Obligation",updated.body());audit.record(actor,"CANCEL_INAPPLICABLE",updated,"Sole tariff source no longer applies: "+reason);
        }
    }

    private List<EntityStore.Entity> noticeDuties(EntityStore.Entity notice) {
        return store.jdbc().query("select entity_id from cg_lifecycle_consequence where kind='NOTICE_ACK' and source_id=? order by entity_id",(rs,n)->rs.getObject(1,UUID.class),notice.id()).stream().map(id->store.lock("Obligation",id)).toList();
    }
    public void synchronizeBreach(EntityStore.Entity duty) {
        var body=duty.body();if(Set.of("CLOSED","CANCELLED").contains(body.path("state").asString()))return;
        var due=Instant.parse(body.path("currentDueAt").asString());if(!clock.instant().isAfter(due))return;
        int inserted=store.jdbc().update("insert into cg_lifecycle_obligation_breach(obligation_id,due_revision,breached_at,recorded_at) values(?,?,?,?) on conflict do nothing",duty.id(),body.path("dueRevision").asInt(),Timestamp.from(due),Timestamp.from(clock.instant()));
        if(inserted==0)return;var next=(tools.jackson.databind.node.ObjectNode)body.deepCopy();
        if(!next.hasNonNull("breachedAt"))next.put("breachedAt",JsonSupport.timestamp(due));
        for(var item:next.path("deadlineHistory"))if(item.path("number").asInt()==body.path("dueRevision").asInt())((tools.jackson.databind.node.ObjectNode)item).put("breachedAt",JsonSupport.timestamp(due));
        next.put("updatedAt",JsonSupport.timestamp(clock.instant()));var updated=store.update(duty,next);schemas.validateSchema("Obligation",updated.body());audit.record(null,"BREACH",updated,"Missed operative due before Notice disposition");
    }

    private void create(EntityStore.Entity contract, String type, UUID sourceId, int sourceVersion,
            Instant due, Instant eventAt, JsonNode sourceContributors,String basisType,String basisText) {
        if (store.jdbc().queryForObject("select count(*) from cg_lifecycle_consequence where kind=? and source_id=? and source_version=?",
                Long.class, type, sourceId, sourceVersion) != 0L) return;
        var relationship = store.lock("Relationship", UUID.fromString(contract.body().path("relationshipId").asString()));
        var assignedMaker = relationship.body().path("assignedMakerId").asString();
        var now = JsonSupport.timestamp(clock.instant());
        boolean missed = clock.instant().isAfter(due);
        var body = json.object().put("createdAt", now).put("updatedAt", now)
                .put("branchId", contract.branchId().toString()).put("state", "OPEN")
                .put("contractId", contract.id().toString()).put("sourceId", sourceId.toString())
                .put("type", type).put("basisType", basisType).put("basisText",basisText)
                .put("assignedMakerId", assignedMaker)
                .put("originalDueAt", JsonSupport.timestamp(due)).put("currentDueAt", JsonSupport.timestamp(due)).put("dueRevision", 1);
        for (var field : List.of("breachedAt", "evidenceSummary", "closureOutcome", "extensionRequest", "waiverRequest", "supersededById")) body.putNull(field);
        for (var field : List.of("allowedActions", "blockedActions", "evidenceFileIds", "unresolvedIncidentIds", "externalConfirmations")) body.set(field, json.array());
        body.set("contributorIds", sourceContributors.deepCopy());
        var history = json.object().put("number", 1).put("dueAt", JsonSupport.timestamp(due))
                .put("effectiveAt", JsonSupport.timestamp(eventAt)).put("basis", "DEMO_CAL_2027-v1; nguồn lifecycle bất biến");
        history.putNull("decisionBy").putNull("breachedAt");
        if (missed) {
            body.put("breachedAt", JsonSupport.timestamp(due));
            history.put("breachedAt", JsonSupport.timestamp(due));
        }
        body.set("deadlineHistory", json.array().add(history));
        var entity = store.create("Obligation", UUID.randomUUID(), contract.branchId(), body);
        schemas.validateSchema("Obligation", entity.body());
        store.jdbc().update("insert into cg_lifecycle_consequence(kind,source_id,source_version,entity_id,created_at) values(?,?,?,?,?)",
                type, sourceId, sourceVersion, entity.id(), Timestamp.from(clock.instant()));
        store.jdbc().update("insert into cg_obligation_due(obligation_id,number,due_at,effective_at,decision_by,basis) values(?,1,?,?,null,?)",entity.id(),Timestamp.from(due),Timestamp.from(eventAt),basisText);
        var source=json.object();source.set("sourceProjection",body);source.put("sourceVersion",sourceVersion).put("eventAt",JsonSupport.timestamp(eventAt));
        store.jdbc().update("insert into cg_g5_event(id,target_type,target_id,kind,body,body_hash,actor_id,recorded_at) values(?,'Obligation',?,'SOURCE',?::jsonb,?,null,?)",UUID.randomUUID(),entity.id(),json.stringify(source),json.hash(source),Timestamp.from(clock.instant()));
        audit.record(null, "CREATE", entity, "Lifecycle consequence; pending independent G5 completion");
        if (missed) {
            store.jdbc().update("insert into cg_lifecycle_obligation_breach(obligation_id,due_revision,breached_at,recorded_at) values(?,1,?,?)",
                    entity.id(), Timestamp.from(due), Timestamp.from(clock.instant()));
            audit.record(null, "BREACH", entity, "Operative source due missed at " + JsonSupport.timestamp(due));
        }
    }
}
