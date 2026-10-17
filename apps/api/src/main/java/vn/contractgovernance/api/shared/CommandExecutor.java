package vn.contractgovernance.api.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class CommandExecutor {
    private final JdbcTemplate jdbc;
    private final JsonSupport json;
    private final Clock clock;
    private final TransactionTemplate transactions;
    private final ContractRegistry contracts;
    public CommandExecutor(JdbcTemplate jdbc, JsonSupport json, Clock clock, PlatformTransactionManager manager, ContractRegistry contracts) {
        this.jdbc=jdbc; this.json=json; this.clock=clock; this.transactions=new TransactionTemplate(manager); this.contracts=contracts;
    }
    /** Authorization must use current scopes before this method. The overload rechecks it in transaction. */
    public ApiResult execute(Actor actor, String key, String method, String path, JsonNode body, String ifMatch, Supplier<ApiResult> operation) {
        return execute(actor,key,method,path,body,ifMatch,()->{},operation);
    }
    public ApiResult execute(Actor actor, String key, String method, String path, JsonNode body, String ifMatch, Runnable authorization, Supplier<ApiResult> operation) {
        return execute(actor,key,method,path,body,ifMatch,authorization,()->{},operation);
    }
    /** Maintenance runs only for a cache miss, while the original key arbitration is retained.
     * The hook uses its own REQUIRES_NEW domain transaction before this new mutation checks ETag.
     * Authorization must not hold domain row locks across the hook.
     */
    public ApiResult execute(Actor actor, String key, String method, String path, JsonNode body, String ifMatch,
            Runnable authorization, Runnable afterCacheMiss, Supplier<ApiResult> operation) {
        UUID parsedKey = requireKey(key);
        String fingerprint = fingerprint(method,path,body,ifMatch);
        var contractOperation=contracts.match(method.toUpperCase(java.util.Locale.ROOT),java.net.URI.create(path).normalize().getPath()).orElseThrow(()->new IllegalStateException("Command operation is absent from the frozen contract"));
        return transactions.execute(transaction -> {
            authorization.run();
            // A try-lock reports in-progress without waiting on another process's transaction.
            String lockKey=actor.id()+":"+parsedKey;
            boolean acquired=Boolean.TRUE.equals(jdbc.queryForObject("select pg_try_advisory_xact_lock(hashtextextended(?,0))", Boolean.class,lockKey));
            if (!acquired) throw ApiException.of("IDEMPOTENCY_IN_PROGRESS","Thao tác đang được xử lý.");
            jdbc.update("delete from cg_idempotency where actor_id=? and idempotency_key=? and expires_at<=?",actor.id(),parsedKey,Timestamp.from(clock.instant()));
            var cached=jdbc.query("select fingerprint,status,body::text,etag,location from cg_idempotency where actor_id=? and idempotency_key=?",(rs,row)->new Cached(rs.getString(1),new ApiResult(rs.getInt(2),rs.getString(3)==null?null:json.parse(rs.getString(3)),rs.getString(4),rs.getString(5),true)),actor.id(),parsedKey);
            if (!cached.isEmpty()) {
                if (!cached.getFirst().fingerprint().equals(fingerprint)) throw ApiException.of("IDEMPOTENCY_CONFLICT","Idempotency-Key đã dùng cho nội dung khác.");
                contracts.validateResponse(contractOperation,cached.getFirst().result().status(),cached.getFirst().result().body());
                return cached.getFirst().result();
            }
            afterCacheMiss.run();
            authorization.run();
            ApiResult result=operation.get();
            boolean cache = result.status()>=200 && result.status()<300;
            if (result.status()==409 && result.body()!=null) cache=java.util.Set.of("BASE_CONFLICT","SCHEDULE_CONFLICT").contains(result.body().path("code").asString());
            if (!cache) throw new IllegalStateException("Command callback must throw prevalidation errors, only explicit committed conflicts may return 4xx");
            // A provider defect rolls back state/audit/intents; never cache success before shape validation.
            contracts.validateResponse(contractOperation,result.status(),result.body());
            jdbc.update("insert into cg_idempotency(actor_id,idempotency_key,fingerprint,status,body,etag,location,created_at,expires_at) values(?,?,?,?,?::jsonb,?,?,?,?)",actor.id(),parsedKey,fingerprint,result.status(),result.body()==null?null:json.stringify(result.body()),result.etag(),result.location(),Timestamp.from(clock.instant()),Timestamp.from(clock.instant().plus(Duration.ofHours(24))));
            return result;
        });
    }
    public String fingerprint(String method,String path,JsonNode body,String ifMatch) {
        String normalized=java.net.URI.create(path).normalize().getPath();
        String content=body==null?"null":new String(json.canonical(body),StandardCharsets.UTF_8);
        return JsonSupport.sha256((method.toUpperCase(java.util.Locale.ROOT)+"\n"+normalized+"\n"+content+"\n"+(ifMatch==null?"":ifMatch)).getBytes(StandardCharsets.UTF_8));
    }
    public static UUID requireKey(String key) {
        if (key==null || !key.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) throw ApiException.validation("/headers/Idempotency-Key","Thiếu hoặc sai UUIDv4 Idempotency-Key.");
        return UUID.fromString(key);
    }
    private record Cached(String fingerprint,ApiResult result) {}
}
