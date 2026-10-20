package vn.contractgovernance.api.signing;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.*;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.*;

/** Original CONTRACT adapter. CHANGE is deliberately unavailable until its real source adapter exists. */
@Service
public class SigningService {
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeSigning> changes;
    private final EvidenceAccess access;
    private final EntityStore store;
    private final OriginalAgreementService originals;
    private final ContractLifecycle lifecycle;
    private final SigningEvidence evidence;
    private final ContractContributions contributions;
    private final HistoricalContractFiles historical;
    private final CustomerAccess customers;
    private final UserDirectory users;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final CommandExecutor commands;
    private final AuditService audit;
    private final ListQueries lists;
    private final JobQueue jobs;
    private final ContractRegistry schemas;

    public SigningService(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeSigning> changes,EvidenceAccess access, EntityStore store, OriginalAgreementService originals, ContractLifecycle lifecycle,
            SigningEvidence evidence, ContractContributions contributions, HistoricalContractFiles historical,
            CustomerAccess customers, UserDirectory users, JsonSupport json, Clock clock,
            CalendarService calendar, CommandExecutor commands, AuditService audit, ListQueries lists,
            JobQueue jobs, ContractRegistry schemas) {
        this.changes=changes;this.access=access; this.store=store; this.originals=originals; this.lifecycle=lifecycle; this.evidence=evidence;
        this.contributions=contributions; this.historical=historical; this.customers=customers; this.users=users;
        this.json=json; this.clock=clock; this.calendar=calendar; this.commands=commands;
        this.audit=audit; this.lists=lists; this.jobs=jobs; this.schemas=schemas;
    }

    private EntityStore.Entity contractForPacket(UUID id, Actor actor) {
        var packet=store.require("SigningPacket",id);
        users.actor(actor.id()).requireBranch(packet.branchId());
        if (!packet.body().path("targetKind").asString().equals("CONTRACT")) {
            access.parent(packet.body().path("targetKind").asString(),SigningEvidence.uuid(packet.body(),"targetId"),users.actor(actor.id()));
            throw ApiException.of("SERVICE_UNAVAILABLE","Signing CHANGE adapter chưa triển khai.");
        }
        return lifecycle.require(SigningEvidence.uuid(packet.body(),"targetId"),actor);
    }

    private void maker(UUID contractId, Actor actor) {
        var contract=lifecycle.require(contractId,actor);
        customers.authorizeAssignedMaker(SigningEvidence.uuid(contract.body(),"relationshipId"),users.actor(actor.id()));
    }

    private void checker(UUID packetId, Actor actor) {
        evidence.independent(contractForPacket(packetId,actor),users.actor(actor.id()));
    }

    public ApiResult create(JsonNode request, String key, Actor actor) {
        if(request.path("targetKind").asString().equals("CHANGE"))return changes.getObject().create(request,key,actor);
        users.actor(actor.id()).requireRole("MAKER");
        if (!request.path("targetKind").asString().equals("CONTRACT")) {
            access.parent(request.path("targetKind").asString(),SigningEvidence.uuid(request,"targetId"),users.actor(actor.id()));
            throw ApiException.of("SERVICE_UNAVAILABLE","Signing CHANGE adapter chưa triển khai.");
        }
        var contractId=SigningEvidence.uuid(request,"targetId");
        maker(contractId,actor);
        return commands.execute(actor,key,"POST","/api/v1/signing-packets",request,null,
                () -> maker(contractId,actor), () -> lifecycle.maintain(contractId,actor), () -> {
                    var contract=lifecycle.lock(contractId);
                    maker(contractId,actor);
                    customers.requireAssignedMaker(SigningEvidence.uuid(contract.body(),"relationshipId"),actor);
                    if (!contract.body().path("state").asString().equals("PREPARED")) {
                        throw ApiException.of("STATE_CONFLICT","Chỉ tạo packet cho hợp đồng PREPARED.");
                    }
                    evidence.requireMaterial(contract);
                    evidence.authority(request);
                    evidence.artifactAndCopies(contract,request,actor);
                    contributions.record(contract.id(),actor.id());
                    var ids=evidence.contributors(contract);
                    if (ids.size()>50) throw ApiException.validation("/contributorIds","Nguồn vượt quá 50 người đóng góp.");
                    var manifest=evidence.manifest(contract,evidence.packetFiles(request),actor);
                    historical.retain(contract.id(),evidence.packetFiles(request));
                    var body=(ObjectNode)request.deepCopy();
                    var now=JsonSupport.timestamp(clock.instant());
                    body.put("createdAt",now).put("updatedAt",now).put("branchId",contract.branchId().toString())
                            .put("verificationStatus","PENDING");
                    body.set("allowedActions",json.array()); body.set("blockedActions",json.array());
                    body.set("contributorIds",json.value(ids.stream().map(UUID::toString).sorted().toList()));
                    for (var field:List.of("verificationReason","verifiedBy","verifiedAt")) body.putNull(field);
                    var packet=store.create("SigningPacket",UUID.randomUUID(),contract.branchId(),body);
                    schemas.validateSchema("SigningPacket",packet.body());
                    store.jdbc().update("insert into cg_signing_input(packet_id,contract_id,input,input_hash,file_manifest,submitted_by,created_at) values(?,?,?::jsonb,?,?::jsonb,?,?)",
                            packet.id(),contract.id(),json.stringify(json.nfc(request)),json.hash(request),json.stringify(manifest),actor.id(),Timestamp.from(clock.instant()));
                    var contractBody=(ObjectNode)contract.body().deepCopy();
                    contractBody.put("signingPacketId",packet.id().toString());
                    lifecycle.save(contract,contractBody,actor,"SIGN","Immutable reported signing evidence; pending independent verification");
                    audit.record(actor,"CREATE",packet,null);
                    return ApiResult.created(project(packet.body(),actor),"/api/v1/signing-packets/"+packet.id());
                });
    }

