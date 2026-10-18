package vn.contractgovernance.api.obligation;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.signing.SigningEvidence;

/** Exact parent/ancestor evidence and immutable event authority shared by duties and incidents. */
@Service
public class DutyEvidence {
 public final EntityStore store; public final JsonSupport json; public final Clock clock;
 private final FileService files; private final EvidenceAccess access; private final CustomerAccess customers; private final UserDirectory users;
 public DutyEvidence(EntityStore store,JsonSupport json,Clock clock,FileService files,EvidenceAccess access,CustomerAccess customers,UserDirectory users){this.store=store;this.json=json;this.clock=clock;this.files=files;this.access=access;this.customers=customers;this.users=users;}
 public static UUID id(JsonNode n,String f){return UUID.fromString(n.path(f).asString());}
 public Actor current(Actor a){return users.actor(a.id());}
 public EntityStore.Entity contract(UUID id,Actor a){a=current(a);a.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");var c=store.require("Contract",id);a.requireBranch(c.branchId());customers.requireRelationship(id(c.body(),"relationshipId"),a);return c;}
 public EntityStore.Entity require(String type,UUID id,Actor a){var e=store.require(type,id);var c=contract(id(e.body(),"contractId"),a);if(!Objects.equals(c.branchId(),e.branchId()))throw ApiException.of("NOT_FOUND","Sai nguồn chi nhánh.");return e;}
 public EntityStore.Entity lock(String type,UUID id){var e=store.require(type,id);lockContract(id(e.body(),"contractId"));return store.lock(type,id);}
 public EntityStore.Entity lockContract(UUID id){var c=store.require("Contract",id);store.lock("Dossier",id(c.body(),"dossierId"));return store.lock("Contract",id);}
 public void maker(EntityStore.Entity e,Actor a){var c=contract(id(e.body(),"contractId"),a);customers.authorizeAssignedMaker(id(c.body(),"relationshipId"),current(a));}
 public Set<UUID> contributors(EntityStore.Entity e){var ids=new TreeSet<UUID>();e.body().path("contributorIds").forEach(v->ids.add(UUID.fromString(v.asString())));return ids;}
 private String camel(String type){return type.equals("Obligation")?"OBLIGATION":"INCIDENT";}
 public void independent(EntityStore.Entity e,Actor a,String role){a=current(a);require(e.type(),e.id(),a);a.requireRole(role);if(contributors(e).contains(a.id()))throw ApiException.of("SOD_VIOLATION","Cần reviewer độc lập với mọi người góp nguồn.");}
 public void contribute(EntityStore.Entity e,ObjectNode body,Collection<UUID> ids){var all=contributors(e);body.path("contributorIds").forEach(value->all.add(UUID.fromString(value.asString())));all.addAll(ids);if(all.size()>50)throw ApiException.of("STATE_CONFLICT","Nguồn đã đủ 50 contributors.");body.set("contributorIds",json.value(all.stream().map(UUID::toString).toList()));}
 public Set<UUID> uploaders(JsonNode manifest){var ids=new TreeSet<UUID>();for(var item:manifest){var file=store.require("File",id(item,"fileId"));if(file.body().hasNonNull("uploadedBy"))ids.add(id(file.body(),"uploadedBy"));}return ids;}
 public JsonNode manifest(EntityStore.Entity e,Collection<UUID> ids,Actor actor,boolean attach){var rows=new ArrayList<JsonNode>();var contract=id(e.body(),"contractId");for(var fileId:new TreeSet<>(ids)){var f=attach?store.lock("File",fileId):store.require("File",fileId);String kind=f.body().path("targetKind").asString();boolean own=kind.equals(camel(e.type()))&&f.body().path("targetId").asString().equals(e.id().toString());boolean ancestor=kind.equals("CONTRACT")&&f.body().path("targetId").asString().equals(contract.toString());boolean bound=store.jdbc().queryForObject("select count(*) from cg_file_association where file_id=? and target_type=? and target_id=? and historical",Long.class,fileId,camel(e.type()),e.id())>0;if((!own&&!ancestor&&!bound)||!Objects.equals(f.branchId(),e.branchId()))throw ApiException.of("NOT_FOUND","Bằng chứng phải thuộc đúng nguồn hoặc ancestor Contract.");if(!f.body().path("state").asString().equals("CLEAN"))throw ApiException.of("FILE_NOT_CLEAN","Chưa có bytes CLEAN.");if(actor!=null){access.requireFile(fileId,current(actor));access.requireFull(f,current(actor));}var hash=f.body().path("sha256").asString();if(!files.blob(fileId).hash().equals(hash))throw ApiException.of("STORAGE_INTEGRITY","Sai hash bytes.");files.bytesForScan(fileId,hash);rows.add(json.object().put("fileId",fileId.toString()).put("purpose",f.body().path("purpose").asString()).put("sha256",hash));if(attach){store.jdbc().update("insert into cg_file_association(file_id,target_type,target_id,historical,created_at) values(?,?,?,true,?) on conflict(file_id,target_type,target_id) do update set historical=true",fileId,camel(e.type()),e.id(),Timestamp.from(clock.instant()));if(!f.body().path("attached").asBoolean()){var b=(ObjectNode)f.body().deepCopy();b.put("attached",true).put("updatedAt",now());store.update(f,b);}}}rows.sort(Comparator.comparing((JsonNode n)->n.path("purpose").asString()).thenComparing(n->n.path("fileId").asString()));var out=json.array();rows.forEach(out::add);return out;}
 public Set<UUID> ids(JsonNode n){return SigningEvidence.uniqueIds(n,"/evidenceFileIds");}
 public void nonfuture(JsonNode n){if(Instant.parse(n.asString()).isAfter(clock.instant()))throw ApiException.validation("/timestamp","Không nhận thời điểm tương lai.");}
 public JsonNode receipt(EntityStore.Entity e,JsonNode receipt,Actor a,boolean attach){nonfuture(receipt.path("observedAt"));return manifest(e,ids(receipt.path("fileIds")),a,attach);}
 public UUID event(EntityStore.Entity e,String kind,JsonNode body,Actor a){var eid=UUID.randomUUID();var n=json.nfc(body);store.jdbc().update("insert into cg_g5_event(id,target_type,target_id,kind,body,body_hash,actor_id,recorded_at) values(?,?,?,?,?::jsonb,?,?,?)",eid,e.type(),e.id(),kind,json.stringify(n),json.hash(n),a==null?null:a.id(),Timestamp.from(clock.instant()));return eid;}
 public JsonNode latest(EntityStore.Entity e,String kind){return store.jdbc().query("select body::text,body_hash from cg_g5_event where target_type=? and target_id=? and kind=? order by ordinal desc limit 1",(rs,n)->{var b=json.parse(rs.getString(1));if(!json.hash(b).equals(rs.getString(2)))throw ApiException.of("STORAGE_INTEGRITY","Nguồn lịch sử sai hash.");return b;},e.type(),e.id(),kind).stream().findFirst().orElseThrow(()->ApiException.of("STATE_CONFLICT","Thiếu nguồn lịch sử đã chốt."));}
 public String scope(Actor a,List<Object> args){a=current(a);a.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");String sql="exists(select 1 from cg_entity c join cg_entity r on r.entity_type='Relationship' and r.id=(c.body->>'relationshipId')::uuid and r.branch_id=c.branch_id where c.entity_type='Contract' and c.id=(e.body->>'contractId')::uuid and c.branch_id=e.branch_id";if(!a.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")){sql+=" and r.body->>'assignedMakerId'=?";args.add(a.id().toString());}return sql+")";}
 public boolean redacted(Actor a){return a.hasRole("AUDITOR")&&!a.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER")&&!a.hasPermission("SENSITIVE_READ");}
 public String now(){return JsonSupport.timestamp(clock.instant());}
}
