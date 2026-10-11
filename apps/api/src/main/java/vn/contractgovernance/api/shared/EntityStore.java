package vn.contractgovernance.api.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JsonB snapshots retain the exact frozen contract; transactions are owned by use cases. */
@Repository
public class EntityStore {
    public record Entity(String type, UUID id, UUID branchId, int version, JsonNode body) {}
    private final JdbcTemplate jdbc;
    private final JsonSupport json;
    private final Clock clock;
    public EntityStore(JdbcTemplate jdbc, JsonSupport json, Clock clock) { this.jdbc = jdbc; this.json = json; this.clock = clock; }
    public Optional<Entity> find(String type, UUID id) { return select(type, id, false); }
    public Entity require(String type, UUID id) { return find(type, id).orElseThrow(() -> ApiException.of("NOT_FOUND", "Không tìm thấy tài nguyên.")); }
    public Entity lock(String type, UUID id) { return select(type, id, true).orElseThrow(() -> ApiException.of("NOT_FOUND", "Không tìm thấy tài nguyên.")); }
    public Entity scoped(String type, UUID id, Actor actor) { var entity = require(type, id); actor.requireBranch(entity.branchId()); return entity; }
    public Entity scopedLock(String type, UUID id, Actor actor) { var entity = lock(type, id); actor.requireBranch(entity.branchId()); return entity; }
    private Optional<Entity> select(String type, UUID id, boolean locked) {
        return jdbc.query("select entity_type,id,branch_id,version,body::text from cg_entity where entity_type=? and id=?" + (locked ? " for update" : ""), (rs, row) -> new Entity(rs.getString(1), rs.getObject(2, UUID.class), rs.getObject(3, UUID.class), rs.getInt(4), json.parse(rs.getString(5))), type, id).stream().findFirst();
    }
    public Entity create(String type, UUID id, UUID branchId, JsonNode body) {
        var stored = (ObjectNode) body.deepCopy(); stored.put("id", id.toString()); stored.put("version", 1);
        jdbc.update("insert into cg_entity(entity_type,id,branch_id,version,body,created_at,updated_at) values (?,?,?,?,?::jsonb,?,?)", type, id, branchId, 1, json.stringify(stored), java.sql.Timestamp.from(clock.instant()), java.sql.Timestamp.from(clock.instant()));
        return new Entity(type, id, branchId, 1, stored);
    }
    public Entity update(Entity current, JsonNode body) {
        var stored = (ObjectNode) body.deepCopy(); stored.put("id", current.id().toString()); stored.put("version", current.version() + 1);
        var changed = jdbc.update("update cg_entity set body=?::jsonb,version=version+1,updated_at=? where entity_type=? and id=? and version=?", json.stringify(stored), java.sql.Timestamp.from(clock.instant()), current.type(), current.id(), current.version());
        if (changed != 1) throw ApiException.stale(require(current.type(), current.id()).version());
        return new Entity(current.type(), current.id(), current.branchId(), current.version() + 1, stored);
    }
    public List<Entity> all(String type) { return jdbc.query("select entity_type,id,branch_id,version,body::text from cg_entity where entity_type=? order by created_at desc,id desc", (rs,row) -> new Entity(rs.getString(1),rs.getObject(2,UUID.class),rs.getObject(3,UUID.class),rs.getInt(4),json.parse(rs.getString(5))), type); }
    public void delete(Entity current) { jdbc.update("delete from cg_entity where entity_type=? and id=? and version=?", current.type(), current.id(), current.version()); }
    public JdbcTemplate jdbc() { return jdbc; }
}
