package vn.contractgovernance.api.approval;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.dossier.DossierService;
import vn.contractgovernance.api.files.FileService;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

/** Only real source associations and immutable byte digests enter business evaluation. */
@Component
public class ApprovalInputs {
    private final EntityStore store;
    private final FileService files;
    private final UserDirectory users;
    private final JsonSupport json;
    private final Clock clock;
    public ApprovalInputs(EntityStore store, FileService files, UserDirectory users, JsonSupport json, Clock clock) {
        this.store=store; this.files=files; this.users=users; this.json=json; this.clock=clock;
    }
    public JsonNode evidence(EntityStore.Entity dossier, JsonNode source, Actor actor) {
        var values=new ArrayList<JsonNode>();
        for(var id:DossierService.referenceIds(source).stream().sorted().toList()) {
            store.lock("File",id);
            var file=files.requireClean(id,"DOSSIER",dossier.id(),dossier.branchId(),actor);
            if(store.jdbc().queryForObject("select count(*) from cg_file_association where file_id=? and target_type='DOSSIER' and target_id=?",Long.class,id,dossier.id())!=1L)
                throw ApiException.of("NOT_FOUND","Bằng chứng chưa được gắn với nguồn hồ sơ.");
            var digest=file.body().path("sha256").asString();
            if(!digest.equals(files.blob(id).hash())) throw ApiException.of("STORAGE_INTEGRITY","Metadata bằng chứng không khớp bytes đã quét.");
            // Private storage verifies the exact digest. This is an integrity read, not scanner/human verification.
            files.bytesForScan(id,digest);
            var item=json.object(); item.put("fileId",id.toString()); item.set("purpose",file.body().path("purpose")); item.put("sha256",digest); values.add(item);
        }
        values.sort(Comparator.comparing((JsonNode v)->v.path("purpose").asString()).thenComparing(v->v.path("fileId").asString()));
        var result=json.array(); values.forEach(result::add); return result;
    }
    public Set<UUID> excluded(JsonNode revision, UUID requester) {
        var ids=new HashSet<UUID>(); revision.path("contributorIds").forEach(id->ids.add(UUID.fromString(id.asString()))); ids.add(requester); return ids;
    }
    public void eligible(UUID reviewer, String level, EntityStore.Entity dossier, JsonNode revision, UUID requester) {
        users.requireEligible(reviewer,dossier.branchId(),level.equals("L1")?"CHECKER":"MANAGER",excluded(revision,requester));
        var actor=users.actor(reviewer);
        for(var id:DossierService.referenceIds(revision)) {
            var file=store.require("File",id);
            if(file.body().path("sensitive").asBoolean()&&!actor.hasPermission("SENSITIVE_READ"))
                throw ApiException.of("SENSITIVE_FORBIDDEN","Người duyệt thiếu quyền đọc bằng chứng nhạy cảm.");
        }
    }
    public void mandatory(JsonNode checks, EntityStore.Entity dossier, Set<UUID> frozenIds, Actor actor) {
        var codes=new HashSet<String>();
        for(var check:checks) {
            if(!codes.add(check.path("code").asString())) throw ApiException.validation("/mandatoryChecks","Mã điều kiện không được trùng.");
            nonfuture(check.path("checkedAt"),"/mandatoryChecks/checkedAt");
            var ids=new HashSet<UUID>();
            for(var item:check.path("evidenceFileIds")) {
                var id=UUID.fromString(item.asString());
                if(!ids.add(id))throw ApiException.validation("/mandatoryChecks/evidenceFileIds","Bằng chứng không được trùng.");
                if(!frozenIds.contains(id))throw ApiException.validation("/mandatoryChecks/evidenceFileIds","Điều kiện phải dẫn chiếu bằng chứng thuộc nguồn chốt.");
                files.requireClean(id,"DOSSIER",dossier.id(),dossier.branchId(),actor);
            }
        }
    }
    /** A known reviewed condition cannot disappear merely by omitting its code in the next assessment. */
    public void retainConditions(JsonNode previous,JsonNode next) {
        var codes=new HashSet<String>();next.forEach(check->codes.add(check.path("code").asString()));
        for(var check:previous)if(!codes.contains(check.path("code").asString()))throw ApiException.of(
                switch(check.path("code").asString()) {
                    case "AUTHORITY_CURRENT"->"AUTHORITY_INVALID";
                    case "LEGAL_CONDITION"->"LEGAL_REVIEW_REQUIRED";
                    case "SERVICE_READINESS"->"READINESS_BLOCKED";
                    default->"POLICY_BLOCKED";
                },"Điều kiện hiện có phải được đánh giá lại rõ ràng; không được bỏ khỏi danh sách.");
    }
    public void nonfuture(JsonNode timestamp, String path) {
        if(Instant.parse(timestamp.asString()).isAfter(clock.instant()))throw ApiException.validation(path,"Thời điểm kiểm tra không được ở tương lai.");
    }
    public JsonNode facts(JsonNode revision, JsonNode steps, JsonNode mandatory, JsonNode checks, boolean authorityChecked) {
        var result=json.object(); result.set("terms",revision.path("terms")); result.set("representation",revision.path("representation").deepCopy());
        var docs=json.array();
        for(var link:revision.path("documents")) {
            var doc=json.object(); doc.set("purpose",link.path("purpose")); doc.put("clean",true); doc.put("humanVerified",passed(checks,link.path("fileId").asString())); docs.add(doc);
        }
        result.set("documents",docs); result.set("contributorIds",revision.path("contributorIds")); result.putNull("candidateL1Id"); result.putNull("candidateL2Id");
        for(var step:steps)result.set(step.path("level").asString().equals("L1")?"candidateL1Id":"candidateL2Id",step.path("reviewerId"));
        result.set("mandatoryChecks",mandatory);
        // Assessment is ephemeral input to current evaluation, never a rewrite of submitted authority.
        ((ObjectNode)result.path("representation")).putNull("checkedAt").putNull("checkEvidenceFileId");
        if(authorityChecked) {
            var authority=(ObjectNode)result.path("representation"); String source=authority.path("sourceFileId").asString();
            for(var check:checks)if(check.path("fileId").asString().equals(source)&&check.path("result").asString().equals("PASS")) {
                authority.set("checkedAt",check.path("checkedAt")); authority.put("checkEvidenceFileId",source);
            }
        } else {
            ((ObjectNode)result.path("representation")).putNull("checkedAt").putNull("checkEvidenceFileId");
        }
        return result;
    }
    private boolean passed(JsonNode checks,String id) {
        for(var check:checks)if(check.path("fileId").asString().equals(id))return check.path("result").asString().equals("PASS"); return false;
    }
    public void requireEvaluation(JsonNode evaluation) {
        if(!evaluation.path("route").asString().equals("BLOCKED"))return;
        String code="POLICY_BLOCKED";
        for(var r:evaluation.path("results"))if(!r.path("result").asString().equals("PASS")) {
            code=switch(r.path("ruleId").asString()) {
                case "REVIEWER_SOD"->"SOD_VIOLATION";
                case "AUTHORITY","MANDATORY_AUTHORITY_CURRENT"->"AUTHORITY_INVALID";
                case "MANDATORY_LEGAL_CONDITION"->"LEGAL_REVIEW_REQUIRED";
                case "MANDATORY_SERVICE_READINESS"->"READINESS_BLOCKED";
                case "REQUIRED_DOCUMENTS"->"FILE_NOT_CLEAN";
                default->"POLICY_BLOCKED";
            }; break;
        }
        throw ApiException.of(code,"Nguồn hiện tại chưa đáp ứng điều kiện xét duyệt.");
    }
}
