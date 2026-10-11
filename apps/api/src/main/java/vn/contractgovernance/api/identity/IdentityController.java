package vn.contractgovernance.api.identity;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.ActorContext;
import vn.contractgovernance.api.shared.ApiResult;
import vn.contractgovernance.api.shared.CommandExecutor;
import vn.contractgovernance.api.shared.EntityStore;
import vn.contractgovernance.api.shared.ListQueries;

@RestController
@RequestMapping("/api/v1")
public class IdentityController {
    private final ActorContext actors;
    private final EntityStore store;
    private final IdentityAdministration admin;
    private final CommandExecutor commands;
    private final ListQueries lists;
    public IdentityController(ActorContext actors, EntityStore store, IdentityAdministration admin,
                              CommandExecutor commands, ListQueries lists) {
        this.actors = actors; this.store = store; this.admin = admin; this.commands = commands; this.lists = lists;
    }

    @GetMapping({"/users", "/branches"})
    ResponseEntity<JsonNode> list(@RequestParam Map<String,String> query, HttpServletRequest request) {
        var actor = actors.current(); IdentityAdministration.requireAdmin(actor); String type = type(request);
        String where = "true"; var args = new ArrayList<Object>();
        if (query.containsKey("active")) { where += " and e.body->>'active'=?"; args.add(query.get("active")); }
        if (query.containsKey("role")) { where += " and e.body->'roles' @> ?::jsonb"; args.add("[\"" + query.get("role") + "\"]"); }
        return ResponseEntity.ok(lists.page(type, actor, query, true, where, args,
                body -> admin.representation(new EntityStore.Entity(type, UUID.fromString(body.path("id").asString()), null, body.path("version").asInt(), body), actor)));
    }

    @GetMapping({"/users/{id}", "/branches/{id}"})
    ResponseEntity<JsonNode> get(@PathVariable UUID id, HttpServletRequest request) {
        var actor = actors.current(); IdentityAdministration.requireAdmin(actor);
        return ApiResult.ok(admin.representation(store.require(type(request), id), actor)).response();
    }

    @PostMapping({"/users", "/branches"})
    ResponseEntity<JsonNode> create(@RequestBody JsonNode body, @RequestHeader(value="Idempotency-Key", required=false) String key,
                                    HttpServletRequest request) {
        var actor = actors.current(); String type = type(request);
        return commands.execute(actor, key, "POST", request.getRequestURI(), body, null,
                () -> IdentityAdministration.requireAdmin(actor), () -> {
                    var created = admin.create(type, body, actor);
                    return ApiResult.created(admin.representation(created, actor), "/api/v1/" + collection(type) + "/" + created.id());
                }).response();
    }

    @PutMapping({"/users/{id}", "/branches/{id}"})
    ResponseEntity<JsonNode> update(@PathVariable UUID id, @RequestBody JsonNode body,
                                    @RequestHeader(value="Idempotency-Key", required=false) String key,
                                    @RequestHeader(value="If-Match", required=false) String etag, HttpServletRequest request) {
        var actor = actors.current(); String type = type(request);
        return commands.execute(actor, key, "PUT", request.getRequestURI(), body, etag,
                () -> { IdentityAdministration.requireAdmin(actor); store.require(type,id); },
                () -> ApiResult.ok(admin.representation(admin.update(type, id, body, etag, actor), actor))).response();
    }

    private static String type(HttpServletRequest request) { return request.getRequestURI().contains("/users") ? "User" : "Branch"; }
    private static String collection(String type) { return type.equals("User") ? "users" : "branches"; }
}
