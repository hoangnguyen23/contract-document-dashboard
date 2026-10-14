package vn.contractgovernance.api.identity;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.EntityStore;

/** Current database grants are authoritative; no role implies all bank branches. */
@Service
public class UserDirectory {
    private final EntityStore store;

    public UserDirectory(EntityStore store) { this.store = store; }
    public EntityStore.Entity require(UUID userId) { return store.require("User", userId); }
    public EntityStore.Entity lock(UUID userId) { return store.lock("User", userId); }

    public Actor actor(UUID userId) {
        var user = store.require("User", userId);
        if (!user.body().path("active").asBoolean())
            throw ApiException.of("UNAUTHENTICATED", "Tài khoản không còn hoạt động.");
        Set<UUID> branchIds = new LinkedHashSet<>();
        user.body().path("branchIds").forEach(value -> {
            UUID branchId = UUID.fromString(value.asString());
            store.find("Branch", branchId).filter(branch -> branch.body().path("active").asBoolean())
                    .ifPresent(branch -> branchIds.add(branchId));
        });
        return new Actor(userId, strings(user.body().path("roles")), branchIds,
                strings(user.body().path("permissions")));
    }

    public EntityStore.Entity requireEligible(UUID userId, UUID branchId, String role, Set<UUID> excluded) {
        if (excluded.contains(userId)) throw ApiException.of("SOD_VIOLATION", "Người đóng góp không được duyệt nguồn do mình sửa.");
        var user = store.find("User", userId).orElseThrow(() -> ApiException.validation("/assignedMakerId", "Người được chỉ định không hợp lệ."));
        if (!user.body().path("active").asBoolean()) throw ApiException.of("ROLE_FORBIDDEN", "Người được chỉ định không hoạt động.");
        var current = actor(userId);
        if (!current.hasRole(role) || !current.inBranch(branchId))
            throw ApiException.of("ROLE_FORBIDDEN", "Người được chỉ định thiếu vai trò hoặc phạm vi chi nhánh.");
        return user;
    }

    public Optional<EntityStore.Entity> byUsername(String username) {
        return store.jdbc().query("select id from cg_entity where entity_type='User' and body->>'username'=?",
                (rs, row) -> rs.getObject(1, UUID.class), username).stream().findFirst().flatMap(id -> store.find("User", id));
    }

    public JsonNode representation(EntityStore.Entity user, Actor caller) {
        ObjectNode body = (ObjectNode) user.body().deepCopy();
        body.putArray("allowedActions");
        body.putArray("blockedActions");
        if (caller != null && caller.hasRole("BANK_ADMIN")) body.withArray("allowedActions").add("UPDATE");
        return body;
    }

    static Set<String> strings(JsonNode node) {
        Set<String> result = new LinkedHashSet<>();
        node.forEach(value -> result.add(value.asString()));
        return result;
    }
}
