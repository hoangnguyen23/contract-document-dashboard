package vn.contractgovernance.api.approval;

import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.EntityStore;

/** Real contract module implementation must join the caller transaction and aggregate lock.
 * Creates exactly one PREPARED contract, immutable source bindings, PDF intent and audit.
 * No default implementation: an absent dependency rolls the final decision back.
 */
public interface OriginalAgreementPort {
    EntityStore.Entity prepare(EntityStore.Entity lockedDossier, RevisionStore.Frozen frozenSource,
                               EntityStore.Entity approvedRound, Actor actor);
}
