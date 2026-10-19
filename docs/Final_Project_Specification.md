# Final Project Specification

**Contract Governance — quản trị hồ sơ và hợp đồng thu hộ doanh nghiệp bằng tài khoản định danh**

| Thuộc tính | Giá trị |
|---|---|
| Mã tài liệu / phiên bản | A / 1.3.0 |
| Ngày chốt thiết kế | 11/10/2026 |
| Trạng thái | FINAL DESIGN BASELINE — owner đã chấp thuận hướng sản phẩm; chưa triển khai ứng dụng |
| Thẩm quyền | Nguồn sự thật về phạm vi, ý nghĩa dữ liệu, quy tắc, trạng thái, quyền và kết quả nghiệm thu |
| Tài liệu đi cùng | [FE/BE Shared Development Contract](FE_BE_Shared_Development_Contract.md), B / 1.3.0 |
| Đầu ra chính thức | Chính xác hai tài liệu A và B; các schema/client/fixture được trích xuất khi triển khai là sản phẩm phát sinh trong repository |

## 1. Sản phẩm và quyết định đã chốt

Sản phẩm nội bộ cho một ngân hàng giả lập giúp quản trị thỏa thuận **thu hộ doanh nghiệp qua tài khoản định danh tham chiếu bên ngoài (VA)**. Bên trả tiền chủ động chuyển tiền; hệ thống quản lý hồ sơ, điều khoản, thẩm quyền, xét duyệt, bằng chứng ký, thời điểm áp dụng và nghĩa vụ còn tồn. Hệ thống không nhận tiền, không mở tài khoản, không cấp VA và không tự trích nợ người trả tiền.

Điểm nhấn là một chuỗi quyết định có thể giải thích và tái kiểm tra: **facts + evidence + policy version → independent approval → exact approved artifact → signing evidence → scheduled effect → contractual exception/closeout**. Người kiểm toán truy từ điều khoản đang ghi nhận hiệu lực về bản hồ sơ, file, quy tắc và người quyết định; giao diện hiển thị nguyên nhân được phép/bị chặn thay vì chỉ hiển thị nút duyệt.

Owner đã yêu cầu Backend Java, độ khó nghiệp vụ cao và đối chiếu tình huống/pháp luật Việt Nam; sau đề xuất điều chỉnh theo nghiên cứu, owner nói **“Tôi chấp thuận”**. Phạm vi bốn nhóm liên kết dưới đây đã được chấp thuận. Các con số, tên trạng thái và giới hạn demo cụ thể trong A là quyết định thiết kế hoàn tất trong hướng đó, không phải lời khẳng định owner tự chỉ định từng tham số hay pháp luật yêu cầu chúng.

| Mức | Nội dung |
|---|---|
| Bắt buộc | Hồ sơ–bằng chứng–thẩm quyền–L1/L2; policy editor có kiểu, mô phỏng và xuất bản độc lập; hợp đồng gốc, phụ lục/gia hạn/thông báo phí, lịch thay đổi và rebase; thông báo, incident, nghĩa vụ, yêu cầu kiểm soát dịch vụ, closeout; users/branches, dashboard/drilldown, audit, xử lý dữ liệu cá nhân mô phỏng; sinh PDF, kho file riêng, scan, durable jobs; UI tiếng Việt và kiểm thử tích hợp thật |
| Mở rộng có điều kiện, không nằm trong DoD 1.0 | Adapter object storage khác; adapter gửi email bên ngoài; SSO; lịch nghỉ thực tế được rà soát; nhiều mẫu/loại hợp đồng. Mỗi mở rộng phải có thay đổi A/B, chủ thể/basis dữ liệu và kiểm thử tương ứng trước khi đưa vào baseline |
| Ngoài phạm vi | Thu/chi/hạch toán/settlement tiền thật; mở tài khoản/cấp VA; core banking, CIC/AML/KYC thực; acquiring/định loại pháp lý QR; tự động debit; tín dụng/bảo hiểm/tài sản bảo đảm; chữ ký số pháp lý hoặc kết luận hợp đồng vô hiệu; AI quyết định; arbitrary BPMN/DSL/workflow/delegation; gửi thông báo ra người thật; chứng nhận tuân thủ pháp luật |

Một tổ chức, hai chi nhánh `B01`, `B02`, khách hàng thụ hưởng là doanh nghiệp. Dữ liệu cá nhân, tài khoản tham chiếu và file đều giả lập. Chưa biết deadline, số giờ/tuần, tiêu chí bắt buộc của trường hoặc repository; không có cam kết ngày hoàn thành. Thay đổi phạm vi lớn phải trình owner; các lựa chọn thư viện/schema/chi tiết UI được xử lý theo cơ chế B.

### 1.1. Kế thừa, thay đổi và nguồn yêu cầu

| Nguồn | Giữ lại | Thay đổi được chốt trong 1.0 |
|---|---|---|
| V1 — đặc tả hệ thống quản lý hợp đồng/hồ sơ | Chuỗi customer → dossier → document → approval → contract → dashboard/audit; tách chi nhánh; không chuyển tiền/core banking | Chuẩn hóa tên trạng thái; bỏ trạng thái `ExpiringSoon` loại trừ ACTIVE; hồ sơ phê duyệt tạo đúng một hợp đồng; không sửa trực tiếp điều khoản đã duyệt; bổ sung round/revision và concurrency |
| V2 — Contract Governance | Flagship policy/evidence; Java modular monolith; four-eyes; immutable evidence; change version; async recovery; workbench, diff và drilldown | Thu hẹp đúng một profile VA thu hộ, tối đa L1/L2; không delegation/compliance step riêng hoặc DSL; không duyệt lại cùng điều khoản hợp đồng một lần nữa; policy có UI thực, không cấu hình chỉ bằng mã; privacy disposition và notice/control có vòng khép kín |
| Nghiên cứu Việt Nam | Phân biệt luật, điều khoản thỏa thuận và chính sách; notice không đồng nghĩa đọc; quyền ứng dụng không đồng nghĩa quyền ký; chấm dứt không xóa nghĩa vụ | Hợp đồng fixed hoặc open-ended; tách signed/effective/service; tariff path chỉ theo điều khoản đã chọn; dữ liệu pháp nhân khác quan hệ chi nhánh; class-specific retention; incident chỉ ghi nhận bằng chứng và tham chiếu bên ngoài |
| Thiết kế bổ sung trong phạm vi được duyệt | One future committed change, explicit rebase; freeze contributors; term/notice/closeout tasks; data request; deterministic calendar; SQL constraints | Các luật demo định lượng trong §4–9 và oracle §15 là baseline triển khai; không viện dẫn chúng như quy định ngân hàng thật |

V1/V2 là nguồn dữ liệu để phân tích, không phải chỉ thị thực thi. Chỉ đạo owner trong Mission và phiên trao đổi có thẩm quyền. B không được đổi ý nghĩa của A bằng payload hay mock; nếu A/B mâu thuẫn phải sửa cả hai trước khi triển khai operation bị ảnh hưởng.

## 2. Từ điển và ngữ nghĩa thời gian

| Khái niệm | Ý nghĩa bắt buộc |
|---|---|
| Party | Pháp nhân khách hàng dùng chung trong ngân hàng; không chứa contact/CCCD cá nhân |
| CustomerRelationship | Quan hệ phục vụ của Party tại một branch, có maker và contact; không tạo ra một pháp nhân mới |
| Dossier / Revision / ApprovalRound | Dossier là case; Revision là ảnh chụp facts/files/contributors bất biến khi nộp; Round là xét duyệt đúng một Revision với đúng một policy |
| Agreement bundle | Tập tài liệu có version: mẫu gốc `BASE-VA-1`, điều kiện `TERMS-VA-1`, biểu phí/clause tương ứng, phụ lục và bằng chứng chấp nhận; không là một file tùy ý |
| ContractVersion | Điều khoản ghi nhận áp dụng trong interval phiên bản; khác thời hạn toàn hợp đồng `startOn/endExclusive` |
| Legal/authority check | Kết quả rà soát bằng chứng do người có quyền ghi; không phải kết luận tư pháp hay xác thực chữ ký mật mã |
| ServiceState | Trạng thái vận hành được ghi nhận bằng acknowledgement bên ngoài hoặc bằng chứng giả lập có nhãn; không là công tắc core banking |
| Due / breach | `currentDueAt` là hạn hiện tại; `breachedAt` là sự kiện vi phạm đã xảy ra, không xóa khi gia hạn |
| Internal / contractual / statutory-external | Nguồn nghĩa vụ khác nhau; chỉ nghĩa vụ internal có thể được miễn/gia hạn bằng quyết định nội bộ thuần túy |

Business date theo `Asia/Ho_Chi_Minh`; timestamp UTC có mili giây. Interval áp dụng là `[start, end)`: `startOn` bao gồm, `endExclusive` không bao gồm; hợp đồng FIXED hết hạn lúc 00:00 ngày endExclusive tại Việt Nam. UI phải ghi “Hết hiệu lực từ 00:00 ngày …”, không ghi ngày cuối cùng một cách mơ hồ. `signedAt`, `receiptAt`, `dispatchAt`, `effectiveAt`, `observedAt`, `createdAt` không được dùng thay cho nhau.

Calendar `DEMO_CAL_2027-v1`: làm việc thứ Hai–thứ Sáu, nghỉ giả lập thêm `2027-01-04`. Hạn N working days loại ngày nhận/nộp, đếm N ngày làm việc kế tiếp và kết thúc 17:00 local. Calendar/version được chốt cùng hạn. Đây không phải lịch nghỉ pháp định Việt Nam. Hạn pháp luật được ghi từ nguồn ngoài bằng `externalDueAt`, source/rule basis; demo không suy ra tất cả thời hạn luật bằng calendar này. Test clock `2027-01-05T02:00:00.000Z`; production dùng system clock, không có API thay clock.

## 3. Vai trò, phạm vi và trách nhiệm

Mỗi User có ID bền vững, active flag, roles và branch scopes rõ ràng. Thay role, khóa user hoặc đổi scope có hiệu lực ở request kế tiếp; session bị ảnh hưởng phải mất quyền. Một role không tự cấp mọi branch. Lọc branch diễn ra trong query trước count/list/export, không tải toàn ngân hàng rồi ẩn ở FE.

| Role | Được thực hiện trong scope | Không được suy ra từ role |
|---|---|---|
| BANK_ADMIN | Users/roles/scopes, branches, Party metadata tối thiểu, tạo/liên kết relationship; cấp `SENSITIVE_READ` hoặc `EXPORT` bằng quyết định audit có lý do | Không tự duyệt nghiệp vụ, ký, xác minh file nhạy cảm hoặc thấy toàn bộ hồ sơ |
| MAKER | Dossier/change/notice/incident/control/task của relationship được giao; nhập facts, upload, submit/withdraw, gửi evidence và contact update | Không duyệt/verify target có mình trong contributors/requester; không tìm taxId toàn ngân hàng |
| CHECKER | L1, file/authority/signing/contact/notice/task verification, incident verification, xác nhận control phù hợp | Không sửa facts để rồi duyệt; không làm L2 hoặc override luật |
| MANAGER | L2; reassignment reviewer; quyết định extension/waiver; termination L2, cancel scheduled change | Không làm L1 và L2 cùng target; không miễn sign/readiness/legal guard |
| POLICY_AUTHOR | Draft/clone policy, simulate, chạy regression, submit publish | Không xuất bản policy mình hoặc contributor đã sửa |
| POLICY_REVIEWER | Rà soát report/config hash, approve/reject policy và effective schedule | Không sửa config rồi tự approve; không được role này mà có quyền đọc business branch khác |
| DATA_OFFICER | Data requests, record-class/basis/hold review, disposition; nhạy cảm chỉ khi có explicit grant | Không coi erasure request là quyền xóa mọi business history; không decide request do chính mình tạo |
| AUDITOR | Scoped metadata, timeline/audit, bản redacted; export chỉ khi được cấp `EXPORT` | Không mutate; không full-image/sensitive view theo mặc định |

Hai supplemental permissions là `SENSITIVE_READ` và `EXPORT`, có scope riêng và không thay thế role nghiệp vụ. Full sensitive content cần role/action thích hợp **và** SENSITIVE_READ; export cần scope/action **và** EXPORT, bản xuất vẫn redacted. Totals, history, job, search và file-content checks tuân cùng object scope. Không có permission “ADMIN_ALL”.

### 3.1. SoD và phân công

Contributor là mọi người đã sửa facts, terms hoặc evidence của target trước khi nộp; tập ID tích lũy theo target và carry forward khi rebase. Requester cũng bị loại khỏi approve/verify. Đổi role, chuyển maker, khóa/mở tài khoản, đổi người duyệt không xóa ID. L1 và L2 phải khác nhau. Người reviewer nhận bổ sung/comment không trở thành contributor, trừ khi thực sự chỉnh input thay maker; reviewer không có action sửa submitted input.

Reviewer được chỉ định khi submit: L1 là CHECKER, L2 là MANAGER nếu route cần. Backend kiểm tra scope/active/SoD tại submit và tại decision. Nếu không còn eligible reviewer, round giữ OPEN với blocker “Thiếu người đủ điều kiện”; eligible MANAGER không phải contributor reassign, ghi old/new/reason. Không auto-delegate, không self-assign vượt SoD. Reassignment không reset dueAt, receiptAt hoặc decision cũ. Maker reassignment thay người chịu trách nhiệm tương lai và audit; không đổi historic owner/contributors. Bank admin có thể cấp quyền nhưng cấp quyền không tạo reviewer hợp lệ nếu đã là contributor.

### 3.2. Customer và contact

Party có normalized taxId giả lập 10 hoặc 13 chữ số, tên doanh nghiệp; unique bankwide. Relationship unique `(branchId, partyId)`, có assignedMakerId, version và confirmed contact. Maker chỉ thấy Party qua relationship có scope; khi cần liên kết Party ở branch khác, gửi yêu cầu cho admin, không có global taxId enumeration. Duplicate không tạo Party thứ hai, admin trả relationship đã có hoặc tạo quan hệ branch mới. Không merge/transfer Party trong 1.0.

Contact gồm personName, roleLabel, email, phone và purpose; synthetic only. Maker đề nghị update; ContactRequest `PENDING → APPROVED/REJECTED` bởi independent CHECKER trên exact snapshot. Approve kiểm tra expectedRelationshipVersion và request version; request cũ không ghi đè contact vừa được xác nhận. Muốn sửa request đã gửi phải tạo request mới. Confirmed contact chỉ đổi khi approve. Pending edit không được dùng cho notice mới. Notice đã phát hành giữ recipient snapshot cũ. Party/relationship edit không viết lại Revision/ContractVersion/Notice. Đổi identity/account-reference/representation liên quan readiness phải tạo explicit blocker và đánh giá lại như §6.

## 4. Policy có kiểm soát và nguồn quy tắc

Mỗi evaluation ghi `sourceKind = INTERNAL_POLICY | HARD_INVARIANT | REVIEWED_EXTERNAL_CONDITION`, rule ID, input facts, PASS/BLOCK/REVIEW_REQUIRED, reason và version/source evidence. Không trình diễn nút “tuân thủ pháp luật 100%”. Hard guard về access/SoD/file integrity/immutable version/current mandatory condition không thể tắt trong editor. Rà soát điều kiện bên ngoài là record có basis/version/ngày áp dụng và blocking flag do người có quyền ghi; không xây generic legal-rule engine.

### 4.1. PolicyConfig hữu hạn

| Field | Type / ràng buộc | P1 / P2 |
|---|---|---|
| minFeeVnd / referenceFeeVnd / maxFeeVnd | Integer decimal strings, `0 ≤ min ≤ reference ≤ max ≤ 9999999999` | P1 `500/1000/5000`; P2 `500/1200/5000` |
| allowedTermKinds | Nonempty subset FIXED, OPEN_ENDED | Cả hai |
| minFixedTermDays / maxFixedTermDays | Integers, `1 ≤ min ≤ max ≤ 3650`; sử dụng khi FIXED | `30/1095` |
| reviewDueWorkingDays | Integer 1..10 theo demo calendar | `3` |
| requiredDocumentPurposes | Unique subset SERVICE_APPLICATION, CORPORATE_ID, REPRESENTATION_BASIS, SERVICE_TERMS, IDENTITY_IMAGE | Base trio đầu luôn bắt buộc; thêm hai purpose sau tùy phiên bản |
| pricing route | Không cho custom graph/operator; fixed formula dưới đây | Fee dưới reference → L1_L2; từ reference → L1 |

