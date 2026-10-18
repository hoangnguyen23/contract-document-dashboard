package vn.contractgovernance.api.signing;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.contract.OriginalAgreementService;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.approval.RevisionStore;
import vn.contractgovernance.api.identity.UserDirectory;
import vn.contractgovernance.api.shared.*;

/** Exact evidence and action-time authority. Human PASS records cover identity/action scope;
 * scopeText is documentary input, never an executable legal-rule language.
 */
@Service
public class SigningEvidence {
    private final EntityStore store;
    private final OriginalAgreementService originals;
    private final FileService files;
    private final EvidenceAccess access;
    private final ContractContributions contributions;
    private final RevisionStore revisions;
    private final UserDirectory users;
    private final JsonSupport json;
    private final Clock clock;
    private final CalendarService calendar;

    public SigningEvidence(EntityStore store, OriginalAgreementService originals, FileService files,
            EvidenceAccess access, ContractContributions contributions, RevisionStore revisions,
            UserDirectory users, JsonSupport json, Clock clock, CalendarService calendar) {
        this.store = store;
        this.originals = originals;
        this.files = files;
        this.access = access;
        this.contributions = contributions;
        this.revisions = revisions;
        this.users = users;
        this.json = json;
        this.clock = clock;
        this.calendar = calendar;
    }

    public Set<UUID> contributors(EntityStore.Entity contract) {
        var source = originals.source(contract.id()).body();
        var result = contributions.all(contract.id());
        source.path("revision").path("contributorIds").forEach(id -> result.add(UUID.fromString(id.asString())));
        result.add(revisions.require(uuid(contract.body(), "dossierId"),
                uuid(contract.body(), "approvedRevisionId")).submittedBy());
        // Include pre-migration actual uploaders as well as the cumulative ledger.
        store.jdbc().query("select body->>'uploadedBy' from cg_entity where entity_type='File' and body->>'targetKind'='CONTRACT' and body->>'targetId'=? and body->>'uploadedBy' is not null",
                (rs, row) -> UUID.fromString(rs.getString(1)), contract.id().toString()).forEach(result::add);
        return result;
    }

    public void independent(EntityStore.Entity contract, Actor actor) {
        var current = users.actor(actor.id());
        current.requireBranch(contract.branchId());
        current.requireRole("CHECKER");
        if (contributors(contract).contains(current.id())) {
            throw ApiException.of("SOD_VIOLATION", "Người đóng góp không được xác minh nguồn của mình.");
        }
    }

    public boolean materialUnchanged(EntityStore.Entity contract) { return materialUnchanged(contract, true); }
    public boolean materialUnchanged(EntityStore.Entity contract, boolean locked) {
        var source = originals.source(contract.id()).body();
        var relationship = locked ? store.lock("Relationship", uuid(contract.body(), "relationshipId"))
                : store.require("Relationship", uuid(contract.body(), "relationshipId"));
        var party = locked ? store.lock("Party", uuid(relationship.body(), "partyId"))
                : store.require("Party", uuid(relationship.body(), "partyId"));
        var frozen = source.path("metadata").path("party");
        for (var field : List.of("id", "name", "taxId")) {
            if (!json.nfc(party.body().path(field)).equals(json.nfc(frozen.path(field)))) return false;
        }
        var dossier = store.require("Dossier", uuid(contract.body(), "dossierId"));
        var revision = source.path("revision");
        for (var field : List.of("representation", "externalAccountReference", "terms")) {
            if (!json.nfc(dossier.body().path(field)).equals(json.nfc(revision.path(field)))) return false;
        }
        return relationship.body().path("active").asBoolean();
    }

    public void requireMaterial(EntityStore.Entity contract) {
        if (!materialUnchanged(contract)) {
            throw ApiException.of("READINESS_BLOCKED", "Identity, account hoặc representation đã đổi; cần hồ sơ liên kết mới.");
        }
    }

    public void authority(JsonNode packet) {
        for (var side : List.of("bank", "beneficiary")) {
            var representation = packet.path("authority").path(side);
            var signedAt = Instant.parse(packet.path(side + "SignedAt").asString());
            if (signedAt.isAfter(clock.instant())) {
                throw ApiException.validation("/" + side + "SignedAt", "Thời điểm ký không được ở tương lai.");
            }
            var from = calendar.startOfDay(LocalDate.parse(representation.path("validFrom").asString()));
            if (signedAt.isBefore(from)
                    || (representation.hasNonNull("validTo") && !signedAt.isBefore(calendar.startOfDay(
                            LocalDate.parse(representation.path("validTo").asString()))))
                    || (representation.hasNonNull("knownRevocationAt") && !signedAt.isBefore(
                            Instant.parse(representation.path("knownRevocationAt").asString())))) {
                throw ApiException.of("AUTHORITY_INVALID", "Thẩm quyền không bao phủ thời điểm ký của " + side + ".");
            }
            if (representation.hasNonNull("checkedAt")) nonfuture(representation.path("checkedAt"));
        }
    }

