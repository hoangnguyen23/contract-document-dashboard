package vn.contractgovernance.api.reporting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

/** Audit has a different table/order from generic entity paging; retain the exact canonical cursor binding. */
@Component
public class ReportingCursor {
    private final JsonSupport json; private final Clock clock; private final byte[] secret;
    public ReportingCursor(JsonSupport json,Clock clock,@Value("${cg.cursor-secret}")String secret) {
        if(secret.length()<32)throw new IllegalArgumentException("CG_CURSOR_SECRET must contain at least 32 characters");
        this.json=json;this.clock=clock;this.secret=secret.getBytes(StandardCharsets.UTF_8);
    }
    public String fingerprint(Actor actor,Map<String,String> query) {
        var parameters=new TreeMap<>(query);parameters.remove("cursor");
        var content=json.object().put("operationId","listAuditEvents").put("order","occurred_at DESC,id DESC").put("actorId",actor.id().toString());
        content.set("roles",json.value(actor.roles().stream().sorted().toList()));
        content.set("permissions",json.value(actor.permissions().stream().sorted().toList()));
        content.set("branchIds",json.value(actor.branchIds().stream().map(UUID::toString).sorted().toList()));
        content.set("query",json.value(parameters));return json.hash(content);
    }
    public JsonNode read(String value,String fingerprint) {
        try {
            String[] parts=value.split("\\.",-1);if(parts.length!=2)throw new IllegalArgumentException();
            byte[] bytes=Base64.getUrlDecoder().decode(parts[0]);
            if(!MessageDigest.isEqual(sign(bytes),Base64.getUrlDecoder().decode(parts[1])))throw new IllegalArgumentException();
            var content=json.parse(new String(bytes,StandardCharsets.UTF_8));
            if(!content.path("fingerprint").asString().equals(fingerprint))throw new IllegalArgumentException();
            if(!clock.instant().isBefore(Instant.parse(content.path("expiresAt").asString())))throw ApiException.of("CURSOR_EXPIRED","Cursor đã hết hạn. Hãy tải lại danh sách.");
            // Validate all signed fields before any SQL receives them.
            Instant.parse(content.path("anchorTime").asString());Instant.parse(content.path("lastTime").asString());
            UUID.fromString(content.path("anchorId").asString());UUID.fromString(content.path("lastId").asString());
            return content;
        }catch(ApiException exception){throw exception;}catch(Exception exception){throw ApiException.validation("/query/cursor","Cursor không phù hợp truy vấn hoặc phạm vi hiện tại.");}
    }
    public String next(String fingerprint,JsonNode previous,Instant firstTime,UUID firstId,Instant lastTime,UUID lastId,Instant asOf) {
        var content=json.object().put("fingerprint",fingerprint)
                .put("anchorTime",previous==null?firstTime.toString():previous.path("anchorTime").asString())
                .put("anchorId",previous==null?firstId.toString():previous.path("anchorId").asString())
                .put("lastTime",lastTime.toString()).put("lastId",lastId.toString())
                .put("expiresAt",previous==null?asOf.plusSeconds(900).toString():previous.path("expiresAt").asString());
        byte[] bytes=json.canonical(content);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(sign(bytes));
    }
    private byte[] sign(byte[] bytes) {
        try {var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return mac.doFinal(bytes);}
        catch(Exception exception){throw new IllegalStateException(exception);}
    }
}
