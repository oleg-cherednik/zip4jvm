package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Builder(toBuilder = true)
public final class ZipEntrySettings {

    @NonNull
    @Builder.Default
    private final Compression compression = Compression.DEFLATE;
    @NonNull
    @Builder.Default
    private final CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    @NonNull
    @Builder.Default
    private final Encryption encryption = Encryption.OFF;
    private final char[] password;
    private final String comment;
    /**
     * Write all entries as well as entire zip archive in ZIP64 format.
     * If it's {@literal false}, then it will be automatically set if require.
     */
    private final boolean zip64;
    @Builder.Default
    private final boolean utf8 = true;

    @Setter
    private Path defaultFolderPath;

    public String getRelativeFileName(Path path) {
        path = path.toAbsolutePath();
        Path root = defaultFolderPath != null ? defaultFolderPath : path.getParent();
        String str = root.relativize(path).toString();

        if (Files.isDirectory(path))
            str += '/';

        return ZipUtils.normalizeFileName(str);
    }

    /*
     * dir:
     * 1. password
     * 2. comment
     */

    /*
     * file:
     * 1. compression
     * 2. compression level
     * 3. encryption
     * 4. zip64
     * 5. password
     * 6. comment
     */
}
