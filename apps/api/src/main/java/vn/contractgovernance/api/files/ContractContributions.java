package vn.contractgovernance.api.files;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.EntityStore;

/** Internal source contribution, intentionally absent from the closed Contract wire schema.
 * The caller already owns the Contract lock. Never acquire a parent lock after this ledger.
 */
@Service
public class ContractContributions {
    private final EntityStore store;
    private final Clock clock;

    public ContractContributions(EntityStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID contractId, UUID actorId) {
        store.jdbc().update("insert into cg_contract_contributor(contract_id,actor_id,created_at) values(?,?,?) on conflict do nothing",
                contractId, actorId, Timestamp.from(clock.instant()));
        if (all(contractId).size() > 50) {
            throw ApiException.validation("/contributorIds", "Nguồn vượt quá 50 người đóng góp.");
        }
    }

    public Set<UUID> all(UUID contractId) {
        return new LinkedHashSet<>(store.jdbc().query(
                "select actor_id from cg_contract_contributor where contract_id=? order by actor_id",
                (rs, row) -> rs.getObject(1, UUID.class), contractId));
    }
}
