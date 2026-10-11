package vn.contractgovernance.api.shared;

import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.HexFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JsonSupport {
    private final JsonMapper mapper;
    public JsonSupport(JsonMapper mapper) { this.mapper = mapper; }
    public JsonNode parse(String value) { try { return mapper.readTree(value); } catch (Exception exception) { throw ApiException.validation("", "JSON không hợp lệ."); } }
    public String stringify(JsonNode value) { return mapper.writeValueAsString(value); }
    public ObjectNode object() { return mapper.createObjectNode(); }
    public ArrayNode array() { return mapper.createArrayNode(); }
    public JsonNode value(Object value) { return mapper.valueToTree(value); }
    public byte[] canonical(JsonNode value) { try { return new JsonCanonicalizer(stringify(value)).getEncodedUTF8(); } catch (Exception exception) { throw new IllegalArgumentException("Cannot canonicalize JSON", exception); } }
    public String hash(JsonNode value) { return sha256(canonical(nfc(value))); }
    public static String sha256(byte[] value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); } catch (Exception exception) { throw new IllegalStateException(exception); } }
    public static String timestamp(Instant value) { return new java.time.format.DateTimeFormatterBuilder().appendInstant(3).toFormatter().format(value.truncatedTo(ChronoUnit.MILLIS)); }
    public JsonNode nfc(JsonNode source) {
        if (source.isTextual()) return mapper.getNodeFactory().stringNode(Normalizer.normalize(source.asString(), Normalizer.Form.NFC));
        if (source.isArray()) { var target = array(); source.forEach(value -> target.add(nfc(value))); return target; }
        if (source.isObject()) { var target = object(); source.properties().forEach(entry -> target.set(Normalizer.normalize(entry.getKey(), Normalizer.Form.NFC), nfc(entry.getValue()))); return target; }
        return source.deepCopy();
    }
}