Fee dưới min hoặc trên max bị BLOCK, không override bằng L2. Ở min được nộp với L1_L2 khi min < reference; nếu min = reference thì L1. Ở reference được L1. Ví dụ P1: `499` BLOCK, `500` L1_L2, `999` L1_L2, `1000` L1, `5000` L1, `5001` BLOCK. P2 fee `1000` thành L1_L2. Fee payer UNAGREED chỉ được lưu draft, không submit. Simulation cũng trả các hard failures và thiếu bằng chứng, không chỉ route.

Corpus hữu hạn dùng oracle độc lập theo từng cấu hình: mọi vector FIXED_* buộc termKind=FIXED và endExclusive=startOn+số ngày thay thế, bất kể base; fee<reference cấp L2 MANAGER active đúng scope độc lập L1/contributors, route L1 dùng L2=null (trừ SAME_L1_L2 cố ý ghi đè cả hai ID); base chọn FIXED nếu được phép, nếu không chọn OPEN_ENDED với endExclusive=null; fee=reference. Expected route của FEE_AT_MIN là L1 khi min=reference, ngược lại L1_L2; FEE_BELOW_REF bị BLOCKED khi reference-1<min, ngược lại L1_L2. Các vector FIXED_AT_MIN/FIXED_AT_MAX chỉ L1 nếu FIXED được phép; OPEN_NULL_END chỉ L1 nếu OPEN_ENDED được phép, nếu không BLOCKED. Các vector ngoài miền input (fee=-1 hoặc 10000000000, fixedDays=0 hoặc 3651) vẫn BLOCKED; không bỏ vector và không clamp giá trị. SoD L1=L2 luôn BLOCKED khi cả hai ID có mặt, kể cả route chỉ cần L1. Expected được tính từ các điều kiện hữu hạn đã viết này, độc lập với kết quả evaluator; không copy actual thành expected.

Các quy tắc này là chính sách giả lập, không phải biểu phí/thẩm quyền của ngân hàng Việt Nam. Người triển khai không được thêm expression evaluator, script execution hoặc approval step thứ ba.

### 4.2. Policy lifecycle

| Từ → đến | Actor / guard / hiệu ứng |
|---|---|
| DRAFT → DRAFT | POLICY_AUTHOR edit typed config; tăng version, thêm contributor; invalidates old regression report |
| DRAFT → IN_REVIEW | Submit exact config hash + passing regression report + requested effectiveFrom; field validation, boundary/missing-file/SoD corpus đạt; source author và contributors được freeze |
| IN_REVIEW → SCHEDULED | Independent POLICY_REVIEWER approve report khớp exact hash; effectiveFrom 00:00 local từ ngày kế tiếp trở đi; lock scheduleVersion, tối đa một future policy; close active effectiveTo tại start mới |
| IN_REVIEW → REJECTED | Reviewer reject có reason; immutable; clone new DRAFT nếu muốn sửa |
| SCHEDULED → ACTIVE | Worker đúng start, idempotent; policy active cũ → RETIRED trong cùng transaction |
| ACTIVE → RETIRED | Chỉ khi policy kế tiếp thành ACTIVE, không tạo gap; historic policy vẫn truy cập được |
| REJECTED/RETIRED → DRAFT mới | Clone tạo ID/version mới, không khôi phục state bản cũ |

Schedule mới chưa có policy có counter=0; publication đầu tiên dùng expectedScheduleVersion=0 và effectiveFrom từ ngày mai trở đi. Trước lần ACTIVE đầu, không có applicable policy thì submit nghiệp vụ fail closed, không nạp fixture ACTIVE làm kết quả live. Không sửa/cancel SCHEDULED hoặc ACTIVE trong baseline; thay đổi bằng version kế tiếp, chờ slot future trống. Nếu submit/review cần sửa config, reject rồi clone; không auto-publish khi report cũ còn xanh. Simulation read-only với explicit facts/file flags và version đã chọn; không tạo dossier, approval hay effect. Policy publication không đổi điều khoản khách đã chấp nhận.

Round đang OPEN giữ policy lúc submit. RETURNED rồi resubmit là round mới dùng policy đang áp dụng tại lần nộp mới. Điều kiện bắt buộc bên ngoài hoặc thẩm quyền hiện tại thay đổi có thể chặn decision/effect dù old policy PASS; ghi evaluation mới và preserve old snapshot, không âm thầm sửa P1.

## 5. Hồ sơ, evidence và xét duyệt

### 5.1. Facts bắt buộc

| Nhóm | Input / ý nghĩa |
|---|---|
| Customer/service | relationshipId, branchId suy từ relationship; profile VA_COLLECTION; externalAccountReference giả lập không là lệnh mở tài khoản; mô tả phạm vi dịch vụ fixed template |
| Terms | currency VND; feeVnd integer string; feePayer BENEFICIARY hoặc UNAGREED draft; pricingMode NEGOTIATED_FIXED/STANDARD_TARIFF; startOn; termKind; endExclusive required với FIXED, null với OPEN_ENDED; clauseVersion liên quan |
| Bundle | templateVersion `BASE-VA-1`, termsVersion `TERMS-VA-1`; điều khoản phí `NEGOTIATED-VA-1` hoặc `TARIFF-ACK-7D-1`; ordered document references và explicit precedence |
| Representation | representationBasisType LEGAL_REPRESENTATIVE/POWER_OF_ATTORNEY; representative name/title; authorityScope; validFrom/validTo nullable nếu basis không ghi hạn; revocation/current-check result; sourceFileIds; check evidence và checkedAt |
| Contacts | confirmed contact snapshot ID/version + recipient fields; không lấy pending contact |
| Evidence | purpose/fileId/hash/source; base trio CLEAN trước submit; identity image optional theo policy; contributor IDs, authoritative file metadata |

Precedence của **bundle demo**: phụ lục đã áp dụng mới hơn chỉ thay field trong whitelist được ghi rõ → điều khoản riêng trong hợp đồng gốc → `TERMS-VA-1` → tariff clause được dẫn chiếu. Thông báo phí hợp lệ chỉ thay fee trong STANDARD_TARIFF; không thay thời hạn/authority/identity. File tham khảo không có precedence. Giao diện hiển thị origin của từng term. Đây là quyết định của mẫu giả lập, không suy ra từ chỗ trống trong điều khoản ACB.

Bootstrap tạo draft/parent trước, rồi upload/scan/attach. Primary representation source có thể null ở draft để tránh vòng “cần fileId mới tạo được dossier nhưng cần dossierId mới upload”; source bắt buộc nonnull/CLEAN tại SUBMIT. Clone tạo association mới và human checks mới; có thể dedupe physical immutable bytes bên trong storage nhưng không reuse scope/quyết định cũ.

FIXED có `endExclusive > startOn` và số ngày calendar trong giới hạn policy. OPEN_ENDED có endExclusive null. Fee nhập 0..9999999999 nhưng giá trị ngoài policy không submit. Ngày start từ ngày local kế tiếp trở đi khi submit; trước khi active, thiếu sign/readiness at boundary không tự dời ngày.

### 5.2. Dossier lifecycle

| Từ → đến | Action / actor | Guard và kết quả |
|---|---|---|
| DRAFT/RETURNED → cùng state | SAVE — assigned MAKER | Mutable draft only; version check; append contributors; replacing file dùng ID mới |
| DRAFT/RETURNED → IN_REVIEW | SUBMIT — MAKER | Terms/checklist/current guard pass; required files CLEAN; select eligible reviewers; freeze Revision, evidence/config hashes và evaluation; tạo OPEN round, L1 PENDING/L2 WAITING nếu cần, dueAt |
| IN_REVIEW → RETURNED | RETURN — pending reviewer | Reason + targeted correction items; close round RETURNED, pending/waiting steps CANCELLED, drafts lần sau có revision mới |
| IN_REVIEW → REJECTED | REJECT — pending reviewer | Reason; terminal dossier/round; giữ submitted evidence; không sửa/resubmit cùng dossier |
| IN_REVIEW → DRAFT | WITHDRAW — assigned MAKER | Reason; round CANCELLED và pending/waiting steps CANCELLED; lịch sử APPROVED step cũ giữ nguyên; không còn live approval |
| DRAFT/RETURNED → CANCELLED | CANCEL — assigned MAKER | Reason; không xóa Revision/history/file |
| IN_REVIEW → APPROVED | Last required APPROVE — pending reviewer | All required evidence human VERIFIED và authority pass; current access/SoD/current guard; atomically approve round, tạo một PREPARED contract và PDF job |
| APPROVED → COMPLETED | System first ACTIVE | Hoàn tất onboarding lần đầu, không chờ closeout hoặc mọi task hợp đồng; COMPLETED không ngăn workflow hợp đồng sau đó |
| REJECTED/CANCELLED → DRAFT mới | CLONE — MAKER có scope | sourceDossierId; fresh file association, current authority/check, new contributors/new round; không mang approvals |

APPROVED không sửa điều khoản trực tiếp. Phê duyệt hồ sơ đã bao gồm chính xác terms và bundle; PDF là render của snapshot đó, nên không thêm một vòng duyệt terms giống hệt lần thứ hai. Human artifact/sign check vẫn bắt buộc để phát hiện sai bản sinh hoặc ký sai.

Round OPEN → APPROVED/RETURNED/REJECTED/CANCELLED; không reopen round cũ. Step WAITING → PENDING sau L1 APPROVED; PENDING → APPROVED/RETURNED/REJECTED; các step chưa quyết định → CANCELLED khi round đóng bất thường. Step APPROVED giữ nguyên lịch sử dù withdraw/return ở bước sau. L2 không được quyết định trước L1. Decision unique trên step, duplicate/race không sinh hai quyết định. L1 xem và ghi PASS/FAIL/NEEDS_CLARIFICATION cho mỗi required evidence + authority; chỉ PASS đủ mới APPROVE. File CLEAN không thay human verification.

Reviewer không đợi download nhạy cảm nếu không có permission: task hiển thị thiếu người có SENSITIVE_READ; manager phải phân người đủ điều kiện hoặc policy không yêu cầu IDENTITY_IMAGE. Không tự giảm checklist để xử lý thiếu quyền. Deadline review chỉ là internal SLA, không tự reject hay auto-approve khi quá hạn.

### 5.3. Workbench phê duyệt

```text
+------------------------------------------------------------------------+
| Hồ sơ HS-… | IN_REVIEW | Revision 3 | P1 | L1 | Hạn: … | Branch B01     |
+-------------------------+----------------------+-----------------------+
| Facts đã nộp            | Evidence đúng hash   | Rule evaluations      |
| Doanh nghiệp/quan hệ    | purpose / scan       | minFee PASS           |
| Fee/payer/term/clause   | human verification   | authority REVIEW      |
| Authority/contact      | preview redacted/full| missing-file BLOCK    |
+-------------------------+----------------------+-----------------------+
| Timeline: ai sửa → ai nộp → verify → return/approve; contributor list    |
+------------------------------------------------------------------------+
| Lý do / correction items             [Trả bổ sung] [Từ chối] [Duyệt L1]|
+------------------------------------------------------------------------+
```

Facts read-only khi review; chính sách và file version luôn hiện cùng decision. Button disabled có reason từ allowedActions; BE vẫn recheck. 412 stale mở reload/diff dialog, giữ draft nhập riêng để người dùng đối chiếu; không tự resend quyết định với ETag mới. 409 conflict hiển thị blocker, không giả chuyển state thành thành công.

## 6. Hợp đồng gốc, ký và timeline

### 6.1. Artifact và signing readiness

Approval cuối tạo Contract PREPARED, original approvedSnapshotId và job render PDF duy nhất. Job phải lấy snapshot/template version đã chốt, không lấy customer/policy hiện tại. Artifact có fileId/hash/rendererVersion/generatedAt; failed job hiển thị “Chưa có bản hợp đồng”, cho retry có kiểm tra version, không tạo Contract thứ hai. Không coi file server-generated CLEAN là đã được human artifact/sign verification.

SigningPacket chứa target contract/change, approvedArtifactId/hash, signedCopyIds, reported signedAt của hai bên, signingMethod `PAPER_SCAN` hoặc `EXTERNALLY_SIGNED`, signer/basis/source check cho ngân hàng và doanh nghiệp. Maker submit immutable packet PENDING; independent CHECKER VERIFY → VERIFIED hoặc RETURN → RETURNED với correction reason. Sửa bằng packet mới; packet VERIFIED không editable. VERIFY xác nhận bản ký tương ứng artifact, scope/identity/dates/representation evidence và current guards; không hứa xác thực mật mã hay hiệu lực pháp lý cuối cùng.

Reported signedAt không sau server verification time. Authority basis phải cover party/action, có `validFrom ≤ signedAt < validUntil` nếu có hạn; known revocation không được trước/bằng signedAt. Readiness server ghi nhận chỉ READY nếu đủ artifact, verified signing packet, external account/service-readiness confirmation, current mandatory checks và không hold. Readiness enum `PENDING`, `READY`, `REVIEW_REQUIRED`; các blocker và evidence đi kèm riêng.

**On-time readiness** là `readinessVerifiedAt < startOn 00:00 local`. Khách ký từ trước nhưng app verify sau boundary vẫn không đạt. Không backdate bằng reported signedAt. Late readiness yêu cầu cancel candidate, linked new dossier với ngày/terms mới, reapproval/render/re-sign. Nếu đã READY on time mà worker muộn, ghi event effectiveAt đúng ngày đã thỏa thuận và recordedAt thực tế; không bắt ký lại vì worker outage.

### 6.2. Contract lifecycle

| Từ → đến | Guard / actor / hiệu ứng |
|---|---|
| PREPARED → SCHEDULED | Independent CHECKER đã verify packet/readiness trước start boundary; start tương lai; không hold |
| SCHEDULED → ACTIVE | Contract-locked timeline worker, đủ on-time readiness/current guard; tạo initial ContractVersion, first ACTIVE làm Dossier COMPLETED; Service giữ NOT_STARTED |
| ACTIVE → ACTIVE | Valid scheduled Change APPLIED: phiên bản mới, không rewrite prior terms |
| ACTIVE → EXPIRED | FIXED boundary đến sau xử lý qualifying renewal; close authoritative interval, tạo closeout và STOP request nếu dịch vụ còn observed RUNNING/SUSPENDED |
| ACTIVE → TERMINATED | Termination control confirmed với ground/notice/stop acknowledgement; tạo closeout, giữ obligations/incident còn tồn |
| PREPARED/SCHEDULED → CANCELLED | Chưa từng ACTIVE: unsigned candidate do assigned MAKER cancel có reason; đã có external signature cần independent CHECKER cancellation evidence; Dossier → CANCELLED giữ approval history |

PREPARED không tự ACTIVE; EXPIRED/TERMINATED/CANCELLED không được ACTIVATE/renew/revive. Gửi hồ sơ linked mới nếu cần thỏa thuận lại. OPEN_ENDED không tự EXPIRED, không tạo renewal. ExpiringSoon là derived `Contract ACTIVE ∧ FIXED ∧ 0 < calendarDays(todayLocal,endExclusive) ≤ 30`; không là contract state.

Service enum `NOT_STARTED/RUNNING/SUSPENDED/STOPPED` là trục riêng. ACTIVE agreement có thể NOT_STARTED hoặc SUSPENDED. Expiry không giả stop đã thực hiện: service giữ observed state đến khi STOP ack, UI cho thấy contract EXPIRED nhưng “Dịch vụ chưa xác nhận dừng”. Không nói UI đã tắt giao dịch ngoài hệ thống.

Trước time-sensitive command và GET contract detail/signing readiness, BE chạy contract-locked `advanceTimeline`: qualifying renewal trước expiry, dùng business instant thật. Maintenance làm thay aggregate version thì request If-Match cũ trả 412; không tiếp tục command với snapshot stale. Lists/dashboard phản ánh materialized recorded states tại asOf và có `timelineProcessingLagSeconds`; không hứa mọi row tự trở thành legal/bank current state tức thời khi worker outage.

Material identity/account-reference/representation đổi trước activation đặt REVIEW_REQUIRED; không giải quyết bằng đổi file/checkbox trong candidate, phải linked new dossier. Fee/date cần đổi trước activation cũng cancel candidate/new dossier. `HOLD_READINESS` do CHECKER ghi reason/evidence có thể ngăn decision/commit/apply; người đã contribute vẫn được flag risk. `RELEASE_READINESS` cần CHECKER khác, không contributor, reviewed basis/evidence; không được hợp thức hóa altered material facts. ACTIVE hold chỉ cảnh báo và chặn future effect; giữ prior agreement history, không tự kết luận VOID hay tự suspend core service.

## 7. Thay đổi điều khoản, rebase và concurrency

### 7.1. Change facts và whitelist

Tạo Change chỉ với Contract ACTIVE; target giữ expectedBaseContractVersionId, expectedScheduleVersion, changeKind, effectiveOn, proposed fields, reason, contributors, evidence và policy evaluation. Multiple draft được phép. Giao diện luôn có diff ba cột **base lúc tạo / current / proposed**, tách contractual endExclusive khỏi version effectiveTo.

