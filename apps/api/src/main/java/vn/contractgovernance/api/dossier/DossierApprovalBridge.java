package vn.contractgovernance.api.dossier;

import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.EntityStore;

/** Implemented by approval; called inside the same use-case transaction and aggregate lock. */
public interface DossierApprovalBridge {
    /** Request-independent current source guard for action hints. Candidates/checks are validated at command time. */
    void requireSubmissionReady(EntityStore.Entity dossier, Actor actor);
    EntityStore.Entity submit(EntityStore.Entity lockedDossier, JsonNode command, Actor actor);
    EntityStore.Entity withdraw(EntityStore.Entity lockedDossier, String reason, Actor actor);
}
