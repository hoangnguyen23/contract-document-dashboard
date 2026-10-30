package vn.contractgovernance.api.shared;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.format.EmailFormat;
import com.networknt.schema.path.PathType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Canonical contract is loaded directly, with no parallel hand-authored DTO vocabulary. */
@Component
public class ContractRegistry {
    public record Operation(String id,String method,String path,JsonNode definition,Pattern pattern,List<String> variables) {}
    private final JsonSupport json;
    private final JsonNode openapi;
    private final JsonNode protocol;
    private final JsonNode seed;
    private final List<Operation> operations=new ArrayList<>();
    private final Map<String,Schema> validators=new ConcurrentHashMap<>();
    private final SchemaRegistry schemas=SchemaRegistry.withDefaultDialect(Dialect.builder(Dialects.getDraft202012()).format(new EmailFormat(new CanonicalEmailValidator())).build(),builder->builder.schemaRegistryConfig(SchemaRegistryConfig.builder().pathType(PathType.JSON_POINTER).formatAssertionsEnabled(true).build()));
    public ContractRegistry(JsonSupport json) {
        this.json=json;this.openapi=read("openapi-v1");this.protocol=read("protocol-rules-v1");this.seed=read("seed-v1");
        openapi.path("paths").properties().forEach(path->{
            path.getValue().properties().forEach(method->{
                if(!Set.of("get","post","put","delete","patch").contains(method.getKey()))return;
                List<String> names=new ArrayList<>();StringBuilder regex=new StringBuilder("^/api/v1");
                var matcher=Pattern.compile("\\{([^}]+)\\}").matcher(path.getKey());int last=0;
                while(matcher.find()){regex.append(Pattern.quote(path.getKey().substring(last,matcher.start()))).append("([^/]+)");names.add(matcher.group(1));last=matcher.end();}
                regex.append(Pattern.quote(path.getKey().substring(last))).append("$");
                operations.add(new Operation(method.getValue().path("operationId").asString(),method.getKey().toUpperCase(Locale.ROOT),path.getKey(),method.getValue(),Pattern.compile(regex.toString()),List.copyOf(names)));
            });
        });
    }
    private JsonNode read(String name) { try(var input=new ClassPathResource("contracts/"+name+".json").getInputStream()){return json.parse(new String(input.readAllBytes(),StandardCharsets.UTF_8));}catch(Exception exception){throw new IllegalStateException("Missing frozen contract "+name,exception);} }
    public JsonNode seed() {return seed;}
    public JsonNode openapi() {return openapi;}
    public int status(String code) {var value=protocol.path("errorStatus").get(code);if(value==null)throw new IllegalArgumentException("Unknown canonical error code: "+code);return value.asInt();}
    public Optional<Operation> match(String method,String path) {return operations.stream().filter(operation->operation.method().equals(method)&&operation.pattern().matcher(path).matches()).findFirst();}
    public Optional<Operation> match(HttpServletRequest request) {return match(request.getMethod(),request.getRequestURI().substring(request.getContextPath().length()));}
    public JsonNode resolve(JsonNode node) {return node.has("$ref")?openapi.at(node.path("$ref").asString().substring(1)):node;}
    public void validateSchema(String name,JsonNode value) { validate("schema:"+name,openapi.path("components").path("schemas").path(name),value,""); }
    public void validateRequest(Operation operation,JsonNode value) {
        var schema=resolve(operation.definition().path("requestBody")).path("content").path("application/json").path("schema");
        if(schema.isMissingNode()) {if(value!=null&&!value.isNull())throw ApiException.validation("/body","Operation không nhận JSON body.");return;}
        validate("request:"+operation.id(),schema,value,"");
    }
    public void validateRequest(String operationId,JsonNode value) {validateRequest(operations.stream().filter(item->item.id().equals(operationId)).findFirst().orElseThrow(),value);}
    public void validateResponse(Operation operation,int status,JsonNode value) {
        if(status==204){if(value!=null&&!value.isNull())throw new IllegalStateException("204 must have no response body: "+operation.id());}
        if(!operation.definition().path("responses").has(Integer.toString(status)))throw new IllegalStateException("Undeclared response status: "+operation.id()+" "+status);
        var response=resolve(operation.definition().path("responses").path(Integer.toString(status)));
        var schema=response.path("content").path(status>=400?"application/problem+json":"application/json").path("schema");
        if(schema.isMissingNode())return;
        try{validate("response:"+operation.id()+":"+status,schema,value,"");}
        catch(ApiException exception){throw new IllegalStateException("Response violates canonical contract: "+operation.id()+" "+exception.violations(),exception);}
    }
    public void validateParameters(Operation operation,HttpServletRequest request) {
        Map<String,List<String>> query=new LinkedHashMap<>();
        if(request.getQueryString()!=null)for(String pair:request.getQueryString().split("&")){
            String[] sides=pair.split("=",2);String key=decode(sides[0]);String value=sides.length==2?decode(sides[1]):"";query.computeIfAbsent(key,unused->new ArrayList<>()).add(value);
        }
        Map<String,JsonNode> allowed=new HashMap<>();
        for(JsonNode raw:operation.definition().path("parameters")) {var parameter=resolve(raw);if(parameter.path("in").asString().equals("query"))allowed.put(parameter.path("name").asString(),parameter);}
        for(var entry:allowed.entrySet()) {
            if(entry.getValue().path("required").asBoolean() && !query.containsKey(entry.getKey()))
                throw ApiException.validation("/query/"+pointer(entry.getKey()),"Thiếu query parameter bắt buộc.");
        }
        for(var entry:query.entrySet()) {
            if(!allowed.containsKey(entry.getKey()))throw ApiException.validation("/query/"+pointer(entry.getKey()),"Query parameter không được định nghĩa.");
            var parameter=allowed.get(entry.getKey());JsonNode schema=parameter.path("schema");
            if(entry.getValue().size()!=1&&!resolve(schema).path("type").asString().equals("array"))throw ApiException.validation("/query/"+pointer(entry.getKey()),"Query parameter không được lặp.");
            JsonNode value=wireValue(schema,entry.getValue());validate("query:"+operation.id()+":"+entry.getKey(),schema,value,"/query/"+pointer(entry.getKey()));
        }
        var pathMatcher=operation.pattern().matcher(request.getRequestURI().substring(request.getContextPath().length()));pathMatcher.matches();
        for(JsonNode raw:operation.definition().path("parameters")) {
            var parameter=resolve(raw);String where=parameter.path("in").asString(),name=parameter.path("name").asString();String value=null;
            if(where.equals("header"))value=request.getHeader(name);
            else if(where.equals("path")){int index=operation.variables().indexOf(name);if(index>=0)value=pathMatcher.group(index+1);}
            else continue;
            if(value==null&&parameter.path("required").asBoolean()) {
                if(name.equals("If-Match"))throw ApiException.of("PRECONDITION_REQUIRED","Thiếu If-Match.");
                throw ApiException.validation("/"+(where.equals("header")?"headers":"path")+"/"+name,"Thiếu tham số bắt buộc.");
            }
            if(value!=null)validate("parameter:"+operation.id()+":"+name,parameter.path("schema"),wireValue(parameter.path("schema"),List.of(value)),"/"+(where.equals("header")?"headers":"path")+"/"+name);
        }
    }
    private JsonNode wireValue(JsonNode raw,List<String> values) {
        JsonNode schema=resolve(raw);String type=schema.path("type").asString();
        if(type.equals("array")){var result=json.array();for(String value:values)for(String part:value.split(",",-1))result.add(wireValue(schema.path("items"),List.of(part)));return result;}
        String value=values.getFirst();
        if(type.equals("integer")){if(!value.matches("-?(0|[1-9][0-9]*)"))return json.value(value);try{return json.value(new java.math.BigInteger(value));}catch(Exception exception){return json.value(value);}}
        if(type.equals("boolean")&&(value.equals("true")||value.equals("false")))return json.value(Boolean.parseBoolean(value));
        return json.value(value);
    }
    private void validate(String key,JsonNode schemaNode,JsonNode value,String prefix) {
        if(schemaNode.isMissingNode())throw new IllegalStateException("Missing schema "+key);
        Schema schema=validators.computeIfAbsent(key,unused->{var root=(ObjectNode)schemaNode.deepCopy();root.put("$schema","https://json-schema.org/draft/2020-12/schema");root.set("components",openapi.path("components"));return schemas.getSchema(SchemaLocation.of("urn:contract-governance:"+key.replace(':','-')),root);});
        var errors=schema.validate(value==null?json.value(null):value);
        if(!errors.isEmpty()){
            var violations=errors.stream().limit(100).map(error->{String path=prefix+error.getInstanceLocation().toString();return new ApiException.Violation(path.isEmpty()?"/":path,error.getKeyword(),truncate(error.getMessage(),1000));}).toList();
            throw new ApiException("VALIDATION_FAILED","Dữ liệu không đúng hợp đồng FE/BE.",violations,null);
        }
    }
    private static String decode(String value){try{return java.net.URLDecoder.decode(value,StandardCharsets.UTF_8);}catch(IllegalArgumentException exception){throw ApiException.validation("/query","Query encoding không hợp lệ.");}}
    private static String pointer(String value){return value.replace("~","~0").replace("/","~1");}
    private static String truncate(String value,int length){return value.length()>length?value.substring(0,length):value;}
}
