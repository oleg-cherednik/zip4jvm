package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import lombok.NonNull;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DirectoryZipEntry extends PathZipEntry {

    public DirectoryZipEntry(Path dir) {
        super(dir);
    }

    @Override
    public void setCompression(@NonNull Compression compression) {
        this.compression = Compression.STORE;
    }

    @Override
    public void setEncryption(@NonNull Encryption encryption) {
        this.encryption = Encryption.OFF;
    }

}
