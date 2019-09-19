package ru.olegcherednik.zip4jvm.model.entry;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier;

import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
final class RegularFileZipEntry extends ZipEntry {

    private long checksum;

    public RegularFileZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes, Compression compression,
            CompressionLevel compressionLevel, Encryption encryption, IOSupplier<InputStream> inputStream) {
        super(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel, encryption, inputStream);
        setDataDescriptorAvailable(() -> true);
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }


}
