package vn.contractgovernance.api.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.text.Normalizer;
import java.util.*;
import java.util.function.UnaryOperator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class ListQueries {
    private final JdbcTemplate jdbc;private final JsonSupport json;private final Clock clock;private final byte[] secret;
    public ListQueries(JdbcTemplate jdbc,JsonSupport json,Clock clock,@Value("${cg.cursor-secret}") String secret){
        this.jdbc=jdbc;this.json=json;this.clock=clock;
        if(secret.length()<32)throw new IllegalArgumentException("CG_CURSOR_SECRET must contain at least 32 characters");this.secret=secret.getBytes(StandardCharsets.UTF_8);
    }
    /** extraWhere is developer-owned SQL against alias e. Every data value must be bound via extraArgs. */
    @Transactional(readOnly=true)
    public JsonNode page(String type,Actor actor,Map<String,String> query,boolean globalNamespace,String extraWhere,List<Object> extraArgs,UnaryOperator<JsonNode> projection){
        int limit;try{limit=Integer.parseInt(query.getOrDefault("limit","25"));}catch(NumberFormatException exception){throw ApiException.validation("/query/limit","limit không hợp lệ.");}
        if(limit<1||limit>100)throw ApiException.validation("/query/limit","limit phải từ 1 đến 100.");
        String signature=fingerprint(type,actor,query,globalNamespace,extraWhere,extraArgs);
        JsonNode cursor=query.containsKey("cursor")?decode(query.get("cursor"),signature):null;
        StringBuilder where=new StringBuilder("e.entity_type=?");List<Object> args=new ArrayList<>();args.add(type);
        if(!globalNamespace){where.append(" and e.branch_id=any(?::uuid[])");args.add("{"+String.join(",",actor.branchIds().stream().map(UUID::toString).sorted().toList())+"}");}
        if(extraWhere!=null&&!extraWhere.isBlank()){where.append(" and (").append(extraWhere).append(')');args.addAll(extraArgs);}
        if(query.containsKey("branchId")){UUID branch=uuid(query.get("branchId"),"branchId");if(!globalNamespace)actor.requireBranch(branch);where.append(" and e.branch_id=?");args.add(branch);}
        for(String key:List.of("state","serviceState","readiness","relationshipId","assignedMakerId","partyId","contractId","targetKind","targetId","purpose","parentKind","parentId","kind","requestType","decision","taxId")){
            if(query.containsKey(key)){where.append(" and e.body->>'").append(key).append("'=?");args.add(query.get(key));}
        }
        if(query.containsKey("active")){where.append(" and (e.body->>'active')::boolean=?");args.add(Boolean.parseBoolean(query.get("active")));}
        if(query.containsKey("role")){where.append(" and e.body->'roles' @> ?::jsonb");args.add(json.stringify(json.array().add(query.get("role"))));}
        if(query.containsKey("name")){where.append(" and lower(normalize(e.body->>'name',NFC)) like ? escape '\\'");String value=Normalizer.normalize(query.get("name"),Normalizer.Form.NFC).toLowerCase(Locale.ROOT);args.add("%"+value.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%");}
        for(String key:List.of("createdFrom","createdTo"))if(query.containsKey(key)){where.append(" and e.created_at ").append(key.equals("createdFrom")?">=":"<").append(" ?");args.add(timestamp(query.get(key),key));}
        if(query.containsKey("dueBefore")){where.append(" and (e.body->>'dueAt')::timestamptz<?");args.add(timestamp(query.get("dueBefore"),"dueBefore"));}
        if(cursor!=null){where.append(" and (e.created_at,e.id)<=(?,?) and (e.created_at,e.id)<(?,?)");args.add(timestamp(cursor.path("anchorTime").asString(),"cursor"));args.add(UUID.fromString(cursor.path("anchorId").asString()));args.add(timestamp(cursor.path("lastTime").asString(),"cursor"));args.add(UUID.fromString(cursor.path("lastId").asString()));}
        args.add(limit+1);
        var rows=jdbc.query("select e.body::text,e.created_at,e.id from cg_entity e where "+where+" order by e.created_at desc,e.id desc limit ?",(rs,row)->new Row(json.parse(rs.getString(1)),rs.getTimestamp(2).toInstant(),rs.getObject(3,UUID.class)),args.toArray());
        boolean more=rows.size()>limit;var visible=rows.subList(0,Math.min(limit,rows.size()));var items=json.array();visible.forEach(row->items.add(projection.apply(row.body())));
        var response=json.object();response.set("items",items);response.put("asOf",JsonSupport.timestamp(clock.instant()));response.put("timelineProcessingLagSeconds",timelineLag());
        if(more){Row anchor=rows.getFirst(),last=visible.getLast();var next=json.object();next.put("fingerprint",signature);next.put("anchorTime",cursor==null?anchor.time().toString():cursor.path("anchorTime").asString());next.put("anchorId",cursor==null?anchor.id().toString():cursor.path("anchorId").asString());next.put("lastTime",last.time().toString());next.put("lastId",last.id().toString());next.put("expiresAt",cursor==null?clock.instant().plusSeconds(900).toString():cursor.path("expiresAt").asString());response.put("nextCursor",encode(next));}else response.putNull("nextCursor");
        return response;
    }
    public JsonNode page(String type,Actor actor,Map<String,String> query,boolean globalNamespace,String extraWhere,List<Object> extraArgs){return page(type,actor,query,globalNamespace,extraWhere,extraArgs,value->value);}
    private int timelineLag(){Double value=jdbc.queryForObject("select greatest(0,coalesce(extract(epoch from (?::timestamptz-min(available_at))),0)) from cg_job where kind='EFFECTIVITY' and state in ('QUEUED','RUNNING') and available_at<=?",Double.class,Timestamp.from(clock.instant()),Timestamp.from(clock.instant()));return value==null?0:(int)Math.min(Integer.MAX_VALUE,Math.ceil(value));}
    private String fingerprint(String type,Actor actor,Map<String,String> query,boolean global,String extraWhere,List<Object> extraArgs){var values=new TreeMap<>(query);values.remove("cursor");var body=json.object();body.put("type",type);body.put("actorId",actor.id().toString());body.set("roles",json.value(actor.roles().stream().sorted().toList()));body.set("scope",json.value(actor.branchIds().stream().map(UUID::toString).sorted().toList()));body.set("permissions",json.value(actor.permissions().stream().sorted().toList()));body.set("query",json.value(values));body.put("global",global);body.put("extraWhere",extraWhere==null?"":extraWhere);body.set("extraArgs",json.value(extraArgs));return json.hash(body);}
    private JsonNode decode(String value,String fingerprint){
        try{String[] parts=value.split("\\.",-1);if(parts.length!=2)throw new IllegalArgumentException();byte[] bytes=Base64.getUrlDecoder().decode(parts[0]),signature=Base64.getUrlDecoder().decode(parts[1]);if(!MessageDigest.isEqual(signature,sign(bytes)))throw new IllegalArgumentException();var content=json.parse(new String(bytes,StandardCharsets.UTF_8));if(!content.path("fingerprint").asString().equals(fingerprint))throw new IllegalArgumentException();if(!clock.instant().isBefore(Instant.parse(content.path("expiresAt").asString())))throw ApiException.of("CURSOR_EXPIRED","Cursor đã hết hạn. Hãy tải lại danh sách.");return content;}catch(ApiException exception){throw exception;}catch(Exception exception){throw ApiException.validation("/query/cursor","Cursor không phù hợp truy vấn hoặc phạm vi hiện tại.");}
    }
    private String encode(JsonNode value){byte[] bytes=json.canonical(value);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(sign(bytes));}
    private byte[] sign(byte[] bytes){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return mac.doFinal(bytes);}catch(Exception exception){throw new IllegalStateException(exception);}}
    private static UUID uuid(String value,String key){try{return UUID.fromString(value);}catch(Exception exception){throw ApiException.validation("/query/"+key,"UUID không hợp lệ.");}}
    private static Timestamp timestamp(String value,String key){try{return Timestamp.from(Instant.parse(value));}catch(Exception exception){throw ApiException.validation("/query/"+key,"Thời điểm không hợp lệ.");}}
    private record Row(JsonNode body,Instant time,UUID id){}
}
