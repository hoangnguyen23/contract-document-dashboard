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
        for(String key:List.of("state","verificationStatus","serviceState","readiness","relationshipId","assignedMakerId","partyId","contractId","targetKind","targetId","purpose","parentKind","parentId","kind","requestType","decision","taxId")){
            if(query.containsKey(key)){where.append(" and e.body->>'").append(key).append("'=?");args.add(query.get(key));}
        }
        if(query.containsKey("active")){where.append(" and (e.body->>'active')::boolean=?");args.add(Boolean.parseBoolean(query.get("active")));}
        if(query.containsKey("role")){where.append(" and e.body->'roles' @> ?::jsonb");args.add(json.stringify(json.array().add(query.get("role"))));}
        if(query.containsKey("name")){where.append(" and lower(normalize(e.body->>'name',NFC)) like ? escape '\\'");String value=Normalizer.normalize(query.get("name"),Normalizer.Form.NFC).toLowerCase(Locale.ROOT);args.add("%"+value.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%");}
        for(String key:List.of("createdFrom","createdTo"))if(query.containsKey(key)){where.append(" and e.created_at ").append(key.equals("createdFrom")?">=":"<").append(" ?");args.add(timestamp(query.get(key),key));}
        if(query.containsKey("dueBefore")){where.append(" and (e.body->>'dueAt')::timestamptz<?");args.add(timestamp(query.get("dueBefore"),"dueBefore"));}
        // Lag concerns the complete filtered collection, not just the current cursor window.
        String lagWhere=where.toString(); var lagArgs=new ArrayList<>(args);
        if(cursor!=null){where.append(" and (e.created_at,e.id)<=(?,?) and (e.created_at,e.id)<(?,?)");args.add(timestamp(cursor.path("anchorTime").asString(),"cursor"));args.add(UUID.fromString(cursor.path("anchorId").asString()));args.add(timestamp(cursor.path("lastTime").asString(),"cursor"));args.add(UUID.fromString(cursor.path("lastId").asString()));}
        args.add(limit+1);
        var rows=jdbc.query("select e.body::text,e.created_at,e.id from cg_entity e where "+where+" order by e.created_at desc,e.id desc limit ?",(rs,row)->new Row(json.parse(rs.getString(1)),rs.getTimestamp(2).toInstant(),rs.getObject(3,UUID.class)),args.toArray());
        boolean more=rows.size()>limit;var visible=rows.subList(0,Math.min(limit,rows.size()));var items=json.array();visible.forEach(row->items.add(projection.apply(row.body())));
        var response=json.object();response.set("items",items);response.put("asOf",JsonSupport.timestamp(clock.instant()));response.put("timelineProcessingLagSeconds",Set.of("User","Branch","Party","Policy").contains(type)?0:timelineLag(actor,lagWhere,lagArgs));
        if(more){Row anchor=rows.getFirst(),last=visible.getLast();var next=json.object();next.put("fingerprint",signature);next.put("anchorTime",cursor==null?anchor.time().toString():cursor.path("anchorTime").asString());next.put("anchorId",cursor==null?anchor.id().toString():cursor.path("anchorId").asString());next.put("lastTime",last.time().toString());next.put("lastId",last.id().toString());next.put("expiresAt",cursor==null?clock.instant().plusSeconds(900).toString():cursor.path("expiresAt").asString());response.put("nextCursor",encode(next));}else response.putNull("nextCursor");
        return response;
    }
    public JsonNode page(String type,Actor actor,Map<String,String> query,boolean globalNamespace,String extraWhere,List<Object> extraArgs){return page(type,actor,query,globalNamespace,extraWhere,extraArgs,value->value);}
    /** ContractVersion has immutable number order, not mutable aggregate createdAt order. */
    @Transactional(readOnly=true)
    public JsonNode contractVersions(UUID contractId, Actor actor, Map<String,String> query) {
        int limit;
        try { limit=Integer.parseInt(query.getOrDefault("limit","25")); }
        catch(NumberFormatException exception) { throw ApiException.validation("/query/limit","limit không hợp lệ."); }
        if(limit<1 || limit>100) throw ApiException.validation("/query/limit","limit phải từ 1 đến 100.");
        var signature=fingerprint("ContractVersion",actor,query,false,"contract_id",List.of(contractId));
        var cursor=query.containsKey("cursor")?decode(query.get("cursor"),signature):null;
        var args=new ArrayList<Object>(); args.add(contractId);
        String where="contract_id=?";
        if(cursor!=null) {
            where+=" and (number,id)<=(?,?) and (number,id)<(?,?)";
            args.add(cursor.path("anchorNumber").asInt()); args.add(UUID.fromString(cursor.path("anchorId").asString()));
            args.add(cursor.path("lastNumber").asInt()); args.add(UUID.fromString(cursor.path("lastId").asString()));
        }
        args.add(limit+1);
        var rows=jdbc.query("select (case when i.version_id is null then v.body else jsonb_set(v.body,'{effectiveTo}',to_jsonb(i.effective_to::text)) end)::text from cg_contract_version v left join cg_contract_version_closure i on i.version_id=v.id where "+where+" order by v.number desc,v.id desc limit ?",
                (rs,row)->json.parse(rs.getString(1)),args.toArray());
        boolean more=rows.size()>limit;
        var visible=rows.subList(0,Math.min(limit,rows.size()));
        var response=json.object(); var items=json.array(); visible.forEach(items::add);
        response.set("items",items); response.put("asOf",JsonSupport.timestamp(clock.instant()));
        response.put("timelineProcessingLagSeconds",timelineLag(actor,"e.entity_type='Contract' and e.id=?",List.of(contractId)));
        if(more) {
            var next=json.object().put("fingerprint",signature);
            next.put("anchorNumber",cursor==null?rows.getFirst().path("number").asInt():cursor.path("anchorNumber").asInt());
            next.put("anchorId",cursor==null?rows.getFirst().path("id").asString():cursor.path("anchorId").asString());
            next.put("lastNumber",visible.getLast().path("number").asInt()); next.put("lastId",visible.getLast().path("id").asString());
            next.put("expiresAt",cursor==null?clock.instant().plusSeconds(900).toString():cursor.path("expiresAt").asString());
            response.put("nextCursor",encode(next));
        } else response.putNull("nextCursor");
        return response;
    }
    /**
     * Measures only contracts reachable from the authorized, filtered entity collection.
     * selectedWhere is trusted server SQL against alias e; every data value is bound.
     * The additional current branch/assignment check cannot be removed by callers.
     * Administration entities have no contract ancestry and therefore expose zero lag.
     */
    public int timelineLag(Actor actor,String selectedWhere,List<Object> selectedArgs) {
        var args=new ArrayList<Object>(selectedArgs);
        args.add("{"+String.join(",",actor.branchIds().stream().map(UUID::toString).sorted().toList())+"}");
        args.add(actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR"));
        args.add(actor.hasRole("MAKER")); args.add(actor.id().toString());
        var now=Timestamp.from(clock.instant()); args.add(now); args.add(now);
        String sql="""
            with recursive selected as (
              select e.entity_type,e.id,e.branch_id,e.body,0 as depth
              from cg_entity e where %s
              union all
              select p.entity_type,p.id,p.branch_id,p.body,s.depth+1
              from selected s join cg_entity p on p.id=case
                when s.entity_type in ('Round','SigningPacket','File') then (s.body->>'targetId')::uuid
                when s.entity_type='Job' then (s.body->>'parentId')::uuid
                when s.body ? 'contractId' then (s.body->>'contractId')::uuid
                else null end
              and p.entity_type=case
                when s.entity_type in ('Round','SigningPacket','File','Job') then
                  case coalesce(s.body->>'targetKind',s.body->>'parentKind')
                    when 'CONTRACT' then 'Contract' when 'CHANGE' then 'Change'
                    when 'DOSSIER' then 'Dossier' when 'FILE' then 'File'
                    when 'NOTICE' then 'Notice' when 'OBLIGATION' then 'Obligation'
                    when 'CONTROL_REQUEST' then 'ControlRequest' when 'INCIDENT' then 'Incident'
                    when 'DATA_REQUEST' then 'DataRequest' else null end
                when s.body ? 'contractId' then 'Contract' else null end
              where s.depth<6 and p.branch_id=s.branch_id
            ), scoped_contracts as (
              select c.id from cg_entity c
              join cg_entity r on r.entity_type='Relationship'
                and r.id=(c.body->>'relationshipId')::uuid and r.branch_id=c.branch_id
              where c.entity_type='Contract' and c.branch_id=any(?::uuid[])
                and (?::boolean or (?::boolean and r.body->>'assignedMakerId'=?))
                and exists(select 1 from selected s where s.branch_id=c.branch_id and
                  ((s.entity_type='Contract' and s.id=c.id)
                    or (s.entity_type='Dossier' and s.id=(c.body->>'dossierId')::uuid)
                    or (s.entity_type='Relationship' and s.id=r.id)))
            )
            select greatest(0,coalesce(extract(epoch from (?::timestamptz-min(j.available_at))),0))
            from cg_job j where j.kind='EFFECTIVITY' and j.state in ('QUEUED','RUNNING')
              and j.available_at<=? and exists(select 1 from scoped_contracts c where
                (j.target_type='CONTRACT' and j.target_id=c.id) or
                (j.target_type='CHANGE' and exists(select 1 from cg_entity ch
                  where ch.entity_type='Change' and ch.id=j.target_id
                    and ch.body->>'contractId'=c.id::text)))
            """.formatted(selectedWhere);
        Double value=jdbc.queryForObject(sql,Double.class,args.toArray());
        return value==null?0:(int)Math.min(Integer.MAX_VALUE,Math.ceil(value));
    }
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
