// ─── RFC9457 Problem Detail (Hợp đồng B1.3.0) ──────────────────────────────
export interface ApiViolation {
  path: string;       // JSON pointer, e.g. "/terms/feeVnd" or "/branchId"
  code: string;       // e.g. "REQUIRED", "INVALID_FORMAT", "VALUE_OUT_OF_RANGE"
  message: string;
}

export interface ApiProblem {
  type?: string;      // URI-reference, e.g. "urn:contract-governance:problem:csrf_invalid"
  title?: string;     // Short human-readable title, e.g. "CSRF_INVALID"
  status: number;     // 400, 401, 403, 404, 409, 410, 412, 413, 415, 422, 428, 429, 503
  detail: string;     // Detailed explanation in Vietnamese or English
  instance?: string;  // Resource URI, e.g. "/api/v1/session/login"
  code: string;       // Exact enum code from B1.3.0
  traceId?: string;   // UUIDv4 trace correlation ID
  violations?: ApiViolation[];
  currentVersion?: number | null; // For 412 PRECONDITION_FAILED
  retryAfterSeconds?: number;     // For 429 RATE_LIMITED
}

// ─── Idempotency Key ───────────────────────────────────────────────────────
/**
 * Generate a fresh UUIDv4 Idempotency-Key for each distinct business mutation.
 * Per B §4: required on all business create/mutation POST/PUT/DELETE.
 * Key is scoped to (actorId, key); same key + same fingerprint = replay.
 */
export function generateIdempotencyKey(): string {
  return crypto.randomUUID();
}

// ─── ETag store ────────────────────────────────────────────────────────────
// Simple in-memory store; maps resourceId → ETag value
const etagStore = new Map<string, string>();

export const etagCache = {
  set: (resourceId: string, etag: string) => etagStore.set(resourceId, etag),
  get: (resourceId: string): string | undefined => etagStore.get(resourceId),
  delete: (resourceId: string) => etagStore.delete(resourceId),
};

// ─── Formatted Error Object ────────────────────────────────────────────────
export interface FormattedError {
  title: string;
  message: string;
  code: string;
  status: number;
  traceId?: string;
  violations?: ApiViolation[];
  currentVersion?: number | null;
}

