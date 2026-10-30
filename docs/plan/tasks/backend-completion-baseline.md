---
id: BE-C01
scope: Backend delivery baseline and current-status reconciliation
status: done
depends-on: []
---

# Chốt baseline bàn giao Backend

## Objective

Thực hiện C01 trong [kế hoạch30/10](../backend-completion-plan-20261030.md): một checkpoint hiện hành phân biệt source, build, startup và nghiệm thu; giữ lịch sử FAIL/approval; map đầy đủ operation/action/target và caller bị repair tác động.

## Context

- `docs/Final_Project_Specification.md`, đặc biệt §13–15.
- `docs/FE_BE_Shared_Development_Contract.md`, đặc biệt §1–9; contracts canonical1.3.0.
- `docs/INDEX.md`, `docs/plan/README.md`, coverage/inventory/acceptance matrix/handoff.
- Repair review và runtime report30/10; branch audit và smoke record tại `feature/backend-manual-smoke@d352f0d`.
- Owner30/10: test là khâu riêng; không viết/chạy automated tests, HTTP smoke hoặc business scenarios trong task.

## Path

Sửa operational docs trong `docs/plan/**`, link `docs/INDEX.md` khi cần. Đọc `apps/api/**`, contracts, CI và Git để đối chiếu. Không sửa source, canonical/derived contracts, runtime config, FE hoặc migrations trong task này. Finding kỹ thuật cần task riêng và independent review.

## Deliverables

1. Source application81c3d7f, docs delivery60e4f3e trước task, candidate JAR checksum và canonical hash có provenance; remote/PR state có thời điểm kiểm tra.
2. 85 operations đối chiếu registry/dispatch/action/target, không suy coverage từ count; danh sách cụ thể các caller operation chịu ảnh hưởng email/JSONB repair. Nếu chưa đọc đủ, ghi phạm vi còn pending thay vì kết luận đủ.
3. INDEX/README/handoff/matrix/verification plan có current-status rõ; các đoạn20/21October giữ nhãn historical. Manual smoke từng được phép và đã chạy một phần; testing hiện tại deferred. Không xóa/làm đẹp FAIL.
4. Handoff trạng thái FE và danh sách decision/toolchain gap; không xác nhận external FE approval khi chưa có.
5. Bảng finding: ID, BR/AC, file/line, nguyên nhân, mức độ, ảnh hưởng FE, task sở hữu, evidence và bước nghiệm thu tương lai.

## Verification

Review tài liệu/source mapping và diff; independent reviewer xác nhận phạm vi và không đổi semantics. Không test, build lại, HTTP request, Docker/process restart hoặc data mutation. Review chỉ đóng baseline task; không đóng acceptance. Giữ worktree/history, không tự merge/push main/dev. Không yêu cầu owner phê duyệt lại các sửa docs thông thường đã thuộc scope.

## Progress — 30 October

Static reconciliation saved at `../analysis/backend-baseline-20261030.json`: canonical/inventory operation sets and referenced source-path existence compared. This is not action/target semantic certification. Canonical hashes recorded; verification proposal now distinguishes prior partial smoke from current testing deferral. Caller-level repair mapping and independent review remain pending; C01 is not done.

## Closure — bounded delivery reconciliation

Independent caller review: `../reviews/backend-baseline-caller-review-20261030.md`. Canonical/inventory85-operation sets match;14command action sets match; actual JSONB caller and24-operation email schema reach recorded. Two stale CLOSEOUT labels corrected from existing Control producer proof. New current-status entry links historical reports; API README and verification proposal distinguish partial prior smoke/current deferral. PR11 independently observed open Draft by GitHub API, head60e4f3e/base dev; no merge. Full action/target semantics remain attributed to prior module reviews plus C02/C03 current review, not recertified by literal matching. New JOB-FENCE-01 is owned by the C02 repair, not hidden by C01 closure. This closes documentation/static reconciliation only.
