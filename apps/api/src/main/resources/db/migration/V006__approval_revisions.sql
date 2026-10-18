-- Wire Revision intentionally has no mutable Entity metadata.
create table cg_dossier_revision (
    id uuid primary key,
    dossier_id uuid not null,
    number integer not null check(number > 0),
    revision jsonb not null,
    source_metadata jsonb not null,
    submitted_by uuid not null,
    created_at timestamptz not null,
    unique(dossier_id, number),
    check(revision->>'id'=id::text),
    check((revision->>'number')::integer=number)
);
create function cg_approval_immutable() returns trigger language plpgsql as $$
begin raise exception 'approval source/history is immutable'; end $$;
create trigger cg_revision_immutable before update or delete on cg_dossier_revision for each row execute function cg_approval_immutable();
create table cg_round_decision (
    round_id uuid not null,
    level text not null check(level in ('L1','L2')),
    actor_id uuid not null,
    action text not null check(action in ('APPROVE','RETURN','REJECT')),
    reason text not null,
    decided_at timestamptz not null,
    primary key(round_id,level)
);
create trigger cg_decision_immutable before update or delete on cg_round_decision for each row execute function cg_approval_immutable();
create table cg_round_reassignment (
    id uuid primary key,
    round_id uuid not null,
    level text not null check(level in ('L1','L2')),
    old_reviewer_id uuid not null,
    new_reviewer_id uuid not null,
    actor_id uuid not null,
    reason text not null,
    recorded_at timestamptz not null
);
create trigger cg_reassignment_immutable before update or delete on cg_round_reassignment for each row execute function cg_approval_immutable();
create table cg_round_assessment (
    id uuid primary key,
    round_id uuid not null,
    actor_id uuid not null,
    assessment jsonb not null,
    recorded_at timestamptz not null
);
create trigger cg_assessment_immutable before update or delete on cg_round_assessment for each row execute function cg_approval_immutable();
create table cg_round_evaluation (
    id uuid primary key,
    round_id uuid not null,
    actor_id uuid not null,
    evaluation jsonb not null,
    recorded_at timestamptz not null
);
create trigger cg_round_evaluation_immutable before update or delete on cg_round_evaluation for each row execute function cg_approval_immutable();
create index cg_round_assessment_latest on cg_round_assessment(round_id,recorded_at desc,id desc);
create function cg_protect_round_history() returns trigger language plpgsql as $$
declare previous_step jsonb; current_step jsonb;
begin
    if old.entity_type<>'Round' then return case when tg_op='DELETE' then old else new end; end if;
    if tg_op='DELETE' then raise exception 'approval round history cannot be deleted'; end if;
    if old.body->>'state'<>'OPEN' then raise exception 'closed approval round is immutable'; end if;
    if (new.branch_id,new.body->>'targetKind',new.body->>'targetId',new.body->>'revisionId',new.body->>'policyVersionId',new.body->>'dueAt',new.body->>'createdAt')
       is distinct from (old.branch_id,old.body->>'targetKind',old.body->>'targetId',old.body->>'revisionId',old.body->>'policyVersionId',old.body->>'dueAt',old.body->>'createdAt') then
       raise exception 'approval source binding is immutable';
    end if;
    if jsonb_array_length(new.body->'steps')<>jsonb_array_length(old.body->'steps') then raise exception 'approval route is immutable'; end if;
    for previous_step in select value from jsonb_array_elements(old.body->'steps') loop
        select value into current_step from jsonb_array_elements(new.body->'steps') where value->>'level'=previous_step->>'level';
        if current_step is null then raise exception 'approval levels are immutable'; end if;
        if previous_step->>'state' in ('APPROVED','RETURNED','REJECTED','CANCELLED') and current_step is distinct from previous_step then
            raise exception 'decided approval step is immutable';
        end if;
    end loop;
    return new;
end $$;
create trigger cg_round_history before update or delete on cg_entity for each row when(old.entity_type='Round') execute function cg_protect_round_history();