    public Set<UUID> packetFiles(JsonNode packet) {
        var ids = new TreeSet<UUID>();
        ids.add(uuid(packet, "artifactId"));
        ids.addAll(uniqueIds(packet.path("signedCopyIds"), "/signedCopyIds"));
        for (var side : List.of("bank", "beneficiary")) {
            var representation = packet.path("authority").path(side);
            ids.add(uuid(representation, "sourceFileId"));
            if (representation.hasNonNull("checkEvidenceFileId")) ids.add(uuid(representation, "checkEvidenceFileId"));
        }
        return ids;
    }

    public JsonNode manifest(EntityStore.Entity contract, Collection<UUID> ids, Actor actor) {
        var result = json.array();
        var source = originals.source(contract.id()).body();
        var ancestorIds = new HashSet<UUID>();
        source.path("metadata").path("evidence").forEach(item -> ancestorIds.add(uuid(item, "fileId")));
        for (var id : new TreeSet<>(ids)) {
            var file = store.lock("File", id);
            var body = file.body();
            boolean target = body.path("targetKind").asString().equals("CONTRACT")
                    && body.path("targetId").asString().equals(contract.id().toString());
            boolean ancestor = body.path("targetKind").asString().equals("DOSSIER")
                    && body.path("targetId").asString().equals(contract.body().path("dossierId").asString())
                    && ancestorIds.contains(id);
            if ((!target && !ancestor) || !Objects.equals(file.branchId(), contract.branchId())) {
                throw ApiException.of("NOT_FOUND", "Bằng chứng không thuộc đúng nguồn hợp đồng.");
            }
            if (!body.path("state").asString().equals("CLEAN")) {
                throw ApiException.of("FILE_NOT_CLEAN", "Bằng chứng chưa CLEAN.");
            }
            if (actor != null) {
                access.requireFile(id, actor);
                access.requireFull(file, actor);
            }
            var hash = body.path("sha256").asString();
            if (ancestor) {
                boolean frozenMatch = false;
                for (var item : source.path("metadata").path("evidence"))
                    frozenMatch |= item.path("fileId").asString().equals(id.toString()) && item.path("sha256").asString().equals(hash);
                if (!frozenMatch) throw ApiException.of("STORAGE_INTEGRITY", "Bằng chứng thẩm quyền khác hash Revision.");
            }
            if (!hash.equals(files.blob(id).hash())) throw ApiException.of("STORAGE_INTEGRITY", "Hash metadata không khớp.");
            files.bytesForScan(id, hash);
            result.add(json.object().put("fileId", id.toString()).put("sha256", hash));
        }
        return result;
    }

    public void artifactAndCopies(EntityStore.Entity contract, JsonNode packet, Actor actor) {
        if (!contract.body().hasNonNull("approvedArtifactId")) throw ApiException.of("READINESS_BLOCKED", "Chưa có artifact đã quét.");
        if (!contract.body().path("approvedArtifactId").equals(packet.path("artifactId"))
                || !contract.body().path("approvedArtifactHash").equals(packet.path("artifactHash"))) {
            throw ApiException.of("STORAGE_INTEGRITY", "Artifact không khớp nguồn đã duyệt.");
        }
        var artifact = store.require("File", uuid(packet, "artifactId"));
        if (!artifact.body().path("sha256").equals(packet.path("artifactHash"))
                || store.jdbc().queryForObject("select count(*) from cg_agreement_artifact where contract_id=? and file_id=? and artifact_hash=? and source_hash=?",
                    Long.class, contract.id(), artifact.id(), packet.path("artifactHash").asString(), originals.source(contract.id()).hash()) != 1L) {
            throw ApiException.of("STORAGE_INTEGRITY", "Artifact bytes/binding không khớp nguồn hợp đồng.");
        }
        for (var id : uniqueIds(packet.path("signedCopyIds"), "/signedCopyIds")) {
            var file = store.require("File", id);
            if (id.equals(uuid(packet, "artifactId")) || !file.body().path("targetKind").asString().equals("CONTRACT")
                    || !file.body().path("targetId").asString().equals(contract.id().toString())
                    || !file.body().path("origin").asString().equals("UPLOADED")) {
                throw ApiException.validation("/signedCopyIds", "Bản ký phải là file tải lên trên đúng hợp đồng.");
            }
        }
        manifest(contract, packetFiles(packet), actor);
    }

