package vn.contractgovernance.api.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Clock;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class AuditService {
    private final JdbcTemplate jdbc; private final JsonSupport json; private final Clock clock;
    public AuditService(JdbcTemplate jdbc,JsonSupport json,Clock clock) {this.jdbc=jdbc;this.json=json;this.clock=clock;}
    public void record(Actor actor,String action,EntityStore.Entity entity,String reason) { record(actor,action,entity.type(),entity.id(),entity.version(),entity.branchId(),reason); }
    public void record(Actor actor,String action,String resourceType,UUID resourceId,int version,UUID branchId,String reason) {
        UUID id=UUID.randomUUID(),traceId=traceId(); var body=json.object();
        body.put("id",id.toString());body.put("occurredAt",JsonSupport.timestamp(clock.instant()));
        if(actor==null)body.putNull("actorId");else body.put("actorId",actor.id().toString());
        body.put("action",action);body.put("resourceType",resourceType);body.put("resourceId",resourceId.toString());body.put("resourceVersion",version);
        if(branchId==null)body.putNull("branchId");else body.put("branchId",branchId.toString());
        body.put("traceId",traceId.toString());if(reason==null)body.putNull("reason");else body.put("reason",reason);
        body.putNull("beforeHash");body.putNull("afterHash");
        jdbc.update("insert into cg_audit(id,occurred_at,actor_id,action,resource_type,resource_id,resource_version,branch_id,trace_id,reason,body) values(?,?,?,?,?,?,?,?,?,?,?::jsonb)",id,Timestamp.from(clock.instant()),actor==null?null:actor.id(),action,resourceType,resourceId,version,branchId,traceId,reason,json.stringify(body));
    }
    public static UUID traceId() {
        if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servlet && servlet.getRequest().getAttribute(TraceFilter.ATTRIBUTE) instanceof UUID trace) return trace;
        return UUID.randomUUID();
    }
}
