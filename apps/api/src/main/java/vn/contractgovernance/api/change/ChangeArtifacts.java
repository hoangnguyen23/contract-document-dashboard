package vn.contractgovernance.api.change;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.AgreementRenderer;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.shared.*;
import static vn.contractgovernance.api.change.ChangeService.id;

/** Called only by the shared ARTIFACT_PDF claimant; rendering/scanning outside the DB transaction. */
@Service
public class ChangeArtifacts {
    private record Source(UUID revision,JsonNode body,String hash,UUID job) {}
    private final ChangeService changes;
    private final ChangeApproval approval;
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final JobQueue queue;
    private final AgreementRenderer renderer;
    private final ClamAvScanner scanner;
    private final PrivateBlobStore blobs;
    private final GeneratedArtifactFiles files;
    private final AuditService audit;
    private final TransactionTemplate tx;
    public ChangeArtifacts(ChangeService changes,ChangeApproval approval,EntityStore store,JsonSupport json,
            Clock clock,JobQueue queue,AgreementRenderer renderer,ClamAvScanner scanner,PrivateBlobStore blobs,
            GeneratedArtifactFiles files,AuditService audit,PlatformTransactionManager manager) {
        this.changes=changes; this.approval=approval; this.store=store; this.json=json; this.clock=clock;
        this.queue=queue; this.renderer=renderer; this.scanner=scanner; this.blobs=blobs; this.files=files;
        this.audit=audit; this.tx=new TransactionTemplate(manager);
    }
    private Source source(JobQueue.Job job) {
        return store.jdbc().query("select revision_id,source::text,source_hash,job_id from cg_change_artifact_source where job_id=? and change_id=?",
                (rs,n)->new Source(rs.getObject(1,UUID.class),json.parse(rs.getString(2)),rs.getString(3),rs.getObject(4,UUID.class)),job.id(),job.targetId())
                .stream().findFirst().orElseThrow(()->ApiException.of("STORAGE_INTEGRITY","Thiếu appendix render source."));
    }
    public void run(JobQueue.Job job) {
        try {
            var source=source(job);
            if(!source.hash().equals(job.expectedHash())||!source.hash().equals(json.hash(source.body()))) throw ApiException.of("STORAGE_INTEGRITY","Appendix source bị thay đổi.");
            var bytes=renderer.render(source.body()); var generatedAt=JsonSupport.timestamp(clock.instant());
            var scan=scanner.scan(bytes); if(scan.infected()) throw ApiException.of("STORAGE_INTEGRITY","Scanner từ chối appendix.");
            try(var pending=blobs.writePending(new ByteArrayInputStream(bytes))) {
                tx.executeWithoutResult(status->{
                    var change=changes.lock(job.targetId());
                    if(!change.body().path("state").asString().equals("APPROVED")||!change.body().path("artifactJobId").asString().equals(job.id().toString())||!approval.frozen(change).revision().path("id").asString().equals(source.revision().toString())) { queue.cancel(job,"Appendix source is cancelled or obsolete"); return; }
                    if(!queue.succeed(job)) return;
                    if(change.body().hasNonNull("artifactId")) throw ApiException.of("STORAGE_INTEGRITY","Appendix đã có artifact.");
                    var file=files.register(change,pending.stored(),scan,AgreementRenderer.VERSION,generatedAt);
                    store.jdbc().update("insert into cg_change_artifact(revision_id,file_id,source_hash,artifact_hash,created_at) values(?,?,?,?,?)",source.revision(),file.id(),source.hash(),pending.stored().sha256(),Timestamp.from(clock.instant()));
                    var body=(ObjectNode)change.body().deepCopy(); body.put("artifactId",file.id().toString()).put("artifactHash",pending.stored().sha256()); changes.save(change,body,null,"ARTIFACT_PDF",null);
                    store.jdbc().update("update cg_job set result_file_id=? where id=?",file.id(),job.id());
                });
            }
        } catch(Exception failure) {
            tx.executeWithoutResult(status->{
                var change=changes.lock(job.targetId());
                if(!change.body().path("state").asString().equals("APPROVED")||!change.body().path("artifactJobId").asString().equals(job.id().toString())) queue.cancel(job,"Appendix source obsolete");
                else queue.fail(job,failure instanceof ApiException a&&a.code().equals("STORAGE_INTEGRITY")?"STORAGE_INTEGRITY":"SCANNER_UNAVAILABLE","Appendix render/storage/scan unavailable");
            });
        }
    }
    public void authorize(UUID changeId,Actor actor) {
        changes.require(changeId,actor); actor.requireRole("MAKER","CHECKER"); if(!actor.hasRole("CHECKER")) changes.maker(changeId,actor);
    }
    public void requireRetry(EntityStore.Entity job,Actor actor) {
        UUID changeId=id(job.body(),"parentId"); authorize(changeId,actor); var change=store.require("Change",changeId);
        if(!job.body().path("state").asString().equals("FAILED")||!change.body().path("state").asString().equals("APPROVED")||!change.body().path("artifactJobId").asString().equals(job.id().toString())||change.body().hasNonNull("artifactId")) throw ApiException.of("STATE_CONFLICT","Appendix không đủ điều kiện retry.");
        var rows=store.jdbc().query("select s.source::text,s.source_hash,j.expected_hash from cg_change_artifact_source s join cg_job j on j.id=s.job_id where s.change_id=? and j.id=? and s.revision_id=?",(rs,n)->List.of(rs.getString(1),rs.getString(2),rs.getString(3)),changeId,job.id(),id(approval.frozen(change).revision(),"id"));
        if(rows.size()!=1||!rows.getFirst().get(1).equals(rows.getFirst().get(2))||!rows.getFirst().get(1).equals(json.hash(json.parse(rows.getFirst().get(0))))) throw ApiException.of("STORAGE_INTEGRITY","Appendix retry source không khớp.");
    }
    public EntityStore.Entity retry(UUID id,String tag,String reason,Actor actor) {
        var preview=store.require("Job",id); changes.lock(id(preview.body(),"parentId")); var job=store.lock("Job",id); ETags.check(tag,job.version()); requireRetry(job,actor);
        if(!queue.retry(id)) throw ApiException.of("STATE_CONFLICT","Job đã thay đổi."); var updated=store.require("Job",id); audit.record(actor,"RETRY",updated,reason); return updated;
    }
}
