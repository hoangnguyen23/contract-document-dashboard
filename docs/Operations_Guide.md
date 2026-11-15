# Tài Liệu Vận Hành & Hướng Dẫn Hệ Thống (Operations & Verification Guide)

> **Dự án**: Contract Governance Dashboard  
> **Phiên bản Hợp đồng**: **B1.3.0** (`contracts/openapi-v1.json`, `docs/FE_BE_Shared_Development_Contract.md`)  
> **Trạng thái**: Hoàn thiện toàn diện Phase 6 (6.1 – 6.12)

---

## 1. Kiến Trúc Tổng Thể & Môi Trường

```
                      +---------------------------------------+
                      |     Web Dashboard (React 19 + TS)     |
                      |   Port: 5173 (Dev) / Production CDN   |
                      +-------------------+-------------------+
                                          |
                        HTTP / B1.3.0 Wire Protocol
                     (If-Match, X-CSRF-Token, RFC 9457)
                                          v
                      +---------------------------------------+
                      |    Spring Boot 4.1.1 API (Modulith)   |
                      |   Oracle JDK 25.0.4.1 -- Port: 8081    |
                      +---------+-------------------+---------+
                                |                   |
                 JDBC (Hikari)  |                   | TCP Socket (INSTREAM)
                                v                   v
             +-----------------------+   +-----------------------+
             | PostgreSQL 18.6 DB    |   | ClamAV Daemon 1.5.4   |
             | Port: 56432           |   | Port: 3310            |
             | Flyway V001 - V013    |   | Antivirus Scanner     |
             +-----------------------+   +-----------------------+
```

### Thông số môi trường chuẩn:
- **Hệ điều hành**: Windows 11 / Windows Server / Linux Container
- **Runtime Java**: Oracle JDK `25.0.4.1` (Architecture: `ALL-UNNAMED`, Timezone: `UTC`)
- **Runtime Node**: Node.js `v22.19.0` (ESM native)
- **Cơ sở dữ liệu**: PostgreSQL container `contract-governance-postgres-1` (`127.0.0.1:56432`), database `contract_governance`.
- **Dịch vụ diệt virus**: ClamAV container `contract-governance-scanner-1` (`127.0.0.1:3310`).
- **Thư mục lưu trữ tài liệu riêng tư**: `var/private-files`.

---

## 2. Danh Sách Tài Khoản Thử Nghiệm Chuẩn (Demo Accounts)

Mật khẩu dùng chung cho các tài khoản demo (khởi tạo qua biến môi trường `CG_DEMO_PASSWORD`):
`TfYnSqjKLV70F9oSVCuCiPf0KFqHm7f_M7tAnYRMWrM`

| Tên đăng nhập | Vai trò (Roles) | Quyền hạn chính |
| :--- | :--- | :--- |
| `demo.maker` | `MAKER` | Khởi tạo hồ sơ thu hộ (Dossier), tải tài liệu, cập nhật điều khoản, chỉ định Reviewer, nộp duyệt, hủy hồ sơ. |
| `demo.checker` | `CHECKER` | Thẩm định vòng phê duyệt (Round), xem Snapshot bất biến, duyệt (`APPROVE`), trả về (`RETURN`), từ chối (`REJECT`), xác minh chứng từ (`VERIFY_EVIDENCE`), điều phối thông báo (`DISPATCH`, `RECORD_ACK`, `VERIFY_ACK`). |
| `demo.admin` | `BANK_ADMIN` | Quản trị người dùng, phân quyền chi nhánh, cấu hình tham số, theo dõi toàn bộ Audit Trail nhật ký kiểm toán. |

---

## 3. Quy Ước Giao Thức Wire Protocol B1.3.0 & RFC 9457

### 3.1. Phân trang dựa trên con trỏ (Cursor-based Pagination)
Theo Điều khoản B §5, tất cả các danh sách nghiệp vụ **không có trường total**, mà sử dụng cấu trúc:
```json
{
  "items": [ ... ],
  "nextCursor": "string | null",
  "asOf": "2026-11-15T15:00:00.000Z",
  "timelineProcessingLagSeconds": 0
}
```

