package vn.contractgovernance.api.obligation;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
/** Persisted operative deadlines and immutable per-revision dedupe survive restarts/process races. */
@Component
public class ObligationDeadlines {
 private final DutyEvidence e;private final LifecycleObligations lifecycle;private final TransactionTemplate tx;
 public ObligationDeadlines(DutyEvidence e,LifecycleObligations lifecycle,PlatformTransactionManager manager){this.e=e;this.lifecycle=lifecycle;this.tx=new TransactionTemplate(manager);}
 @Scheduled(fixedDelayString="${cg.obligation-poll-ms:15000}") public void poll(){var ids=e.store.jdbc().query("select id from cg_entity where entity_type='Obligation' and body->>'state' not in ('CLOSED','CANCELLED') and (body->>'currentDueAt')::timestamptz<? and not exists(select 1 from cg_lifecycle_obligation_breach b where b.obligation_id=cg_entity.id and b.due_revision=(cg_entity.body->>'dueRevision')::integer) order by id limit 50",(rs,n)->rs.getObject(1,UUID.class),java.sql.Timestamp.from(e.clock.instant()));for(var id:ids)try{tx.executeWithoutResult(s->lifecycle.synchronizeBreach(e.lock("Obligation",id)));}catch(RuntimeException failure){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Obligation deadline processing deferred for opaque duty {}",id);}}
}
