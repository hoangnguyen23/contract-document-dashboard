package vn.contractgovernance.api.customer;

import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.EntityStore;

/** Explicit metadata administration is separate from business-record access. */
@Service
public class CustomerAccess {
    private final EntityStore store;
    public CustomerAccess(EntityStore store) { this.store = store; }

    public EntityStore.Entity requireRelationship(UUID relationshipId, Actor actor) {
        var relationship = store.require("Relationship", relationshipId);
        actor.requireBranch(relationship.branchId());
        if (!actor.hasAnyRole("CHECKER", "MANAGER", "DATA_OFFICER", "AUDITOR")
                && !(actor.hasRole("MAKER") && actor.id().toString().equals(relationship.body().path("assignedMakerId").asString())))
            throw ApiException.of("NOT_FOUND", "Không tìm thấy tài nguyên.");
        return relationship;
    }

    public EntityStore.Entity requireAssignedMaker(UUID relationshipId, Actor actor) {
        var relationship = authorizeAssignedMaker(relationshipId, actor);
        if (!relationship.body().path("active").asBoolean()) throw ApiException.of("STATE_CONFLICT", "Quan hệ đã ngừng hoạt động.");
        return relationship;
    }

    /** Authorization-only check, suitable before an idempotency replay; state guards follow in the callback. */
    public EntityStore.Entity authorizeAssignedMaker(UUID relationshipId, Actor actor) {
        var relationship = store.require("Relationship", relationshipId);
        actor.requireBranch(relationship.branchId());
        if (!actor.hasRole("MAKER")) throw ApiException.of("ROLE_FORBIDDEN", "Thao tác dành cho maker được giao quan hệ.");
        if (!actor.id().toString().equals(relationship.body().path("assignedMakerId").asString()))
            throw ApiException.of("NOT_FOUND", "Không tìm thấy tài nguyên.");
        return relationship;
    }

    public EntityStore.Entity metadataRelationship(UUID relationshipId, Actor actor) {
        if (actor.hasRole("BANK_ADMIN")) return store.require("Relationship", relationshipId);
        return requireRelationship(relationshipId, actor);
    }

    public String businessSqlScope(Actor actor, String relationshipAlias) {
        if (actor.hasAnyRole("CHECKER", "MANAGER", "DATA_OFFICER", "AUDITOR")) return "true";
        if (actor.hasRole("MAKER")) return relationshipAlias + ".body->>'assignedMakerId'=?";
        return "false";
    }
}
