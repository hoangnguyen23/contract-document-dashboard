package vn.contractgovernance.api.identity;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.AuditService;
import vn.contractgovernance.api.shared.ETags;
import vn.contractgovernance.api.shared.EntityStore;
import vn.contractgovernance.api.shared.JsonSupport;

@Service
public class IdentityAdministration {
    private final EntityStore store;
    private final UserDirectory users;
    private final SessionService sessions;
    private final AuditService audit;
    private final JsonSupport json;
    private final Clock clock;
    public IdentityAdministration(EntityStore store, UserDirectory users, SessionService sessions,
                                  AuditService audit, JsonSupport json, Clock clock) {
        this.store = store; this.users = users; this.sessions = sessions;
        this.audit = audit; this.json = json; this.clock = clock;
    }

    public static void requireAdmin(Actor actor) {
        if (!actor.hasRole("BANK_ADMIN")) throw ApiException.of("ROLE_FORBIDDEN", "Chỉ bank admin được quản lý danh mục này.");
    }

    public EntityStore.Entity create(String type, JsonNode input, Actor actor) {
        requireAdmin(actor);
        var body = base(); UUID id = UUID.randomUUID();
        switch (type) {
            case "User" -> {
                String username = input.path("username").asString();
                if (!username.matches("^[a-z][a-z0-9._-]{2,39}$"))
                    throw ApiException.validation("/username", "Tên đăng nhập phải bắt đầu bằng chữ thường và có 3–40 ký tự hợp lệ.");
                validateBranches(input.path("branchIds"));
                body.put("username", username); body.put("active", true);
                copy(input, body, "displayName", "roles", "branchIds", "permissions");
            }
            case "Branch" -> { copy(input, body, "code", "name"); body.put("active", true); }
            default -> throw new IllegalArgumentException("Not an identity resource");
        }
        EntityStore.Entity created;
        try { created = store.create(type, id, null, body); }
        catch (DuplicateKeyException ex) { throw ApiException.of("STATE_CONFLICT", "Tên đăng nhập hoặc mã chi nhánh đã tồn tại."); }
        if (type.equals("User")) sessions.createCredential(id, input.path("temporaryPassword").asString());
        audit.record(actor, "CREATE", created, type.equals("User") ? input.path("reason").asString() : null);
        return created;
    }

    public EntityStore.Entity update(String type, UUID id, JsonNode input, String ifMatch, Actor actor) {
        requireAdmin(actor); var current = store.lock(type, id); ETags.check(ifMatch, current.version());
        var body = (ObjectNode)current.body().deepCopy();
        if (type.equals("User")) {
            validateBranches(input.path("branchIds"));
            copy(input, body, "displayName", "active", "roles", "branchIds", "permissions");
        } else if (type.equals("Branch")) copy(input, body, "name", "active");
        else throw new IllegalArgumentException("Not an identity resource");
        body.put("updatedAt", JsonSupport.timestamp(clock.instant()));
        var updated = store.update(current, body);
        if (type.equals("User")) sessions.revokeUser(id);
        audit.record(actor, "UPDATE", updated, input.path("reason").asString());
        return updated;
    }

    public JsonNode representation(EntityStore.Entity entity, Actor actor) {
        if (entity.type().equals("User")) return users.representation(entity, actor);
        var body = (ObjectNode)entity.body().deepCopy();
        body.putArray("allowedActions").add("UPDATE"); body.putArray("blockedActions");
        return body;
    }

    private void validateBranches(JsonNode branches) {
        Set<UUID> seen = new java.util.HashSet<>();
        for (JsonNode value : branches) {
            UUID id = UUID.fromString(value.asString());
            if (!seen.add(id)) throw ApiException.validation("/branchIds", "Phạm vi chi nhánh bị lặp.");
            var branch = store.find("Branch", id);
            if (branch.isEmpty())
                throw ApiException.validation("/branchIds", "Phạm vi chi nhánh phải tham chiếu chi nhánh có trong danh mục.");
        }
    }

    private ObjectNode base() {
        var body = json.object(); String timestamp = JsonSupport.timestamp(clock.instant());
        body.put("createdAt", timestamp); body.put("updatedAt", timestamp);
        body.putArray("allowedActions"); body.putArray("blockedActions");
        return body;
    }

    private void copy(JsonNode input, ObjectNode body, String... names) {
        for (String name : names) body.set(name, json.nfc(input.path(name)));
    }
}
