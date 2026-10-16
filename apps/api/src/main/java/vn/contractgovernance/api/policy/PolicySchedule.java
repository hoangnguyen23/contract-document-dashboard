package vn.contractgovernance.api.policy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import vn.contractgovernance.api.shared.*;
import tools.jackson.databind.node.ObjectNode;
import java.time.*;
import java.util.UUID;

/** All schedule changes and activation share one persistent row lock. */
@Service
public class PolicySchedule {
    public record Schedule(int version,UUID activeId,UUID futureId,LocalDate futureFrom){}
    private final EntityStore store;private final CalendarService calendar;private final Clock clock;private final AuditService audit;
    public PolicySchedule(EntityStore store,CalendarService calendar,Clock clock,AuditService audit){this.store=store;this.calendar=calendar;this.clock=clock;this.audit=audit;}
    public Schedule read(){return row(false);}
    public Schedule lock(){store.jdbc().queryForObject("select lock_key from cg_schedule_lock where lock_key='POLICY_PUBLICATION' for update",String.class);return row(true);}
    private Schedule row(boolean lock){return store.jdbc().queryForObject("select version,active_policy_id,future_policy_id,future_from from cg_policy_schedule where singleton=true"+(lock?" for update":""),(rs,n)->new Schedule(rs.getInt(1),rs.getObject(2,UUID.class),rs.getObject(3,UUID.class),rs.getObject(4,LocalDate.class)));}
    public void reserve(Schedule schedule,EntityStore.Entity policy,LocalDate from,Actor actor){
        if(schedule.futureId()!=null)throw ApiException.of("POLICY_SCHEDULE_CONFLICT","Đã có một chính sách chờ hiệu lực.");
        if(schedule.activeId()!=null){var active=store.lock("Policy",schedule.activeId());var body=(ObjectNode)active.body().deepCopy();body.put("effectiveTo",from.toString());body.put("updatedAt",JsonSupport.timestamp(clock.instant()));var saved=store.update(active,body);audit.record(actor,"POLICY_INTERVAL_CLOSED",saved,"Successor policy publication");store.jdbc().update("update cg_policy_interval set effective_to=? where policy_id=?",from,active.id());}
        store.jdbc().update("insert into cg_policy_interval(policy_id,effective_from,effective_to) values(?,?,null)",policy.id(),from);
        store.jdbc().update("update cg_policy_schedule set version=version+1,future_policy_id=?,future_from=? where singleton=true",policy.id(),from);
    }
    @Scheduled(fixedDelayString="${cg.policy.activation-poll-ms:1000}")
    @Transactional
    public void activateDue(){activateLocked(lock());}
    public Schedule activateLocked(Schedule schedule){
        if(schedule.futureId()==null||calendar.localDate(clock.instant()).isBefore(schedule.futureFrom()))return schedule;
        var next=store.lock("Policy",schedule.futureId());
        if(!next.body().path("state").asString().equals("SCHEDULED")||!next.body().path("effectiveFrom").asString().equals(schedule.futureFrom().toString()))throw ApiException.of("STORAGE_INTEGRITY","Schedule không khớp policy đã đóng nguồn.");
        if(schedule.activeId()!=null){var old=store.lock("Policy",schedule.activeId());if(!old.body().path("state").asString().equals("ACTIVE"))throw ApiException.of("STORAGE_INTEGRITY","Active schedule không khớp nguồn.");var retired=(ObjectNode)old.body().deepCopy();retired.put("state","RETIRED");retired.put("scheduleVersion",schedule.version()+1);retired.put("updatedAt",JsonSupport.timestamp(clock.instant()));var saved=store.update(old,retired);audit.record(null,"POLICY_RETIRED",saved,null);}
        var activated=(ObjectNode)next.body().deepCopy();activated.put("state","ACTIVE");activated.put("scheduleVersion",schedule.version()+1);activated.put("updatedAt",JsonSupport.timestamp(clock.instant()));var saved=store.update(next,activated);audit.record(null,"POLICY_ACTIVATED",saved,null);
        store.jdbc().update("update cg_policy_schedule set version=version+1,active_policy_id=future_policy_id,future_policy_id=null,future_from=null where singleton=true");return read();
    }
    @Transactional
    public EntityStore.Entity requireApplicable(){var schedule=activateLocked(lock());if(schedule.activeId()==null)throw ApiException.of("POLICY_BLOCKED","Chưa có chính sách áp dụng.");return store.require("Policy",schedule.activeId());}
}

