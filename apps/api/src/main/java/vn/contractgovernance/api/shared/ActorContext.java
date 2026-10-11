package vn.contractgovernance.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ActorContext {
    public static final String ATTRIBUTE = ActorContext.class.getName() + ".actor";
    public static void bind(HttpServletRequest request, Actor actor) { request.setAttribute(ATTRIBUTE, actor); }
    public Actor current() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet && servlet.getRequest().getAttribute(ATTRIBUTE) instanceof Actor actor) return actor;
        throw ApiException.of("UNAUTHENTICATED", "Bạn cần đăng nhập.");
    }
}
