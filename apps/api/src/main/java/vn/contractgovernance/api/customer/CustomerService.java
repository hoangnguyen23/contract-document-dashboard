package vn.contractgovernance.api.customer;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.identity.IdentityAdministration;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.AuditService;
import vn.contractgovernance.api.shared.ETags;
import vn.contractgovernance.api.shared.EntityStore;
import vn.contractgovernance.api.shared.JsonSupport;
import vn.contractgovernance.api.shared.ListQueries;

@Service
public class CustomerService {
    private final EntityStore store;
    private final CustomerAccess access;
    private final UserDirectory users;
    private final AuditService audit;
    private final JsonSupport json;
    private final ListQueries lists;
    private final Clock clock;
    public CustomerService(EntityStore store, CustomerAccess access, UserDirectory users,
                           AuditService audit, JsonSupport json, ListQueries lists, Clock clock) {
        this.store = store; this.access = access; this.users = users; this.audit = audit;
        this.json = json; this.lists = lists; this.clock = clock;
    }

    public EntityStore.Entity get(String type, UUID id, Actor actor) {
        return switch (type) {
            case "Party" -> {
                if (!actor.hasRole("BANK_ADMIN")) requirePartyVisible(id, actor);
                yield store.require(type,id);
            }
            case "Relationship" -> access.metadataRelationship(id, actor);
            case "ContactRequest" -> {
                var request = store.scoped(type,id,actor);
                access.requireRelationship(uuid(request.body(),"relationshipId"),actor);
                yield request;
            }
            default -> throw new IllegalArgumentException("Unsupported customer resource");
        };
    }

