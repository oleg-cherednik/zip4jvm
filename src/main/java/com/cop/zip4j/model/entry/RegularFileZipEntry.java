package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import lombok.Getter;
import lombok.NonNull;
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
public class RegularFileZipEntry extends PathZipEntry {

    private final long size;
    private final long crc32;

    public RegularFileZipEntry(Path file, long size, long crc32) {
        super(file);
        this.size = size;
        this.crc32 = crc32;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public long crc32() {
        return crc32;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(path.toFile())) {
            return IOUtils.copyLarge(in, out);
        }
    }

    @Override
    public void setCompressionMethod(@NonNull CompressionMethod compressionMethod) throws IOException {
        this.compressionMethod = size == 0 ? CompressionMethod.STORE : compressionMethod;
    }

    @Override
    public void setEncryption(@NonNull Encryption encryption) {
        this.encryption = encryption;
    }

}
