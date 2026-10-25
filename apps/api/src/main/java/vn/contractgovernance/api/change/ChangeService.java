package vn.contractgovernance.api.change;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.*;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.policy.PolicyAccess;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.signing.SigningEvidence;

/** Change source and slot authority. All existing-contract mutations lock Dossier/Contract first. */
@Service
public class ChangeService {
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final ContractLifecycle lifecycle;
    private final OriginalAgreementService originals;
    private final CustomerAccess customers;
    private final UserDirectory users;
    private final CommandExecutor commands;
    private final AuditService audit;
    private final ContractRegistry schemas;
    private final ListQueries lists;
    private final PolicyAccess policies;
    private final FileService files;
    private final EvidenceAccess evidence;
    private final SigningEvidence signingEvidence;
    private final JobQueue jobs;
    private final ObjectProvider<ChangeNoticeBridge> notices;
    private final ObjectProvider<ChangeApproval> approval;
    private final ObjectProvider<ChangeSigning> signing;
    public ChangeService(EntityStore store,JsonSupport json,Clock clock,CalendarService calendar,
            ContractLifecycle lifecycle,OriginalAgreementService originals,CustomerAccess customers,
            UserDirectory users,CommandExecutor commands,AuditService audit,ContractRegistry schemas,
            ListQueries lists,PolicyAccess policies,FileService files,EvidenceAccess evidence,SigningEvidence signingEvidence,JobQueue jobs,
            ObjectProvider<ChangeApproval> approval,ObjectProvider<ChangeSigning> signing,ObjectProvider<ChangeNoticeBridge> notices) {
        this.store=store; this.json=json; this.clock=clock; this.calendar=calendar; this.lifecycle=lifecycle;
        this.originals=originals; this.customers=customers; this.users=users; this.commands=commands;
        this.notices=notices; this.audit=audit; this.schemas=schemas; this.lists=lists; this.policies=policies;
        this.files=files; this.evidence=evidence; this.signingEvidence=signingEvidence; this.jobs=jobs; this.approval=approval; this.signing=signing;
    }
    public static UUID id(JsonNode body,String field) { return SigningEvidence.uuid(body,field); }
    public String now() { return JsonSupport.timestamp(clock.instant()); }
    public EntityStore.Entity require(UUID id,Actor actor) {
        var change=store.require("Change",id); users.actor(actor.id()).requireBranch(change.branchId());
        lifecycle.require(id(change.body(),"contractId"),actor); return change;
    }
    public EntityStore.Entity lock(UUID id) {
        var change=store.require("Change",id);
        lifecycle.lock(id(change.body(),"contractId")); return store.lock("Change",id);
    }
    public void maker(UUID id,Actor actor) {
        var change=require(id,actor); makerContract(id(change.body(),"contractId"),actor);
    }
    public void makerContract(UUID id,Actor actor) {
        var contract=lifecycle.require(id,actor);
        customers.authorizeAssignedMaker(id(contract.body(),"relationshipId"),users.actor(actor.id()));
    }
    public Set<UUID> contributors(EntityStore.Entity change) {
        var result=new TreeSet<>(SigningEvidence.uniqueIds(change.body().path("contributorIds"),"/contributorIds"));
        store.jdbc().query("select actor_id from cg_change_contributor where change_id=?",(rs,n)->rs.getObject(1,UUID.class),change.id()).forEach(result::add);
        store.jdbc().query("select body->>'uploadedBy' from cg_entity where entity_type='File' and body->>'targetKind'='CHANGE' and body->>'targetId'=? and body->>'uploadedBy' is not null",
                (rs,n)->UUID.fromString(rs.getString(1)),change.id().toString()).forEach(result::add);
        return result;
    }
    public void contribute(UUID changeId,UUID actor) {
        store.jdbc().update("insert into cg_change_contributor(change_id,actor_id,created_at) values(?,?,?) on conflict do nothing",changeId,actor,Timestamp.from(clock.instant()));
        if(contributors(store.require("Change",changeId)).size()>50) throw ApiException.validation("/contributorIds","Vượt giới hạn người đóng góp.");
    }
    public EntityStore.Entity save(EntityStore.Entity change,ObjectNode body,Actor actor,String action,String reason) {
        body.put("updatedAt",now()); var updated=store.update(change,body);
        schemas.validateSchema("Change",updated.body()); audit.record(actor,action,updated,reason); return updated;
    }
    public JsonNode list(Map<String,String> query,Actor actor) {
        actor=users.actor(actor.id()); actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR");
        var args=new ArrayList<Object>(); String where="exists(select 1 from cg_entity c join cg_entity r on r.entity_type='Relationship' and r.id=(c.body->>'relationshipId')::uuid and r.branch_id=c.branch_id where c.entity_type='Contract' and c.id=(e.body->>'contractId')::uuid and c.branch_id=e.branch_id and "+customers.businessSqlScope(actor,"r")+")";
        if(!actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR")) args.add(actor.id().toString());
        var current=actor; return lists.page("Change",actor,query,false,where,args,b->project(b,current));
    }
    public JsonNode get(UUID id,Actor actor) {
        var change=require(id,actor); lifecycle.maintain(id(change.body(),"contractId"),actor);
        return project(require(id,actor).body(),actor);
    }
    public ApiResult create(JsonNode request,String key,Actor actor) {
        UUID contractId=id(request,"contractId"); makerContract(contractId,actor);
        return commands.execute(actor,key,"POST","/api/v1/changes",request,null,
                ()->makerContract(contractId,actor),()->lifecycle.maintain(contractId,actor),()->{
            var contract=lifecycle.lock(contractId); makerContract(contractId,actor); active(contract);
            var body=(ObjectNode)request.deepCopy(); body.put("createdAt",now()).put("updatedAt",now())
                    .put("branchId",contract.branchId().toString()).put("state","DRAFT")
                    .put("assignedMakerId",actor.id().toString());
            body.set("contributorIds",json.array().add(actor.id().toString()));
            for(String field:List.of("evaluation","roundId","artifactId","artifactHash","artifactJobId","signingPacketId","noticeId","cancelScheduleRequest","appliedVersionId","conflictReason","committedScheduleVersion")) body.putNull(field);
            body.set("allowedActions",json.array()); body.set("blockedActions",json.array());
            currentBase(contract,body); validateTerms(contract,body,policies.findApplicableHint().orElse(null));
            var change=store.create("Change",UUID.randomUUID(),contract.branchId(),body);
            attach(change,request.path("documents"),actor); contribute(change.id(),actor.id());
            schemas.validateSchema("Change",change.body()); audit.record(actor,"CREATE",change,request.path("reason").asString());
            return ApiResult.created(project(change.body(),actor),"/api/v1/changes/"+change.id());
        });
    }
    public ApiResult update(UUID id,JsonNode request,String tag,String key,Actor actor) {
        maker(id,actor); ETags.require(tag);
        return commands.execute(actor,key,"PUT","/api/v1/changes/"+id,request,tag,()->maker(id,actor),()->{
            var change=lock(id); maker(id,actor); ETags.check(tag,change.version()); editable(change);
            var body=(ObjectNode)change.body().deepCopy();
            for(String field:List.of("target","effectiveOn","documents","reason")) body.set(field,json.nfc(request.path(field)));
            var contract=store.require("Contract",id(body,"contractId")); active(contract);
            validateTerms(contract,body,policies.findApplicableHint().orElse(null)); attach(change,body.path("documents"),actor);
            contribute(id,actor.id()); body.set("contributorIds",json.value(contributors(change).stream().map(UUID::toString).toList()));
            return ApiResult.ok(project(save(change,body,actor,"UPDATE",body.path("reason").asString()).body(),actor));
        });
    }
    public ApiResult command(UUID id,JsonNode request,String tag,String key,Actor actor) {
        authorize(id,request.path("action").asString(),actor); ETags.require(tag);
        var contractId=id(require(id,actor).body(),"contractId");
        return commands.execute(actor,key,"POST","/api/v1/changes/"+id+"/commands",request,tag,
                ()->authorize(id,request.path("action").asString(),actor),()->lifecycle.maintain(contractId,actor),()->{
            var change=lock(id); authorize(id,request.path("action").asString(),actor); ETags.check(tag,change.version());
            var contract=store.require("Contract",contractId); var body=(ObjectNode)change.body().deepCopy();
            String action=request.path("action").asString();
            switch(action) {
                case "SUBMIT" -> { return ApiResult.ok(project(approval.getObject().submit(change,contract,request,actor).body(),actor)); }
                case "WITHDRAW" -> { return ApiResult.ok(project(approval.getObject().withdraw(change,request.path("reason").asString(),actor).body(),actor)); }
                case "COMMIT" -> { return commit(change,contract,request,actor); }
                case "REBASE" -> {
                    if(!Set.of("DRAFT","RETURNED","CONFLICTED").contains(body.path("state").asString())) throw ApiException.of("STATE_CONFLICT","Không được rebase nguồn này.");
                    active(contract); var proposed=(ObjectNode)body.deepCopy();
                    proposed.set("baseContractVersionId",request.path("baseContractVersionId")); proposed.set("expectedScheduleVersion",request.path("expectedScheduleVersion")); currentBase(contract,proposed);
                    cleanup(change,"REBASE",actor); body=proposed;
                    body.put("state","DRAFT"); for(String field:List.of("evaluation","roundId","artifactId","artifactHash","artifactJobId","signingPacketId","noticeId","cancelScheduleRequest","conflictReason","committedScheduleVersion")) body.putNull(field);
                    contribute(id,actor.id()); body.set("contributorIds",json.value(contributors(change).stream().map(UUID::toString).toList()));
                    event(id,"REBASE",request,actor);
                }
                case "CANCEL" -> {
                    if(!Set.of("DRAFT","RETURNED","APPROVED","READY").contains(body.path("state").asString())) throw ApiException.of("STATE_CONFLICT","Nguồn không cho phép hủy trực tiếp.");
                    cleanup(change,"CANCEL",actor); body.put("state","CANCELLED");
                }
                case "REQUEST_CANCEL_SCHEDULE" -> {
                    scheduledBefore(change); if(body.hasNonNull("cancelScheduleRequest")&&body.path("cancelScheduleRequest").path("state").asString().equals("PENDING")) throw ApiException.of("STATE_CONFLICT","Yêu cầu hủy đang chờ.");
                    manifest(change,SigningEvidence.uniqueIds(request.path("evidenceFileIds"),"/evidenceFileIds"),actor);
                    var cancel=json.object().put("id",UUID.randomUUID().toString()).put("requesterId",actor.id().toString()).put("reason",request.path("reason").asString()).put("state","PENDING");
                    cancel.set("contributors",json.array().add(actor.id().toString())); cancel.set("evidenceFileIds",request.path("evidenceFileIds"));
                    cancel.putNull("requestedDueAt").putNull("decisionBy").putNull("decidedAt"); body.set("cancelScheduleRequest",cancel); event(id,action,cancel,actor);
                }
                case "APPROVE_CANCEL_SCHEDULE","REJECT_CANCEL_SCHEDULE" -> {
                    scheduledBefore(change); var cancel=(ObjectNode)body.path("cancelScheduleRequest").deepCopy();
                    if(!cancel.path("state").asString().equals("PENDING")) throw ApiException.of("STATE_CONFLICT","Không có yêu cầu hủy chờ quyết định.");
                    if(action.equals("APPROVE_CANCEL_SCHEDULE")) {
                        manifest(change,SigningEvidence.uniqueIds(request.path("evidenceFileIds"),"/evidenceFileIds"),actor);
                        cleanup(change,action,actor); body.put("state","CANCELLED"); releaseSlot(contract,change,actor,action);
                    }
                    cancel.put("state",action.startsWith("APPROVE")?"APPROVED":"REJECTED").put("decisionBy",actor.id().toString()).put("decidedAt",now());
                    body.set("cancelScheduleRequest",cancel); event(id,action,request,actor);
                }
                default -> throw ApiException.validation("/action","Thao tác không được hỗ trợ.");
            }
            return ApiResult.ok(project(save(change,body,actor,action,request.path("reason").asString()).body(),actor));
        });
    }
    private void authorize(UUID id,String action,Actor actor) {
        var change=require(id,actor);
        if(action.equals("APPROVE_CANCEL_SCHEDULE")||action.equals("REJECT_CANCEL_SCHEDULE")) {
            users.actor(actor.id()).requireRole("MANAGER");
            var excluded=contributors(change); var cancel=change.body().path("cancelScheduleRequest");
            if(cancel.hasNonNull("requesterId")) excluded.add(id(cancel,"requesterId"));
            for(var c:cancel.path("contributors")) excluded.add(UUID.fromString(c.asString()));
            if(excluded.contains(actor.id())) throw ApiException.of("SOD_VIOLATION","Quyết định hủy cần manager độc lập.");
        } else maker(id,actor);
    }
    private ApiResult commit(EntityStore.Entity change,EntityStore.Entity contract,JsonNode request,Actor actor) {
        if(!change.body().path("state").asString().equals("READY")) throw ApiException.of(change.body().path("target").path("kind").asString().equals("TARIFF_NOTICE")?"NOTICE_NOT_READY":"READINESS_BLOCKED","Change chưa READY.");
        active(contract); beforeBoundary(change);
        String code=null;
        if(!request.path("expectedBaseContractVersionId").equals(change.body().path("baseContractVersionId"))||!contract.body().path("currentVersion").path("id").equals(change.body().path("baseContractVersionId"))) code="BASE_CONFLICT";
        else if(!counter(request.path("expectedScheduleVersion"),change.body().path("expectedScheduleVersion"))||!counter(contract.body().path("scheduleVersion"),change.body().path("expectedScheduleVersion"))||contract.body().hasNonNull("scheduledChangeId")) code="SCHEDULE_CONFLICT";
        if(code!=null) {
            var body=(ObjectNode)change.body().deepCopy(); body.put("state","CONFLICTED").put("conflictReason",code);
            var updated=save(change,body,actor,code,"Current base or future slot differs");
            var problem=json.object().put("type","urn:contract-governance:problem:"+code.toLowerCase(Locale.ROOT)).put("title",code).put("status",409).put("detail","Base hoặc future slot đã thay đổi; cần explicit rebase.")
                    .put("instance","/api/v1/changes/"+change.id()+"/commands").put("code",code).put("traceId",AuditService.traceId().toString()).put("currentVersion",updated.version()); problem.set("violations",json.array());
            return new ApiResult(409,problem,ETags.format(updated.version()),null);
        }
        signingEvidence.requireMaterial(contract); signing.getObject().requireReady(change,null); requireCurrentGuard(contract);
        long next=contract.body().path("scheduleVersion").asLong()+1;
        var cb=(ObjectNode)contract.body().deepCopy(); cb.put("scheduleVersion",next).put("scheduledChangeId",change.id().toString());
        lifecycle.save(contract,cb,actor,"CHANGE_SCHEDULED",null);
        var body=(ObjectNode)change.body().deepCopy(); body.put("state","SCHEDULED").put("committedScheduleVersion",next);
        var updated=save(change,body,actor,"COMMIT",null); enqueueEffect(updated); return ApiResult.ok(project(updated.body(),actor));
    }
    public void requireCurrentGuard(EntityStore.Entity contract) {
        if(!contract.body().path("readiness").asString().equals("READY")||ContractLifecycle.unresolvedRisk(contract.body())) throw ApiException.of("READINESS_BLOCKED","Hợp đồng có readiness risk.");
    }
    public void currentBase(EntityStore.Entity contract,JsonNode change) {
        if(!contract.body().path("currentVersion").path("id").equals(change.path("baseContractVersionId"))) throw ApiException.of("BASE_CONFLICT","Base version không còn current.");
        if(!counter(contract.body().path("scheduleVersion"),change.path("expectedScheduleVersion"))) throw ApiException.of("SCHEDULE_CONFLICT","Schedule version không còn current.");
    }
    public static boolean counter(JsonNode a,JsonNode b) {
        try { return new BigDecimal(a.asString()).toBigIntegerExact().equals(new BigDecimal(b.asString()).toBigIntegerExact()); }
        catch(ArithmeticException|NumberFormatException e) { return false; }
    }
    public void active(EntityStore.Entity contract) {
        if(!contract.body().path("state").asString().equals("ACTIVE")) throw ApiException.of("STATE_CONFLICT","Change chỉ dùng cho hợp đồng ACTIVE.");
    }
    private void editable(EntityStore.Entity change) {
        if(!Set.of("DRAFT","RETURNED").contains(change.body().path("state").asString())) throw ApiException.of("STATE_CONFLICT","Nguồn không còn sửa được.");
    }
    public ObjectNode consolidated(EntityStore.Entity contract,JsonNode change) {
        var terms=(ObjectNode)contract.body().path("currentVersion").path("terms").deepCopy();
        for(String field:List.of("feeVnd","endExclusive")) if(change.path("target").has(field)) terms.set(field,change.path("target").path(field));
        return terms;
    }
    public void validateTerms(EntityStore.Entity contract,JsonNode change,EntityStore.Entity policy) {
        var base=contract.body().path("currentVersion").path("terms"); var proposed=consolidated(contract,change);
        var on=LocalDate.parse(change.path("effectiveOn").asString()); var today=calendar.localDate(clock.instant());
        if(!on.isAfter(today)) throw ApiException.validation("/effectiveOn","Ngày hiệu lực phải từ ngày địa phương kế tiếp.");
        var kind=change.path("target").path("kind").asString(); boolean fixed=base.path("termKind").asString().equals("FIXED");
        if(kind.equals("RENEWAL")) {
            if(!fixed) throw ApiException.of("NO_RENEWAL","OPEN_ENDED không được renewal.");
            var end=LocalDate.parse(base.path("endExclusive").asString());
            if(!on.equals(end)||!LocalDate.parse(proposed.path("endExclusive").asString()).isAfter(end)) throw ApiException.validation("/target/endExclusive","Renewal phải nối đúng boundary và tăng thời hạn.");
            if(policy!=null) duration(ChronoUnit.DAYS.between(end,LocalDate.parse(proposed.path("endExclusive").asString())),policy.body().path("config"),true);
        } else {
            if(fixed&&!on.isBefore(LocalDate.parse(base.path("endExclusive").asString()))) throw ApiException.validation("/effectiveOn","Phải trước hết hạn hiện hành.");
            if(!fixed&&change.path("target").has("endExclusive")) throw ApiException.validation("/target/endExclusive","OPEN_ENDED chỉ thay phí.");
            if(fixed) {
                var end=LocalDate.parse(proposed.path("endExclusive").asString());
                if(!end.isAfter(on)) throw ApiException.validation("/target/endExclusive","Thời hạn phải sau ngày hiệu lực.");
                if(policy!=null) duration(ChronoUnit.DAYS.between(LocalDate.parse(base.path("startOn").asString()),end),policy.body().path("config"),false);
            }
            if(kind.equals("TARIFF_NOTICE")&&(!base.path("pricingMode").asString().equals("STANDARD_TARIFF")||!base.path("clauseVersion").asString().equals("TARIFF-ACK-7D-1"))) throw ApiException.of("NOTICE_NOT_READY","Không có điều khoản tariff hợp lệ.");
        }
    }
    private void duration(long days,JsonNode config,boolean renewal) {
        String min="minFixedTermDays",max="maxFixedTermDays";
        if(days<config.path(min).asLong()||days>config.path(max).asLong()) throw ApiException.of("POLICY_BLOCKED","Thời hạn không đáp ứng chính sách.");
    }
    public void beforeBoundary(EntityStore.Entity change) {
        if(!clock.instant().isBefore(calendar.startOfDay(LocalDate.parse(change.body().path("effectiveOn").asString())))) throw ApiException.of("STATE_CONFLICT","Đã đến boundary; không backdate command.");
    }
    private void scheduledBefore(EntityStore.Entity change) {
        if(!change.body().path("state").asString().equals("SCHEDULED")) throw ApiException.of("STATE_CONFLICT","Change không SCHEDULED."); beforeBoundary(change);
    }
    public Set<UUID> references(JsonNode source) {
        var ids=new TreeSet<UUID>(); for(var link:source.path("documents")) if(!ids.add(id(link,"fileId"))) throw ApiException.validation("/documents","File không được trùng.");
        return ids;
    }
    private void attach(EntityStore.Entity change,JsonNode links,Actor actor) {
        var seen=new HashSet<UUID>();
        for(var link:links) {
            var id=id(link,"fileId"); if(!seen.add(id)) throw ApiException.validation("/documents","File không được trùng.");
            var file=evidence.requireClean(id,"CHANGE",change.id(),change.branchId(),actor);
            if(!file.body().path("purpose").equals(link.path("purpose"))) throw ApiException.validation("/documents/purpose","Purpose không khớp file.");
        }
        files.validateDraftLimits("CHANGE",change.id(),seen);
        files.synchronizeDraftReferences("CHANGE",change.id(),change.branchId(),new TreeSet<>(seen),actor);
    }
    public JsonNode manifest(EntityStore.Entity change,Collection<UUID> ids,Actor actor){return manifest(change,ids,actor,true);}
    public JsonNode manifest(EntityStore.Entity change,Collection<UUID> ids,Actor actor,boolean attach) {
        var values=new ArrayList<JsonNode>();
        for(var id:new TreeSet<>(ids)) {
            var file=attach?store.lock("File",id):store.require("File",id);
            if(!file.body().path("targetKind").asString().equals("CHANGE")||!file.body().path("targetId").asString().equals(change.id().toString())||!Objects.equals(file.branchId(),change.branchId())) throw ApiException.of("NOT_FOUND","Bằng chứng không thuộc đúng Change.");
            if(!file.body().path("state").asString().equals("CLEAN")) throw ApiException.of("FILE_NOT_CLEAN","Bằng chứng chưa CLEAN.");
            if(actor!=null) { evidence.requireFile(id,actor); evidence.requireFull(file,actor); }
            var hash=file.body().path("sha256").asString(); if(!hash.equals(files.blob(id).hash())) throw ApiException.of("STORAGE_INTEGRITY","Metadata không khớp bytes."); files.bytesForScan(id,hash);
            values.add(json.object().put("fileId",id.toString()).put("purpose",file.body().path("purpose").asString()).put("sha256",hash));
            if(attach)store.jdbc().update("insert into cg_file_association(file_id,target_type,target_id,historical,created_at) values(?,'CHANGE',?,true,?) on conflict(file_id,target_type,target_id) do update set historical=true",id,change.id(),Timestamp.from(clock.instant()));
            if(attach&&!file.body().path("attached").asBoolean()) {
                var attached=(ObjectNode)file.body().deepCopy(); attached.put("attached",true).put("updatedAt",now()); store.update(file,attached);
            }
        }
        values.sort(Comparator.comparing((JsonNode b)->b.path("purpose").asString()).thenComparing(b->b.path("fileId").asString())); var result=json.array(); values.forEach(result::add); return result;
    }
    private void cleanup(EntityStore.Entity change,String reason,Actor actor) {
        notices.getObject().cleanup(change,reason,actor);
        store.jdbc().update("update cg_job set state='CANCELLED',lease_owner=null,lease_until=null,error_code='GUARD_BLOCKED',updated_at=? where target_type='CHANGE' and target_id=? and state in ('QUEUED','RUNNING','FAILED')",Timestamp.from(clock.instant()),change.id());
        event(change.id(),reason,change.body(),actor);
    }
    public void detachNotice(EntityStore.Entity change,UUID noticeId,Actor actor){
        if(!change.body().path("noticeId").asString().equals(noticeId.toString()))return;
        if(!Set.of("APPROVED","READY").contains(change.body().path("state").asString()))throw ApiException.of("STATE_CONFLICT","Không thể thay Notice của Change đã scheduled/terminal.");
        var body=(ObjectNode)change.body().deepCopy();body.putNull("noticeId").put("state","APPROVED");save(change,body,actor,"NOTICE_DETACHED",noticeId.toString());
    }
    public void attachNotice(EntityStore.Entity change,UUID noticeId,Actor actor) {
        if(!change.body().path("target").path("kind").asString().equals("TARIFF_NOTICE")||!change.body().path("state").asString().equals("APPROVED")) throw ApiException.of("STATE_CONFLICT","Tariff source chưa APPROVED.");
        if(change.body().hasNonNull("noticeId")&&!change.body().path("noticeId").asString().equals(noticeId.toString())) throw ApiException.of("NOTICE_NOT_READY","Change đã có Notice khác; cần cleanup rõ ràng.");
        recheckNotice(change,actor); var body=(ObjectNode)change.body().deepCopy();body.put("noticeId",noticeId.toString());save(change,body,actor,"NOTICE_ATTACHED",null);
    }
    public void recheckNotice(EntityStore.Entity change,Actor actor) {
        var contract=store.require("Contract",id(change.body(),"contractId"));active(contract);requireCurrentGuard(contract);signingEvidence.requireMaterial(contract);
        if(!contract.body().path("currentVersion").path("id").equals(change.body().path("baseContractVersionId")))throw ApiException.of("BASE_CONFLICT","Tariff base đã thay đổi.");
        if(!change.body().path("state").asString().equals("SCHEDULED")){currentBase(contract,change.body());if(contract.body().hasNonNull("scheduledChangeId"))throw ApiException.of("SCHEDULE_CONFLICT","Future slot đã được sử dụng.");beforeBoundary(change);}
        approval.getObject().noticeCurrent(change,actor);var frozen=approval.getObject().frozen(change);if(!frozen.revision().path("target").equals(change.body().path("target"))||!frozen.revision().path("effectiveOn").equals(change.body().path("effectiveOn")))throw ApiException.of("STORAGE_INTEGRITY","Tariff khác approved revision.");
    }
    public void promoteNotice(EntityStore.Entity change,UUID noticeId,Actor actor) {
        if(!change.body().path("state").asString().equals("APPROVED")||!change.body().path("noticeId").asString().equals(noticeId.toString()))throw ApiException.of("NOTICE_NOT_READY","Notice không còn là nguồn hiện hành.");
        recheckNotice(change,actor);var body=(ObjectNode)change.body().deepCopy();body.put("state","READY");save(change,body,actor,"NOTICE_READY",null);
    }
    public JsonNode effectiveArtifact(EntityStore.Entity change) {return change.body().path("target").path("kind").asString().equals("TARIFF_NOTICE")?notices.getObject().artifact(change):change.body();}
    public void noticeReady(EntityStore.Entity change,Actor actor){notices.getObject().requireReady(change,actor);}
    public void endApplicability(EntityStore.Entity contract,Actor actor){
        var ids=store.jdbc().query("select id from cg_entity where entity_type='Change' and body->>'contractId'=? and body->>'state' not in ('APPLIED','CANCELLED','REJECTED') order by id",(rs,n)->rs.getObject(1,UUID.class),contract.id().toString());for(var id:ids){var change=store.lock("Change",id);cleanup(change,"TERMINATION",actor);if(change.body().hasNonNull("roundId")){var round=store.lock("Round",id(change.body(),"roundId"));if(round.body().path("state").asString().equals("OPEN")){var rb=(ObjectNode)round.body().deepCopy();rb.put("state","CANCELLED").put("updatedAt",now());for(var step:rb.path("steps"))if(Set.of("PENDING","WAITING").contains(step.path("state").asString()))((ObjectNode)step).put("state","CANCELLED");audit.record(actor,"CANCEL",store.update(round,rb),"Contract termination ended change applicability");}}var b=(ObjectNode)change.body().deepCopy();b.put("state","CANCELLED");save(change,b,actor,"CANCEL","Contract termination ended applicability");}
        var current=store.require("Contract",contract.id());if(current.body().hasNonNull("scheduledChangeId")){var b=(ObjectNode)current.body().deepCopy();b.putNull("scheduledChangeId").put("scheduleVersion",current.body().path("scheduleVersion").asLong()+1);lifecycle.save(current,b,actor,"RELEASE_TERMINATED_SLOT",null);}
    }
    public void event(UUID id,String kind,JsonNode body,Actor actor) {
        store.jdbc().update("insert into cg_change_event(id,change_id,kind,body,actor_id,recorded_at) values(?,?,?,?::jsonb,?,?)",UUID.randomUUID(),id,kind,json.stringify(body),actor==null?null:actor.id(),Timestamp.from(clock.instant()));
    }
    public void releaseSlot(EntityStore.Entity contract,EntityStore.Entity change,Actor actor,String reason) {
        if(!contract.body().path("scheduledChangeId").asString().equals(change.id().toString())) throw ApiException.of("SCHEDULE_CONFLICT","Slot không thuộc Change.");
        var body=(ObjectNode)contract.body().deepCopy(); body.putNull("scheduledChangeId"); body.put("scheduleVersion",contract.body().path("scheduleVersion").asLong()+1); lifecycle.save(contract,body,actor,reason,null);
    }
    public void enqueueEffect(EntityStore.Entity change) {
        var revision=approval.getObject().frozen(change); var payload=json.object().put("revisionId",revision.revision().path("id").asString()).put("factsHash",revision.revision().path("factsHash").asString()).put("boundaryAt",JsonSupport.timestamp(calendar.startOfDay(LocalDate.parse(change.body().path("effectiveOn").asString()))));
        var job=jobs.enqueue("EFFECTIVITY","CHANGE",change.id(),change.branchId(),change.version(),json.hash(payload),"EFFECTIVITY:CHANGE:"+change.id()+":"+revision.revision().path("id").asString(),payload,AuditService.traceId());
        store.jdbc().update("update cg_job set available_at=? where id=? and attempts=0 and state='QUEUED'",Timestamp.from(Instant.parse(payload.path("boundaryAt").asString())),job);
    }
    public JsonNode project(JsonNode raw,Actor actor) {
        actor=users.actor(actor.id());
        var body=(ObjectNode)raw.deepCopy(); var allowed=json.array(); var blocked=json.array();
        String state=body.path("state").asString();
        for(String action:List.of("UPDATE","SUBMIT","WITHDRAW","CANCEL","COMMIT","REBASE","REQUEST_CANCEL_SCHEDULE","APPROVE_CANCEL_SCHEDULE","REJECT_CANCEL_SCHEDULE")) {
            boolean relevant=switch(action) { case "UPDATE","SUBMIT"->Set.of("DRAFT","RETURNED").contains(state); case "WITHDRAW"->state.equals("IN_REVIEW"); case "CANCEL"->Set.of("DRAFT","RETURNED","APPROVED","READY").contains(state); case "COMMIT"->state.equals("READY"); case "REBASE"->Set.of("DRAFT","RETURNED","CONFLICTED").contains(state); default->state.equals("SCHEDULED"); };
            if(!relevant) continue;
            try {
                authorize(id(body,"id"),action,actor);
                var contract=store.require("Contract",id(body,"contractId"));
                if(action.equals("SUBMIT")) submissionHint(entity(body),contract,actor);
                if(action.equals("COMMIT")) {
                    beforeBoundary(entity(body)); active(contract); currentBase(contract,body); requireCurrentGuard(contract);
                    if(!signingEvidence.materialUnchanged(contract,false)) throw ApiException.of("READINESS_BLOCKED","Material source đã thay đổi.");
                    if(contract.body().hasNonNull("scheduledChangeId")) throw ApiException.of("SCHEDULE_CONFLICT","Future slot đã có nguồn khác.");
                }
                if(action.contains("CANCEL_SCHEDULE")) {
                    beforeBoundary(entity(body)); boolean pending=body.hasNonNull("cancelScheduleRequest")&&body.path("cancelScheduleRequest").path("state").asString().equals("PENDING");
                    if(action.equals("REQUEST_CANCEL_SCHEDULE")?pending:!pending) throw ApiException.of("STATE_CONFLICT",pending?"Yêu cầu hủy đang chờ.":"Không có yêu cầu hủy đang chờ.");
                }
                allowed.add(action);
            }
            catch(ApiException exception) { if(!actor.hasRole("AUDITOR")) blocked.add(json.object().put("action",action).put("code",exception.code()).put("reason",exception.getMessage())); }
        }
        if(state.equals("APPROVED")&&!body.path("target").path("kind").asString().equals("TARIFF_NOTICE")) {
            try {
                maker(id(body,"id"),actor); beforeBoundary(entity(body));
                var contract=store.require("Contract",id(body,"contractId")); active(contract); currentBase(contract,body); requireCurrentGuard(contract);
                if(!signingEvidence.materialUnchanged(contract,false)) throw ApiException.of("READINESS_BLOCKED","Material source đã thay đổi.");
                if(!body.hasNonNull("artifactId")) throw ApiException.of("READINESS_BLOCKED","Chưa có appendix artifact đã quét.");
                if(!store.require("File",id(body,"artifactId")).body().path("state").asString().equals("CLEAN")) throw ApiException.of("FILE_NOT_CLEAN","Appendix chưa CLEAN.");
                allowed.add("SIGN");
            } catch(ApiException exception) { if(actor.hasRole("MAKER")) blocked.add(json.object().put("action","SIGN").put("code",exception.code()).put("reason",exception.getMessage())); }
        }
        if(state.equals("APPROVED")&&body.path("target").path("kind").asString().equals("TARIFF_NOTICE")) blocked.add(json.object().put("action","COMMIT").put("code","NOTICE_NOT_READY").put("reason","Cần Notice dispatch và explicit acknowledgement được xác minh."));
        if(actor.hasRole("AUDITOR")&&!actor.hasPermission("SENSITIVE_READ")&&!actor.hasAnyRole("MAKER","CHECKER","MANAGER","DATA_OFFICER")) { body.put("reason","[REDACTED]"); if(body.hasNonNull("conflictReason")) body.put("conflictReason","[REDACTED]"); }
        body.set("allowedActions",allowed); body.set("blockedActions",blocked); return body;
    }
    /** Read-only action hint: actual SUBMIT still checks bytes, authority and reviewer input. */
    private void submissionHint(EntityStore.Entity change,EntityStore.Entity contract,Actor actor) {
        active(contract); currentBase(contract,change.body()); requireCurrentGuard(contract);
        if(!signingEvidence.materialUnchanged(contract,false)) throw ApiException.of("READINESS_BLOCKED","Material source đã thay đổi.");
        var policy=policies.findApplicableHint().orElseThrow(()->ApiException.of("POLICY_BLOCKED","Chưa có chính sách áp dụng."));
        validateTerms(contract,change.body(),policy);
        var terms=consolidated(contract,change.body()); var config=policy.body().path("config");
        long fee=Long.parseLong(terms.path("feeVnd").asString());
        if(fee<Long.parseLong(config.path("minFeeVnd").asString())||fee>Long.parseLong(config.path("maxFeeVnd").asString())||!terms.path("feePayer").asString().equals("BENEFICIARY")) throw ApiException.of("POLICY_BLOCKED","Phí hoặc bên chịu phí chưa đạt chính sách.");
        boolean allowedTerm=false; for(var kind:config.path("allowedTermKinds")) allowedTerm|=kind.asString().equals(terms.path("termKind").asString());
        if(!allowedTerm) throw ApiException.of("POLICY_BLOCKED","Loại thời hạn chưa đạt chính sách.");
        var purposes=new HashSet<String>();
        for(var link:change.body().path("documents")) {
            var file=evidence.requireClean(id(link,"fileId"),"CHANGE",change.id(),change.branchId(),actor);
            if(!file.body().path("purpose").equals(link.path("purpose"))) throw ApiException.validation("/documents/purpose","Purpose không khớp nguồn.");
            purposes.add(link.path("purpose").asString());
        }
        for(var required:policy.body().path("config").path("requiredDocumentPurposes")) if(!purposes.contains(required.asString())) throw ApiException.of("FILE_NOT_CLEAN","Thiếu bằng chứng bắt buộc cho submission.");
    }
    public EntityStore.Entity entity(JsonNode body) { return new EntityStore.Entity("Change",id(body,"id"),id(body,"branchId"),body.path("version").asInt(),body); }
}
