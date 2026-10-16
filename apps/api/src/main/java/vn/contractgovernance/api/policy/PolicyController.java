package vn.contractgovernance.api.policy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    private final PolicyService service;private final ActorContext actors;
    public PolicyController(PolicyService service,ActorContext actors){this.service=service;this.actors=actors;}
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query){return service.list(query,actors.current());}
    @PostMapping public ResponseEntity<JsonNode> create(@RequestBody JsonNode body,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.create(body,key,actors.current()).response();}
    @GetMapping("/{id}") public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
    @PutMapping("/{id}") public ResponseEntity<JsonNode> update(@PathVariable UUID id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.update(id,body,tag,key,actors.current()).response();}
    @PostMapping("/{id}/commands") public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.command(id,body,tag,key,actors.current()).response();}
    @PostMapping("/{id}/simulation") public JsonNode simulate(@PathVariable UUID id,@RequestBody JsonNode body){return service.simulate(id,body,actors.current());}
}
