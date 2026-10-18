# S024 exact action-hint correction — owner approved; internal shared review GO

Independent static review of all63scenario expected bodies found one remaining high-confidence role/SoD hint inconsistency after S060. S024 uses MAKER actor00000000-0000-4000-8000-000000000004, also Notice contributor/ACK recorder, while expected.body.allowedActions contains VERIFY_ACK. A8.1/B6 Notice require an independent noncontributor CHECKER.

Exact proposed behavioral delta in canonical scenarios-v1:

scenario[id=S024].expected.body.allowedActions: ["VERIFY_ACK"] -> []

Keep actor, RECORD_ACK request, status200, DISPATCHED state, immutablecandidate and durableeffects unchanged. Do not move the CHECKER action into maker blockedActions or grant permissions. Schema/fields/enum/authorization unchanged. FE maker should not render a verify button; checker behavior stays unchanged. After owner approval: technical patchversion metadata/hash/extractions and independent BE-wire/FE-consumer compatibility review; external FE acknowledgement remains separate.

Owner explicitly approved on 18/10/2026: “Chấp thuận patch S024 và shared review”. Exact patch implemented in canonical A/B1.2.2 and regenerated six blocks/runtime copies. Internal independent shared review GO at 129642e; external FE developer acknowledgement pending. No schema, permission, Java, migration or Frontend source changes; implementation continues independently.