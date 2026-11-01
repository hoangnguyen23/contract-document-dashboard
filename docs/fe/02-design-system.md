# Design system and reusable component specification

Đây là quyết định visual đề xuất của dự án; không thay enum/state/API. Designer dùng thống nhất và ghi thay đổi trong handoff. Không sao chép thương hiệu ngân hàng thật.

## Tokens

| Token | Giá trị |
|---|---|
| Typeface | Be Vietnam Pro, fallback system-ui; agent phải xác minh license/nguồn trước khi thêm asset vào code; có thể dùng system-ui khi chưa có font |
| Text | body14px/22px; label14/20 semibold; table13/20; heading page24/32; section18/26; caption12/18; không dùng caption cho thông tin quyết định |
| Spacing |4,8,12,16,24,32,48px |
| Canvas/surface |#F4F6FA / #FFFFFF |
| Text primary/secondary |#172B4D / #475569 |
| Brand/action |#1D4ED8; hover#1E40AF; white label |
| Border |#CBD5E1; input focus#1D4ED8 outline2px offset2 |
| Semantic |success#166534, warning#92400E, danger#B91C1C, info#1D4ED8; light tints as background; icon+text required |
| Radius/shadow |control6px, card8px; subtle shadow only drawers/dialog; no decorative gradients |
| Control |min height40px; touch target44px; reason text remains readable even if action disabled |

Designer must measure contrast in Figma; token choice is not a WCAG pass. Do not communicate state only through red/green. No heavy animation; use reduced-motion-aware feedback. No placeholder text replacing labels.

## Layout

Desktop master1440x900; required review frame1280x720. Header64px, sidebar240px expanded/64px collapsed, main padding24px. Content max1440px; detail main:flex approximately2:1 with right action/evidence summary min300px. At width below1100 move side panel below main, preserve logical reading order. At200% zoom essential actions reflow and remain reachable; sticky footer must not cover errors/focused fields.

Mobile390x844: drawer navigation,16px outerpadding, one-column form/detail. Tables use a semantic list-card variant for queues when meaningful; dense comparison/terms tables may scroll horizontally inside labelled region. Never hide key state/deadline/action just to fit. Destructive action always opens explicit review with reason/impact. No gesture-only actions.

## Component inventory and required variants

| Component | Variants/states | Contract |
|---|---|---|
| AppShell/Breadcrumb/ScopeSelector | expanded/collapsed/mobile/role-minimal | scope badge not an all-bank selector; branch change clears cursor |
| Button/IconButton | primary/secondary/quiet/danger; default/hover/focus/disabled/pending | label action-specific; pending prevents accidental doubleclick, not substitute idempotency |
| TextField/Textarea/Select/DateInput/MoneyInput | required/optional/readOnly/invalid/disabled/help | required vs nullable exact schema; VND raw decimalstring, dates local-date; timezone explicit for timestamps |
| ResourceTable | loading/empty/results/error/refreshing/cursor-expired | no invented total/page-number; next cursor + firstpage refresh; stable row IDs |
| FilterBar | default/applied/invalid/scopechanged | only query allowlist; labels for timewindow |
| StatusBadge | state-specific icon+label | keep raw state in technical tooltip; translate display only; colors do not combine independent state dimensions |
| AsyncJobPanel | queued/running/failed/cancelled/succeeded/nojob | show kind/attempt/error/source; retry only server hints; job success not automatic parenteffect |
| UploadItem | selected/uploading/quarantined/scanning/clean/infected/failed/disposed | canonical File.state labels from enum; local uploading separate; CLEAN vs humanverified separate chips |
| EvidenceViewer | metadata/redacted/full/unavailable/disposed/denied | authenticated representation; no publichref/path; download alternative to PDF preview |
| RuleResultList | block/review/pass/unknown per canonical | exact evaluator output/reason; no client policy score |
| ActionPanel | allowed/blocked/loading/stale/submitting | hints+reason, role alone cannot enable command |
| ReasonDialog | confirm/return/reject/reassign/cancel/hold | exact schema fields/limits; no invented generic optionalreason |
| ProblemBanner/FieldError | validation/auth/forbidden/notfound/conflict/server | traceId visible copyable; no stack/rawpayload; focus first invalid field |
| RevisionDiff/Timeline | unchanged/added/changed/removed; recorded/effective | distinguish no value/null/omitted; immutable history |
| Tabs/Drawer/Modal | keyboard/focus/overflow/mobile | aria labels, focus trap/return, escape unless destructive pending |
| Toast | success/info | never sole presentation of failure or requirednextaction |

## Copy conventions

Use “Đã quét an toàn” for CLEAN plus “Chưa xác minh nghiệp vụ” when applicable. Contract PREPARED: “Đã chuẩn bị — chưa có hiệu lực”. Notice DISPATCHED: “Đã gửi — chưa xác nhận nhận”. Service state has its own label; do not display ACTIVE as “Dịch vụ đang hoạt động”. Unknown enum: safe unsupportedstate banner, no default successbadge.

Confirm dialogs state action, target, consequence, required reason and next step. Example rejection: “Từ chối hồ sơ” not generic “OK”. Do not fabricate authoritative legal language, signature validity or a completion percentage.

## Accessibility handoff

Each Figma component specifies focus target/order, keyboard trigger, error/status announcement, label/description and reading order. Long data includes text alternatives; tables include header labels; dates show timezone/format. Input error is attached to its field and summarized in an error area. File scanner asynchronous updates use nonintrusive status announcement; pending command does not steal focus. Designer includes1280frame/mobile/200%zoom notes; actual keyboard/screenreader checks belong to later authorized verification.
