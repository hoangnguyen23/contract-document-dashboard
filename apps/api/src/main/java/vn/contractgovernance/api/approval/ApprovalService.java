package vn.contractgovernance.api.approval;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.dossier.*;
import vn.contractgovernance.api.files.EvidenceAccess;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.policy.PolicyAccess;
import vn.contractgovernance.api.shared.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Service
public class ApprovalService implements DossierApprovalBridge {
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.control.ControlApproval> controls;
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeApproval> changes;
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final DossierAccess access;
    private final EvidenceAccess evidence;
    private final PolicyAccess policies;
    private final ApprovalInputs inputs;
    private final RevisionStore revisions;
    private final CalendarService calendar;
    private final CommandExecutor commands;
    private final AuditService audit;
    private final ListQueries lists;
    private final UserDirectory users;
    private final ContractRegistry contracts;
    private final ObjectProvider<OriginalAgreementPort> originals;
    public ApprovalService(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.control.ControlApproval> controls,org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeApproval> changes,EntityStore store,JsonSupport json,Clock clock,DossierAccess access,EvidenceAccess evidence,
            PolicyAccess policies,ApprovalInputs inputs,RevisionStore revisions,CalendarService calendar,
            CommandExecutor commands,AuditService audit,ListQueries lists,UserDirectory users,ContractRegistry contracts,
            ObjectProvider<OriginalAgreementPort> originals) {
        this.controls=controls;this.changes=changes;this.store=store;this.json=json;this.clock=clock;this.access=access;this.evidence=evidence;this.policies=policies;
        this.inputs=inputs;this.revisions=revisions;this.calendar=calendar;this.commands=commands;this.audit=audit;
        this.lists=lists;this.users=users;this.contracts=contracts;this.originals=originals;
    }
    /** Caller already holds Dossier. All mutations use Dossier -> Relationship -> Round -> sorted File locks. */
    @Override public void requireSubmissionReady(EntityStore.Entity dossier,Actor actor) {
        access.requireEditable(dossier);access.requireMaker(dossier.id(),actor);
        var source=dossier.body();var terms=source.path("terms");
        var policy=policies.findApplicableHint().orElseThrow(()->ApiException.of("POLICY_BLOCKED","Chưa có chính sách áp dụng."));
        var config=policy.body().path("config");
        if(!LocalDate.parse(terms.path("startOn").asString()).isAfter(calendar.localDate(clock.instant())))throw ApiException.validation("/terms/startOn","Ngày bắt đầu phải từ ngày địa phương kế tiếp.");
        if(!source.path("representation").hasNonNull("sourceFileId"))throw ApiException.of("AUTHORITY_INVALID","Thiếu nguồn thẩm quyền.");
        long fee=Long.parseLong(terms.path("feeVnd").asString());
        if(fee<Long.parseLong(config.path("minFeeVnd").asString())||fee>Long.parseLong(config.path("maxFeeVnd").asString())||!terms.path("feePayer").asString().equals("BENEFICIARY"))throw ApiException.of("POLICY_BLOCKED","Phí hoặc bên chịu phí chưa đạt chính sách.");
        boolean allowed=false;for(var kind:config.path("allowedTermKinds"))allowed|=kind.asString().equals(terms.path("termKind").asString());
        if(!allowed)throw ApiException.of("POLICY_BLOCKED","Loại thời hạn chưa đạt chính sách.");
        if(terms.path("termKind").asString().equals("FIXED")) {
            long days=java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(terms.path("startOn").asString()),LocalDate.parse(terms.path("endExclusive").asString()));
            if(days<config.path("minFixedTermDays").asInt()||days>config.path("maxFixedTermDays").asInt())throw ApiException.of("POLICY_BLOCKED","Thời hạn chưa đạt chính sách.");
        }
        var purposes=new HashSet<String>();for(var id:DossierService.referenceIds(source)) {
            var file=evidence.requireClean(id,"DOSSIER",dossier.id(),dossier.branchId(),actor);
            for(var link:source.path("documents"))if(link.path("fileId").asString().equals(id.toString()))purposes.add(file.body().path("purpose").asString());
        }
        for(var purpose:config.path("requiredDocumentPurposes"))if(!purposes.contains(purpose.asString()))throw ApiException.of("FILE_NOT_CLEAN","Thiếu bằng chứng bắt buộc.");
        var representation=source.path("representation");var at=clock.instant();
        if(at.isBefore(calendar.startOfDay(LocalDate.parse(representation.path("validFrom").asString())))
                ||(representation.hasNonNull("validTo")&&!at.isBefore(calendar.startOfDay(LocalDate.parse(representation.path("validTo").asString()))))
                ||(representation.hasNonNull("knownRevocationAt")&&!at.isBefore(Instant.parse(representation.path("knownRevocationAt").asString()))))throw ApiException.of("AUTHORITY_INVALID","Thẩm quyền không còn hiệu lực.");
    }
    @Override public EntityStore.Entity submit(EntityStore.Entity dossier,JsonNode command,Actor actor) {
        access.requireEditable(dossier);access.requireMaker(dossier.id(),actor);
        var relationship=store.lock("Relationship",uuid(dossier.body(),"relationshipId"));
        if(!relationship.body().path("active").asBoolean())throw ApiException.of("STATE_CONFLICT","Quan hệ đã ngừng hoạt động.");
        var source=(ObjectNode)json.nfc(dossier.body());
        DossierService.contribute(source,actor.id());
        if(source.path("contributorIds").size()>50)throw ApiException.validation("/contributorIds","Nguồn vượt giới hạn 50 người đóng góp.");
        if(!LocalDate.parse(source.path("terms").path("startOn").asString()).isAfter(calendar.localDate(clock.instant())))
            throw ApiException.validation("/terms/startOn","Ngày bắt đầu phải từ ngày địa phương kế tiếp.");
        if(!source.path("representation").hasNonNull("sourceFileId"))throw ApiException.of("AUTHORITY_INVALID","Phải có nguồn thẩm quyền trước khi nộp.");
        contracts.validateSchema("SubmittedRepresentation",source.path("representation"));
        if(source.path("representation").hasNonNull("checkedAt"))inputs.nonfuture(source.path("representation").path("checkedAt"),"/representation/checkedAt");
        if(!source.path("templateVersion").asString().equals("BASE-VA-1")||!source.path("bundleVersion").asString().equals("TERMS-VA-1"))
            throw ApiException.of("POLICY_BLOCKED","Mẫu điều khoản không được hỗ trợ.");
        var digest=inputs.evidence(dossier,source,actor);
        inputs.mandatory(command.path("mandatoryChecks"),dossier,DossierService.referenceIds(source),actor);
        inputs.retainConditions(source.path("mandatoryChecks"),command.path("mandatoryChecks"));
        var revision=json.object();revision.put("id",UUID.randomUUID().toString());revision.put("number",revisions.nextNumber(dossier.id()));
        for(var field:List.of("terms","representation","documents","contributorIds","externalAccountReference"))revision.set(field,source.path(field));
        revision.set("contact",json.nfc(relationship.body().path("confirmedContact")));revision.put("submittedAt",now());
        var projection=json.object();for(var field:List.of("terms","representation","contact","externalAccountReference"))projection.set(field,revision.path(field));
        projection.set("templateVersion",source.path("templateVersion"));projection.set("bundleVersion",source.path("bundleVersion"));
        revision.put("factsHash",json.hash(projection));revision.put("evidenceHash",json.hash(digest));
        var steps=json.array();steps.add(step("L1",command.path("l1ReviewerId").asString(),"PENDING"));
        if(command.hasNonNull("l2ReviewerId"))steps.add(step("L2",command.path("l2ReviewerId").asString(),"WAITING"));
        validateReviewers(dossier,revision,actor.id(),steps);
        var policy=policies.requireApplicable();
        var evaluation=policies.evaluateFrozen(policy.id(),inputs.facts(revision,steps,command.path("mandatoryChecks"),json.array(),false),dossier.branchId(),revision.path("factsHash").asString(),false);
        inputs.requireEvaluation(evaluation);revision.set("evaluation",evaluation);contracts.validateSchema("Revision",revision);
        var metadata=json.object();metadata.set("evidence",digest);metadata.set("templateVersion",source.path("templateVersion"));metadata.set("bundleVersion",source.path("bundleVersion"));
        metadata.put("relationshipId",relationship.id().toString());metadata.set("contactVersion",relationship.body().path("contactVersion"));
        var party=store.lock("Party",uuid(relationship.body(),"partyId"));var identity=json.object();
        for(var field:List.of("id","name","taxId"))identity.set(field,party.body().path(field));metadata.set("party",json.nfc(identity));
        metadata.put("policyConfigHash",policy.body().path("configHash").asString());
        metadata.set("mandatoryChecks",json.nfc(command.path("mandatoryChecks")));
        revisions.save(dossier.id(),revision,metadata,actor.id());
        evidence.freezeReferences("DOSSIER",dossier.id(),DossierService.referenceIds(source),actor);
        var body=json.object();body.put("createdAt",now());body.put("updatedAt",now());body.put("branchId",dossier.branchId().toString());
        body.put("targetKind","DOSSIER");body.put("targetId",dossier.id().toString());body.set("revisionId",revision.path("id"));body.put("state","OPEN");
        body.put("policyVersionId",policy.id().toString());body.put("dueAt",JsonSupport.timestamp(calendar.dueByBusinessDays(clock.instant(),policy.body().path("config").path("reviewDueWorkingDays").asInt())));
        body.set("steps",steps);body.set("evidenceChecks",json.array());body.put("authorityChecked",false);body.set("mandatoryChecks",json.nfc(command.path("mandatoryChecks")));
        body.set("allowedActions",json.array());body.set("blockedActions",json.array());
        var round=store.create("Round",UUID.randomUUID(),dossier.branchId(),body);audit.record(actor,"CREATE",round,null);
        source.put("state","IN_REVIEW");source.set("revision",revision);source.put("roundId",round.id().toString());source.set("contactSnapshot",revision.path("contact"));
        source.set("mandatoryChecks",json.nfc(command.path("mandatoryChecks")));source.put("updatedAt",now());return store.update(dossier,source);
    }
    @Override public EntityStore.Entity withdraw(EntityStore.Entity dossier,String reason,Actor actor) {
        var round=store.lock("Round",uuid(dossier.body(),"roundId"));requireBinding(round,dossier);requireOpen(round,dossier);
        var body=(ObjectNode)round.body().deepCopy();body.put("state","CANCELLED");cancelUndecided(body);body.put("updatedAt",now());
        var closed=store.update(round,body);audit.record(actor,"WITHDRAW",closed,reason);
        var next=(ObjectNode)dossier.body().deepCopy();next.put("state","DRAFT");next.set("mandatoryChecks",round.body().path("mandatoryChecks").deepCopy());next.put("updatedAt",now());return store.update(dossier,next);
    }
    public JsonNode list(Map<String,String> query,Actor actor) {
        actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");
        var args=new ArrayList<Object>();String where="e.body->>'targetKind' in ('DOSSIER','CHANGE','CONTROL_REQUEST') and exists(select 1 from cg_entity d left join cg_entity c on c.entity_type='Contract' and c.id=(d.body->>'contractId')::uuid join cg_entity r on r.entity_type='Relationship' and r.id=(coalesce(d.body->>'relationshipId',c.body->>'relationshipId'))::uuid and r.branch_id=d.branch_id where d.entity_type=case e.body->>'targetKind' when 'DOSSIER' then 'Dossier' when 'CHANGE' then 'Change' else 'ControlRequest' end and d.id=(e.body->>'targetId')::uuid and d.branch_id=e.branch_id";
        if(!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")){where+=" and r.body->>'assignedMakerId'=?";args.add(actor.id().toString());}where+=")";
        if(query.containsKey("reviewerId")){where+=" and exists(select 1 from jsonb_array_elements(e.body->'steps') s where s->>'reviewerId'=?)";args.add(query.get("reviewerId"));}
        return lists.page("Round",actor,query,false,where,args,b->project(entity(b),actor));
    }
    public JsonNode get(UUID id,Actor actor){if(controlRound(id))return controls.getObject().project(controls.getObject().requireRound(id,actor),actor);if(changeRound(id))return changes.getObject().project(changes.getObject().requireRound(id,actor),actor);return project(require(id,actor),actor);}
    public JsonNode snapshot(UUID id,Actor actor) {
        if(controlRound(id))return controls.getObject().snapshot(id,actor);
        if(changeRound(id))return changes.getObject().snapshot(id,actor);
        var round=require(id,actor);var dossier=access.require(uuid(round.body(),"targetId"),actor);var frozen=frozen(round);
        var result=json.object();result.put("targetKind","DOSSIER");result.set("revision",redactRevision(frozen.revision(),dossier,actor));return result;
    }
    public ApiResult command(UUID id,JsonNode request,String tag,String key,Actor actor) {
        if(controlRound(id))return controls.getObject().command(id,request,tag,key,actor);
        if(changeRound(id))return changes.getObject().command(id,request,tag,key,actor);
        authorize(id,request,actor);ETags.require(tag);
        return commands.execute(actor,key,"POST","/api/v1/rounds/"+id+"/commands",request,tag,()->authorize(id,request,actor),()->{
            var before=require(id,actor);var dossier=access.lock(uuid(before.body(),"targetId"),actor);
            var relationship=store.lock("Relationship",uuid(dossier.body(),"relationshipId"));
            var round=store.lock("Round",id);requireBinding(round,dossier);authorize(id,request,actor);ETags.check(tag,round.version());requireOpen(round,dossier);
            var frozen=frozen(round);var revision=frozen.revision();var action=request.path("action").asString();var body=(ObjectNode)round.body().deepCopy();
            if(action.equals("VERIFY_EVIDENCE"))verify(dossier,round,frozen,request,body,actor);
            else if(action.equals("REASSIGN"))reassign(dossier,round,frozen,request,body,actor);
            else {
                var step=pending(body,request.path("level").asString(),actor);
                if(action.equals("APPROVE")) {
                    requireCurrent(dossier,relationship,round,frozen,actor);
                    boolean finalStep=body.path("steps").size()==1||request.path("level").asString().equals("L2");
                    if(finalStep&&originals.getIfAvailable()==null)throw ApiException.of("SERVICE_UNAVAILABLE","Module hợp đồng và PDF chưa sẵn sàng; quyết định cuối được giữ nguyên.");
                    decide(step,"APPROVED",request,actor);recordDecision(round,request,actor);
                    if(finalStep) {
                        body.put("state","APPROVED");body.put("updatedAt",now());var approved=store.update(round,body);
                        var contract=originals.getObject().prepare(dossier,frozen,approved,actor);
                        if(!contract.type().equals("Contract")||!contract.body().path("state").asString().equals("PREPARED")||!contract.body().path("approvedRevisionId").asString().equals(revision.path("id").asString()))
                            throw new IllegalStateException("Original agreement port returned an invalid binding");
                        var next=(ObjectNode)dossier.body().deepCopy();next.put("state","APPROVED");next.put("contractId",contract.id().toString());next.put("updatedAt",now());
                        var source=store.update(dossier,next);audit.record(actor,"APPROVE",source,request.path("reason").asString());audit.record(actor,action,approved,request.path("reason").asString());return ApiResult.ok(project(approved,actor));
                    }
                    for(var item:body.path("steps"))if(item.path("state").asString().equals("WAITING"))((ObjectNode)item).put("state","PENDING");
                } else if(action.equals("RETURN")||action.equals("REJECT")) {
                    decide(step,action.equals("RETURN")?"RETURNED":"REJECTED",request,actor);recordDecision(round,request,actor);
                    body.put("state",action.equals("RETURN")?"RETURNED":"REJECTED");cancelUndecided(body);
                    var next=(ObjectNode)dossier.body().deepCopy();next.set("state",body.path("state"));next.set("mandatoryChecks",body.path("mandatoryChecks").deepCopy());next.put("updatedAt",now());var source=store.update(dossier,next);audit.record(actor,action,source,request.path("reason").asString());
                } else throw ApiException.validation("/action","Thao tác xét duyệt không hợp lệ.");
            }
            body.put("updatedAt",now());var next=store.update(round,body);audit.record(actor,action,next,request.hasNonNull("reason")?request.path("reason").asString():null);return ApiResult.ok(project(next,actor));
        });
    }
    private void verify(EntityStore.Entity dossier,EntityStore.Entity round,RevisionStore.Frozen frozen,JsonNode request,ObjectNode body,Actor actor) {
        pending(body,"L1",actor);
        var revision=frozen.revision();var digest=inputs.evidence(dossier,revision,actor);requireDigest(frozen,digest);
        var ids=DossierService.referenceIds(revision);var seen=new HashSet<UUID>();
        for(var check:request.path("checks")) {
            var id=uuid(check,"fileId");if(!seen.add(id))throw ApiException.validation("/checks","Bằng chứng kiểm tra không được trùng.");
            if(!ids.contains(id))throw ApiException.validation("/checks/fileId","Chỉ kiểm tra bằng chứng của bản chốt này.");inputs.nonfuture(check.path("checkedAt"),"/checks/checkedAt");
        }
        inputs.mandatory(request.path("mandatoryChecks"),dossier,ids,actor);
        inputs.retainConditions(round.body().path("mandatoryChecks"),request.path("mandatoryChecks"));
        body.set("evidenceChecks",json.nfc(request.path("checks")));body.set("authorityChecked",request.path("authorityChecked"));body.set("mandatoryChecks",json.nfc(request.path("mandatoryChecks")));
        store.jdbc().update("insert into cg_round_assessment(id,round_id,actor_id,assessment,recorded_at) values(?,?,?,?::jsonb,?)",UUID.randomUUID(),round.id(),actor.id(),json.stringify(json.nfc(request)),Timestamp.from(clock.instant()));
    }
    private void requireCurrent(EntityStore.Entity dossier,EntityStore.Entity relationship,EntityStore.Entity round,RevisionStore.Frozen frozen,Actor actor) {
        if(!relationship.body().path("active").asBoolean())throw ApiException.of("STATE_CONFLICT","Quan hệ đã ngừng hoạt động.");
        var party=store.lock("Party",uuid(relationship.body(),"partyId"));
        if(!party.id().toString().equals(frozen.metadata().path("party").path("id").asString())
                ||!json.nfc(party.body().path("name")).equals(frozen.metadata().path("party").path("name"))
                ||!party.body().path("taxId").equals(frozen.metadata().path("party").path("taxId")))
            throw ApiException.of("LEGAL_REVIEW_REQUIRED","Thông tin pháp nhân đã thay đổi sau khi chốt.");
        requireDigest(frozen,inputs.evidence(dossier,frozen.revision(),actor));
        inputs.mandatory(round.body().path("mandatoryChecks"),dossier,DossierService.referenceIds(frozen.revision()),actor);
        validateReviewers(dossier,frozen.revision(),frozen.submittedBy(),round.body().path("steps"));
        if(!round.body().path("authorityChecked").asBoolean())throw ApiException.of("AUTHORITY_INVALID","L1 chưa xác minh thẩm quyền.");
        if(!hasCurrentAssessment(round))throw ApiException.of("POLICY_BLOCKED","Người xác minh không còn là L1 hợp lệ; cần xác minh lại.");
        var requiredPurposes=store.require("Policy",uuid(round.body(),"policyVersionId")).body().path("config").path("requiredDocumentPurposes");
        for(var document:frozen.revision().path("documents")) {
            boolean required=false;for(var purpose:requiredPurposes)required|=purpose.asString().equals(document.path("purpose").asString());
            if(required) {
                boolean pass=false;for(var check:round.body().path("evidenceChecks"))pass|=check.path("fileId").equals(document.path("fileId"))&&check.path("result").asString().equals("PASS");
                if(!pass)throw ApiException.of("POLICY_BLOCKED","Mỗi bằng chứng bắt buộc phải có đánh giá PASS của L1.");
            }
        }
        var evaluation=policies.evaluateFrozen(uuid(round.body(),"policyVersionId"),inputs.facts(frozen.revision(),round.body().path("steps"),round.body().path("mandatoryChecks"),round.body().path("evidenceChecks"),true),dossier.branchId(),frozen.revision().path("factsHash").asString(),true);
        inputs.requireEvaluation(evaluation);
        store.jdbc().update("insert into cg_round_evaluation(id,round_id,actor_id,evaluation,recorded_at) values(?,?,?,?::jsonb,?)",UUID.randomUUID(),round.id(),actor.id(),json.stringify(evaluation),Timestamp.from(clock.instant()));
    }
    private boolean hasCurrentAssessment(EntityStore.Entity round) {
        var latest=store.jdbc().query("select actor_id from cg_round_assessment where round_id=? order by recorded_at desc,id desc limit 1",(rs,row)->rs.getObject(1,UUID.class),round.id());
        return !latest.isEmpty()&&round.body().path("steps").get(0).path("reviewerId").asString().equals(latest.getFirst().toString());
    }
    private void reassign(EntityStore.Entity dossier,EntityStore.Entity round,RevisionStore.Frozen frozen,JsonNode request,ObjectNode body,Actor actor) {
        var level=request.path("level").asString();var target=findStep(body,level);
        if(!Set.of("PENDING","WAITING").contains(target.path("state").asString()))throw ApiException.of("STATE_CONFLICT","Không thay người của bước đã quyết định.");
        var replacement=uuid(request,"reviewerId");inputs.eligible(replacement,level,dossier,frozen.revision(),frozen.submittedBy());
        for(var other:body.path("steps"))if(!other.path("level").asString().equals(level)&&other.path("reviewerId").asString().equals(replacement.toString()))throw ApiException.of("SOD_VIOLATION","L1 và L2 phải khác nhau.");
        var old=uuid(target,"reviewerId");if(old.equals(replacement))throw ApiException.validation("/reviewerId","Phải chọn người thay thế khác.");
        store.jdbc().update("insert into cg_round_reassignment(id,round_id,level,old_reviewer_id,new_reviewer_id,actor_id,reason,recorded_at) values(?,?,?,?,?,?,?,?)",UUID.randomUUID(),round.id(),level,old,replacement,actor.id(),request.path("reason").asString(),Timestamp.from(clock.instant()));
        target.put("reviewerId",replacement.toString());
        if(level.equals("L1")){body.set("evidenceChecks",json.array());body.put("authorityChecked",false);}
    }
    private void authorize(UUID id,JsonNode request,Actor actor) {
        var round=require(id,actor);var dossier=access.require(uuid(round.body(),"targetId"),actor);var frozen=frozen(round);
        var current=users.actor(actor.id());current.requireBranch(dossier.branchId());
        if(inputs.excluded(frozen.revision(),frozen.submittedBy()).contains(actor.id()))throw ApiException.of("SOD_VIOLATION","Người đóng góp hoặc yêu cầu không được xét duyệt nguồn.");
        if(request.path("action").asString().equals("REASSIGN")){current.requireRole("MANAGER");return;}
        String level=request.path("action").asString().equals("VERIFY_EVIDENCE")?"L1":request.path("level").asString();
        var step=findStep(round.body(),level);
        if(!step.path("reviewerId").asString().equals(actor.id().toString()))throw ApiException.of("ROLE_FORBIDDEN","Thao tác dành cho người duyệt được chỉ định.");
        inputs.eligible(actor.id(),level,dossier,frozen.revision(),frozen.submittedBy());
    }
    private EntityStore.Entity require(UUID id,Actor actor) {
        var round=store.require("Round",id);actor.requireBranch(round.branchId());
        if(!round.body().path("targetKind").asString().equals("DOSSIER"))throw ApiException.of("SERVICE_UNAVAILABLE","Adapter nguồn xét duyệt chưa được triển khai.");
        var dossier=access.require(uuid(round.body(),"targetId"),actor);requireBinding(round,dossier);return round;
    }
    private void requireBinding(EntityStore.Entity round,EntityStore.Entity dossier) {
        if(!round.body().path("targetKind").asString().equals("DOSSIER")||!round.body().path("targetId").asString().equals(dossier.id().toString())||!Objects.equals(round.branchId(),dossier.branchId()))throw ApiException.of("NOT_FOUND","Vòng xét duyệt không thuộc nguồn này.");
    }
    private void requireOpen(EntityStore.Entity round,EntityStore.Entity dossier) {
        if(!round.body().path("state").asString().equals("OPEN")||!dossier.body().path("state").asString().equals("IN_REVIEW")||!dossier.body().path("roundId").asString().equals(round.id().toString()))throw ApiException.of("STATE_CONFLICT","Vòng xét duyệt không còn mở trên nguồn hiện tại.");
    }
    private RevisionStore.Frozen frozen(EntityStore.Entity round){return revisions.require(uuid(round.body(),"targetId"),uuid(round.body(),"revisionId"));}
    private void validateReviewers(EntityStore.Entity dossier,JsonNode revision,UUID requester,JsonNode steps) {
        var seen=new HashSet<UUID>();for(var step:steps){var id=uuid(step,"reviewerId");if(!seen.add(id))throw ApiException.of("SOD_VIOLATION","L1 và L2 phải khác nhau.");inputs.eligible(id,step.path("level").asString(),dossier,revision,requester);}
    }
    private void requireDigest(RevisionStore.Frozen frozen,JsonNode digest) {
        if(!json.hash(digest).equals(frozen.revision().path("evidenceHash").asString())||!digest.equals(frozen.metadata().path("evidence")))throw ApiException.of("STORAGE_INTEGRITY","Bằng chứng không khớp nguồn đã chốt.");
    }
    private ObjectNode pending(JsonNode round,String level,Actor actor) {
        var step=findStep(round,level);if(!step.path("state").asString().equals("PENDING"))throw ApiException.of("STATE_CONFLICT","Bước duyệt chưa đến lượt hoặc đã quyết định.");
        if(!step.path("reviewerId").asString().equals(actor.id().toString()))throw ApiException.of("ROLE_FORBIDDEN","Không phải người duyệt bước này.");return step;
    }
    private ObjectNode findStep(JsonNode round,String level){for(var step:round.path("steps"))if(step.path("level").asString().equals(level))return (ObjectNode)step;throw ApiException.of("STATE_CONFLICT","Vòng xét duyệt không có cấp này.");}
    private void decide(ObjectNode step,String state,JsonNode request,Actor actor){step.put("state",state);step.put("decidedBy",actor.id().toString());step.put("decisionAt",now());step.set("reason",json.nfc(request.path("reason")));}
    private void recordDecision(EntityStore.Entity round,JsonNode request,Actor actor){store.jdbc().update("insert into cg_round_decision(round_id,level,actor_id,action,reason,decided_at) values(?,?,?,?,?,?)",round.id(),request.path("level").asString(),actor.id(),request.path("action").asString(),request.path("reason").asString(),Timestamp.from(clock.instant()));}
    private void cancelUndecided(ObjectNode body){for(var step:body.path("steps"))if(Set.of("PENDING","WAITING").contains(step.path("state").asString()))((ObjectNode)step).put("state","CANCELLED");}
    private ObjectNode step(String level,String reviewer,String state){var s=json.object();s.put("level",level);s.put("state",state);s.put("reviewerId",reviewer);s.putNull("decidedBy");s.putNull("decisionAt");s.putNull("reason");return s;}
    private EntityStore.Entity entity(JsonNode b){return new EntityStore.Entity("Round",uuid(b,"id"),uuid(b,"branchId"),b.path("version").asInt(),b);}
    private static UUID uuid(JsonNode body,String field){return UUID.fromString(body.path(field).asString());}
    private String now(){return JsonSupport.timestamp(clock.instant());}

    private boolean controlRound(UUID id){return store.require("Round",id).body().path("targetKind").asString().equals("CONTROL_REQUEST");}
    private boolean changeRound(UUID id){return store.require("Round",id).body().path("targetKind").asString().equals("CHANGE");}
    public JsonNode project(EntityStore.Entity round,Actor actor) {
        if(round.body().path("targetKind").asString().equals("CONTROL_REQUEST"))return controls.getObject().project(round,actor);
        if(round.body().path("targetKind").asString().equals("CHANGE"))return changes.getObject().project(round,actor);
        var body=(ObjectNode)round.body().deepCopy();var allowed=json.array();var blocked=json.array();
        if(body.path("state").asString().equals("OPEN")) {
            for(var action:List.of("VERIFY_EVIDENCE","APPROVE","RETURN","REJECT","REASSIGN")) {
                var request=json.object();request.put("action",action);
                if(!action.equals("VERIFY_EVIDENCE")&&!action.equals("REASSIGN")) {
                    String level=null;for(var step:body.path("steps"))if(step.path("reviewerId").asString().equals(actor.id().toString())&&step.path("state").asString().equals("PENDING"))level=step.path("level").asString();
                    if(level==null)continue;request.put("level",level);
                }
                try {
                    authorize(round.id(),request,actor);
                    if(action.equals("VERIFY_EVIDENCE")&&!body.path("steps").get(0).path("state").asString().equals("PENDING"))continue;
                    if(action.equals("APPROVE")) {
                        if((body.path("steps").size()==1||request.path("level").asString().equals("L2"))&&originals.getIfAvailable()==null)throw ApiException.of("SERVICE_UNAVAILABLE","Module hợp đồng và PDF chưa sẵn sàng.");
                        if(!body.path("authorityChecked").asBoolean())throw ApiException.of("AUTHORITY_INVALID","Chưa xác minh thẩm quyền.");
                        var dossier=store.require("Dossier",uuid(body,"targetId"));var frozen=frozen(round);
                        validateReviewers(dossier,frozen.revision(),frozen.submittedBy(),body.path("steps"));
                        inputs.requireEvaluation(policies.evaluateFrozen(uuid(body,"policyVersionId"),inputs.facts(frozen.revision(),body.path("steps"),body.path("mandatoryChecks"),body.path("evidenceChecks"),true),round.branchId(),frozen.revision().path("factsHash").asString(),true));
                    }
                    allowed.add(action);
                } catch(ApiException blockedAction) {
                    // Only workflow actors receive hints; readers do not gain command visibility.
                    boolean relevant=action.equals("REASSIGN")?actor.hasRole("MANAGER"):
                            action.equals("VERIFY_EVIDENCE")?body.path("steps").get(0).path("reviewerId").asString().equals(actor.id().toString()):
                            java.util.stream.StreamSupport.stream(body.path("steps").spliterator(),false).anyMatch(s->s.path("reviewerId").asString().equals(actor.id().toString()));
                    if(relevant)blocked.add(json.object().put("action",action).put("code",blockedAction.code()).put("reason",blockedAction.getMessage()));
                }
            }
        }
        body.set("allowedActions",allowed);body.set("blockedActions",blocked);return body;
    }
    private JsonNode redactRevision(JsonNode revision,EntityStore.Entity dossier,Actor actor) {
        var result=(ObjectNode)revision.deepCopy();var relationship=store.require("Relationship",uuid(dossier.body(),"relationshipId"));
        boolean full=actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER")||(actor.hasRole("MAKER")&&actor.id().toString().equals(relationship.body().path("assignedMakerId").asString()));
        if(actor.hasRole("AUDITOR")&&!actor.hasPermission("SENSITIVE_READ")&&!full) {
            var r=(ObjectNode)result.path("representation");for(var key:List.of("personName","title","scopeText"))r.put(key,"[REDACTED]");
            var c=(ObjectNode)result.path("contact");c.put("personName","[REDACTED]");c.put("roleLabel","[REDACTED]");c.put("email","redacted@example.invalid");c.put("phone","0000000000");
        }
        return result;
    }
}
