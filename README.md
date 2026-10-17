# Contract Document Dashboard

Backend cho hệ thống quản trị hồ sơ và hợp đồng thu hộ doanh nghiệp. Đợt hiện tại triển khai nền tảng BE G1/G2 và policy và xét duyệt hồ sơ G3 trên nhánh `feature/backend-approval-agreement`.

## Phần đã triển khai

- Java 25, Spring Boot 4.1.1, Spring Modulith 2.1.1 và Maven Wrapper 3.10.0.
- PostgreSQL/Flyway, session/CSRF, quyền hiện hành và scope chi nhánh.
- Người dùng, chi nhánh, Party, quan hệ khách hàng, phân công maker và xác nhận contact độc lập.
- Hồ sơ nháp, private evidence, quét ClamAV và durable jobs.
- Policy hữu hạn, mô phỏng, regression độc lập và lịch công bố bền vững.
- Nộp/rút hồ sơ, bản chốt bất biến, kiểm tra bằng chứng và vòng L1/L2; duyệt cuối còn chờ hợp đồng/PDF thực.
- Schema canonical, ETag, idempotency, audit và cursor có chữ ký.

Hợp đồng/PDF, ký/effectivity, thay đổi hợp đồng, thông báo và các luồng G3–G5 nằm trong các đợt tiếp theo. Có schema của một operation không đồng nghĩa operation đó đã được triển khai.

Xem [bảng đủ 85 operation](docs/plan/backend-operation-coverage.md): 43 operation đã có handler, 5 operation mới hỗ trợ một phần và 37 operation thuộc các đợt tiếp theo. Đây là mức độ triển khai từ mã nguồn; nghiệm thu tích hợp vẫn còn chờ.

## Bắt đầu

Đọc [hướng dẫn Backend](apps/api/README.md) để cấu hình Java 25, PostgreSQL, ClamAV, secret và profile demo. Docker Compose cung cấp PostgreSQL/scanner; Java API được khởi chạy riêng. Browser dùng cùng origin HTTPS qua proxy theo contract.

Trên Windows, từ thư mục repo:

```powershell
.\scripts\start-backend.ps1 -DatabasePort 56432 -Demo
```

Script mặc định dùng PostgreSQL `127.0.0.1:55432`, scanner `127.0.0.1:3310` và API `127.0.0.1:8081`, chạy Java nền với timezone UTC và kiểm tra tiến trình đúng dự án. Ví dụ trên đổi database sang `56432` vì Windows trên máy hiện tại giữ dải cổng có `55432`. Secret được tạo vào `.env` nếu chưa có; cấu hình hiện có được giữ nguyên. Dừng API bằng `scripts/stop-backend.ps1`; thêm `-StopServices` để dừng hai container của dự án và giữ dữ liệu.

Build tại `apps/api`, bỏ qua cả biên dịch và chạy test theo yêu cầu của chủ dự án:

```powershell
.\mvnw.cmd '-Dmaven.test.skip=true' package
```

## Tài liệu và cấu trúc

- [Chỉ mục tài liệu](docs/INDEX.md): hai đặc tả canonical A/B 1.2.0 và kế hoạch triển khai.
- `apps/api/`: mã Backend, migrations và hướng dẫn từng module.
- `contracts/`: dữ liệu dẫn xuất từ canonical blocks; cập nhật qua `scripts/extract_contracts.py`.
- `compose.yml`: PostgreSQL và ClamAV được pin bằng image digest.
- `.github/workflows/backend-build.yml`: trích xuất contract và package build, không chạy test.

Đã quan sát PostgreSQL/Flyway, ClamAV và API khởi động thực; xem [bằng chứng runtime](docs/plan/reviews/backend-runtime-startup.md). Build, review nguồn và kết nối hạ tầng không thay thế nghiệm thu tích hợp G1/G2. Browser, các luồng nghiệp vụ, race/recovery và NFR chưa được xác nhận trong đợt này.
