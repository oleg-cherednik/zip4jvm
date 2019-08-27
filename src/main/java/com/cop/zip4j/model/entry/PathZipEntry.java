package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@RequiredArgsConstructor
public abstract class PathZipEntry extends ZipEntry {

    protected final Path path;
    @Setter
    protected String name;
    private final int lastModifiedTime;

    protected Compression compression = Compression.STORE;
    protected CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    protected Encryption encryption = Encryption.OFF;

    @Setter
    protected AesStrength strength = AesStrength.NONE;
    @Setter
    protected char[] password;

    @Setter
    private long compressedSizeNew;
    @Setter
    private int disc;

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(path);
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    @Override
    public String getAbsolutePath() {
        return path.toAbsolutePath().toString();
    }

    public abstract long getCompressedSize();

    public boolean isRoot() {
        return "/".equals(name) || "\\".equals(name);
    }

    public abstract void setCompression(@NonNull Compression compression) throws IOException;

    public void setCompressionLevel(@NonNull CompressionLevel compressionLevel) throws IOException {
        this.compressionLevel = CompressionLevel.NORMAL;
    }

    public abstract void setEncryption(@NonNull Encryption encryption);

    public AesStrength getStrength() {
        return encryption == Encryption.AES ? strength : AesStrength.NONE;
    }

}
