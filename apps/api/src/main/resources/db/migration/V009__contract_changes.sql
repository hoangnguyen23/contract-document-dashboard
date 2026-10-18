create table cg_change_revision (
 id uuid primary key, change_id uuid not null, number integer not null check(number>0),
 revision jsonb not null, metadata jsonb not null, submitted_by uuid not null,
 created_at timestamptz not null, unique(change_id,number),
 check(revision->>'id'=id::text), check((revision->>'number')::integer=number)
);
create trigger cg_change_revision_immutable before update or delete on cg_change_revision
 for each row execute function cg_original_source_immutable();
create table cg_change_artifact_source (
 revision_id uuid primary key references cg_change_revision(id), change_id uuid not null,
 source jsonb not null, source_hash char(64) not null, job_id uuid not null unique,
 created_at timestamptz not null
);
create trigger cg_change_artifact_source_immutable before update or delete on cg_change_artifact_source
 for each row execute function cg_original_source_immutable();
create table cg_change_artifact (
 revision_id uuid primary key references cg_change_revision(id), file_id uuid not null unique,
 source_hash char(64) not null, artifact_hash char(64) not null, created_at timestamptz not null
);
create trigger cg_change_artifact_immutable before update or delete on cg_change_artifact
 for each row execute function cg_original_source_immutable();
create table cg_change_contributor (
 change_id uuid not null, actor_id uuid not null, created_at timestamptz not null,
 primary key(change_id,actor_id)
);
create trigger cg_change_contributor_immutable before update or delete on cg_change_contributor
 for each row execute function cg_original_source_immutable();
create table cg_change_signing_input (
 packet_id uuid primary key, change_id uuid not null,
 revision_id uuid not null references cg_change_revision(id), input jsonb not null,
 input_hash char(64) not null, file_manifest jsonb not null, submitted_by uuid not null,
 created_at timestamptz not null
);
create trigger cg_change_signing_input_immutable before update or delete on cg_change_signing_input
 for each row execute function cg_original_source_immutable();
create table cg_change_signing_assessment (
 packet_id uuid primary key references cg_change_signing_input(packet_id),
 assessment jsonb not null, assessed_by uuid not null, assessed_at timestamptz not null
);
create trigger cg_change_signing_assessment_immutable before update or delete on cg_change_signing_assessment
 for each row execute function cg_original_source_immutable();
create table cg_change_effect (
 change_id uuid primary key, revision_id uuid not null references cg_change_revision(id),
 version_id uuid not null unique references cg_contract_version(id),
 effective_at timestamptz not null, recorded_at timestamptz not null
);
create trigger cg_change_effect_immutable before update or delete on cg_change_effect
 for each row execute function cg_original_source_immutable();
create table cg_change_event (
 id uuid primary key, change_id uuid not null, kind text not null, body jsonb not null,
 actor_id uuid, recorded_at timestamptz not null
);
create trigger cg_change_event_immutable before update or delete on cg_change_event
 for each row execute function cg_original_source_immutable();
create unique index cg_change_future_slot on cg_entity((body->>'contractId'))
 where entity_type='Change' and body->>'state'='SCHEDULED';
create unique index cg_change_live_round on cg_entity((body->>'targetId'))
 where entity_type='Round' and body->>'targetKind'='CHANGE' and body->>'state'='OPEN';
