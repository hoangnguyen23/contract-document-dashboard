package vn.contractgovernance.api.approval;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rounds")
public class ApprovalController {
    private final ApprovalService service;
    private final ActorContext actors;
    public ApprovalController(ApprovalService service,ActorContext actors){this.service=service;this.actors=actors;}
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query){return service.list(query,actors.current());}
    @GetMapping("/{id}") public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
    @GetMapping("/{id}/snapshot") public JsonNode snapshot(@PathVariable UUID id){return service.snapshot(id,actors.current());}
    @PostMapping("/{id}/commands") public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,
            @RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.command(id,body,tag,key,actors.current()).response();}
}
