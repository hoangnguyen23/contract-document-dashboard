package vn.contractgovernance.api.control;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.contractgovernance.api.shared.*;
import vn.contractgovernance.api.files.*;
import vn.contractgovernance.api.signing.SigningEvidence;
import static vn.contractgovernance.api.change.ChangeService.id;

/** Exact same-contract source associations; no original-artifact fallback. */
@Service
public class ControlEvidence {
 private final EntityStore store;private final FileService files;private final EvidenceAccess access;private final JsonSupport json;private final Clock clock;
 public ControlEvidence(EntityStore store,FileService files,EvidenceAccess access,JsonSupport json,Clock clock){this.store=store;this.files=files;this.access=access;this.json=json;this.clock=clock;}
 public JsonNode manifest(EntityStore.Entity request,Collection<UUID> ids,Actor actor,boolean attach){var result=json.array();UUID contract=id(request.body(),"contractId");var current=store.require("Contract",contract).body().path("currentVersion");var rows=new ArrayList<JsonNode>();for(var fileId:new TreeSet<>(ids)){
  var file=attach?store.lock("File",fileId):store.require("File",fileId);var b=file.body();boolean direct=b.path("targetKind").asString().equals("CONTRACT")&&b.path("targetId").asString().equals(contract.toString());boolean own=b.path("targetKind").asString().equals("CONTROL_REQUEST")&&b.path("targetId").asString().equals(request.id().toString());boolean artifact=current.path("artifactId").asString().equals(fileId.toString())&&Set.of("CONTRACT","CHANGE","NOTICE").contains(b.path("targetKind").asString());if(!Objects.equals(file.branchId(),request.branchId())||!(direct||own||artifact))throw ApiException.of("NOT_FOUND","Evidence không thuộc đúng nguồn hợp đồng/control.");if(!b.path("state").asString().equals("CLEAN"))throw ApiException.of("FILE_NOT_CLEAN","Evidence chưa CLEAN.");if(actor!=null){access.requireFile(fileId,actor);access.requireFull(file,actor);}var hash=b.path("sha256").asString();if(!hash.equals(files.blob(fileId).hash()))throw ApiException.of("STORAGE_INTEGRITY","Evidence metadata/hash mismatch.");files.bytesForScan(fileId,hash);rows.add(json.object().put("fileId",fileId.toString()).put("purpose",b.path("purpose").asString()).put("sha256",hash));if(attach){store.jdbc().update("insert into cg_file_association(file_id,target_type,target_id,historical,created_at) values(?,'CONTROL_REQUEST',?,true,?) on conflict(file_id,target_type,target_id) do update set historical=true",fileId,request.id(),Timestamp.from(clock.instant()));if(!b.path("attached").asBoolean()){var next=(ObjectNode)b.deepCopy();next.put("attached",true).put("updatedAt",JsonSupport.timestamp(clock.instant()));store.update(file,next);}}
 }rows.sort(Comparator.comparing((JsonNode r)->r.path("purpose").asString()).thenComparing(r->r.path("fileId").asString()));rows.forEach(result::add);return result;}
 public Set<UUID> ids(JsonNode values){return SigningEvidence.uniqueIds(values,"/evidenceFileIds");}
 public void nonfuture(JsonNode time){if(Instant.parse(time.asString()).isAfter(clock.instant()))throw ApiException.validation("/observedAt","Thời điểm không được ở tương lai.");}
 public Set<UUID> uploaders(Collection<UUID> ids){var result=new TreeSet<UUID>();for(var file:ids){var b=store.require("File",file).body();if(b.hasNonNull("uploadedBy"))result.add(id(b,"uploadedBy"));}return result;}
}
