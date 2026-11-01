# FE design and delivery blueprint

Ngày lập:01/11/2026. Baseline source/remote dev81d6c17; canonical A/B1.3.0. Trạng thái: đặc tả để agent thiết kế Figma/triển khai, chưa tạo file Figma, chưa có UI code hoặc browser acceptance. Owner giao agent lập kế hoạch/khung; code, thiết kế thực thi và kiểm thử do agent khác thực hiện. Không chạy test hoặc kích hoạt runtime trong gói này.

## Đọc theo thứ tự

1. `../../docs/Final_Project_Specification.md` §3–15 và `../../docs/FE_BE_Shared_Development_Contract.md` §1–9 là nguồn yêu cầu. Tài liệu này chỉ quyết định cách trình bày, không cấp quyền hoặc đổi nghiệp vụ.
2. `01-screen-map.md`: sitemap, vai trò, phạm vi và ưu tiên.
3. `02-design-system.md`: kích thước, token, component, nội dung và accessibility.
4. `03-foundation-flow.md`: đặc tả từng màn hình của luồng đầu tiên.
5. `04-domain-workbenches.md`: đặc tả các module còn lại và invariant cần hiển thị.
6. `05-figma-agent-task.md`: nhiệm vụ thiết kế, cấu trúc file, frame, prototype và đầu ra.
7. `06-implementation-tasks.md`: thứ tự triển khai, ranh giới file, protocol, kiểm chứng để sau.
8. `07-screen-api-matrix.md`: từng operation có screen/component owner.
9. `08-wireframes.md`: khung bố cục nền tảng.
10. `reference/api-fields.md` và `reference/canonical-ui-reference.json`: đủ85operations/188schemas trích nguyên cấu trúc canonical phục vụ tra cứu. Đây là reference dẫn xuất riêng dưới docs, không thay contracts hoặc sinh DTO mới.

## Quyết định giao diện đã chốt trong blueprint

- Ngôn ngữ mặc định tiếng Việt; công cụ nghiệp vụ desktop-first, nền sáng, mật độ vừa, typography đọc dài; mobile hỗ trợ queue/detail/action an toàn.
- App shell chung; danh sách dùng server pagination/cursor, detail dùng tabs và action panel cố định trong luồng đọc; form có nhãn rõ, không tự lưu khi contract không có autosave.
- Giao diện không dùng phần trăm tuân thủ pháp lý, không badge AI quyết định, không giả money execution. KPI chỉ lấy canonical Dashboard.
- Mọi screen phải có loading/empty/error/no-access/stale/pending phù hợp. Figma happy path đơn lẻ không đạt bàn giao.
- Dùng server hints và quyền hiện tại; API vẫn là authority. Role matrix chỉ để điều hướng; không suy permission từ route hay user role đơn lẻ.
- Không tự thêm API inbox/search/notification. Inbox ghép các danh sách canonical có filter hợp lệ; không cộng thành global total.

## Phân biệt hoàn tất

Blueprint hoàn tất nghĩa yêu cầu thiết kế và task đã giao rõ. Figma hoàn tất cần actual link/file, frame IDs, prototype và checklist designer. FE code hoàn tất cần source/client/build và review. Nghiệm thu FE cần real browser/API/backend evidence riêng; mock/prototype không thay kết quả đó. Theo owner, testing vẫn là giai đoạn sau, không được tự chạy khi nhận task thiết kế/code.

## Baseline và đổi contract

Nếu baseline đã đổi, agent phải so diff của A/B/contracts trước khi làm; không tự nâng React/TS/Vite/generator ngoài pin. Nếu contract thiếu dữ liệu cần cho thiết kế, dùng trạng thái chưa hỗ trợ/không hiển thị trường đó và báo planner; không tạo con số/API giả. Chỉ hỏi owner khi cần quyết định sản phẩm/canonical thực sự; quyết định bố cục thường nhật làm theo blueprint.
