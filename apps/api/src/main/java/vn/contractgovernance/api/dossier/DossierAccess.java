package vn.contractgovernance.api.dossier;

import org.springframework.stereotype.Component;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.EntityStore;
import java.util.UUID;

/** Current relationship assignment is authoritative; historic maker IDs never grant access. */
@Component
public class DossierAccess {
    private final EntityStore store;
    private final CustomerAccess customers;
    public DossierAccess(EntityStore store, CustomerAccess customers) { this.store = store; this.customers = customers; }
    public EntityStore.Entity require(UUID id, Actor actor) { return check(store.require("Dossier", id), actor); }
    public EntityStore.Entity lock(UUID id, Actor actor) { return check(store.lock("Dossier", id), actor); }
    public EntityStore.Entity check(EntityStore.Entity dossier, Actor actor) {
        actor.requireBranch(dossier.branchId());
        actor.requireRole("MAKER", "CHECKER", "MANAGER", "DATA_OFFICER", "AUDITOR");
        customers.requireRelationship(UUID.fromString(dossier.body().path("relationshipId").asString()), actor);
        return dossier;
    }
    public EntityStore.Entity requireMaker(UUID id, Actor actor) { return maker(require(id, actor), actor); }
    public EntityStore.Entity authorizeMaker(UUID id,Actor actor){var dossier=require(id,actor);customers.authorizeAssignedMaker(UUID.fromString(dossier.body().path("relationshipId").asString()),actor);return dossier;}
    public EntityStore.Entity lockMaker(UUID id, Actor actor) { return maker(lock(id, actor), actor); }
    private EntityStore.Entity maker(EntityStore.Entity entity, Actor actor) {
        customers.requireAssignedMaker(UUID.fromString(entity.body().path("relationshipId").asString()), actor);
        return entity;
    }
    public void requireEditable(EntityStore.Entity entity) {
        if (!java.util.Set.of("DRAFT", "RETURNED").contains(entity.body().path("state").asString()))
            throw ApiException.of("STATE_CONFLICT", "Chỉ hồ sơ nháp hoặc được trả bổ sung mới được sửa.");
    }
}
