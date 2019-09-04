package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
public class DirectoryZipEntry extends PathZipEntry {

    private final ExternalFileAttributes externalFileAttributes;

    public DirectoryZipEntry(int lastModifiedTime, ExternalFileAttributes externalFileAttributes) {
        super(lastModifiedTime, Compression.STORE, CompressionLevel.NORMAL);
        this.externalFileAttributes = externalFileAttributes;
    }

    @Override
    public void setEncryption(@NonNull Encryption encryption) {
        this.encryption = Encryption.OFF;
    }

    @Override
    public long getExpectedCompressedSize() {
        return 0;
    }

    @Override
    public void setCompressedSize(long compressedSize) {
    }

    @Override
    public long getCompressedSize() {
        return 0;
    }

    @Override
    public void setFileName(String fileName) {
        if (StringUtils.isNotBlank(fileName) && !ZipUtils.isDirectory(fileName))
            fileName += '/';
        super.setFileName(fileName);
    }

    @Override
    public boolean isDataDescriptorAvailable() {
        if (dataDescriptorAvailable != null)
            return dataDescriptorAvailable;
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        return 0;
    }

}
