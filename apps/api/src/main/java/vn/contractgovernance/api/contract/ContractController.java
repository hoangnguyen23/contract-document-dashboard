package vn.contractgovernance.api.contract;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {
    private final OriginalAgreementService service;
    private final ContractLifecycle lifecycle;
    private final ActorContext actors;
    public ContractController(OriginalAgreementService service, ContractLifecycle lifecycle, ActorContext actors) {
        this.service=service; this.lifecycle=lifecycle; this.actors=actors;
    }
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query) { return service.list(query,actors.current()); }
    @GetMapping("/{id}") public ResponseEntity<JsonNode> get(@PathVariable UUID id) { return ApiResult.ok(lifecycle.get(id,actors.current())).response(); }
    @GetMapping("/{id}/versions") public JsonNode versions(@PathVariable UUID id,@RequestParam Map<String,String> query) { return lifecycle.versions(id,query,actors.current()); }
    @PostMapping("/{id}/commands") public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,
            @RequestHeader(value="If-Match",required=false) String tag,
            @RequestHeader(value="Idempotency-Key",required=false) String key) {
        return lifecycle.command(id,body,tag,key,actors.current()).response();
    }
}
