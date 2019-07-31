package com.cop.zip4j.model;

import com.cop.zip4j.model.aes.AesStrength;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
@Builder(toBuilder = true)
public class ZipParameters {

    @NonNull
    @Builder.Default
    private CompressionMethod compressionMethod = CompressionMethod.DEFLATE;
    @NonNull
    @Builder.Default
    private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    @NonNull
    @Builder.Default
    private Encryption encryption = Encryption.OFF;
    private char[] password;
    @Builder.Default
    private AesStrength aesStrength = AesStrength.NONE;
    private String rootFolderInZip;
    private Path defaultFolderPath;
    @Builder.Default
    private long splitLength = ZipModel.NO_SPLIT;
    private String comment;
    public boolean zip64;

    @NonNull
    public String getRelativeEntryName(Path entry) {
        Path entryPath = entry.toAbsolutePath();
        Path rootPath = defaultFolderPath != null ? defaultFolderPath : entryPath.getParent();

        String path = rootPath.relativize(entryPath).toString();

        if (Files.isDirectory(entryPath))
            path += File.separator;

        if (rootFolderInZip != null)
            path = FilenameUtils.concat(path, rootFolderInZip);

        return path;
    }

}
