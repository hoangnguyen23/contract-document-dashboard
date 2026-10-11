package vn.contractgovernance.api.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobQueue {
    public record Job(UUID id,String kind,String targetType,UUID targetId,UUID branchId,int expectedVersion,String expectedHash,JsonNode payload,int attempts,UUID leaseOwner,UUID traceId) {}
    private final JdbcTemplate jdbc;private final JsonSupport json;private final Clock clock;private final TransactionTemplate transactions;
    public JobQueue(JdbcTemplate jdbc,JsonSupport json,Clock clock,PlatformTransactionManager manager) {this.jdbc=jdbc;this.json=json;this.clock=clock;this.transactions=new TransactionTemplate(manager);}
    /** Call within the same transaction as the corresponding domain/audit write. */
    public UUID enqueue(String kind,String targetType,UUID targetId,UUID branchId,int expectedVersion,String expectedHash,String dedupeKey,JsonNode payload,UUID traceId) {
        UUID id=UUID.randomUUID();var now=Timestamp.from(clock.instant());
        jdbc.update("insert into cg_job(id,dedupe_key,kind,target_type,target_id,branch_id,expected_version,expected_hash,payload,state,available_at,trace_id,created_at,updated_at) values(?,?,?,?,?,?,?,?,?::jsonb,'QUEUED',?,?,?,?) on conflict(dedupe_key) do nothing",id,dedupeKey,kind,targetType,targetId,branchId,expectedVersion,expectedHash,json.stringify(payload),now,traceId,now,now);
        return jdbc.queryForObject("select id from cg_job where dedupe_key=?",UUID.class,dedupeKey);
    }
    public Optional<Job> claim(UUID workerId) { return claimForKind(null,workerId); }
    public Optional<Job> claimForKind(String kind,UUID workerId) {
        return transactions.execute(transaction->{
            var now=Timestamp.from(clock.instant());
            jdbc.update("update cg_job set state=case when attempts>=5 then 'FAILED' else 'QUEUED' end,lease_owner=null,lease_until=null,error_code='LEASE_EXPIRED',updated_at=? where state='RUNNING' and lease_until<=?",now,now);
            var claimed=jdbc.query("select id,kind,target_type,target_id,branch_id,expected_version,expected_hash,payload::text,attempts,trace_id from cg_job where state='QUEUED' and available_at<=? and (?::text is null or kind=?) order by available_at,created_at,id for update skip locked limit 1",(rs,row)->new Job(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getObject(4,UUID.class),rs.getObject(5,UUID.class),rs.getInt(6),rs.getString(7),json.parse(rs.getString(8)),rs.getInt(9)+1,workerId,rs.getObject(10,UUID.class)),now,kind,kind);
            if(claimed.isEmpty())return Optional.empty();var job=claimed.getFirst();
            jdbc.update("update cg_job set state='RUNNING',attempts=attempts+1,lease_owner=?,lease_until=?,updated_at=? where id=?",workerId,Timestamp.from(clock.instant().plusSeconds(60)),now,job.id());return Optional.of(job);
        });
    }
    public boolean renew(Job job) { return jdbc.update("update cg_job set lease_until=?,updated_at=? where id=? and state='RUNNING' and lease_owner=? and lease_until>?",Timestamp.from(clock.instant().plusSeconds(60)),Timestamp.from(clock.instant()),job.id(),job.leaseOwner(),Timestamp.from(clock.instant()))==1; }
    /** Domain effect and complete must share a transaction after current source/version guards. */
    public boolean succeed(Job job) { return finish(job,"SUCCEEDED",null,null); }
    public boolean cancel(Job job,String message) { return finish(job,"CANCELLED","GUARD_BLOCKED",message); }
    private boolean finish(Job job,String state,String code,String message) {return jdbc.update("update cg_job set state=?,error_code=?,error_message=?,lease_owner=null,lease_until=null,updated_at=? where id=? and state='RUNNING' and lease_owner=? and lease_until>?",state,code,message,Timestamp.from(clock.instant()),job.id(),job.leaseOwner(),Timestamp.from(clock.instant()))==1;}
    public boolean fail(Job job,String code,String message) {
        long delay=Math.min(900,5L*(1L<<Math.max(0,job.attempts()-1)));
        return jdbc.update("update cg_job set state=?,available_at=?,error_code=?,error_message=?,lease_owner=null,lease_until=null,updated_at=? where id=? and state='RUNNING' and lease_owner=? and lease_until>?",job.attempts()>=5?"FAILED":"QUEUED",Timestamp.from(clock.instant().plusSeconds(delay)),code,message,Timestamp.from(clock.instant()),job.id(),job.leaseOwner(),Timestamp.from(clock.instant()))==1;
    }
    public boolean retry(UUID id) { return jdbc.update("update cg_job set state='QUEUED',attempts=0,available_at=?,lease_owner=null,lease_until=null,error_code=null,error_message=null,updated_at=? where id=? and state='FAILED'",Timestamp.from(clock.instant()),Timestamp.from(clock.instant()),id)==1; }
}
