package vn.contractgovernance.api.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.JsonSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Opaque names, private filesystem, finalized bytes before the database reference. */
@Component
public class PrivateBlobStore {
    public record Stored(String key, String sha256, int sizeBytes, String mediaType) {}
    public static final int MAX_BYTES = 10 * 1024 * 1024;
    private final Path root;
    public PrivateBlobStore(@Value("${cg.storage-root:./var/private-files}") String path) {
        try { root = Path.of(path).toAbsolutePath().normalize(); Files.createDirectories(root); }
        catch (IOException exception) { throw new IllegalStateException("Private file store unavailable", exception); }
    }
    public Stored write(InputStream input) {
        try {
            var bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) throw ApiException.of("FILE_TOO_LARGE", "Tệp vượt quá giới hạn 10 MiB.");
            if (bytes.length == 0) throw ApiException.validation("/file", "Tệp phải có nội dung.");
            var mediaType = sniff(bytes);
            var key = UUID.randomUUID().toString();
            var temporary = Files.createTempFile(root, "quarantine-", ".part");
            try {
                Files.write(temporary, bytes);
                try { Files.move(temporary, resolve(key), StandardCopyOption.ATOMIC_MOVE); }
                catch (java.nio.file.AtomicMoveNotSupportedException exception) { Files.move(temporary, resolve(key)); }
            } finally { Files.deleteIfExists(temporary); }
            return new Stored(key, JsonSupport.sha256(bytes), bytes.length, mediaType);
        } catch (IOException exception) { throw ApiException.of("SERVICE_UNAVAILABLE", "Kho tệp riêng tư hiện không khả dụng."); }
    }
    public byte[] read(String key, String expectedHash) {
        try {
            var path = resolve(key);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path))
                throw ApiException.of("STORAGE_INTEGRITY", "Không xác minh được tệp đã lưu.");
            var bytes = Files.readAllBytes(path);
            if (!JsonSupport.sha256(bytes).equals(expectedHash)) throw ApiException.of("STORAGE_INTEGRITY", "Hash tệp không khớp bản ghi.");
            return bytes;
        } catch (IOException exception) { throw ApiException.of("STORAGE_INTEGRITY", "Không đọc được tệp đã lưu."); }
    }
    public void delete(String key) {
        try { Files.deleteIfExists(resolve(key)); }
        catch (IOException exception) { throw ApiException.of("SERVICE_UNAVAILABLE", "Không xóa được bytes trong kho riêng tư."); }
    }
    private Path resolve(String key) {
        if (!key.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
            throw ApiException.of("STORAGE_INTEGRITY", "Khóa kho tệp không hợp lệ.");
        var path = root.resolve(key).normalize();
        if (!path.getParent().equals(root)) throw ApiException.of("STORAGE_INTEGRITY", "Khóa kho tệp không hợp lệ.");
        return path;
    }
    private String sniff(byte[] bytes) {
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-') return "application/pdf";
        if (bytes.length >= 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255) return "image/jpeg";
        if (bytes.length >= 8 && (bytes[0] & 255) == 137 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G' && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10) return "image/png";
        throw ApiException.of("MEDIA_UNSUPPORTED", "Chỉ chấp nhận PDF, JPEG hoặc PNG có chữ ký nội dung hợp lệ.");
    }
}
