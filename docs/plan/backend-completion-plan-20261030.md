# Kế hoạch hoàn thành Backend — 30/10/2026

Trạng thái: kế hoạch thực hiện, chưa phải kết quả nghiệm thu. Owner yêu cầu lập kế hoạch; không chạy test, HTTP smoke, business scenario, build hoặc restart trong lượt lập kế hoạch này.

## 1. Mục tiêu và giới hạn của “hoàn thành”

Mục tiêu là hoàn tất Backend trong phạm vi canonical A/B1.3.0: source đúng nghiệp vụ, gói bàn giao tái lập được, tích hợp được với FE và có danh mục nghiệm thu rõ ràng. Không mở rộng nghiệp vụ, thêm microservices hoặc nâng công nghệ chỉ vì có phiên bản mới.

Phân biệt ba mốc:

1. **Hoàn tất phát triển BE:** đủ operation/action/target; không còn finding source chặn bàn giao; independent review; build bỏ toàn bộ test; contract và tài liệu vận hành nhất quán.
2. **Sẵn sàng bàn giao tích hợp:** xác định đúng source/JAR/runtime/contract; cấu hình HTTPS/session và tài liệu FE đủ rõ; có người tiếp nhận cùng version. Chưa đồng nghĩa tích hợp đã chạy thành công.
3. **Nghiệm thu BE và tích hợp:** có bằng chứng nghiệp vụ, quyền, concurrency/recovery, file/PDF, FE và NFR theo A/B. Khâu này tách riêng theo chỉ đạo owner. Hoàn tất mốc 1 không được gọi là hoàn thành toàn bộ G3–G6.

Kế hoạch không tự cấp quyền chạy kiểm thử. Quyết định hiện hành ngày 30/10 tách testing khỏi implementation; phê duyệt smoke và Clock ngày 21/10 được giữ làm lịch sử, không tự kích hoạt lại. Không merge PR, push main/dev, force push, reset, sửa FE, xóa dữ liệu/volume hoặc thay canonical trong nhiệm vụ lập kế hoạch.

## 2. Baseline thực tế và độ tin cậy của thông tin

| Hạng mục | Bằng chứng hiện có | Giới hạn |
|---|---|---|
| Nhánh đang lập kế hoạch | `feature/backend-smoke-fixes`, HEAD trước kế hoạch `60e4f3e308a2b103abda793bf31cc0730078d994`; worktree sạch trước chỉnh tài liệu | Thay đổi kế hoạch sau HEAD này là docs-only |
| Remote | `git ls-remote` ngày 30/10: dev `df34af6`, main `b536f6e`, fixes `60e4f3e`, manual-smoke `d352f0d` | Không suy trạng thái remote tương lai; refs origin cục bộ có thể cũ |
| Coverage | Inventory/reviews hiện tại: 85 implemented, 0 partial, 0 missing | Đây là coverage source theo review trước, không phải 85 operation đã nghiệm thu; lượt lập kế hoạch không audit lại mọi dòng source |
| Sửa lỗi gần nhất | Application `81c3d7f66628a397aa9c2d62dd26877448c25d10`: email synthetic domain và 10 JSONB operators trong prepared SQL | Hai nguyên nhân P1, biểu hiện ở ba HTTP case; source review GO, chưa retest |
| Build | 108 Java files, package thành công 30/10 07:51:56+07, test resources/compile/run đều skipped | Java thực tế 25.0.1, chưa khớp Oracle25.0.4.1 của A/B |
| JAR candidate | SHA256 `b29274dc1dc401ac4c0019233449f01e8cd85e2e0e8b6ceddaf43da867e26fe3` | Không thay tên baseline cũ để làm mất FAIL |
| Startup | Báo cáo runtime 30/10: API ready tại 8081, Flyway V001–V013 hợp lệ, dữ liệu seed giữ nguyên | Startup không chứng minh workflow; không kiểm tra lại PID trong lượt lập kế hoạch |
| Smoke cũ | Session/CSRF, Party/idempotency, Dossier DRAFT, 3 CLEAN files, Policy DRAFT qua RUN_REGRESSION; evidence đến s31 | Chưa có Revision/Round/Contract/PDF artifact/signing/readiness/effectivity trong run này |
| Git tích hợp | dev đã nhận stack BE trước sửa lỗi; PR11 được ghi Draft ở checkpoint trước | Sửa lỗi chưa nằm trong dev. Không xác minh lại trạng thái PR bằng API trong lượt này; cần kiểm tra lúc delivery |
| FE | Audit toàn nhánh 30/10 ghi FE/setup/test refs chỉ có README ban đầu; external FE acknowledgement còn thiếu | Developer FE có thể có công việc ngoài các refs đó; không kết luận FE chưa làm gì |

