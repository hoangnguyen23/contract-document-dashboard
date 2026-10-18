package vn.contractgovernance.api.reporting;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;

@Service
public class AuditReportingService {
    private final JdbcTemplate jdbc; private final JsonSupport json; private final Clock clock;
    private final ContractRegistry contracts; private final ListQueries lists; private final AuditService audit;
    private final ReportingCursor cursors;
    public AuditReportingService(JdbcTemplate jdbc,JsonSupport json,Clock clock,ContractRegistry contracts,
            ListQueries lists,AuditService audit,ReportingCursor cursors) {
        this.jdbc=jdbc;this.json=json;this.clock=clock;this.contracts=contracts;this.lists=lists;this.audit=audit;this.cursors=cursors;
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public JsonNode list(Actor actor,Map<String,String> query) {
        var branches=ReportingScope.branches(actor,query); var asOf=clock.instant();
        var selected=predicate(actor,branches,query); var args=new ArrayList<Object>(selected.args());
        String signature=cursors.fingerprint(actor,query);
        var cursor=query.containsKey("cursor")?cursors.read(query.get("cursor"),signature):null;
        String where=selected.sql();
        if(cursor!=null) {
            where+=" and (a.occurred_at,a.id)<=(?,?) and (a.occurred_at,a.id)<(?,?)";
            args.add(Timestamp.from(Instant.parse(cursor.path("anchorTime").asString())));args.add(UUID.fromString(cursor.path("anchorId").asString()));
            args.add(Timestamp.from(Instant.parse(cursor.path("lastTime").asString())));args.add(UUID.fromString(cursor.path("lastId").asString()));
        }
        int limit=limit(query);var rows=rows(where,args,limit+1);boolean more=rows.size()>limit;
        var visible=rows.subList(0,Math.min(rows.size(),limit));var items=json.array();visible.forEach(row->items.add(row.body()));
        var result=json.object().put("asOf",JsonSupport.timestamp(asOf));result.set("items",items);
        result.put("timelineProcessingLagSeconds",lists.timelineLag(actor,
                "exists(select 1 from cg_audit a where a.resource_id=e.id and e.entity_type="+ReportingScope.entityType("a.resource_type")+" and "+selected.sql()+")",selected.args(),asOf));
        if(more) {
            var first=rows.getFirst();var last=visible.getLast();
            result.put("nextCursor",cursors.next(signature,cursor,first.time(),first.id(),last.time(),last.id(),asOf));
        }else result.putNull("nextCursor");
        contracts.validateSchema("AuditEventPage",result);return result;
    }
    /** Audited read: selection and successful export provenance share one repeatable SQL snapshot/transaction. */
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public byte[] export(Actor actor,Map<String,String> query) {
        if(!actor.hasPermission("EXPORT"))throw ApiException.of("EXPORT_FORBIDDEN","Xuất dữ liệu cần quyền EXPORT trong phạm vi hiện tại.");
        var branches=ReportingScope.branches(actor,query);
        if(branches.isEmpty())throw ApiException.of("ROLE_FORBIDDEN","Không có phạm vi chi nhánh để xuất dữ liệu.");
        var selected=predicate(actor,branches,query);var rows=rows(selected.sql(),selected.args(),1001);
        if(rows.size()>1000)throw ApiException.of("EXPORT_LIMIT","Có quá 1000 sự kiện. Hãy thu hẹp bộ lọc trước khi xuất.");
        StringBuilder csv=new StringBuilder("\ufeffoccurredAt,actorId,action,resourceType,resourceId,resourceVersion,branchId,traceId,reason\r\n");
        for(var row:rows) {
            var body=row.body();var columns=new ArrayList<String>();
            for(var field:List.of("occurredAt","actorId","action","resourceType","resourceId","resourceVersion","branchId","traceId","reason"))
                columns.add(csvCell(body.path(field).isNull()?"":body.path(field).asString()));
            csv.append(String.join(",",columns)).append("\r\n");
        }
        byte[] bytes=csv.toString().getBytes(StandardCharsets.UTF_8);
        // One opaque export provenance per requested authorized branch. Never record raw query/PII/CSV values.
        UUID exportId=UUID.randomUUID();
        for(var branch:branches)audit.record(actor,"EXPORT","EXPORT",exportId,1,branch,"Scoped redacted audit CSV; rows="+rows.size());
        return bytes;
    }
    private ReportingScope.Predicate predicate(Actor actor,List<UUID> branches,Map<String,String> query) {
        var scope=ReportingScope.audit(actor,branches,query.containsKey("branchId"));
        var where=new StringBuilder(scope.sql());var args=new ArrayList<Object>(scope.args());
        for(var key:List.of("createdFrom","createdTo"))if(query.containsKey(key)) {
            where.append(" and a.occurred_at ").append(key.equals("createdFrom")?">=":"<").append(" ?");
            args.add(Timestamp.from(Instant.parse(query.get(key))));
        }
        for(var key:List.of("resourceType","resourceId","actorId","action"))if(query.containsKey(key)) {
            where.append(" and ").append(key.equals("resourceType")?ReportingScope.wireType("a.resource_type"):"a."+switch(key){case "resourceId"->"resource_id";case "actorId"->"actor_id";default->"action";}).append("=?");
            args.add(key.endsWith("Id")?UUID.fromString(query.get(key)):query.get(key));
        }
        return new ReportingScope.Predicate(where.toString(),List.copyOf(args));
    }
    private List<Row> rows(String where,List<Object> filterArgs,int limit) {
        var args=new ArrayList<Object>(filterArgs);args.add(limit);
        return jdbc.query("select a.id,a.occurred_at,a.actor_id,a.action,"+ReportingScope.wireType("a.resource_type")+" as wire_type,a.resource_id,a.resource_version,a.branch_id,a.trace_id,a.reason,a.body::text from cg_audit a where "+where+" order by a.occurred_at desc,a.id desc limit ?",(rs,index)->{
            var body=json.object().put("id",rs.getObject("id",UUID.class).toString()).put("occurredAt",JsonSupport.timestamp(rs.getTimestamp("occurred_at").toInstant()))
                    .put("action",rs.getString("action")).put("resourceType",rs.getString("wire_type")).put("resourceId",rs.getObject("resource_id",UUID.class).toString())
                    .put("resourceVersion",rs.getInt("resource_version")).put("traceId",rs.getObject("trace_id",UUID.class).toString());
            var actorId=rs.getObject("actor_id",UUID.class);var branchId=rs.getObject("branch_id",UUID.class);
            if(actorId==null)body.putNull("actorId");else body.put("actorId",actorId.toString());
            if(branchId==null)body.putNull("branchId");else body.put("branchId",branchId.toString());
            // Reasons are user-authored and can contain sensitive input, regardless of the exporter's grants.
            if(rs.getString("reason")==null)body.putNull("reason");else body.put("reason","[REDACTED]");
            var stored=json.parse(rs.getString("body"));
            body.set("beforeHash",stored.path("beforeHash").isMissingNode()?json.value(null):stored.path("beforeHash"));
            body.set("afterHash",stored.path("afterHash").isMissingNode()?json.value(null):stored.path("afterHash"));
            contracts.validateSchema("AuditEvent",body);
            return new Row(rs.getObject("id",UUID.class),rs.getTimestamp("occurred_at").toInstant(),body);
        },args.toArray());
    }
    private static int limit(Map<String,String> query) {
        try {int value=Integer.parseInt(query.getOrDefault("limit","25"));if(value<1||value>100)throw new IllegalArgumentException();return value;}
        catch(Exception exception){throw ApiException.validation("/query/limit","limit phải từ 1 đến 100.");}
    }
    private static String csvCell(String value) {
        int at=0;while(at<value.length()&&(Character.isWhitespace(value.charAt(at))||Character.isSpaceChar(value.charAt(at))))at++;
        if(at<value.length()&&"=+-@".indexOf(value.charAt(at))>=0)value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }
    private record Row(UUID id,Instant time,JsonNode body) {}
}
