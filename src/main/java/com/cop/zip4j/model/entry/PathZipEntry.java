package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class PathZipEntry extends ZipEntry {

    protected final Path path;
    protected String fileName;
    private final int lastModifiedTime;
    // TODO set from ZipModel
    private final Charset charset = StandardCharsets.UTF_8;

    protected Compression compression = Compression.STORE;
    protected CompressionLevel compressionLevel = CompressionLevel.NORMAL;
    protected Encryption encryption = Encryption.OFF;

    protected AesStrength strength = AesStrength.NONE;
    protected char[] password;
    private long disk;
    private long localFileHeaderOffs;
    protected Boolean dataDescriptorAvailable;

    /**
     * {@literal true} only if section {@link Zip64.ExtendedInfo} exists in {@link LocalFileHeader} and {@link CentralDirectory.FileHeader}.
     * In other words, do set this to {@code true}, to write given entry in ZIP64 format.
     */
    @Setter
    private boolean zip64;

    public boolean isRegularFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public abstract long getExpectedCompressedSize();

    public abstract void setCompressedSize(long compressedSize);

    public abstract long getCompressedSize();

    public boolean isRoot() {
        return "/".equals(fileName) || "\\".equals(fileName);
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

    public void setFileName(String fileName) {
        if (StringUtils.isBlank(fileName))
            throw new Zip4jException("PathZipEntry.name cannot be blank");

        this.fileName = ZipUtils.normalizeFileName.apply(fileName);
    }

    public abstract boolean isDataDescriptorAvailable();

    public abstract long write(@NonNull OutputStream out) throws IOException;

    @Override
    public String toString() {
        return path.toAbsolutePath().toString();
    }

    public ExternalFileAttributes getExternalFileAttribute() throws IOException {
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
        attributes.readFrom(path);
        return attributes;
    }

    public InternalFileAttributes getInternalFileAttributes() throws IOException {
        return InternalFileAttributes.NULL;
    }

}
