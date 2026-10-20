package vn.contractgovernance.api.policy;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.identity.UserDirectory;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiPredicate;

/** Same finite evaluator serves authored corpus and real scoped business input. */
@Service
public class PolicyEvaluator {
    private final JsonSupport json;private final Clock clock;private final UserDirectory users;
    public PolicyEvaluator(JsonSupport json,Clock clock,UserDirectory users){this.json=json;this.clock=clock;this.users=users;}
    public JsonNode evaluateBusiness(EntityStore.Entity policy,JsonNode facts,UUID branchId,String sourceFactsHash,boolean humanRequired) {
        return evaluateBusinessAt(policy,facts,branchId,sourceFactsHash,humanRequired,clock.instant());
    }
    public JsonNode evaluateBusinessAt(EntityStore.Entity policy,JsonNode facts,UUID branchId,String sourceFactsHash,boolean humanRequired,Instant authorityAt) {
        return evaluateBusinessDurationAt(policy,facts,branchId,sourceFactsHash,humanRequired,authorityAt,null);
    }
    /** A renewal keeps original startOn in consolidated terms but validates its extension interval. */
    public JsonNode evaluateBusinessDurationAt(EntityStore.Entity policy,JsonNode facts,UUID branchId,String sourceFactsHash,boolean humanRequired,Instant authorityAt,LocalDate durationFrom) {
        return evaluateRoundBusiness(policy,facts,branchId,sourceFactsHash,humanRequired,authorityAt,durationFrom,Set.of());
    }
    // Only PolicyAccess supplies historical IDs after checking persisted Round decision evidence.
    JsonNode evaluateRoundBusiness(EntityStore.Entity policy,JsonNode facts,UUID branchId,String sourceFactsHash,boolean humanRequired,Instant authorityAt,LocalDate durationFrom,Set<UUID> historicalReviewers) {
        Objects.requireNonNull(authorityAt,"Real business evaluation requires an action instant");
        Objects.requireNonNull(branchId,"Real business evaluation requires a target branch");
        return evaluateAt(policy,facts,sourceFactsHash,(id,role)->{
            if(historicalReviewers.contains(id))return true;
            try {var actor=users.actor(id);return actor.hasRole(role)&&actor.inBranch(branchId);}catch(ApiException invalid){return false;}
        },humanRequired,authorityAt,durationFrom);
    }
    JsonNode evaluate(EntityStore.Entity policy,JsonNode facts,String factsHash,BiPredicate<UUID,String> eligible,boolean humanRequired) {
        Instant startInstant;
        try{startInstant=LocalDate.parse(facts.path("terms").path("startOn").asString()).atStartOfDay(CalendarService.ZONE).toInstant();}catch(DateTimeException invalid){startInstant=clock.instant();}
        return evaluateAt(policy,facts,factsHash,eligible,humanRequired,startInstant);
    }
    private JsonNode evaluateAt(EntityStore.Entity policy,JsonNode facts,String factsHash,BiPredicate<UUID,String> eligible,boolean humanRequired,Instant authorityAt) {
        return evaluateAt(policy,facts,factsHash,eligible,humanRequired,authorityAt,null);
    }
    private JsonNode evaluateAt(EntityStore.Entity policy,JsonNode facts,String factsHash,BiPredicate<UUID,String> eligible,boolean humanRequired,Instant authorityAt,LocalDate durationFrom) {
        JsonNode config=policy.body().path("config"),terms=facts.path("terms");var results=json.array();
        String source=policy.id().toString();long fee;
        try{fee=Long.parseLong(terms.path("feeVnd").asString());}catch(NumberFormatException invalid){fee=-1;}
        long min=Long.parseLong(config.path("minFeeVnd").asString()),ref=Long.parseLong(config.path("referenceFeeVnd").asString()),max=Long.parseLong(config.path("maxFeeVnd").asString());
        add(results,"FEE_RANGE",fee>=0&&fee<=9999999999L&&fee>=min&&fee<=max,"INTERNAL_POLICY",source,Long.toString(fee),min+".."+max,"Phí trong khoảng chính sách.");
        add(results,"PAYER_AGREED",terms.path("feePayer").asString().equals("BENEFICIARY"),"HARD_INVARIANT","A1.2.0",terms.path("feePayer").asString(),"BENEFICIARY","Phải thống nhất bên chịu phí.");
        boolean clause=terms.path("currency").asString().equals("VND")&&switch(terms.path("pricingMode").asString()){
            case "NEGOTIATED_FIXED"->terms.path("clauseVersion").asString().equals("NEGOTIATED-VA-1");case "STANDARD_TARIFF"->terms.path("clauseVersion").asString().equals("TARIFF-ACK-7D-1");default->false;};
        add(results,"CLAUSE",clause,"HARD_INVARIANT","A1.2.0",terms.path("clauseVersion").asString(),"Điều khoản đúng pricingMode","Loại biểu phí phải khớp điều khoản.");
        String kind=terms.path("termKind").asString();boolean allowed=contains(config.path("allowedTermKinds"),kind);boolean duration=false;
        LocalDate start=null;try{start=durationFrom==null?LocalDate.parse(terms.path("startOn").asString()):durationFrom;if(kind.equals("OPEN_ENDED"))duration=terms.path("endExclusive").isNull();else if(kind.equals("FIXED")&&terms.hasNonNull("endExclusive")){long days=ChronoUnit.DAYS.between(start,LocalDate.parse(terms.path("endExclusive").asString()));duration=days>=1&&days<=3650&&days>=config.path("minFixedTermDays").asInt()&&days<=config.path("maxFixedTermDays").asInt();}}catch(DateTimeException ignored){}
        add(results,"TERM",allowed&&duration,"INTERNAL_POLICY",source,kind,"Loại và khoảng thời hạn hợp lệ","Ngày kết thúc exclusive theo cấu hình.");
        boolean documents=true,human=true;for(var purpose:config.path("requiredDocumentPurposes")){boolean clean=false,verified=false;for(var doc:facts.path("documents"))if(doc.path("purpose").asString().equals(purpose.asString())){clean|=doc.path("clean").asBoolean();verified|=doc.path("clean").asBoolean()&&doc.path("humanVerified").asBoolean();}documents&=clean;human&=verified;}
        add(results,"REQUIRED_DOCUMENTS",documents,"HARD_INVARIANT","A1.2.0",documents?"CLEAN":"MISSING_OR_UNCLEAN","CLEAN required purposes","Bằng chứng bắt buộc phải được scanner xác nhận.");
        if(humanRequired){add(results,"EVIDENCE_REVIEW",human,"HARD_INVARIANT","A1.2.0",human?"VERIFIED":"NOT_VERIFIED","Human VERIFIED","Đánh giá của con người độc lập với scanner.");if(!human)((ObjectNode)results.get(results.size()-1)).put("result","REVIEW_REQUIRED");}
        var representation=facts.path("representation");boolean authority=false;
        try{Instant from=LocalDate.parse(representation.path("validFrom").asString()).atStartOfDay(CalendarService.ZONE).toInstant();authority=!authorityAt.isBefore(from)&&(!representation.hasNonNull("validTo")||authorityAt.isBefore(LocalDate.parse(representation.path("validTo").asString()).atStartOfDay(CalendarService.ZONE).toInstant()))&&(!representation.hasNonNull("knownRevocationAt")||authorityAt.isBefore(Instant.parse(representation.path("knownRevocationAt").asString())))&&representation.hasNonNull("sourceFileId");}catch(DateTimeException ignored){}
        if(humanRequired)authority=authority&&representation.hasNonNull("checkedAt")&&representation.hasNonNull("checkEvidenceFileId")&&!Instant.parse(representation.path("checkedAt").asString()).isAfter(clock.instant());
        add(results,"AUTHORITY",authority,"HARD_INVARIANT","A1.2.0",authority?"VALID":"INVALID","Nguồn thẩm quyền còn hiệu lực tại mốc action","Interval validTo exclusive; thu hồi chỉ chặn từ instant có hiệu lực.");
        UUID l1=uuid(facts.path("candidateL1Id")),l2=uuid(facts.path("candidateL2Id"));Set<UUID> contributors=new HashSet<>();facts.path("contributorIds").forEach(id->contributors.add(UUID.fromString(id.asString())));
        boolean dual=fee<ref;boolean sod=l1!=null&&!contributors.contains(l1)&&(l2==null||(!l2.equals(l1)&&!contributors.contains(l2)));
        add(results,"REVIEWER_SOD",sod,"HARD_INVARIANT","A1.2.0",sod?"INDEPENDENT":"CONFLICT","L1/L2 khác nhau và không contributor","Stable IDs giữ lịch sử đóng góp.");
        boolean reviewers=l1!=null&&eligible.test(l1,"CHECKER")&&(dual?l2!=null&&eligible.test(l2,"MANAGER"):l2==null);
        add(results,"REVIEWER_ELIGIBILITY",reviewers,"HARD_INVARIANT","A1.2.0",reviewers?"ELIGIBLE":"INELIGIBLE","Current active role và target scope","Không tự cấp quyền từ simulation facts.");
        boolean mandatory=true;for(var check:facts.path("mandatoryChecks")){boolean pass=check.path("result").asString().equals("PASS")&&Instant.parse(check.path("checkedAt").asString()).compareTo(clock.instant())<=0;mandatory&=pass;add(results,"MANDATORY_"+check.path("code").asString(),pass,"REVIEWED_EXTERNAL_CONDITION",source,check.path("result").asString(),"PASS","Điều kiện bên ngoài đã được rà soát: "+check.path("code").asString());if(check.path("result").asString().equals("REVIEW_REQUIRED"))((ObjectNode)results.get(results.size()-1)).put("result","REVIEW_REQUIRED");}
        boolean all=true;for(var result:results)all&=result.path("result").asString().equals("PASS");var out=json.object();out.put("policyVersionId",policy.id().toString());out.put("configHash",policy.body().path("configHash").asString());out.put("factsHash",factsHash);out.put("route",all?(dual?"L1_L2":"L1"):"BLOCKED");out.set("results",results);out.put("evaluatedAt",JsonSupport.timestamp(clock.instant()));return out;
    }
    private void add(tools.jackson.databind.node.ArrayNode results,String rule,boolean passed,String source,String version,String actual,String expected,String message){var r=json.object();r.put("ruleId",rule);r.put("result",passed?"PASS":"BLOCK");r.put("sourceKind",source);r.put("sourceVersion",version);r.put("actual",actual);r.put("expected",expected);r.put("message",message);results.add(r);}
    static boolean contains(JsonNode array,String value){for(var item:array)if(item.asString().equals(value))return true;return false;}
    private static UUID uuid(JsonNode value){return value.isNull()||value.isMissingNode()?null:UUID.fromString(value.asString());}
}


