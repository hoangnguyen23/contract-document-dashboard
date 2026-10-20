package vn.contractgovernance.api.files;

import com.sun.jna.Platform;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HexFormat;

/** Provider file keys are optional; Windows uses a no-follow native handle identity instead. */
final class ManagedFileIdentity {
    private ManagedFileIdentity() {}
    static Object capture(Path path, BasicFileAttributes attributes) throws IOException {
        if (attributes.isSymbolicLink() || attributes.isOther()) throw new IOException("Unsafe managed filesystem entry");
        if (Platform.isWindows()) {
            if (path.getFileSystem() != FileSystems.getDefault()) throw new IOException("Unsupported Windows filesystem provider");
            return Windows.capture(path, attributes.isDirectory());
        }
        if (attributes.fileKey() == null) throw new IOException("Filesystem provider does not expose stable identity");
        return attributes.fileKey();
    }

    private record WindowsIdentity(long volumeSerialNumber, String fileId128) {}
    private static final class Windows {
        private static final Kernel32 KERNEL = Native.load("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
        static WindowsIdentity capture(Path path, boolean directory) throws IOException {
            var kernel = KERNEL;
            String absolute = path.toAbsolutePath().normalize().toString();
            String nativePath = absolute.startsWith("\\\\?\\") ? absolute
                    : absolute.startsWith("\\\\") ? "\\\\?\\UNC\\" + absolute.substring(2) : "\\\\?\\" + absolute;
            var handle = kernel.CreateFile(nativePath, WinNT.FILE_READ_ATTRIBUTES,
                    WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE | WinNT.FILE_SHARE_DELETE, null,
                    WinNT.OPEN_EXISTING, WinNT.FILE_FLAG_OPEN_REPARSE_POINT | WinNT.FILE_FLAG_BACKUP_SEMANTICS, null);
            if (handle == null || WinBase.INVALID_HANDLE_VALUE.equals(handle))
                throw new IOException("Windows filesystem metadata open failed (code=" + kernel.GetLastError() + ")");
            try {
                var tag = new WinBase.FILE_ATTRIBUTE_TAG_INFO();
                if (!kernel.GetFileInformationByHandleEx(handle, WinBase.FileAttributeTagInfo, tag.getPointer(), new DWORD(tag.size())))
                    throw new IOException("Windows filesystem attributes unavailable (code=" + kernel.GetLastError() + ")");
                tag.read();
                if (kernel.GetFileType(handle) != WinNT.FILE_TYPE_DISK
                        || (tag.FileAttributes & WinNT.FILE_ATTRIBUTE_REPARSE_POINT) != 0
                        || ((tag.FileAttributes & WinNT.FILE_ATTRIBUTE_DIRECTORY) != 0) != directory)
                    throw new IOException("Unsafe managed Windows filesystem entry");
                var info = new WinBase.FILE_ID_INFO();
                if (!kernel.GetFileInformationByHandleEx(handle, WinBase.FileIdInfo, info.getPointer(), new DWORD(info.size())))
                    throw new IOException("Windows filesystem identity unavailable (code=" + kernel.GetLastError() + ")");
                info.read();
                byte[] identifier = info.FileId.getPointer().getByteArray(0, 16);
                boolean nonzero = false;
                for (byte value : identifier) nonzero |= value != 0;
                if (!nonzero) throw new IOException("Windows filesystem returned an unusable identity");
                return new WindowsIdentity(info.VolumeSerialNumber, HexFormat.of().formatHex(identifier));
            } finally {
                if (!kernel.CloseHandle(handle)) throw new IOException("Windows filesystem metadata handle close failed");
            }
        }
    }
}
