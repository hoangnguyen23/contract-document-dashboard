package vn.contractgovernance.api.shared;

import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

public record ApiResult(int status, JsonNode body, String etag, String location, boolean replayed) {
    public ApiResult(int status, JsonNode body, String etag, String location) { this(status, body, etag, location, false); }
    public static ApiResult ok(JsonNode body) { return new ApiResult(200, body, body.has("version") ? ETags.format(body.path("version").asInt()) : null, null); }
    public static ApiResult created(JsonNode body, String location) { return new ApiResult(201, body, body.has("version") ? ETags.format(body.path("version").asInt()) : null, location); }
    public static ApiResult empty() { return new ApiResult(204, null, null, null); }
    public ResponseEntity<JsonNode> response() {
        var builder = ResponseEntity.status(status);
        if (etag != null) builder.header("ETag", etag);
        if (location != null) builder.header("Location", location);
        if (replayed) builder.header("Idempotent-Replayed", "true");
        return body == null ? builder.build() : builder.body(body);
    }
}
