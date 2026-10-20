package vn.contractgovernance.api.reporting;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc; private final JsonSupport json; private final Clock clock;
    private final ListQueries lists; private final ContractRegistry contracts;
    public DashboardService(JdbcTemplate jdbc,JsonSupport json,Clock clock,ListQueries lists,ContractRegistry contracts) {
        this.jdbc=jdbc;this.json=json;this.clock=clock;this.lists=lists;this.contracts=contracts;
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public JsonNode dashboard(Actor actor,Map<String,String> query) {
        ReportingScope.businessRole(actor);
        var branches=ReportingScope.branches(actor,query);
        if (branches.isEmpty()) throw ApiException.of("ROLE_FORBIDDEN","Không có phạm vi chi nhánh để xem báo cáo.");
        var asOf=clock.instant(); var now=Timestamp.from(asOf);
        var scope=ReportingScope.business(actor,branches,"e");
        var result=json.object().put("asOf",JsonSupport.timestamp(asOf));
        result.set("branchIds",json.value(branches.stream().map(UUID::toString).toList()));
        var stateArgs=new ArrayList<Object>(scope.args());
        var states=jdbc.query("select e.body->>'state' as state,count(*) as count from cg_entity e where e.entity_type='Dossier' and "+scope.sql()+" group by e.body->>'state'",(rs,row)->Map.entry(rs.getString(1),rs.getLong(2)),stateArgs.toArray());
        var stateCounts=new HashMap<String,Long>(); states.forEach(s->stateCounts.put(s.getKey(),s.getValue()));
        var byState=json.array();
        for(var state:List.of("DRAFT","IN_REVIEW","RETURNED","APPROVED","COMPLETED","REJECTED","CANCELLED")) byState.add(json.object().put("state",state).put("count",stateCounts.getOrDefault(state,0L)));
        result.set("dossiersByState",byState);
        result.put("reviewOverdue",count(scope,"e.entity_type='Round' and e.body->>'state'='OPEN' and (e.body->>'dueAt')::timestamptz<? and exists(select 1 from cg_entity t where t.id=(e.body->>'targetId')::uuid and t.branch_id=e.branch_id and t.entity_type=case e.body->>'targetKind' when 'DOSSIER' then 'Dossier' when 'CHANGE' then 'Change' when 'CONTROL_REQUEST' then 'ControlRequest' end and t.body->>'roundId'=e.id::text and t.body->>'state'='IN_REVIEW')",List.of(now),"count(distinct (e.body->>'targetKind',e.body->>'targetId'))"));
        result.put("activeAgreements",count(scope,"e.entity_type='Contract' and e.body->>'state'='ACTIVE'",List.of(),"count(*)"));
        var today=asOf.atZone(CalendarService.ZONE).toLocalDate();
        result.put("expiring30",count(scope,"e.entity_type='Contract' and e.body->>'state'='ACTIVE' and e.body->'currentVersion'->'terms'->>'termKind'='FIXED' and (e.body->'currentVersion'->'terms'->>'endExclusive')::date>?::date and (e.body->'currentVersion'->'terms'->>'endExclusive')::date<=?::date",List.of(today.toString(),today.plusDays(30).toString()),"count(*)"));
        result.put("overdueObligations",count(scope,"e.entity_type='Obligation' and e.body->>'state' in ('OPEN','EVIDENCE_SUBMITTED','RETURNED') and (e.body->>'currentDueAt')::timestamptz<?",List.of(now),"count(*)"));
        result.put("historicalBreaches",count(scope,"e.entity_type='Obligation' and exists(select 1 from cg_lifecycle_obligation_breach b where b.obligation_id=e.id and b.recorded_at<=?)",List.of(now),"count(*)"));
        result.put("unresolvedCloseout",count(scope,"e.entity_type='Obligation' and e.body->>'type'='CLOSEOUT' and e.body->>'state' in ('OPEN','EVIDENCE_SUBMITTED','RETURNED')",List.of(),"count(*)"));
        result.put("noticeFailures",count(scope,"e.entity_type='Notice' and e.body->>'state'='FAILED'",List.of(),"count(*)"));
        result.put("pendingServiceControls",count(scope,"e.entity_type='ControlRequest' and e.body->>'state' in ('REQUESTED','IN_REVIEW','APPROVED')",List.of(),"count(*)"));
        result.put("timelineProcessingLagSeconds",lists.timelineLag(actor,"e.entity_type='Contract' and "+scope.sql(),scope.args(),asOf));
        contracts.validateSchema("Dashboard",result);
        return result;
    }
    private long count(ReportingScope.Predicate scope,String condition,List<Object> extra,String expression) {
        var args=new ArrayList<Object>(scope.args()); args.addAll(extra);
        return Objects.requireNonNull(jdbc.queryForObject("select "+expression+" from cg_entity e where "+scope.sql()+" and "+condition,Long.class,args.toArray()));
    }
}
