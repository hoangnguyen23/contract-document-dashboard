package vn.contractgovernance.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ContractWebConfiguration implements WebMvcConfigurer {
    private final ContractRegistry contracts;
    public ContractWebConfiguration(ContractRegistry contracts){this.contracts=contracts;}
    @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new HandlerInterceptor(){
        @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){contracts.match(request).ifPresent(operation->contracts.validateParameters(operation,request));return true;}
    }).addPathPatterns("/api/v1/**");}
}
