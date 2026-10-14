package vn.contractgovernance.api.identity;

import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import vn.contractgovernance.api.shared.ActorContext;
import vn.contractgovernance.api.shared.ApiException;

@Component
public class SessionFilter extends OncePerRequestFilter {
    private final SessionService sessions;
    private final HandlerExceptionResolver resolver;
    public SessionFilter(SessionService sessions, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.sessions = sessions; this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getRequestURI().startsWith(request.getContextPath() + "/api/v1/"); }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            boolean bootstrap = path.equals("/api/v1/session") && request.getMethod().equals("GET");
            boolean login = path.equals("/api/v1/session/login") && request.getMethod().equals("POST");
            Optional<SessionService.Session> found = sessions.resolve(cookie(request));
            SessionService.Session session;
            if (found.isEmpty() && bootstrap) {
                var issued = sessions.bootstrap(); session = issued.session();
                setCookie(response, issued.opaqueToken(), false);
            } else if (found.isEmpty()) {
                throw ApiException.of(login ? "CSRF_INVALID" : "UNAUTHENTICATED",
                        login ? "Hãy lấy session và CSRF token trước khi đăng nhập." : "Bạn cần đăng nhập.");
            } else session = found.get();
            request.setAttribute(SessionService.ATTRIBUTE, session);
            if (!request.getMethod().equals("GET") && !request.getMethod().equals("HEAD") && !request.getMethod().equals("OPTIONS")
                    && !PasswordHash.tokenEquals(session.csrfToken(), request.getHeader("X-CSRF-Token")))
                throw ApiException.of("CSRF_INVALID", "CSRF token không hợp lệ.");
            if (session.actor() != null) ActorContext.bind(request, session.actor());
            else if (!bootstrap && !login) throw ApiException.of("UNAUTHENTICATED", "Bạn cần đăng nhập.");
            chain.doFilter(request, response);
        } catch (ApiException ex) {
            if (ex.code().equals("RATE_LIMITED")) response.setHeader("Retry-After", "60");
            resolver.resolveException(request, response, null, ex);
        }
    }

    static String cookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        String value = null;
        for (Cookie cookie : request.getCookies()) if (cookie.getName().equals(SessionService.COOKIE)) {
            if (value != null) return null; // Ambiguous cookies never choose one attacker-controlled token.
            value = cookie.getValue();
        }
        return value;
    }

    static void setCookie(HttpServletResponse response, String value, boolean expire) {
        var cookie = ResponseCookie.from(SessionService.COOKIE, value).secure(true).httpOnly(true)
                .path("/").sameSite("Lax");
        if (expire) cookie.maxAge(0);
        response.addHeader("Set-Cookie", cookie.build().toString());
    }
}