    public void assessment(EntityStore.Entity contract, JsonNode packet, JsonNode checks,
            JsonNode mandatory, Actor actor) {
        authority(packet);
        artifactAndCopies(contract, packet, actor);
        var checked = new HashSet<UUID>();
        for (var check : checks) {
            var id = uuid(check, "fileId");
            if (!checked.add(id)) throw ApiException.validation("/checks", "File kiểm tra không được trùng.");
            nonfuture(check.path("checkedAt"));
            if (!check.path("result").asString().equals("PASS")) {
                throw ApiException.of("AUTHORITY_INVALID", "Bản ký/artifact/thẩm quyền còn kết quả chưa PASS; trả bổ sung packet.");
            }
        }
        if (!checked.containsAll(uniqueIds(packet.path("signedCopyIds"), "/signedCopyIds"))) {
            throw ApiException.of("AUTHORITY_INVALID", "Cần human PASS đối chiếu mọi bản ký với exact artifact.");
        }
        // Other assessed evidence must follow the same exact parent/ancestor and bytes rules.
        manifest(contract, checked, actor);
        var codes = new HashSet<String>();
        var authorityEvidence = new HashSet<UUID>();
        for (var condition : mandatory) {
            var code = condition.path("code").asString();
            if (!codes.add(code)) throw ApiException.validation("/mandatoryChecks", "Mã điều kiện không được trùng.");
            nonfuture(condition.path("checkedAt"));
            if (!condition.path("result").asString().equals("PASS")) throw conditionFailure(code);
            var ids = uniqueIds(condition.path("evidenceFileIds"), "/mandatoryChecks/evidenceFileIds");
            if (ids.isEmpty()) throw conditionFailure(code);
            manifest(contract, ids, actor);
            if (code.equals("AUTHORITY_CURRENT")) authorityEvidence.addAll(ids);
        }
        for (var side : List.of("bank", "beneficiary")) {
            var representation = packet.path("authority").path(side);
            if (!authorityEvidence.contains(uuid(representation, "sourceFileId"))
                    || (representation.hasNonNull("checkEvidenceFileId") && !authorityEvidence.contains(uuid(representation, "checkEvidenceFileId")))) {
                throw conditionFailure("AUTHORITY_CURRENT");
            }
        }
        var frozenRepresentation = originals.source(contract.id()).body().path("revision").path("representation");
        var beneficiary = packet.path("authority").path("beneficiary");
        for (var field : List.of("basisType", "personName", "title", "scopeText", "validFrom", "validTo", "sourceFileId", "issuerReference", "knownRevocationAt")) {
            if (!json.nfc(beneficiary.path(field)).equals(json.nfc(frozenRepresentation.path(field))))
                throw ApiException.of("AUTHORITY_INVALID", "Thẩm quyền bên thụ hưởng khác nguồn đã duyệt; cần hồ sơ mới.");
        }
        for (var previous : contract.body().path("mandatoryChecks")) {
            if (!codes.contains(previous.path("code").asString())) throw conditionFailure(previous.path("code").asString());
        }
        // External account/service confirmation is mandatory at readiness, unlike optional legal conditions.
        if (!codes.contains("SERVICE_READINESS")) throw conditionFailure("SERVICE_READINESS");
        requireMaterial(contract);
    }

    public void nonfuture(JsonNode value) {
        if (Instant.parse(value.asString()).isAfter(clock.instant())) {
            throw ApiException.validation("/checkedAt", "Thời điểm kiểm tra không được ở tương lai.");
        }
    }

    public static ApiException conditionFailure(String code) {
        return ApiException.of(switch (code) {
            case "AUTHORITY_CURRENT" -> "AUTHORITY_INVALID";
            case "LEGAL_CONDITION" -> "LEGAL_REVIEW_REQUIRED";
            default -> "READINESS_BLOCKED";
        }, "Điều kiện hiện hành phải có đánh giá PASS và chứng cứ thực.");
    }

    public static UUID uuid(JsonNode node, String field) { return UUID.fromString(node.path(field).asString()); }
    public static Set<UUID> uniqueIds(JsonNode values, String path) {
        var result = new LinkedHashSet<UUID>();
        for (var value : values) if (!result.add(UUID.fromString(value.asString()))) {
            throw ApiException.validation(path, "ID không được trùng.");
        }
        return result;
    }
}
