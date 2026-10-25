package vn.contractgovernance.api.policy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.util.UUID;

/** Approval port: the source module locks its aggregate and materializes current scoped evidence. */
@Service
public class PolicyAccess {
    private final EntityStore store;private final PolicySchedule schedule;private final PolicyEvaluator evaluator;private final java.time.Clock clock;
    public PolicyAccess(EntityStore store,PolicySchedule schedule,PolicyEvaluator evaluator,java.time.Clock clock){this.store=store;this.schedule=schedule;this.evaluator=evaluator;this.clock=clock;}
    /** Advisory projection only: no activation, row lock, transaction interceptor or unavailable-policy exception.
     * A due published SCHEDULED policy supplies its immutable config while the actual command materializes activation.
     * Empty availability must not mark an enclosing draft-write transaction rollback-only.
     */
    public java.util.Optional<EntityStore.Entity> findApplicableHint(){
        var date=clock.instant().atZone(CalendarService.ZONE).toLocalDate();
        var ids=store.jdbc().query("select policy_id from cg_policy_interval where effective_from<=? and (effective_to is null or ?<effective_to)",
                (rs,row)->rs.getObject(1,UUID.class),date,date);
        if(ids.size()!=1)return java.util.Optional.empty();
        return store.find("Policy",ids.getFirst()).filter(policy->java.util.Set.of("ACTIVE","SCHEDULED").contains(policy.body().path("state").asString()));
    }
    @Transactional
    public EntityStore.Entity requireApplicable(){return schedule.requireApplicable();}
    /** facts must come from real scoped file/check records, never HTTP simulation flags.
     * sourceFactsHash uses the source module's canonical dossier/change projection.
     */
    public JsonNode evaluateFrozen(UUID policyId,JsonNode facts,UUID targetBranchId,String sourceFactsHash,boolean humanVerificationRequired){
        if(targetBranchId==null)throw new IllegalArgumentException("Explicit business target branch is required");
        var policy=store.require("Policy",policyId);
        if(!java.util.Set.of("ACTIVE","RETIRED").contains(policy.body().path("state").asString()))throw ApiException.of("POLICY_BLOCKED","Business evaluation requires an applicable or frozen historical published policy.");
        return evaluator.evaluateBusiness(policy,facts,targetBranchId,sourceFactsHash,humanVerificationRequired);
    }
    /** Owning module supplies the real action instant (for example recorded signing/effect).
     * This Java-only port does not accept a clock override through any HTTP endpoint.
     */
    public JsonNode evaluateFrozenAt(UUID policyId,JsonNode facts,UUID targetBranchId,String sourceFactsHash,boolean humanVerificationRequired,java.time.Instant actionAt){
        if(targetBranchId==null)throw new IllegalArgumentException("Explicit business target branch is required");
        var policy=store.require("Policy",policyId);
        if(!java.util.Set.of("ACTIVE","RETIRED").contains(policy.body().path("state").asString()))throw ApiException.of("POLICY_BLOCKED","Business evaluation requires an applicable or frozen historical published policy.");
        return evaluator.evaluateBusinessAt(policy,facts,targetBranchId,sourceFactsHash,humanVerificationRequired,actionAt);
    }
}