Nguồn: [coverage](backend-operation-coverage.md), [handoff](backend-final-handoff.md), [acceptance matrix](backend-acceptance-matrix.md), [repair review](reviews/backend-manual-smoke-repairs-review.md), [runtime](reviews/backend-repair-runtime-20261030.md). Branch audit và smoke record nằm trên `feature/backend-manual-smoke@d352f0d`: `docs/plan/analysis/git-branches-audit-20261030.md`, `docs/plan/manual-smoke-execution.md`. Phải hợp nhất thông tin lịch sử có chọn lọc, không lấy nguyên các đoạn trạng thái cũ làm trạng thái hiện tại.

## 3. Những khoảng trống đã xác định

- Ba FAIL cũ s07/s12/s18 chưa có kết quả retest trên JAR sửa lỗi. Đây là thiếu bằng chứng kiểm chứng, không có nghĩa phải viết lại repair đã được review.
- Runtime/CI lệch pin: A§13/B§2 ghi Oracle25.0.4.1; build local25.0.1; `.github/workflows/backend-build.yml` dùng Temurin `java-version: '25'`. CI đã tồn tại và đã skip tests, không cần dựng CI mới từ đầu.
- Các checkpoint mới và báo cáo cũ cùng tồn tại; acceptance matrix vẫn mở đầu bằng source/startup20/10. Verification plan còn tiêu đề NOT AUTHORIZED trong khi đã từng có smoke giới hạn rồi tạm ngừng testing. Cần bảng trạng thái hiện hành và giữ riêng lịch sử.
- Source fix, docs smoke, remote dev và runtime checkout là những revision khác nhau; cần một manifest bàn giao rõ ràng, không dùng tên thư mục/nhánh để suy JAR đang chạy.
- External FE acknowledgement, provider/consumer execution, browser workflow, races/recovery/restore và NFR còn thiếu. Không thể lấp các khoảng này bằng source review.

Các mục audit ở phần sau là **phạm vi cần đối chiếu**, chưa phải lỗi đã xác nhận. Chỉ tạo nhiệm vụ sửa source khi có BR/AC, vị trí code và đường đi dẫn đến vi phạm cụ thể. Không mặc định mọi SERVICE_UNAVAILABLE là stub: fail-closed khi scanner/storage/source không hợp lệ có thể đúng canonical.

## 4. Thứ tự giao việc và điều kiện hoàn tất

