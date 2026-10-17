create table cg_contract_contributor (
 contract_id uuid not null, actor_id uuid not null, created_at timestamptz not null,
 primary key(contract_id,actor_id)
);
create table cg_signing_input (
 packet_id uuid primary key, contract_id uuid not null references cg_original_agreement(contract_id),
 input jsonb not null, input_hash char(64) not null, file_manifest jsonb not null, submitted_by uuid not null,
 created_at timestamptz not null
);
create trigger cg_signing_input_immutable before update or delete on cg_signing_input
 for each row execute function cg_original_source_immutable();
create table cg_signing_assessment (
 packet_id uuid primary key references cg_signing_input(packet_id), assessment jsonb not null,
 assessed_by uuid not null, assessed_at timestamptz not null
);
create trigger cg_signing_assessment_immutable before update or delete on cg_signing_assessment
 for each row execute function cg_original_source_immutable();
create table cg_contract_version (
 id uuid primary key, contract_id uuid not null references cg_original_agreement(contract_id),
 number integer not null check(number>0), body jsonb not null,
 unique(contract_id,number)
);
create trigger cg_contract_version_immutable before update or delete on cg_contract_version
 for each row execute function cg_original_source_immutable();
create table cg_contract_effect (
 contract_id uuid not null, kind text not null check(kind in ('ACTIVE','EXPIRED')),
 effective_at timestamptz not null, recorded_at timestamptz not null,
 primary key(contract_id,kind)
);
create trigger cg_contract_effect_immutable before update or delete on cg_contract_effect
 for each row execute function cg_original_source_immutable();
create table cg_lifecycle_consequence (
 kind text not null, source_id uuid not null, source_version integer not null,
 entity_id uuid not null unique, created_at timestamptz not null,
 primary key(kind,source_id,source_version)
);
create unique index cg_control_live_kind on cg_entity((body->>'contractId'),(body->>'kind'))
 where entity_type='ControlRequest' and body->>'state' not in ('CONFIRMED','REJECTED','CANCELLED');
create trigger cg_contract_contributor_immutable before update or delete on cg_contract_contributor
 for each row execute function cg_original_source_immutable();
