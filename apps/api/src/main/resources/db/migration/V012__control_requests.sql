create table cg_control_revision (
 id uuid primary key, request_id uuid not null, revision jsonb not null, metadata jsonb not null,
 source_hash char(64) not null, submitted_by uuid, submitted_at timestamptz not null
);
create index cg_control_revision_request on cg_control_revision(request_id,submitted_at,id);
create table cg_control_confirmation (
 request_id uuid primary key, contract_id uuid not null, kind text not null, revision_id uuid not null references cg_control_revision(id),
 body jsonb not null, body_hash char(64) not null, assessed_by uuid not null, observed_at timestamptz not null, recorded_at timestamptz not null
);
create index cg_control_confirmation_contract on cg_control_confirmation(contract_id,recorded_at,request_id);
create table cg_control_event (
 id uuid primary key, request_id uuid not null, kind text not null, body jsonb not null, body_hash char(64) not null, actor_id uuid, recorded_at timestamptz not null
);
create trigger cg_control_revision_immutable before update or delete on cg_control_revision for each row execute function cg_immutable_audit();
create trigger cg_control_confirmation_immutable before update or delete on cg_control_confirmation for each row execute function cg_immutable_audit();
create trigger cg_control_event_immutable before update or delete on cg_control_event for each row execute function cg_immutable_audit();

alter table cg_contract_effect drop constraint cg_contract_effect_kind_check;
alter table cg_contract_effect add constraint cg_contract_effect_kind_check check(kind in ('ACTIVE','EXPIRED','TERMINATED'));
