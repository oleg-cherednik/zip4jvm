package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
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
public class RegularFileZipEntry extends ZipEntry {

    private static final long SIZE_2GB = 2_147_483_648L;

    private final IOSupplier<InputStream> inputStream;

    private long checksum;
    private long compressedSize;

    public RegularFileZipEntry(String fileName, long uncompressedSize, int lastModifiedTime, Compression compression,
            CompressionLevel compressionLevel, Encryption encryption, boolean zip64, ExternalFileAttributes externalFileAttributes,
            IOSupplier<InputStream> inputStream) {
        super(ZipUtils.normalizeFileName.apply(fileName), uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                externalFileAttributes);
        setDataDescriptorAvailable(() -> true);
        this.inputStream = inputStream;
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = inputStream.get()) {
            return uncompressedSize > SIZE_2GB ? IOUtils.copyLarge(in, out) : IOUtils.copy(in, out);
        }
    }

    /** It's able to check compressed size only for {@link Compression#STORE} */
    @Override
    public void checkCompressedSize(long actual) {
        if (compression != Compression.STORE)
            return;

        long expected = encryption.getCompressedSizeFunc().apply(uncompressedSize);

        if (expected != actual)
            throw new Zip4jException("CompressedSize is not matched: " + fileName);
    }

}
