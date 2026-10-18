package vn.contractgovernance.api.files;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/** The durable queue owns state; cg_entity Job is maintained by a SQL snapshot trigger. */
@Service
public class JobService {
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.notice.NoticeService> notices;private final vn.contractgovernance.api.contract.EffectivityJobs effectivity;private final vn.contractgovernance.api.contract.ArtifactJobs artifacts;private final EntityStore store;private final EvidenceAccess access;private final FileService files;private final JobQueue queue;private final CommandExecutor commands;private final AuditService audit;private final ListQueries lists;private final JsonSupport json;private final Clock clock;
    public JobService(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.notice.NoticeService> notices,vn.contractgovernance.api.contract.EffectivityJobs effectivity,vn.contractgovernance.api.contract.ArtifactJobs artifacts,EntityStore store,EvidenceAccess access,FileService files,JobQueue queue,CommandExecutor commands,AuditService audit,ListQueries lists,JsonSupport json,Clock clock){this.notices=notices;this.effectivity=effectivity;this.artifacts=artifacts;this.store=store;this.access=access;this.files=files;this.queue=queue;this.commands=commands;this.audit=audit;this.lists=lists;this.json=json;this.clock=clock;}
    public EntityStore.Entity require(UUID id,Actor actor){var job=store.require("Job",id);var body=job.body();if(body.path("parentKind").asString().equals("FILE"))access.requireFile(UUID.fromString(body.path("parentId").asString()),actor);else access.parent(body.path("parentKind").asString(),UUID.fromString(body.path("parentId").asString()),actor);return job;}
    public JsonNode get(UUID id,Actor actor){return project(require(id,actor),actor);}
    public JsonNode list(Map<String,String> query,Actor actor){if(query.containsKey("branchId"))actor.requireBranch(UUID.fromString(query.get("branchId")));var args=new ArrayList<Object>();var scope=access.sqlScope(true,actor,args);return lists.page("Job",actor,query,true,scope,args,body->project(new EntityStore.Entity("Job",UUID.fromString(body.path("id").asString()),body.hasNonNull("branchId")?UUID.fromString(body.path("branchId").asString()):null,body.path("version").asInt(),body),actor));}
    public ApiResult retry(UUID id,JsonNode request,String tag,String key,Actor actor){
        authorizeRetry(id,actor);ETags.require(tag);
        return commands.execute(actor,key,"POST","/api/v1/jobs/"+id+"/commands",request,tag,()->authorizeRetry(id,actor),()->{
            var preview=require(id,actor);if(preview.body().path("kind").asString().equals("NOTICE_DISPATCH"))return ApiResult.ok(project(notices.getObject().retryJob(id,tag,request.path("reason").asString(),actor),actor));if(preview.body().path("kind").asString().equals("EFFECTIVITY"))return ApiResult.ok(project(effectivity.retry(id,tag,request.path("reason").asString(),actor),actor));if(preview.body().path("kind").asString().equals("ARTIFACT_PDF"))return ApiResult.ok(project(artifacts.retry(id,tag,request.path("reason").asString(),actor),actor));if(!preview.body().path("kind").asString().equals("FILE_SCAN"))throw ApiException.of("SERVICE_UNAVAILABLE","Module xử lý loại job này chưa được triển khai; job giữ nguyên.");
            var fileId=UUID.fromString(preview.body().path("parentId").asString());var parentFile=access.requireFile(fileId,actor);access.lockParent(parentFile.body(),actor);var file=store.lock("File",fileId);var job=store.lock("Job",id);ETags.check(tag,job.version());files.requireRetrySource(file,actor);
            if(!job.body().path("state").asString().equals("FAILED") || !id.toString().equals(file.body().path("scanJobId").asString()))throw ApiException.of("STATE_CONFLICT","Job không phải lần quét bị lỗi hiện tại của tệp.");
            var expected=store.jdbc().queryForObject("select expected_hash from cg_job where id=?",String.class,id);if(!file.body().path("sha256").asString().equals(expected))throw ApiException.of("STATE_CONFLICT","Nguồn bytes đã thay đổi; không thực hiện lại ý định cũ.");
            var body=(ObjectNode)file.body().deepCopy();body.put("state","SCANNING");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var updated=store.update(file,body);
            if(!queue.retry(id))throw ApiException.of("STATE_CONFLICT","Job đã thay đổi.");store.jdbc().update("update cg_job set expected_version=? where id=?",updated.version(),id);var next=store.require("Job",id);audit.record(actor,"RETRY",next,request.path("reason").asString());audit.record(actor,"RETRY",updated,request.path("reason").asString());return ApiResult.ok(project(next,actor));
        });
    }
    private EntityStore.Entity authorizeRetry(UUID id,Actor actor){var job=require(id,actor);if(job.body().path("kind").asString().equals("FILE_SCAN"))files.authorizeRetry(UUID.fromString(job.body().path("parentId").asString()),actor);else if(job.body().path("kind").asString().equals("ARTIFACT_PDF"))artifacts.authorize(UUID.fromString(job.body().path("parentId").asString()),actor);else if(job.body().path("kind").asString().equals("EFFECTIVITY"))effectivity.authorize(UUID.fromString(job.body().path("parentId").asString()),actor);else if(job.body().path("kind").asString().equals("NOTICE_DISPATCH"))notices.getObject().authorizeRetry(UUID.fromString(job.body().path("parentId").asString()),actor);else actor.requireRole("MAKER","CHECKER","DATA_OFFICER");return job;}
    public JsonNode project(EntityStore.Entity job,Actor actor){
        var body=(ObjectNode)job.body().deepCopy();var actions=json.array();var blocked=json.array();
        if(body.path("state").asString().equals("FAILED")) {
            if(body.path("kind").asString().equals("FILE_SCAN")){try{var file=access.requireFile(UUID.fromString(body.path("parentId").asString()),actor);files.requireRetrySource(file,actor);if(job.id().toString().equals(file.body().path("scanJobId").asString()))actions.add("RETRY");}catch(ApiException exception){if(!actor.hasRole("AUDITOR"))blocked.add(json.object().put("action","RETRY").put("code",exception.code()).put("reason","Nguồn không còn cho phép thực hiện lại job."));}}
            else if(body.path("kind").asString().equals("ARTIFACT_PDF")){try{artifacts.requireRetry(job,actor);actions.add("RETRY");}catch(ApiException e){if(!actor.hasRole("AUDITOR"))blocked.add(json.object().put("action","RETRY").put("code",e.code()).put("reason","Nguồn không cho phép sinh lại bản hợp đồng."));}}
            else if(body.path("kind").asString().equals("EFFECTIVITY")){try{effectivity.requireRetry(job,actor);actions.add("RETRY");}catch(ApiException e){if(!actor.hasRole("AUDITOR"))blocked.add(json.object().put("action","RETRY").put("code",e.code()).put("reason","Nguồn effectivity chưa đủ điều kiện retry."));}}
            else if(body.path("kind").asString().equals("NOTICE_DISPATCH")){try{notices.getObject().requireDispatchRetry(job,actor);actions.add("RETRY");}catch(ApiException e){if(!actor.hasRole("AUDITOR"))blocked.add(json.object().put("action","RETRY").put("code",e.code()).put("reason",e.getMessage()));}}
            else if(actor.hasAnyRole("MAKER","CHECKER","DATA_OFFICER"))blocked.add(json.object().put("action","RETRY").put("code","SERVICE_UNAVAILABLE").put("reason","Module worker của loại job này chưa triển khai."));
        }
        body.set("allowedActions",actions);body.set("blockedActions",blocked);return body;
    }
}
