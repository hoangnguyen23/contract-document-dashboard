package vn.contractgovernance.api.files;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.dossier.DossierService;
import vn.contractgovernance.api.shared.*;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {
    private final DispositionLocks dispositionLocks;private final ContractContributions contractContributions;
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(FileService.class);
    public record Download(byte[] bytes,String mediaType,String filename,String hash,int version) {}
    private final EntityStore store; private final EvidenceAccess access; private final PrivateBlobStore blobs;
    private final JsonSupport json; private final Clock clock; private final CommandExecutor commands; private final AuditService audit;private final JobQueue jobs;private final ListQueries lists;
    public FileService(DispositionLocks dispositionLocks,ContractContributions contractContributions,EntityStore store,EvidenceAccess access,PrivateBlobStore blobs,JsonSupport json,Clock clock,CommandExecutor commands,AuditService audit,JobQueue jobs,ListQueries lists) {
        this.dispositionLocks=dispositionLocks;this.contractContributions=contractContributions;this.store=store;this.access=access;this.blobs=blobs;this.json=json;this.clock=clock;this.commands=commands;this.audit=audit;this.jobs=jobs;this.lists=lists;
    }
    public EntityStore.Entity requireClean(UUID id,String kind,UUID parent,UUID branch,Actor actor) {return access.requireClean(id,kind,parent,branch,actor);}
    public JsonNode get(UUID id,Actor actor) {return project(access.requireFile(id,actor),actor);}
    public JsonNode list(Map<String,String> query,Actor actor){if(query.containsKey("branchId"))actor.requireBranch(UUID.fromString(query.get("branchId")));var args=new java.util.ArrayList<Object>();var scope=access.sqlScope(false,actor,args);return lists.page("File",actor,query,true,scope,args,body->project(new EntityStore.Entity("File",UUID.fromString(body.path("id").asString()),body.hasNonNull("branchId")?UUID.fromString(body.path("branchId").asString()):null,body.path("version").asInt(),body),actor));}
    public ApiResult upload(MultipartFile upload,UUID branchId,String targetKind,UUID targetId,String purpose,String key,Actor actor) {
        var parent=access.authorizeWriter(targetKind,targetId,actor);if(!Objects.equals(branchId,parent.branchId()))throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");
        if(targetKind.equals("POLICY") && purpose.equals("IDENTITY_IMAGE"))throw ApiException.validation("/purpose","Policy chỉ nhận evidence không chứa dữ liệu cá nhân.");
        if(upload.getSize()>PrivateBlobStore.MAX_BYTES)throw ApiException.of("FILE_TOO_LARGE","Tệp vượt quá giới hạn 10 MiB.");
        final PrivateBlobStore.Stored stored;
        try {stored=blobs.write(upload.getInputStream());}catch(java.io.IOException exception){throw ApiException.of("SERVICE_UNAVAILABLE","Không đọc được nội dung tải lên.");}
        var fingerprint=json.object();if(branchId==null)fingerprint.putNull("branchId");else fingerprint.put("branchId",branchId.toString());fingerprint.put("targetKind",targetKind);fingerprint.put("targetId",targetId.toString());fingerprint.put("purpose",purpose);fingerprint.put("fileSha256",stored.sha256());
        try {
            var result=commands.execute(actor,key,"POST","/api/v1/files",fingerprint,null,()->access.authorizeWriter(targetKind,targetId,actor),()->{
                var locked=access.lockOwner(targetKind,targetId);access.requireWritableParent(targetKind,targetId,actor);validateUploadLimits(targetKind,targetId,stored.sizeBytes());
                var id=UUID.randomUUID();var body=base(stored,targetKind,targetId,branchId,purpose,filename(upload.getOriginalFilename()),actor.id());
                var jobId=jobs.enqueue("FILE_SCAN","FILE",id,branchId,1,stored.sha256(),"FILE_SCAN:"+id+":"+stored.sha256(),json.object(),AuditService.traceId());body.put("scanJobId",jobId.toString());
                var file=store.create("File",id,branchId,body);store.jdbc().update("insert into cg_file_blob(file_id,storage_key,original_sha256,size_bytes,created_at) values(?,?,?,?,?)",id,stored.key(),stored.sha256(),stored.sizeBytes(),Timestamp.from(clock.instant()));
                contribute(locked,actor);audit.record(actor,"CREATE",file,null);return ApiResult.created(project(file,actor),"/api/v1/files/"+id);
            });
            if(result.replayed())discardOrphan(stored.key());return result;
        } catch(RuntimeException exception) {
            // Finalized orphan bytes are safe to remove only when no committed database record refers to them.
            discardOrphan(stored.key());throw exception;
        }
    }
    private void discardOrphan(String key){try{if(store.jdbc().queryForObject("select count(*) from cg_file_blob where storage_key=?",Long.class,key)==0L)blobs.delete(key);}catch(RuntimeException cleanupFailure){LOG.warn("Unreferenced replay/upload bytes need private maintenance reconciliation");}}
    private ObjectNode base(PrivateBlobStore.Stored stored,String kind,UUID target,UUID branch,String purpose,String filename,UUID actorId) {
        var body=json.object();var now=JsonSupport.timestamp(clock.instant());body.put("createdAt",now);body.put("updatedAt",now);body.set("allowedActions",json.array());body.set("blockedActions",json.array());if(branch==null)body.putNull("branchId");else body.put("branchId",branch.toString());body.put("state","SCANNING");body.put("targetKind",kind);body.put("targetId",target.toString());body.put("purpose",purpose);body.put("mediaType",stored.mediaType());body.put("filename",filename);body.put("sizeBytes",stored.sizeBytes());body.put("sha256",stored.sha256());body.putNull("scanJobId");body.putNull("scannerVersion");body.putNull("signatureDatabaseVersion");body.putNull("scannedAt");body.put("humanVerification","NOT_CHECKED");body.put("sensitive",purpose.equals("IDENTITY_IMAGE"));body.put("attached",false);body.put("uploadedBy",actorId.toString());body.put("origin","UPLOADED");body.putNull("rendererVersion");body.putNull("generatedAt");return body;
    }
    private String filename(String source) {
        var name=source==null?"upload":java.text.Normalizer.normalize(source,java.text.Normalizer.Form.NFC).replace('\\','/');var slash=name.lastIndexOf('/');if(slash>=0)name=name.substring(slash+1);name=name.replaceAll("[\\p{Cntrl}]", "_").trim();if(name.isBlank())name="upload";if(name.length()>150)name=name.substring(0,150);return name;
    }
    private void contribute(EntityStore.Entity parent,Actor actor) {
        if (parent.type().equals("Change")) {
            store.jdbc().update("insert into cg_change_contributor(change_id,actor_id,created_at) values(?,?,?) on conflict do nothing",parent.id(),actor.id(),Timestamp.from(clock.instant()));
            if(store.jdbc().queryForObject("select count(*) from cg_change_contributor where change_id=?",Long.class,parent.id())>50L) throw ApiException.validation("/contributorIds","Nguồn vượt giới hạn 50 người đóng góp.");
        }
        if (parent.type().equals("Contract")) {
            contractContributions.record(parent.id(), actor.id());
        }
        if(parent.body().has("contributorIds")) {var body=(ObjectNode)parent.body().deepCopy();DossierService.contribute(body,actor.id());if(body.path("contributorIds").size()>50)throw ApiException.validation("/contributorIds","Nguồn vượt giới hạn 50 người đóng góp.");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));store.update(parent,body);}
    }
    public ApiResult retry(UUID id,JsonNode request,String tag,String key,Actor actor) {
        authorizeRetry(id,actor);ETags.require(tag);
        return commands.execute(actor,key,"POST","/api/v1/files/"+id+"/commands",request,tag,()->authorizeRetry(id,actor),()->{
            var preview=access.requireFile(id,actor);access.lockParent(preview.body(),actor);var file=store.lock("File",id);ETags.check(tag,file.version());requireRetrySource(file,actor);
            var body=(ObjectNode)file.body().deepCopy();body.put("state","SCANNING");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var next=store.update(file,body);
            var jobId=UUID.fromString(body.path("scanJobId").asString());if(!jobs.retry(jobId))throw ApiException.of("STATE_CONFLICT","Job quét không ở trạng thái FAILED.");
            store.jdbc().update("update cg_job set expected_version=?,expected_hash=? where id=?",next.version(),body.path("sha256").asString(),jobId);audit.record(actor,"RETRY",next,request.path("reason").asString());return ApiResult.ok(project(next,actor));
        });
    }
    public void requireRetrySource(EntityStore.Entity file,Actor actor) {
        access.requireFile(file.id(),actor);access.requireWritableParent(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);
        if(!file.body().path("state").asString().equals("SCAN_FAILED"))throw ApiException.of("STATE_CONFLICT","Chỉ tệp SCAN_FAILED được quét lại.");
        if(!actor.id().toString().equals(file.body().path("uploadedBy").asString()))throw ApiException.of("ROLE_FORBIDDEN","Chỉ người tải tệp còn có quyền trên nguồn mới được quét lại.");
        if(!retryJobEligible(file))throw ApiException.of("STATE_CONFLICT","Tệp không có job FAILED khớp nguồn hiện tại để thực hiện lại.");
        var blob=blob(file.id());blobs.read(blob.key(),file.body().path("sha256").asString());
    }
    private boolean retryJobEligible(EntityStore.Entity file){if(!file.body().hasNonNull("scanJobId"))return false;return store.jdbc().queryForObject("select count(*) from cg_job where id=? and target_type='FILE' and target_id=? and kind='FILE_SCAN' and state='FAILED' and expected_hash=?",Long.class,UUID.fromString(file.body().path("scanJobId").asString()),file.id(),file.body().path("sha256").asString())==1L;}
    public EntityStore.Entity authorizeRetry(UUID id,Actor actor){var file=access.requireFile(id,actor);access.authorizeWriter(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);if(!actor.id().toString().equals(file.body().path("uploadedBy").asString()))throw ApiException.of("ROLE_FORBIDDEN","Chỉ người tải lên còn quyền trên nguồn được quét lại.");return file;}
    public ApiResult delete(UUID id,JsonNode request,String tag,String key,Actor actor) {
        authorizeDelete(id,actor);ETags.require(tag);
        return commands.execute(actor,key,"DELETE","/api/v1/files/"+id,request,tag,()->authorizeDelete(id,actor),()->{
            var preview=access.requireFile(id,actor);access.lockParent(preview.body(),actor);var file=store.lock("File",id);access.requireFile(id,actor);ETags.check(tag,file.version());access.requireWritableParent(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);
            if(!actor.id().toString().equals(file.body().path("uploadedBy").asString()) || file.body().path("origin").asString().equals("GENERATED"))throw ApiException.of("ROLE_FORBIDDEN","Chỉ được xóa staging do mình tải lên.");
            if(file.body().path("attached").asBoolean() || store.jdbc().queryForObject("select count(*) from cg_file_association where file_id=?",Long.class,id)>0)throw ApiException.of("STATE_CONFLICT","Evidence đã liên kết phải giữ lịch sử và xử lý qua yêu cầu dữ liệu.");
            var blob=blob(id);audit.record(actor,"DELETE",file,request.path("reason").asString());
            store.jdbc().update("update cg_job set state='CANCELLED',error_code='GUARD_BLOCKED',lease_owner=null,lease_until=null,updated_at=? where target_type='FILE' and target_id=? and state in ('QUEUED','RUNNING','FAILED')",Timestamp.from(clock.instant()),id);
            store.jdbc().update("insert into cg_file_staging_deletion(file_id,uploaded_by,target_kind,target_id,branch_id,deleted_at) values(?,?,?,?,?,?)",id,actor.id(),file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),file.branchId(),Timestamp.from(clock.instant()));store.delete(file);store.jdbc().update("delete from cg_file_blob where file_id=?",id);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){if(store.jdbc().queryForObject("select count(*) from cg_file_blob where storage_key=?",Long.class,blob.key())==0L)try{blobs.delete(blob.key());}catch(ApiException exception){/* Unreferenced bytes remain for safe maintenance reconciliation. */}}});return ApiResult.empty();
        });
    }
    private void authorizeDelete(UUID id,Actor actor){
        var existing=store.find("File",id);
        if(existing.isPresent()){var file=access.requireFile(id,actor);access.authorizeWriter(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);if(!actor.id().toString().equals(file.body().path("uploadedBy").asString()))throw ApiException.of("ROLE_FORBIDDEN","Chỉ được xóa staging do mình tải lên.");return;}
        var tombstones=store.jdbc().query("select target_kind,target_id from cg_file_staging_deletion where file_id=? and uploaded_by=?",(rs,row)->Map.entry(rs.getString(1),rs.getObject(2,UUID.class)),id,actor.id());if(tombstones.isEmpty())throw ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên.");var tombstone=tombstones.getFirst();access.authorizeWriter(tombstone.getKey(),tombstone.getValue(),actor);
    }
    @Transactional public Download download(UUID id,String representation,Actor actor) {
        var file=access.requireFile(id,actor);if(file.body().path("state").asString().equals("DISPOSED"))throw ApiException.of("DISPOSITION_COMMITTED","Bytes của bản ghi đã được xử lý theo quyết định dữ liệu.");
        if(!file.body().path("state").asString().equals("CLEAN"))throw ApiException.of("FILE_NOT_CLEAN","Tệp chưa an toàn để tải.");
        var blob=blob(id);byte[] bytes;String mediaType;
        if(representation.equals("FULL")){access.requireFull(file,actor);bytes=blobs.read(blob.key(),blob.hash());mediaType=file.body().path("mediaType").asString();}
        else if(representation.equals("REDACTED")) {
            if(blob.redactedKey()==null || blob.redactedHash()==null || blob.verifiedBy()==null)throw ApiException.of("READINESS_BLOCKED","Chưa có bản che dữ liệu được người độc lập xác minh; không trả bản gốc thay thế.");
            bytes=blobs.read(blob.redactedKey(),blob.redactedHash());mediaType=blob.redactedMediaType();
        } else throw ApiException.validation("/query/representation","Phải chọn FULL hoặc REDACTED.");
        audit.record(actor,"DOWNLOAD_"+representation,file,null);String extension=mediaType.equals("application/pdf")?".pdf":mediaType.equals("image/png")?".png":".jpg";return new Download(bytes,mediaType,"file-"+id+extension,JsonSupport.sha256(bytes),file.version());
    }
    public record Blob(String key,String hash,String redactedKey,String redactedHash,String redactedMediaType,UUID verifiedBy) {}
    public Blob blob(UUID id) {return store.jdbc().query("select storage_key,original_sha256,redacted_storage_key,redacted_sha256,redacted_media_type,redacted_verified_by from cg_file_blob where file_id=?",(rs,row)->new Blob(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getObject(6,UUID.class)),id).stream().findFirst().orElseThrow(()->ApiException.of("STORAGE_INTEGRITY","Tệp không có bản ghi lưu trữ bytes."));}
    public byte[] bytesForScan(UUID id,String hash) {return blobs.read(blob(id).key(),hash);}
    public void validateDraftLimits(String kind,UUID target,Set<UUID> references) {
        long total=0;for(var id:references)total+=store.require("File",id).body().path("sizeBytes").asLong();if(references.size()>20 || total>100L*1024*1024)throw ApiException.of("FILE_LIMIT","Nháp chỉ nhận 20 tệp với tổng tối đa 100 MiB.");
    }
    private void validateUploadLimits(String kind,UUID target,int incomingSize) {
        // Serialize against the parent lock; both associated and unattached staging count toward a draft budget.
        var totals=store.jdbc().queryForMap("select count(*) as count,coalesce(sum((body->>'sizeBytes')::bigint),0) as total from cg_entity where entity_type='File' and body->>'targetKind'=? and body->>'targetId'=? and body->>'state'<>'DISPOSED'",kind,target.toString());
        if(((Number)totals.get("count")).longValue()>=20 || ((Number)totals.get("total")).longValue()+incomingSize>100L*1024*1024)throw ApiException.of("FILE_LIMIT","Parent chỉ nhận 20 tệp với tổng tối đa 100 MiB.");
    }
    public void synchronizeDraftReferences(String kind,UUID parentId,UUID branch,Set<UUID> references,Actor actor) {
        for(var id:references) {
            var file=store.lock("File",id);access.requireClean(id,kind,parentId,branch,actor);store.jdbc().update("insert into cg_file_association(file_id,target_type,target_id,historical,created_at) values(?,?,?,false,?) on conflict do nothing",id,kind,parentId,Timestamp.from(clock.instant()));
            if(!file.body().path("attached").asBoolean()){var body=(ObjectNode)file.body().deepCopy();body.put("attached",true);body.put("updatedAt",JsonSupport.timestamp(clock.instant()));store.update(file,body);}
        }
        var oldIds=store.jdbc().query("select file_id from cg_file_association where target_type=? and target_id=? and not historical",(rs,row)->rs.getObject(1,UUID.class),kind,parentId);
        for(var id:oldIds)if(!references.contains(id)) {
            var file=store.lock("File",id);store.jdbc().update("delete from cg_file_association where file_id=? and target_type=? and target_id=? and not historical",id,kind,parentId);
            if(store.jdbc().queryForObject("select count(*) from cg_file_association where file_id=?",Long.class,id)==0L){var body=(ObjectNode)file.body().deepCopy();body.put("attached",false);body.put("updatedAt",JsonSupport.timestamp(clock.instant()));store.update(file,body);}
        }
    }
    public Map<UUID,UUID> cloneEvidence(EntityStore.Entity source,UUID newParent,UUID branch,Set<UUID> ids,Actor actor) {
        var result=new LinkedHashMap<UUID,UUID>();
        for(var sourceId:ids) {
            var old=store.lock("File",sourceId);dispositionLocks.available(sourceId);access.requireClean(sourceId,"DOSSIER",source.id(),source.branchId(),actor);var blob=blob(sourceId);blobs.read(blob.key(),blob.hash());
            var id=UUID.randomUUID();var body=(ObjectNode)old.body().deepCopy();var now=JsonSupport.timestamp(clock.instant());body.put("createdAt",now);body.put("updatedAt",now);body.put("branchId",branch.toString());body.put("targetId",newParent.toString());body.put("uploadedBy",actor.id().toString());body.put("attached",false);body.put("humanVerification","NOT_CHECKED");body.putNull("scanJobId");body.set("allowedActions",json.array());body.set("blockedActions",json.array());store.create("File",id,branch,body);
            store.jdbc().update("insert into cg_file_blob(file_id,storage_key,original_sha256,size_bytes,created_at) values(?,?,?,?,?)",id,blob.key(),blob.hash(),body.path("sizeBytes").asLong(),Timestamp.from(clock.instant()));result.put(sourceId,id);
        }
        return result;
    }
    public JsonNode project(EntityStore.Entity file,Actor actor) {
        var body=(ObjectNode)file.body().deepCopy();var actions=json.array();var blocked=json.array();var state=body.path("state").asString();
        if(actor.hasRole("AUDITOR") && !actor.hasPermission("SENSITIVE_READ") && !access.fullBusinessFacts(body.path("targetKind").asString(),UUID.fromString(body.path("targetId").asString()),actor))body.put("filename","redacted-file"+(body.path("mediaType").asString().equals("application/pdf")?".pdf":body.path("mediaType").asString().equals("image/png")?".png":".jpg"));
        if(state.equals("CLEAN")){try{access.requireFull(file,actor);actions.add("DOWNLOAD_FULL");}catch(ApiException denied){blocked.add(json.object().put("action","DOWNLOAD_FULL").put("code",denied.code()).put("reason","Cần quyền đọc bản đầy đủ nhạy cảm."));}var blob=blob(file.id());if(blob.redactedKey()!=null && blob.verifiedBy()!=null)actions.add("DOWNLOAD_REDACTED");else blocked.add(json.object().put("action","DOWNLOAD_REDACTED").put("code","READINESS_BLOCKED").put("reason","Chưa có bản che dữ liệu an toàn."));}
        if(state.equals("SCAN_FAILED") && actor.id().toString().equals(body.path("uploadedBy").asString())) {try{access.requireWritableParent(body.path("targetKind").asString(),UUID.fromString(body.path("targetId").asString()),actor);if(!retryJobEligible(file))throw ApiException.of("STATE_CONFLICT","Job nguồn không ở trạng thái FAILED.");actions.add("RETRY");}catch(ApiException denied){blocked.add(json.object().put("action","RETRY").put("code",denied.code()).put("reason","Nguồn không còn cho phép quét lại."));}}
        body.set("allowedActions",actions);body.set("blockedActions",blocked);return body;
    }
}


