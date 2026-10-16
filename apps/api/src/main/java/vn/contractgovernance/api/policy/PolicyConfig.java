package vn.contractgovernance.api.policy;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.ContractRegistry;
import java.util.HashSet;
import java.util.Set;

/** The editor accepts a finite configuration, never executable expressions. */
@Component
public class PolicyConfig {
    private final ContractRegistry contracts;
    public PolicyConfig(ContractRegistry contracts) { this.contracts=contracts; }
    public void validate(JsonNode config) {
        contracts.validateSchema("PolicyConfig",config);
        long min=config.path("minFeeVnd").asLong(),ref=config.path("referenceFeeVnd").asLong(),max=config.path("maxFeeVnd").asLong();
        if(min>ref||ref>max)throw ApiException.validation("/config/referenceFeeVnd","Cần minFeeVnd ≤ referenceFeeVnd ≤ maxFeeVnd.");
        if(config.path("minFixedTermDays").asInt()>config.path("maxFixedTermDays").asInt())throw ApiException.validation("/config/maxFixedTermDays","Khoảng thời hạn không hợp lệ.");
        unique(config,"allowedTermKinds");Set<String> purposes=unique(config,"requiredDocumentPurposes");
        if(!purposes.containsAll(Set.of("SERVICE_APPLICATION","CORPORATE_ID","REPRESENTATION_BASIS")))throw ApiException.validation("/config/requiredDocumentPurposes","Ba loại chứng từ nền tảng luôn bắt buộc.");
    }
    private Set<String> unique(JsonNode config,String key) {
        Set<String> values=new HashSet<>();for(var value:config.path(key))if(!values.add(value.asString()))throw ApiException.validation("/config/"+key,"Danh sách không được trùng giá trị.");return values;
    }
}
