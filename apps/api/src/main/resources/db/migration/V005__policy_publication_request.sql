create table cg_policy_publication_request (
    policy_id uuid primary key,
    requester_id uuid not null,
    requested_at timestamptz not null
);
create trigger cg_policy_publication_request_immutable before update or delete on cg_policy_publication_request for each row execute function cg_immutable_audit();
