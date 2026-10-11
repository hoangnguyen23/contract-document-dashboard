package vn.contractgovernance.api.files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.ActorContext;
import vn.contractgovernance.api.shared.ApiResult;
import vn.contractgovernance.api.shared.ETags;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.ContractRegistry;
import vn.contractgovernance.api.shared.JsonSupport;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService service;private final ActorContext actors;private final ContractRegistry contracts;private final JsonSupport json;
    public FileController(FileService service,ActorContext actors,ContractRegistry contracts,JsonSupport json){this.service=service;this.actors=actors;this.contracts=contracts;this.json=json;}
    @GetMapping public JsonNode list(@RequestParam java.util.Map<String,String> query){return service.list(query,actors.current());}
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)public ResponseEntity<JsonNode> upload(@RequestPart(value="file",required=false)MultipartFile file,@RequestParam(value="branchId",required=false)String branch,@RequestParam(value="targetKind",required=false)String targetKind,@RequestParam(value="targetId",required=false)String targetId,@RequestParam(value="purpose",required=false)String purpose,@RequestHeader(value="Idempotency-Key",required=false)String key,HttpServletRequest request){
        var actor=actors.current();var fields=json.object();var seen=new java.util.HashSet<String>();
        try{for(var part:request.getParts()){if(!java.util.Set.of("file","branchId","targetKind","targetId","purpose").contains(part.getName()))throw ApiException.validation("/"+part.getName(),"Multipart field không được định nghĩa.");if(!seen.add(part.getName()))throw ApiException.validation("/"+part.getName(),"Multipart field không được lặp.");}}
        catch(java.io.IOException | jakarta.servlet.ServletException exception){throw ApiException.validation("/file","Multipart request không hợp lệ.");}
        if(file!=null)fields.put("file","<binary>");if(branch==null || branch.isBlank() || branch.equals("null"))fields.putNull("branchId");else fields.put("branchId",branch);if(targetKind!=null)fields.put("targetKind",targetKind);if(targetId!=null)fields.put("targetId",targetId);if(purpose!=null)fields.put("purpose",purpose);
        contracts.validateSchema("FileUpload",fields);var branchId=fields.hasNonNull("branchId")?UUID.fromString(branch):null;return service.upload(file,branchId,targetKind,UUID.fromString(targetId),purpose,key,actor).response();
    }
    @GetMapping("/{id}")public ResponseEntity<JsonNode> get(@PathVariable UUID id){return ApiResult.ok(service.get(id,actors.current())).response();}
    @DeleteMapping("/{id}")public ResponseEntity<JsonNode> delete(@PathVariable UUID id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.delete(id,body,tag,key,actors.current()).response();}
    @PostMapping("/{id}/commands")public ResponseEntity<JsonNode> command(@PathVariable UUID id,@RequestBody JsonNode body,@RequestHeader(value="If-Match",required=false)String tag,@RequestHeader(value="Idempotency-Key",required=false)String key){return service.retry(id,body,tag,key,actors.current()).response();}
    @GetMapping("/{id}/content")public ResponseEntity<byte[]> content(@PathVariable UUID id,@RequestParam(value="representation",required=false)String representation){if(representation==null)throw ApiException.validation("/query/representation","Phải chọn FULL hoặc REDACTED.");var download=service.download(id,representation,actors.current());return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.mediaType())).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''"+download.filename()).header("X-Content-Type-Options","nosniff").header("X-Content-SHA256",download.hash()).header("ETag",ETags.format(download.version())).contentLength(download.bytes().length).body(download.bytes());}
}
