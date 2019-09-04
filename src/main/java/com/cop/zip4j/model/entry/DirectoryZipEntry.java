package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
class DirectoryZipEntry extends ZipEntry {

    public DirectoryZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes) {
        super(fileName, 0, lastModifiedTime, Compression.STORE, CompressionLevel.NORMAL, Encryption.OFF, false, externalFileAttributes);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void setCompressedSize(long compressedSize) {
    }

    @Override
    public boolean isRoot() {
        return "/".equals(fileName);
    }

}
