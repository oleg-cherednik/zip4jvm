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

    private final ExternalFileAttributes externalFileAttributes;
    private final IOSupplier<InputStream> inputStream;

    private long checksum;
    private long compressedSize;

    public RegularFileZipEntry(long uncompressedSize, int lastModifiedTime, Compression compression, CompressionLevel compressionLevel,
            Encryption encryption, boolean zip64, ExternalFileAttributes externalFileAttributes, IOSupplier<InputStream> inputStream) {
        super(uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64);
        this.externalFileAttributes = externalFileAttributes;
        this.inputStream = inputStream;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = inputStream.get()) {
            return uncompressedSize > SIZE_2GB ? IOUtils.copyLarge(in, out) : IOUtils.copy(in, out);
        }
    }

    @Override
    public long getExpectedCompressedSize() {
        return compression == Compression.STORE ? encryption.getCompressedSizeFunc().apply(uncompressedSize) : 0;
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

}
