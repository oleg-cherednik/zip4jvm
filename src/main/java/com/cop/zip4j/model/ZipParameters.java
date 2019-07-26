package com.cop.zip4j.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

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
    private boolean readHiddenFiles;
    private char[] password;
    @Builder.Default
    private AesStrength aesStrength = AesStrength.NONE;
    @Builder.Default
    private boolean includeRootFolder = true;
    private String rootFolderInZip;
    @Builder.Default
    private TimeZone timeZone = TimeZone.getDefault();
    private long crc32;
    private Path defaultFolderPath;
    @Builder.Default
    private long splitLength = ZipModel.NO_SPLIT;
    private String comment;
    public boolean zip64;

    @NonNull
    public CompressionMethod getActualCompressionMethod() {
        return encryption == Encryption.AES ? CompressionMethod.AES_ENC : compressionMethod;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public void setPassword(char[] password) {
        this.password = ArrayUtils.clone(password);
    }

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
