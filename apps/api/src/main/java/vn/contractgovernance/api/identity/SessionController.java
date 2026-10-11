package vn.contractgovernance.api.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/session")
public class SessionController {
    private final SessionService sessions;
    public SessionController(SessionService sessions) { this.sessions = sessions; }

    @GetMapping
    ResponseEntity<JsonNode> bootstrap(HttpServletRequest request) {
        return ResponseEntity.ok(sessions.representation(current(request)));
    }

    @PostMapping("/login")
    ResponseEntity<JsonNode> login(@RequestBody JsonNode body, HttpServletRequest request, HttpServletResponse response) {
        var issued = sessions.login(current(request), body.path("username").asString(), body.path("password").asString(), request.getRemoteAddr());
        SessionFilter.setCookie(response, issued.opaqueToken(), false);
        return ResponseEntity.ok(sessions.representation(issued.session()));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.logout(current(request)); SessionFilter.setCookie(response, "", true);
        return ResponseEntity.noContent().build();
    }

    private static SessionService.Session current(HttpServletRequest request) {
        return (SessionService.Session)request.getAttribute(SessionService.ATTRIBUTE);
    }
}
