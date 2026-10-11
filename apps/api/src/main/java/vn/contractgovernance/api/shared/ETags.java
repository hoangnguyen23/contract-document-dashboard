package vn.contractgovernance.api.shared;

import java.util.regex.Pattern;

public final class ETags {
    private static final Pattern STRONG = Pattern.compile("\"r-([1-9][0-9]*)\"");
    private ETags() {}
    public static String format(int version) { return "\"r-" + version + "\""; }
    public static int require(String value) {
        if (value == null) throw ApiException.of("PRECONDITION_REQUIRED", "Thiếu If-Match.");
        var match = STRONG.matcher(value);
        if (!match.matches()) throw ApiException.validation("/headers/If-Match", "If-Match phải là strong ETag của tài nguyên.");
        try { return Integer.parseInt(match.group(1)); } catch (NumberFormatException exception) { throw ApiException.validation("/headers/If-Match", "Phiên bản không hợp lệ."); }
    }
    public static void check(String value, int current) { if (require(value) != current) throw ApiException.stale(current); }
}