// ─── Error formatting helpers ──────────────────────────────────────────────
export function formatApiError(err: unknown): FormattedError {
  const e = err as Partial<ApiProblem>;
  const status = e.status || 500;
  const code = e.code || 'UNKNOWN_ERROR';
  const detail = e.detail || (err as Error)?.message || 'Đã xảy ra lỗi không xác định từ máy chủ.';
  const traceId = e.traceId;
  const violations = e.violations || [];
  const currentVersion = e.currentVersion;

  // 401: Hết hạn phiên / Chưa xác thực
  if (code === 'UNAUTHENTICATED' || status === 401) {
    return {
      title: 'Phiên đăng nhập hết hạn',
      message: detail || 'Phiên làm việc đã kết thúc. Vui lòng đăng nhập lại.',
      code,
      status,
      traceId,
    };
  }

  // 403: Không có quyền truy cập / CSRF / Phân tách nhiệm vụ (SoD)
  if (code === 'CSRF_INVALID') {
    return {
      title: 'Mã bảo mật phiên (CSRF) không hợp lệ',
      message: detail || 'Vui lòng làm mới trang để nhận mã phiên làm việc mới.',
      code,
      status,
      traceId,
    };
  }
  if (code === 'SOD_VIOLATION') {
    return {
      title: 'Vi phạm quy tắc Phân tách nhiệm vụ (SoD)',
      message: detail || 'Người lập hồ sơ không được phép tự phê duyệt hoặc xác minh hồ sơ này.',
      code,
      status,
      traceId,
    };
  }
  if (code === 'ROLE_FORBIDDEN' || code === 'SENSITIVE_FORBIDDEN' || code === 'EXPORT_FORBIDDEN' || status === 403) {
    return {
      title: 'Không đủ quyền truy cập',
      message: detail || 'Tài khoản hoặc chi nhánh hiện hành của bạn không có quyền thực hiện thao tác này.',
      code,
      status,
      traceId,
    };
  }

  // 404: Không tìm thấy tài nguyên trong phạm vi
  if (code === 'NOT_FOUND' || status === 404) {
    return {
      title: 'Không tìm thấy dữ liệu',
      message: detail || 'Tài nguyên được yêu cầu không tồn tại hoặc nằm ngoài phạm vi chi nhánh của bạn.',
      code,
      status,
      traceId,
    };
  }

  // 409: Xung đột trạng thái / Idempotency / Base conflict
  if (code === 'IDEMPOTENCY_CONFLICT') {
    return {
      title: 'Xung đột khóa lặp (Idempotency Conflict)',
      message: detail || 'Yêu cầu trùng khóa nhưng khác nội dung đã được xử lý trước đó.',
      code,
      status,
      traceId,
    };
  }
  if (code === 'IDEMPOTENCY_IN_PROGRESS') {
    return {
      title: 'Yêu cầu đang được xử lý',
      message: detail || 'Một thao tác tương tự đang diễn ra trên máy chủ, vui lòng đợi trong giây lát.',
      code,
      status,
      traceId,
    };
  }
  if (code === 'STATE_CONFLICT' || code === 'BASE_CONFLICT' || code === 'SCHEDULE_CONFLICT' || status === 409) {
    return {
      title: 'Xung đột trạng thái dữ liệu',
      message: detail || 'Trạng thái hiện tại của đối tượng không cho phép thực hiện thao tác này.',
      code,
      status,
      traceId,
    };
  }

  // 410: Cursor hết hạn
  if (code === 'CURSOR_EXPIRED' || status === 410) {
    return {
      title: 'Trang tìm kiếm đã hết hiệu lực',
      message: detail || 'Con trỏ phân trang đã hết hạn (15 phút). Vui lòng làm mới danh sách.',
      code,
      status,
      traceId,
    };
  }

  // 412: Phiên bản cũ (Precondition Failed)
  if (code === 'PRECONDITION_FAILED' || status === 412) {
    return {
      title: 'Dữ liệu đã thay đổi trên máy chủ',
      message: detail || 'Bản ghi đã được người dùng khác cập nhật. Vui lòng tải lại trang để nhận dữ liệu mới nhất.',
      code,
      status,
      traceId,
      currentVersion,
    };
  }

  // 428: Thiếu ETag (Precondition Required)
  if (code === 'PRECONDITION_REQUIRED' || status === 428) {
    return {
      title: 'Yêu cầu thông tin phiên bản (If-Match)',
      message: detail || 'Thao tác cập nhật yêu cầu xác định phiên bản hiện hành. Vui lòng tải lại trang.',
      code,
      status,
      traceId,
    };
  }

  // 422: Lỗi dữ liệu đầu vào (Validation Failed)
  if (code === 'VALIDATION_FAILED' || status === 422) {
    let violationMsg = detail;
    if (violations.length > 0) {
      violationMsg = violations
        .map((v) => `${v.path ? v.path + ': ' : ''}${v.message}`)
        .join(' · ');
    }
    return {
      title: 'Dữ liệu không hợp lệ',
      message: violationMsg || 'Vui lòng kiểm tra lại các trường dữ liệu theo quy định.',
      code,
      status,
      traceId,
      violations,
    };
  }

  // 429: Giới hạn tần suất
  if (code === 'RATE_LIMITED' || status === 429) {
    const wait = e.retryAfterSeconds ?? 60;
    return {
      title: 'Tần suất thao tác quá nhanh',
      message: detail || `Hệ thống tạm khóa yêu cầu. Vui lòng chờ ${wait} giây trước khi thử lại.`,
      code,
      status,
      traceId,
    };
  }

  // 503: Dịch vụ tạm gián đoạn / ClamAV Scanner
  if (code === 'SERVICE_UNAVAILABLE' || status === 503) {
    return {
      title: 'Dịch vụ tạm thời không khả dụng',
      message: detail || 'Hệ thống hạ tầng máy chủ hoặc quét mã độc đang bảo trì. Vui lòng thử lại sau.',
      code,
      status,
      traceId,
    };
  }

  // Lỗi mạng hoặc lỗi không xác định
  if (code === 'NETWORK_ERROR') {
    return {
      title: 'Lỗi kết nối máy chủ',
      message: detail || 'Không thể kết nối đến máy chủ backend (http://127.0.0.1:8081).',
      code,
      status: 0,
      traceId,
    };
  }

  return {
    title: 'Thao tác không thành công',
    message: detail,
    code,
    status,
    traceId,
    violations,
  };
}
