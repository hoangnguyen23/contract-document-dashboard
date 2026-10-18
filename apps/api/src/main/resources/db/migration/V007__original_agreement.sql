create table cg_original_agreement (
 contract_id uuid primary key, dossier_id uuid not null unique, revision_id uuid not null unique,
 source jsonb not null, source_hash char(64) not null, job_id uuid not null unique,
 created_at timestamptz not null
);
create function cg_original_source_immutable() returns trigger language plpgsql as $$
begin raise exception 'original agreement source is immutable'; end $$;
create trigger cg_original_source_history before update or delete on cg_original_agreement for each row execute function cg_original_source_immutable();
create table cg_agreement_artifact (
 contract_id uuid primary key references cg_original_agreement(contract_id), file_id uuid not null unique references cg_file_blob(file_id),
 source_hash char(64) not null, artifact_hash char(64) not null, created_at timestamptz not null
);
create trigger cg_artifact_history before update or delete on cg_agreement_artifact for each row execute function cg_original_source_immutable();
