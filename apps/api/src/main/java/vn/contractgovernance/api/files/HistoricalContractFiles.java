package vn.contractgovernance.api.files;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;

/** Retains references without deleting earlier packet/risk references. Caller owns parent lock. */
@Service
public class HistoricalContractFiles {
    private final EntityStore store;
    private final Clock clock;
    public HistoricalContractFiles(EntityStore store, Clock clock) { this.store = store; this.clock = clock; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void retain(UUID contractId, Collection<UUID> ids) {
        for (var id : new TreeSet<>(ids)) {
            var file = store.lock("File", id);
            if (file.body().path("targetKind").asString().equals("CONTRACT")
                    && file.body().path("targetId").asString().equals(contractId.toString())) {
                store.jdbc().update("insert into cg_file_association(file_id,target_type,target_id,historical,created_at) values(?,'CONTRACT',?,true,?) on conflict(file_id,target_type,target_id) do update set historical=true",
                        id, contractId, Timestamp.from(clock.instant()));
                if (!file.body().path("attached").asBoolean()) {
                    var body = (ObjectNode) file.body().deepCopy();
                    body.put("attached", true).put("updatedAt", JsonSupport.timestamp(clock.instant()));
                    store.update(file, body);
                }
            }
        }
    }
}
