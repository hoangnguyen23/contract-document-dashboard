package vn.contractgovernance.api.contract;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.control.LifecycleControls;
import vn.contractgovernance.api.customer.CustomerAccess;
import vn.contractgovernance.api.files.HistoricalContractFiles;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.obligation.LifecycleObligations;
import vn.contractgovernance.api.signing.SigningEvidence;
import vn.contractgovernance.api.shared.*;

/** Single original-contract timeline authority. Maintenance commits before command ETag checks.
 * Existing final approval owns Dossier/Relationship before creating a NEW Contract; all paths for
 * an existing contract lock Dossier -> Contract -> Relationship -> Party -> File -> ledger/job.
 */
@Service
public class ContractLifecycle {
    private final org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeTimeline> changes;
    private final EntityStore store;
    private final OriginalAgreementService originals;
    private final SigningEvidence evidence;
    private final CustomerAccess customers;
    private final UserDirectory users;
    private final HistoricalContractFiles historical;
    private final LifecycleObligations obligations;
    private final LifecycleControls controls;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;
    private final AuditService audit;
    private final JobQueue jobs;
    private final CommandExecutor commands;
    private final ContractRegistry schemas;
    private final ListQueries lists;
    private final TransactionTemplate maintenance;

    public ContractLifecycle(org.springframework.beans.factory.ObjectProvider<vn.contractgovernance.api.change.ChangeTimeline> changes,EntityStore store, OriginalAgreementService originals, SigningEvidence evidence,
            CustomerAccess customers, UserDirectory users, HistoricalContractFiles historical,
            LifecycleObligations obligations, LifecycleControls controls, JsonSupport json, Clock clock,
            CalendarService calendar, AuditService audit, JobQueue jobs, CommandExecutor commands,
            ContractRegistry schemas, ListQueries lists, PlatformTransactionManager manager) {
        this.changes=changes; this.store = store; this.originals = originals; this.evidence = evidence; this.customers = customers;
        this.users = users; this.historical = historical; this.obligations = obligations; this.controls = controls;
        this.json = json; this.clock = clock; this.calendar = calendar; this.audit = audit; this.jobs = jobs;
        this.commands = commands; this.schemas = schemas; this.lists = lists;
        this.maintenance = new TransactionTemplate(manager);
        this.maintenance.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public EntityStore.Entity require(UUID id, Actor actor) {
        return originals.require(id, users.actor(actor.id()));
    }

    public EntityStore.Entity lock(UUID id) {
        var preview = store.require("Contract", id);
        store.lock("Dossier", SigningEvidence.uuid(preview.body(), "dossierId"));
        return store.lock("Contract", id);
    }

    public EntityStore.Entity maintain(UUID id, Actor actor) {
        require(id, actor);
        return maintenance.execute(status -> {
            var contract = lock(id);
            require(id, actor);
            return advanceLocked(contract);
        });
    }

    /** Must be called in a real transaction holding the Dossier/Contract locks. */
    public EntityStore.Entity advanceLocked(EntityStore.Entity contract) {
        var state = contract.body().path("state").asString();
        if (!List.of("SCHEDULED", "ACTIVE").contains(state)) {
            if (state.equals("PREPARED") && !evidence.materialUnchanged(contract)
                    && !contract.body().path("readiness").asString().equals("REVIEW_REQUIRED")) {
                var blocked = (ObjectNode) contract.body().deepCopy();
                blocked.put("readiness", "REVIEW_REQUIRED");
                return save(contract, blocked, null, "MATERIAL_SOURCE_CHANGED", "Cần hồ sơ mới cho material source đã đổi");
            }
            return contract;
        }
        if(state.equals("ACTIVE")) contract=changes.getObject().advanceLocked(contract);
        var terms = contract.body().hasNonNull("currentVersion")?contract.body().path("currentVersion").path("terms"):contract.body().path("approvedTerms");
        var start = calendar.startOfDay(LocalDate.parse(terms.path("startOn").asString()));
        var now = clock.instant();
        if (state.equals("SCHEDULED")) {
            if (!evidence.materialUnchanged(contract)) {
                if (!contract.body().path("readiness").asString().equals("REVIEW_REQUIRED")) {
                    var blocked = (ObjectNode) contract.body().deepCopy();
                    blocked.put("readiness", "REVIEW_REQUIRED");
                    return save(contract, blocked, null, "MATERIAL_SOURCE_CHANGED", "Material source changed before activation");
                }
                return contract;
            }
            if (!contract.body().path("readiness").asString().equals("READY") || unresolvedRisk(contract.body())) return contract;
            try {
                requireReadyEvidence(contract);
            } catch (ApiException guard) {
                var blocked = (ObjectNode) contract.body().deepCopy();
                blocked.put("readiness", "REVIEW_REQUIRED");
                return save(contract, blocked, null, "READINESS_GUARD_BLOCKED", guard.code() + ": current readiness evidence requires review");
            }
            if (now.isBefore(start)) return contract;
            pendingChangeGuard(contract);
            if (!Instant.parse(contract.body().path("readinessVerifiedAt").asString()).isBefore(start)) {
                throw ApiException.of("LATE_SIGNING", "Xác minh readiness không đúng hạn.");
            }
            var version = json.object().put("id", UUID.randomUUID().toString()).put("number", 1)
                    .put("sourceKind", "ORIGINAL").put("sourceId", contract.body().path("dossierId").asString())
                    .put("effectiveFrom", terms.path("startOn").asString())
                    .put("artifactId", contract.body().path("approvedArtifactId").asString())
                    .put("artifactHash", contract.body().path("approvedArtifactHash").asString())
                    .put("createdAt", JsonSupport.timestamp(now));
            version.set("terms", terms.deepCopy());
            // Version interval stays open while current; contractual endExclusive is immutable terms.
            version.putNull("effectiveTo");
            schemas.validateSchema("ContractVersion", version);
            store.jdbc().update("insert into cg_contract_version(id,contract_id,number,body) values(?,?,1,?::jsonb)",
                    SigningEvidence.uuid(version, "id"), contract.id(), json.stringify(version));
            effect(contract.id(), "ACTIVE", start);
            var body = (ObjectNode) contract.body().deepCopy();
            body.put("state", "ACTIVE").put("effectiveAt", JsonSupport.timestamp(start)).put("recordedAt", JsonSupport.timestamp(now));
            body.set("currentVersion", version);
            contract = save(contract, body, null, "ACTIVATE", "Original effectivity; observed service unchanged");
            var dossier = store.require("Dossier", SigningEvidence.uuid(body, "dossierId"));
            if (!dossier.body().path("state").asString().equals("APPROVED")) throw ApiException.of("STATE_CONFLICT", "Hồ sơ không còn APPROVED.");
            var dossierBody = (ObjectNode) dossier.body().deepCopy();
            dossierBody.put("state", "COMPLETED").put("updatedAt", JsonSupport.timestamp(now));
            audit.record(null, "COMPLETE", store.update(dossier, dossierBody), "First ACTIVE");
            if (terms.path("termKind").asString().equals("FIXED")) {
                obligations.termReview(contract, version, originals.source(contract.id()).body().path("revision").path("contributorIds"), start);
            }
        }
        if (contract.body().path("state").asString().equals("ACTIVE") && terms.path("termKind").asString().equals("FIXED")) {
            var end = calendar.startOfDay(LocalDate.parse(terms.path("endExclusive").asString()));
            if (!now.isBefore(end)) {
                pendingChangeGuard(contract);
                var body = (ObjectNode) contract.body().deepCopy();
                body.put("state", "EXPIRED").put("recordedAt", JsonSupport.timestamp(now));
                var version = (ObjectNode) body.path("currentVersion").deepCopy();
                version.set("effectiveTo", terms.path("endExclusive").deepCopy());
                body.set("currentVersion", version);
                store.jdbc().update("insert into cg_contract_version_closure(version_id,effective_to,recorded_at) values(?,?::date,?)",
                        SigningEvidence.uuid(version, "id"), terms.path("endExclusive").asString(), Timestamp.from(now));
                obligations.closeout(contract, version, originals.source(contract.id()).body().path("revision").path("contributorIds"), end);
                controls.stop(contract, version, end);
                effect(contract.id(), "EXPIRED", end);
                contract = save(contract, body, null, "EXPIRE", "Fixed end boundary; service acknowledgement remains pending");
            }
        }
        return contract;
    }

    public void pendingChangeGuard(EntityStore.Entity contract) {
        // Change application is owned by advanceLocked under this same Contract lock.
        // Pending drafts/READY proposals do not reserve a slot or block expiry.
        if(contract.body().hasNonNull("scheduledChangeId")) {
            var change=store.require("Change",SigningEvidence.uuid(contract.body(),"scheduledChangeId"));
            if(!change.body().path("state").asString().equals("SCHEDULED")) throw ApiException.of("SCHEDULE_CONFLICT","Future slot is inconsistent.");
        }
    }

    public void requireReadyEvidence(EntityStore.Entity contract) {
        evidence.requireMaterial(contract);
        if (!contract.body().hasNonNull("signingPacketId")) throw ApiException.of("READINESS_BLOCKED", "Thiếu verified packet.");
        var packet = store.require("SigningPacket", SigningEvidence.uuid(contract.body(), "signingPacketId"));
        if (!packet.body().path("verificationStatus").asString().equals("VERIFIED")
                || !packet.body().path("targetId").asString().equals(contract.id().toString())) {
            throw ApiException.of("READINESS_BLOCKED", "Packet không VERIFIED trên đúng contract.");
        }
        verifyFrozenPacket(contract, packet.body());
        var assessment = store.jdbc().query("select assessment::text from cg_signing_assessment where packet_id=?",
                (rs, row) -> json.parse(rs.getString(1)), packet.id()).stream().findFirst()
                .orElseThrow(() -> ApiException.of("READINESS_BLOCKED", "Thiếu assessment độc lập."));
        // A historical valid signature does not expire merely because today's date exceeds validTo.
        var refs = new TreeSet<>(evidence.packetFiles(packet.body()));
        assessment.path("checks").forEach(check -> refs.add(SigningEvidence.uuid(check, "fileId")));
        assessment.path("mandatoryChecks").forEach(check -> refs.addAll(SigningEvidence.uniqueIds(check.path("evidenceFileIds"), "/mandatoryChecks/evidenceFileIds")));
        if (!evidence.manifest(contract, refs, null).equals(assessment.path("fileManifest")))
            throw ApiException.of("STORAGE_INTEGRITY", "Chứng cứ assessment khác nguồn đã chốt.");
        evidence.assessment(contract, packet.body(), assessment.path("checks"), assessment.path("mandatoryChecks"), null);
    }

    public void verifyFrozenPacket(EntityStore.Entity contract, JsonNode packet) {
        var rows = store.jdbc().query("select input::text,input_hash,file_manifest::text from cg_signing_input where packet_id=? and contract_id=?",
                (rs, row) -> List.of(rs.getString(1), rs.getString(2), rs.getString(3)), SigningEvidence.uuid(packet, "id"), contract.id());
        if (rows.size() != 1 || !json.hash(json.parse(rows.getFirst().getFirst())).equals(rows.getFirst().get(1))) {
            throw ApiException.of("STORAGE_INTEGRITY", "Nguồn packet thiếu hoặc thay đổi.");
        }
        var input = json.parse(rows.getFirst().getFirst());
        for (var field : List.of("targetKind", "targetId", "artifactId", "artifactHash", "signedCopyIds", "signingMethod", "authority", "bankSignedAt", "beneficiarySignedAt")) {
            if (!input.path(field).equals(packet.path(field))) throw ApiException.of("STORAGE_INTEGRITY", "Packet khác immutable input.");
        }
        if (!evidence.manifest(contract, evidence.packetFiles(packet), null).equals(json.parse(rows.getFirst().get(2)))) {
            throw ApiException.of("STORAGE_INTEGRITY", "Bytes packet khác nguồn đã chốt.");
        }
    }

    public JsonNode get(UUID id, Actor actor) { return project(maintain(id, actor).body(), actor); }

    public ApiResult command(UUID id, JsonNode request, String tag, String key, Actor actor) {
        authorizeCommand(id, request.path("action").asString(), actor);
        ETags.require(tag);
        return commands.execute(actor, key, "POST", "/api/v1/contracts/" + id + "/commands", request, tag,
                () -> authorizeCommand(id, request.path("action").asString(), actor),
                () -> maintain(id, actor), () -> {
                    var contract = lock(id);
                    authorizeCommand(id, request.path("action").asString(), actor);
                    ETags.check(tag, contract.version());
                    var body = (ObjectNode) contract.body().deepCopy();
                    var action = request.path("action").asString();
                    switch (action) {
                        case "CANCEL" -> cancel(contract, body, request, actor);
                        case "HOLD_READINESS" -> hold(contract, body, request, actor);
                        case "RELEASE_READINESS" -> release(contract, body, request, actor);
                        default -> throw ApiException.validation("/action", "Command không hỗ trợ.");
                    }
                    return ApiResult.ok(project(save(contract, body, actor, action,
                            request.has("reason") ? request.path("reason").asString() : request.path("basis").asString()).body(), actor));
                });
    }

    private void authorizeCommand(UUID id, String action, Actor actor) {
        var contract = require(id, actor);
        var current = users.actor(actor.id());
        if (action.equals("HOLD_READINESS")) { current.requireRole("CHECKER"); return; }
        if (action.equals("RELEASE_READINESS")) { evidence.independent(contract, current); return; }
        if (action.equals("CANCEL")) {
            if (hasSignature(contract.id())) evidence.independent(contract, current);
            else customers.authorizeAssignedMaker(SigningEvidence.uuid(contract.body(), "relationshipId"), current);
        }
    }

    private boolean hasSignature(UUID id) {
        return store.jdbc().queryForObject("select count(*) from cg_signing_input where contract_id=?", Long.class, id) != 0L;
    }

    private void cancel(EntityStore.Entity contract, ObjectNode body, JsonNode request, Actor actor) {
        if (!List.of("PREPARED", "SCHEDULED").contains(body.path("state").asString()) || body.hasNonNull("currentVersion")) {
            throw ApiException.of("STATE_CONFLICT", "Chỉ hủy trước first ACTIVE.");
        }
        var ids = SigningEvidence.uniqueIds(request.path("cancellationEvidenceFileIds"), "/cancellationEvidenceFileIds");
        if (hasSignature(contract.id()) && ids.isEmpty()) throw ApiException.of("READINESS_BLOCKED", "Bản đã ký cần bằng chứng hủy độc lập.");
        evidence.manifest(contract, ids, actor);
        historical.retain(contract.id(), ids);
        body.put("state", "CANCELLED");
        var dossier = store.require("Dossier", SigningEvidence.uuid(body, "dossierId"));
        var dossierBody = (ObjectNode) dossier.body().deepCopy();
        dossierBody.put("state", "CANCELLED").put("updatedAt", JsonSupport.timestamp(clock.instant()));
        audit.record(actor, "CANCEL", store.update(dossier, dossierBody), request.path("reason").asString());
        store.jdbc().update("update cg_job set state='CANCELLED',lease_owner=null,lease_until=null,error_code='GUARD_BLOCKED',updated_at=? where target_type='CONTRACT' and target_id=? and state in ('QUEUED','RUNNING','FAILED')",
                Timestamp.from(clock.instant()), contract.id());
    }

    private void hold(EntityStore.Entity contract, ObjectNode body, JsonNode request, Actor actor) {
        if (!List.of("PREPARED", "SCHEDULED", "ACTIVE").contains(body.path("state").asString())) throw ApiException.of("STATE_CONFLICT", "Nguồn đã kết thúc.");
        if (body.path("readinessRisks").size() >= 100) throw ApiException.of("STATE_CONFLICT", "Đã đạt giới hạn lịch sử risk của schema.");
        var ids = SigningEvidence.uniqueIds(request.path("evidenceIds"), "/evidenceIds");
        evidence.manifest(contract, ids, actor); historical.retain(contract.id(), ids);
        var risk = json.object().put("id", UUID.randomUUID().toString()).put("raisedBy", actor.id().toString())
                .put("raisedAt", JsonSupport.timestamp(clock.instant())).put("reason", request.path("reason").asString());
        risk.set("evidenceIds", request.path("evidenceIds").deepCopy());
        risk.putNull("resolutionBy").putNull("resolvedAt").putNull("resolutionBasis");
        risk.set("resolutionEvidenceIds", json.array());
        body.withArray("readinessRisks").add(risk); body.put("readiness", "REVIEW_REQUIRED");
    }

    private void release(EntityStore.Entity contract, ObjectNode body, JsonNode request, Actor actor) {
        if (!List.of("PREPARED", "SCHEDULED", "ACTIVE").contains(body.path("state").asString())) throw ApiException.of("STATE_CONFLICT", "Nguồn đã kết thúc.");
        evidence.requireMaterial(contract);
        ObjectNode risk = null;
        for (var item : body.path("readinessRisks")) if (item.path("id").equals(request.path("riskId"))) risk = (ObjectNode) item;
        if (risk == null || risk.hasNonNull("resolvedAt")) throw ApiException.of("STATE_CONFLICT", "Risk không còn mở.");
        if (risk.path("raisedBy").asString().equals(actor.id().toString())) throw ApiException.of("SOD_VIOLATION", "Người giải tỏa phải khác người nêu risk.");
        var ids = SigningEvidence.uniqueIds(request.path("evidenceIds"), "/evidenceIds");
        evidence.manifest(contract, ids, actor); historical.retain(contract.id(), ids);
        risk.put("resolutionBy", actor.id().toString()).put("resolvedAt", JsonSupport.timestamp(clock.instant()))
                .put("resolutionBasis", request.path("basis").asString());
        risk.set("resolutionEvidenceIds", request.path("evidenceIds").deepCopy());
        if (!unresolvedRisk(body)) {
            if (body.hasNonNull("readinessVerifiedAt")) requireReadyEvidence(contract);
            body.put("readiness", body.hasNonNull("readinessVerifiedAt") ? "READY" : "PENDING");
        }
    }

    public JsonNode project(JsonNode raw, Actor actor) {
        var body = (ObjectNode) raw.deepCopy();
        var actions = json.array(); var blocked = json.array();
        boolean live = List.of("PREPARED", "SCHEDULED", "ACTIVE").contains(body.path("state").asString());
        if (live && actor.hasRole("CHECKER")) actions.add("HOLD_READINESS");
        if (body.path("state").asString().equals("PREPARED") && actor.hasRole("MAKER")) {
            try {
                var contract = require(SigningEvidence.uuid(body, "id"), actor);
                customers.authorizeAssignedMaker(SigningEvidence.uuid(body, "relationshipId"), users.actor(actor.id()));
                if (!body.hasNonNull("approvedArtifactId")) throw ApiException.of("READINESS_BLOCKED", "Chưa có approved artifact.");
                if (!evidence.materialUnchanged(contract, false)) throw ApiException.of("READINESS_BLOCKED", "Material source đã thay đổi.");
                actions.add("SIGN");
            } catch (ApiException exception) {
                blocked.add(json.object().put("action", "SIGN").put("code", exception.code()).put("reason", exception.getMessage()));
            }
        }
        for (var action : List.of("CANCEL", "RELEASE_READINESS")) {
            if (action.equals("CANCEL") && !List.of("PREPARED", "SCHEDULED").contains(body.path("state").asString())) continue;
            if (action.equals("RELEASE_READINESS") && (!live || !unresolvedRisk(body))) continue;
            try {
                authorizeCommand(SigningEvidence.uuid(body, "id"), action, actor);
                if (action.equals("RELEASE_READINESS")) {
                    boolean otherRisk = false;
                    for (var risk : body.path("readinessRisks")) otherRisk |= !risk.hasNonNull("resolvedAt") && !risk.path("raisedBy").asString().equals(actor.id().toString());
                    if (!otherRisk) throw ApiException.of("SOD_VIOLATION", "Không được tự giải tỏa risk do mình nêu.");
                    if (!evidence.materialUnchanged(require(SigningEvidence.uuid(body, "id"), actor), false)) throw ApiException.of("READINESS_BLOCKED", "Material source đã đổi; cần hồ sơ mới.");
                }
                actions.add(action);
            }
            catch (ApiException exception) { if (!actor.hasRole("AUDITOR")) blocked.add(json.object().put("action", action).put("code", exception.code()).put("reason", "Cần actor độc lập, đúng vai trò và scope hiện hành.")); }
        }
        if (body.path("readiness").asString().equals("REVIEW_REQUIRED") && !actor.hasRole("AUDITOR")) {
            blocked.add(json.object().put("action", "SIGN").put("code", "READINESS_BLOCKED").put("reason", "Có risk hoặc material source thay đổi; cần đánh giá lại."));
        }
        if (actor.hasRole("AUDITOR") && !fullBusiness(body, actor) && !actor.hasPermission("SENSITIVE_READ")) {
            for (var check : body.path("mandatoryChecks")) ((ObjectNode) check).put("basis", "[REDACTED]");
            for (var risk : body.path("readinessRisks")) {
                ((ObjectNode) risk).put("reason", "[REDACTED]");
                if (risk.hasNonNull("resolutionBasis")) ((ObjectNode) risk).put("resolutionBasis", "[REDACTED]");
            }
        }
        body.set("allowedActions", actions); body.set("blockedActions", blocked); return body;
    }

    private boolean fullBusiness(JsonNode body, Actor actor) {
        if (actor.hasAnyRole("CHECKER", "MANAGER", "DATA_OFFICER")) return true;
        return actor.hasRole("MAKER") && store.require("Relationship", SigningEvidence.uuid(body, "relationshipId"))
                .body().path("assignedMakerId").asString().equals(actor.id().toString());
    }
    public JsonNode versions(UUID id, Map<String, String> query, Actor actor) {
        require(id, actor);
        return lists.contractVersions(id, actor, query);
    }

    public EntityStore.Entity save(EntityStore.Entity current, ObjectNode body, Actor actor, String action, String reason) {
        body.put("updatedAt", JsonSupport.timestamp(clock.instant()));
        var updated = store.update(current, body); schemas.validateSchema("Contract", updated.body());
        audit.record(actor, action, updated, reason); return updated;
    }

    public EntityStore.Entity terminateLocked(EntityStore.Entity current,EntityStore.Entity request,Instant completedAt,Actor actor){
        current=store.require("Contract",current.id());if(!current.body().path("state").asString().equals("ACTIVE"))throw ApiException.of("STATE_CONFLICT","Termination requires current ACTIVE.");var terms=current.body().path("currentVersion").path("terms");if(terms.path("termKind").asString().equals("FIXED")&&!completedAt.isBefore(calendar.startOfDay(LocalDate.parse(terms.path("endExclusive").asString()))))throw ApiException.of("STATE_CONFLICT","Actual agreement interval ended before completed confirmation.");var on=LocalDate.parse(request.body().path("effectOn").asString());var version=(ObjectNode)current.body().path("currentVersion").deepCopy();if(on.isBefore(LocalDate.parse(version.path("effectiveFrom").asString()))||on.isAfter(calendar.localDate(clock.instant())))throw ApiException.of("STATE_CONFLICT","Termination effectOn outside current authoritative interval.");version.put("effectiveTo",on.toString());store.jdbc().update("insert into cg_contract_version_closure(version_id,effective_to,recorded_at) values(?,?::date,?)",SigningEvidence.uuid(version,"id"),on.toString(),Timestamp.from(clock.instant()));effect(current.id(),"TERMINATED",calendar.startOfDay(on));var b=(ObjectNode)current.body().deepCopy();b.put("state","TERMINATED").put("serviceState","STOPPED").put("recordedAt",JsonSupport.timestamp(clock.instant()));b.set("currentVersion",version);obligations.closeout(current,version,request.body().path("contributorIds"),calendar.startOfDay(on));store.jdbc().update("update cg_job set state='CANCELLED',lease_owner=null,lease_until=null,error_code='GUARD_BLOCKED',updated_at=? where kind='EFFECTIVITY' and target_type='CONTRACT' and target_id=? and state in ('QUEUED','RUNNING','FAILED')",Timestamp.from(clock.instant()),current.id());return save(current,b,actor,"TERMINATE",request.body().path("reason").asString());
    }
    private void effect(UUID id, String kind, Instant effectiveAt) {
        store.jdbc().update("insert into cg_contract_effect(contract_id,kind,effective_at,recorded_at) values(?,?,?,?)",
                id, kind, Timestamp.from(effectiveAt), Timestamp.from(clock.instant()));
    }

    public static boolean unresolvedRisk(JsonNode body) {
        for (var risk : body.path("readinessRisks")) if (!risk.hasNonNull("resolvedAt")) return true;
        return false;
    }
}
