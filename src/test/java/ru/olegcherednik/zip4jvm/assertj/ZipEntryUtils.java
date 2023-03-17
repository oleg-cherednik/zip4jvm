package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ZipEntryUtils {

    public static boolean isDirectory(ZipArchiveEntry entry) {
        return entry.isDirectory();
    }

    public static boolean isRegularFile(ZipArchiveEntry entry) {
        return !isDirectory(entry) && !getExternalAttributes(entry).isSymlink();
    }

    public static boolean isSymlink(ZipArchiveEntry entry) {
        return !isDirectory(entry) && getExternalAttributes(entry).isSymlink();

    }

    private static ExternalFileAttributes getExternalAttributes(ZipArchiveEntry entry) {
        long attr = entry.getExternalAttributes();

        return ExternalFileAttributes.build(PROP_OS_NAME)
                                     .readFrom(new byte[] {
                                             (byte)(attr & 0xFF),
                                             (byte)((attr >> 8) & 0xFF),
                                             (byte)((attr >> 16) & 0xFF),
                                             (byte)((attr >> 24) & 0xFF)
                                     });
    }
}
