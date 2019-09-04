package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
public class DirectoryZipEntry extends PathZipEntry {

    public DirectoryZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes) {
        super(dirFileName(fileName), 0, lastModifiedTime, Compression.STORE, CompressionLevel.NORMAL, Encryption.OFF, false, externalFileAttributes);
    }

    private static String dirFileName(String fileName) {
        return ZipUtils.normalizeFileName.apply(ZipUtils.isDirectory(fileName) ? fileName : fileName + '/');
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void setCompressedSize(long compressedSize) {
    }

    @Override
    public long getCompressedSize() {
        return 0;
    }

}
