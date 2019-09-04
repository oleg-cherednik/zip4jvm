package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.function.IOSupplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
public class RegularFileZipEntry extends PathZipEntry {

    private static final long SIZE_2GB = 2_147_483_648L;

    private final long size;
    private final ExternalFileAttributes externalFileAttributes;
    private final IOSupplier<InputStream> inputStream;

    private long checksum;
    private long compressedSize;

    public RegularFileZipEntry(int lastModifiedTime, Compression compression, CompressionLevel compressionLevel, long size,
            ExternalFileAttributes externalFileAttributes, IOSupplier<InputStream> inputStream) {
        super(lastModifiedTime, compression, compressionLevel);
        this.size = size;
        this.externalFileAttributes = externalFileAttributes;
        this.inputStream = inputStream;
    }

    @Override
    public long getUncompressedSize() {
        return size;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = inputStream.get()) {
            return size > SIZE_2GB ? IOUtils.copyLarge(in, out) : IOUtils.copy(in, out);
        }
    }

    @Override
    public void setEncryption(@NonNull Encryption encryption) {
        this.encryption = encryption;
    }

    @Override
    public long getExpectedCompressedSize() {
        return compression == Compression.STORE ? encryption.getCompressedSize().apply(this) : 0;
    }

    @Override
    public boolean isDataDescriptorAvailable() {
        if (dataDescriptorAvailable != null)
            return dataDescriptorAvailable;
        return true;
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public String toString() {
        return getFileName();
    }

}
