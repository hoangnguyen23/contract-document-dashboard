package vn.contractgovernance.api.customer;

import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.identity.IdentityAdministration;
import vn.contractgovernance.api.shared.ActorContext;
import vn.contractgovernance.api.shared.ApiResult;
import vn.contractgovernance.api.shared.CommandExecutor;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    private final ActorContext actors;
    private final CustomerService customers;
    private final CommandExecutor commands;
    public CustomerController(ActorContext actors,CustomerService customers,CommandExecutor commands){this.actors=actors;this.customers=customers;this.commands=commands;}

    @GetMapping({"/parties","/relationships","/contact-requests"})
    ResponseEntity<JsonNode> list(@RequestParam Map<String,String> query,HttpServletRequest request){return ResponseEntity.ok(customers.list(type(request),query,actors.current()));}

    @GetMapping({"/parties/{id}","/relationships/{id}","/contact-requests/{id}"})
    ResponseEntity<JsonNode> get(@PathVariable UUID id,HttpServletRequest request){var actor=actors.current();return ApiResult.ok(customers.representation(customers.get(type(request),id,actor),actor)).response();}

    @PostMapping({"/parties","/relationships","/contact-requests"})
    ResponseEntity<JsonNode> create(@RequestBody JsonNode body,@RequestHeader(value="Idempotency-Key",required=false)String key,HttpServletRequest request){
        var actor=actors.current();String type=type(request);
        return commands.execute(actor,key,"POST",request.getRequestURI(),body,null,()->customers.authorizeCreate(type,body,actor),()->{
            var created=customers.create(type,body,actor);
            return ApiResult.created(customers.representation(created,actor),"/api/v1/"+collection(type)+"/"+created.id());
        }).response();
    }

    @PutMapping({"/parties/{id}","/relationships/{id}"})
    ResponseEntity<JsonNode> update(@PathVariable UUID id,@RequestBody JsonNode body,
                                  @RequestHeader(value="Idempotency-Key",required=false)String key,
                                  @RequestHeader(value="If-Match",required=false)String etag,HttpServletRequest request){
        var actor=actors.current();String type=type(request);
        return commands.execute(actor,key,"PUT",request.getRequestURI(),body,etag,
                ()->{IdentityAdministration.requireAdmin(actor);customers.get(type,id,actor);},
                ()->ApiResult.ok(customers.representation(customers.update(type,id,body,etag,actor),actor))).response();
    }

    @PostMapping("/contact-requests/{id}/commands")
    ResponseEntity<JsonNode> decide(@PathVariable UUID id,@RequestBody JsonNode body,
                                  @RequestHeader(value="Idempotency-Key",required=false)String key,
                                  @RequestHeader(value="If-Match",required=false)String etag,HttpServletRequest request){
        var actor=actors.current();return commands.execute(actor,key,"POST",request.getRequestURI(),body,etag,
                ()->customers.authorizeDecision(id,actor),
                ()->ApiResult.ok(customers.representation(customers.decide(id,body,etag,actor),actor))).response();
    }

    @GetMapping("/reviewer-options")
    ResponseEntity<JsonNode> reviewerOptions(@RequestParam String targetKind,@RequestParam UUID targetId){return ResponseEntity.ok(customers.reviewerOptions(targetKind,targetId,actors.current()));}

    private static String type(HttpServletRequest request){String path=request.getRequestURI();return path.contains("/parties")?"Party":path.contains("/relationships")?"Relationship":"ContactRequest";}
    private static String collection(String type){return switch(type){case "Party"->"parties";case "Relationship"->"relationships";case "ContactRequest"->"contact-requests";default->throw new IllegalArgumentException();};}
}