    public JsonNode list(String type, Map<String,String> query, Actor actor) {
        String where = "true"; var args = new ArrayList<Object>();
        boolean global = type.equals("Party") || (type.equals("Relationship") && actor.hasRole("BANK_ADMIN"));
        if (type.equals("Party")) {
            IdentityAdministration.requireAdmin(actor);
            if (query.containsKey("taxId")) { where += " and e.body->>'taxId'=?"; args.add(query.get("taxId")); }
            if (query.containsKey("name")) { where += " and strpos(lower(e.body->>'name'),lower(?))>0"; args.add(nfc(query.get("name"))); }
        } else {
            if (!actor.hasAnyRole("BANK_ADMIN","MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR"))
                throw ApiException.of("ROLE_FORBIDDEN","Vai trò không được đọc quan hệ khách hàng.");
            if (type.equals("ContactRequest") && !actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR"))
                throw ApiException.of("ROLE_FORBIDDEN","Admin danh mục không có quyền đọc yêu cầu xác nhận liên hệ.");
            for (String field : type.equals("Relationship") ? List.of("partyId","assignedMakerId","active") : List.of("relationshipId","decision")) {
                if (query.containsKey(field)) { where += " and e.body->>'"+field+"'=?"; args.add(query.get(field)); }
            }
            if (global && query.containsKey("branchId")) { where += " and e.branch_id=?::uuid"; args.add(query.get("branchId")); }
            if (!global && !actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")) {
                if (type.equals("Relationship")) {
                    where += " and e.body->>'assignedMakerId'=?"; args.add(actor.id().toString());
                } else {
                    where += " and exists(select 1 from cg_entity r where r.entity_type='Relationship' and r.id=(e.body->>'relationshipId')::uuid and r.branch_id=e.branch_id and r.body->>'assignedMakerId'=?)";
                    args.add(actor.id().toString());
                }
            }
        }
        return lists.page(type, actor, query, global, where, args, body -> representation(
                new EntityStore.Entity(type,uuid(body,"id"),body.has("branchId")?uuid(body,"branchId"):null,body.path("version").asInt(),body), actor));
    }

    public void authorizeCreate(String type, JsonNode input, Actor actor) {
        if (type.equals("ContactRequest")) access.authorizeAssignedMaker(uuid(input,"relationshipId"), actor);
        else IdentityAdministration.requireAdmin(actor);
    }

    public EntityStore.Entity create(String type, JsonNode input, Actor actor) {
        authorizeCreate(type,input,actor);
        var body = base(); UUID id = UUID.randomUUID(), branchId = null;
        String reason = input.path("reason").isMissingNode() ? null : input.path("reason").asString();
        switch (type) {
            case "Party" -> {
                body.put("taxId",input.path("taxId").asString()); body.set("name",json.nfc(input.path("name")));
            }
            case "Relationship" -> {
                branchId = uuid(input,"branchId"); requireActiveBranch(branchId);
                store.require("Party",uuid(input,"partyId"));
                users.requireEligible(uuid(input,"assignedMakerId"),branchId,"MAKER",Set.of());
                copy(input,body,"branchId","partyId","assignedMakerId","confirmedContact");
                body.put("contactVersion",1); body.put("active",true);
            }
            case "ContactRequest" -> {
                var relationship = store.lock("Relationship",uuid(input,"relationshipId"));
                access.requireAssignedMaker(relationship.id(),actor);
                if (input.path("expectedRelationshipVersion").asInt()!=relationship.version())
                    throw ApiException.of("BASE_CONFLICT","Quan hệ đã thay đổi; hãy tạo yêu cầu trên phiên bản hiện tại.");
                branchId=relationship.branchId(); body.put("branchId",branchId.toString());
                copy(input,body,"relationshipId","requestedContact","expectedRelationshipVersion");
                body.put("requesterId",actor.id().toString()); body.putArray("contributors").add(actor.id().toString());
                body.put("decision","PENDING"); body.putNull("reason"); body.putNull("decidedBy");
            }
            default -> throw new IllegalArgumentException("Unsupported customer resource");
        }
        EntityStore.Entity created;
        try { created = store.create(type,id,branchId,body); }
        catch (DuplicateKeyException ex) {
            throw ApiException.of(type.equals("Party")?"DUPLICATE_PARTY":"DUPLICATE_RELATIONSHIP",
                    type.equals("Party")?"Mã số thuế đã thuộc một pháp nhân trong ngân hàng.":"Quan hệ chi nhánh và pháp nhân đã tồn tại.");
        }
        if (type.equals("Relationship")) {
            contactHistory(created,null,actor.id());
            assignmentHistory(created.id(),null,uuid(created.body(),"assignedMakerId"),actor.id(),null);
        }
        audit.record(actor,"CREATE",created,reason);
        return created;
    }

    public EntityStore.Entity update(String type, UUID id, JsonNode input, String ifMatch, Actor actor) {
        IdentityAdministration.requireAdmin(actor); var current=store.lock(type,id); ETags.check(ifMatch,current.version());
        var body=(ObjectNode)current.body().deepCopy();
        if (type.equals("Party")) copy(input,body,"name");
        else if (type.equals("Relationship")) {
            if (input.path("active").asBoolean()) requireActiveBranch(current.branchId());
            UUID oldMaker=uuid(current.body(),"assignedMakerId"),newMaker=uuid(input,"assignedMakerId");
            if(input.path("active").asBoolean() || !oldMaker.equals(newMaker))users.requireEligible(newMaker,current.branchId(),"MAKER",Set.of());
            copy(input,body,"assignedMakerId","active");
            if (!oldMaker.equals(newMaker)) assignmentHistory(id,oldMaker,newMaker,actor.id(),input.path("reason").asString());
        } else throw new IllegalArgumentException("Unsupported customer update");
        body.put("updatedAt",JsonSupport.timestamp(clock.instant()));
        var updated=store.update(current,body); audit.record(actor,"UPDATE",updated,input.path("reason").asString()); return updated;
    }

    public void authorizeDecision(UUID requestId, Actor actor) {
        var request=get("ContactRequest",requestId,actor);
        if (!actor.hasRole("CHECKER")) throw ApiException.of("ROLE_FORBIDDEN","Chỉ checker được quyết định yêu cầu liên hệ.");
        excludeContributor(request.body(),actor.id());
    }

    public EntityStore.Entity decide(UUID requestId,JsonNode input,String ifMatch,Actor actor) {
        authorizeDecision(requestId,actor);
        var before=store.require("ContactRequest",requestId);
        // All decisions serialize on the relationship, then the immutable request.
        var relationship=store.lock("Relationship",uuid(before.body(),"relationshipId"));
        var request=store.lock("ContactRequest",requestId);
        authorizeDecision(requestId,actor); ETags.check(ifMatch,request.version());
        if (!request.body().path("decision").asString().equals("PENDING"))
            throw ApiException.of("STATE_CONFLICT","Yêu cầu liên hệ đã được quyết định.");
        String action=input.path("action").asString();
        if (action.equals("APPROVE")) {
            int expected=input.path("expectedRelationshipVersion").asInt();
            if (expected!=request.body().path("expectedRelationshipVersion").asInt() || expected!=relationship.version())
                throw ApiException.of("BASE_CONFLICT","Phiên bản quan hệ đã thay đổi; yêu cầu cũ không được ghi đè liên hệ hiện tại.");
            if (!relationship.body().path("active").asBoolean()) throw ApiException.of("STATE_CONFLICT","Quan hệ đã ngừng hoạt động.");
            var next=(ObjectNode)relationship.body().deepCopy();
            next.set("confirmedContact",request.body().path("requestedContact").deepCopy());
            next.put("contactVersion",relationship.body().path("contactVersion").asInt()+1);
            next.put("updatedAt",JsonSupport.timestamp(clock.instant()));
            var updated=store.update(relationship,next); contactHistory(updated,requestId,actor.id());
            audit.record(actor,"CONFIRM_CONTACT",updated,input.path("reason").asString());
        } else if (!action.equals("REJECT")) throw ApiException.validation("/action","Command liên hệ không hợp lệ.");
        var next=(ObjectNode)request.body().deepCopy();
        next.put("decision",action.equals("APPROVE")?"APPROVED":"REJECTED"); next.put("reason",nfc(input.path("reason").asString()));
        next.put("decidedBy",actor.id().toString()); next.put("updatedAt",JsonSupport.timestamp(clock.instant()));
        var updated=store.update(request,next); audit.record(actor,action,updated,input.path("reason").asString()); return updated;
    }

    public JsonNode representation(EntityStore.Entity entity,Actor actor) {
        var body=(ObjectNode)entity.body().deepCopy(); var actions=body.putArray("allowedActions"); var blocked=body.putArray("blockedActions");
        if ((entity.type().equals("Party") || entity.type().equals("Relationship")) && actor.hasRole("BANK_ADMIN")) actions.add("UPDATE");
        if (entity.type().equals("ContactRequest") && actor.hasRole("CHECKER")) {
            String code=null,reason=null;
            if (contributors(body).contains(actor.id())) {code="SOD_VIOLATION";reason="Người đóng góp không được duyệt yêu cầu này.";}
            else if (!body.path("decision").asString().equals("PENDING")) {code="STATE_CONFLICT";reason="Yêu cầu đã được quyết định.";}
            var relationship=store.find("Relationship",uuid(body,"relationshipId"));
            for (String action:List.of("APPROVE","REJECT")) {
                String actionCode=code,actionReason=reason;
                if(actionCode==null && action.equals("APPROVE")){
                    if(relationship.isEmpty() || !relationship.get().body().path("active").asBoolean()){actionCode="STATE_CONFLICT";actionReason="Quan hệ không còn hoạt động.";}
                    else if(relationship.get().version()!=body.path("expectedRelationshipVersion").asInt()){actionCode="BASE_CONFLICT";actionReason="Quan hệ đã đổi; yêu cầu cũ không được ghi đè liên hệ hiện tại.";}
                }
                if(actionCode==null) actions.add(action);
                else {var item=json.object();item.put("action",action);item.put("code",actionCode);item.put("reason",actionReason);blocked.add(item);}
            }
        }
        boolean fullBusinessRole=actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","BANK_ADMIN");
        if(!fullBusinessRole && actor.hasRole("MAKER")){
            EntityStore.Entity relationship=entity.type().equals("Relationship")?entity:
                    entity.type().equals("ContactRequest")?store.find("Relationship",uuid(body,"relationshipId")).orElse(null):null;
            fullBusinessRole=relationship!=null && relationship.body().path("assignedMakerId").asString().equals(actor.id().toString());
        }
        if (actor.hasRole("AUDITOR") && !fullBusinessRole && !actor.hasPermission("SENSITIVE_READ")) {
            if(body.has("confirmedContact"))body.set("confirmedContact",redact(body.path("confirmedContact")));
            if(body.has("requestedContact"))body.set("requestedContact",redact(body.path("requestedContact")));
        }
        return body;
    }

    public JsonNode reviewerOptions(String kind,UUID targetId,Actor actor) {
        String type=switch(kind){case "DOSSIER"->"Dossier";case "CHANGE"->"Change";case "CONTROL_REQUEST"->"ControlRequest";default->throw ApiException.validation("/query/targetKind","Target không hợp lệ.");};
        var target=store.scoped(type,targetId,actor); var body=target.body();
        EntityStore.Entity relationship;
        if(type.equals("Dossier")) relationship=access.requireRelationship(uuid(body,"relationshipId"),actor);
        else {
            var contract=store.scoped("Contract",uuid(body,"contractId"),actor);
            UUID relationshipId=contract.body().has("relationshipId")?uuid(contract.body(),"relationshipId"):
                    uuid(store.scoped("Dossier",uuid(contract.body(),"dossierId"),actor).body(),"relationshipId");
            relationship=access.requireRelationship(relationshipId,actor);
        }
        if (!(actor.hasRole("MAKER") && relationship.body().path("assignedMakerId").asString().equals(actor.id().toString()))
                && !actor.hasAnyRole("CHECKER","MANAGER")) throw ApiException.of("ROLE_FORBIDDEN","Bạn không được chọn reviewer cho target này.");
        Set<UUID> excluded=contributors(body);
        for(String field:List.of("requesterId","createdBy","makerId"))if(body.hasNonNull(field))excluded.add(uuid(body,field));
        var ids=store.jdbc().query("select id from cg_entity where entity_type='User' and body->>'active'='true' and body->'branchIds' @> ?::jsonb and (body->'roles' @> '[\"CHECKER\"]'::jsonb or body->'roles' @> '[\"MANAGER\"]'::jsonb) order by body->>'displayName',id",
                (rs,row)->rs.getObject(1,UUID.class),"[\""+target.branchId()+"\"]");
        var output=json.object();var items=output.putArray("items");
        for(UUID id:ids){
            if(excluded.contains(id))continue;
            var user=store.require("User",id); var candidate=users.actor(id);
            if(!candidate.inBranch(target.branchId()))continue;
            for(String role:List.of("CHECKER","MANAGER"))if(candidate.hasRole(role)){
                if(items.size()>=100)break;
                var option=json.object();option.put("userId",id.toString());option.put("displayName",user.body().path("displayName").asString());option.put("level",role.equals("CHECKER")?"L1":"L2");items.add(option);
            }
        }
        output.put("asOf",JsonSupport.timestamp(clock.instant()));return output;
    }

    private void requirePartyVisible(UUID partyId,Actor actor){
        if(!actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR"))throw ApiException.of("ROLE_FORBIDDEN","Bạn không được đọc khách hàng nghiệp vụ.");
        var args=new ArrayList<Object>();args.add(partyId.toString());
        String scope="false";
        if(!actor.branchIds().isEmpty()){scope="branch_id in ("+String.join(",",java.util.Collections.nCopies(actor.branchIds().size(),"?::uuid"))+")";actor.branchIds().forEach(id->args.add(id.toString()));}
        if(!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")){scope+=" and body->>'assignedMakerId'=?";args.add(actor.id().toString());}
        Boolean visible=store.jdbc().queryForObject("select exists(select 1 from cg_entity where entity_type='Relationship' and body->>'partyId'=? and "+scope+")",Boolean.class,args.toArray());
        if(!Boolean.TRUE.equals(visible))throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");
    }
    private void requireActiveBranch(UUID id){if(!store.require("Branch",id).body().path("active").asBoolean())throw ApiException.of("STATE_CONFLICT","Chi nhánh đã ngừng hoạt động.");}
    private void contactHistory(EntityStore.Entity relationship,UUID requestId,UUID checker){store.jdbc().update("insert into cg_customer_contact_history(relationship_id,contact_version,contact,request_id,confirmed_by,confirmed_at) values (?,?,?::jsonb,?,?,?)",relationship.id(),relationship.body().path("contactVersion").asInt(),json.stringify(relationship.body().path("confirmedContact")),requestId,checker,Timestamp.from(clock.instant()));}
    private void assignmentHistory(UUID id,UUID oldMaker,UUID newMaker,UUID actor,String reason){store.jdbc().update("insert into cg_customer_assignment_history(id,relationship_id,old_maker_id,new_maker_id,actor_id,reason,recorded_at) values (?,?,?,?,?,?,?)",UUID.randomUUID(),id,oldMaker,newMaker,actor,reason,Timestamp.from(clock.instant()));}
    private ObjectNode base(){var body=json.object();String now=JsonSupport.timestamp(clock.instant());body.put("createdAt",now);body.put("updatedAt",now);body.putArray("allowedActions");body.putArray("blockedActions");return body;}
    private void copy(JsonNode input,ObjectNode body,String...fields){for(String field:fields)body.set(field,json.nfc(input.path(field)));}
    private ObjectNode redact(JsonNode source){var node=(ObjectNode)source.deepCopy();node.put("personName","[REDACTED]");node.put("roleLabel","[REDACTED]");node.put("email","redacted@example.invalid");node.put("phone","0000000000");return node;}
    private static String nfc(String value){return java.text.Normalizer.normalize(value,java.text.Normalizer.Form.NFC);}
    private static UUID uuid(JsonNode node,String field){return UUID.fromString(node.path(field).asString());}
    private static Set<UUID> contributors(JsonNode node){Set<UUID> ids=new HashSet<>();for(String field:List.of("contributors","contributorIds"))node.path(field).forEach(value->ids.add(UUID.fromString(value.asString())));if(node.hasNonNull("requesterId"))ids.add(uuid(node,"requesterId"));return ids;}
    private static void excludeContributor(JsonNode node,UUID actor){if(contributors(node).contains(actor))throw ApiException.of("SOD_VIOLATION","Người đóng góp không được duyệt yêu cầu của mình.");}
}
