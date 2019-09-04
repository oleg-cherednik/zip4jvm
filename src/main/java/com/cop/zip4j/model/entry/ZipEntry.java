package com.cop.zip4j.model.entry;

import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.aes.AesStrength;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

/**
 * Represents one single entry in zip archive, i.e. one instance of {@link LocalFileHeader} and related {@link CentralDirectory.FileHeader}.
 *
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class ZipEntry {

    protected final String fileName;
    protected final long uncompressedSize;
    private final int lastModifiedTime;
    // TODO set from ZipModel
    protected final Charset charset = StandardCharsets.UTF_8;
    protected final Compression compression;
    private final CompressionLevel compressionLevel;
    protected final Encryption encryption;
    /**
     * {@literal true} only if section {@link Zip64.ExtendedInfo} exists in {@link LocalFileHeader} and {@link CentralDirectory.FileHeader}.
     * In other words, do set this to {@code true}, to write given entry in ZIP64 format.
     */
    private final boolean zip64;
    private final ExternalFileAttributes externalFileAttributes;

    // zip: set it in constructor
    // unzip: set it before unzip
    private char[] password;

    private long disk;
    private long localFileHeaderOffs;
    @Getter(AccessLevel.NONE)
    private BooleanSupplier dataDescriptorAvailable = () -> false;
    private long compressedSize;

    public boolean isRegularFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public abstract boolean isRoot();

    public final AesStrength getStrength() {
        return AesEngine.getStrength(encryption);
    }

    public final boolean isEncrypted() {
        return encryption != Encryption.OFF;
    }

    public long write(@NonNull OutputStream out) throws IOException {
        return 0;
    }

    @Override
    public String toString() {
        return fileName;
    }

    public InternalFileAttributes getInternalFileAttributes() throws IOException {
        return InternalFileAttributes.NULL;
    }

    public long getChecksum() {
        return 0;
    }

    public void setChecksum(long checksum) {
    }

    public void checkCompressedSize(long actual) {
    }

    public final boolean isDataDescriptorAvailable() {
        return dataDescriptorAvailable.getAsBoolean();
    }

}
