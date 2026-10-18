package vn.contractgovernance.api.change;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.*;
/** Notice owns its evidence; Change owns the source/slot and invokes these guards under its lock. */
public interface ChangeNoticeBridge {
 void requireReady(EntityStore.Entity change,Actor actor);
 void cleanup(EntityStore.Entity change,String reason,Actor actor);
 JsonNode artifact(EntityStore.Entity change);
}
