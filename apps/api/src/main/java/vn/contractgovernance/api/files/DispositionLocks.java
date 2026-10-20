package vn.contractgovernance.api.files;
import java.util.*;
import org.springframework.stereotype.Service;
import vn.contractgovernance.api.shared.*;
/** Files-owned guard shared with SQL triggers across record IDs and cloned storage keys. */
@Service public class DispositionLocks {
 private final EntityStore store;
 public DispositionLocks(EntityStore store){this.store=store;}
 public void available(UUID file){if(store.jdbc().queryForObject("select case when cg_disposition_pending(?) then 1 else 0 end",Long.class,file)>0||store.jdbc().queryForObject("select count(*) from cg_file_blob b join cg_disposition_key k on k.storage_key=b.storage_key or k.storage_key=b.redacted_storage_key where b.file_id=?",Long.class,file)>0)throw ApiException.of("DISPOSITION_COMMITTED","Nguồn đã được reserve theo quyết định xử lý dữ liệu.");}
 public void key(String key){store.jdbc().queryForObject("select pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"blob:"+key);}
}
