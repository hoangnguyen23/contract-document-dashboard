package vn.contractgovernance.api.approval;

import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.UUID;

@Repository
public class RevisionStore {
    public record Frozen(JsonNode revision, JsonNode metadata, UUID submittedBy) {}
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    public RevisionStore(EntityStore store, JsonSupport json, Clock clock) {
        this.store=store; this.json=json; this.clock=clock;
    }
    public int nextNumber(UUID dossierId) {
        return store.jdbc().queryForObject("select coalesce(max(number),0)+1 from cg_dossier_revision where dossier_id=?",Integer.class,dossierId);
    }
    public void save(UUID dossierId, JsonNode revision, JsonNode metadata, UUID actorId) {
        store.jdbc().update("insert into cg_dossier_revision(id,dossier_id,number,revision,source_metadata,submitted_by,created_at) values(?,?,?,?::jsonb,?::jsonb,?,?)",
                UUID.fromString(revision.path("id").asString()),dossierId,revision.path("number").asInt(),json.stringify(revision),json.stringify(metadata),actorId,Timestamp.from(clock.instant()));
    }
    public Frozen require(UUID dossierId, UUID revisionId) {
        return store.jdbc().query("select revision::text,source_metadata::text,submitted_by from cg_dossier_revision where id=? and dossier_id=?",
                (rs,row)->new Frozen(json.parse(rs.getString(1)),json.parse(rs.getString(2)),rs.getObject(3,UUID.class)),revisionId,dossierId)
                .stream().findFirst().orElseThrow(()->ApiException.of("NOT_FOUND","Không tìm thấy bản chốt của vòng xét duyệt."));
    }
}