| ID / ưu tiên | Phạm vi công việc | Phụ thuộc | Đầu ra và điều kiện đóng phần phát triển |
|---|---|---|---|
| BE-C01 / P0 | Chốt baseline, inventory, lịch sử phê duyệt và trạng thái hiện hành | Không | Một bảng source/build/startup/business/FE/NFR; exact app+docs+contract+JAR identities; 85 operation đối chiếu đầy đủ action/target, không chỉ đếm route; không còn trạng thái hiện tại mâu thuẫn giữa INDEX/plan/handoff/matrix |
| BE-C02 / P0 | Rà soát các invariant dùng chung | C01 | Review current authorization/scope/SoD, request/response, ETag, idempotency, transaction/audit/job, SQL binding và lock order; findings cụ thể được sửa/review riêng, hoặc ghi không thấy finding trong phạm vi đã đọc |
| BE-C03 / P0 | Khép source hợp đồng gốc | C02 | Trace Policy→Revision→Round→PREPARED→PDF→scan/download→SigningPacket→readiness→effectivity; mỗi cạnh có implementation, guard, owner và bằng chứng review |
| BE-C04 / P1 | Khép source thay đổi/gia hạn/thông báo | C03 | AMENDMENT/RENEWAL/TARIFF_NOTICE đủ đường đi; exact base/version, rebase, slot, ACK và boundary được đối chiếu; không có effect sinh hai lần hoặc lấy readiness cũ |
| BE-C05 / P1 | Khép nghĩa vụ/incident/control/closeout | C04 | Trace source→duty→incident→control→end proof→closeout; không tạo xác nhận ngân hàng giả; giữ lịch sử breach và guard termination1.3.0 |
| BE-C06 / P1 | Khép privacy/file/reporting | C05 | Manifest/hold/safe-copy/correction/delete/tombstone và ancestry scope đầy đủ; reporting đọc đúng dữ liệu của các producer đã có; không đọc bản FULL thay thế khi REDACTED lỗi |
| BE-C07 / P0 trước bàn giao | Chốt toolchain, cấu hình và runbook | C01; chạy song song với C02–C06 nếu tách file | Exact pin hoặc quyết định owner về baseline khác; skipped-test build có identity; mô tả fresh setup, TLS, secrets, jobs, backup DB+files và rollback tương thích; không tuyên bố restore đã chạy |
| BE-C08 / P1 | Gói bàn giao FE và khâu kiểm thử | C01; hoàn thiện sau C03–C07 | Bảng operation/schema/error/action impact; cùng contract hash; tài liệu pending jobs và protocol; danh sách case theo oracle độc lập; FE acknowledgement ghi đúng thực tế |
| BE-C09 / P0 trước chốt | Review release candidate và bàn giao Git | C02–C08 | Independent source review không còn finding chặn; build/manifest cập nhật theo đúng candidate; PR mô tả rõ NOT RUN; owner quyết định merge. Không đòi BE tự hoàn thành công việc của FE |
| BE-V01…V05 / khâu riêng | Nghiệm thu nghiệp vụ, tích hợp, recovery và NFR | Candidate C09 + phạm vi owner mở lại | Kết quả thực đo và bằng chứng theo phần 8; không thực hiện trong giai đoạn phát triển hiện hành |

C02–C06 tận dụng module reviews đã có, tập trung điểm nối và phần thay đổi; không lặp lại toàn bộ review lịch sử chỉ để tạo thêm tài liệu. Nếu audit không tìm thấy lỗi cụ thể thì giữ nguyên source. Nếu sửa dùng chung, đánh giá lại tất cả caller chịu ảnh hưởng trước khi chuyển task. Mỗi task có người triển khai và người review độc lập; root quản lý docs/Git, không tự gọi một lần đọc là independent review.

## 5. Checklist kỹ thuật cho các chuỗi nghiệp vụ

### C02 — nền tảng và invariant xuyên module

- Mọi operation/action kiểm tra user active, role/grant/branch/assignment hiện tại; scope trước list/count/file/export/job và trước replay kết quả nhạy cảm. Thay role không xóa contributor history; lịch sử quyết định hợp lệ không bị viết lại theo quyền hiện tại.
- Request/response giữ required/nullable/enum/date/money/error của B; đặc biệt response validation có thể gây503 dù mutation path đã chạy. Đối chiếu transaction boundary, không chỉ handler tồn tại.
- ETag/version check và idempotency fingerprint/replay/conflict/expiry theo canonical; phân biệt lỗi prevalidation với trường hợp nghiệp vụ commit rồi trả409 đã được B quy định.
- State + audit + durable intent + idempotency result cùng ranh giới giao dịch; unique constraints, aggregate lock và lock order bảo vệ cùng một invariant. Không giữ DB transaction khi chờ scanner ngoài nếu contract yêu cầu tách.
- Review cả 5 worker: FILE_SCAN, ARTIFACT_PDF, EFFECTIVITY, NOTICE_DISPATCH, DATA_DISPOSITION. Nêu claim/lease/heartbeat/fencing/dedupe/current-source/retry và cancellation; job SUCCEEDED phải gắn với effect thực, không chỉ enqueue.

