package vn.contractgovernance.api.files;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.dossier.DossierAccess;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A file never has an independent role or scope grant: authorization comes from its real parent. */
@Component
public class EvidenceAccess {
    private static final Map<String,String> TYPES = Map.of("DOSSIER","Dossier","CONTRACT","Contract","CHANGE","Change","RELATIONSHIP","Relationship","INCIDENT","Incident","OBLIGATION","Obligation","CONTROL_REQUEST","ControlRequest","DATA_REQUEST","DataRequest","NOTICE","Notice","POLICY","Policy");
    private final EntityStore store;
    private final DossierAccess dossiers;
    private final CustomerAccess customers;
    public EvidenceAccess(EntityStore store, DossierAccess dossiers, CustomerAccess customers) { this.store=store; this.dossiers=dossiers; this.customers=customers; }
    public EntityStore.Entity parent(String targetKind, UUID targetId, Actor actor) {
        var type=TYPES.get(targetKind);
        if(type==null) throw ApiException.validation("/targetKind","Loại parent không hợp lệ.");
        var parent=store.require(type,targetId);
        if(targetKind.equals("POLICY")) { actor.requireRole("POLICY_AUTHOR","POLICY_REVIEWER"); return parent; }
        actor.requireBranch(parent.branchId());
        actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");
        if(type.equals("Dossier")) return dossiers.check(parent,actor);
        if(type.equals("Relationship")) return customers.requireRelationship(targetId,actor);
        var body=parent.body();
        if(body.hasNonNull("relationshipId")) customers.requireRelationship(UUID.fromString(body.path("relationshipId").asString()),actor);
        else if(body.hasNonNull("contractId")) parent("CONTRACT",UUID.fromString(body.path("contractId").asString()),actor);
        else if(actor.hasRole("MAKER") && !actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR"))
            throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");
        return parent;
    }
    public EntityStore.Entity requireFile(UUID id, Actor actor) {
        var file=store.require("File",id); var body=file.body();
        var parent=parent(body.path("targetKind").asString(),UUID.fromString(body.path("targetId").asString()),actor);
        if(!java.util.Objects.equals(parent.branchId(),file.branchId())) throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");
        return file;
    }
    public EntityStore.Entity requireWritableParent(String kind,UUID id,Actor actor) {
        var parent=authorizeWriter(kind,id,actor);
        if(kind.equals("POLICY")) {
            actor.requireRole("POLICY_AUTHOR");
            if(!parent.body().path("state").asString().equals("DRAFT")) throw ApiException.of("STATE_CONFLICT","Policy đã đóng nguồn.");
        } else if(kind.equals("DOSSIER")) { dossiers.requireMaker(id,actor); dossiers.requireEditable(parent); }
        else {
            actor.requireRole("MAKER","CHECKER","DATA_OFFICER");
            if(actor.hasRole("MAKER") && !actor.hasAnyRole("CHECKER","DATA_OFFICER")) {
                var body=parent.body();
                if(body.hasNonNull("relationshipId")) customers.requireAssignedMaker(UUID.fromString(body.path("relationshipId").asString()),actor);
                else if(body.hasNonNull("contractId")) {
                    var contract=parent("CONTRACT",UUID.fromString(body.path("contractId").asString()),actor);
                    customers.requireAssignedMaker(UUID.fromString(contract.body().path("relationshipId").asString()),actor);
                }
            }
            if(Set.of("CANCELLED","REJECTED","COMPLETED","EXECUTED","DISPOSED","CLOSED").contains(parent.body().path("state").asString()))
                throw ApiException.of("STATE_CONFLICT","Nguồn đã đóng, không nhận thêm evidence.");
        }
        return parent;
    }
    public EntityStore.Entity authorizeWriter(String kind,UUID id,Actor actor){
        var parent=parent(kind,id,actor);
        if(kind.equals("POLICY")){actor.requireRole("POLICY_AUTHOR");return parent;}
        if(kind.equals("DOSSIER")){dossiers.authorizeMaker(id,actor);return parent;}
        actor.requireRole("MAKER","CHECKER","DATA_OFFICER");
        if(actor.hasRole("MAKER") && !actor.hasAnyRole("CHECKER","DATA_OFFICER")){
            var body=parent.body();if(body.hasNonNull("relationshipId"))customers.authorizeAssignedMaker(UUID.fromString(body.path("relationshipId").asString()),actor);
            else if(body.hasNonNull("contractId")){var contract=parent("CONTRACT",UUID.fromString(body.path("contractId").asString()),actor);customers.authorizeAssignedMaker(UUID.fromString(contract.body().path("relationshipId").asString()),actor);}
        }
        return parent;
    }
    public EntityStore.Entity lockParent(JsonNode file,Actor actor){var parent=parent(file.path("targetKind").asString(),UUID.fromString(file.path("targetId").asString()),actor);return store.lock(parent.type(),parent.id());}
    public EntityStore.Entity requireClean(UUID fileId,String kind,UUID parentId,UUID branchId,Actor actor) {
        var file=requireFile(fileId,actor);
        if(!kind.equals(file.body().path("targetKind").asString()) || !parentId.toString().equals(file.body().path("targetId").asString()) || !java.util.Objects.equals(branchId,file.branchId()))
            throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");
        if(!file.body().path("state").asString().equals("CLEAN")) throw ApiException.of("FILE_NOT_CLEAN","Bằng chứng chưa được scanner xác nhận an toàn.");
        return file;
    }
    public void requireFull(EntityStore.Entity file,Actor actor) {
        boolean redactedAuditor=actor.hasRole("AUDITOR") && !fullBusinessFacts(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);
        if((file.body().path("sensitive").asBoolean() || redactedAuditor) && !actor.hasPermission("SENSITIVE_READ")) throw ApiException.of("SENSITIVE_FORBIDDEN","Tải bản đầy đủ cần quyền đọc dữ liệu nhạy cảm.");
    }
    public boolean fullBusinessFacts(String kind,UUID id,Actor actor){if(actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER"))return true;if(!actor.hasRole("MAKER"))return false;var p=parent(kind,id,actor);if(kind.equals("RELATIONSHIP"))return actor.id().toString().equals(p.body().path("assignedMakerId").asString());if(p.body().hasNonNull("relationshipId")){var r=store.require("Relationship",UUID.fromString(p.body().path("relationshipId").asString()));return actor.id().toString().equals(r.body().path("assignedMakerId").asString());}if(p.body().hasNonNull("contractId"))return fullBusinessFacts("CONTRACT",UUID.fromString(p.body().path("contractId").asString()),actor);return false;}
    /** Bind before evidence is frozen; this reference is retained even when a later draft replaces it. */
    public void freezeReferences(String kind, UUID parentId, Iterable<UUID> ids, Actor actor) {
        var parent=parent(kind,parentId,actor);
        for(var id:ids) {
            requireClean(id,kind,parentId,parent.branchId(),actor);
            store.jdbc().update("update cg_file_association set historical=true where file_id=? and target_type=? and target_id=?",id,kind,parentId);
        }
    }
    public JsonNode metadata(EntityStore.Entity file,Actor actor) { return file.body().deepCopy(); }
    /** SQL scope before pagination; files and jobs follow the same polymorphic parent ancestry. */
    public String sqlScope(boolean job,Actor actor,java.util.List<Object> args) {
        if(!actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR","POLICY_AUTHOR","POLICY_REVIEWER"))throw ApiException.of("ROLE_FORBIDDEN","Vai trò không được đọc evidence hoặc job nghiệp vụ.");
        String kind=job?"e.body->>'parentKind'":"e.body->>'targetKind'";
        String id=job?"e.body->>'parentId'":"e.body->>'targetId'";
        args.add(actor.hasAnyRole("POLICY_AUTHOR","POLICY_REVIEWER"));
        args.add("{"+String.join(",",actor.branchIds().stream().map(UUID::toString).sorted().toList())+"}");
        args.add(actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR"));
        args.add(actor.hasRole("MAKER"));args.add(actor.id().toString());
        String typeCase="case "+kind+" when 'FILE' then 'File' when 'DOSSIER' then 'Dossier' when 'CONTRACT' then 'Contract' when 'CHANGE' then 'Change' when 'RELATIONSHIP' then 'Relationship' when 'INCIDENT' then 'Incident' when 'OBLIGATION' then 'Obligation' when 'CONTROL_REQUEST' then 'ControlRequest' when 'DATA_REQUEST' then 'DataRequest' when 'NOTICE' then 'Notice' when 'POLICY' then 'Policy' end";
        return "exists(with recursive ancestors as (select p.entity_type,p.id,p.branch_id,p.body,1 as depth from cg_entity p where p.entity_type="+typeCase+" and p.id=("+id+")::uuid union all select p.entity_type,p.id,p.branch_id,p.body,a.depth+1 from ancestors a join cg_entity p on p.id=case when a.entity_type='File' then (a.body->>'targetId')::uuid when a.body ? 'relationshipId' then (a.body->>'relationshipId')::uuid when a.body ? 'contractId' then (a.body->>'contractId')::uuid end and p.entity_type=case when a.entity_type='File' then case a.body->>'targetKind' when 'DOSSIER' then 'Dossier' when 'CONTRACT' then 'Contract' when 'CHANGE' then 'Change' when 'RELATIONSHIP' then 'Relationship' when 'INCIDENT' then 'Incident' when 'OBLIGATION' then 'Obligation' when 'CONTROL_REQUEST' then 'ControlRequest' when 'DATA_REQUEST' then 'DataRequest' when 'NOTICE' then 'Notice' when 'POLICY' then 'Policy' end when a.body ? 'relationshipId' then 'Relationship' when a.body ? 'contractId' then 'Contract' end where a.depth<5) select 1 from ancestors p where p.entity_type<>'File' and p.branch_id is not distinct from e.branch_id and ((p.entity_type='Policy' and p.branch_id is null and ?::boolean) or (p.branch_id=any(?::uuid[]) and (?::boolean or (p.entity_type='Relationship' and ?::boolean and p.body->>'assignedMakerId'=?)))))";
    }
}