    public ApiResult command(UUID id, JsonNode request, String tag, String key, Actor actor) {
        if(changePacket(id))return changes.getObject().command(id,request,tag,key,actor);
        checker(id,actor); ETags.require(tag);
        var preview=contractForPacket(id,actor);
        return commands.execute(actor,key,"POST","/api/v1/signing-packets/"+id+"/commands",request,tag,
                () -> checker(id,actor), () -> lifecycle.maintain(preview.id(),actor), () -> {
                    var contract=lifecycle.lock(preview.id());
                    checker(id,actor);
                    var packet=store.lock("SigningPacket",id);
                    ETags.check(tag,packet.version());
                    if (!packet.body().path("targetId").asString().equals(contract.id().toString())
                            || !packet.body().path("verificationStatus").asString().equals("PENDING")) {
                        throw ApiException.of("STATE_CONFLICT","Packet không còn PENDING.");
                    }
                    if (!contract.body().path("state").asString().equals("PREPARED")) {
                        throw ApiException.of("STATE_CONFLICT","Contract không còn PREPARED.");
                    }
                    var body=(ObjectNode)packet.body().deepCopy();
                    if (request.path("action").asString().equals("VERIFY_SIGNING")) {
                        var verifiedAt=verify(contract,packet,request,actor);
                        body.put("verificationStatus","VERIFIED").put("verifiedBy",actor.id().toString())
                                .put("verifiedAt",JsonSupport.timestamp(verifiedAt));
                    } else if (request.path("action").asString().equals("RETURN_SIGNING")) {
                        body.put("verificationStatus","RETURNED");
                    } else throw ApiException.validation("/action","Command không hỗ trợ.");
                    body.put("verificationReason",request.path("reason").asString()).put("updatedAt",JsonSupport.timestamp(clock.instant()));
                    var updated=store.update(packet,body);
                    schemas.validateSchema("SigningPacket",updated.body());
                    audit.record(actor,request.path("action").asString(),updated,request.path("reason").asString());
                    return ApiResult.ok(project(updated.body(),actor));
                });
    }

    private Instant verify(EntityStore.Entity contract, EntityStore.Entity packet, JsonNode request, Actor actor) {
        var start=calendar.startOfDay(LocalDate.parse(contract.body().path("approvedTerms").path("startOn").asString()));
        if (!clock.instant().isBefore(start)) throw ApiException.of("LATE_SIGNING","Readiness phải được server xác minh trước startOn 00:00 Việt Nam.");
        if (ContractLifecycle.unresolvedRisk(contract.body())) throw ApiException.of("READINESS_BLOCKED","Còn readiness risk chưa giải quyết.");
        lifecycle.pendingChangeGuard(contract);
        evidence.requireMaterial(contract);
        lifecycle.verifyFrozenPacket(contract,packet.body());
        evidence.assessment(contract,packet.body(),request.path("checks"),request.path("mandatoryChecks"),actor);
        var refs=new TreeSet<>(evidence.packetFiles(packet.body()));
        for (var check:request.path("checks")) refs.add(SigningEvidence.uuid(check,"fileId"));
        for (var check:request.path("mandatoryChecks")) refs.addAll(SigningEvidence.uniqueIds(check.path("evidenceFileIds"),"/mandatoryChecks/evidenceFileIds"));
        var assessment=(ObjectNode)request.deepCopy();
        assessment.set("fileManifest",evidence.manifest(contract,refs,actor));
        historical.retain(contract.id(),refs);
        var verifiedAt=clock.instant();
        if (!verifiedAt.isBefore(start)) throw ApiException.of("LATE_SIGNING","Server hoàn tất rà soát sau start boundary.");
        store.jdbc().update("insert into cg_signing_assessment(packet_id,assessment,assessed_by,assessed_at) values(?,?::jsonb,?,?)",
                packet.id(),json.stringify(assessment),actor.id(),Timestamp.from(verifiedAt));
        var body=(ObjectNode)contract.body().deepCopy();
        body.put("state","SCHEDULED").put("readiness","READY").put("readinessVerifiedAt",JsonSupport.timestamp(verifiedAt))
                .put("signingPacketId",packet.id().toString());
        body.set("mandatoryChecks",request.path("mandatoryChecks").deepCopy());
        var updated=lifecycle.save(contract,body,actor,"VERIFY_SIGNING",request.path("reason").asString());
        EffectivityJobs.enqueue(jobs,store,json,clock,updated,packet.body(),start);
        return verifiedAt;
    }

