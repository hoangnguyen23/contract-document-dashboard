package vn.contractgovernance.api.control;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.files.HistoricalContractFiles;
import vn.contractgovernance.api.shared.*;

/** Creates a real pending SYSTEM STOP. It never fabricates acknowledgement or observed STOPPED. */
@Service
public class LifecycleControls {
    private final EntityStore store;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final ContractRegistry schemas;
    private final AuditService audit;
    private final HistoricalContractFiles files;
    public LifecycleControls(EntityStore store, JsonSupport json, Clock clock, CalendarService calendar,
            ContractRegistry schemas, AuditService audit, HistoricalContractFiles files) {
        this.store = store; this.json = json; this.clock = clock; this.calendar = calendar;
        this.schemas = schemas; this.audit = audit; this.files = files;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void stop(EntityStore.Entity contract, JsonNode version, Instant expiredAt) {
        if (!List.of("RUNNING", "SUSPENDED").contains(contract.body().path("serviceState").asString())) return;
        var versionId = UUID.fromString(version.path("id").asString());
        if (store.jdbc().queryForObject("select count(*) from cg_lifecycle_consequence where kind='SYSTEM_STOP' and source_id=? and source_version=?",
                Long.class, versionId, version.path("number").asInt()) != 0L) return;
        if (store.jdbc().queryForObject("select count(*) from cg_entity where entity_type='ControlRequest' and body->>'contractId'=? and body->>'kind'='STOP' and body->>'state' not in ('CONFIRMED','REJECTED','CANCELLED')",
                Long.class, contract.id().toString()) != 0L) {
            throw ApiException.of("STATE_CONFLICT", "Đã có STOP chưa kết thúc; cần xử lý nguồn kiểm soát hiện hành.");
        }
        var now = JsonSupport.timestamp(clock.instant());
        var artifact = UUID.fromString(version.path("artifactId").asString());
        files.retain(contract.id(), List.of(artifact));
        var body = json.object().put("createdAt", now).put("updatedAt", now)
                .put("branchId", contract.branchId().toString()).put("state", "REQUESTED")
                .put("contractId", contract.id().toString()).put("kind", "STOP")
                .put("effectOn", calendar.localDate(expiredAt).toString())
                .put("reason", "SYSTEM: hợp đồng FIXED đã hết kỳ hạn; cần xác nhận dừng dịch vụ bên ngoài.")
                .put("urgent", false);
        body.set("evidenceFileIds", json.array().add(artifact.toString()));
        for (var field : List.of("requesterId", "roundId", "noticeId", "externalAcknowledgement", "observedAt")) body.putNull(field);
        for (var field : List.of("allowedActions", "blockedActions", "contributorIds")) body.set(field, json.array());
        var entity = store.create("ControlRequest", UUID.randomUUID(), contract.branchId(), body);
        schemas.validateSchema("ControlRequest", entity.body());
        store.jdbc().update("insert into cg_lifecycle_consequence(kind,source_id,source_version,entity_id,created_at) values('SYSTEM_STOP',?,?,?,?)",
                versionId, version.path("number").asInt(), entity.id(), Timestamp.from(clock.instant()));
        audit.record(null, "CREATE", entity, "SYSTEM expiry; external acknowledgement pending");
    }
}