### C03 — hợp đồng gốc, ưu tiên cao nhất về nghiệp vụ

- Policy publish/schedule có nguồn thực; Revision và policy/route snapshot bất biến; return/resubmit sinh round mới, không sửa bằng chứng round cũ.
- L1/L2 đúng thứ tự, khác nhau và độc lập contributors; final approval tạo đúng một PREPARED và ARTIFACT_PDF intent trong giao dịch.
- Renderer lấy approved source/hash/template; file phải tồn tại, đúng bytes/hash, scan CLEAN và đúng parent/scope trước download. CLEAN không thay human verification.
- SigningPacket bind đúng artifact, hai bên, thẩm quyền tại thời điểm ký và independent verifier; không biến bằng chứng ghi nhận thành chứng thực chữ ký mật mã nếu canonical không yêu cầu.
- Thời điểm hoàn tất verification lấy sau byte guards; readiness đúng hạn và hold/release đúng quyền. EFFECTIVITY kiểm tra source/current guards, không ACTIVE sớm, không đồng nhất contract ACTIVE với dịch vụ đã hoạt động.

### C04 — Change, Renewal, Notice

- Mọi loại Change đủ các đường approve→artifact→sign hoặc notice/ACK→commit→apply; rebase vô hiệu hóa proof/readiness cũ đúng quy tắc.
- COMMIT kiểm tra base/schedule/slot; APPLY khóa Contract, tạo đúng version và consequence; expiry/gia hạn theo boundary A§7.3, phân biệt effectiveAt/recordedAt.
- Notice đóng băng recipient/content/attachments; local capture duy nhất và DISPATCHED atomic, không đồng nghĩa delivered/ACK. RECORD_ACK và VERIFY_ACK độc lập.
- Tariff bám điều khoản, số ngày lịch và thời điểm verified ACK theo A; ACK hợp lệ vẫn được giữ khi Change bị chặn.

### C05 — Obligation, Incident, Control

- Producer thực cho cả bốn loại obligation; extension không xóa historical breach; nghĩa vụ statutory không được waive/extend tùy ý.
- Incident trigger, remediation, resolution evidence và quyền độc lập nhất quán; không mô phỏng tiền đã hoàn/đã chuyển.
- ACTIVATE/RESUME/SUSPEND/STOP/TERMINATE phân biệt request, approval, acknowledgement và trạng thái dịch vụ thật được ghi nhận.
- TERMINATE giữ guard `currentVersion.effectiveFrom <= effectOn <= today`; không lùi timeline, đảo APPLIED Change hoặc xóa nghĩa vụ/incident phải tiếp tục. CLOSEOUT cần end proof và các điều kiện còn lại.

### C06 — Privacy, Files, Reporting

- Exact disposition manifest/version/hash, independent decision và execution authority; global hold khóa đúng target đối với reservation/execution.
- Safe copy thực, CLEAN, đúng source và independent review; correction phải đi qua confirmed-contact proof; DELETE_BYTES giữ tombstone và truthful incomplete state khi cleanup lỗi.
- File/audit/job/reporting kế thừa đúng ancestry và scope, không lộ dữ liệu qua count, cursor, export, download hoặc đường retry.
- Dashboard/KPI/drilldown dùng cùng asOf, trạng thái và lag theo canonical; CSV giới hạn/redaction/formula escaping. Đối chiếu writer/read model của C03–C05 trước khi tối ưu truy vấn.

## 6. Toolchain và vận hành cần chốt

