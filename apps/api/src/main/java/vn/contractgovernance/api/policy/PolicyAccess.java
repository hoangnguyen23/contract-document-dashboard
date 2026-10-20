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
    /** Change-owned renewal extension source; no HTTP caller can override duration or clock. */
    public JsonNode evaluateChangeFrozen(UUID policyId,JsonNode facts,UUID branchId,String evaluationHash,boolean humanRequired,java.time.LocalDate renewalFrom) {
        var policy=store.require("Policy",policyId);
        if(!java.util.Set.of("ACTIVE","RETIRED").contains(policy.body().path("state").asString()))throw ApiException.of("POLICY_BLOCKED","Frozen policy is not published.");
        return evaluator.evaluateBusinessDurationAt(policy,facts,branchId,evaluationHash,humanRequired,clock.instant(),renewalFrom);
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
    /** Internal Round port: exemptions come only from persisted approved steps and their immutable decisions.
     * Caller source modules still validate current actor/scope/material and lock their aggregate for mutations.
     * No HTTP facts, simulation, signing or arbitrary historical clock can grant this exemption.
     */
    public JsonNode evaluateApprovalRound(UUID roundId,JsonNode facts,UUID branchId,String sourceFactsHash,java.time.LocalDate renewalFrom) {
        var round=store.require("Round",roundId);
        if(!java.util.Set.of("DOSSIER","CHANGE").contains(round.body().path("targetKind").asString())||!java.util.Objects.equals(branchId,round.branchId()))throw ApiException.of("STORAGE_INTEGRITY","Round policy scope mismatch.");
        String targetType=round.body().path("targetKind").asString().equals("DOSSIER")?"Dossier":"Change";
        var source=store.require(targetType,UUID.fromString(round.body().path("targetId").asString()));
        if(!java.util.Objects.equals(source.branchId(),branchId)||!round.id().toString().equals(source.body().path("roundId").asString()))throw ApiException.of("STORAGE_INTEGRITY","Round no longer belongs to its authoritative source.");
        var historical=new java.util.HashSet<UUID>();
        for(var step:round.body().path("steps")) {
            String level=step.path("level").asString();UUID reviewer=UUID.fromString(step.path("reviewerId").asString());
            if(!reviewer.toString().equals(facts.path(level.equals("L1")?"candidateL1Id":"candidateL2Id").asString()))throw ApiException.of("STORAGE_INTEGRITY","Reviewer facts do not match persisted Round.");
            if(!step.path("state").asString().equals("APPROVED"))continue;
            var decision=store.jdbc().queryForObject("select count(*) from cg_round_decision where round_id=? and level=? and actor_id=? and action='APPROVE'",Long.class,roundId,level,reviewer);
            if(!reviewer.toString().equals(step.path("decidedBy").asString())||!step.hasNonNull("decisionAt")||decision==null||decision!=1)throw ApiException.of("STORAGE_INTEGRITY","Approved reviewer lacks immutable decision proof.");
            historical.add(reviewer);
        }
        var policy=store.require("Policy",UUID.fromString(round.body().path("policyVersionId").asString()));
        if(!java.util.Set.of("ACTIVE","RETIRED").contains(policy.body().path("state").asString()))throw ApiException.of("POLICY_BLOCKED","Frozen policy is not published.");
        return evaluator.evaluateRoundBusiness(policy,facts,branchId,sourceFactsHash,true,clock.instant(),renewalFrom,java.util.Set.copyOf(historical));
    }
}