### 3.2. Quản lý đồng thời lạc quan (Optimistic Concurrency Control)
- Mọi tài nguyên đơn thể trả về header `ETag` chuẩn regex `^"r-[1-9][0-9]*"$`.
- Mọi lệnh cập nhật (`PUT`) hoặc lệnh chuyển trạng thái (`POST /commands`) **bắt buộc** truyền header:
  `If-Match: "r-X"`
- Khi phát hiện xung đột phiên bản, máy chủ phản hồi HTTP **412 PRECONDITION_FAILED** kèm mã `PRECONDITION_FAILED` và trường `currentVersion` chỉ rõ số phiên bản hiện tại trên server.

### 3.3. Định dạng dữ liệu tiền tệ & ngày tháng
- Tiền tệ VND tuân thủ regex `^(0|[1-9][0-9]{0,9})$` (chuỗi số không âm, tối đa 10 chữ số).
- Ngày hiệu lực (`startOn`, `endExclusive`, `validFrom`, `validTo`): Định dạng chuẩn RFC 3339 full-date (`YYYY-MM-DD`).
- Dấu thời gian hệ thống (`createdAt`, `updatedAt`, `scannedAt`): Định dạng RFC 3339 date-time có mili-giây và múi giờ Z (`YYYY-MM-DDTHH:mm:ss.sssZ`).

### 3.4. Định dạng lỗi chuẩn RFC 9457 Problem Details
Tất cả các phản hồi lỗi từ máy chủ trả về `Content-Type: application/problem+json`:
```json
{
  "type": "urn:contract-governance:problem:<code_lower>",
  "title": "<ERROR_CODE>",
  "status": 4xx | 5xx,
  "detail": "Mô tả lỗi tiếng Việt dành cho người dùng.",
  "instance": "/api/v1/...",
  "code": "<ERROR_CODE>",
  "traceId": "uuid-v4",
  "violations": [
    { "path": "/terms/feeVnd", "code": "pattern", "message": "..." }
  ],
  "currentVersion": null | number
}
```

### 3.5. Cơ chế phân quyền theo gợi ý máy chủ (Action Hints)
- Giao diện **không tự suy đoán quyền hạn**.
- Mỗi thực thể trả về:
  - `allowedActions`: Danh sách các hành động người dùng hiện tại được phép thực hiện.
  - `blockedActions`: Mảng các đối tượng `{ action, code, reason }` giải thích lý do cụ thể máy chủ từ chối hành động.
- Nút bấm trên giao diện tự động disable và hiển thị tooltip chứa `reason` khi hành động nằm trong `blockedActions`.

---

## 4. Hướng Dẫn Vận Hành & Khởi Chạy

### 4.1. Khởi động hạ tầng Docker
```powershell
docker start contract-governance-postgres-1 contract-governance-scanner-1
```
Kiểm tra trạng thái:
```powershell
docker ps --filter "name=contract-governance"
```

### 4.2. Khởi động Backend API
Chạy tệp JAR đã được đóng gói bằng Oracle JDK 25.0.4.1:
```powershell
$env:CG_DATABASE_PASSWORD = "H7stILohY2jyFGa8K29qTDa9eScBreYiDzc6prAnE9Y"
$env:CG_CURSOR_SECRET     = "lT9CPO02LGgKCOo7ZLL3pBPACKfwYNUJG628-tqiW3Y"
$env:CG_DATABASE_URL      = "jdbc:postgresql://127.0.0.1:56432/contract_governance"
$env:CG_DATABASE_USER     = "contract_governance"
$env:CG_SCANNER_HOST      = "127.0.0.1"
$env:CG_SCANNER_PORT      = "3310"
$env:CG_SERVER_ADDRESS    = "127.0.0.1"
$env:CG_STORAGE_ROOT      = "D:\Projects\contract-document-dashboard\var\private-files"
$env:PORT                 = "8081"
$env:SPRING_PROFILES_ACTIVE = "demo"
$env:CG_DEMO_PASSWORD     = "TfYnSqjKLV70F9oSVCuCiPf0KFqHm7f_M7tAnYRMWrM"

& 'C:\Program Files\Java\jdk-25.0.4.1\bin\java.exe' `
  --enable-native-access=ALL-UNNAMED `
  '-Duser.timezone=UTC' `
  -jar D:\Projects\contract-document-dashboard\apps\api\target\contract-governance-api-0.1.0-SNAPSHOT.jar