    public JsonNode get(UUID id, Actor actor) {
        if(changePacket(id))return changes.getObject().get(id,actor);
        var contract=contractForPacket(id,actor);
        lifecycle.maintain(contract.id(),actor);
        return project(store.require("SigningPacket",id).body(),actor);
    }

    public JsonNode list(Map<String,String> query, Actor actor) {
        actor=users.actor(actor.id());
        actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");
        var args=new ArrayList<Object>();
        var scope="e.body->>'targetKind' in ('CONTRACT','CHANGE') and exists(select 1 from cg_entity c join cg_entity r on r.entity_type='Relationship' and r.id=(c.body->>'relationshipId')::uuid and r.branch_id=c.branch_id where c.entity_type='Contract' and c.id=case e.body->>'targetKind' when 'CONTRACT' then (e.body->>'targetId')::uuid else (select (ch.body->>'contractId')::uuid from cg_entity ch where ch.entity_type='Change' and ch.id=(e.body->>'targetId')::uuid and ch.branch_id=e.branch_id) end and c.branch_id=e.branch_id and "+customers.businessSqlScope(actor,"r")+")";
        if (!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")) args.add(actor.id().toString());
        var caller=actor;
        return lists.page("SigningPacket",actor,query,false,scope,args,body -> project(body,caller));
    }

    private boolean fullBusiness(JsonNode packet, Actor actor) {
        if (actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER")) return true;
        var contract=store.require("Contract",SigningEvidence.uuid(packet,"targetId"));
        return actor.hasRole("MAKER") && store.require("Relationship",SigningEvidence.uuid(contract.body(),"relationshipId"))
                .body().path("assignedMakerId").asString().equals(actor.id().toString());
    }
    private boolean changePacket(UUID id){return store.require("SigningPacket",id).body().path("targetKind").asString().equals("CHANGE");}
    public JsonNode project(JsonNode raw, Actor actor) {
        if(raw.path("targetKind").asString().equals("CHANGE"))return changes.getObject().project(raw,actor);
        var body=(ObjectNode)raw.deepCopy();
        var actions=json.array(); var blocked=json.array();
        if (body.path("verificationStatus").asString().equals("PENDING") && actor.hasRole("CHECKER")) {
            for (var action:List.of("VERIFY_SIGNING","RETURN_SIGNING")) {
                try {
                    checker(SigningEvidence.uuid(body,"id"),actor);
                    var contract=contractForPacket(SigningEvidence.uuid(body,"id"),actor);
                    if (!contract.body().path("state").asString().equals("PREPARED")) throw ApiException.of("STATE_CONFLICT","Contract không còn PREPARED.");
                    if (action.equals("VERIFY_SIGNING")) {
                        if (!clock.instant().isBefore(calendar.startOfDay(LocalDate.parse(contract.body().path("approvedTerms").path("startOn").asString()))))
                            throw ApiException.of("LATE_SIGNING","Xác minh ban đầu đã trễ hạn.");
                        if (ContractLifecycle.unresolvedRisk(contract.body())) throw ApiException.of("READINESS_BLOCKED","Còn risk chưa giải quyết.");
                        if (!evidence.materialUnchanged(contract,false)) throw ApiException.of("READINESS_BLOCKED","Material source thay đổi; cần hồ sơ mới.");
                    }
                    actions.add(action);
                } catch (ApiException exception) {
                    blocked.add(json.object().put("action",action).put("code",exception.code()).put("reason",exception.getMessage()));
                }
            }
        }
        if (actor.hasRole("AUDITOR") && !fullBusiness(body,actor) && !actor.hasPermission("SENSITIVE_READ")) {
            for (var side:List.of("bank","beneficiary")) {
                var representation=(ObjectNode)body.path("authority").path(side);
                for (var field:List.of("personName","title","scopeText","issuerReference")) representation.put(field,"[REDACTED]");
            }
            if (body.hasNonNull("verificationReason")) body.put("verificationReason","[REDACTED]");
        }
        body.set("allowedActions",actions); body.set("blockedActions",blocked); return body;
    }
}
