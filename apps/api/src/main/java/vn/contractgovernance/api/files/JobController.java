package vn.contractgovernance.api.files;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.ActorContext;
import vn.contractgovernance.api.shared.ApiResult;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
    private final JobService service;private final ActorContext actors;
    public JobController(JobService service,ActorContext actors){this.service=service;this.actors=actors;}
    @GetMapping public JsonNode list(@RequestParam Map<String,String> query){return service.list(query,actors.current());}
    @GetMapping("/{id}")public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
    @PostMapping("/{id}/commands")public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.retry(id,body,tag,key,actors.current()).response();}
}
