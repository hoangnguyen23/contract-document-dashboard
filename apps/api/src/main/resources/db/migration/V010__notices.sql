create table cg_notice_source (
 id uuid primary key, notice_id uuid not null, revision integer not null check(revision>0),
 source jsonb not null, source_hash char(64) not null, change_revision_id uuid,
 job_id uuid not null unique, created_at timestamptz not null, unique(notice_id,revision)
);
create table cg_notice_artifact (
 source_id uuid primary key references cg_notice_source(id), file_id uuid not null unique,
 source_hash char(64) not null, artifact_hash char(64) not null, created_at timestamptz not null
);
create table cg_notice_event (
 id uuid primary key, notice_id uuid not null, source_id uuid references cg_notice_source(id),
 kind text not null, body jsonb not null, body_hash char(64) not null,
 actor_id uuid, recorded_at timestamptz not null
);
create index cg_notice_event_source on cg_notice_event(notice_id,source_id,recorded_at,id);
create table cg_notice_ack_input (
 id uuid primary key, notice_id uuid not null, source_id uuid not null references cg_notice_source(id),
 input jsonb not null, input_hash char(64) not null, file_manifest jsonb not null,
 recorded_by uuid not null, recorded_at timestamptz not null
);
create table cg_notice_ack_assessment (
 candidate_id uuid primary key references cg_notice_ack_input(id), assessment jsonb not null, assessment_hash char(64) not null,
 assessed_by uuid not null, assessed_at timestamptz not null
);
create trigger cg_notice_source_immutable before update or delete on cg_notice_source for each row execute function cg_immutable_audit();
create trigger cg_notice_artifact_immutable before update or delete on cg_notice_artifact for each row execute function cg_immutable_audit();
create trigger cg_notice_event_immutable before update or delete on cg_notice_event for each row execute function cg_immutable_audit();
create trigger cg_notice_ack_input_immutable before update or delete on cg_notice_ack_input for each row execute function cg_immutable_audit();
create trigger cg_notice_ack_assessment_immutable before update or delete on cg_notice_ack_assessment for each row execute function cg_immutable_audit();
create index cg_notice_contract on cg_entity((body->>'contractId'),created_at desc,id desc) where entity_type='Notice';

create trigger cg_mailbox_capture_immutable before update or delete on cg_mailbox_capture for each row execute function cg_immutable_audit();
