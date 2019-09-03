package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
public class RegularFileZipEntry extends PathZipEntry {

    private static final long SIZE_2GB = 2_147_483_648L;

    private final long size;

    private long checksum;
    private long compressedSize;

    public RegularFileZipEntry(Path file, long size, int lastModifiedTime) {
        super(file, lastModifiedTime);
        this.size = size;
    }

    @Override
    public long getUncompressedSize() {
        return size;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(getPath().toFile())) {
            return size > SIZE_2GB ? IOUtils.copyLarge(in, out) : IOUtils.copy(in, out);
        }
    }

    @Override
    public void setCompression(@NonNull Compression compression) {
        this.compression = size == 0 ? Compression.STORE : compression;
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

}
