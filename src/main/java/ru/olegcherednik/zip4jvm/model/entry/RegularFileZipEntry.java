package ru.olegcherednik.zip4jvm.model.entry;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamSupplier;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
final class RegularFileZipEntry extends ZipEntry {

    private long checksum;

    public RegularFileZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes, Compression compression,
            CompressionLevel compressionLevel, Encryption encryption, ZipEntryInputStreamSupplier inputStreamSup) {
        super(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel, encryption, inputStreamSup);
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

}
