package vn.contractgovernance.api.contract;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.files.*;
import java.io.*;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
@Service
public class ArtifactJobs {
 private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeArtifacts> changes;private final JsonSupport json;private final EntityStore store;private final OriginalAgreementService agreements;private final AgreementRenderer renderer;private final GeneratedArtifactFiles files;private final PrivateBlobStore blobs;private final ClamAvScanner scanner;private final JobQueue queue;private final Clock clock;private final AuditService audit;private final TransactionTemplate tx;
 private final Semaphore slots=new Semaphore(1);private final ExecutorService worker=Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon(true).name("agreement-pdf-",0).factory());private final ScheduledExecutorService heartbeats=Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon(true).name("agreement-pdf-lease-",0).factory());
 public ArtifactJobs(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeArtifacts> changes,JsonSupport json,EntityStore store,OriginalAgreementService agreements,AgreementRenderer renderer,GeneratedArtifactFiles files,PrivateBlobStore blobs,ClamAvScanner scanner,JobQueue queue,Clock clock,AuditService audit,PlatformTransactionManager manager){this.changes=changes;this.json=json;this.store=store;this.agreements=agreements;this.renderer=renderer;this.files=files;this.blobs=blobs;this.scanner=scanner;this.queue=queue;this.clock=clock;this.audit=audit;this.tx=new TransactionTemplate(manager);}
 @Scheduled(fixedDelayString="${cg.artifact.poll-ms:1500}")public void poll(){if(!slots.tryAcquire())return;try{var job=queue.claimForKind("ARTIFACT_PDF",UUID.randomUUID());if(job.isEmpty()){slots.release();return;}worker.execute(()->{try{run(job.get());}finally{slots.release();}});}catch(RuntimeException e){slots.release();throw e;}}
 private void run(JobQueue.Job job){var heartbeat=heartbeats.scheduleAtFixedRate(()->queue.renew(job),20,20,TimeUnit.SECONDS);String failureCode="STORAGE_INTEGRITY";try{
  if(job.targetType().equals("CHANGE")){changes.getObject().run(job);return;}
  if(!job.targetType().equals("CONTRACT"))throw ApiException.of("STORAGE_INTEGRITY","Sai nguồn job.");
  var source=agreements.source(job.targetId());if(!source.jobId().equals(job.id())||!source.hash().equals(job.expectedHash())||!source.hash().equals(json.hash(source.body())))throw ApiException.of("STORAGE_INTEGRITY","Nguồn job không khớp.");
  var bytes=renderer.render(source.body());var generatedAt=JsonSupport.timestamp(clock.instant());failureCode="SCANNER_UNAVAILABLE";var scan=scanner.scan(bytes);failureCode="STORAGE_INTEGRITY";if(scan.infected())throw ApiException.of("STORAGE_INTEGRITY","Scanner từ chối bản sinh.");
  try(var pending=blobs.writePending(new ByteArrayInputStream(bytes))){if(!pending.stored().sha256().equals(JsonSupport.sha256(bytes)))throw ApiException.of("STORAGE_INTEGRITY","Bytes bản sinh không khớp.");tx.executeWithoutResult(status->{
   var c=store.lock("Contract",job.targetId());var current=agreements.source(c.id());if(!valid(c,current,job)){queue.cancel(job,"Original source is cancelled or obsolete");return;}
   if(!queue.succeed(job))return;
   if(store.jdbc().queryForObject("select count(*) from cg_agreement_artifact where contract_id=?",Long.class,c.id())!=0)throw ApiException.of("STORAGE_INTEGRITY","Bản sinh đã có liên kết.");
   var file=files.register(c,pending.stored(),scan,AgreementRenderer.VERSION,generatedAt);
   store.jdbc().update("insert into cg_agreement_artifact(contract_id,file_id,source_hash,artifact_hash,created_at) values(?,?,?,?,?)",c.id(),file.id(),source.hash(),pending.stored().sha256(),Timestamp.from(clock.instant()));
   var b=(ObjectNode)c.body().deepCopy();b.put("approvedArtifactId",file.id().toString());b.put("approvedArtifactHash",pending.stored().sha256());b.put("updatedAt",JsonSupport.timestamp(clock.instant()));var updated=store.update(c,b);store.jdbc().update("update cg_job set result_file_id=? where id=?",file.id(),job.id());audit.record(null,"ARTIFACT_PDF",updated,null);
  });}
 }catch(Exception e){String code=e instanceof ApiException a&&a.code().equals("STORAGE_INTEGRITY")?"STORAGE_INTEGRITY":failureCode;tx.executeWithoutResult(status->{var c=store.find("Contract",job.targetId());if(c.isEmpty()){queue.cancel(job,"Source is missing");return;}var locked=store.lock("Contract",job.targetId());if(locked.body().path("state").asString().equals("CANCELLED"))queue.cancel(job,"Source is cancelled");else queue.fail(job,code,"Artifact renderer, private storage or scanner unavailable");});}finally{heartbeat.cancel(false);}}
 private boolean valid(EntityStore.Entity c,OriginalAgreementService.Source source,JobQueue.Job job){return source.jobId().equals(job.id())&&source.hash().equals(job.expectedHash())&&source.hash().equals(json.hash(source.body()))&&c.body().path("artifactJobId").asString().equals(job.id().toString())&&c.body().path("approvedRevisionId").asString().equals(source.body().path("revision").path("id").asString())&&c.body().path("state").asString().equals("PREPARED")&&!c.body().hasNonNull("approvedArtifactId");}
 public void authorize(UUID contractId,Actor actor){if(store.find("Change",contractId).isPresent()){changes.getObject().authorize(contractId,actor);return;}agreements.authorizeArtifactMutation(contractId,actor);}
 public void requireRetry(EntityStore.Entity job,Actor actor){if(job.body().path("parentKind").asString().equals("CHANGE")){changes.getObject().requireRetry(job,actor);return;}var contractId=UUID.fromString(job.body().path("parentId").asString());authorize(contractId,actor);var c=store.require("Contract",contractId);var s=agreements.source(c.id());if(!job.body().path("state").asString().equals("FAILED")||!c.body().path("state").asString().equals("PREPARED")||c.body().hasNonNull("approvedArtifactId")||!s.jobId().equals(job.id())||!c.body().path("artifactJobId").asString().equals(job.id().toString()))throw ApiException.of("STATE_CONFLICT","Nguồn không cho phép sinh lại.");var expected=store.jdbc().queryForObject("select expected_hash from cg_job where id=?",String.class,job.id());if(!s.hash().equals(expected)||!s.hash().equals(json.hash(s.body())))throw ApiException.of("STORAGE_INTEGRITY","Nguồn job thay đổi.");}
 public EntityStore.Entity retry(UUID id,String tag,String reason,Actor actor){if(store.require("Job",id).body().path("parentKind").asString().equals("CHANGE"))return changes.getObject().retry(id,tag,reason,actor);var preview=store.require("Job",id);store.lock("Contract",UUID.fromString(preview.body().path("parentId").asString()));var job=store.lock("Job",id);ETags.check(tag,job.version());requireRetry(job,actor);if(!queue.retry(id))throw ApiException.of("STATE_CONFLICT","Job đã thay đổi.");var updated=store.require("Job",id);audit.record(actor,"RETRY",updated,reason);return updated;}
 @PreDestroy public void close(){worker.shutdownNow();heartbeats.shutdownNow();}
}