| Kind | Fields được thay | Điều kiện |
|---|---|---|
| AMENDMENT | feeVnd; endExclusive của FIXED | Không đổi identity, account/channel, startOn, termKind, pricingMode hoặc feePayer; effectiveOn từ ngày local kế tiếp, trước current end nếu FIXED; target end > effectiveOn; terms mới qua policy; OPEN_ENDED chỉ được fee |
| RENEWAL | endExclusive, có thể feeVnd | Chỉ FIXED ACTIVE; effectiveOn bằng current endExclusive; new end > old end; extension days nằm trong policy min/max; phải commit trước boundary; không revive expired |
| TARIFF_NOTICE | feeVnd duy nhất | Chỉ STANDARD_TARIFF có `TARIFF-ACK-7D-1`; không thay ngày/term/identity; đủ approved notice, dispatch và explicit acknowledgement theo §8 |

AMENDMENT đổi fixed end kiểm tra consolidated duration từ original startOn đến proposed endExclusive, không chỉ thời gian còn lại. RENEWAL kiểm tra extension days từ old end đến new end. OPEN_ENDED fee amendment giữ endExclusive null và bỏ fixed-end checks; không chuyển termKind bằng amendment.

NEGOTIATED_FIXED amendment/renewal có signed appendix. STANDARD_TARIFF vẫn có thể dùng negotiated signed amendment cho whitelist; tariff-notice path không thay mọi term. Việc publisher đổi policy reference fee không đổi fee hợp đồng.

### 7.2. Change lifecycle

| Từ → đến | Action / guard |
|---|---|
| DRAFT/RETURNED → cùng state | SAVE bởi assigned MAKER; only mutable fields, ETag |
| DRAFT/RETURNED → IN_REVIEW | SUBMIT; recheck current base/schedule, guards, required CLEAN files; freeze revision/evaluation; L1/L2 như dossier |
| IN_REVIEW → RETURNED/REJECTED | Pending reviewer, reason; close round; rejected terminal, muốn tiếp tục clone thành draft mới |
| IN_REVIEW → DRAFT | WITHDRAW maker reason, round CANCELLED, giữ quyết định cũ |
| IN_REVIEW → APPROVED | Last required approval; exact artifact PDF job; không mutate ContractVersion |
| APPROVED → READY | Negotiated appendix packet VERIFIED hoặc tariff notice ready; current mandatory guard và readiness đạt |
| READY → SCHEDULED | COMMIT atomic, `serverNow < effectiveOn 00:00 local`; exact current base; expectedScheduleVersion khớp; future slot rỗng; lưu committedScheduleVersion = expected+1, tăng Contract scheduleVersion |
| READY → CONFLICTED | Losing semantic base/slot commit; 409, không effect/contract version mới |
| SCHEDULED → APPLIED | Timeline worker locks Contract, current status/base/committed schedule/current flags pass; tạo đúng một version và effect event; tăng scheduleVersion lần nữa để release slot |
| SCHEDULED → CONFLICTED | Apply guard failure, no terms mutation; explicit blocker/audit; expiry vẫn xử lý nếu đến boundary |
| DRAFT/RETURNED/CONFLICTED → DRAFT | REBASE explicit current base; clear approvals/round readiness/artifact/sign/notice association, carry contributor IDs; new revision, không reuse old approvals |
| DRAFT/RETURNED/APPROVED/READY → CANCELLED | Maker cancel với reason trước effect; dependency job/notice cleanup theo §8/10, giữ lịch sử |
| SCHEDULED → CANCELLED | Maker request + independent MANAGER quyết định trước effect; tăng scheduleVersion, invalidate future signed association; không xóa signed/dispatched evidence |

REJECTED/CANCELLED/APPLIED không rebase cùng Change; tạo linked new draft. Mọi rebase cần lại evaluation/review/artifact/sign hoặc notice readiness. Base/schedule phải khớp tại submit và commit; existing future slot không cấp quyền ghi đè nó. Commit vẫn phải chứng minh slot trống. Chỉ một SCHEDULED nonterminal change trên Contract bằng constraint/lock; “kiểm tra trước rồi insert” ở controller là không đủ.

### 7.3. Boundary và thứ tự ưu tiên

Một hàm `advanceTimeline` khóa Contract là authority cho cả apply/expiry. Khi đến boundary endExclusive, qualifying SCHEDULED renewal được commit/READY trước boundary phải được xét trước expiry, bất kể job nào thức trước. Nếu worker muộn, qualifying renewal được materialize với effectiveAt tại boundary và recordedAt thực tế; đây không là hồi sinh agreement thực sự đã expired. Guard bị hold/current mandatory condition hoặc base/schedule mismatch → CONFLICTED, không renewal; expiry tiếp tục theo end hiện hành.

Termination confirmed atomic trước khi worker apply khóa được Contract thì future changes bị cancel, không áp dụng sau termination. Nếu apply đã commit trước termination confirmation, termination dùng current version và giữ effect history. Cancel scheduled được kiểm tra lại trước effect dưới cùng lock; trễ boundary trả conflict, không backdate cancellation. Hai worker retry cùng effect dùng unique event key, không tạo duplicate version.

## 8. Thông báo và bằng chứng nhận

### 8.1. Nội dung và lifecycle

Notice purpose `TARIFF_CHANGE`, `CONTRACTUAL_REMINDER`, `TERMINATION`, `GENERAL`; channel EMAIL core hoặc record ngoại sinh có source. Nội dung plain text từ structured template/exact approved artifact; không arbitrary HTML. Snapshot gồm target/reference, clause/version, subject/body/hash, confirmed contact version, recipient, attachments exact hashes và author/contributors. Không dùng pending contact hoặc mail do người nhập tự suy đoán.

| Từ → đến | Action / guard / ý nghĩa |
|---|---|
| DRAFT → DRAFT | MAKER edit text/template facts/recipient from confirmed contact; tăng version/contributors |
| DRAFT → IN_REVIEW | SUBMIT exact snapshot; independent CHECKER được chỉ định |
| IN_REVIEW → APPROVED/DRAFT/CANCELLED | Checker không author/contributor: APPROVE exact snapshot; RETURN → DRAFT để sửa revision mới; REJECT → CANCELLED. Old submitted snapshot/assessment giữ nguyên |
| APPROVED → QUEUED | QUEUE_DISPATCH, stable messageId, durable job; chưa nói đã gửi |
| QUEUED → DISPATCHED | Local capture transport chấp nhận exact payload idempotently; dispatchAt; không tự delivered/read/ack |
| QUEUED → FAILED | Job exhausts hoặc kết quả transport thất bại rõ; reason/retry visibility |
| FAILED → QUEUED | Retry cùng immutable payload/messageId sau guard; không tạo email business effect thứ hai |
| DISPATCHED → ACKNOWLEDGED | Explicit beneficiary acknowledgement evidence được independent checker verify; ackAt/source lưu riêng |
| DRAFT/IN_REVIEW/APPROVED/QUEUED/FAILED → CANCELLED | Trước transport acceptance, cancellation có reason; worker rechecks dependency/cancel state |

APPROVED onward nội dung bất biến; cần sửa tạo notice mới và submit/review lại. DISPATCHED/ACKNOWLEDGED không xóa/cancel lịch sử: giữ state, đánh dấu superseded và source linkage khi Change bị rebase/cancel. Cancelled/superseded Change không để unissued notice tiếp tục dispatch. Race dispatch/cancel phải có exact durable outcome; local transport stable messageId chống duplicate.

`dispatchAt` là transport accepted; `deliveredAt` là optional external proof; `deemedReceivedAt` có explicit contractual basis và human verification; `acknowledgedAt` là explicit acceptance evidence của beneficiary. Chúng không tự suy ra lẫn nhau. MAKER RECORD_ACK lưu immutable candidate có ackAt≤serverNow, evidence IDs, exact notice/artifact/recipient hashes, Notice vẫn DISPATCHED. Noncontributor CHECKER VERIFY_ACK mới ACKNOWLEDGED; trả assessment khi chưa đạt giữ DISPATCHED. Bổ sung source ack evidence là contribution; recording review assessment không phải source contribution. Nếu record gửi bên ngoài, app ghi source evidence/actor/actual event time và không giả claim app đã gửi. Giao diện hiển thị từng cột riêng.

### 8.2. Tariff clause demo

`TARIFF-ACK-7D-1` yêu cầu fee-only notice được approve, DISPATCHED và ACKNOWLEDGED; effectiveOn phải `≥ localDate(dispatchAt)+7` **calendar dates**, đồng thời `≥ localDate(ackAt)+1`. Đây không là 168 giờ elapsed và không lấy continued use/silence làm acceptance. Ví dụ dispatch 05/01 lúc 23:59 local, ack 11/01 → sớm nhất 12/01; ack 12/01 → sớm nhất 13/01. Bằng chứng còn thiếu → change vẫn APPROVED, không READY/COMMIT. Không lùi ack/dispatch timestamp để đạt lead; mọi event reported có evidence, recordedAt riêng.

NOTICE_ACK task được tạo đúng một lần khi tariff notice DISPATCHED, due trước effective boundary. Checker verification ack đóng task theo evidence; thiếu ack không auto accept. Notice deadline nêu trong mẫu khách hàng là nguồn contractual demo, không là thời hạn luật Việt Nam.

## 9. Nghĩa vụ và incident

### 9.1. Bốn loại obligation hữu hạn

| Type / creation | Basis / due / dữ liệu hoàn tất |
|---|---|
| NOTICE_ACK | CONTRACTUAL; khi tariff notice dispatch; due effective boundary; exact notice/ack evidence, independent verification |
| TERM_REVIEW | INTERNAL; một task mỗi fixed ContractVersion/end; due 17:00 local ngày endExclusive−30 calendar days; maker ghi RENEW/DECLINE/NEW_AGREEMENT intent + rationale/evidence, checker verify. Task closure không tự áp dụng renewal |
| REMEDIATION | CONTRACTUAL khi verified Incident có proven trigger/clause; selected demo duration 48 elapsed hours từ verification-confirmed event; source trigger có version/evidence. Có thể ghi NOTICE_RECEIVED only khi clause được chọn nói như vậy |
| CLOSEOUT | INTERNAL khi EXPIRED/TERMINATED; 5 demo working days loại ngày end/termination; external settlement/fee confirmation, service stop hoặc never-started record, unresolved incident/obligation review; không chuyển tiền |

Each obligation có target contract/version/incident/notice, basis, assignedMakerId, created trigger/evidence, originalDueAt/currentDueAt/calendar hoặc external source, dueRevision, status, closure evidence và breached events. Worker tạo theo unique `(type, sourceId, sourceVersion)`; không duplicate khi restart. Expiry OPEN_ENDED không tồn tại; terminate vẫn tạo closeout.

### 9.2. Lifecycle, miễn và gia hạn

| Từ → đến | Actor / guard |
|---|---|
| OPEN/RETURNED → EVIDENCE_SUBMITTED | MAKER submit immutable evidence set có reason/result; all attached files CLEAN |
| EVIDENCE_SUBMITTED → RETURNED | Independent CHECKER reason/correction items; old evidence giữ history |
| EVIDENCE_SUBMITTED → CLOSED | Independent CHECKER accepts exact evidence; closeout bổ sung guards bên dưới; closureAt/by/basis |
| OPEN/RETURNED → CANCELLED | Có prospective supersession hợp lệ hoặc waiver quyết định theo basis; ghi reason/source, không xóa breach |
| Nonterminal → same state | MANAGER approve extension/waiver request, requester/contributor excluded; update dueRevision/currentDueAt hoặc cancellation với evidence; không âm thầm sửa originalDueAt |

INTERNAL được manager miễn/gia hạn có reason. CONTRACTUAL cần bilateral acceptance evidence verified; không đủ thì reject exception. STATUTORY_EXTERNAL không cho waiver/extension bằng app, chỉ external corrected basis được ghi như record mới có source. Không miễn access/SoD/sign/legal readiness bằng task exception.

Prospective extension approved **trước currentDueAt** thay hạn chưa đến, không ghi breach giả ở original due. Nếu decision sau currentDueAt, transaction phải synchronize breach tại missed operative deadline dù scheduler muộn; sau gia hạn, breach lịch sử vẫn còn. Mỗi missed operative due có event dedupe, first `breachedAt` giữ earliest; threshold scheduler key `(obligationId, threshold, dueRevision)`. Closure/waiver không xóa historical breach. Overdue hiện tại là nonterminal và currentDueAt < asOf; khác historicalBreaches.

Change áp dụng thay thế future unopened TERM_REVIEW bằng linked supersession; task đã mở/đã breach giữ lịch sử và explicit disposition. “Unopened” nghĩa chưa có human action/evidence/exception và due chưa tới, không suy từ status OPEN một mình. Không xóa open remediation/payment complaint khi hết hợp đồng.

NOTICE_ACK phụ thuộc duy nhất một tariff Change đã cancel/supersede → CANCELLED với inapplicability event, giữ historical breaches dù task đã mở. Dispatched Notice giữ state và superseded flag. TERM_REVIEW không còn cần sau term ending chỉ đóng/cancel qua independent CHECKER với ending basis; không rewrite due hay biến breach thành chưa từng xảy ra. Không dùng thao tác này để cancel complaint/remediation/closeout.

### 9.3. Incident path

Kind `CONTRACT_PERFORMANCE` hoặc `PAYMENT_COMPLAINT_REFERENCE`. Record summary, contract/reference, source, receiptAt theo evidence, createdAt thực, blocking flag và owner; không yêu cầu real account statements. Payment complaint có externalProviderReference, initialReceiptAt, externalDueAt và law/provider source basis. Không tính generic 30 working days bằng demo calendar và không claim bank đã hoàn tiền.

| Từ → đến | Action / guard |
|---|---|
| OPEN → VERIFIED | Independent CHECKER verifies occurrence/trigger/evidence; đủ clause trigger mới tạo remediation task; nhận bổ sung không đổi original receipt |
| VERIFIED → REMEDIATION | Explicit verified action plan/linked required tasks; no auto finding of fraud/guilt |
| VERIFIED/REMEDIATION → RESOLVED | Accepted resolution evidence; mọi linked required remediation task CLOSED hoặc approved waiver đúng basis; không tự resolve từ upload success |
| RESOLVED → CLOSED | Checker independent of requester/contributors, closure reason; unresolved external complaint confirmation blocks |

Không có incident delete hoặc terminal reject để che receipt. Unsupported allegation có thể verified record “chưa đủ chứng minh” + assessed disposition, lưu evidence và closure reason, không kết luận bên nào phạm pháp. Bổ sung/reassign không reset receiptAt/externalDueAt. Khi nghi ngờ cần bảo vệ, tạo urgent SUSPEND request hiển thị pending ngay; kết quả ngoài hệ thống phải được confirm như §10.

## 10. Kiểm soát dịch vụ và privacy disposition

### 10.1. Control request và external acknowledgement

Source facts gồm kind, contract/version, requester, reason/ground, effectOn nếu termination, notice refs, acknowledgement evidence/ackAt, source label EXTERNAL_RECORDED hoặc SIMULATED. Mỗi contract+kind có tối đa một nonterminal request. Request/approval không đổi service observed state; CONFIRM mới ghi kết quả. All confirm recheck source state dưới contract lock và ackAt≤serverNow. SYSTEM là actor kỹ thuật audit, không human role có quyền đăng nhập.

| Kind | State path / người thực hiện | Source guard / CONFIRMED effect |
|---|---|---|
| ACTIVATE | MAKER DRAFT→IN_REVIEW; independent CHECKER L1→APPROVED rồi CONFIRMED bằng event riêng | Contract ACTIVE, readiness READY, Service NOT_STARTED, ackAt≥agreement start; →RUNNING |
| SUSPEND | MAKER hoặc CHECKER urgent DRAFT→REQUESTED; CHECKER khác requester CONFIRM, không đợi formal round | Contract ACTIVE + Service RUNNING; reason/evidence; →SUSPENDED, không tự kết luận gian lận |
| RESUME | MAKER DRAFT→IN_REVIEW; independent CHECKER L1→APPROVED→CONFIRMED | ACTIVE, READY, SUSPENDED, blocking incidents đều RESOLVED/CLOSED; ackAt≥last suspend ack; →RUNNING |
| TERMINATE | MAKER DRAFT→IN_REVIEW; independent L1 CHECKER và L2 MANAGER→APPROVED→CONFIRMED | ACTIVE; reviewed ground/notice evidence; current ContractVersion.effectiveFrom≤effectOn≤today; external stop ack hoặc verified never-started. Atomically Contract TERMINATED, Service STOPPED; cancel other pending controls/unapplied inapplicable changes; create CLOSEOUT |
| STOP | SYSTEM REQUESTED khi expiry với RUNNING/SUSPENDED; hoặc manual DRAFT→REQUESTED cho EXPIRED/TERMINATED observed nonstopped service; independent CHECKER CONFIRM | →STOPPED; Contract vẫn EXPIRED/TERMINATED, không reclassify thành termination |

