package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

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
