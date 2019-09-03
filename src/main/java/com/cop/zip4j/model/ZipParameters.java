package com.cop.zip4j.model;

import com.cop.zip4j.utils.ZipUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
@Builder(toBuilder = true)
public class ZipParameters {

    @NonNull
    @Builder.Default
    private Compression compression = Compression.DEFLATE;
    @NonNull
    @Builder.Default
    private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    @NonNull
    @Builder.Default
    private Encryption encryption = Encryption.OFF;
    private char[] password;
    private String rootFolderInZip;
    private Path defaultFolderPath;
    @Builder.Default
    private long splitLength = ZipModel.NO_SPLIT;
    private String comment;

    /**
     * Write all entries as well as entire zip archive in ZIP64 format.
     * If it's {@literal false}, then it will be automatically set if require.
     */
    public boolean zip64;

    @NonNull
    public String getRelativeEntryName(Path entry) {
        Path entryPath = entry.toAbsolutePath();
        Path rootPath = defaultFolderPath != null ? defaultFolderPath : entryPath.getParent();

        String path = rootPath.relativize(entryPath).toString();

        if (Files.isDirectory(entryPath))
            path += '/';

        if (rootFolderInZip != null)
            path = FilenameUtils.concat(path, rootFolderInZip);

        return ZipUtils.normalizeFileName.apply(path);
    }

}