```

### 4.3. Chạy Frontend Web Dashboard
Vào thư mục `apps/web`:
```powershell
cd apps/web

# Chế độ phát triển (Dev server)
npm run dev

# Kiểm tra cú pháp và build sản phẩm (Production build)
npm run build

# Xem thử bản dựng sản phẩm
npm run preview
```

### 4.4. Chạy kiểm thử tự động toàn diện (E2E Workflow Test)
Chạy bộ kiểm thử E2E tương thích 100% với backend thực tế:
```powershell
cd apps/web
npm run test:e2e
```
Kết quả kiểm thử thực hiện 18 bước kiểm tra nghiêm ngặt:
1. Xác thực bảo mật CSRF (HTTP 403 `CSRF_INVALID`).
2. Đăng nhập đa vai trò: `MAKER`, `CHECKER`, `BANK_ADMIN`.
3. Truy vấn quan hệ khách hàng với phân trang con trỏ (Cursor pagination).
4. Khởi tạo hồ sơ thu hộ (`Dossier`) theo đúng schema B1.3.0.
5. Truy vấn động danh sách người thẩm định (`/reviewer-options`).
6. Kiểm tra khóa đồng thời `If-Match`: phát hiện lỗi 412 `PRECONDITION_FAILED` và cập nhật phiên bản.
7. Kiểm tra thẩm định dữ liệu: bắt lỗi 422 `VALIDATION_FAILED` với danh sách vi phạm trường.
8. Cập nhật hồ sơ thành công và tăng ETag lên `"r-2"`.
9. Tải tài liệu PDF qua multipart, gửi qua hàng đợi quét virus ClamAV, kiểm tra trạng thái chuyển đổi sang `CLEAN`.
10. Tải nội dung tệp nhị phân (`/content?representation=FULL`).
11. Truy vấn các vòng phê duyệt của Kiểm soát viên.
12. Kiểm tra Snapshot bất biến của vòng duyệt (`/rounds/{id}/snapshot`).
13. Truy vấn danh sách thông báo và kiểm tra hộp thư nội bộ (`/notices/{id}/capture`).
14. Truy vấn hợp đồng và kiểm tra trạng thái sẵn sàng (Readiness risks).
15. Thực thi lệnh hủy (`CANCEL`) dọn dẹp tài nguyên.

---

## 5. Xử Lý Sự Cố Thường Gặp (Troubleshooting Runbook)

| Hiện tượng | Nguyên nhân khả dĩ | Biện pháp xử lý |
| :--- | :--- | :--- |
| **HTTP 403 `CSRF_INVALID`** | Gửi request `POST`/`PUT`/`DELETE` mà không kèm `X-CSRF-Token` hợp lệ từ session hiện tại. | Luôn gọi `GET /api/v1/session` trước để lấy session cookie và `csrfToken`. `apiClient` của Frontend đã tự động đồng bộ hóa token này. |
| **HTTP 412 `PRECONDITION_FAILED`** | Header `If-Match` không khớp với revision hiện hành của tài nguyên (có người khác đã sửa). | Nhấn nút **"Tải lại dữ liệu mới nhất"** trên modal lỗi RFC 9457 để lấy lại ETag mới nhất rồi thao tác lại. |
| **HTTP 422 `VALIDATION_FAILED`** | Payload chứa thuộc tính thừa (`additionalProperties: false`) hoặc giá trị sai định dạng (ví dụ: ngày sai chuẩn RFC 3339). | Kiểm tra bảng `violations[]` trên modal thông báo để xác định chính xác đường dẫn trường vi phạm và chuẩn hóa lại dữ liệu. |
| **HTTP 422 `FILE_NOT_CLEAN`** | Cố gắng tải nội dung tệp khi tệp đang ở trạng thái `SCANNING` hoặc quét thất bại `SCAN_FAILED`. | Chờ tiến trình quét nền của ClamAV hoàn tất (thường dưới 1 giây) chuyển sang `CLEAN` trước khi tải. |
| **ClamAV Scanner không phản hồi** | Container ClamAV bị dừng hoặc cấu hình sai port. | Chạy `docker restart contract-governance-scanner-1`, kiểm tra port 3310 bằng `docker ps`. |
