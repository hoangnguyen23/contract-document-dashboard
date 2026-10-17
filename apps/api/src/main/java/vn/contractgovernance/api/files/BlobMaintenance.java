package vn.contractgovernance.api.files;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;

/** No entity transitions: only managed bytes without any original/redacted database reference. */
@Component
public class BlobMaintenance {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BlobMaintenance.class);
    private static final Duration GRACE = Duration.ofHours(24);
    private final PrivateBlobStore blobs;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final TransactionTemplate transactions;
    private final int scanLimit;
    private final int candidateLimit;
    private DirectoryStream<Path> stream;
    private Iterator<Path> iterator;

    public BlobMaintenance(PrivateBlobStore blobs, JdbcTemplate jdbc, Clock clock, PlatformTransactionManager manager,
                           @Value("${cg.storage-maintenance.scan-limit:512}") int scanLimit,
                           @Value("${cg.storage-maintenance.candidate-limit:32}") int candidateLimit) {
        this.blobs = blobs; this.jdbc = jdbc; this.clock = clock;
        if (scanLimit < 1 || scanLimit > 4096 || candidateLimit < 1 || candidateLimit > 128)
            throw new IllegalArgumentException("Private maintenance bounds are invalid");
        this.scanLimit = scanLimit; this.candidateLimit = candidateLimit;
        transactions = new TransactionTemplate(manager);
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactions.setTimeout(2);
    }

    @Scheduled(initialDelayString = "${cg.storage-maintenance.initial-delay-ms:60000}", fixedDelayString = "${cg.storage-maintenance.interval-ms:300000}")
    public synchronized void reconcile() {
        int scanned = 0, candidates = 0, removed = 0, failed = 0;
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        var cutoff = clock.instant().minus(GRACE);
        try {
            var root = blobs.maintenanceRoot();
            if (stream == null) { stream = Files.newDirectoryStream(root); iterator = stream.iterator(); }
            while (scanned < scanLimit && candidates < candidateLimit && System.nanoTime() < deadline && iterator.hasNext()) {
                var entry = iterator.next(); scanned++;
                String name = entry.getFileName().toString();
                if (!blobs.managedName(name)) continue;
                try {
                    var candidate = blobs.candidate(name);
                    if (candidate.isEmpty() || candidate.get().modifiedAt().isAfter(cutoff)) continue;
                    candidates++;
                    var guard = blobs.maintenanceGuard(name);
                    if (guard.isEmpty()) continue;
                    try (var ignored = guard.get()) {
                        boolean deleted = Boolean.TRUE.equals(transactions.execute(status -> {
                            // This conflicts with reference INSERT/UPDATE/DELETE from every application process.
                            PrivateBlobStore.lockReferences(jdbc);
                            String reference = blobs.referenceKey(name);
                            Long references = jdbc.queryForObject("select count(*) from cg_file_blob where storage_key in (?,?) or redacted_storage_key in (?,?)", Long.class, name, reference, name, reference);
                            if (references == null || references != 0L) return false;
                            try { return blobs.removeCandidate(candidate.get(), cutoff); }
                            catch (NoSuchFileException gone) { return false; }
                            catch (IOException exception) { throw new MaintenanceFailure(); }
                        }));
                        if (deleted) removed++;
                    }
                } catch (NoSuchFileException gone) {
                    // Another authorized cleanup removed the orphan before this guard was acquired.
                } catch (RuntimeException | IOException failure) {
                    failed++; LOG.warn("Private orphan reconciliation skipped a candidate after a guarded failure");
                }
            }
            if (!iterator.hasNext()) closeCursor();
        } catch (RuntimeException | IOException failure) {
            failed++; closeCursor(); LOG.warn("Private orphan reconciliation could not enumerate the verified store");
        }
        if (removed > 0 || failed > 0)
            LOG.info("Private orphan reconciliation: scanned={}, eligible={}, removed={}, failed={}", scanned, candidates, removed, failed);
    }

    private static final class MaintenanceFailure extends RuntimeException {}
    private void closeCursor() {
        if (stream != null) try { stream.close(); } catch (IOException exception) { LOG.warn("Private maintenance cursor close failed"); }
        stream = null; iterator = null;
    }
    @PreDestroy public synchronized void close() { closeCursor(); }
}
