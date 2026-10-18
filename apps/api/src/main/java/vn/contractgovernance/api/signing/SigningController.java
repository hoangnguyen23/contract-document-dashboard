package vn.contractgovernance.api.signing;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

@RestController
@RequestMapping("/api/v1/signing-packets")
public class SigningController {
    private final SigningService service;
    private final ActorContext actors;
    public SigningController(SigningService service, ActorContext actors) { this.service=service; this.actors=actors; }
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query) { return service.list(query,actors.current()); }
    @GetMapping("/{id}") public ResponseEntity<JsonNode> get(@PathVariable UUID id) { return ApiResult.ok(service.get(id,actors.current())).response(); }
    @PostMapping public ResponseEntity<JsonNode> create(@RequestBody JsonNode body,
            @RequestHeader(value="Idempotency-Key",required=false) String key) { return service.create(body,key,actors.current()).response(); }
    @PostMapping("/{id}/commands") public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,
            @RequestHeader(value="If-Match",required=false) String tag,
            @RequestHeader(value="Idempotency-Key",required=false) String key) { return service.command(id,body,tag,key,actors.current()).response(); }
}
