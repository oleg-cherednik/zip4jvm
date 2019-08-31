package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.activity.Activity;
import com.cop.zip4j.model.activity.PlainActivity;
import com.cop.zip4j.model.activity.Zip64Activity;
import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    protected String name;
    private final int lastModifiedTime;
    // TODO set from ZipModel
    private final Charset charset = StandardCharsets.UTF_8;

    protected Compression compression = Compression.STORE;
    protected CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    protected Encryption encryption = Encryption.OFF;

    @Setter
    protected AesStrength strength = AesStrength.NONE;
    @Setter
    protected char[] password;

    @Setter
    private long compressedSizeWithEncryptionHeader;
    @Setter
    private int disc;
    @Setter
    private long localFileHeaderOffs;
    @Setter
    private Activity activity = PlainActivity.INSTANCE;

    @Setter
    protected Boolean dataDescriptorAvailable;

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

    public abstract long getExpectedCompressedSize();

    public boolean isRoot() {
        return "/".equals(name) || "\\".equals(name);
    }

    public abstract void setCompression(@NonNull Compression compression);

    public void setCompressionLevel(@NonNull CompressionLevel compressionLevel) {
        this.compressionLevel = CompressionLevel.NORMAL;
    }

    public abstract void setEncryption(@NonNull Encryption encryption);

    public AesStrength getStrength() {
        return encryption == Encryption.AES ? strength : AesStrength.NONE;
    }

    public boolean isEncrypted() {
        return getEncryption() != Encryption.OFF;
    }

    public void setName(String name) {
        if (StringUtils.isBlank(name))
            throw new Zip4jException("PathZipEntry.name cannot be blank");

        this.name = ZipUtils.normalizeFileName.apply(name);
    }

    public abstract boolean isDataDescriptorAvailable();

    public boolean isZip64() {
        return activity instanceof Zip64Activity;
    }

}
