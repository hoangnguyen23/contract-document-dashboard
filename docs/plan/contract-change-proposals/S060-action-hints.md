# S060 action hint correction proposal — owner approved; internal shared review pending

Status: OWNER APPROVED on 18/10/2026. Exact patch implemented in A/B1.2.1 and regenerated derived contracts; internal shared review pending. No external FE developer acknowledgement is claimed. Frontend files and application authorization are unchanged.

## Concrete inconsistency

Canonical scenarios-v1 scenario S060 uses actorId 00000000-0000-4000-8000-000000000004. seed-v1 core User for that actor has roles=[MAKER], permissions=[]. Yet expected.body.allowedActions is ["HOLD_READINESS"]. A section6.2 and B section6 Contract row reserve HOLD_READINESS to CHECKER; B section6 limits action hints to the current role/workflow. The new implementation therefore correctly returns no HOLD action for this maker.

## Proposed exact semantic patch

Keep S060 actor, request, clock, successful ACTIVE response, on-time readiness, effect/history expectations, source hashes and server authorization unchanged. Change only:

```diff
 scenarios-v1.scenarios[id=S060].expected.body.allowedActions
- ["HOLD_READINESS"]
+ []
```

The action is not moved into blockedActions for MAKER: it is outside that actor's role/workflow union. No role grant or permission broadening is proposed. Regenerate the extracted scenario block/runtime copy and dependent manifest hashes through the normal shared contract process only after approval; agree version metadata with FE before publication. No other fixture response is to be rewritten from application output.

## FE/owner impact

- Operation: getContract.
- Field: Contract.allowedActions for the S060 maker fixture.
- Schema/required/enum/status/permission changes: none.
- Breaking runtime API change: no; this aligns the fixture hint with the existing CHECKER-only rule.
- FE impact: its S060 mock/expectation must stop rendering HOLD_READINESS for the maker; CHECKER behavior is unchanged.
- Owner approval: approved 18/10/2026: “Chấp thuận patch S060 và shared review”.
- Internal shared contract review: pending independent review of this candidate. External FE acknowledgement: pending; no message has been sent to the separate FE developer.

Until reviewed, do not report exact fixture integration or full G3 acceptance. This proposal does not change the source-review verdict or turn build/startup into workflow acceptance.