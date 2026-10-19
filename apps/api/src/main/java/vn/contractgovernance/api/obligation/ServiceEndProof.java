package vn.contractgovernance.api.obligation;
import java.util.UUID;
/** Implemented by actual Control confirmation evidence; absent producer means no verified proof. */
public interface ServiceEndProof { boolean verifiedEnded(UUID contractId); }
