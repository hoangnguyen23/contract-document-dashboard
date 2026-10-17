package vn.contractgovernance.api.contract;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {
 private final OriginalAgreementService service;private final ActorContext actors;
 public ContractController(OriginalAgreementService service,ActorContext actors){this.service=service;this.actors=actors;}
 @GetMapping public JsonNode list(@RequestParam Map<String,String> query){return service.list(query,actors.current());}
 @GetMapping("/{id}")public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
}
