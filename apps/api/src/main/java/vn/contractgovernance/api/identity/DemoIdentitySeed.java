package vn.contractgovernance.api.identity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.EntityStore;
import vn.contractgovernance.api.shared.JsonSupport;

/** Only the explicit demo profile imports synthetic core identities; never fixture workflow states. */
@Component
@Profile("demo")
public class DemoIdentitySeed implements ApplicationRunner {
    private final EntityStore store;
    private final SessionService sessions;
    private final JsonSupport json;
    private final Clock clock;
    private final Environment environment;
    public DemoIdentitySeed(EntityStore store,SessionService sessions,JsonSupport json,Clock clock,Environment environment){this.store=store;this.sessions=sessions;this.json=json;this.clock=clock;this.environment=environment;}

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        String password=environment.getProperty("CG_DEMO_PASSWORD");
        if(password==null || password.length()<12 || password.length()>128)
            throw new IllegalStateException("The explicit demo profile requires CG_DEMO_PASSWORD (12–128 characters); no default credentials exist.");
        store.jdbc().execute("select pg_advisory_xact_lock(7342051901)");
        if(Boolean.TRUE.equals(store.jdbc().queryForObject("select exists(select 1 from cg_entity where entity_type='User')",Boolean.class)))return;
        JsonNode seed;
        try(var input=new ClassPathResource("contracts/seed-v1.json").getInputStream()){seed=json.parse(new String(input.readAllBytes(),StandardCharsets.UTF_8));}
        var branch=normalized(seed.path("resources").path("Branch"));insert("Branch",branch,null);
        var branch2=(ObjectNode)branch.deepCopy();branch2.put("id","00000000-0000-4000-8000-000000000002");branch2.put("code","B02");branch2.put("name","Chi nhánh mẫu 02");insert("Branch",branch2,null);
        for(JsonNode source:seed.path("actors")){var user=normalized(source);UUID id=UUID.fromString(user.path("id").asString());insert("User",user,null);sessions.createCredential(id,password);}
        insert("Party",normalized(seed.path("resources").path("Party")),null);
        var relationship=normalized(seed.path("resources").path("Relationship"));UUID relationshipId=UUID.fromString(relationship.path("id").asString());UUID branchId=UUID.fromString(relationship.path("branchId").asString());
        insert("Relationship",relationship,branchId);
        UUID adminId=UUID.fromString("00000000-0000-4000-8000-000000000003");
        store.jdbc().update("insert into cg_customer_contact_history(relationship_id,contact_version,contact,request_id,confirmed_by,confirmed_at) values (?,1,?::jsonb,null,?,?)",relationshipId,json.stringify(relationship.path("confirmedContact")),adminId,Timestamp.from(clock.instant()));
        store.jdbc().update("insert into cg_customer_assignment_history(id,relationship_id,old_maker_id,new_maker_id,actor_id,reason,recorded_at) values (?,?,null,?,?,?,?)",UUID.randomUUID(),relationshipId,UUID.fromString(relationship.path("assignedMakerId").asString()),adminId,"Synthetic demo core seed",Timestamp.from(clock.instant()));
    }

    private ObjectNode normalized(JsonNode source){var body=(ObjectNode)json.nfc(source);String now=JsonSupport.timestamp(clock.instant());body.put("createdAt",now);body.put("updatedAt",now);body.putArray("allowedActions");body.putArray("blockedActions");return body;}
    private void insert(String type,ObjectNode body,UUID branchId){store.create(type,UUID.fromString(body.path("id").asString()),branchId,body);}
}
