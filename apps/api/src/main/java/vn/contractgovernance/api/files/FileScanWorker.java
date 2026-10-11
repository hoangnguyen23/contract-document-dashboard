package vn.contractgovernance.api.files;

import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.*;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
public class FileScanWorker {
    private final JobQueue queue;private final EntityStore store;private final FileService files;private final EvidenceAccess access;private final ClamAvScanner scanner;private final UserDirectory users;private final AuditService audit;private final Clock clock;private final TransactionTemplate transactions;
    private final UUID workerId=UUID.randomUUID();private final Semaphore slots=new Semaphore(2);
    private final ExecutorService workers=Executors.newFixedThreadPool(2,Thread.ofPlatform().daemon(true).name("file-scan-",0).factory());
    private final ScheduledExecutorService heartbeat=Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon(true).name("file-scan-heartbeat").factory());
    public FileScanWorker(JobQueue queue,EntityStore store,FileService files,EvidenceAccess access,ClamAvScanner scanner,UserDirectory users,AuditService audit,Clock clock,PlatformTransactionManager manager){this.queue=queue;this.store=store;this.files=files;this.access=access;this.scanner=scanner;this.users=users;this.audit=audit;this.clock=clock;this.transactions=new TransactionTemplate(manager);}
    @Scheduled(fixedDelayString="${cg.scanner.poll-ms:1000}")public void poll(){
        reconcileExhaustedScans();
        while(slots.tryAcquire()) {var claimed=queue.claimForKind("FILE_SCAN",workerId);if(claimed.isEmpty()){slots.release();return;}var job=claimed.get();workers.execute(()->{try{run(job);}finally{slots.release();}});}
    }
    private void run(JobQueue.Job job) {
        var renewal=heartbeat.scheduleAtFixedRate(()->queue.renew(job),20,20,TimeUnit.SECONDS);
        try {
            var file=store.require("File",job.targetId());var result=scanner.scan(files.bytesForScan(file.id(),job.expectedHash()));
            transactions.executeWithoutResult(status->{
                lockParentIfPresent(job.targetId());var current=store.lock("File",job.targetId());if(!guard(current,job)){cancelObsolete(current,job);return;}
                if(!queue.succeed(job))return;
                var body=(ObjectNode)current.body().deepCopy();body.put("state",result.infected()?"INFECTED":"CLEAN");body.put("scannerVersion",result.engineVersion());body.put("signatureDatabaseVersion",result.databaseVersion());body.put("scannedAt",JsonSupport.timestamp(clock.instant()));body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var next=store.update(current,body);audit.record(null,"FILE_SCAN",next,null);
            });
        } catch(Exception exception) {
            String code=exception instanceof ApiException api && api.code().equals("STORAGE_INTEGRITY")?"STORAGE_INTEGRITY":"SCANNER_UNAVAILABLE";
            transactions.executeWithoutResult(status->{
                var current=store.find("File",job.targetId());if(current.isEmpty()){queue.cancel(job,"Source was deleted");return;}
                lockParentIfPresent(job.targetId());var file=store.lock("File",job.targetId());if(!guard(file,job)){cancelObsolete(file,job);return;}
                if(!queue.fail(job,code,"Scanner or stored bytes are unavailable"))return;
                if(job.attempts()>=5){var body=(ObjectNode)file.body().deepCopy();body.put("state","SCAN_FAILED");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var next=store.update(file,body);audit.record(null,"FILE_SCAN_FAILED",next,null);}
            });
        } finally {renewal.cancel(false);}
    }
    private void lockParentIfPresent(UUID fileId){var file=store.require("File",fileId);var kind=file.body().path("targetKind").asString();String type=switch(kind){case "DOSSIER"->"Dossier";case "CONTRACT"->"Contract";case "CHANGE"->"Change";case "RELATIONSHIP"->"Relationship";case "INCIDENT"->"Incident";case "OBLIGATION"->"Obligation";case "CONTROL_REQUEST"->"ControlRequest";case "DATA_REQUEST"->"DataRequest";case "NOTICE"->"Notice";case "POLICY"->"Policy";default->throw ApiException.of("STORAGE_INTEGRITY","Loại parent tệp không hợp lệ.");};var id=UUID.fromString(file.body().path("targetId").asString());if(store.find(type,id).isPresent())store.lock(type,id);}
    private void cancelObsolete(EntityStore.Entity file,JobQueue.Job job){
        if(!queue.cancel(job,"Source hash/version or parent guard changed"))return;
        if(file.version()==job.expectedVersion() && file.body().path("state").asString().equals("SCANNING") && file.body().path("sha256").asString().equals(job.expectedHash())){
            var body=(ObjectNode)file.body().deepCopy();body.put("state","SCAN_FAILED");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var next=store.update(file,body);audit.record(null,"FILE_SCAN_CANCELLED",next,null);
        }
    }
    /** Recover the fifth crash lease: queue exhaustion must leave a visible retryable scan failure. */
    private void reconcileExhaustedScans(){
        var ids=store.jdbc().query("select j.target_id from cg_job j join cg_entity f on f.entity_type='File' and f.id=j.target_id where j.kind='FILE_SCAN' and j.state='FAILED' and j.attempts>=5 and f.body->>'state'='SCANNING' order by j.updated_at,j.id limit 20",(rs,row)->rs.getObject(1,UUID.class));
        for(var id:ids)transactions.executeWithoutResult(status->{
            if(store.find("File",id).isEmpty())return;lockParentIfPresent(id);var file=store.lock("File",id);
            var jobs=store.jdbc().query("select id,expected_version,expected_hash from cg_job where target_type='FILE' and target_id=? and kind='FILE_SCAN' and state='FAILED' and attempts>=5 for update",(rs,row)->new Exhausted(rs.getObject(1,UUID.class),rs.getInt(2),rs.getString(3)),id);
            if(jobs.isEmpty())return;var job=jobs.getFirst();
            if(file.version()!=job.version() || !file.body().path("sha256").asString().equals(job.hash()) || !file.body().path("state").asString().equals("SCANNING") || !file.body().path("scanJobId").asString().equals(job.id().toString()))return;
            var body=(ObjectNode)file.body().deepCopy();body.put("state","SCAN_FAILED");body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var next=store.update(file,body);audit.record(null,"FILE_SCAN_FAILED",next,"Worker lease exhausted after five attempts.");
        });
    }
    private record Exhausted(UUID id,int version,String hash){}
    private boolean guard(EntityStore.Entity file,JobQueue.Job job) {
        if(file.version()!=job.expectedVersion() || !file.body().path("sha256").asString().equals(job.expectedHash()) || !file.body().path("state").asString().equals("SCANNING"))return false;
        try {var actor=users.actor(UUID.fromString(file.body().path("uploadedBy").asString()));access.requireWritableParent(file.body().path("targetKind").asString(),UUID.fromString(file.body().path("targetId").asString()),actor);return true;}catch(ApiException exception){return false;}
    }
    @PreDestroy public void close(){workers.shutdownNow();heartbeat.shutdownNow();}
}
