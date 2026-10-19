package vn.contractgovernance.api.change;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

@RestController
@RequestMapping("/api/v1/changes")
public class ChangeController {
    private final ChangeService changes;
    private final ActorContext actors;
    public ChangeController(ChangeService changes, ActorContext actors) {
        this.changes=changes; this.actors=actors;
    }
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query) {
        return changes.list(query,actors.current());
    }
    @GetMapping("/{id}") public ResponseEntity<JsonNode> get(@PathVariable UUID id) {
        return ApiResult.ok(changes.get(id,actors.current())).response();
    }
    @PostMapping public ResponseEntity<JsonNode> create(@RequestBody JsonNode body,
            @RequestHeader(value="Idempotency-Key",required=false) String key) {
        return changes.create(body,key,actors.current()).response();
    }
    @PutMapping("/{id}") public ResponseEntity<JsonNode> update(@PathVariable UUID id,@RequestBody JsonNode body,
            @RequestHeader(value="If-Match",required=false) String tag,
            @RequestHeader(value="Idempotency-Key",required=false) String key) {
        return changes.update(id,body,tag,key,actors.current()).response();
    }
    @PostMapping("/{id}/commands") public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,
            @RequestHeader(value="If-Match",required=false) String tag,
            @RequestHeader(value="Idempotency-Key",required=false) String key) {
        return changes.command(id,body,tag,key,actors.current()).response();
    }
}
