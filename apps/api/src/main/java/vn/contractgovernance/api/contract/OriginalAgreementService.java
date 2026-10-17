package vn.contractgovernance.api.contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.approval.*;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.customer.CustomerAccess;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.*;
@Service
public class OriginalAgreementService implements OriginalAgreementPort {
 private final EntityStore store;private final JsonSupport json;private final Clock clock;private final JobQueue jobs;private final AuditService audit;private final AgreementRenderer renderer;private final CustomerAccess customers;private final ListQueries lists;private final ContractRegistry schemas;
 public OriginalAgreementService(EntityStore store,JsonSupport json,Clock clock,JobQueue jobs,AuditService audit,AgreementRenderer renderer,CustomerAccess customers,ListQueries lists,ContractRegistry schemas){this.store=store;this.json=json;this.clock=clock;this.jobs=jobs;this.audit=audit;this.renderer=renderer;this.customers=customers;this.lists=lists;this.schemas=schemas;}
 @Transactional(propagation=Propagation.MANDATORY)
 public EntityStore.Entity prepare(EntityStore.Entity dossier,RevisionStore.Frozen frozen,EntityStore.Entity round,Actor actor){
  var revision=frozen.revision();if(!dossier.type().equals("Dossier")||!dossier.body().path("state").asString().equals("IN_REVIEW")||!round.body().path("state").asString().equals("APPROVED")||!round.body().path("targetId").asString().equals(dossier.id().toString())||!round.body().path("revisionId").asString().equals(revision.path("id").asString()))throw ApiException.of("STATE_CONFLICT","Nguồn phê duyệt không khớp.");
  if(!frozen.metadata().path("templateVersion").asString().equals("BASE-VA-1")||!frozen.metadata().path("bundleVersion").asString().equals("TERMS-VA-1"))throw ApiException.of("POLICY_BLOCKED","Không hỗ trợ mẫu đã chốt.");
  if(store.jdbc().queryForObject("select count(*) from cg_original_agreement where dossier_id=?",Long.class,dossier.id())!=0)throw ApiException.of("STATE_CONFLICT","Hồ sơ đã có hợp đồng gốc.");
  var id=UUID.randomUUID();var now=JsonSupport.timestamp(clock.instant());var source=json.object().put("dossierId",dossier.id().toString()).put("roundId",round.id().toString()).put("generatedAt",now);source.set("revision",revision.deepCopy());source.set("metadata",frozen.metadata().deepCopy());source.set("approvedChecks",round.body().path("mandatoryChecks").deepCopy());source.set("manifest",renderer.manifest());var hash=json.hash(source);
  var job=jobs.enqueue("ARTIFACT_PDF","CONTRACT",id,dossier.branchId(),1,hash,"ARTIFACT_PDF:ORIGINAL:"+revision.path("id").asString()+":BASE-VA-1",json.object(),AuditService.traceId());
  var b=json.object().put("createdAt",now).put("updatedAt",now).put("branchId",dossier.branchId().toString()).put("state","PREPARED").put("dossierId",dossier.id().toString()).put("relationshipId",dossier.body().path("relationshipId").asString()).put("assignedMakerId",dossier.body().path("assignedMakerId").asString()).put("scheduleVersion",1).put("artifactJobId",job.toString()).put("readiness","PENDING").put("serviceState","NOT_STARTED").put("approvedRevisionId",revision.path("id").asString());
  for(var field:List.of("currentVersion","scheduledChangeId","signingPacketId","terminationEffectOn","terminationObservedAt","approvedArtifactId","approvedArtifactHash","readinessVerifiedAt","effectiveAt","recordedAt"))b.putNull(field);
  b.set("allowedActions",json.array());b.set("blockedActions",json.array());b.set("readinessRisks",json.array());b.set("mandatoryChecks",round.body().path("mandatoryChecks").deepCopy());b.set("approvedTerms",revision.path("terms").deepCopy());
  var contract=store.create("Contract",id,dossier.branchId(),b);schemas.validateSchema("Contract",contract.body());
  store.jdbc().update("insert into cg_original_agreement(contract_id,dossier_id,revision_id,source,source_hash,job_id,created_at) values(?,?,?,?::jsonb,?,?,?)",id,dossier.id(),UUID.fromString(revision.path("id").asString()),json.stringify(source),hash,job,Timestamp.from(clock.instant()));audit.record(actor,"CREATE",contract,null);return contract;
 }
 public EntityStore.Entity require(UUID id,Actor actor){var c=store.require("Contract",id);actor.requireBranch(c.branchId());customers.requireRelationship(UUID.fromString(c.body().path("relationshipId").asString()),actor);return c;}
 public JsonNode get(UUID id,Actor actor){return project(require(id,actor).body(),actor);}
 public JsonNode list(Map<String,String> query,Actor actor){actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");var args=new ArrayList<Object>();String where="exists(select 1 from cg_entity r where r.entity_type='Relationship' and r.id=(e.body->>'relationshipId')::uuid and r.branch_id=e.branch_id and "+customers.businessSqlScope(actor,"r")+")";if(!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")&&actor.hasRole("MAKER"))args.add(actor.id().toString());if(Boolean.parseBoolean(query.getOrDefault("expiring30","false"))){var today=clock.instant().atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();where+=" and e.body->>'state'='ACTIVE' and e.body->'currentVersion'->'terms'->>'termKind'='FIXED' and (e.body->'currentVersion'->'terms'->>'endExclusive')::date>?::date and (e.body->'currentVersion'->'terms'->>'endExclusive')::date<=?::date";args.add(today.toString());args.add(today.plusDays(30).toString());}return lists.page("Contract",actor,query,false,where,args,b->project(b,actor));}
 public JsonNode project(JsonNode raw,Actor actor){var b=(ObjectNode)raw.deepCopy();b.set("allowedActions",json.array());var blocked=json.array();if(actor.hasAnyRole("MAKER","CHECKER"))for(String action:List.of("CANCEL","HOLD_READINESS","RELEASE_READINESS"))blocked.add(json.object().put("action",action).put("code","SERVICE_UNAVAILABLE").put("reason","Quy trình ký và quản lý sẵn sàng chưa triển khai."));b.set("blockedActions",blocked);return b;}
 public record Source(JsonNode body,String hash,UUID jobId){}
 public Source source(UUID id){return store.jdbc().query("select source::text,source_hash,job_id from cg_original_agreement where contract_id=?",(rs,n)->new Source(json.parse(rs.getString(1)),rs.getString(2),rs.getObject(3,UUID.class)),id).stream().findFirst().orElseThrow(()->ApiException.of("STORAGE_INTEGRITY","Thiếu nguồn hợp đồng gốc."));}
}
