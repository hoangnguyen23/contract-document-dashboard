package vn.contractgovernance.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import tools.jackson.databind.JsonNode;

@RestControllerAdvice
public class ProblemAdvice {
    private static final Logger LOGGER=LoggerFactory.getLogger(ProblemAdvice.class);
    private final ContractRegistry contracts;private final JsonSupport json;
    public ProblemAdvice(ContractRegistry contracts,JsonSupport json){this.contracts=contracts;this.json=json;}
    public JsonNode problem(ApiException exception,HttpServletRequest request){
        int status=contracts.status(exception.code());var body=json.object();
        body.put("type","urn:contract-governance:problem:"+exception.code().toLowerCase(java.util.Locale.ROOT));body.put("title",exception.code());body.put("status",status);body.put("detail",exception.getMessage());body.put("instance",request.getRequestURI());body.put("code",exception.code());
        Object trace=request.getAttribute(TraceFilter.ATTRIBUTE);body.put("traceId",(trace==null?java.util.UUID.randomUUID():trace).toString());body.set("violations",json.value(exception.violations()));if(exception.currentVersion()==null)body.putNull("currentVersion");else body.put("currentVersion",exception.currentVersion());return body;
    }
    @ExceptionHandler(ApiException.class) public ResponseEntity<JsonNode> business(ApiException exception,HttpServletRequest request){
        var response=ResponseEntity.status(contracts.status(exception.code())).header("Content-Type","application/problem+json");
        if(exception.code().equals("RATE_LIMITED"))response.header("Retry-After","60");
        if(exception.code().equals("IDEMPOTENCY_IN_PROGRESS"))response.header("Retry-After","1");
        if(exception.code().equals("SERVICE_UNAVAILABLE")||exception.code().equals("STORAGE_INTEGRITY"))response.header("Retry-After","5");
        return response.body(problem(exception,request));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<JsonNode> badJson(HttpMessageNotReadableException exception,HttpServletRequest request){return business(ApiException.of("BAD_JSON","JSON không đọc được."),request);}
    @ExceptionHandler(MaxUploadSizeExceededException.class) public ResponseEntity<JsonNode> tooLarge(MaxUploadSizeExceededException exception,HttpServletRequest request){return business(ApiException.of("FILE_TOO_LARGE","File vượt giới hạn 10 MiB."),request);}
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class) public ResponseEntity<JsonNode> media(HttpMediaTypeNotSupportedException exception,HttpServletRequest request){return business(ApiException.of("MEDIA_UNSUPPORTED","Định dạng nội dung không được hỗ trợ."),request);}
    @ExceptionHandler(NoResourceFoundException.class) public ResponseEntity<JsonNode> missing(NoResourceFoundException exception,HttpServletRequest request){return business(ApiException.of("NOT_FOUND","Không tìm thấy tài nguyên."),request);}
    @ExceptionHandler(MissingServletRequestParameterException.class) public ResponseEntity<JsonNode> missingParameter(MissingServletRequestParameterException exception,HttpServletRequest request){return business(ApiException.validation(argumentPath(request,exception.getParameterName()),"Thiếu tham số bắt buộc."),request);}
    @ExceptionHandler(MissingServletRequestPartException.class) public ResponseEntity<JsonNode> missingPart(MissingServletRequestPartException exception,HttpServletRequest request){return business(ApiException.validation("/body/"+pointer(exception.getRequestPartName()),"Thiếu multipart part bắt buộc."),request);}
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) public ResponseEntity<JsonNode> argumentType(MethodArgumentTypeMismatchException exception,HttpServletRequest request){return business(ApiException.validation(argumentPath(request,exception.getName()),"Kiểu hoặc định dạng tham số không hợp lệ."),request);}
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<JsonNode> argumentInvalid(MethodArgumentNotValidException exception,HttpServletRequest request){var violations=exception.getBindingResult().getFieldErrors().stream().limit(100).map(error->new ApiException.Violation("/body/"+pointer(error.getField()),"INVALID","Dữ liệu không hợp lệ.")).toList();return business(new ApiException("VALIDATION_FAILED","Dữ liệu không hợp lệ.",violations,null),request);}
    @ExceptionHandler(ServletRequestBindingException.class) public ResponseEntity<JsonNode> requestBinding(ServletRequestBindingException exception,HttpServletRequest request){return business(ApiException.validation("/body","Thiếu hoặc sai dữ liệu request."),request);}
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) public ResponseEntity<JsonNode> dispositionConflict(org.springframework.dao.DataIntegrityViolationException failure,HttpServletRequest request){for(Throwable cause=failure;cause!=null;cause=cause.getCause())if(cause instanceof java.sql.SQLException sql&&"23514".equals(sql.getSQLState())&&sql.getMessage()!=null&&sql.getMessage().startsWith("ERROR: DISPOSITION_COMMITTED"))return business(ApiException.of("DISPOSITION_COMMITTED","Source is reserved by a committed disposition."),request);return infrastructure(failure,request);}
    @ExceptionHandler(Exception.class) public ResponseEntity<JsonNode> infrastructure(Exception exception,HttpServletRequest request){LOGGER.error("Request failed; traceId={}",request.getAttribute(TraceFilter.ATTRIBUTE),exception);return business(ApiException.of("SERVICE_UNAVAILABLE","Không thể hoàn tất thao tác lúc này."),request);}
    private static String argumentPath(HttpServletRequest request,String name){return (request.getContentType()!=null&&request.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data")?"/body/":"/query/")+pointer(name);}
    private static String pointer(String value){return value.replace("~","~0").replace("/","~1");}
}