1. Kiểm tra khả năng cung cấp exact JDK/vendor/build từ nguồn chính thức, checksum và dependency graph. Đây là công việc tương lai, chưa xác minh upstream trong lượt lập kế hoạch. Không coi số pin trong tài liệu là bằng chứng bản phân phối hiện có.
2. Nếu exact pin dùng được, cấu hình build local/CI cùng baseline và ghi actual version. Nếu không dùng được, trình owner concrete technical-baseline patch trước khi đổi A/B; không tự thay bằng “latest”. Giữ CI không chạy test theo chỉ đạo hiện tại.
3. Mô tả profile local/demo/integration, biến bắt buộc và secret provisioning; kiểm tra cấu hình mặc định phát triển không bị dùng nhầm ngoài local. Không đưa secrets/cookies/private evidence vào Git.
4. BE chuẩn bị reverse-proxy/runtime contract cho same-origin HTTPS; FE review origin/CSRF/client assumptions. Không giảm Secure cookie, nới wildcard CORS hoặc thay schema để vượt lỗi kết nối.
5. Runbook nêu process/container/volume ownership, ports, storage path, scanner signatures, queue lag/failure, trace ID, startup/shutdown và xử lý lỗi mà không reset dữ liệu.
6. Backup/restore phải ghép DB với private bytes/hash; viết quy trình và điều kiện nhất quán trước. Việc thử restore/fresh migration/failure thuộc khâu nghiệm thu. Không sửa V001–V013 đã áp dụng; migration mới chỉ khi có finding cụ thể và đúng owner.
7. Gói candidate gồm source/docs/contract hashes, JAR checksum, dependency/container identities, cấu hình đã che secrets, giới hạn đã biết và rollback tương thích schema. Không rollback JAR mù nếu schema không tương thích.

## 7. Bàn giao FE và quy trình Git

Mỗi thay đổi ảnh hưởng FE phải có operation ID, schema/field/status liên quan, breaking wire hay thay semantics, tác động UI, owner approval và trạng thái external acknowledgement. Không sửa `apps/web/**`.

Các mục cần đưa vào handoff hiện tại:

| Thay đổi | Operation/field | Mức ảnh hưởng |
|---|---|---|
| Email repair | `createRelationship`, `getRelationship`; Contact.email và các schema dùng format=email | Khôi phục validation đúng canonical, không đổi schema/quyền; chưa retest |
| JSONB repair | Các caller của EvidenceAccess/ReportingScope/ListQueries, có `listFiles` đã FAIL | Không đổi wire; C01 phải liệt kê chính xác caller/operation chịu ảnh hưởng, không giới hạn ở một endpoint |
| S060 | `getContract`, Contract.allowedActions | MAKER không có HOLD_READINESS; owner đã duyệt; FE acknowledgement còn chờ |
| S024 | `commandNotice`, Notice.allowedActions | ACK recorder không có VERIFY_ACK; owner đã duyệt; FE acknowledgement còn chờ |
| Termination1.3.0 | `commandControlRequest`, existing409 STATE_CONFLICT và ControlRequest action hints | Thay điều kiện ngày đã duyệt, không đổi schema; FE giữ source/input và hướng dẫn tạo request mới khi cần |

Handoff còn phải giải thích session/CSRF, If-Match, idempotency và các lỗi canonical; allowedActions/blockedActions không thay server authorization; polling jobs và các trạng thái pending/failed/disposed; file download có xác thực; readiness/ACTIVE không đồng nghĩa service execution.

Giữ PR11 và lịch sử lỗi hiện có. Task mới dựa trên reviewed candidate có ghi dependency; nếu PR11 chưa merge có thể dùng feature/worktree kế tiếp với base commit rõ, không chặn đọc/sửa vì thiếu merge và không tạo nhánh từ main README-only. Khi đến delivery, kiểm tra lại remote/PR; chỉ owner quyết định merge vào dev. Không squash/rebase/reset/merge tự động hoặc xóa worktree để “dọn” kế hoạch. Docs smoke được chuyển/hợp nhất có chọn lọc bằng normal commit và giữ provenance; không overwrite canonical hoặc nguyên trạng thái BLOCKED cũ.

