package vn.contractgovernance.api.reporting;

import java.util.*;
import vn.contractgovernance.api.shared.*;

/** Read-only reporting predicates. Current parent ancestry, rather than an audit actor's old grants, owns scope. */
final class ReportingScope {
    private ReportingScope() {}
    record Predicate(String sql, List<Object> args) {}
    static List<UUID> branches(Actor actor, Map<String,String> query) {
        if (query.containsKey("branchId")) {
            UUID branch=UUID.fromString(query.get("branchId")); actor.requireBranch(branch); return List.of(branch);
        }
        return actor.branchIds().stream().sorted().toList();
    }
    static String array(List<UUID> branches) { return "{"+String.join(",",branches.stream().map(UUID::toString).toList())+"}"; }
    static boolean broad(Actor actor) { return actor.hasAnyRole("CHECKER","MANAGER","DATA_OFFICER","AUDITOR"); }
    static void businessRole(Actor actor) { actor.requireRole("MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR"); }
    static final String PARENT_TYPE="""
        case when n.entity_type in ('Round','SigningPacket','File','Job') then
          case coalesce(n.body->>'targetKind',n.body->>'parentKind')
            when 'DOSSIER' then 'Dossier' when 'CONTRACT' then 'Contract' when 'CHANGE' then 'Change'
            when 'CONTROL_REQUEST' then 'ControlRequest' when 'INCIDENT' then 'Incident'
            when 'OBLIGATION' then 'Obligation' when 'NOTICE' then 'Notice' when 'DATA_REQUEST' then 'DataRequest'
            when 'RELATIONSHIP' then 'Relationship' when 'POLICY' then 'Policy' when 'FILE' then 'File' end
          when jsonb_exists(n.body,'relationshipId') then 'Relationship' when jsonb_exists(n.body,'contractId') then 'Contract' end
        """;
    static final String PARENT_ID="""
        case when n.entity_type in ('Round','SigningPacket','File') then (n.body->>'targetId')::uuid
          when n.entity_type='Job' then (n.body->>'parentId')::uuid
          when jsonb_exists(n.body,'relationshipId') then (n.body->>'relationshipId')::uuid
          when jsonb_exists(n.body,'contractId') then (n.body->>'contractId')::uuid end
        """;
    static Predicate business(Actor actor,List<UUID> branches,String alias) {
        var args=new ArrayList<Object>(); args.add(array(branches));
        String sql=alias+".branch_id=any(?::uuid[])";
        String ancestry="with recursive ancestry as (select "+alias+".entity_type,"+alias+".id,"+alias+".branch_id,"+alias+".body,0 as depth union all select p.entity_type,p.id,p.branch_id,p.body,n.depth+1 from ancestry n join cg_entity p on p.entity_type="+PARENT_TYPE+" and p.id="+PARENT_ID+" and p.branch_id=n.branch_id where n.depth<8) ";
        // Privacy ancestors require every selected record's current scope, also for attached File/Job audits.
        args.add(actor.hasAnyRole("MAKER","DATA_OFFICER","AUDITOR"));args.add(actor.id());args.add(array(branches));args.add(actor.hasAnyRole("DATA_OFFICER","AUDITOR"));
        sql+=" and not exists ("+ancestry+"select 1 from ancestry where entity_type='DataRequest' and (not ?::boolean or not cg_privacy_record_scoped(id,?,?::uuid[],?)))";
        if (!broad(actor)) {
            args.add(actor.hasRole("MAKER")); args.add(actor.id().toString());
            args.add(actor.id());args.add(array(branches));
            sql+=" and ?::boolean and exists ("+ancestry+"select 1 from ancestry where (entity_type='Relationship' and body->>'assignedMakerId'=?) or (entity_type='DataRequest' and cg_privacy_record_scoped(id,?,?::uuid[],false)))";
        }
        return new Predicate(sql,List.copyOf(args));
    }
    static String wireType(String expression) {
        return "case "+expression+" when 'User' then 'USER' when 'Branch' then 'BRANCH' when 'Party' then 'PARTY' when 'Relationship' then 'RELATIONSHIP' when 'ContactRequest' then 'CONTACT_REQUEST' when 'Dossier' then 'DOSSIER' when 'Round' then 'ROUND' when 'Policy' then 'POLICY' when 'Contract' then 'CONTRACT' when 'SigningPacket' then 'SIGNING_PACKET' when 'Change' then 'CHANGE' when 'Notice' then 'NOTICE' when 'Obligation' then 'OBLIGATION' when 'Incident' then 'INCIDENT' when 'ControlRequest' then 'CONTROL_REQUEST' when 'DataRequest' then 'DATA_REQUEST' when 'File' then 'FILE' when 'Job' then 'JOB' when 'Export' then 'EXPORT' else "+expression+" end";
    }
    static String entityType(String expression) {
        return "case "+wireType(expression)+" when 'USER' then 'User' when 'BRANCH' then 'Branch' when 'PARTY' then 'Party' when 'RELATIONSHIP' then 'Relationship' when 'CONTACT_REQUEST' then 'ContactRequest' when 'DOSSIER' then 'Dossier' when 'ROUND' then 'Round' when 'POLICY' then 'Policy' when 'CONTRACT' then 'Contract' when 'SIGNING_PACKET' then 'SigningPacket' when 'CHANGE' then 'Change' when 'NOTICE' then 'Notice' when 'OBLIGATION' then 'Obligation' when 'INCIDENT' then 'Incident' when 'CONTROL_REQUEST' then 'ControlRequest' when 'DATA_REQUEST' then 'DataRequest' when 'FILE' then 'File' when 'JOB' then 'Job' end";
    }
    static Predicate audit(Actor actor,List<UUID> branches,boolean branchFilter) {
        actor.requireRole("BANK_ADMIN","MAKER","CHECKER","MANAGER","DATA_OFFICER","AUDITOR","POLICY_AUTHOR","POLICY_REVIEWER");
        var visible=business(actor,branches,"e"); var args=new ArrayList<Object>(visible.args());
        String business="exists(select 1 from cg_entity e where e.entity_type="+entityType("a.resource_type")+" and e.id=a.resource_id and e.branch_id=a.branch_id and "+visible.sql()+")";
        // Deleted own staging uploads retain an opaque audit link; no historical business object is removed.
        args.add(array(branches)); args.add(broad(actor)); args.add(actor.hasRole("MAKER")); args.add(actor.id());
        var stagingParent=business(actor,branches,"p");args.addAll(stagingParent.args());
        String staging="("+wireType("a.resource_type")+"='FILE' and exists(select 1 from cg_file_staging_deletion d where d.file_id=a.resource_id and d.branch_id=a.branch_id and d.branch_id=any(?::uuid[]) and (?::boolean or (?::boolean and d.uploaded_by=?)) and exists(select 1 from cg_entity p where p.id=d.target_id and p.entity_type="+entityType("d.target_kind")+" and p.branch_id=d.branch_id and "+stagingParent.sql()+")))";
        args.add(array(branches)); args.add(broad(actor)); args.add(actor.id());
        String exported="("+wireType("a.resource_type")+"='EXPORT' and a.branch_id=any(?::uuid[]) and (?::boolean or a.actor_id=?))";
        // Null-branch namespaces are purpose bounded; an administrative/policy role grants no business visibility.
        args.add(actor.hasRole("BANK_ADMIN")); args.add(array(branches)); args.add(array(branches));
        String admin="(a.branch_id is null and ?::boolean and exists(select 1 from cg_entity d where d.id=a.resource_id and d.entity_type="+entityType("a.resource_type")+" and ((d.entity_type='Branch' and d.id=any(?::uuid[])) or (d.entity_type='User' and exists(select 1 from jsonb_array_elements_text(d.body->'branchIds') b where b::uuid=any(?::uuid[])))))";
        args.add(actor.hasAnyRole("POLICY_AUTHOR","POLICY_REVIEWER")); args.add(!branchFilter);
        String policy="(a.branch_id is null and ?::boolean and ?::boolean and ("+wireType("a.resource_type")+"='POLICY' or exists(with recursive ancestry as (select e.entity_type,e.id,e.branch_id,e.body,0 as depth from cg_entity e where e.entity_type="+entityType("a.resource_type")+" and e.id=a.resource_id union all select p.entity_type,p.id,p.branch_id,p.body,n.depth+1 from ancestry n join cg_entity p on p.entity_type="+PARENT_TYPE+" and p.id="+PARENT_ID+" where n.depth<8 and p.branch_id is null) select 1 from ancestry where entity_type='Policy')))";
        args.add(actor.hasRole("BANK_ADMIN")); args.add(!branchFilter); args.add(array(branches)); args.add(broad(actor)); args.add(actor.hasRole("MAKER"));args.add(actor.id().toString());
        String party="(a.branch_id is null and "+wireType("a.resource_type")+"='PARTY' and ((?::boolean and ?::boolean) or exists(select 1 from cg_entity r where r.entity_type='Relationship' and r.body->>'partyId'=a.resource_id::text and r.branch_id=any(?::uuid[]) and (?::boolean or (?::boolean and r.body->>'assignedMakerId'=?)))))";
        return new Predicate("("+business+" or "+staging+" or "+exported+" or "+admin+" or "+policy+" or "+party+")",List.copyOf(args));
    }
}
