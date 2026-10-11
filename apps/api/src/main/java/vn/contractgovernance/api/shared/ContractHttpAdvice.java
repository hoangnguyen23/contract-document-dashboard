package vn.contractgovernance.api.shared;

import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Type;

@ControllerAdvice
public class ContractHttpAdvice extends RequestBodyAdviceAdapter implements ResponseBodyAdvice<Object> {
    private final ContractRegistry contracts;private final JsonSupport json;
    public ContractHttpAdvice(ContractRegistry contracts,JsonSupport json){this.contracts=contracts;this.json=json;}
    @Override public boolean supports(MethodParameter parameter,Type targetType,Class<? extends HttpMessageConverter<?>> converter){return true;}
    @Override public Object afterBodyRead(Object body,HttpInputMessage input,MethodParameter parameter,Type targetType,Class<? extends HttpMessageConverter<?>> converter){
        if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servlet)contracts.match(servlet.getRequest()).ifPresent(operation->contracts.validateRequest(operation,json.value(body)));
        return body;
    }
    @Override public boolean supports(MethodParameter parameter,Class<? extends HttpMessageConverter<?>> converter){return true;}
    @Override public Object beforeBodyWrite(Object body,MethodParameter parameter,MediaType mediaType,Class<? extends HttpMessageConverter<?>> converter,ServerHttpRequest request,ServerHttpResponse response){
        if(request instanceof ServletServerHttpRequest servlet && response instanceof ServletServerHttpResponse outgoing && body!=null && ! (body instanceof byte[]) && ! (body instanceof String))contracts.match(servlet.getServletRequest()).ifPresent(operation->contracts.validateResponse(operation,outgoing.getServletResponse().getStatus(),json.value(body)));
        return body;
    }
}
