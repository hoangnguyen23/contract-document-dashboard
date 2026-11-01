# Sitemap, screen ownership and access

Các route dưới đây là route UI đề xuất, không phải thêm API. Luôn resolve object scope trên server trước khi render dữ liệu. BANK_ADMIN không có quyền đọc mọi nghiệp vụ; supplemental SENSITIVE_READ/EXPORT không thay role. User nhiều role dùng cùng shell, không có nút “đóng vai” cấp quyền.

| ID | UI route | Screen / mục đích | Người sử dụng dự kiến | Tranche |
|---|---|---|---|---|
| S01 | /login | Đăng nhập/session expired | Tất cả | F1 |
| S02 | / | Trang tổng quan/công việc theo scope | Role có query phù hợp | F1 |
| S03 | /relationships | Quan hệ khách hàng, tìm/lọc đúng scope | MAKER/CHECKER/MANAGER/AUDITOR theo API; admin tối thiểu | F1 |
| S04 | /relationships/:id | Relationship360, contacts/pending contacts/nguồn liên quan | Theo current object access | F1 |
| S05 | /dossiers | Danh sách hồ sơ | Business roles đúng scope | F1 |
| S06 | /dossiers/new?relationshipId= | Tạo hồ sơ cho relationship có quyền | Assigned MAKER | F1 |
| S07 | /dossiers/:id/edit | Wizard chỉnh draft | Assigned MAKER khi server cho phép | F1 |
| S08 | /dossiers/:id | Detail hồ sơ, Revision và timeline | Theo object access | F1 |
| S09 | /reviews | Queue Round, phân biệt L1/L2/target kind | CHECKER/MANAGER theo phân công | F1 |
| S10 | /rounds/:id | Immutable review snapshot/workbench | Reviewer đủ điều kiện/current scope | F1 |
| S11 | /policies | Policy list, global namespace riêng | POLICY_AUTHOR/POLICY_REVIEWER | F2 |
| S12 | /policies/:id | Policy studio, simulate/regression/publication | Role tương ứng, contributor independence | F2 |
| S13 | /contracts | Danh sách hợp đồng | Business roles đúng scope | F3 |
| S14 | /contracts/:id | Contract360 + version timeline | Theo object access | F3 |
| S15 | /signing-packets/:id | Báo cáo ký/independent verification | MAKER nhập; CHECKER độc lập xác minh | F3 |
| S16 | /changes | Danh sách thay đổi/gia hạn | Theo object access | F4 |
| S17 | /changes/:id | Base/current/proposed diff, review/commit/rebase | Theo current hints và phân công | F4 |
| S18 | /notices | Danh sách thông báo | Theo scope và role | F4 |
| S19 | /notices/:id | Recipient snapshot/review/capture/receipt/ACK | MAKER và reviewer độc lập | F4 |
| S20 | /obligations | Nghĩa vụ/deadline/evidence queue | Theo object access | F5 |
| S21 | /obligations/:id | Bằng chứng, extension/waiver/breach history | Role/action theo API | F5 |
| S22 | /incidents | Incident queue | Theo object access | F5 |
| S23 | /incidents/:id | Receipt/trigger/remediation/resolution | Role/action theo API | F5 |
| S24 | /controls | Service control request queue | Theo scope | F5 |
| S25 | /controls/:id | Source/approval/external acknowledgement | Role/action theo API | F5 |
| S26 | /data-requests | Privacy queue | DATA_OFFICER và requester theo API | F6 |
| S27 | /data-requests/:id | Record manifest/holds/decision/execute | Independent officer/action theo API | F6 |
| S28 | /audit | Audit/filter/export | Scoped AUDITOR/authorized role + EXPORT cho xuất | F6 |
| S29 | /files | File metadata/parent/scan/download | Theo parent scope + representation grant | F1/F6 |
| S30 | /jobs | Queue/attempt/error/source/retry | Theo parent scope/action | F1/F6 |
| S31 | /admin/users | User role/scope/grants/lock | BANK_ADMIN | F2 |
| S32 | /admin/branches | Branch metadata | BANK_ADMIN | F2 |
| S33 | /parties | Party metadata tối thiểu/create/link | BANK_ADMIN theo API | F2 |

## Điều hướng

Sidebar nhóm: Tổng quan; Khách hàng & hồ sơ; Phê duyệt; Hợp đồng; Thay đổi & thông báo; Nghĩa vụ & kiểm soát; Quản trị dữ liệu; Vận hành; Quản trị hệ thống. Chỉ hiện link nhóm có khả năng sử dụng, nhưng vẫn có route-level loading/access handling; không coi ẩn link là security. Chính sách là nhóm riêng cho policy roles, không nằm trong business branch queues.

Topbar: breadcrumb, nhãn scope hiện tại, chọn branch nếu danh sách authorized phù hợp, menu người dùng/logout. Không search toàn ngân hàng, không bell notification count giả, không nút switch-role. Filter branch chỉ dùng branch user được phép; không dùng admin-only listBranchs để dựng branch selector cho business user. Nếu Session không có tên branch, dùng branch ID rút gọn/nhãn nguồn được cấp; báo planner nếu muốn bổ sung dữ liệu.

URL giữ filter/sort/tab được allowlist, không lưu tên/email/CCCD/token/password trong URL. Cursor là opaque; thay filter/branch phải bỏ cursor và tải trang đầu. Không hỗ trợ tùy ý sort nếu canonical không có. Sidebar không hiển thị số total chưa được cấp API.

## Home/inbox không có endpoint riêng

Home sử dụng getDashboard khi role có quyền; các queue dùng listRounds/listDossiers/listObligations/listControlRequests với query thực có trong reference. Nếu API không có filter assigned-to-me cho một danh sách, không giả server đã lọc và không lấy toàn hệ thống về lọc client. Chỉ dựng module home mà current user thực sự có quyền đọc; admin/policy có home tối thiểu liên kết tác vụ phù hợp. Ghi rõ thời điểm asOf và lag; không coi các request khác nhau là một snapshot chung.

## Prototype paths bắt buộc

P1 MAKER login→relationship→draft→upload SCANNING→CLEAN→save→submit.
P2 CHECKER assigned review→snapshot/evidence assessment→APPROVE hoặc RETURN.
P3 MANAGER L2→final approval→PREPARED/render job pending; không autoACTIVE.
P4 upload fail→retry allowed→pending; P5 stale save→refresh/retain unsaved edits; P6 session expired→login→khôi phục draft UI nhưng không tự gửi command.
P7 ký/authority độc lập→readiness/hold→scheduled/active tách service. P8 Change conflict/rebase. P9 ACK recorder bị loại VERIFY_ACK. P10 hold/disposition incomplete.
