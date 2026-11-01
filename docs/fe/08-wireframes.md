# Foundation wireframes / layout contracts

Schematic only; actual Figma agent converts these into responsive autolayout frames using02 tokens. Text in brackets is component/state, not API data. Desktop width1440, review1280. No fake totals or unimplemented actions.

## Shell + scoped list (S03/S05/S09)

```text
+----------------------+-----------------------------------------------------+
| Product              | Breadcrumb        [Current scope] [Account/logout] |
+----------------------+-----------------------------------------------------+
| Role-aware nav       | Page title                    [Allowed create CTA] |
|                      | Description / data scope                           |
| Khách hàng & hồ sơ    | [Allowed filters] [Apply] [Clear]                  |
| Phê duyệt            | asOf ...      Processing lag ... [Refresh]         |
| Hợp đồng             | +------------------------------------------------+ |
| ...                  | | header | accessible label | state | next action| |
|                      | | scoped row                                  >  | |
|                      | +------------------------------------------------+ |
|                      | [Refresh first page]           [Next cursor page] |
+----------------------+-----------------------------------------------------+
```

Loading replaces table with skeleton retaining headers, not a fake empty result. Empty filtered result preserves filterbar and offers clear.403 is access-state, not an empty table.410cursor offers first-page refresh. Mobile rows become labelled cards preserving target/state/due/source.

## Wizard (S07)

```text
+ Sidebar + Header ---------------------------------------------------------+
| Hồ sơ / draftID       [DRAFT]       [Unsaved edits or newer version banner] |
| [1 Context] -- [2 Terms] -- [3 Representation] -- [4 Files] -- [5 Review]   |
| +------------------------------------------------+----------------------+ |
| | Section heading / instructions                 | Context summary      | |
| | Label *                                        | Relationship / scope | |
| | [Input]                                        | Persisted version    | |
| | Help text / exact field error                  | Server blockers      | |
| | [Date/money/select pairs]                      | Contributors         | |
| | [Upload rows: scan status + human status]       |                     | |
| +------------------------------------------------+----------------------+ |
| [Back] [Next]                    [Save draft] [Submit on final review]    |
+---------------------------------------------------------------------------+
```

Footer participates in document flow/reflows atzoom. Review/submit uses saved state; unsaved edits cannot masquerade as snapshot. Error summary above section focuses matching field. Mobile summary collapses into labelled read section, not disappeared facts.

## Review workbench (S10)

```text
+ Frozen dossier revision / Round ID / current step / due ------------------+
| [Snapshot facts] [Evidence] [Policy/rules] [Assessment history]             |
| +----------------------------------------------+------------------------+ |
| | Exact immutable source and version           | Current assigned actor | |
| | Terms / representation / confirmed contact   | L1 or L2 / contributors| |
| | Evidence file metadata & authorized viewer   | Required checks        | |
| | Assessment results, separate from scan       | Allowed action buttons | |
| | Immutable rule result / relevant reason      | Blocked action reasons | |
| +----------------------------------------------+------------------------+ |
| Timeline: submitted → L1 verified/decision → L2 → result                    |
+---------------------------------------------------------------------------+
```

Drawer preview has close/focus-return and metadata/download alternative. APPROVE is a deliberate dialog with source/step/required reason; no default selected PASS. Differentactor prototype entrypoints are not product switch-role controls.

## Contract360 (S14)

```text
+ Contract reference | [Contract state] [Readiness] [Service state] --------+
| Current version: actual number OR "Chưa có phiên bản hiệu lực"           |
| [Current terms] [Versions] [Signing] [Changes] [Duties/control] [History] |
| Artifact: actual file or PDF job pending/failed with reason               |
| Signing packet: pending verification / real assessment                   |
| Effective timeline: effectiveAt distinct from recordedAt                 |
| Current holds/blockers                 [Allowed commands + reasons]     |
+---------------------------------------------------------------------------+
```

Prototype must include PREPARED/currentVersionnull and ACTIVE/serviceNOT_STARTED, not only a green ACTIVE tile. No fake contractual version before firstACTIVE.
