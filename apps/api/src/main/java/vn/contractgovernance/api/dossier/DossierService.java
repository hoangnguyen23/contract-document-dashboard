package vn.contractgovernance.api.dossier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.files.FileService;
import vn.contractgovernance.api.shared.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DossierService {
    private final EntityStore store; private final JsonSupport json; private final Clock clock;
    private final CustomerAccess customers; private final DossierAccess access; private final FileService files;
    private final CommandExecutor commands; private final AuditService audit;
    private final ObjectProvider<DossierApprovalBridge> approval;private final ListQueries lists;
    public DossierService(EntityStore store,JsonSupport json,Clock clock,CustomerAccess customers,DossierAccess access,FileService files,CommandExecutor commands,AuditService audit,ObjectProvider<DossierApprovalBridge> approval,ListQueries lists) {
        this.store=store;this.json=json;this.clock=clock;this.customers=customers;this.access=access;this.files=files;this.commands=commands;this.audit=audit;this.approval=approval;this.lists=lists;
    }
    public ApiResult create(JsonNode request,String key,Actor actor) {
        var relationshipId=UUID.fromString(request.path("relationshipId").asString());
        customers.authorizeAssignedMaker(relationshipId,actor);
        return commands.execute(actor,key,"POST","/api/v1/dossiers",request,null,()->customers.authorizeAssignedMaker(relationshipId,actor),()->{
            var relationship=store.lock("Relationship",relationshipId);customers.requireAssignedMaker(relationshipId,actor);
            var id=UUID.randomUUID();validateDraft(request,id,relationship.branchId(),actor);
            if(request.hasNonNull("linkedFromDossierId")) {
                var source=access.require(UUID.fromString(request.path("linkedFromDossierId").asString()),actor);
                if(!Set.of("REJECTED","CANCELLED").contains(source.body().path("state").asString())) throw ApiException.of("STATE_CONFLICT","Nguồn liên kết phải là hồ sơ đã kết thúc.");
            }
            var body=newDraft(request,relationship,actor);body.set("linkedFromDossierId",request.path("linkedFromDossierId"));
            var result=store.create("Dossier",id,relationship.branchId(),body);audit.record(actor,"CREATE",result,null);
            return ApiResult.created(project(result,actor),"/api/v1/dossiers/"+id);
        });
    }
    public JsonNode get(UUID id,Actor actor) { return project(access.require(id,actor),actor); }
    public JsonNode list(Map<String,String> query,Actor actor){
        actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");var args=new ArrayList<Object>();String where="true";
        if(!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")){where="exists(select 1 from cg_entity r where r.entity_type='Relationship' and r.id=(e.body->>'relationshipId')::uuid and r.branch_id=e.branch_id and r.body->>'assignedMakerId'=?)";args.add(actor.id().toString());}
        return lists.page("Dossier",actor,query,false,where,args,body->project(new EntityStore.Entity("Dossier",UUID.fromString(body.path("id").asString()),UUID.fromString(body.path("branchId").asString()),body.path("version").asInt(),body),actor));
    }
    public ApiResult update(UUID id,JsonNode request,String ifMatch,String key,Actor actor) {
        access.authorizeMaker(id,actor);ETags.require(ifMatch);
        return commands.execute(actor,key,"PUT","/api/v1/dossiers/"+id,request,ifMatch,()->access.authorizeMaker(id,actor),()->{
            var current=access.lockMaker(id,actor);ETags.check(ifMatch,current.version());access.requireEditable(current);
            validateDraft(request,id,current.branchId(),actor);
            var body=(ObjectNode)current.body().deepCopy();copyMutable(body,request);contribute(body,actor.id());body.put("updatedAt",JsonSupport.timestamp(clock.instant()));
            var next=store.update(current,body);files.synchronizeDraftReferences("DOSSIER",id,current.branchId(),referenceIds(body),actor);audit.record(actor,"UPDATE",next,null);
            return ApiResult.ok(project(next,actor));
        });
    }
    public ApiResult command(UUID id,JsonNode request,String ifMatch,String key,Actor actor) {
        access.authorizeMaker(id,actor);ETags.require(ifMatch);
        return commands.execute(actor,key,"POST","/api/v1/dossiers/"+id+"/commands",request,ifMatch,()->access.authorizeMaker(id,actor),()->{
            var current=access.lockMaker(id,actor);ETags.check(ifMatch,current.version());var action=request.path("action").asString();
            if(action.equals("CLONE")) return cloneDossier(current,request,actor);
            EntityStore.Entity next;
            if(action.equals("CANCEL")) {
                access.requireEditable(current);var body=(ObjectNode)current.body().deepCopy();body.put("state","CANCELLED");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));next=store.update(current,body);
            } else if(action.equals("SUBMIT") || action.equals("WITHDRAW")) {
                var bridge=approval.getIfAvailable();
                if(bridge==null) throw ApiException.of("SERVICE_UNAVAILABLE","Module xét duyệt chưa được triển khai; hồ sơ được giữ nguyên.");
                if(action.equals("SUBMIT")) {access.requireEditable(current);next=bridge.submit(current,request,actor);}
                else {if(!current.body().path("state").asString().equals("IN_REVIEW"))throw ApiException.of("STATE_CONFLICT","Hồ sơ không có vòng xét duyệt đang mở.");next=bridge.withdraw(current,request.path("reason").asString(),actor);}
            } else throw ApiException.validation("/action","Thao tác hồ sơ không hợp lệ.");
            audit.record(actor,action,next,request.hasNonNull("reason")?request.path("reason").asString():null);return ApiResult.ok(project(next,actor));
        });
    }
    private ApiResult cloneDossier(EntityStore.Entity source,JsonNode request,Actor actor) {
        if(!Set.of("REJECTED","CANCELLED").contains(source.body().path("state").asString())) throw ApiException.of("STATE_CONFLICT","Chỉ clone hồ sơ bị từ chối hoặc hủy.");
        var relationship=customers.requireAssignedMaker(UUID.fromString(request.path("relationshipId").asString()),actor);
        var sourceRelationship=store.require("Relationship",UUID.fromString(source.body().path("relationshipId").asString()));
        if(!relationship.body().path("partyId").equals(sourceRelationship.body().path("partyId"))) throw ApiException.validation("/relationshipId","Clone phải thuộc cùng pháp nhân; hồ sơ khác pháp nhân phải tạo nháp mới.");
        var id=UUID.randomUUID();var body=newDraft(source.body(),relationship,actor);body.put("linkedFromDossierId",source.id().toString());
        // Parent must exist before new scoped associations. Clone shares immutable bytes internally only.
        body.set("documents",json.array());var representation=(ObjectNode)body.path("representation");representation.putNull("sourceFileId");representation.putNull("checkEvidenceFileId");representation.putNull("checkedAt");
        var draft=store.create("Dossier",id,relationship.branchId(),body);
        var mapping=files.cloneEvidence(source,id,relationship.branchId(),referenceIds(source.body()),actor);
        var documents=json.array();for(var link:source.body().path("documents")){var copy=(ObjectNode)link.deepCopy();copy.put("fileId",mapping.get(UUID.fromString(link.path("fileId").asString())).toString());documents.add(copy);}body.set("documents",documents);
        if(source.body().path("representation").hasNonNull("sourceFileId"))representation.put("sourceFileId",mapping.get(UUID.fromString(source.body().path("representation").path("sourceFileId").asString())).toString());
        var next=store.update(draft,body);files.synchronizeDraftReferences("DOSSIER",id,relationship.branchId(),referenceIds(body),actor);audit.record(actor,"CLONE",next,request.path("reason").asString());
        return ApiResult.created(project(next,actor),"/api/v1/dossiers/"+id);
    }
    private ObjectNode newDraft(JsonNode request,EntityStore.Entity relationship,Actor actor) {
        var body=json.object();var now=JsonSupport.timestamp(clock.instant());body.put("createdAt",now);body.put("updatedAt",now);body.put("branchId",relationship.branchId().toString());body.put("state","DRAFT");body.put("relationshipId",relationship.id().toString());body.put("assignedMakerId",relationship.body().path("assignedMakerId").asString());body.putNull("linkedFromDossierId");
        copyMutable(body,request);body.set("contactSnapshot",relationship.body().path("confirmedContact").deepCopy());body.put("templateVersion","BASE-VA-1");body.put("bundleVersion","TERMS-VA-1");body.set("contributorIds",json.array().add(actor.id().toString()));body.putNull("revision");body.putNull("roundId");body.putNull("contractId");body.set("mandatoryChecks",json.array());body.set("allowedActions",json.array());body.set("blockedActions",json.array());return body;
    }
    private void copyMutable(ObjectNode body,JsonNode request) { for(var field:java.util.List.of("terms","representation","documents","externalAccountReference"))body.set(field,json.nfc(request.path(field))); }
    private void validateDraft(JsonNode request,UUID id,UUID branchId,Actor actor) {
        var terms=request.path("terms");var start=LocalDate.parse(terms.path("startOn").asString());
        if(terms.path("termKind").asString().equals("FIXED")) {
            if(!terms.hasNonNull("endExclusive") || !LocalDate.parse(terms.path("endExclusive").asString()).isAfter(start))throw ApiException.validation("/terms/endExclusive","Hạn kết thúc phải sau ngày bắt đầu.");
        } else if(terms.hasNonNull("endExclusive")) throw ApiException.validation("/terms/endExclusive","Thời hạn không xác định phải có endExclusive=null.");
        var expectedClause=terms.path("pricingMode").asString().equals("STANDARD_TARIFF")?"TARIFF-ACK-7D-1":"NEGOTIATED-VA-1";
        if(!expectedClause.equals(terms.path("clauseVersion").asString()))throw ApiException.validation("/terms/clauseVersion","Điều khoản phí không khớp cơ chế định giá.");
        var representation=request.path("representation");
        if(representation.hasNonNull("validTo") && !LocalDate.parse(representation.path("validTo").asString()).isAfter(LocalDate.parse(representation.path("validFrom").asString())))throw ApiException.validation("/representation/validTo","Hạn thẩm quyền phải sau ngày bắt đầu.");
        var unique=new HashSet<UUID>();
        for(var link:request.path("documents")) {
            var fileId=UUID.fromString(link.path("fileId").asString());if(!unique.add(fileId))throw ApiException.validation("/documents","File IDs không được trùng.");
            var file=files.requireClean(fileId,"DOSSIER",id,branchId,actor);
            if(!file.body().path("purpose").asString().equals(link.path("purpose").asString()))throw ApiException.validation("/documents","Purpose phải khớp metadata của tệp.");
        }
        for(var field:java.util.List.of("sourceFileId","checkEvidenceFileId"))if(representation.hasNonNull(field))files.requireClean(UUID.fromString(representation.path(field).asString()),"DOSSIER",id,branchId,actor);
        files.validateDraftLimits("DOSSIER",id,referenceIds(request));
    }
    public static Set<UUID> referenceIds(JsonNode body) {
        var ids=new java.util.LinkedHashSet<UUID>();for(var link:body.path("documents"))ids.add(UUID.fromString(link.path("fileId").asString()));
        for(var field:java.util.List.of("sourceFileId","checkEvidenceFileId"))if(body.path("representation").hasNonNull(field))ids.add(UUID.fromString(body.path("representation").path(field).asString()));
        return ids;
    }
    public static void contribute(ObjectNode body,UUID actorId) {
        var contributors=body.withArray("contributorIds");boolean present=false;for(var id:contributors)if(id.asString().equals(actorId.toString()))present=true;if(!present)contributors.add(actorId.toString());
    }
    public JsonNode project(EntityStore.Entity dossier,Actor actor) {
        var body=(ObjectNode)dossier.body().deepCopy();var allowed=json.array();var blocked=json.array();
        var relationship=store.require("Relationship",UUID.fromString(body.path("relationshipId").asString()));
        boolean maker=actor.hasRole("MAKER") && actor.id().toString().equals(relationship.body().path("assignedMakerId").asString()) && relationship.body().path("active").asBoolean();
        var state=body.path("state").asString();if(maker && Set.of("DRAFT","RETURNED").contains(state)){
            allowed.add("UPDATE").add("CANCEL");
            try {
                var bridge=approval.getIfAvailable();
                if(bridge==null)throw ApiException.of("SERVICE_UNAVAILABLE","Module xét duyệt chưa triển khai.");
                bridge.requireSubmissionReady(dossier,actor);allowed.add("SUBMIT");
            } catch(ApiException blocker){blocked.add(json.object().put("action","SUBMIT").put("code",blocker.code()).put("reason",blocker.getMessage()));}
        }
        if(maker && Set.of("REJECTED","CANCELLED").contains(state))allowed.add("CLONE");
        if(maker && state.equals("IN_REVIEW")){if(approval.getIfAvailable()!=null)allowed.add("WITHDRAW");else blocked.add(json.object().put("action","WITHDRAW").put("code","SERVICE_UNAVAILABLE").put("reason","Module xét duyệt chưa triển khai."));}
        body.set("allowedActions",allowed);body.set("blockedActions",blocked);
        boolean fullBusinessRole=actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER") || (actor.hasRole("MAKER") && actor.id().toString().equals(relationship.body().path("assignedMakerId").asString()));
        if(actor.hasRole("AUDITOR") && !actor.hasPermission("SENSITIVE_READ") && !fullBusinessRole) {
            var r=(ObjectNode)body.path("representation");for(var field:java.util.List.of("personName","title","scopeText"))r.put(field,"[REDACTED]");
            var c=(ObjectNode)body.path("contactSnapshot");c.put("personName","[REDACTED]");c.put("roleLabel","[REDACTED]");c.put("email","redacted@example.invalid");c.put("phone","0000000000");
            if(body.hasNonNull("revision")) {
                var revision=(ObjectNode)body.path("revision");var rr=(ObjectNode)revision.path("representation");
                for(var field:java.util.List.of("personName","title","scopeText"))rr.put(field,"[REDACTED]");
                var rc=(ObjectNode)revision.path("contact");rc.put("personName","[REDACTED]");rc.put("roleLabel","[REDACTED]");rc.put("email","redacted@example.invalid");rc.put("phone","0000000000");
            }
        }
        return body;
    }
}
