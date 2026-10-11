create table cg_file_blob (
    file_id uuid primary key,
    storage_key text not null,
    original_sha256 char(64) not null,
    size_bytes bigint not null check (size_bytes > 0 and size_bytes <= 10485760),
    redacted_storage_key text,
    redacted_sha256 char(64),
    redacted_media_type text,
    redacted_verified_by uuid,
    created_at timestamptz not null,
    check ((redacted_storage_key is null) = (redacted_sha256 is null)),
    check (redacted_storage_key is null or redacted_verified_by is not null)
);
create index cg_file_blob_storage on cg_file_blob(storage_key);
create table cg_file_association (
    file_id uuid not null references cg_file_blob(file_id),
    target_type text not null,
    target_id uuid not null,
    historical boolean not null default false,
    created_at timestamptz not null,
    primary key (file_id,target_type,target_id)
);
create index cg_file_association_parent on cg_file_association(target_type,target_id);
create table cg_file_staging_deletion(
    file_id uuid primary key,
    uploaded_by uuid not null,
    target_kind text not null,
    target_id uuid not null,
    branch_id uuid,
    deleted_at timestamptz not null
);
create function cg_protect_file_history() returns trigger language plpgsql as $$
begin
    if old.historical and (tg_op = 'DELETE' or not new.historical) then raise exception 'historical evidence association is immutable'; end if;
    return case when tg_op = 'DELETE' then old else new end;
end $$;
create trigger cg_file_association_history before update or delete on cg_file_association for each row execute function cg_protect_file_history();
create unique index cg_one_live_dossier_round on cg_entity ((body->>'targetId')) where entity_type='Round' and body->>'targetType'='DOSSIER' and body->>'state'='OPEN';
create index cg_dossier_relationship on cg_entity ((body->>'relationshipId'),created_at desc,id desc) where entity_type='Dossier';
alter table cg_job add column version integer not null default 1 check (version > 0);
alter table cg_job add column started_at timestamptz;
alter table cg_job add column completed_at timestamptz;
alter table cg_job add column result_file_id uuid;
create function cg_job_wire_version() returns trigger language plpgsql as $$
begin
    if (new.state,new.attempts,new.expected_version,new.expected_hash,new.error_code,new.result_file_id)
       is distinct from (old.state,old.attempts,old.expected_version,old.expected_hash,old.error_code,old.result_file_id) then
        new.version := old.version + 1;
    else
        new.updated_at := old.updated_at;
    end if;
    if new.state='RUNNING' and old.state<>'RUNNING' then new.started_at := new.updated_at; end if;
    if new.state in ('SUCCEEDED','FAILED','CANCELLED') and old.state<>new.state then new.completed_at := new.updated_at; end if;
    if new.state='QUEUED' and old.state in ('FAILED','CANCELLED') then new.completed_at:=null; new.started_at:=null; end if;
    return new;
end $$;
create trigger cg_job_version before update on cg_job for each row execute function cg_job_wire_version();
create function cg_job_wire_snapshot() returns trigger language plpgsql as $$
declare wire jsonb;
begin
    wire:=jsonb_build_object(
      'id',new.id,'version',new.version,
      'createdAt',to_char(new.created_at at time zone 'UTC','YYYY-MM-DD"T"HH24:MI:SS.MS"Z"'),
      'updatedAt',to_char(new.updated_at at time zone 'UTC','YYYY-MM-DD"T"HH24:MI:SS.MS"Z"'),
      'allowedActions','[]'::jsonb,'blockedActions','[]'::jsonb,
      'branchId',new.branch_id,'state',new.state,'kind',new.kind,
      'parentKind',new.target_type,'parentId',new.target_id,'attempts',new.attempts,
      'nextAttemptAt',case when new.state='QUEUED' then to_char(new.available_at at time zone 'UTC','YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') else null end,
      'startedAt',case when new.started_at is not null then to_char(new.started_at at time zone 'UTC','YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') else null end,
      'completedAt',case when new.completed_at is not null then to_char(new.completed_at at time zone 'UTC','YYYY-MM-DD"T"HH24:MI:SS.MS"Z"') else null end,
      'resultFileId',new.result_file_id,'lastErrorCode',new.error_code);
    insert into cg_entity(entity_type,id,branch_id,version,body,created_at,updated_at)
      values('Job',new.id,new.branch_id,new.version,wire,new.created_at,new.updated_at)
      on conflict(entity_type,id) do update set body=excluded.body,version=excluded.version,updated_at=excluded.updated_at;
    return new;
end $$;
create trigger cg_job_snapshot after insert or update on cg_job for each row execute function cg_job_wire_snapshot();
