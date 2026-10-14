# Contract Document Dashboard

Backend cho hệ thống quản trị hồ sơ và hợp đồng thu hộ doanh nghiệp. Đợt hiện tại triển khai phần nền tảng BE của G1/G2 trên nhánh `feature/backend-foundation`.

## Phần đã triển khai

- Java 25, Spring Boot 4.1.1, Spring Modulith 2.1.1 và Maven Wrapper 3.10.0.
- PostgreSQL/Flyway, session/CSRF, quyền hiện hành và scope chi nhánh.
- Người dùng, chi nhánh, Party, quan hệ khách hàng, phân công maker và xác nhận contact độc lập.
- Hồ sơ nháp, private evidence, quét ClamAV và durable jobs.
- Schema canonical, ETag, idempotency, audit và cursor có chữ ký.

Phê duyệt, ký/effectivity, thay đổi hợp đồng, thông báo và các luồng G3–G5 nằm trong các đợt tiếp theo. Có schema của một operation không đồng nghĩa operation đó đã được triển khai.

Xem [bảng đủ 85 operation](docs/plan/backend-operation-coverage.md): 36 operation đã có handler, 2 operation mới hỗ trợ một phần và 47 operation thuộc các đợt tiếp theo. Đây là mức độ triển khai từ mã nguồn; nghiệm thu tích hợp vẫn còn chờ.

## Bắt đầu

Đọc [hướng dẫn Backend](apps/api/README.md) để cấu hình Java 25, PostgreSQL, ClamAV, secret và profile demo. Docker Compose cung cấp PostgreSQL/scanner; Java API được khởi chạy riêng. Browser dùng cùng origin HTTPS qua proxy theo contract.

Build tại `apps/api`, bỏ qua cả biên dịch và chạy test theo yêu cầu của chủ dự án:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' package
```

## Tài liệu và cấu trúc

- [Chỉ mục tài liệu](docs/INDEX.md): hai đặc tả canonical A/B 1.1.0 và kế hoạch triển khai.
- `apps/api/`: mã Backend, migrations và hướng dẫn từng module.
- `contracts/`: dữ liệu dẫn xuất từ canonical blocks; cập nhật qua `scripts/extract_contracts.py`.
- `compose.yml`: PostgreSQL và ClamAV được pin bằng image digest.
- `.github/workflows/backend-build.yml`: trích xuất contract và package build, không chạy test.

Build thành công và review nguồn không thay thế nghiệm thu tích hợp G1/G2. Chưa có bằng chứng chạy PostgreSQL/scanner, browser, race hoặc NFR trong đợt này.
