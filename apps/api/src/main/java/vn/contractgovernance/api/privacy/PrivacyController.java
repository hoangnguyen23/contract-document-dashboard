package vn.contractgovernance.api.privacy;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
@RestController @RequestMapping("/api/v1/data-requests") public class PrivacyController {
 private final PrivacyService service;private final ActorContext actors;
 public PrivacyController(PrivacyService service,ActorContext actors){this.service=service;this.actors=actors;}
 @GetMapping public JsonNode list(@RequestParam Map<String,String> q){return service.list(q,actors.current());}
 @GetMapping("/{id}")public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
 @PostMapping public ResponseEntity<JsonNode> create(@RequestBody JsonNode b,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.create(b,key,actors.current()).response();}
 @PostMapping("/{id}/commands")public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode b,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.command(id,b,tag,key,actors.current()).response();}
}
