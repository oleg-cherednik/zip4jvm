package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public class DirectoryZipEntry extends PathZipEntry {

    private final Path dir;

    public DirectoryZipEntry(Path dir, int lastModifiedTime) {
        super(lastModifiedTime);
        this.dir = dir;
    }

    @Override
    public void setCompression(@NonNull Compression compression) {
        this.compression = Compression.STORE;
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
    public ExternalFileAttributes getExternalFileAttributes() throws IOException {
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
        attributes.readFrom(dir);
        return attributes;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        return 0;
    }

}
