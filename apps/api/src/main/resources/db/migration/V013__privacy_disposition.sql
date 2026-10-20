create table cg_privacy_event (
 ordinal bigint generated always as identity unique, id uuid primary key, request_id uuid not null,
 kind text not null, body jsonb not null, body_hash char(64) not null, actor_id uuid not null, recorded_at timestamptz not null
);
create trigger cg_privacy_event_immutable before update or delete on cg_privacy_event for each row execute function cg_immutable_audit();
create index cg_privacy_event_request on cg_privacy_event(request_id,kind,ordinal desc);
create table cg_record_hold (
 id uuid primary key, request_id uuid not null, body jsonb not null, placed_by uuid not null,
 released_by uuid, released_at timestamptz, created_at timestamptz not null
);
create table cg_record_hold_member (hold_id uuid references cg_record_hold(id),record_id uuid not null,primary key(hold_id,record_id));
create trigger cg_record_hold_member_immutable before update or delete on cg_record_hold_member for each row execute function cg_immutable_audit();
create table cg_disposition_record (
 request_id uuid not null,record_id uuid not null,action text not null,expected_version integer not null,
 phase text not null check(phase in ('RESERVED','LOGICAL','DONE')),proof jsonb not null,proof_hash char(64) not null,
 result_version integer, primary key(request_id,record_id)
);
create unique index cg_disposition_active_record on cg_disposition_record(record_id) where phase<>'DONE' or action='DELETE_BYTES';
create table cg_disposition_key (
 storage_key text primary key,request_id uuid not null,record_id uuid not null,expected_hash char(64) not null
);
create trigger cg_disposition_key_immutable before update or delete on cg_disposition_key for each row execute function cg_immutable_audit();
create function cg_disposition_pending(rid uuid) returns boolean language sql stable as $$
 select exists(select 1 from cg_disposition_record r join cg_entity d on d.entity_type='DataRequest' and d.id=r.request_id where r.record_id=rid and (r.phase<>'DONE' or d.body->>'state'<>'EXECUTED' or r.action='DELETE_BYTES'))
$$;

-- Shared key serialization protects aliases/clones, not only the original File ID.
create function cg_disposition_reference_guard() returns trigger language plpgsql as $$
declare k text;owner uuid;fid uuid;
begin
 if tg_table_name='cg_file_blob' then
  for k in select distinct v from unnest(array[new.storage_key,new.redacted_storage_key]) v where v is not null order by v loop
   perform pg_advisory_xact_lock(hashtextextended('blob:'||k,0));
   select request_id into owner from cg_disposition_key where storage_key=k;
   if owner is not null and owner::text is distinct from current_setting('cg.disposition_request',true) then raise exception 'DISPOSITION_COMMITTED' using errcode='23514';end if;
  end loop;
 else
  fid:=new.file_id;
  if cg_disposition_pending(fid) then raise exception 'DISPOSITION_COMMITTED' using errcode='23514';end if;
  for k in select v from cg_file_blob b cross join lateral unnest(array[b.storage_key,b.redacted_storage_key]) v where b.file_id=fid and v is not null order by v loop
   perform pg_advisory_xact_lock(hashtextextended('blob:'||k,0));
   if exists(select 1 from cg_disposition_key where storage_key=k) then raise exception 'DISPOSITION_COMMITTED' using errcode='23514';end if;
  end loop;
 end if;
 return new;
end $$;
create trigger cg_disposition_blob_guard before insert or update of storage_key,redacted_storage_key on cg_file_blob for each row execute function cg_disposition_reference_guard();
create trigger cg_disposition_association_guard before insert on cg_file_association for each row execute function cg_disposition_reference_guard();
create function cg_disposition_entity_guard() returns trigger language plpgsql as $$
begin
 if old.entity_type in ('File','Relationship') and exists(select 1 from cg_disposition_record where record_id=old.id and (phase<>'DONE' or exists(select 1 from cg_entity d where d.entity_type='DataRequest' and d.id=request_id and d.body->>'state'<>'EXECUTED')) and request_id::text is distinct from current_setting('cg.disposition_request',true)) then raise exception 'DISPOSITION_COMMITTED' using errcode='23514';end if;
 return case when tg_op='DELETE' then old else new end;
end $$;
create trigger cg_disposition_entity_guard before update or delete on cg_entity for each row execute function cg_disposition_entity_guard();
create function cg_privacy_record_scoped(rid uuid,actor uuid,scopes uuid[],broad boolean,seen uuid[] default array[]::uuid[]) returns boolean language plpgsql stable as $$
declare r cg_entity%rowtype;child text;
begin
 if rid=any(seen) or cardinality(seen)>=16 then return false;end if;
 select * into r from cg_entity where id=rid and entity_type in ('File','Relationship','Dossier','Contract','Change','Notice','Incident','Obligation','ControlRequest','DataRequest') limit 1;
 if not found or not(r.branch_id=any(scopes)) then return false;end if;
 seen:=array_append(seen,rid);
 if r.entity_type='Relationship' then return broad or r.body->>'assignedMakerId'=actor::text;end if;
 if r.entity_type='File' then return cg_privacy_record_scoped((r.body->>'targetId')::uuid,actor,scopes,broad,seen);end if;
 if r.entity_type='DataRequest' then
  for child in select jsonb_array_elements_text(r.body->'recordIds') loop
   if not cg_privacy_record_scoped(child::uuid,actor,scopes,broad,seen) then return false;end if;
  end loop;return true;
 end if;
 if r.body->>'relationshipId' is not null then return cg_privacy_record_scoped((r.body->>'relationshipId')::uuid,actor,scopes,broad,seen);end if;
 if r.body->>'contractId' is not null then return cg_privacy_record_scoped((r.body->>'contractId')::uuid,actor,scopes,broad,seen);end if;
 return false;
end $$;
create unique index cg_privacy_one_decision on cg_privacy_event(request_id) where kind='DECISION';
create unique index cg_privacy_one_completion on cg_privacy_event(request_id) where kind='EXECUTION_COMPLETED';
create trigger cg_contact_history_immutable before update or delete on cg_customer_contact_history for each row execute function cg_immutable_audit();
create function cg_disposition_intent_immutable() returns trigger language plpgsql as $$
begin
 if (new.request_id,new.record_id,new.action,new.expected_version,new.proof,new.proof_hash) is distinct from (old.request_id,old.record_id,old.action,old.expected_version,old.proof,old.proof_hash) then raise exception 'disposition intent is immutable';end if;
 if old.phase='DONE' and new.phase<>'DONE' or old.phase='LOGICAL' and new.phase='RESERVED' then raise exception 'disposition phase cannot move backward';end if;
 return new;
end $$;
create trigger cg_disposition_intent_immutable before update on cg_disposition_record for each row execute function cg_disposition_intent_immutable();
