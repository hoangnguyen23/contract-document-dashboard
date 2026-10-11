create table cg_entity (
    entity_type text not null,
    id uuid not null,
    branch_id uuid,
    version integer not null check (version > 0),
    body jsonb not null check (jsonb_typeof(body) = 'object'),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    primary key (entity_type,id),
    check (body->>'id' = id::text),
    check ((body->>'version')::integer = version)
);
create index cg_entity_scope_page on cg_entity(entity_type,branch_id,created_at desc,id desc);
create index cg_entity_body on cg_entity using gin(body);

create table cg_idempotency (
    actor_id uuid not null,
    idempotency_key uuid not null,
    fingerprint char(64) not null,
    status integer not null,
    body jsonb,
    etag text,
    location text,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    primary key(actor_id,idempotency_key)
);
create index cg_idempotency_expiry on cg_idempotency(expires_at);

create table cg_audit (
    id uuid primary key,
    occurred_at timestamptz not null,
    actor_id uuid,
    action text not null,
    resource_type text not null,
    resource_id uuid not null,
    resource_version integer not null check(resource_version>0),
    branch_id uuid,
    trace_id uuid not null,
    reason text,
    body jsonb not null
);
create index cg_audit_scope_page on cg_audit(branch_id,occurred_at desc,id desc);
create function cg_immutable_audit() returns trigger language plpgsql as $$ begin raise exception 'audit is immutable'; end $$;
create trigger cg_audit_immutable before update or delete on cg_audit for each row execute function cg_immutable_audit();

create table cg_snapshot (
    snapshot_type text not null,
    id uuid not null,
    parent_id uuid not null,
    branch_id uuid,
    body jsonb not null,
    source_hash char(64) not null,
    created_at timestamptz not null,
    primary key(snapshot_type,id)
);
create index cg_snapshot_parent on cg_snapshot(parent_id,created_at,id);
create trigger cg_snapshot_immutable before update or delete on cg_snapshot for each row execute function cg_immutable_audit();

create table cg_job (
    id uuid primary key,
    dedupe_key text not null unique,
    kind text not null,
    target_type text not null,
    target_id uuid not null,
    branch_id uuid,
    expected_version integer not null,
    expected_hash char(64),
    payload jsonb not null,
    state text not null check(state in ('QUEUED','RUNNING','SUCCEEDED','FAILED','CANCELLED')),
    attempts integer not null default 0,
    available_at timestamptz not null,
    lease_owner uuid,
    lease_until timestamptz,
    trace_id uuid not null,
    error_code text,
    error_message text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
create index cg_job_claim on cg_job(state,available_at,created_at);

create table cg_mailbox_capture (
    message_id uuid primary key,
    notice_id uuid not null unique,
    branch_id uuid not null,
    content_hash char(64) not null,
    envelope jsonb not null,
    accepted_at timestamptz not null
);
create table cg_schedule_lock (lock_key text primary key);
insert into cg_schedule_lock(lock_key) values ('POLICY_PUBLICATION');
