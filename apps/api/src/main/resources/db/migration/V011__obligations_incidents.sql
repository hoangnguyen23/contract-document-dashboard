create table cg_g5_event (
 ordinal bigint generated always as identity unique, id uuid primary key, target_type text not null check(target_type in ('Obligation','Incident')),
 target_id uuid not null, kind text not null, body jsonb not null, body_hash char(64) not null,
 actor_id uuid, recorded_at timestamptz not null
);
create index cg_g5_event_target on cg_g5_event(target_type,target_id,recorded_at,id);
create trigger cg_g5_event_immutable before update or delete on cg_g5_event for each row execute function cg_immutable_audit();
create table cg_obligation_due (
 obligation_id uuid not null, number integer not null check(number>0), due_at timestamptz not null,
 effective_at timestamptz not null, decision_by uuid, basis text not null,
 primary key(obligation_id,number)
);
create trigger cg_obligation_due_immutable before update or delete on cg_obligation_due for each row execute function cg_immutable_audit();
insert into cg_obligation_due(obligation_id,number,due_at,effective_at,decision_by,basis)
 select e.id,(d->>'number')::integer,(d->>'dueAt')::timestamptz,(d->>'effectiveAt')::timestamptz,(d->>'decisionBy')::uuid,d->>'basis'
 from cg_entity e cross join lateral jsonb_array_elements(e.body->'deadlineHistory') d where e.entity_type='Obligation';
create index cg_obligation_operative_due on cg_entity(((body->>'currentDueAt')::text),id) where entity_type='Obligation' and body->>'state' not in ('CLOSED','CANCELLED');
