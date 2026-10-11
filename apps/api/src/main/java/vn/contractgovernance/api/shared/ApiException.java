package vn.contractgovernance.api.shared;

import java.util.List;

public class ApiException extends RuntimeException {
    public record Violation(String path, String code, String message) {}
    private final String code;
    private final List<Violation> violations;
    private final Integer currentVersion;
    public ApiException(String code, String detail, List<Violation> violations, Integer currentVersion) {
        super(detail); this.code = code; this.violations = List.copyOf(violations); this.currentVersion = currentVersion;
    }
    public static ApiException of(String code, String detail) { return new ApiException(code, detail, List.of(), null); }
    public static ApiException validation(String path, String detail) { return new ApiException("VALIDATION_FAILED", detail, List.of(new Violation(path, "INVALID", detail)), null); }
    public static ApiException stale(int version) { return new ApiException("PRECONDITION_FAILED", "Tài nguyên đã thay đổi. Hãy tải lại trước khi tiếp tục.", List.of(), version); }
    public String code() { return code; }
    public List<Violation> violations() { return violations; }
    public Integer currentVersion() { return currentVersion; }
}
