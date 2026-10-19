-- The schedule row is both the serialisation point and the durable activation intent.
create table cg_policy_schedule (
    singleton boolean primary key default true check(singleton),
    version integer not null default 0 check(version>=0),
    active_policy_id uuid,
    future_policy_id uuid,
    future_from date,
    check((future_policy_id is null)=(future_from is null)),
    check(active_policy_id is null or active_policy_id is distinct from future_policy_id)
);
insert into cg_policy_schedule(singleton,version) values(true,0);
create table cg_policy_interval (
    policy_id uuid primary key,
    effective_from date not null,
    effective_to date,
    check(effective_to is null or effective_to>effective_from),
    exclude using gist(daterange(effective_from,effective_to,'[)') with &&)
);
-- One future slot in the singleton row; no fabricated ACTIVE policy is seeded.
create unique index cg_policy_one_active on cg_entity ((body->>'state')) where entity_type='Policy' and body->>'state'='ACTIVE';
create unique index cg_policy_one_scheduled on cg_entity ((body->>'state')) where entity_type='Policy' and body->>'state'='SCHEDULED';
