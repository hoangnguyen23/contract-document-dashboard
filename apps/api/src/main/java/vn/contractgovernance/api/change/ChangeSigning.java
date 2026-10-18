package vn.contractgovernance.api.change;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.*;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.signing.SigningEvidence;
import static vn.contractgovernance.api.change.ChangeService.id;

/** Negotiated appendix signing. Approval source, source packet and assessment remain distinct. */
@Service
public class ChangeSigning {
    private final ChangeService changes;
    private final ChangeApproval approval;
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final UserDirectory users;
    private final SigningEvidence evidence;
    private final ContractLifecycle lifecycle;
    private final CommandExecutor commands;
    private final ContractRegistry schemas;
    private final AuditService audit;
    public ChangeSigning(ChangeService changes,ChangeApproval approval,EntityStore store,JsonSupport json,
            Clock clock,CalendarService calendar,UserDirectory users,SigningEvidence evidence,
            ContractLifecycle lifecycle,CommandExecutor commands,ContractRegistry schemas,AuditService audit) {
        this.changes=changes; this.approval=approval; this.store=store; this.json=json; this.clock=clock;
        this.calendar=calendar; this.users=users; this.evidence=evidence; this.lifecycle=lifecycle;
        this.commands=commands; this.schemas=schemas; this.audit=audit;
    }
    public EntityStore.Entity source(UUID packetId,Actor actor) {
        var packet=store.require("SigningPacket",packetId); users.actor(actor.id()).requireBranch(packet.branchId());
        if(!packet.body().path("targetKind").asString().equals("CHANGE")) throw ApiException.of("NOT_FOUND","Không phải CHANGE packet.");
        return changes.require(id(packet.body(),"targetId"),actor);
    }
    private void checker(UUID packetId,Actor actor) {
        var change=source(packetId,actor); independent(change,actor);
    }
    private void independent(EntityStore.Entity change,Actor actor) {
        var current=users.actor(actor.id()); current.requireRole("CHECKER"); current.requireBranch(change.branchId());
        if(changes.contributors(change).contains(actor.id())) throw ApiException.of("SOD_VIOLATION","Signing cần người xác minh không đóng góp nguồn.");
    }
    private void approved(EntityStore.Entity change) {
        if(!change.body().path("state").asString().equals("APPROVED")) throw ApiException.of("STATE_CONFLICT","Change không APPROVED.");
        if(change.body().path("target").path("kind").asString().equals("TARIFF_NOTICE")) throw ApiException.of("NOTICE_NOT_READY","Tariff dùng exact notice/ack, không dùng signing packet.");
    }
    public ApiResult create(JsonNode request,String key,Actor actor) {
        UUID changeId=id(request,"targetId"); changes.maker(changeId,actor);
        return commands.execute(actor,key,"POST","/api/v1/signing-packets",request,null,()->changes.maker(changeId,actor),
                ()->lifecycle.maintain(id(changes.require(changeId,actor).body(),"contractId"),actor),()->{
            var change=changes.lock(changeId); changes.maker(changeId,actor); approved(change); changes.beforeBoundary(change);
            var contract=store.require("Contract",id(change.body(),"contractId")); changes.active(contract); changes.currentBase(contract,change.body()); changes.requireCurrentGuard(contract); evidence.requireMaterial(contract);
            evidence.authority(request); artifact(change,request,actor);
            changes.contribute(changeId,actor.id()); var refs=evidence.packetFiles(request); var manifest=changes.manifest(change,refs,actor);
            var body=(ObjectNode)json.nfc(request); body.put("createdAt",changes.now()).put("updatedAt",changes.now()).put("branchId",change.branchId().toString()).put("verificationStatus","PENDING");
            body.set("contributorIds",json.value(changes.contributors(change).stream().map(UUID::toString).toList())); body.set("allowedActions",json.array()); body.set("blockedActions",json.array());
            body.putNull("verificationReason").putNull("verifiedBy").putNull("verifiedAt");
            var packet=store.create("SigningPacket",UUID.randomUUID(),change.branchId(),body); schemas.validateSchema("SigningPacket",packet.body());
            store.jdbc().update("insert into cg_change_signing_input(packet_id,change_id,revision_id,input,input_hash,file_manifest,submitted_by,created_at) values(?,?,?,?::jsonb,?,?::jsonb,?,?)",packet.id(),changeId,id(approval.frozen(change).revision(),"id"),json.stringify(json.nfc(request)),json.hash(request),json.stringify(manifest),actor.id(),Timestamp.from(clock.instant()));
            var cb=(ObjectNode)change.body().deepCopy(); cb.put("signingPacketId",packet.id().toString()); cb.set("contributorIds",body.path("contributorIds")); changes.save(change,cb,actor,"SIGN",null); audit.record(actor,"CREATE",packet,null);
            return ApiResult.created(project(packet.body(),actor),"/api/v1/signing-packets/"+packet.id());
        });
    }
    public ApiResult command(UUID id,JsonNode request,String tag,String key,Actor actor) {
        checker(id,actor); ETags.require(tag); var changeId=source(id,actor).id();
        return commands.execute(actor,key,"POST","/api/v1/signing-packets/"+id+"/commands",request,tag,()->checker(id,actor),
                ()->lifecycle.maintain(id(changes.require(changeId,actor).body(),"contractId"),actor),()->{
            var change=changes.lock(changeId); var packet=store.lock("SigningPacket",id); checker(id,actor); ETags.check(tag,packet.version()); approved(change);
            if(!packet.body().path("verificationStatus").asString().equals("PENDING")||!change.body().path("signingPacketId").asString().equals(id.toString())) throw ApiException.of("STATE_CONFLICT","Packet không còn là candidate hiện tại.");
            var body=(ObjectNode)packet.body().deepCopy(); String action=request.path("action").asString();
            if(action.equals("VERIFY_SIGNING")) {
                changes.beforeBoundary(change); var contract=store.require("Contract",id(change.body(),"contractId")); changes.active(contract); changes.currentBase(contract,change.body()); changes.requireCurrentGuard(contract); evidence.requireMaterial(contract);
                frozen(change,packet.body()); assessment(change,packet.body(),request,actor);
                changes.beforeBoundary(change); var assessed=(ObjectNode)request.deepCopy(); assessed.set("fileManifest",changes.manifest(change,assessmentFiles(packet.body(),request),actor));
                store.jdbc().update("insert into cg_change_signing_assessment(packet_id,assessment,assessed_by,assessed_at) values(?,?::jsonb,?,?)",id,json.stringify(assessed),actor.id(),Timestamp.from(clock.instant()));
                body.put("verificationStatus","VERIFIED").put("verifiedBy",actor.id().toString()).put("verifiedAt",changes.now());
                var cb=(ObjectNode)change.body().deepCopy(); cb.put("state","READY"); changes.save(change,cb,actor,"VERIFY_SIGNING",request.path("reason").asString());
            } else if(action.equals("RETURN_SIGNING")) body.put("verificationStatus","RETURNED");
            else throw ApiException.validation("/action","Thao tác không hỗ trợ.");
            body.put("verificationReason",request.path("reason").asString()).put("updatedAt",changes.now()); var updated=store.update(packet,body); schemas.validateSchema("SigningPacket",updated.body()); audit.record(actor,action,updated,request.path("reason").asString()); return ApiResult.ok(project(updated.body(),actor));
        });
    }
    private void artifact(EntityStore.Entity change,JsonNode packet,Actor actor) {
        if(!change.body().hasNonNull("artifactId")||!change.body().path("artifactId").equals(packet.path("artifactId"))||!change.body().path("artifactHash").equals(packet.path("artifactHash"))) throw ApiException.of("STORAGE_INTEGRITY","Artifact không khớp Change đã duyệt.");
        var revision=approval.frozen(change).revision();
        if(store.jdbc().queryForObject("select count(*) from cg_change_artifact a join cg_change_artifact_source s using(revision_id) where a.revision_id=? and a.file_id=? and a.artifact_hash=? and a.source_hash=s.source_hash",Long.class,id(revision,"id"),id(packet,"artifactId"),packet.path("artifactHash").asString())!=1L) throw ApiException.of("STORAGE_INTEGRITY","Thiếu artifact binding chính xác.");
        for(UUID copy:SigningEvidence.uniqueIds(packet.path("signedCopyIds"),"/signedCopyIds")) {
            var file=store.require("File",copy);
            if(copy.equals(id(packet,"artifactId"))||!file.body().path("origin").asString().equals("UPLOADED")||!file.body().path("sha256").isTextual()) throw ApiException.validation("/signedCopyIds","Bản ký phải là file tải lên riêng.");
        }
        changes.manifest(change,evidence.packetFiles(packet),actor);
    }
    private Set<UUID> assessmentFiles(JsonNode packet,JsonNode assessment) {
        var refs=new TreeSet<>(evidence.packetFiles(packet)); for(var check:assessment.path("checks")) refs.add(id(check,"fileId")); for(var check:assessment.path("mandatoryChecks")) refs.addAll(SigningEvidence.uniqueIds(check.path("evidenceFileIds"),"/mandatoryChecks/evidenceFileIds")); return refs;
    }
    private void assessment(EntityStore.Entity change,JsonNode packet,JsonNode assessment,Actor actor) {
        evidence.authority(packet); artifact(change,packet,actor); var checked=new HashSet<UUID>();
        for(var check:assessment.path("checks")) {
            if(!checked.add(id(check,"fileId"))) throw ApiException.validation("/checks","File kiểm tra trùng."); evidence.nonfuture(check.path("checkedAt"));
            if(!check.path("result").asString().equals("PASS")) throw ApiException.of("AUTHORITY_INVALID","Bản ký/artifact chưa PASS.");
        }
        if(!checked.containsAll(SigningEvidence.uniqueIds(packet.path("signedCopyIds"),"/signedCopyIds"))) throw ApiException.of("AUTHORITY_INVALID","Cần kiểm tra mọi bản ký.");
        var codes=new HashSet<String>(); var authorityIds=new HashSet<UUID>();
        for(var check:assessment.path("mandatoryChecks")) {
            if(!codes.add(check.path("code").asString())) throw ApiException.validation("/mandatoryChecks","Mã điều kiện trùng."); evidence.nonfuture(check.path("checkedAt"));
            var refs=SigningEvidence.uniqueIds(check.path("evidenceFileIds"),"/mandatoryChecks/evidenceFileIds");
            if(!check.path("result").asString().equals("PASS")||refs.isEmpty()) throw SigningEvidence.conditionFailure(check.path("code").asString());
            if(check.path("code").asString().equals("AUTHORITY_CURRENT")) authorityIds.addAll(refs);
        }
        for(String side:List.of("bank","beneficiary")) {
            var representation=packet.path("authority").path(side);
            if(!authorityIds.contains(id(representation,"sourceFileId"))||(representation.hasNonNull("checkEvidenceFileId")&&!authorityIds.contains(id(representation,"checkEvidenceFileId")))) throw SigningEvidence.conditionFailure("AUTHORITY_CURRENT");
        }
        var revision=approval.frozen(change).revision(); var beneficiary=packet.path("authority").path("beneficiary");
        for(String field:List.of("basisType","personName","title","scopeText","validFrom","validTo","sourceFileId","issuerReference","knownRevocationAt")) if(!json.nfc(beneficiary.path(field)).equals(json.nfc(revision.path("representation").path(field)))) throw ApiException.of("AUTHORITY_INVALID","Đại diện khác nguồn Change được duyệt.");
        var round=store.require("Round",id(change.body(),"roundId")); for(var check:round.body().path("mandatoryChecks")) if(!codes.contains(check.path("code").asString())) throw SigningEvidence.conditionFailure(check.path("code").asString());
        if(!codes.contains("SERVICE_READINESS")) throw SigningEvidence.conditionFailure("SERVICE_READINESS"); changes.manifest(change,assessmentFiles(packet,assessment),actor);
    }
    private void frozen(EntityStore.Entity change,JsonNode packet) {
        var rows=store.jdbc().query("select input::text,input_hash,file_manifest::text,revision_id from cg_change_signing_input where packet_id=? and change_id=?",
                (rs,n)->List.of(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)),id(packet,"id"),change.id());
        if(rows.size()!=1) throw ApiException.of("STORAGE_INTEGRITY","Thiếu immutable signing input.");
        var row=rows.getFirst(); var input=json.parse(row.get(0));
        if(!json.hash(input).equals(row.get(1))||!row.get(3).equals(approval.frozen(change).revision().path("id").asString())) throw ApiException.of("STORAGE_INTEGRITY","Packet chốt theo revision khác.");
        for(String field:List.of("targetKind","targetId","artifactId","artifactHash","signedCopyIds","signingMethod","authority","bankSignedAt","beneficiarySignedAt")) if(!input.path(field).equals(packet.path(field))) throw ApiException.of("STORAGE_INTEGRITY","Packet source bị thay đổi.");
        if(!changes.manifest(change,evidence.packetFiles(packet),null).equals(json.parse(row.get(2)))) throw ApiException.of("STORAGE_INTEGRITY","Packet evidence bytes bị đổi.");
    }
    public void requireReady(EntityStore.Entity change,Actor actor) {
        if(change.body().path("target").path("kind").asString().equals("TARIFF_NOTICE")) throw ApiException.of("NOTICE_NOT_READY","Cần adapter Notice/acknowledgement thật.");
        if(!change.body().hasNonNull("signingPacketId")) throw ApiException.of("READINESS_BLOCKED","Thiếu packet.");
        var packet=store.require("SigningPacket",id(change.body(),"signingPacketId"));
        if(!packet.body().path("verificationStatus").asString().equals("VERIFIED")||!packet.body().path("targetId").asString().equals(change.id().toString())) throw ApiException.of("READINESS_BLOCKED","Packet không VERIFIED trên Change.");
        var rows=store.jdbc().query("select assessment::text,assessed_at from cg_change_signing_assessment where packet_id=?",(rs,n)->Map.entry(json.parse(rs.getString(1)),rs.getTimestamp(2).toInstant()),packet.id());
        if(rows.size()!=1||!rows.getFirst().getValue().isBefore(calendar.startOfDay(LocalDate.parse(change.body().path("effectiveOn").asString())))) throw ApiException.of("READINESS_BLOCKED","Thiếu assessment đúng hạn.");
        frozen(change,packet.body()); var assessed=rows.getFirst().getKey(); assessment(change,packet.body(),assessed,actor);
        if(!changes.manifest(change,assessmentFiles(packet.body(),assessed),actor).equals(assessed.path("fileManifest"))) throw ApiException.of("STORAGE_INTEGRITY","Assessment evidence đã thay đổi.");
    }
    public JsonNode get(UUID packetId,Actor actor) { var change=source(packetId,actor); lifecycle.maintain(id(change.body(),"contractId"),actor); return project(store.require("SigningPacket",packetId).body(),actor); }
    public JsonNode project(JsonNode raw,Actor actor) {
        var body=(ObjectNode)raw.deepCopy(); var allowed=json.array(); var blocked=json.array();
        if(body.path("verificationStatus").asString().equals("PENDING")&&actor.hasRole("CHECKER")) for(String action:List.of("VERIFY_SIGNING","RETURN_SIGNING")) {
            try { checker(id(body,"id"),actor); var change=source(id(body,"id"),actor); approved(change); if(!change.body().path("signingPacketId").equals(body.path("id"))) throw ApiException.of("STATE_CONFLICT","Packet không còn candidate."); if(action.equals("VERIFY_SIGNING")) changes.beforeBoundary(change); allowed.add(action); }
            catch(ApiException exception) { blocked.add(json.object().put("action",action).put("code",exception.code()).put("reason",exception.getMessage())); }
        }
        if(actor.hasRole("AUDITOR")&&!actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER")&&!actor.hasPermission("SENSITIVE_READ")) { for(String side:List.of("bank","beneficiary")) for(String field:List.of("personName","title","scopeText","issuerReference")) ((ObjectNode)body.path("authority").path(side)).put(field,"[REDACTED]"); if(body.hasNonNull("verificationReason")) body.put("verificationReason","[REDACTED]"); }
        body.set("allowedActions",allowed); body.set("blockedActions",blocked); return body;
    }
}
