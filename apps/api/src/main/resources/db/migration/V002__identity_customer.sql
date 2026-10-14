-- Metadata uniqueness is bankwide; inactive resources retain their identity.
create unique index cg_user_username_uq on cg_entity ((body->>'username')) where entity_type='User';
create unique index cg_branch_code_uq on cg_entity ((body->>'code')) where entity_type='Branch';
create unique index cg_party_tax_id_uq on cg_entity ((body->>'taxId')) where entity_type='Party';
create unique index cg_relationship_party_branch_uq on cg_entity (branch_id,(body->>'partyId')) where entity_type='Relationship';
create index cg_relationship_maker_scope on cg_entity (branch_id,(body->>'assignedMakerId'),created_at desc,id desc) where entity_type='Relationship';
create index cg_contact_relationship_scope on cg_entity (branch_id,(body->>'relationshipId'),created_at desc,id desc) where entity_type='ContactRequest';

create table cg_customer_contact_history (
    relationship_id uuid not null,
    contact_version integer not null check(contact_version > 0),
    contact jsonb not null,
    request_id uuid,
    confirmed_by uuid not null,
    confirmed_at timestamptz not null,
    primary key(relationship_id,contact_version)
);
create table cg_customer_assignment_history (
    id uuid primary key,
    relationship_id uuid not null,
    old_maker_id uuid,
    new_maker_id uuid not null,
    actor_id uuid not null,
    reason text,
    recorded_at timestamptz not null
);

create table cg_identity_credential (
    user_id uuid primary key,
    password_hash text not null,
    auth_version bigint not null default 1 check(auth_version > 0),
    created_at timestamptz not null
);

-- Only hashes of opaque session tokens persist; CSRF never appears in URLs/logs.
create table cg_identity_session (
    token_hash varchar(64) primary key,
    csrf_token varchar(64) not null,
    user_id uuid references cg_identity_credential(user_id),
    auth_version bigint,
    created_at timestamptz not null,
    last_seen_at timestamptz not null,
    absolute_expires_at timestamptz not null,
    check ((user_id is null and auth_version is null) or (user_id is not null and auth_version is not null))
);
create index cg_identity_session_user on cg_identity_session(user_id);
create index cg_identity_session_expiry on cg_identity_session(absolute_expires_at);

-- Atomic bucket rows serialize username+IP rate limiting across application instances.
create table cg_identity_login_rate (
    key_hash varchar(64) primary key,
    window_started_at timestamptz not null,
    attempts integer not null check(attempts > 0)
);
