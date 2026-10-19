package vn.contractgovernance.api.reporting;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.ActorContext;

@RestController
@RequestMapping("/api/v1")
public class ReportingController {
    private final DashboardService dashboard; private final AuditReportingService audit; private final ActorContext actors;
    public ReportingController(DashboardService dashboard,AuditReportingService audit,ActorContext actors) {
        this.dashboard=dashboard;this.audit=audit;this.actors=actors;
    }
    @GetMapping("/dashboard") public JsonNode dashboard(@RequestParam Map<String,String> query) { return dashboard.dashboard(actors.current(),query); }
    @GetMapping("/audit-events") public JsonNode audit(@RequestParam Map<String,String> query) { return audit.list(actors.current(),query); }
    @GetMapping(value="/exports/audit",produces="text/csv") public ResponseEntity<byte[]> export(@RequestParam Map<String,String> query) {
        return ResponseEntity.ok().contentType(new MediaType("text","csv",java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"audit.csv\"")
                .header("X-Content-Type-Options","nosniff").body(audit.export(actors.current(),query));
    }
}
