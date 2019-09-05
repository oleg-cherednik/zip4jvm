package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
    private final String rootFolderInZip;
    @Builder.Default
    private final long splitLength = ZipModel.NO_SPLIT;
    private final String comment;
    /**
     * Write all entries as well as entire zip archive in ZIP64 format.
     * If it's {@literal false}, then it will be automatically set if require.
     */
    public final boolean zip64;

    @Setter
    private Path defaultFolderPath;

}