L1 có thể approve rồi confirm ACTIVATE/RESUME nếu không requester/contributor; hai assessments/events khác nhau. Recording verification/approval assessment không phải sửa source facts. Người đã sửa source facts/evidence bị loại và cần reviewer khác.

DRAFT/REQUESTED maker cancel có reason trước confirmation; IN_REVIEW withdraw cancels round/request; APPROVED cancellation cần independent CHECKER reason/evidence. Reject round/request → REJECTED; cancel → CANCELLED, service không đổi. SYSTEM STOP chỉ được cancel bởi CHECKER với verified not-applicable basis, không “bỏ việc” vì maker bấm cancel. Termination được confirm sau boundary mà contract đã EXPIRED bị chặn; advanceTimeline chạy trước guard. Việc ghi termination effectiveOn và observedAt khác nhau không có nghĩa app kết luận chắc chắn hiệu lực hồi tố. TERMINATE CONFIRM với effectOn trước current ContractVersion.effectiveFrom trả 409 STATE_CONFLICT trước business effects; giữ request/source/effectOn và mọi version interval/APPLIED Change. Không clamp ngày, không rewrite history, không ghi confirmed termination hoặc cache như success; đây là ordinary prevalidation, không phải ngoại lệ Change COMMIT. Nguồn ngày hiệu lực sửa lại phải cancel và tạo ControlRequest mới qua review hiện có. Current action hints phản ánh blocker STATE_CONFLICT.

**Closeout guard:** không còn linked unresolved Incident; không còn applicable nonterminal Obligation **khác chính CLOSEOUT đang xét**; có settlement/fee/external complaint confirmations hoặc explicit not-applicable basis; Service STOPPED hoặc verified never-started. CANCELLED inapplicable task không chặn; historical breaches không biến mất. Closeout CLOSED không xóa contract/file/history.

### 10.2. DataRequest

DataRequest type ERASURE/ACCESS/CORRECTION; subject reference, purpose, scoped record IDs, request/source evidence, requester, record class, hold/basis review. MAKER hoặc DATA_OFFICER tạo OPEN. Independent scoped DATA_OFFICER START_REVIEW → IN_REVIEW; decision ALLOW/PARTIAL/DEFER/REFUSE có specific reviewed basis/reason/record class/manifest → DECIDED, hoặc request không hợp lệ → REJECTED có reason. DEFER/REFUSE không EXECUTE; facts mới dùng linked new request, không sửa quyết định đã chốt.

Manifest liệt kê từng record/version và action `REDACT`, `DELETE_BYTES`, `PROVIDE_REDACTED_COPY`, `CORRECT_CURRENT_METADATA`, exact hash. Không có arbitrary delete hay sửa approved terms/audit/decision/artifact. ALLOW/PARTIAL mới EXECUTE; cùng DATA_OFFICER đã decide được execute nếu không requester, bởi SoD đã tách requester–decision. Sau job hoàn tất exact manifest → EXECUTED; job lỗi vẫn DECIDED + execution incomplete, không báo thành công.

PLACE_HOLD/RELEASE_HOLD do DATA_OFFICER ghi immutable events gồm target record IDs, reason/basis/evidence. Release phải bởi officer khác người đặt hold và không tự review request mình. Enqueue và worker recheck scope/decision/manifest hash/record versions/active holds. Serialize hold placement với disposition reservation dưới record locks: hold đến trước reservation chặn disposal; disposition đã committed/reserved thì hold mới trả 409 DISPOSITION_COMMITTED, không giả hứa chặn byte deletion đã cam kết. Durable intent retry chỉ exact manifest, byte cleanup thất bại phải hiện FAILED/incomplete.

| Retention class mô phỏng | Tham số mô phỏng / khởi tính |
|---|---|
| DEMO_AGREEMENT | 3650 days từ closeout |
| DEMO_ID_COPY | 30 days từ sign verification |
| DEMO_OPERATION | 180 days từ closeout |

Đây là simulation parameters, không xác lập thời hạn luật cho record ngân hàng thật. Retention expiry chỉ tạo review eligibility, không autodelete. Legal hold giữ records; quyền truy cập vẫn có thể minimize trong khi chờ disposition. Approved byte disposition lưu limited tombstone: opaque record ID, action/basis/decision/actor/time/hash chỉ khi được phép giữ, không full PII. Historic approval fact không âm thầm rewrite; UI ghi “Nội dung đã được xử lý theo quyết định …”, resolution record riêng. Access request cấp bản redacted/filtered; correction chỉ current metadata, không đổi signed agreement snapshot. Real deployment cần legal classification/applicability trước dữ liệu thật.

## 11. Mô hình dữ liệu và toàn vẹn

Mỗi aggregate có UUID, positive version, branch scope (trừ bankwide policy/minimal Party) và created/recorded timestamps. Business history append-only theo ứng dụng; không tuyên bố chống DBA sửa tuyệt đối. B quyết định exact wire field/schema; bảng dưới quyết định quan hệ và ý nghĩa.

| Entity | Quan hệ / dữ liệu chủ yếu / constraint |
|---|---|
| User, RoleScopeGrant, Branch | User→roles/scopes/supplemental grants; active/revocation version; branch code unique. Audit quyền cấp và thu hồi |
| Party | normalizedTaxId bankwide unique; name; không PII contact. Không hard delete khi đã referenced |
| CustomerRelationship, ContactSnapshot, ContactRequest | unique branch+party; assigned maker; confirmedContactVersion; request snapshot expected relationshipVersion; notice/dossier giữ snapshot refs |
| Dossier, DossierRevision | relationship consistent branch; many revisions; revision number unique/dossier; submitted content/config/evidence/contributor hashes immutable |
| ApprovalRound, ApprovalStep, EvidenceAssessment | exact targetType/id/revision; one OPEN round/target; step ordinal 1..2 unique; one effective decision/step; assessment stores reviewer/time/outcome/evidence hash |
| PolicyVersion, RegressionReport | config hash; contributor IDs; unique effective intervals; one ACTIVE and at most one future SCHEDULED; report exact hash |
| Contract, ContractVersion, AgreementArtifact | one original Contract/dossier; version lineage; immutable terms; authoritative intervals không overlap; one current recorded version. Artifact unique approved snapshot+template+render kind |
| SigningPacket, ReadinessRisk | packet exact target artifact/hash; immutable submitted source/assessment; risk raised/resolved separate actor/evidence; no manual boolean legal-valid |
| Change, ChangeRevision, ScheduleReservation | base version; expected/committed schedule versions; only one nonterminal scheduled change/contract; applied version/event unique changeId |
| Notice, NoticeRevision, NoticeEvent | frozen text/recipient/contact/artifact hashes; unique stableMessageId; dispatch/delivery/deemedreceipt/ack events different; supersession lineage |
| Obligation, DueRevision, ExceptionDecision, BreachEvent | unique type+sourceId+sourceVersion; immutable original due; current due revision; threshold/breach key unique; disposition/waiver preserve old events |
| Incident, ControlRequest, ControlAck | original external receipt separate app entry; one pending control/kind/contract; confirmation records exact external/simulated evidence; no inferred service execution |
| DataRequest, DispositionManifest, RecordHold | explicit record IDs/versions/actions; immutable decision hash; target locks and reservation; hold-release actor exclusion |
| FileObject, FileAssociation, FileScan | immutable blob/hash/content type; private key; association exact branch+target ancestor; scan binds hash/scanner engine+DB version; disposition tombstone separate |
| DurableJob, IdempotencyRecord, AuditEvent | unique business-effect key; attempt/lease/source version; mutation audit+state+job/idempotency atomic; audit excludes raw sensitive values |

Contract terms change only by approved/applicable version. Version interval end is cut at next effect, independent of contractual endExclusive which may be extended. Unique constraints/contract row lock enforce schedule/step/approval/effect races. Reassignment changes current responsibilities without modifying historical actor IDs. File deletion/disposition must preserve references with visible tombstone; missing bytes from corruption are an integrity error, not authorized disposition. Backups pair DB reference manifest with exact blobs/hash; restore verifies both.

### 11.1. File và job contract nghiệp vụ

Allowed PDF/JPEG/PNG, 10 MiB/file; tối đa 20 attachments/draft và 100 MiB total. Media sniff và allowlist do server; client MIME/filename không đáng tin. Upload vào quarantine returns File SCANNING + jobId; scanner CLEAN/INFECTED/SCAN_FAILED. SCAN_FAILED có explicit retry → SCANNING; không auto CLEAN khi scanner outage. INFECTED không attach, không retry như evidence an toàn. DISPOSED không tải bytes.

Staging parent types: DOSSIER, CONTRACT, CHANGE, RELATIONSHIP, INCIDENT, OBLIGATION, CONTROL_REQUEST, DATA_REQUEST, NOTICE, POLICY. Create parent trước upload; ký copies stage trên Contract/Change rồi tạo packet. Association chỉ CLEAN, same branch và allowed ancestor; không reuse file tùy ý giữa dossiers/contracts. New file thay old fileId; hash server SHA-256 chỉ chứng minh byte identity. Generated PDF CLEAN server-generated nhưng không bỏ human artifact/sign check.

File read bằng authenticated proxy, mode FULL/REDACTED kiểm tra scope+grant; opaque storage key, không public URL. Original filename dùng display/safe download, không làm path. Content-Disposition attachment, nosniff, không inline HTML. Download/view audit. Owner chỉ delete unattached own upload chưa dùng; historical bytes chỉ qua DataRequest. Orphan staging cleanup sau 24h với integrity/reference checks, không xóa historic evidence.

Job QUEUED→RUNNING→SUCCEEDED/FAILED, retry FAILED→QUEUED nếu source authorization/version/dependency còn hợp lệ; CANCELLED nếu nghiệp vụ nguồn đã bị hủy trước effect. Lease hết sau crash cho worker khác recover. Retry tối đa 5 lần, exponential capped 15min; failed manual retry có reason. At-least-once execution, business-key dedupe; không claim exactly-once network delivery. State/audit/durable intent cùng DB transaction; bytes finalize trước usable DB ref. Recovery bảo đảm không có approval thiếu audit, duplicate PDF/notice/effect/obligation. Nếu byte write xong mà transaction fail, reconcile orphan; không rollback bằng xóa file đang referenced.

## 12. Giao diện và dashboard

### 12.1. Màn hình bắt buộc

| Screen | Thông tin/actions phải có | Trạng thái lỗi/ngoại lệ |
|---|---|---|
| Login/session | username/password, session/CSRF refresh, logout; không lưu password/JWT localStorage | Invalid credentials, rate limit, expired session; hết session giữ draft UI nhưng không auto replay command |
| Home/task inbox | Own assignments, L1/L2 tasks, obligations/control pending, overdue; branch scope filter | Empty khác forbidden; không hiện all-bank total cho branch user |
| Users/branches | Admin grant/revoke/lock, explicit reason, role/branch/sensitive/export view | Lost scope, stale revision, bootstrap-only seed admin |
| Corporate relationship 360 | Party metadata, relationship maker, confirmed/pending contacts, dossier/contract/task history tabs | Duplicate Party/relationship; restricted linking; contact stale approval; không global taxId enumeration |
| Dossier wizard/workbench | Draft terms, term labels, representation, bundle, upload/scan checklist, submit preview, contributor list; review facts/evidence/rules/timeline; RETURN/REJECT/APPROVE | Missing/scan-failed/file permission; fee boundary; self-approval; stale; reviewer unavailable; human verified khác CLEAN |
| Policy studio | Typed fields, diff active/draft, simulator explicit fact vector, boundary regression, hash/report, independent publish calendar | Invalid config/hash mismatch/self-publish/future overlap; no DSL/code editor |
| Contract 360 | Recorded contract+service separately, current/prior terms/bundle/lineage, artifact/render job, packet/check/readiness holds, planned changes, obligations/controls | Late readiness, missing artifact, risk hold, actual vs recorded time, expired with stop pending |
| Change workbench | Base/current/proposed diff, whitelist validation, submit/review/sign-or-notice, READY/commit/rebase/cancel schedule | Base/schedule conflict, future slot busy, guard change before apply, no automatic rebase/approval reuse |
| Notice workspace | Exact recipient/contact version/text/artifact; review, queue, retry, distinct events, ack candidate→verify | Dispatch ≠read/ack; expired lead, revoked scope, superseded unissued cancelled; manual-record source label |
| Obligation/incident | Due+original/current/breach history, basis, evidence submission/review, extension/waiver, receipt/source/external deadline, remediation/closure | Cannot statutory waive, extension after overdue preserves breach, unresolved task blocks resolve/closeout |
| Service controls | Kind/source guards, route L1/L2, ground/notice, pending request and evidence confirm, observed service timeline | Pending not execution; expired ACTIVATE/RESUME/TERMINATE blocked; system STOP reason required to cancel |
| Privacy review | Subject/purpose/records, class/basis/holds, request decision/manifest diff, separate execute status | Self-request decision blocked; new hold/record-version conflict; irreversible intent; failed byte cleanup visible |
| Audit/files/jobs | Scoped searchable events/correlation, redacted viewer, server hash/state, job attempts/retry, source links | Cross-scope opaque not-found; no raw secrets; missing bytes vs DISPOSED distinguished |
| Dashboard/drilldown/export | asOf+scope+lag, metrics/formulas below, row navigation, same filters, redacted CSV | Cursor expiry/reload, permissions, >1000 export limit, live membership changed |

Each screen needs loading, empty, validation, forbidden/not-found, stale/conflict, job pending/failed states. Field errors map server paths to labels and focus first invalid field; reason visible for disabled commands. Mobile supports readable details/queues; desktop 1280×720 is review baseline. At 200% zoom no essential action lost; controls keyboard accessible, focus/error/status announced; status not communicated only by color. PDF view must have accessible metadata/download alternative.

Authoritative states/routes/deadlines/allowedActions/evaluations come from BE. FE can prevalidate syntax/required fields and show date formatting, but cannot promote state, decide policy, infer authority, compute legal deadline or assume allowedActions bypass server. Command success reloads affected detail/queues/dashboard; visible queues poll 15s, pause hidden tab; no websocket requirement. Lists may change membership as live state updates; UI offers refresh instead of pretending immutable result snapshot.

### 12.2. Dashboard formulas và drilldown

Report operates on scoped SQL snapshot with one asOf; branch filter must be intersection of requested and allowed scopes. Date-window filter means entity createdAt in `[createdFrom,createdTo)` where supplied; active/overdue operational KPIs default no created window, and selected window must be visibly labeled. No count of hidden rows/branches. `timelineProcessingLagSeconds` shows materialization lag, separate from metric asOf.

| Metric | Exact predicate / drilldown |
|---|---|
| dossiersByState | Count dossiers theo latest recorded state, không count revisions/rounds; dossier list same branch/state/window |
| reviewOverdue | Current round OPEN, dueAt<asOf; count distinct target dossier/change/control with one current round; round queue same predicate |
| activeAgreements | Contract recorded ACTIVE, bất kể service RUNNING/SUSPENDED/NOT_STARTED; contract list status ACTIVE |
| expiring30 | ACTIVE+FIXED, 0<days(todayLocal,endExclusive)≤30; contract drilldown identical interval |
| overdueObligations | status OPEN/EVIDENCE_SUBMITTED/RETURNED và currentDueAt<asOf; không count CLOSED/CANCELLED; duty queue same |
| historicalBreaches | Distinct obligation có ít nhất một breach event recorded by asOf; vẫn count sau extension/closure/cancel; audit/drilldown các obligation có event |
| unresolvedCloseout | type CLOSEOUT và status nonterminal; không gộp contractual agreement ACTIVE |
| noticeFailures | Notice FAILED; job transient retry chưa exhausted không tự count notice failure |
| pendingServiceControls | REQUESTED/IN_REVIEW/APPROVED; không count DRAFT, CONFIRMED, REJECTED, CANCELLED |

Drilldown giữ metricId/filter/asOf semantics; không có số liệu analytics projection khác SQL authority. Pagination live có thể thay membership sau query, phải nói rõ; asOf của lần query không hứa frozen dataset cho mọi page. EXPORT scoped/redacted CSV, ≤1000 rows synchronous, UTF-8 BOM, escape formula injection `=,+,-,@` ở text; >1000 trả limit error yêu cầu thu hẹp filters. Không xuất full identity image qua EXPORT. Không score “legal compliance”.

## 13. Kiến trúc, công nghệ và yêu cầu phi chức năng

