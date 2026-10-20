package vn.contractgovernance.api.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.JsonSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Opaque private names; pending reference creation holds a separate cross-process guard. */
@Component
public class PrivateBlobStore {
    public record Stored(String key, String sha256, int sizeBytes, String mediaType) {}
    record Candidate(String name, Object identity, Instant modifiedAt) {}
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrivateBlobStore.class);
    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
    private static final Pattern KEY = Pattern.compile(UUID_PATTERN);
    private static final Pattern CURRENT_PART = Pattern.compile("quarantine-(" + UUID_PATTERN + ")\\.part");
    private static final Pattern LEGACY_PART = Pattern.compile("quarantine-([0-9]{1,20})\\.part");
    public static final int MAX_BYTES = 10 * 1024 * 1024;
    private final Path root;
    private final Path guards;
    private final Object rootIdentity;
    private final Object guardsIdentity;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate cleanup;
    private final ConcurrentHashMap<String, PendingWrite> pending = new ConcurrentHashMap<>();

    public PrivateBlobStore(@Value("${cg.storage-root:./var/private-files}") String path,
                            JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        cleanup = new TransactionTemplate(manager);
        cleanup.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        cleanup.setTimeout(2);
        try {
            root = Path.of(path).toAbsolutePath().normalize();
            verifyExistingAncestors(root);
            Files.createDirectories(root);
            verifyExistingAncestors(root);
            rootIdentity = directoryIdentity(root);
            guards = root.resolve(".guards");
            if (!Files.exists(guards, LinkOption.NOFOLLOW_LINKS)) Files.createDirectories(guards);
            guardsIdentity = directoryIdentity(guards);
        } catch (IOException exception) { throw new IllegalStateException("Private file store unavailable or unsafe"); }
    }

    /** HTTP convenience: the guard survives until the request/transaction has completed. */
    public Stored write(InputStream input) {
        var request = RequestContextHolder.getRequestAttributes();
        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive();
        if (request == null && !transaction)
            throw ApiException.of("SERVICE_UNAVAILABLE", "Ghi bytes ngoài request cần lifecycle writePending rõ ràng.");
        var write = writePending(input);
        try {
            if (request != null) request.registerDestructionCallback("private-blob:" + write.stored().key(), write::close, RequestAttributes.SCOPE_REQUEST);
            else TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) { write.close(); }
            });
            return write.stored();
        } catch (RuntimeException exception) { write.close(); throw exception; }
    }

    /** Future workers retain this guard until their reference transaction has committed/rolled back. */
    public PendingWrite writePending(InputStream input) {
        String key = UUID.randomUUID().toString();
        Guard guard = null;
        Path temporary = null;
        try {
            verifyRoot();
            guard = tryGuard(key).orElseThrow(() -> ApiException.of("SERVICE_UNAVAILABLE", "Không giữ được khóa ghi bytes riêng tư."));
            var bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) throw ApiException.of("FILE_TOO_LARGE", "Tệp vượt quá giới hạn 10 MiB.");
            if (bytes.length == 0) throw ApiException.validation("/file", "Tệp phải có nội dung.");
            var mediaType = sniff(bytes);
            temporary = root.resolve("quarantine-" + key + ".part");
            Files.write(temporary, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
            verifyRoot();
            if (Files.exists(resolve(key), LinkOption.NOFOLLOW_LINKS)) throw ApiException.of("STORAGE_INTEGRITY", "Không ghi đè blob đã tồn tại.");
            try { Files.move(temporary, resolve(key), StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, resolve(key)); }
            var write = new PendingWrite(new Stored(key, JsonSupport.sha256(bytes), bytes.length, mediaType), guard, Thread.currentThread());
            pending.put(key, write);
            guard = null;
            return write;
        } catch (IOException exception) { throw ApiException.of("SERVICE_UNAVAILABLE", "Kho tệp riêng tư hiện không khả dụng."); }
        finally {
            if (temporary != null) {
                try { verifyRoot(); Files.deleteIfExists(temporary); }
                catch (IOException | ApiException exception) { LOG.warn("Private temporary bytes need maintenance reconciliation"); }
            }
            if (guard != null) guard.close();
        }
    }

    public final class PendingWrite implements AutoCloseable {
        private final Stored stored;
        private final Guard guard;
        private final Thread owner;
        private boolean closed;
        private PendingWrite(Stored stored, Guard guard, Thread owner) { this.stored = stored; this.guard = guard; this.owner = owner; }
        public Stored stored() { return stored; }
        @Override public synchronized void close() {
            if (closed) return;
            closed = true;
            pending.remove(stored.key(), this);
            guard.close();
        }
    }

    public byte[] read(String key, String expectedHash) {
        try {
            var path = resolve(key);
            var attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile() || attributes.isSymbolicLink() || attributes.isOther() || attributes.size() > MAX_BYTES)
                throw ApiException.of("STORAGE_INTEGRITY", "Không xác minh được tệp đã lưu.");
            var identity = ManagedFileIdentity.capture(path, attributes);
            byte[] bytes;
            try (var channel = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
                var output = new java.io.ByteArrayOutputStream();
                var buffer = java.nio.ByteBuffer.allocate(8192);
                while (channel.read(buffer) != -1) {
                    buffer.flip();
                    if (output.size() + buffer.remaining() > MAX_BYTES) throw ApiException.of("STORAGE_INTEGRITY", "Nội dung tệp đã lưu vượt giới hạn.");
                    output.write(buffer.array(), 0, buffer.remaining());
                    buffer.clear();
                }
                bytes = output.toByteArray();
            }
            verifyRoot();
            var after = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!Objects.equals(identity, ManagedFileIdentity.capture(path, after)) || !after.isRegularFile() || after.isSymbolicLink()
                    || !JsonSupport.sha256(bytes).equals(expectedHash)) throw ApiException.of("STORAGE_INTEGRITY", "Hash hoặc identity tệp không khớp bản ghi.");
            return bytes;
        } catch (IOException exception) { throw ApiException.of("STORAGE_INTEGRITY", "Không đọc được tệp đã lưu."); }
    }

    /** Recheck both reference columns, even for immediate post-commit/replay orphan cleanup. */
    public void delete(String key) {
        try {
            var own = pending.get(key);
            if (own != null && own.owner == Thread.currentThread()) { deleteUnreferenced(key); return; }
            try (var guard = tryGuard(key).orElseThrow(() -> ApiException.of("SERVICE_UNAVAILABLE", "Bytes đang có lifecycle chưa hoàn tất."))) {
                deleteUnreferenced(key);
            }
        } catch (IOException | RuntimeException exception) {
            LOG.warn("Private byte cleanup deferred; reference guards or storage are unavailable");
            throw ApiException.of("SERVICE_UNAVAILABLE", "Không xóa được bytes trong kho riêng tư.");
        }
    }
    private void deleteUnreferenced(String key) {
        cleanup.executeWithoutResult(status -> {
            lockReferences(jdbc);
            if(jdbc.queryForObject("select count(*) from cg_disposition_key where storage_key=?",Long.class,key)>0)return;
            Long references = jdbc.queryForObject("select count(*) from cg_file_blob where storage_key=? or redacted_storage_key=?", Long.class, key, key);
            if (references == null || references != 0L) return;
            try { Files.deleteIfExists(resolve(key)); }
            catch (IOException exception) { throw ApiException.of("SERVICE_UNAVAILABLE", "Không xóa được bytes mồ côi."); }
        });
    }
    /** Only a durable LOGICAL disposition may treat already-missing bytes as recovered cleanup. */
    public void disposeCommitted(String key,String expectedHash,UUID request,UUID record) {
        try (var guard=tryGuard(key).orElseThrow(()->ApiException.of("SERVICE_UNAVAILABLE","Disposition bytes are busy."))) {
            cleanup.executeWithoutResult(status->{
                lockReferences(jdbc);
                if(jdbc.queryForObject("select count(*) from cg_disposition_key k join cg_disposition_record r on r.request_id=k.request_id and r.record_id=k.record_id where k.storage_key=? and k.expected_hash=? and k.request_id=? and k.record_id=? and r.phase in ('LOGICAL','DONE')",Long.class,key,expectedHash,request,record)!=1L)throw ApiException.of("DISPOSITION_COMMITTED","Missing durable cleanup authorization.");
                if(jdbc.queryForObject("select count(*) from cg_file_blob where storage_key=? or redacted_storage_key=?",Long.class,key,key)!=0L)throw ApiException.of("READINESS_BLOCKED","Retained byte owner still references cleanup key.");
                var path=resolve(key);
                if(Files.exists(path,LinkOption.NOFOLLOW_LINKS)){read(key,expectedHash);try{Files.delete(path);}catch(IOException failure){throw ApiException.of("SERVICE_UNAVAILABLE","Disposition byte cleanup failed.");}}
                if(Files.exists(path,LinkOption.NOFOLLOW_LINKS))throw ApiException.of("SERVICE_UNAVAILABLE","Disposition cleanup incomplete.");
            });
        }catch(IOException failure){throw ApiException.of("SERVICE_UNAVAILABLE","Disposition guard unavailable.");}
    }
    static void lockReferences(JdbcTemplate jdbc) {
        jdbc.execute("set local lock_timeout='250ms'");
        jdbc.execute("set local statement_timeout='1000ms'");
        jdbc.execute("lock table cg_file_blob in share row exclusive mode");
    }

    Path maintenanceRoot() { verifyRoot(); return root; }
    boolean managedName(String name) { return KEY.matcher(name).matches() || CURRENT_PART.matcher(name).matches() || LEGACY_PART.matcher(name).matches(); }
    String referenceKey(String name) {
        if (KEY.matcher(name).matches()) return name;
        var current = CURRENT_PART.matcher(name);
        return current.matches() ? current.group(1) : name;
    }
    Optional<Candidate> candidate(String name) throws IOException {
        verifyRoot();
        if (!managedName(name)) return Optional.empty();
        var path = root.resolve(name);
        var attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile() || attributes.isSymbolicLink() || attributes.isOther()) return Optional.empty();
        return Optional.of(new Candidate(name, ManagedFileIdentity.capture(path, attributes), attributes.lastModifiedTime().toInstant()));
    }
    Optional<Guard> maintenanceGuard(String name) throws IOException {
        if (!managedName(name)) return Optional.empty();
        return tryGuard(guardKey(name));
    }
    boolean removeCandidate(Candidate expected, Instant cutoff) throws IOException {
        var current = candidate(expected.name());
        if (current.isEmpty() || !Objects.equals(current.get().identity(), expected.identity())
                || !current.get().modifiedAt().equals(expected.modifiedAt()) || current.get().modifiedAt().isAfter(cutoff)) return false;
        Files.delete(root.resolve(expected.name()));
        return true;
    }
    private String guardKey(String name) {
        var current = CURRENT_PART.matcher(name);
        if (current.matches()) return current.group(1);
        var legacy = LEGACY_PART.matcher(name);
        return legacy.matches() ? "legacy-" + legacy.group(1) : name;
    }
    private Optional<Guard> tryGuard(String key) throws IOException {
        verifyRoot();
        if (!KEY.matcher(key).matches() && !key.matches("legacy-[0-9]{1,20}")) throw ApiException.of("STORAGE_INTEGRITY", "Khóa guard không hợp lệ.");
        var path = guards.resolve(key + ".lck");
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            var attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile() || attributes.isSymbolicLink() || attributes.isOther()) throw ApiException.of("STORAGE_INTEGRITY", "Guard riêng tư không an toàn.");
        }
        var channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
        try {
            var lock = channel.tryLock();
            if (lock == null) { channel.close(); return Optional.empty(); }
            return Optional.of(new Guard(channel, lock));
        } catch (OverlappingFileLockException exception) { channel.close(); return Optional.empty(); }
        catch (IOException | RuntimeException exception) { channel.close(); throw exception; }
    }
    static final class Guard implements AutoCloseable {
        private final FileChannel channel;
        private final FileLock lock;
        private Guard(FileChannel channel, FileLock lock) { this.channel = channel; this.lock = lock; }
        @Override public void close() {
            try { lock.release(); } catch (IOException | RuntimeException exception) { LOG.warn("Private guard release failed"); }
            try { channel.close(); } catch (IOException | RuntimeException exception) { LOG.warn("Private guard channel close failed"); }
        }
    }
    private Path resolve(String key) {
        verifyRoot();
        if (!KEY.matcher(key).matches()) throw ApiException.of("STORAGE_INTEGRITY", "Khóa kho tệp không hợp lệ.");
        var path = root.resolve(key).normalize();
        if (!path.getParent().equals(root)) throw ApiException.of("STORAGE_INTEGRITY", "Khóa kho tệp không hợp lệ.");
        return path;
    }
    private void verifyRoot() {
        try {
            verifyExistingAncestors(root);
            if (!Objects.equals(rootIdentity, directoryIdentity(root)) || !Objects.equals(guardsIdentity, directoryIdentity(guards)))
                throw ApiException.of("STORAGE_INTEGRITY", "Root kho tệp đã thay đổi identity.");
        } catch (IOException exception) { throw ApiException.of("STORAGE_INTEGRITY", "Root kho tệp riêng tư không an toàn."); }
    }
    private static void verifyExistingAncestors(Path target) throws IOException {
        var current = target.getRoot();
        for (var part : target) {
            current = current.resolve(part);
            if (!Files.exists(current, LinkOption.NOFOLLOW_LINKS)) continue;
            directoryIdentity(current);
        }
    }
    private static Object directoryIdentity(Path path) throws IOException {
        var attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isDirectory() || attributes.isSymbolicLink() || attributes.isOther()) throw new IOException("Unsafe managed directory");
        return ManagedFileIdentity.capture(path, attributes);
    }
    private String sniff(byte[] bytes) {
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-') return "application/pdf";
        if (bytes.length >= 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255) return "image/jpeg";
        if (bytes.length >= 8 && (bytes[0] & 255) == 137 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G' && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10) return "image/png";
        throw ApiException.of("MEDIA_UNSUPPORTED", "Chỉ chấp nhận PDF, JPEG hoặc PNG có chữ ký nội dung hợp lệ.");
    }
}
