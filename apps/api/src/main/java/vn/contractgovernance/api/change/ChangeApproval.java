package vn.contractgovernance.api.change;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.approval.ApprovalInputs;
import vn.contractgovernance.api.contract.*;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.policy.PolicyAccess;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.signing.SigningEvidence;
import static vn.contractgovernance.api.change.ChangeService.id;

/** Real CHANGE Round adapter. Immutable source is distinct from current human assessments. */
@Service
public class ChangeApproval {
    public record Frozen(JsonNode revision,JsonNode metadata,UUID submittedBy) {}
    private final EntityStore store;
    private final ChangeService changes;
    private final JsonSupport json;
    private final Clock clock;
    private final PolicyAccess policies;
    private final ApprovalInputs inputs;
    private final ContractRegistry schemas;
    private final UserDirectory users;
    private final CalendarService calendar;
    private final OriginalAgreementService originals;
    private final SigningEvidence signingEvidence;
    private final CommandExecutor commands;
    private final AuditService audit;
    private final AgreementRenderer renderer;
    private final JobQueue jobs;
    public ChangeApproval(EntityStore store,ChangeService changes,JsonSupport json,Clock clock,PolicyAccess policies,
            ApprovalInputs inputs,ContractRegistry schemas,UserDirectory users,CalendarService calendar,
            OriginalAgreementService originals,SigningEvidence signingEvidence,CommandExecutor commands,
            AuditService audit,AgreementRenderer renderer,JobQueue jobs) {
        this.store=store; this.changes=changes; this.json=json; this.clock=clock; this.policies=policies;
        this.inputs=inputs; this.schemas=schemas; this.users=users; this.calendar=calendar; this.originals=originals;
        this.signingEvidence=signingEvidence; this.commands=commands; this.audit=audit; this.renderer=renderer; this.jobs=jobs;
    }
    public Frozen frozen(EntityStore.Entity change) {
        if(!change.body().hasNonNull("roundId")) throw ApiException.of("STATE_CONFLICT","Chưa có bản chốt xét duyệt.");
        return frozenRound(store.require("Round",id(change.body(),"roundId")));
    }
    public Frozen frozenRound(EntityStore.Entity round) {
        var frozen=store.jdbc().query("select revision::text,metadata::text,submitted_by from cg_change_revision where id=? and change_id=?",
                (rs,n)->new Frozen(json.parse(rs.getString(1)),json.parse(rs.getString(2)),rs.getObject(3,UUID.class)),id(round.body(),"revisionId"),id(round.body(),"targetId"))
                .stream().findFirst().orElseThrow(()->ApiException.of("STORAGE_INTEGRITY","Thiếu immutable ChangeRevision."));
        var projection=json.object();
        for(String field:List.of("contractId","baseContractVersionId","expectedScheduleVersion","effectiveOn","target","reason","consolidatedTerms","representation")) projection.set(field,frozen.revision().path(field));
        if(!json.hash(projection).equals(frozen.revision().path("factsHash").asString())
                ||!json.hash(frozen.metadata().path("evidence")).equals(frozen.revision().path("evidenceHash").asString())
                ||!json.hash(frozen.metadata().path("policyProjection")).equals(frozen.revision().path("evaluation").path("factsHash").asString())) {
            throw ApiException.of("STORAGE_INTEGRITY","Immutable ChangeRevision/hash không khớp.");
        }
        return frozen;
    }
    public EntityStore.Entity submit(EntityStore.Entity change,EntityStore.Entity contract,JsonNode request,Actor actor) {
        if(!Set.of("DRAFT","RETURNED").contains(change.body().path("state").asString())) throw ApiException.of("STATE_CONFLICT","Change không ở trạng thái nộp được.");
        changes.active(contract); changes.currentBase(contract,change.body()); changes.requireCurrentGuard(contract); signingEvidence.requireMaterial(contract);
        var policy=policies.requireApplicable(); changes.validateTerms(contract,change.body(),policy);
        changes.contribute(change.id(),actor.id());
        var revision=json.object().put("id",UUID.randomUUID().toString()).put("number",store.jdbc().queryForObject("select coalesce(max(number),0)+1 from cg_change_revision where change_id=?",Integer.class,change.id()));
        for(String field:List.of("contractId","baseContractVersionId","expectedScheduleVersion","effectiveOn","target","reason","documents")) revision.set(field,json.nfc(change.body().path(field)));
        revision.set("consolidatedTerms",changes.consolidated(contract,change.body()));
        revision.set("representation",originals.source(contract.id()).body().path("revision").path("representation").deepCopy());
        revision.set("contributorIds",json.value(changes.contributors(change).stream().map(UUID::toString).toList())); revision.put("submittedAt",changes.now());
        var digest=changes.manifest(change,changes.references(revision),actor);
        // The submitted representation's source belongs to this Change's exact source set.
        var representation=(ObjectNode)revision.path("representation");
        UUID originalAuthority=id(representation,"sourceFileId");
        boolean ownAuthority=changes.references(revision).contains(originalAuthority);
        if(!ownAuthority) {
            var authorities=new ArrayList<UUID>();
            for(var link:revision.path("documents")) if(link.path("purpose").asString().equals("REPRESENTATION_BASIS")) authorities.add(id(link,"fileId"));
            if(authorities.size()!=1) throw ApiException.of("AUTHORITY_INVALID","Change cần một nguồn REPRESENTATION_BASIS riêng để đối chiếu đại diện đã duyệt.");
            representation.put("sourceFileId",authorities.getFirst().toString()).putNull("checkedAt").putNull("checkEvidenceFileId");
        }
        mandatory(request.path("mandatoryChecks"),change,changes.references(revision),actor);
        // REBASE clears the active association, not knowledge of reviewed external conditions.
        // Carry required codes from immutable Round history; demand fresh facts/checks on input.
        var previousChecks=store.jdbc().query("select body->'mandatoryChecks' from cg_entity where entity_type='Round' and body->>'targetKind'='CHANGE' and body->>'targetId'=?",
                (rs,n)->json.parse(rs.getString(1)),change.id().toString());
        for(var previous:previousChecks) inputs.retainConditions(previous,request.path("mandatoryChecks"));
        var steps=json.array().add(step("L1",request.path("l1ReviewerId").asString(),"PENDING"));
        if(request.hasNonNull("l2ReviewerId")) steps.add(step("L2",request.path("l2ReviewerId").asString(),"WAITING"));
        eligible(change,revision,actor.id(),steps);
        var sourceProjection=json.object(); for(String field:List.of("contractId","baseContractVersionId","expectedScheduleVersion","effectiveOn","target","reason","consolidatedTerms","representation")) sourceProjection.set(field,revision.path(field));
        revision.put("factsHash",json.hash(sourceProjection)).put("evidenceHash",json.hash(digest));
        var policyProjection=policyProjection(contract,revision);
        var evaluation=policies.evaluateChangeFrozen(policy.id(),facts(revision,steps,request.path("mandatoryChecks"),json.array(),false),change.branchId(),json.hash(policyProjection),false,renewalFrom(revision,contract));
        inputs.requireEvaluation(evaluation); revision.set("evaluation",evaluation); schemas.validateSchema("ChangeRevision",revision);
        var metadata=json.object(); metadata.set("evidence",digest); metadata.set("mandatoryChecks",json.nfc(request.path("mandatoryChecks"))); metadata.set("policyProjection",policyProjection);
        metadata.set("originalRepresentation",originals.source(contract.id()).body().path("revision").path("representation"));
        metadata.put("policyId",policy.id().toString());
        store.jdbc().update("insert into cg_change_revision(id,change_id,number,revision,metadata,submitted_by,created_at) values(?,?,?,?::jsonb,?::jsonb,?,?)",id(revision,"id"),change.id(),revision.path("number").asInt(),json.stringify(revision),json.stringify(metadata),actor.id(),Timestamp.from(clock.instant()));
        var body=json.object().put("createdAt",changes.now()).put("updatedAt",changes.now()).put("branchId",change.branchId().toString()).put("targetKind","CHANGE").put("targetId",change.id().toString()).put("revisionId",revision.path("id").asString()).put("state","OPEN").put("policyVersionId",policy.id().toString())
                .put("dueAt",JsonSupport.timestamp(calendar.dueByBusinessDays(clock.instant(),policy.body().path("config").path("reviewDueWorkingDays").asInt()))).put("authorityChecked",false);
        body.set("steps",steps); body.set("evidenceChecks",json.array()); body.set("mandatoryChecks",json.nfc(request.path("mandatoryChecks"))); body.set("allowedActions",json.array()); body.set("blockedActions",json.array());
        var round=store.create("Round",UUID.randomUUID(),change.branchId(),body); schemas.validateSchema("Round",round.body()); audit.record(actor,"CREATE",round,null);
        var cb=(ObjectNode)change.body().deepCopy(); cb.put("state","IN_REVIEW").put("roundId",round.id().toString()); cb.set("evaluation",evaluation); cb.set("contributorIds",revision.path("contributorIds"));
        return changes.save(change,cb,actor,"SUBMIT",null);
    }
    private LocalDate renewalFrom(JsonNode revision,EntityStore.Entity contract) {
        return revision.path("target").path("kind").asString().equals("RENEWAL")?LocalDate.parse(contract.body().path("currentVersion").path("terms").path("endExclusive").asString()):null;
    }
    private JsonNode policyProjection(EntityStore.Entity contract,JsonNode revision) {
        var original=originals.source(contract.id()).body(); var projection=json.object(); projection.set("terms",revision.path("consolidatedTerms")); projection.set("representation",revision.path("representation"));
        for(String field:List.of("contact","externalAccountReference")) projection.set(field,original.path("revision").path(field));
        for(String field:List.of("templateVersion","bundleVersion")) projection.set(field,original.path("metadata").path(field)); return projection;
    }
    public JsonNode facts(JsonNode revision,JsonNode steps,JsonNode mandatory,JsonNode checks,boolean checked) {
        var adapted=(ObjectNode)revision.deepCopy(); adapted.set("terms",revision.path("consolidatedTerms")); return inputs.facts(adapted,steps,mandatory,checks,checked);
    }
    private void eligible(EntityStore.Entity change,JsonNode revision,UUID submitter,JsonNode steps) {
        var excluded=changes.contributors(change); excluded.add(submitter); var seen=new HashSet<UUID>();
        for(var step:steps) {
            UUID reviewer=id(step,"reviewerId"); if(!seen.add(reviewer)) throw ApiException.of("SOD_VIOLATION","L1/L2 phải khác nhau.");
            users.requireEligible(reviewer,change.branchId(),step.path("level").asString().equals("L1")?"CHECKER":"MANAGER",excluded);
            var actor=users.actor(reviewer); for(var fileId:changes.references(revision)) if(store.require("File",fileId).body().path("sensitive").asBoolean()&&!actor.hasPermission("SENSITIVE_READ")) throw ApiException.of("SENSITIVE_FORBIDDEN","Reviewer thiếu quyền evidence nhạy cảm.");
        }
    }
    public void mandatory(JsonNode checks,EntityStore.Entity change,Set<UUID> sourceIds,Actor actor) {
        var codes=new HashSet<String>();
        for(var check:checks) {
            if(!codes.add(check.path("code").asString())) throw ApiException.validation("/mandatoryChecks","Mã điều kiện không được trùng."); inputs.nonfuture(check.path("checkedAt"),"/mandatoryChecks/checkedAt");
            var ids=SigningEvidence.uniqueIds(check.path("evidenceFileIds"),"/mandatoryChecks/evidenceFileIds");
            if(!sourceIds.containsAll(ids)) throw ApiException.validation("/mandatoryChecks/evidenceFileIds","Điều kiện phải thuộc bản chốt Change."); changes.manifest(change,ids,actor);
        }
    }
    public EntityStore.Entity requireRound(UUID id,Actor actor) {
        var round=store.require("Round",id); actor=users.actor(actor.id()); actor.requireBranch(round.branchId());
        if(!round.body().path("targetKind").asString().equals("CHANGE")) throw ApiException.of("NOT_FOUND","Không phải CHANGE Round.");
        var change=changes.require(id(round.body(),"targetId"),actor); if(!Objects.equals(round.branchId(),change.branchId())) throw ApiException.of("NOT_FOUND","Sai nguồn chi nhánh."); return round;
    }
    private void open(EntityStore.Entity round,EntityStore.Entity change) {
        if(!round.body().path("state").asString().equals("OPEN")||!change.body().path("state").asString().equals("IN_REVIEW")||!change.body().path("roundId").asString().equals(round.id().toString())) throw ApiException.of("STATE_CONFLICT","Vòng duyệt không còn mở.");
    }
    private void authorize(UUID id,JsonNode request,Actor actor) {
        var round=requireRound(id,actor); var change=changes.require(id(round.body(),"targetId"),actor); var frozen=frozenRound(round);
        var excluded=changes.contributors(change); excluded.add(frozen.submittedBy()); if(excluded.contains(actor.id())) throw ApiException.of("SOD_VIOLATION","Nguồn cần người duyệt độc lập.");
        var current=users.actor(actor.id());
        if(request.path("action").asString().equals("REASSIGN")) { current.requireRole("MANAGER"); return; }
        String level=request.path("action").asString().equals("VERIFY_EVIDENCE")?"L1":request.path("level").asString();
        var step=findStep(round.body(),level); if(!step.path("reviewerId").asString().equals(actor.id().toString())) throw ApiException.of("ROLE_FORBIDDEN","Không phải reviewer được chỉ định.");
        eligible(change,frozen.revision(),frozen.submittedBy(),json.array().add(step));
    }
    public ApiResult command(UUID id,JsonNode request,String tag,String key,Actor actor) {
        authorize(id,request,actor); ETags.require(tag);
        var changeId=id(requireRound(id,actor).body(),"targetId");
        return commands.execute(actor,key,"POST","/api/v1/rounds/"+id+"/commands",request,tag,()->authorize(id,request,actor),()->{
            var change=changes.lock(changeId); var round=store.lock("Round",id); authorize(id,request,actor); ETags.check(tag,round.version()); open(round,change);
            var frozen=frozenRound(round); var body=(ObjectNode)round.body().deepCopy(); String action=request.path("action").asString();
            if(action.equals("VERIFY_EVIDENCE")) {
                pending(body,"L1",actor); digest(change,frozen,actor); var ids=changes.references(frozen.revision()); var seen=new HashSet<UUID>();
                for(var check:request.path("checks")) { UUID file=id(check,"fileId"); if(!seen.add(file)||!ids.contains(file)) throw ApiException.validation("/checks","Bằng chứng trùng hoặc ngoài bản chốt."); inputs.nonfuture(check.path("checkedAt"),"/checks/checkedAt"); }
                mandatory(request.path("mandatoryChecks"),change,ids,actor); inputs.retainConditions(body.path("mandatoryChecks"),request.path("mandatoryChecks"));
                body.set("evidenceChecks",json.nfc(request.path("checks"))); body.set("mandatoryChecks",json.nfc(request.path("mandatoryChecks"))); body.set("authorityChecked",request.path("authorityChecked"));
                store.jdbc().update("insert into cg_round_assessment(id,round_id,actor_id,assessment,recorded_at) values(?,?,?,?::jsonb,?)",UUID.randomUUID(),id,actor.id(),json.stringify(request),Timestamp.from(clock.instant()));
            } else if(action.equals("REASSIGN")) {
                var step=findStep(body,request.path("level").asString()); if(!Set.of("PENDING","WAITING").contains(step.path("state").asString())) throw ApiException.of("STATE_CONFLICT","Bước đã quyết định.");
                UUID previous=id(step,"reviewerId"); step.set("reviewerId",request.path("reviewerId")); eligible(change,frozen.revision(),frozen.submittedBy(),body.path("steps"));
                if(previous.equals(id(step,"reviewerId"))) throw ApiException.validation("/reviewerId","Reviewer mới phải khác.");
                store.jdbc().update("insert into cg_round_reassignment(id,round_id,level,old_reviewer_id,new_reviewer_id,actor_id,reason,recorded_at) values(?,?,?,?,?,?,?,?)",UUID.randomUUID(),id,request.path("level").asString(),previous,id(step,"reviewerId"),actor.id(),request.path("reason").asString(),Timestamp.from(clock.instant()));
                if(request.path("level").asString().equals("L1")) { body.set("evidenceChecks",json.array()); body.put("authorityChecked",false); }
            } else {
                var step=pending(body,request.path("level").asString(),actor);
                if(action.equals("APPROVE")) {
                    current(change,round,frozen,actor);
                    step.put("state","APPROVED");
                    if(body.path("steps").size()==1||request.path("level").asString().equals("L2")) {
                        body.put("state","APPROVED"); var cb=(ObjectNode)change.body().deepCopy(); cb.put("state","APPROVED");
                        var updated=changes.save(change,cb,actor,"APPROVE",request.path("reason").asString()); prepare(updated,frozen,body,actor);
                    } else for(var next:body.path("steps")) if(next.path("state").asString().equals("WAITING")) ((ObjectNode)next).put("state","PENDING");
                } else if(action.equals("RETURN")||action.equals("REJECT")) {
                    String result=action.equals("RETURN")?"RETURNED":"REJECTED"; step.put("state",result); body.put("state",result); cancelUndecided(body);
                    var cb=(ObjectNode)change.body().deepCopy(); cb.put("state",result); changes.save(change,cb,actor,action,request.path("reason").asString());
                } else throw ApiException.validation("/action","Không hỗ trợ thao tác.");
                step.put("decidedBy",actor.id().toString()).put("decisionAt",changes.now()).put("reason",request.path("reason").asString());
                store.jdbc().update("insert into cg_round_decision(round_id,level,actor_id,action,reason,decided_at) values(?,?,?,?,?,?)",id,request.path("level").asString(),actor.id(),action,request.path("reason").asString(),Timestamp.from(clock.instant()));
            }
            body.put("updatedAt",changes.now()); var updated=store.update(round,body); schemas.validateSchema("Round",updated.body()); audit.record(actor,action,updated,request.has("reason")?request.path("reason").asString():null); return ApiResult.ok(project(updated,actor));
        });
    }
    private void current(EntityStore.Entity change,EntityStore.Entity round,Frozen frozen,Actor actor) {
        var contract=store.require("Contract",id(change.body(),"contractId")); changes.active(contract); changes.currentBase(contract,change.body()); changes.requireCurrentGuard(contract); signingEvidence.requireMaterial(contract);
        changes.validateTerms(contract,change.body(),store.require("Policy",id(round.body(),"policyVersionId"))); digest(change,frozen,actor); eligible(change,frozen.revision(),frozen.submittedBy(),round.body().path("steps"));
        mandatory(round.body().path("mandatoryChecks"),change,changes.references(frozen.revision()),actor);
        if(!round.body().path("authorityChecked").asBoolean()) throw ApiException.of("AUTHORITY_INVALID","Chưa kiểm tra thẩm quyền.");
        var assessors=store.jdbc().query("select actor_id from cg_round_assessment where round_id=? order by recorded_at desc,id desc limit 1",(rs,n)->rs.getObject(1,UUID.class),round.id());
        if(assessors.isEmpty()||!assessors.getFirst().toString().equals(round.body().path("steps").get(0).path("reviewerId").asString())) throw ApiException.of("POLICY_BLOCKED","Cần assessment của L1 hiện hành.");
        requiredDocumentPasses(frozen.revision(),round.body());
        var evaluation=policies.evaluateChangeFrozen(id(round.body(),"policyVersionId"),facts(frozen.revision(),round.body().path("steps"),round.body().path("mandatoryChecks"),round.body().path("evidenceChecks"),true),change.branchId(),json.hash(frozen.metadata().path("policyProjection")),true,renewalFrom(frozen.revision(),contract)); inputs.requireEvaluation(evaluation);
        store.jdbc().update("insert into cg_round_evaluation(id,round_id,actor_id,evaluation,recorded_at) values(?,?,?,?::jsonb,?)",UUID.randomUUID(),round.id(),actor.id(),json.stringify(evaluation),Timestamp.from(clock.instant()));
    }
    private void requiredDocumentPasses(JsonNode revision,JsonNode round) {
        var required=new HashSet<String>(); store.require("Policy",id(round,"policyVersionId")).body().path("config").path("requiredDocumentPurposes").forEach(purpose->required.add(purpose.asString()));
        for(var document:revision.path("documents")) {
            if(!required.contains(document.path("purpose").asString())) continue;
            boolean passed=false;
            for(var check:round.path("evidenceChecks")) if(check.path("fileId").equals(document.path("fileId"))&&check.path("result").asString().equals("PASS")) passed=true;
            if(!passed) throw ApiException.of("POLICY_BLOCKED","Mọi bằng chứng bắt buộc phải có human PASS riêng.");
        }
    }
    private void digest(EntityStore.Entity change,Frozen frozen,Actor actor) {
        var actual=changes.manifest(change,changes.references(frozen.revision()),actor);
        if(!actual.equals(frozen.metadata().path("evidence"))||!json.hash(actual).equals(frozen.revision().path("evidenceHash").asString())) throw ApiException.of("STORAGE_INTEGRITY","Bằng chứng khác nguồn đã chốt.");
    }
    private void prepare(EntityStore.Entity change,Frozen frozen,JsonNode round,Actor actor) {
        var source=json.object().put("changeId",change.id().toString()).put("generatedAt",changes.now()); source.set("revision",frozen.revision()); source.set("metadata",frozen.metadata()); source.set("approvedChecks",round.path("mandatoryChecks")); source.set("manifest",renderer.manifest());
        var hash=json.hash(source); var job=jobs.enqueue("ARTIFACT_PDF","CHANGE",change.id(),change.branchId(),change.version(),hash,"ARTIFACT_PDF:CHANGE:"+frozen.revision().path("id").asString(),json.object().put("revisionId",frozen.revision().path("id").asString()),AuditService.traceId());
        store.jdbc().update("insert into cg_change_artifact_source(revision_id,change_id,source,source_hash,job_id,created_at) values(?,?,?::jsonb,?,?,?)",id(frozen.revision(),"id"),change.id(),json.stringify(source),hash,job,Timestamp.from(clock.instant()));
        var body=(ObjectNode)change.body().deepCopy(); body.put("artifactJobId",job.toString()); changes.save(change,body,actor,"ARTIFACT_REQUEST",null);
    }
    public EntityStore.Entity withdraw(EntityStore.Entity change,String reason,Actor actor) {
        var round=store.lock("Round",id(change.body(),"roundId")); open(round,change); var body=(ObjectNode)round.body().deepCopy(); body.put("state","CANCELLED").put("updatedAt",changes.now()); cancelUndecided(body); audit.record(actor,"WITHDRAW",store.update(round,body),reason);
        var cb=(ObjectNode)change.body().deepCopy(); cb.put("state","DRAFT"); return changes.save(change,cb,actor,"WITHDRAW",reason);
    }
    public JsonNode snapshot(UUID id,Actor actor) {
        var round=requireRound(id,actor); var revision=(ObjectNode)frozenRound(round).revision().deepCopy();
        if(actor.hasRole("AUDITOR")&&!actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER")&&!actor.hasPermission("SENSITIVE_READ")) {
            var representation=(ObjectNode)revision.path("representation"); for(String field:List.of("personName","title","scopeText")) representation.put(field,"[REDACTED]"); revision.put("reason","[REDACTED]");
        }
        var result=json.object().put("targetKind","CHANGE"); result.set("revision",revision); return result;
    }
    public JsonNode project(EntityStore.Entity round,Actor actor) {
        actor=users.actor(actor.id());
        var body=(ObjectNode)round.body().deepCopy(); var allowed=json.array(); var blocked=json.array();
        if(body.path("state").asString().equals("OPEN")) for(String action:List.of("VERIFY_EVIDENCE","APPROVE","RETURN","REJECT","REASSIGN")) {
            var request=json.object().put("action",action); String level="L1";
            for(var step:body.path("steps")) if(step.path("reviewerId").asString().equals(actor.id().toString())&&step.path("state").asString().equals("PENDING")) level=step.path("level").asString(); request.put("level",level);
            try {
                authorize(round.id(),request,actor);
                if(!action.equals("REASSIGN")) pending(body,action.equals("VERIFY_EVIDENCE")?"L1":level,actor);
                if(action.equals("APPROVE")) approvalHint(round,actor);
                allowed.add(action);
            }
            catch(ApiException exception) { if(!actor.hasRole("AUDITOR")&&actor.hasAnyRole("CHECKER","MANAGER")) blocked.add(json.object().put("action",action).put("code",exception.code()).put("reason",exception.getMessage())); }
        }
        body.set("allowedActions",allowed); body.set("blockedActions",blocked); return body;
    }
    /** Advisory read-only counterpart of current(); no assessment/history or private-file writes. */
    private void approvalHint(EntityStore.Entity round,Actor actor) {
        var change=changes.require(id(round.body(),"targetId"),actor); var contract=store.require("Contract",id(change.body(),"contractId")); var frozen=frozenRound(round);
        changes.active(contract); changes.currentBase(contract,change.body()); changes.requireCurrentGuard(contract);
        if(!signingEvidence.materialUnchanged(contract,false)) throw ApiException.of("READINESS_BLOCKED","Material source đã thay đổi.");
        changes.validateTerms(contract,change.body(),store.require("Policy",id(round.body(),"policyVersionId")));
        eligible(change,frozen.revision(),frozen.submittedBy(),round.body().path("steps"));
        if(!round.body().path("authorityChecked").asBoolean()) throw ApiException.of("AUTHORITY_INVALID","Chưa kiểm tra thẩm quyền.");
        var assessors=store.jdbc().query("select actor_id from cg_round_assessment where round_id=? order by recorded_at desc,id desc limit 1",(rs,n)->rs.getObject(1,UUID.class),round.id());
        if(assessors.isEmpty()||!assessors.getFirst().toString().equals(round.body().path("steps").get(0).path("reviewerId").asString())) throw ApiException.of("POLICY_BLOCKED","Cần assessment của L1 hiện hành.");
        for(var digest:frozen.metadata().path("evidence")) {
            var file=store.require("File",id(digest,"fileId"));
            if(!file.body().path("state").asString().equals("CLEAN")) throw ApiException.of("FILE_NOT_CLEAN","Bằng chứng không còn CLEAN.");
            if(!file.body().path("sha256").equals(digest.path("sha256"))) throw ApiException.of("STORAGE_INTEGRITY","Evidence metadata khác nguồn chốt.");
        }
        requiredDocumentPasses(frozen.revision(),round.body());
        inputs.requireEvaluation(policies.evaluateChangeFrozen(id(round.body(),"policyVersionId"),facts(frozen.revision(),round.body().path("steps"),round.body().path("mandatoryChecks"),round.body().path("evidenceChecks"),true),change.branchId(),json.hash(frozen.metadata().path("policyProjection")),true,renewalFrom(frozen.revision(),contract)));
    }
    private ObjectNode findStep(JsonNode round,String level) { for(var step:round.path("steps")) if(step.path("level").asString().equals(level)) return (ObjectNode)step; throw ApiException.of("STATE_CONFLICT","Không có bước duyệt này."); }
    private ObjectNode pending(JsonNode round,String level,Actor actor) { var step=findStep(round,level); if(!step.path("state").asString().equals("PENDING")) throw ApiException.of("STATE_CONFLICT","Bước chưa đến lượt."); if(!step.path("reviewerId").asString().equals(actor.id().toString())) throw ApiException.of("ROLE_FORBIDDEN","Không đúng reviewer."); return step; }
    private ObjectNode step(String level,String reviewer,String state) { var body=json.object().put("level",level).put("reviewerId",reviewer).put("state",state); return body.putNull("decidedBy").putNull("decisionAt").putNull("reason"); }
    private void cancelUndecided(ObjectNode body) { for(var step:body.path("steps")) if(Set.of("PENDING","WAITING").contains(step.path("state").asString())) ((ObjectNode)step).put("state","CANCELLED"); }
}