## 8. Kế hoạch khâu kiểm thử riêng — chưa thực hiện

| Gói | Nội dung và thứ tự | Bằng chứng bắt buộc |
|---|---|---|
| V01 | Mở lại môi trường biệt lập khi owner mở giai đoạn testing; kiểm tra thời gian/dữ liệu/hash; retest s07/s12/s18 trước, rồi original workflow từ Policy/Revision đến hiệu lực | Expected lấy từ A/B trước request; sanitized request/response; state và DB/audit/job/file effects; PASS/FAIL/BLOCKED/NOT RUN. Không đổi FAIL cũ thành PASS của candidate |
| V02 | Change/renewal/notice/ACK, nghĩa vụ/incident/control/closeout và privacy/reporting | Cả success và denied scope/SoD/current guard, evidence thật; không mở rộng từ smoke cũ sang toàn bộ domain nếu chưa được phép |
| V03 | Race, replay, lease/fencing, retry/crash recovery, scanner/storage failure, hold/disposition | Không duplicate hoặc mất effect/audit; fail-closed; run biệt lập và approval riêng cho fault injection/concurrency |
| V04 | FE provider/consumer + browser theo G2→G3→G4→G5, cùng contract version | Real BE/PG/files/jobs, không API mock cho acceptance; FE owner tham gia; automated tests chỉ khi owner cho phép riêng |
| V05 | Fresh setup/coherent DB+file restore, NFR A§13.2 và release review | Actual hardware/load/version; latency/error/queue/recovery measurements; không tuyên bố production readiness từ startup/manual smoke |

V01 là điểm mở testing hợp lý đầu tiên; V02/V03 có thể được phân nhỏ theo rủi ro sau khi V01 ổn định. Chuẩn bị integration FE sớm, chạy browser nền tảng ngay khi hai bên và quyền thực thi đã sẵn sàng, không cần chờ xong mọi NFR. Không ấn định ngày hoàn thành khi chưa biết năng lực FE/khâu kiểm thử và số finding phát sinh.

Thời gian thực đã thay đổi từ21/10: không dùng nguyên session/startOn/policy schedule cũ hoặc làm clock lùi trên dữ liệu đã tiến. Clock proposal cũ chỉ dùng lại sau đối chiếu đúng phạm vi và approval hiện hành; không tự đổi giờ máy/DB. Giữ project/DB/storage/process riêng và không ảnh hưởng API8081.

## 9. Tiêu chí chốt và việc làm ngay tiếp theo

Chốt phần phát triển khi C01–C09 có deliverable thực, review độc lập và không còn finding chặn. Sổ lỗi phải tách **source fixed / review passed / build passed / runtime retest pending**, không chỉ một chữ DONE. Mọi operation/action/target phải truy về BR/AC; mọi khoảng trống nghiệm thu có owner và gói V tương ứng. Không tính phần không chạy thành N/A để nâng tỷ lệ hoàn thành.

**Tranche tiếp theo: C01 + phần đối chiếu toolchain C07.** Hoàn tất một checkpoint bàn giao duy nhất, xác định caller của repair và mismatch runtime/CI; sau đó C02–C03 là tranche review/sửa nghiệp vụ ưu tiên. Không chạy lại repair build khi source không đổi chỉ để lấy thêm một dấu PASS. Nếu cần đổi pin, trình patch cụ thể; nếu không có lỗi source mới, không tạo code hoặc abstraction để lấp thời gian.

Kết thúc mỗi tranche báo: branch/commit; API/action/target thực sự thay đổi; dependency đã khép; finding còn mở; review/build evidence; test NOT RUN; FE impact; tranche tiếp theo. Chỉ tuyên bố nghiệm thu toàn BE khi các gói nghiệm thu bắt buộc có bằng chứng tương ứng, không suy từ đủ85 handlers.