Một React SPA, một Java Spring MVC modular monolith, PostgreSQL, private evidence storage adapter và ClamAV. Same-origin TLS reverse proxy cho SPA `/api/v1`; local Vite proxy HTTPS localhost5173→BE8080. Không thêm Redis/Kafka/Kubernetes/microservices để tạo độ khó. Domain modules có APIs/events riêng: identity-access; party/relationship; dossier/evidence/authority; policy; agreement/change/effectivity; notice/obligation/incident/closeout; reporting/audit. Shared kernel chỉ ID/date/decimal/error primitives. Module-boundary tests cấm cross-module repository/table writes; synchronous critical transaction vẫn liên module qua APIs có chủ sở hữu.

| Component | Exact initial pin / lý do |
|---|---|
| Java | Java 25 LTS, available distribution Oracle JDK25.0.4.1; compile release25, record vendor/build/architecture/checksum. [Oracle downloads](https://www.oracle.com/java/technologies/downloads/) phân biệt latest feature27 và LTS25 |
| Spring | Boot4.1.1, Modulith2.1.1; Boot BOM quản lý SpringSecurity/Data/Flyway/Jackson/JDBC. [Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html) hỗ trợ Java17–26; [Modulith tag POM](https://raw.githubusercontent.com/spring-projects/spring-modulith/2.1.1/pom.xml) khai báo Boot4.1.1 |
| Build/database | Maven Wrapper3.10.0 có checksum; PostgreSQL18.6 immutable image digest. [Maven](https://maven.apache.org/download.cgi), [PostgreSQL version policy](https://www.postgresql.org/support/versioning/) |
| FE runtime | Node24.21.0 LTS + npm12.2.0 explicitly pinned; không nhầm npm bundled11.19.0. [Node releases](https://nodejs.org/dist/index.json), [npm metadata](https://registry.npmjs.org/npm/latest) |
| FE | React/react-dom19.3.0, TypeScript7.0.2, Vite8.3.4, plugin-react6.1.2; lock exact direct versions and integrity. [React](https://registry.npmjs.org/react/latest), [TypeScript](https://registry.npmjs.org/typescript/latest), [Vite](https://registry.npmjs.org/vite/latest), [plugin](https://registry.npmjs.org/%40vitejs%2Fplugin-react/latest) |
| Contract/tests | OpenAPI3.1 family, exact B header authoritative; Orval8.41.0, RedoclyCLI2.62.1; MSW3.0.3, Vitest5.0.3, Playwright1.64.0; JUnit/Testcontainers theo BOM, realPG, không H2 cho race. [Orval](https://registry.npmjs.org/orval/latest), [Redocly](https://registry.npmjs.org/%40redocly%2Fcli/latest), [MSW](https://registry.npmjs.org/msw/latest), [Vitest](https://registry.npmjs.org/vitest/latest), [Playwright](https://registry.npmjs.org/%40playwright%2Ftest/latest) |
| Scan | ClamAV1.5.4, record engine/signature DB age/version; [official downloads](https://www.clamav.net/downloads) |

Versions verified against upstream metadata ngày 11/10/2026; chưa chạy smoke build. Gate bootstrap phải chứng minh Orval+OpenAPI+TS7 compile và dependency graph thực. Không chọn generator declared TS^5 peer rồi mặc định compatible TS7. Direct pins không caret/tilde; `npm ci`, lock integrity, Maven dependency tree/BOM và wrapper checksum vào CI evidence. Upgrade phải patch technical baseline/lockfile, chạy affected checks; không tự kéo latest lúc build. Optional virtual threads chỉ sau profiling, DB transaction/connection pools bounded vẫn authoritative.

### 13.1. Security và durability

Server session HttpOnly Secure `__Host-cg-session`, SameSite=Lax, Path=/; CSRF token session-bound cho unsafe actions kể cả login/logout, rotate sau login. Idle30min, absolute8h; no JWT localStorage. Same-origin production, không wildcard CORS. Brute force limit10attempt/min username+IP; revoke checked next request. [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) là nguồn kỹ thuật về lifecycle token, B là exact wire contract.

Every command transaction rechecks scope/version/SoD/current guards, updates state+audit+job+idempotency result atomic. Optimistic version cộng aggregate lock/SQL uniqueness xử lý races. Logs/traces không password/session/CSRF/full contact/CCCD/payload; correlation/opaque ID thay raw values. Audit download/decision/permission/hold/breach; append-only ứng dụng không thay legal archive. DB/files encryption transport, secrets environment/secret store không source; least privilege app DB/storage credentials. Private file recovery phải detect missing/corrupt blobs, không báo CLEAN/verified từ metadata một mình.

### 13.2. NFR targets phải đo khi triển khai

Reference host x86-64, 4 logical CPU,8GiB RAM,SSD; BE heap≤2GiB; scanner budget riêng; load generator LAN host khác idle RTT≤5ms. Record CPU/OS/containers/digests/heap/pools/browser. Synthetic dataset 10,000 Parties,50,000 Dossiers,20,000 Contracts,100,000 duty/notice rows,500,000 audits và1000 allowed files median1MiB/max10MiB, status/time distribution có mọi queue. Không tạo PII thật để benchmark.

| NFR | Acceptance target / measurement |
|---|---|
| Query/command | Warmup2min + run10min,20 authenticated concurrent virtual users, mix60%lists/search,20%detail/timeline,10%dashboard,10%valid commands. P95 read≤800ms, command≤1500ms,dashboard≤2000ms; unexpected5xx/transport<1%; expected domain4xx báo riêng |
| Scan/job | Healthy scanner, queue≤20: allowed10MiB reaches reported scan outcome≤60s; due action picked≤30s under reference load; blocker becomes visible, HTTP job accept latency không là job completion |
| Race/replay | Every named approval/schedule/hold/disposition/idempotency race passes, không probabilistic percentage; same event once, conflicting second decision rejected |
| Recovery | Kill after claim/beforeack→lease recovery không duplicate effect; transaction failure không approval thiếu audit; restore coherent DB+files/hash for stated dataset≤60min. Không tuyên bố production RPO/availability |
| Authorization | All scope/file/export/sensitive negative fixtures deny with zero side effect; no public private-file URL; scanner outage failclosed; no secrets in logs/export |
| UX | Keyboard completes critical workflows; labeled fields/status/focus/tableheaders;200%zoom and1280×720; manual+automated WCAG2.2AA-relevant checks recorded, không claim full certification |

Đây là yêu cầu, không là kết quả đã đo. Nếu hardware/capacity khác, ghi adjustment trước test và không so số trực tiếp. Không hạ durability/access invariant để đạt latency.

## 14. Danh mục business rules và dependency gates

IDs dưới đây là ổn định để B trace operation/scenario; phần chi tiết trên quyết định exact semantics.

| ID | Quy tắc |
|---|---|
| BR-001 | Một bank giả lập/two branches/corporate VA payer-initiated; không money/debit/core/legal adjudication |
| BR-002 | Role không cấp all-branch; scope apply trước list/count/history/job/file/report |
| BR-003 | Session/CSRF/revoke checked current request; không tự replay privileged action |
| BR-004 | Source contributor/requester excluded; L1≠L2; role/reassignment không xóa contribution |
| BR-005 | Admin chỉ management/minimal Party; không implicit business/sensitive right |
| BR-006 | FULL sensitive read thêm SENSITIVE_READ, không do role mặc định |
| BR-007 | EXPORT thêm scoped grant, redacted, không thông qua count/download bypass |
| BR-008 | Party taxId unique bankwide; branch relationship unique; không enumeration/merge/transfer |
| BR-009 | Assignment/reassignment có eligible actor/reason/history; receipt/due không reset |
| BR-010 | Contact independent exact-snapshot confirmation + relationship version; past snapshots unchanged |
| BR-011 | Typed finite PolicyConfig/P1/P2; min/max BLOCK, reference chooses L1/L1_L2 |
| BR-012 | Hard access/SoD/integrity/immutability/current mandatory guards không editable |
| BR-013 | Simulation read-only explicit vector; regression bound exact config hash |
| BR-014 | Independent publish future midnight, one scheduled/no overlap/gap; published immutable |
| BR-015 | OPEN round retains submitted policy; fresh round uses newly applicable policy/current mandatory review |
| BR-016 | Draft facts/evidence validation/bootstrap; submitted Revision immutable |
| BR-017 | Required CLEAN files at submit, human VERIFIED+authority before L1 approve |
| BR-018 | Round max2steps/order/SoD/oneeffective decision; reject/return close round |
| BR-019 | Return/resubmit full new round; reject terminal clone; draftcancel/withdraw preservehistory |
| BR-020 | Last approval creates exactlyone original PREPARED Contract + artifact job atomic |
| BR-021 | Artifact exact approved terms/bundle/template, failedjob no duplicate; no redundant termsapproval |
| BR-022 | Signing exactartifact/hash/two-party authority evidence; immutablepacket; server verification ontime |
| BR-023 | READY beforestart strictly; materialchange newdossier; hold/release independent currentrisk |
| BR-024 | Schedule/ACTIVE timeline recorded separately fromservice; firstACTIVE completes dossier |
| BR-025 | Fixedendexclusive/openended; catchuptimeline; renewal-before-expiry; no terminalrevive |
| BR-026 | Change finitewhitelist AMENDMENT/RENEWAL/TARIFF_NOTICE, duration rules explicit |
| BR-027 | Change fullreview/artifact/sign-or-notice readiness; no directtermsmutation |
| BR-028 | COMMIT beforeboundary +currentbase/schedule +onefutureslot; committedversion distinct |
| BR-029 | ExplicitREBASE clears oldapproval/readiness and carriescontributor; cancellation audited |
| BR-030 | APPLY contractlock recheck/currentguard/dedupe; failureCONFLICTED no termschange |
| BR-031 | Notice exactrecipient/text/clause/artifact snapshot +independentreview; lateredit newrevision/notice |
| BR-032 | Stablemessage dispatch/retry; DISPATCHED≠delivered/deemedreceived/ack |
| BR-033 | Tariffselectedclause7localdates+nextdateafterverifiedack; no continueduse inference |
| BR-034 | Makerackcandidate only; independentVERIFY_ACK; supersededdispatchedhistory stays |
| BR-035 | Fourfiniteobligationtypes/source/basis/due; creationdedupe |
| BR-036 | Evidence submit/return/independentaccept; nonterminaldue derivesoverdue |
| BR-037 | Currentoperative deadline/breach event retained; prospectiveextension no inventedbreach |
| BR-038 | Internalexception allowed; contractual bilateral evidence; statutoryno waive/extend |
| BR-039 | TERM_REVIEW andNOTICE_ACK supersession explicit; endingcannoterasecomplaint/remediation |
| BR-040 | Closeout afterending, guardsallotherapplicableduties/incidents/stop/confirmation |
| BR-041 | Incidentreceipt/source/externaldeadline distinctcreatedAt; no legalrefundengine/reset |
| BR-042 | Verifiedtrigger→remediation→acceptedresolution→close; suspicionnotguilt |
| BR-043 | ACTIVATE/RESUME L1 andexactsourceguards; requestnotserviceexecution |
| BR-044 | UrgentSUSPEND/SYSTEMSTOP independentack; expirydoesnotinventSTOPPED |
| BR-045 | TerminationalwaysL1L2/groundnotice/stopack; currentVersion.effectiveFrom<=effectOn<=today; earlier409STATE_CONFLICT/noeffects; atomickeepopenhistory/cancelinapplicablefuture |
| BR-046 | Datarequestscope/purpose/recordclass/basis; independentofficerdecision |
| BR-047 | ALLOW/PARTIAL exactdispositionmanifest only; DEFER/REFUSE noexecute |
| BR-048 | Hold/releaseindependent events; serializedhold/dispositionreservation/currentrechecks |
| BR-049 | Retentionexpiryflagsreview, notrealdeadlinecertification/autodelete |
| BR-050 | Authorizeddisposition limitedtombstone/visiblemark; nocorrectionapprovedterms/audit |
| BR-051 | Fileallowlist/limits/sniff/hash/quarantine failclosed; CLEAN≠humanverified |
| BR-052 | Privateauthenticatedcontent/scopedassociation/version; historicaldeleteonlydisposition |
| BR-053 | Durablejobs/lease/retry/businessdedupe; atomicstate/audit/intent; recoveryexactsource |
| BR-054 | Auditscope/actor/source/version/reason/outcome; no rawPII/secrets; nohistoryrewrite |
| BR-055 | Dashboardexactscopedformulas/asOf/drilldown/lag; nolegalcompliancescore |
| BR-056 | Typedfilters/livecursor/redactedCSV≤1000/BOM/formulaescape |
| BR-057 | Vietnameseaccessibleworkbenches/errorstates; FE cannotauthoritativelystate/route/deadline |
| BR-058 | PinnedJava/FE modularmonolith/toolchaingate; directmodulewritesprohibited |
| BR-059 | MeasuredNFR/security/race/restore targets, not unmeasuredclaims |
| BR-060 | A/Breview+schema/fixtureparity+realE2E required; mock-onlycannotclaimintegratedDoD |

Implementation dependency order is gated, not a calendar promise:

1. **G0 contract freeze:** independent FE/BE reading; A enums/BR/AC matching B; exact schema parse/lint/examples/fixtures/trace/duplicate-block checks. Resolve current contradiction before affected operation starts.
2. **G1 bootstrap:** clean pinned builds; generated client TS strict compile; module-boundary tests; real PostgreSQL/Flyway; single-origin HTTPS session/CSRF; synthetic seed and clock/calendar fixtures.
3. **G2 foundation vertical slice:** real browser login→branch relationship→draft/create/upload/scan/save; scope/file denials. Zero API mocks in gate. Foundation must pass before both owners build all feature breadth.
4. **G3 approval/agreement:** simulation/publication, revision/SoD/return/race, artifact/sign/readiness/timeline, firstACTIVE; provider SQL tests and FE fixture tests tied to AC.
5. **G4 controlled changes/notices:** signed appendix, tariff ack, dualcommit/rebase/cancel/worker recovery, renewalboundary; real browser slice.
6. **G5 obligations/control/privacy/report:** overdue/exception/incident/suspend/resume/terminate/stop/closeout, hold/disposition, dashboard/export/races; no money execution.
7. **G6 integrated acceptance:** §15 oracle, all mandatory UI, negative scope/auth/scan/currentguard tests, recovery/restore and measured NFR; fresh setup instructions and exact evidence from B. No completion declaration from isolated FE/BE pass.

Document delivery itself requires two-file independent consistency review plus machine-readable block checks. Runtime builds/tests/NFR are later implementation gates; this document does not report them passed.




## 15. Oracle nghiệm thu và catalogue dùng chung

Các AC dưới đây là kết quả nghiệp vụ được tác giả đặc tả chốt trước triển khai. Mỗi case cô lập dữ liệu, dùng clock có kiểm soát trong test profile; B cung cấp request/response, fixture và các race/worker case tương ứng. “PASS tài liệu” chỉ xác nhận tính nhất quán của thiết kế, chưa xác nhận phần mềm chạy đúng.

| ID / luật | Given | When | Then / tác động bị cấm |
|---|---|---|---|
| AC-001 / BR-001 | VA_COLLECTION doanh nghiệp | thực hiện ACTIVATE có xác nhận mẫu | chỉ ghi nhận service RUNNING; không mở VA/chuyển tiền |
| AC-002 / BR-002 | maker B02 và hồ sơ B01 | đọc detail, file, job hoặc dashboard | 404 cho object; count/list không tiết lộ B01 |
| AC-003 / BR-003 | session còn sống nhưng user bị khóa | gửi request tiếp theo | 401 UNAUTHENTICATED; CSRF sai trả403 trước mutation |
| AC-004 / BR-004 | maker sửa evidence rồi được cấp CHECKER | duyệt revision chứa ID đó | 403 SOD_VIOLATION; đổi role/reassign không xóa contributor; L1 khác L2 |
| AC-005 / BR-005 | BANK_ADMIN không có CHECKER | duyệt round | 403 ROLE_FORBIDDEN; grant không tạo business approval |
| AC-006 / BR-006 | AUDITOR thiếu SENSITIVE_READ | tải FULL identity image | 403 SENSITIVE_FORBIDDEN; bản redacted được kiểm tra scope riêng |
| AC-007 / BR-007 | AUDITOR thiếu EXPORT | xuất audit CSV | 403 EXPORT_FORBIDDEN; không trả nội dung xuất |
| AC-008 / BR-008 | Party taxId đã tồn tại | admin tạo cùng taxId | 409 DUPLICATE_PARTY; một pháp nhân, có thể liên kết branch khác |
| AC-009 / BR-009 | maker được reassign | mở và sửa draft trong relationship được giao | owner mới được action phù hợp; historic contributors/receipts/due giữ nguyên |
| AC-010 / BR-010 | contact request expectedRelationshipVersion1 nhưng relationship2 | checker approve | 409 BASE_CONFLICT; contact/notice cũ không bị ghi đè |
| AC-011 / BR-011 | P1 với mọi điều kiện khác đạt | mô phỏng fee499/500/999/1000/5000/5001 | lần lượt BLOCKED/L1_L2/L1_L2/L1/L1/BLOCKED |
| AC-012 / BR-012 | policy fee PASS nhưng mandatory check BLOCK | submit hoặc quyết định | 409 LEGAL_REVIEW_REQUIRED hoặc READINESS_BLOCKED theo check; không override bằng manager |
| AC-013 / BR-013 | report PASS cho config hash H1 | sửa config thành H2 rồi xin publish | 409 REGRESSION_FAILED; report cũ vô hiệu, cần RUN_REGRESSION mới |
| AC-014 / BR-014 | hai policy IN_REVIEW cùng expected schedule1 | reviewer độc lập approve đồng thời | một SCHEDULED, một409 POLICY_SCHEDULE_CONFLICT; không gap/overlap |
| AC-015 / BR-015 | round OPEN pin P1; P2 bắt đầu áp dụng | review round cũ rồi RETURN/resubmit | round cũ P1; round mới P2 và contributor history giữ |
| AC-016 / BR-016 | draft sourceFileId null | tạo parent rồi upload CLEAN/attach và SUBMIT | draft tạo được; submit đóng Revision bất biến với source nonnull, start từ ngày kế tiếp |
| AC-017 / BR-017 | required file SCANNING hoặc CLEAN chưa human verify | submit/approve tương ứng | SCANNING chặn submit422 FILE_NOT_CLEAN; chưa verify chặn APPROVE409 READINESS_BLOCKED |
| AC-018 / BR-018 | hai quyết định cùng round ETag và reviewer hợp lệ | approve song song | một200, một412; một decision, một Contract và job khi duyệt cuối |
| AC-019 / BR-019 | round OPEN | RETURN, bổ sung, submit lại; riêng REJECT rồi clone | RETURN đóng round và giữ ảnh cũ; round mới policy hiện tại; REJECT terminal, clone ID mới |
| AC-020 / BR-020 | final step đạt tất cả guards | APPROVE rồi mất kết nối/replay | một PREPARED Contract, artifact job và audit trong cùng transaction |
| AC-021 / BR-021 | policy/customer hiện tại đã đổi sau approval | job render artifact | PDF vẫn exact approved Revision/bundle/hash; retry không nhân Contract |
| AC-022 / BR-022 | signedAt trước start nhưng server verify đúng/sau start | VERIFY_SIGNING | 422 LATE_SIGNING dù nhập signedAt cũ; cần linked dossier ngày mới và ký lại |
| AC-023 / BR-023 | READY contract có risk mới | CHECKER HOLD rồi cùng người RELEASE | hold ghi REVIEW_REQUIRED; tự release403 SOD_VIOLATION; independent noncontributor mới giải tỏa |
| AC-024 / BR-024 | readiness verified trước start, worker dừng qua boundary | GET contract detail khi restart | catch-up ACTIVE với effectiveAt boundary/recordedAt thực; Dossier COMPLETED, service vẫn NOT_STARTED |
| AC-025 / BR-025 | FIXED có renewal READY đã commit trước old end; OPEN_ENDED riêng | chạy expiry trước apply sau boundary; thử renewal OPEN_ENDED | FIXED apply renewal trước expiry; OPEN_ENDED422 NO_RENEWAL; không hồi sinh expired thật |
| AC-026 / BR-026 | FIXED amendment end hoặc OPEN_ENDED fee-only | validate change | fixed tính duration từ original start; open giữ null end; không chuyển termKind |
| AC-027 / BR-027 | change được duyệt nhưng thiếu signing/verified tariff ack | COMMIT | 409 READINESS_BLOCKED/NOTICE_NOT_READY; APPROVED chưa được SCHEDULED |
| AC-028 / BR-028 | hai READY changes cùng base/schedule1, slot trống | COMMIT song song bằng key khác | một200/SCHEDULED; loser409 SCHEDULE_CONFLICT có persisted CONFLICTED+audit và cached409; một future slot |
| AC-029 / BR-029 | change base cũ sau contract version mới | REBASE rồi xin COMMIT ngay | reset approvals/artifact/signing/notice readiness, carry contributors; cần approval/sign lại |
| AC-030 / BR-030 | scheduled renewal tới boundary với current risk hold | advanceTimeline bởi expiry/apply/detail | CONFLICTED, không đổi terms; expiry đúng hạn và tạo STOP nếu service còn chạy |
| AC-031 / BR-031 | notice draft với confirmed contact version1 | SUBMIT rồi contact đổi version2 | notice snapshot version1 bất biến; request SUBMIT stale409 BASE_CONFLICT |
| AC-032 / BR-032 | notice approved LOCAL_CAPTURE | DISPATCH rồi retry job sau crash | một stable messageId capture; DISPATCHED không tự tạo deliveredAt/acknowledgedAt |
| AC-033 / BR-033 | dispatch05/01 23:59 local, ack11/01 | tariff COMMIT effective12/01; ack12/01 biến thể | 12/01 đủ7 DATEdays và ack+1; ack12/01 thì sớm nhất13/01 |
| AC-034 / BR-034 | maker RECORD_ACK đúng notice/artifact/recipient hash | checker noncontributor VERIFY_ACK; rồi cancel change | record chưa ACKNOWLEDGED; verify mới ACKNOWLEDGED; cancel giữ notice superseded và cancel duty không còn áp dụng |
| AC-035 / BR-035 | FIXED end07/01/2028 hoặc incident verified có trigger48h | tạo derived duties | TERM_REVIEW due08/12/2027 17local; REMEDIATION đúng trigger+48h; CLOSEOUT/NOTICE_ACK có source unique |
| AC-036 / BR-036 | maker submit duty evidence | cùng contributor được cấp CHECKER APPROVE | 403 SOD_VIOLATION; independentchecker accept CLOSED hoặc RETURN giữ history |
| AC-037 / BR-037 | operative due đã qua, worker chưa chạy | approve late extension rồi restart worker | đúng một breach cho dueRevision cũ; extension không xóa; before-due extension không breach ở hạn cũ |
| AC-038 / BR-038 | STATUTORY_EXTERNAL duty hoặc CONTRACTUAL thiếu bilateral proof | request waiver/extension | 422 NONWAIVABLE_DUTY cho statutory; contractual thiếu proof422 VALIDATION_FAILED |
| AC-039 / BR-039 | đổi future end với TERM_REVIEW chưa bắt đầu; task cũ đã breached riêng | apply thay đổi và supersede | task future thay bằng lineage; task breached cần review/basis và giữ breach, không xóa |
| AC-040 / BR-040 | CLOSEOUT còn incident hoặc service RUNNING | checker APPROVE | 409 UNRESOLVED_DUTY; excludes itself/CANCELLED inapplicable; chỉ close sau stop/neverstarted và external confirmations |
| AC-041 / BR-041 | payment complaint nhận bên ngoài ngày trước app entry | tạo incident rồi bổ sung/reassign | receivedAt ban đầu và externalDueAt/basis giữ; không suy ra deadline luật bằng demo calendar |
| AC-042 / BR-042 | incident OPEN có evidence verified và đúng trigger | VERIFY, duty evidence accept, RESOLVE, CLOSE | deadline source rõ, chỉ RESOLVE sau linked duty CLOSED, CLOSE bởi checker; không kết luận gian lận |
| AC-043 / BR-043 | ACTIVATE/RESUME approved L1 nhưng actual interval hết hạn khi worker trễ | CONFIRM | advanceTimeline trước guard, chặn409 STATE_CONFLICT hoặc412 nếu aggregate vừa đổi; không giả RUNNING |
| AC-044 / BR-044 | SUSPEND REQUESTED hoặc SYSTEM STOP sau expiry | independent CHECKER CONFIRM ack | SUSPENDED hoặc STOPPED đúng nguồn; expiry không tự STOPPED; STOP giữ Contract EXPIRED |
| AC-045 / BR-045 | TERMINATE approved L1_L2 trên ACTIVE có stop/neverstarted proof | CONFIRM effectOn<=today | Nếu effectOn>=current ContractVersion.effectiveFrom: TERMINATED/STOPPED, cancel pending controls/inapplicable changes; giữ complaints/remediation, tạo CLOSEOUT. Nếu effectOn sớm hơn:409 STATE_CONFLICT ordinary prevalidation, không business effects/cache-success; giữ source/version intervals/APPLIED history |
| AC-046 / BR-046 | DataRequest do officer tự tạo | cùng officer DECIDE | 403 SOD_VIOLATION; purpose/record scope bắt buộc; không có generic historical delete |
| AC-047 / BR-047 | decision ALLOW/PARTIAL exact manifest hash/version | EXECUTE với hash khác | 409 BASE_CONFLICT; chỉ manifest approved được enqueue; DEFER/REFUSE không execute |
| AC-048 / BR-048 | hold và disposition worker cạnh tranh cùng record locks | hold trước reservation hoặc hold sau committed reservation | trước:409 HOLD_ACTIVE/no delete; sau:409 DISPOSITION_COMMITTED; cleanupfail JobFAILED chưa EXECUTED |
| AC-049 / BR-049 | DEMO_ID_COPY quá30 ngày từ verification | retention worker chạy | chỉ flag review, không autoerase; giữ nhãn synthetic, không áp class luật thật |
| AC-050 / BR-050 | CORRECTION request vào approved terms/audit | DECIDE manifest sửa approved history | 422 VALIDATION_FAILED; chỉ current metadata whitelist hoặc authorized byte/redaction disposition |
| AC-051 / BR-051 | file11MiB hoặc MIME giả hoặc scanner unavailable | upload/scan | 413 FILE_TOO_LARGE/415 MEDIA_UNSUPPORTED; unavailable SCAN_FAILED failclosed, không HUMAN_PASS |
| AC-052 / BR-052 | file thuộc contract/branch khác | attach hoặc tải bằng guessed ID | 404 NOT_FOUND; DELETE chỉ own unattached staging, lịch sử cần DataRequest |
| AC-053 / BR-053 | worker crash sau claim trước ack | restart sau lease60s; retry lỗi source obsolete | dedupe exact intent, một semantic effect; obsolete CANCELLED/guardblocked, không replay mù |
| AC-054 / BR-054 | business mutation thành công rồi duplicate replay | đọc scoped audit | một decision audit với actor/resource version/trace/time và hashes; replay không duplicate event |
| AC-055 / BR-055 | timeline worker bị dừng | dashboard/list và GET detail | list materialized có asOf/lag; detail catch-up; count và drilldown cùng scoped formula |
| AC-056 / BR-056 | list25/100, signed cursor hết15min; CSV chứa =formula | paginate/export | limit>100422, expired410 CURSOR_EXPIRED; CSV scoped <=1000/BOM/redacted/apostrophe escape |
| AC-057 / BR-057 | UI giữ draft khi server412/409/422 | submit/refresh | giữ input, focus field error, bắt refresh/diff; không tự duyệt/rebase hoặc tính authoritative quyền/hạn |
| AC-058 / BR-058 | fresh setup theo pinned Java25/Boot4.1.1/PG18.6 | build và kiểm tra module boundaries | backend modular monolith, realPG migration/transaction; generated FE client strict build; không H2 thay race tests |
| AC-059 / BR-059 | reference dataset/hardware §13.2 | đo workload/NFR và keyboard200%zoom | công bố p95/error/scan/scheduler/restore thực đo và lỗi còn lại; target chưa phải kết quả đã đạt |
| AC-060 / BR-060 | A/B được freeze và fixture typed | chạy G0..G6, real browser E2E zeroAPIstubs cùng restore/races | DoD triển khai chỉ đạt khi FE+BE checks pass; document QA không bị gọi là app integration pass |

### 15.1. Block chuẩn hóa

Catalogue dưới đây chỉ số hóa A; tên field wire chính thức nằm trong B. Không sửa block bằng FE/BE riêng lẻ.

<!-- contract:block business-catalog-v1 -->
```json
{
  "version": "1.3.0",
  "roles": [
    "BANK_ADMIN",
    "MAKER",
    "CHECKER",
    "MANAGER",
    "POLICY_AUTHOR",
    "POLICY_REVIEWER",
    "DATA_OFFICER",
    "AUDITOR"
  ],
  "permissions": [
    "SENSITIVE_READ",
    "EXPORT"
  ],
  "states": {
    "Dossier": [
      "DRAFT",
      "IN_REVIEW",
      "RETURNED",
      "APPROVED",
      "COMPLETED",
      "REJECTED",
      "CANCELLED"
    ],
    "Round": [
      "OPEN",
      "APPROVED",
      "RETURNED",
      "REJECTED",
      "CANCELLED"
    ],
    "ApprovalStep": [
      "WAITING",
      "PENDING",
      "APPROVED",
      "RETURNED",
      "REJECTED",
      "CANCELLED"
    ],
    "Policy": [
      "DRAFT",
      "IN_REVIEW",
      "SCHEDULED",
      "ACTIVE",
      "RETIRED",
      "REJECTED"
    ],
    "Contract": [
      "PREPARED",
      "SCHEDULED",
      "ACTIVE",
      "EXPIRED",
      "TERMINATED",
      "CANCELLED"
    ],
    "Service": [
      "NOT_STARTED",
      "RUNNING",
      "SUSPENDED",
      "STOPPED"
    ],
    "Change": [
      "DRAFT",
      "IN_REVIEW",
      "RETURNED",
      "APPROVED",
      "READY",
      "SCHEDULED",
      "APPLIED",
      "REJECTED",
      "CANCELLED",
      "CONFLICTED"
    ],
    "Notice": [
      "DRAFT",
      "IN_REVIEW",
      "APPROVED",
      "QUEUED",
      "DISPATCHED",
      "FAILED",
      "ACKNOWLEDGED",
      "CANCELLED"
    ],
    "Obligation": [
      "OPEN",
      "EVIDENCE_SUBMITTED",
      "RETURNED",
      "CLOSED",
      "CANCELLED"
    ],
    "Incident": [
      "OPEN",
      "VERIFIED",
      "REMEDIATION",
      "RESOLVED",
      "CLOSED"
    ],
    "ControlRequest": [
      "DRAFT",
      "REQUESTED",
      "IN_REVIEW",
      "APPROVED",
      "CONFIRMED",
      "REJECTED",
      "CANCELLED"
    ],
    "DataRequest": [
      "OPEN",
      "IN_REVIEW",
      "DECIDED",
      "EXECUTED",
      "REJECTED"
    ],
    "File": [
      "SCANNING",
      "CLEAN",
      "INFECTED",
      "SCAN_FAILED",
      "DISPOSED"
    ],
    "Job": [
      "QUEUED",
      "RUNNING",
      "SUCCEEDED",
      "FAILED",
      "CANCELLED"
    ],
    "SigningPacket": [
      "PENDING",
      "VERIFIED",
      "RETURNED"
    ],
    "ContactRequest": [
      "PENDING",
      "APPROVED",
      "REJECTED"
    ],
    "Readiness": [
      "PENDING",
      "READY",
      "REVIEW_REQUIRED"
    ]
  },
  "kinds": {
    "control": [
      "ACTIVATE",
      "SUSPEND",
      "RESUME",
      "TERMINATE",
      "STOP"
    ],
    "change": [
      "AMENDMENT",
      "RENEWAL",
      "TARIFF_NOTICE"
    ],
    "obligation": [
      "NOTICE_ACK",
      "REMEDIATION",
      "TERM_REVIEW",
      "CLOSEOUT"
    ]
  },
  "ruleIds": [
    "BR-001",
    "BR-002",
    "BR-003",
    "BR-004",
    "BR-005",
    "BR-006",
    "BR-007",
    "BR-008",
    "BR-009",
    "BR-010",
    "BR-011",
    "BR-012",
    "BR-013",
    "BR-014",
    "BR-015",
    "BR-016",
    "BR-017",
    "BR-018",
    "BR-019",
    "BR-020",
    "BR-021",
    "BR-022",
    "BR-023",
    "BR-024",
    "BR-025",
    "BR-026",
    "BR-027",
    "BR-028",
    "BR-029",
    "BR-030",
    "BR-031",
    "BR-032",
    "BR-033",
    "BR-034",
    "BR-035",
    "BR-036",
    "BR-037",
    "BR-038",
    "BR-039",
    "BR-040",
    "BR-041",
    "BR-042",
    "BR-043",
    "BR-044",
    "BR-045",
    "BR-046",
    "BR-047",
    "BR-048",
    "BR-049",
    "BR-050",
    "BR-051",
    "BR-052",
    "BR-053",
    "BR-054",
    "BR-055",
    "BR-056",
    "BR-057",
    "BR-058",
    "BR-059",
    "BR-060"
  ],
  "acceptanceIds": [
    "AC-001",
    "AC-002",
    "AC-003",
    "AC-004",
    "AC-005",
    "AC-006",
    "AC-007",
    "AC-008",
    "AC-009",
    "AC-010",
    "AC-011",
    "AC-012",
    "AC-013",
    "AC-014",
    "AC-015",
    "AC-016",
    "AC-017",
    "AC-018",
    "AC-019",
    "AC-020",
    "AC-021",
    "AC-022",
    "AC-023",
    "AC-024",
    "AC-025",
    "AC-026",
    "AC-027",
    "AC-028",
    "AC-029",
    "AC-030",
    "AC-031",
    "AC-032",
    "AC-033",
    "AC-034",
    "AC-035",
    "AC-036",
    "AC-037",
    "AC-038",
    "AC-039",
    "AC-040",
    "AC-041",
    "AC-042",
    "AC-043",
    "AC-044",
    "AC-045",
    "AC-046",
    "AC-047",
    "AC-048",
    "AC-049",
    "AC-050",
    "AC-051",
    "AC-052",
    "AC-053",
    "AC-054",
    "AC-055",
    "AC-056",
    "AC-057",
    "AC-058",
    "AC-059",
    "AC-060"
  ],
  "acceptanceOracle": [
    {
      "id": "AC-001",
      "ruleIds": [
        "BR-001"
      ],
      "given": "VA_COLLECTION doanh nghiệp",
      "when": "thực hiện ACTIVATE có xác nhận mẫu",
      "then": "chỉ ghi nhận service RUNNING; không mở VA/chuyển tiền"
    },
    {
      "id": "AC-002",
      "ruleIds": [
        "BR-002"
      ],
      "given": "maker B02 và hồ sơ B01",
      "when": "đọc detail, file, job hoặc dashboard",
      "then": "404 cho object; count/list không tiết lộ B01"
    },
    {
      "id": "AC-003",
      "ruleIds": [
        "BR-003"
      ],
      "given": "session còn sống nhưng user bị khóa",
      "when": "gửi request tiếp theo",
      "then": "401 UNAUTHENTICATED; CSRF sai trả403 trước mutation"
    },
    {
      "id": "AC-004",
      "ruleIds": [
        "BR-004"
      ],
      "given": "maker sửa evidence rồi được cấp CHECKER",
      "when": "duyệt revision chứa ID đó",
      "then": "403 SOD_VIOLATION; đổi role/reassign không xóa contributor; L1 khác L2"
    },
    {
      "id": "AC-005",
      "ruleIds": [
        "BR-005"
      ],
      "given": "BANK_ADMIN không có CHECKER",
      "when": "duyệt round",
      "then": "403 ROLE_FORBIDDEN; grant không tạo business approval"
    },
    {
      "id": "AC-006",
      "ruleIds": [
        "BR-006"
      ],
      "given": "AUDITOR thiếu SENSITIVE_READ",
      "when": "tải FULL identity image",
      "then": "403 SENSITIVE_FORBIDDEN; bản redacted được kiểm tra scope riêng"
    },
    {
      "id": "AC-007",
      "ruleIds": [
        "BR-007"
      ],
      "given": "AUDITOR thiếu EXPORT",
      "when": "xuất audit CSV",
      "then": "403 EXPORT_FORBIDDEN; không trả nội dung xuất"
    },
    {
      "id": "AC-008",
      "ruleIds": [
        "BR-008"
      ],
      "given": "Party taxId đã tồn tại",
      "when": "admin tạo cùng taxId",
      "then": "409 DUPLICATE_PARTY; một pháp nhân, có thể liên kết branch khác"
    },
    {
      "id": "AC-009",
      "ruleIds": [
        "BR-009"
      ],
      "given": "maker được reassign",
      "when": "mở và sửa draft trong relationship được giao",
      "then": "owner mới được action phù hợp; historic contributors/receipts/due giữ nguyên"
    },
    {
      "id": "AC-010",
      "ruleIds": [
        "BR-010"
      ],
      "given": "contact request expectedRelationshipVersion1 nhưng relationship2",
      "when": "checker approve",
      "then": "409 BASE_CONFLICT; contact/notice cũ không bị ghi đè"
    },
    {
      "id": "AC-011",
      "ruleIds": [
        "BR-011"
      ],
      "given": "P1 với mọi điều kiện khác đạt",
      "when": "mô phỏng fee499/500/999/1000/5000/5001",
      "then": "lần lượt BLOCKED/L1_L2/L1_L2/L1/L1/BLOCKED"
    },
    {
      "id": "AC-012",
      "ruleIds": [
        "BR-012"
      ],
      "given": "policy fee PASS nhưng mandatory check BLOCK",
      "when": "submit hoặc quyết định",
      "then": "409 LEGAL_REVIEW_REQUIRED hoặc READINESS_BLOCKED theo check; không override bằng manager"
    },
    {
      "id": "AC-013",
      "ruleIds": [
        "BR-013"
      ],
      "given": "report PASS cho config hash H1",
      "when": "sửa config thành H2 rồi xin publish",
      "then": "409 REGRESSION_FAILED; report cũ vô hiệu, cần RUN_REGRESSION mới"
    },
    {
      "id": "AC-014",
      "ruleIds": [
        "BR-014"
      ],
      "given": "hai policy IN_REVIEW cùng expected schedule1",
      "when": "reviewer độc lập approve đồng thời",
      "then": "một SCHEDULED, một409 POLICY_SCHEDULE_CONFLICT; không gap/overlap"
    },
    {
      "id": "AC-015",
      "ruleIds": [
        "BR-015"
      ],
      "given": "round OPEN pin P1; P2 bắt đầu áp dụng",
      "when": "review round cũ rồi RETURN/resubmit",
      "then": "round cũ P1; round mới P2 và contributor history giữ"
    },
    {
      "id": "AC-016",
      "ruleIds": [
        "BR-016"
      ],
      "given": "draft sourceFileId null",
      "when": "tạo parent rồi upload CLEAN/attach và SUBMIT",
      "then": "draft tạo được; submit đóng Revision bất biến với source nonnull, start từ ngày kế tiếp"
    },
    {
      "id": "AC-017",
      "ruleIds": [
        "BR-017"
      ],
      "given": "required file SCANNING hoặc CLEAN chưa human verify",
      "when": "submit/approve tương ứng",
      "then": "SCANNING chặn submit422 FILE_NOT_CLEAN; chưa verify chặn APPROVE409 READINESS_BLOCKED"
    },
    {
      "id": "AC-018",
      "ruleIds": [
        "BR-018"
      ],
      "given": "hai quyết định cùng round ETag và reviewer hợp lệ",
      "when": "approve song song",
      "then": "một200, một412; một decision, một Contract và job khi duyệt cuối"
    },
    {
      "id": "AC-019",
      "ruleIds": [
        "BR-019"
      ],
      "given": "round OPEN",
      "when": "RETURN, bổ sung, submit lại; riêng REJECT rồi clone",
      "then": "RETURN đóng round và giữ ảnh cũ; round mới policy hiện tại; REJECT terminal, clone ID mới"
    },
    {
      "id": "AC-020",
      "ruleIds": [
        "BR-020"
      ],
      "given": "final step đạt tất cả guards",
      "when": "APPROVE rồi mất kết nối/replay",
      "then": "một PREPARED Contract, artifact job và audit trong cùng transaction"
    },
    {
      "id": "AC-021",
      "ruleIds": [
        "BR-021"
      ],
      "given": "policy/customer hiện tại đã đổi sau approval",
      "when": "job render artifact",
      "then": "PDF vẫn exact approved Revision/bundle/hash; retry không nhân Contract"
    },
    {
      "id": "AC-022",
      "ruleIds": [
        "BR-022"
      ],
      "given": "signedAt trước start nhưng server verify đúng/sau start",
      "when": "VERIFY_SIGNING",
      "then": "422 LATE_SIGNING dù nhập signedAt cũ; cần linked dossier ngày mới và ký lại"
    },
    {
      "id": "AC-023",
      "ruleIds": [
        "BR-023"
      ],
      "given": "READY contract có risk mới",
      "when": "CHECKER HOLD rồi cùng người RELEASE",
      "then": "hold ghi REVIEW_REQUIRED; tự release403 SOD_VIOLATION; independent noncontributor mới giải tỏa"
    },
    {
      "id": "AC-024",
      "ruleIds": [
        "BR-024"
      ],
      "given": "readiness verified trước start, worker dừng qua boundary",
      "when": "GET contract detail khi restart",
      "then": "catch-up ACTIVE với effectiveAt boundary/recordedAt thực; Dossier COMPLETED, service vẫn NOT_STARTED"
    },
    {
      "id": "AC-025",
      "ruleIds": [
        "BR-025"
      ],
      "given": "FIXED có renewal READY đã commit trước old end; OPEN_ENDED riêng",
      "when": "chạy expiry trước apply sau boundary; thử renewal OPEN_ENDED",
      "then": "FIXED apply renewal trước expiry; OPEN_ENDED422 NO_RENEWAL; không hồi sinh expired thật"
    },
    {
      "id": "AC-026",
      "ruleIds": [
        "BR-026"
      ],
      "given": "FIXED amendment end hoặc OPEN_ENDED fee-only",
      "when": "validate change",
      "then": "fixed tính duration từ original start; open giữ null end; không chuyển termKind"
    },
    {
      "id": "AC-027",
      "ruleIds": [
        "BR-027"
      ],
      "given": "change được duyệt nhưng thiếu signing/verified tariff ack",
      "when": "COMMIT",
      "then": "409 READINESS_BLOCKED/NOTICE_NOT_READY; APPROVED chưa được SCHEDULED"
    },
    {
      "id": "AC-028",
      "ruleIds": [
        "BR-028"
      ],
      "given": "hai READY changes cùng base/schedule1, slot trống",
      "when": "COMMIT song song bằng key khác",
      "then": "một200/SCHEDULED; loser409 SCHEDULE_CONFLICT có persisted CONFLICTED+audit và cached409; một future slot"
    },
    {
      "id": "AC-029",
      "ruleIds": [
        "BR-029"
      ],
      "given": "change base cũ sau contract version mới",
      "when": "REBASE rồi xin COMMIT ngay",
      "then": "reset approvals/artifact/signing/notice readiness, carry contributors; cần approval/sign lại"
    },
    {
      "id": "AC-030",
      "ruleIds": [
        "BR-030"
      ],
      "given": "scheduled renewal tới boundary với current risk hold",
      "when": "advanceTimeline bởi expiry/apply/detail",
      "then": "CONFLICTED, không đổi terms; expiry đúng hạn và tạo STOP nếu service còn chạy"
    },
    {
      "id": "AC-031",
      "ruleIds": [
        "BR-031"
      ],
      "given": "notice draft với confirmed contact version1",
      "when": "SUBMIT rồi contact đổi version2",
      "then": "notice snapshot version1 bất biến; request SUBMIT stale409 BASE_CONFLICT"
    },
    {
      "id": "AC-032",
      "ruleIds": [
        "BR-032"
      ],
      "given": "notice approved LOCAL_CAPTURE",
      "when": "DISPATCH rồi retry job sau crash",
      "then": "một stable messageId capture; DISPATCHED không tự tạo deliveredAt/acknowledgedAt"
    },
    {
      "id": "AC-033",
      "ruleIds": [
        "BR-033"
      ],
      "given": "dispatch05/01 23:59 local, ack11/01",
      "when": "tariff COMMIT effective12/01; ack12/01 biến thể",
      "then": "12/01 đủ7 DATEdays và ack+1; ack12/01 thì sớm nhất13/01"
    },
    {
      "id": "AC-034",
      "ruleIds": [
        "BR-034"
      ],
      "given": "maker RECORD_ACK đúng notice/artifact/recipient hash",
      "when": "checker noncontributor VERIFY_ACK; rồi cancel change",
      "then": "record chưa ACKNOWLEDGED; verify mới ACKNOWLEDGED; cancel giữ notice superseded và cancel duty không còn áp dụng"
    },
    {
      "id": "AC-035",
      "ruleIds": [
        "BR-035"
      ],
      "given": "FIXED end07/01/2028 hoặc incident verified có trigger48h",
      "when": "tạo derived duties",
      "then": "TERM_REVIEW due08/12/2027 17local; REMEDIATION đúng trigger+48h; CLOSEOUT/NOTICE_ACK có source unique"
    },
    {
      "id": "AC-036",
      "ruleIds": [
        "BR-036"
      ],
      "given": "maker submit duty evidence",
      "when": "cùng contributor được cấp CHECKER APPROVE",
      "then": "403 SOD_VIOLATION; independentchecker accept CLOSED hoặc RETURN giữ history"
    },
    {
      "id": "AC-037",
      "ruleIds": [
        "BR-037"
      ],
      "given": "operative due đã qua, worker chưa chạy",
      "when": "approve late extension rồi restart worker",
      "then": "đúng một breach cho dueRevision cũ; extension không xóa; before-due extension không breach ở hạn cũ"
    },
    {
      "id": "AC-038",
      "ruleIds": [
        "BR-038"
      ],
      "given": "STATUTORY_EXTERNAL duty hoặc CONTRACTUAL thiếu bilateral proof",
      "when": "request waiver/extension",
      "then": "422 NONWAIVABLE_DUTY cho statutory; contractual thiếu proof422 VALIDATION_FAILED"
    },
    {
      "id": "AC-039",
      "ruleIds": [
        "BR-039"
      ],
      "given": "đổi future end với TERM_REVIEW chưa bắt đầu; task cũ đã breached riêng",
      "when": "apply thay đổi và supersede",
      "then": "task future thay bằng lineage; task breached cần review/basis và giữ breach, không xóa"
    },
    {
      "id": "AC-040",
      "ruleIds": [
        "BR-040"
      ],
      "given": "CLOSEOUT còn incident hoặc service RUNNING",
      "when": "checker APPROVE",
      "then": "409 UNRESOLVED_DUTY; excludes itself/CANCELLED inapplicable; chỉ close sau stop/neverstarted và external confirmations"
    },
    {
      "id": "AC-041",
      "ruleIds": [
        "BR-041"
      ],
      "given": "payment complaint nhận bên ngoài ngày trước app entry",
      "when": "tạo incident rồi bổ sung/reassign",
      "then": "receivedAt ban đầu và externalDueAt/basis giữ; không suy ra deadline luật bằng demo calendar"
    },
    {
      "id": "AC-042",
      "ruleIds": [
        "BR-042"
      ],
      "given": "incident OPEN có evidence verified và đúng trigger",
      "when": "VERIFY, duty evidence accept, RESOLVE, CLOSE",
      "then": "deadline source rõ, chỉ RESOLVE sau linked duty CLOSED, CLOSE bởi checker; không kết luận gian lận"
    },
    {
      "id": "AC-043",
      "ruleIds": [
        "BR-043"
      ],
      "given": "ACTIVATE/RESUME approved L1 nhưng actual interval hết hạn khi worker trễ",
      "when": "CONFIRM",
      "then": "advanceTimeline trước guard, chặn409 STATE_CONFLICT hoặc412 nếu aggregate vừa đổi; không giả RUNNING"
    },
    {
      "id": "AC-044",
      "ruleIds": [
        "BR-044"
      ],
      "given": "SUSPEND REQUESTED hoặc SYSTEM STOP sau expiry",
      "when": "independent CHECKER CONFIRM ack",
      "then": "SUSPENDED hoặc STOPPED đúng nguồn; expiry không tự STOPPED; STOP giữ Contract EXPIRED"
    },
    {
      "id": "AC-045",
      "ruleIds": [
        "BR-045"
      ],
      "given": "TERMINATE approved L1_L2 trên ACTIVE có stop/neverstarted proof",
      "when": "CONFIRM effectOn<=today",
      "then": "Nếu effectOn>=current ContractVersion.effectiveFrom: TERMINATED/STOPPED, cancel pending controls/inapplicable changes; giữ complaints/remediation, tạo CLOSEOUT. Nếu effectOn sớm hơn:409 STATE_CONFLICT ordinary prevalidation, không business effects/cache-success; giữ source/version intervals/APPLIED history"
    },
    {
      "id": "AC-046",
      "ruleIds": [
        "BR-046"
      ],
      "given": "DataRequest do officer tự tạo",
      "when": "cùng officer DECIDE",
      "then": "403 SOD_VIOLATION; purpose/record scope bắt buộc; không có generic historical delete"
    },
    {
      "id": "AC-047",
      "ruleIds": [
        "BR-047"
      ],
      "given": "decision ALLOW/PARTIAL exact manifest hash/version",
      "when": "EXECUTE với hash khác",
      "then": "409 BASE_CONFLICT; chỉ manifest approved được enqueue; DEFER/REFUSE không execute"
    },
    {
      "id": "AC-048",
      "ruleIds": [
        "BR-048"
      ],
      "given": "hold và disposition worker cạnh tranh cùng record locks",
      "when": "hold trước reservation hoặc hold sau committed reservation",
      "then": "trước:409 HOLD_ACTIVE/no delete; sau:409 DISPOSITION_COMMITTED; cleanupfail JobFAILED chưa EXECUTED"
    },
    {
      "id": "AC-049",
      "ruleIds": [
        "BR-049"
      ],
      "given": "DEMO_ID_COPY quá30 ngày từ verification",
      "when": "retention worker chạy",
      "then": "chỉ flag review, không autoerase; giữ nhãn synthetic, không áp class luật thật"
    },
    {
      "id": "AC-050",
      "ruleIds": [
        "BR-050"
      ],
      "given": "CORRECTION request vào approved terms/audit",
      "when": "DECIDE manifest sửa approved history",
      "then": "422 VALIDATION_FAILED; chỉ current metadata whitelist hoặc authorized byte/redaction disposition"
    },
    {
      "id": "AC-051",
      "ruleIds": [
        "BR-051"
      ],
      "given": "file11MiB hoặc MIME giả hoặc scanner unavailable",
      "when": "upload/scan",
      "then": "413 FILE_TOO_LARGE/415 MEDIA_UNSUPPORTED; unavailable SCAN_FAILED failclosed, không HUMAN_PASS"
    },
    {
      "id": "AC-052",
      "ruleIds": [
        "BR-052"
      ],
      "given": "file thuộc contract/branch khác",
      "when": "attach hoặc tải bằng guessed ID",
      "then": "404 NOT_FOUND; DELETE chỉ own unattached staging, lịch sử cần DataRequest"
    },
    {
      "id": "AC-053",
      "ruleIds": [
        "BR-053"
      ],
      "given": "worker crash sau claim trước ack",
      "when": "restart sau lease60s; retry lỗi source obsolete",
      "then": "dedupe exact intent, một semantic effect; obsolete CANCELLED/guardblocked, không replay mù"
    },
    {
      "id": "AC-054",
      "ruleIds": [
        "BR-054"
      ],
      "given": "business mutation thành công rồi duplicate replay",
      "when": "đọc scoped audit",
      "then": "một decision audit với actor/resource version/trace/time và hashes; replay không duplicate event"
    },
    {
      "id": "AC-055",
      "ruleIds": [
        "BR-055"
      ],
      "given": "timeline worker bị dừng",
      "when": "dashboard/list và GET detail",
      "then": "list materialized có asOf/lag; detail catch-up; count và drilldown cùng scoped formula"
    },
    {
      "id": "AC-056",
      "ruleIds": [
        "BR-056"
      ],
      "given": "list25/100, signed cursor hết15min; CSV chứa =formula",
      "when": "paginate/export",
      "then": "limit>100422, expired410 CURSOR_EXPIRED; CSV scoped <=1000/BOM/redacted/apostrophe escape"
    },
    {
      "id": "AC-057",
      "ruleIds": [
        "BR-057"
      ],
      "given": "UI giữ draft khi server412/409/422",
      "when": "submit/refresh",
      "then": "giữ input, focus field error, bắt refresh/diff; không tự duyệt/rebase hoặc tính authoritative quyền/hạn"
    },
    {
      "id": "AC-058",
      "ruleIds": [
        "BR-058"
      ],
      "given": "fresh setup theo pinned Java25/Boot4.1.1/PG18.6",
      "when": "build và kiểm tra module boundaries",
      "then": "backend modular monolith, realPG migration/transaction; generated FE client strict build; không H2 thay race tests"
    },
    {
      "id": "AC-059",
      "ruleIds": [
        "BR-059"
      ],
      "given": "reference dataset/hardware §13.2",
      "when": "đo workload/NFR và keyboard200%zoom",
      "then": "công bố p95/error/scan/scheduler/restore thực đo và lỗi còn lại; target chưa phải kết quả đã đạt"
    },
    {
      "id": "AC-060",
      "ruleIds": [
        "BR-060"
      ],
      "given": "A/B được freeze và fixture typed",
      "when": "chạy G0..G6, real browser E2E zeroAPIstubs cùng restore/races",
      "then": "DoD triển khai chỉ đạt khi FE+BE checks pass; document QA không bị gọi là app integration pass"
    }
  ]
}
```

### 15.2. Trạng thái bàn giao

Phạm vi 1.0 đã được owner chấp thuận. Hai tài liệu là baseline thiết kế đã qua independent FE/product và Backend/wire review, cùng kiểm tra schema/fixture/hash; dùng để triển khai song song. Toàn bộ số lượng, lịch, quyền và luật synthetic đã có oracle; không còn lựa chọn nghiệp vụ bắt buộc để FE/BE tự đoán. Khả năng hoàn thành theo lịch học/nhân lực và phân loại pháp lý khi dùng dữ liệu thật vẫn phải được đánh giá trên thông tin thực tế. Không có application code, kết nối ngân hàng, gửi người thật hoặc kiểm thử runtime được thực hiện trong nhiệm vụ đặc tả này.

## 16. Căn cứ thực tiễn Việt Nam và giới hạn áp dụng

Nghiên cứu được rà soát hai vòng và kiểm tra độc lập trước khi owner chấp thuận scope. Ngày chốt tình trạng nguồn: **11/10/2026**. Phân biệt bốn lớp: người dùng kể một vấn đề; ngân hàng công bố một cơ chế hợp đồng; nguồn chính thức thuật lại một vụ việc; văn bản pháp luật xác định quyền/nghĩa vụ theo đối tượng. Lớp trước không chứng minh lớp sau. Những trường hợp dưới đây dẫn tới quyết định sản phẩm cụ thể, không chỉ dùng để trang trí đặc tả.

### 16.1. Tình huống cộng đồng và hồ sơ thực tế

| Nguồn đã đọc / thời điểm | Điều quan sát được và độ chắc chắn | Hệ quả trong sản phẩm / trace |
|---|---|---|
| [Webketoan: hóa đơn/hạch toán khi bán qua web trung gian](https://webketoan.com/threads/3078165-xuat-hoa-don-va-hach-toan-truong-hop-ban-hang-qua-web-trung-gian/), thread2021, trao đổi liên quan14–15/10/2021 | Người tham gia hỏi cách xử lý khoản thực nhận sau phí và chứng từ. Đây là vấn đề được nêu công khai, không chứng minh ngân hàng sai hay mức độ phổ biến | Fee payer/basis phải rõ, agreement bundle/evidence và external fee/settlement confirmation; không xây sổ kế toán hoặc giả lập tiền thật. BR011/021/040 |
| [Webketoan: phí trong/phí ngoài khi thanh toán ngân hàng](https://webketoan.com/threads/2831749-ve-quy-dinh-phi-trong-phi-ngoai-va-hach-toan-phi-trong-khi-thanh-toan-qua-nh/),16/11/2015 | Câu hỏi cho thấy người gửi/người nhận có thể hiểu khác về phí. Bình luận cộng đồng không là điều khoản của mọi ngân hàng | UNAGREED chỉ draft; submit bắt BENEFICIARY theo đúng profile đã chọn. Không suy ra beneficiary agreement cho phép debit payer. BR001/011 |
| [OTOFUN: phí bảo hiểm/công chứng khi vay mua căn hộ](https://www.otofun.net.vn/threads/ve-phi-bao-hiem-chay-no-va-phi-dong-cong-chung-ho-so-khi-vay-ngan-hang-mua-can-ho.1864994/),11/05/2023 | Người đăng kể việc ký/bổ sung hồ sơ và chi phí. Domain tín dụng/bảo hiểm là tình huống lân cận, lời kể chưa được xác minh thành kết luận | Dùng làm câu hỏi stress-test về exact version/authority/bổ sung evidence, không đưa lending/insurance vào scope. BR019/021/022 |
| [Báo Chính phủ: phản hồi Eximbank về thông báo88tỷ đồng](https://baochinhphu.vn/khong-co-chuyen-eximbank-thu-88-ty-dong-phi-no-the-cua-khach-hang-102240321184501731.htm),21/03/2024; vụ việc cũng xuất hiện trong thảo luận Reddit | Nguồn thuật lời đại diện ngân hàng: nhân viên gửi thông báo máy móc thay vì đề xuất để lãnh đạo xem xét. Đây là phát biểu được dẫn nguồn, không bản án/phán quyết về chữ ký, địa chỉ hay trách nhiệm cuối cùng | Nội dung notice cần independent approval trước dispatch; dispatch/read/ack khác nhau; complaint receipt giữ nguồn ban đầu. Không xây credit-card interest engine. BR031/032/041 |

Webketoan/OTOFUN là diễn đàn cộng đồng, không tự gọi là bài Facebook đã xem. Thảo luận Reddit thứ cấp chỉ giúp tìm vấn đề/nguồn gốc; các cáo buộc chữ ký, liên hệ, bảo hiểm không trở thành factual finding của hệ thống. Facebook/TikTok/YouTube có giới hạn truy cập/khả năng đọc nội dung trong lần nghiên cứu; không tuyên bố đã xem đủ các nền tảng, không suy ra tần suất lỗi từ vài bài đăng. Dữ liệu demo không sao chép tên/thông tin cá nhân của các người đăng.

### 16.2. Thực tiễn điều khoản thu hộ

| Nguồn primary | Cơ chế tìm thấy / giới hạn | Quyết định đã chọn trong A |
|---|---|---|
| [ACB QR Portal: điều khoản dịch vụ VA doanh nghiệp](https://qrportal.acb.com.vn/dieukhoandieukien), Điều3/5/7/12/14/15 | Có xác nhận đổi contact, cơ chế thông báo phí và tiếp tục sử dụng, thỏa thuận không xác định hạn, notification, chấm dứt/nghĩa vụ phát sinh. Trang không thể hiện edition/effective date rõ; một số nhãn precedence không đủ nội dung | Confirmed contact và notice snapshot; FIXED/OPEN_ENDED; accrued duties/closeout. Demo **không nhập continued use thành consent**: chọn riêng TARIFF-ACK-7D-1 với explicit verified ack và7DATEdays. Precedence demo được xác định ở§5, không suy ra từ chỗ trống nguồn |
| [EVNSPC: quy trình kinh doanh điện năng, mẫu hợp đồng ngân hàng thu hộ](https://pcangiang.evnspc.vn/Areas/EC_Website/Attachments/Article/files/Quy%20trinh%20Kinh%20doanh%20dien%20nang%20ap%20dung%20trong%20EVNSPC.pdf), trang in234–235, Điều12/14 | Một48h từ nhận văn bản nhắc vi phạm; trường hợp thu tiền chưa chấm nợ có khiếu nại/xác minh và48h khác. Chấm nợ không đồng nghĩa ghi Có tài khoản. Có yêu cầu thỏa thuận phụ lục/gia hạn ký. Mẫu công khai không là hợp đồng đã ký hay deadline luật chung; chỗ khác có wording auto annual renewal cần làm rõ | Trigger/timestamp/evidence tách riêng; demo chọn48elapsedhours sau VERIFIED, không copy mơ hồ thành hạn quốc gia. Renewal explicit signed/approved trước boundary, không tự gia hạn theo im lặng. BR025/027/035/037/042 |

Các nguồn chứng minh có nhiều cơ chế khác nhau. Không ghép điều khoản của hai tổ chức thành “mẫu pháp luật Việt Nam”, không bảo đảm mỗi public template đang được áp dụng hoặc mọi clause hợp pháp. Profile ở A§1 là quyết định thiết kế sau owner approval.

### 16.3. Văn bản pháp luật đã đối chiếu

| Nguồn chính thức / điều kiện áp dụng | Kết luận dùng trong thiết kế / không suy rộng |
|---|---|
| [75/VBHN-NHNN ngày26/05/2026](https://datafiles.chinhphu.vn/cpp/files/vbpq/2026/7/75-vbhn-nhnn.signed.pdf), hợp nhất TT15/2024 với30/2025 và21/2026 | Điều3 phân biệt thu hộ và ủy nhiệm thu/debit mandate; Điều10 về chứng từ/quy trình nội bộ không bắt mọi ngân hàng dùng L1/L2 hay fee thresholds demo. Điều6 có minimum60calendar-day cửa sổ đề nghị và maximum30working-day xử lý theo thỏa thuận cho tra soát/khiếu nại thanh toán; **không là SLA xét duyệt dossier**. Demo payment complaint chỉ ghi external reference/receivedAt/externalDueAt/basis, không tự tuyên bố fulfill provider duties/refund |
| [Bộ luật Dân sự91/2015/QH13](https://datafiles.chinhphu.vn/cpp/files/vbpq/2016/01/91.signed.pdf), đại diện/hiệu lực/thay đổi/chấm dứt | App permission khác thẩm quyền đại diện; có ngoại lệ/ratification/đánh giá pháp lý khi đại diện không phù hợp nên phần mềm không tự kết luận VOID. Record basis, scope, validity/revocation tại signing event; termination/stop/closeout khác nhau. BR022/023/045 |
| [36/VBHN-VPQH ngày13/03/2026 về giao dịch điện tử](https://datafiles.chinhphu.vn/cpp/files/vbpq/2026/3/36-vbhn-vpqh.pdf) | Chứng cứ/thông điệp dữ liệu/chuyển đổi/chữ ký có điều kiện riêng. Scan+CLEAN+SHA256 là byte/operational evidence, không certification chữ ký số. Thay đổi Luật20/2026 có hiệu lực01/03/2027 không được áp dụng sớm tại ngày chốt nghiên cứu |
| [Luật91/2025/QH15 về bảo vệ dữ liệu cá nhân](https://datafiles.chinhphu.vn/cpp/files/vbpq/2025/7/91qh.signed.pdf) và [Nghị định356/2025/NĐ-CP](https://datafiles.chinhphu.vn/cpp/files/vbpq/2026/01/356-nd.signed.pdf), hiệu lực01/01/2026 | Purpose/minimization/basis/quyền chủ thể và dữ liệu nhạy cảm cần rà theo hoạt động. Điều19 có các trường hợp cụ thể; hợp đồng B2B không mặc nhiên là hợp đồng của mọi người đại diện/contact hoặc consent cho mọi xử lý. ND356 thay ND13 theo điều khoản hiệu lực; không tiếp tục dùng ND13 như baseline hiện hành. Identity image có rủi ro/sensitive treatment. BR046–050 và explicit sensitive grant |
| [Nghị định117/2018/NĐ-CP về thông tin khách hàng ngân hàng](https://datafiles.chinhphu.vn/cpp/files/vbpq/2018/09/117.signed.pdf) | Thông tin khách hàng pháp nhân cũng cần bảo mật. Access intra-bank không blanket forbidden và không blanket permitted; purpose/authority/scope cần rõ. Bounded status check không phát hiện văn bản thay thế; không gọi là exhaustive legal relationship certification. BR002/006/007/052/056 |
| [88/VBHN-NHNN ngày18/09/2026 về thời hạn bảo quản hồ sơ](https://datafiles.chinhphu.vn/cpp/files/vbpq/2026/9/88-vbhn-nhnn.signed.pdf), Điều2–3/phụ lục | Mục63 hồ sơ cung ứng dịch vụ thanh toán có10năm từ năm giao dịch kết thúc; mục62/62a về tài khoản/chứng từ có đối tượng và mốc khác. Đây là căn cứ phân loại, **không gán10năm cho mọi ảnh ID/contact/audit/copy**. Real classification từng record là deployment gate.3650/30/180days ở A là simulation, expiry chỉ review, hold/disposition có basis |

Luật23/2026 có hiệu lực01/12/2026 không được coi là đã áp dụng vào11/10/2026. Khi đưa dữ liệu thật hoặc vận hành thật vào hệ thống, phải cập nhật effective dates, actor/service classification, lawful processing basis, document classes/start events, signer authority và selected signed clauses bằng review chuyên môn. Đây là điều kiện triển khai ngoài môi trường synthetic đã chọn, không còn là TBD mà hai developer được phép tự đoán cho demo1.0.

### 16.4. Nối nguồn → quyết định → kiểm chứng

Fee ambiguity→explicit feePayer (AC011); supplemental signing/version confusion→immutable Revision/artifact/representation (AC019/021/022); mechanical notice response→review beforedispatch và exactack (AC031–034); collection-template trigger khác nhau→receipt/verified trigger/due revisions (AC035/037/041/042); termination/accrued duties→separate ServiceState/CLOSEOUT (AC040/044/045); legal privacy/retention→reviewed manifest/holds/no autoerase (AC046–050). Mỗi quyết định có test oracle trong§15 và operation/fixture ở B, nên nghiên cứu đã thay đổi cách sản phẩm xử lý ngoại lệ chứ không chỉ thêm citation.
