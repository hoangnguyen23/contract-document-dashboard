package vn.contractgovernance.api.policy;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import java.time.*;
import java.util.*;

/** Product regression operation, with independently authored expected outcomes. No expression engine. */
@Service
public class PolicyRegression {
    private static final UUID MAKER=id("004"),L1=id("006"),L2=id("008");
    private final JsonSupport json;private final ContractRegistry contracts;private final PolicyEvaluator evaluator;private final Clock clock;
    public PolicyRegression(JsonSupport json,ContractRegistry contracts,PolicyEvaluator evaluator,Clock clock){this.json=json;this.contracts=contracts;this.evaluator=evaluator;this.clock=clock;}
    public JsonNode run(EntityStore.Entity policy){
        JsonNode config=policy.body().path("config");var results=json.array();boolean passed=true;
        for(var vector:contracts.seed().path("policyCorpus")){
            String name=vector.path("id").asString();var facts=base(config);var terms=(ObjectNode)facts.path("terms");
            if(vector.has("feeExpression"))terms.put("feeVnd",Long.toString(fee(config,vector.path("feeExpression").asString())));
            long fee=Long.parseLong(terms.path("feeVnd").asString());if(fee<Long.parseLong(config.path("referenceFeeVnd").asString()))facts.put("candidateL2Id",L2.toString());
            if(vector.has("fixedDaysExpression")){long days=days(config,vector.path("fixedDaysExpression").asString());terms.put("termKind","FIXED");terms.put("endExclusive",LocalDate.parse(terms.path("startOn").asString()).plusDays(days).toString());}
            if(vector.has("termKind")){terms.set("termKind",vector.path("termKind"));terms.set("endExclusive",vector.path("endExclusive"));}
            if(vector.has("feePayer"))terms.set("feePayer",vector.path("feePayer"));
            if(vector.has("candidateL1Id"))facts.set("candidateL1Id",vector.path("candidateL1Id"));
            if(vector.has("candidateL2Id"))facts.set("candidateL2Id",vector.path("candidateL2Id"));
            if(vector.has("removePurpose")){var docs=json.array();for(var doc:facts.path("documents"))if(!doc.path("purpose").asString().equals(vector.path("removePurpose").asString()))docs.add(doc);facts.set("documents",docs);}
            if(vector.has("fileState"))for(var doc:facts.path("documents"))((ObjectNode)doc).put("clean",false);
            if(vector.has("mandatoryCode")){var check=json.object();check.set("code",vector.path("mandatoryCode"));check.set("result",vector.path("mandatoryResult"));check.put("basis","Authored corpus condition");check.putArray("evidenceFileIds");check.put("checkedAt",JsonSupport.timestamp(clock.instant()));facts.withArray("mandatoryChecks").add(check);}
            // These flags belong only to the frozen corpus template, never to HTTP/business facts.
            boolean active=!vector.has("candidateActive")||vector.path("candidateActive").asBoolean();
            boolean scoped=!vector.has("candidateBranchIds")||PolicyEvaluator.contains(vector.path("candidateBranchIds"),"00000000-0000-4000-8000-000000000001");
            String actual=evaluator.evaluate(policy,facts,json.hash(facts),(candidate,role)->active&&scoped&&((role.equals("CHECKER")&&candidate.equals(L1))||(role.equals("MANAGER")&&candidate.equals(L2))),true).path("route").asString();
            String expected=expected(name,config,vector.path("expectedRoute").asString());boolean success=expected.equals(actual);passed&=success;
            var result=json.object();result.put("scenarioId",name);result.put("passed",success);result.put("expectedRoute",expected);result.put("actualRoute",actual);results.add(result);
        }
        var report=json.object();report.put("corpusVersion","POLICY-CORPUS-1");report.put("configHash",policy.body().path("configHash").asString());report.set("results",results);report.put("passed",passed);report.put("generatedAt",JsonSupport.timestamp(clock.instant()));return report;
    }
    private ObjectNode base(JsonNode config){
        var facts=json.object();var source=contracts.seed().path("resources").path("Dossier");var terms=(ObjectNode)source.path("terms").deepCopy();terms.put("feeVnd",config.path("referenceFeeVnd").asString());terms.put("startOn","2027-01-07");
        if(PolicyEvaluator.contains(config.path("allowedTermKinds"),"FIXED")){terms.put("termKind","FIXED");terms.put("endExclusive",LocalDate.of(2027,1,7).plusDays(config.path("minFixedTermDays").asInt()).toString());}else{terms.put("termKind","OPEN_ENDED");terms.putNull("endExclusive");}
        facts.set("terms",terms);var rep=(ObjectNode)source.path("representation").deepCopy();rep.put("validFrom","2020-01-01");rep.putNull("validTo");rep.put("checkedAt",JsonSupport.timestamp(clock.instant()));facts.set("representation",rep);var docs=facts.putArray("documents");for(var purpose:config.path("requiredDocumentPurposes")){var doc=json.object();doc.set("purpose",purpose);doc.put("clean",true);doc.put("humanVerified",true);docs.add(doc);}
        facts.putArray("contributorIds").add(MAKER.toString());facts.put("candidateL1Id",L1.toString());facts.putNull("candidateL2Id");facts.putArray("mandatoryChecks");return facts;
    }
    private static long fee(JsonNode c,String expression){return switch(expression){case "minFeeVnd-1"->number(c,"minFeeVnd")-1;case "minFeeVnd"->number(c,"minFeeVnd");case "referenceFeeVnd-1"->number(c,"referenceFeeVnd")-1;case "referenceFeeVnd"->number(c,"referenceFeeVnd");case "maxFeeVnd"->number(c,"maxFeeVnd");case "maxFeeVnd+1"->number(c,"maxFeeVnd")+1;default->throw new IllegalStateException("Unsupported authored fee label");};}
    private static long days(JsonNode c,String expression){return switch(expression){case "minFixedTermDays-1"->c.path("minFixedTermDays").asInt()-1;case "minFixedTermDays"->c.path("minFixedTermDays").asInt();case "maxFixedTermDays"->c.path("maxFixedTermDays").asInt();case "maxFixedTermDays+1"->c.path("maxFixedTermDays").asInt()+1;default->throw new IllegalStateException("Unsupported authored duration label");};}
    // The oracle is intentionally separate from evaluation of the materialized input.
    private static String expected(String id,JsonNode c,String authored){return switch(id){case "FEE_AT_MIN"->number(c,"minFeeVnd")==number(c,"referenceFeeVnd")?"L1":"L1_L2";case "FEE_BELOW_REF"->number(c,"referenceFeeVnd")-1<number(c,"minFeeVnd")?"BLOCKED":"L1_L2";case "FIXED_AT_MIN","FIXED_AT_MAX"->PolicyEvaluator.contains(c.path("allowedTermKinds"),"FIXED")?"L1":"BLOCKED";case "OPEN_NULL_END"->PolicyEvaluator.contains(c.path("allowedTermKinds"),"OPEN_ENDED")?"L1":"BLOCKED";default->authored;};}
    private static long number(JsonNode node,String name){return Long.parseLong(node.path(name).asString());}
    private static UUID id(String suffix){return UUID.fromString("00000000-0000-4000-8000-000000000"+suffix);}
}

