package ru.olegcherednik.zip4jvm.model.entry;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
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

    public RegularFileZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes, long uncompressedSize,
            Compression compression, CompressionLevel compressionLevel, Encryption encryption, boolean zip64, IOSupplier<InputStream> inputStream) {
        super(fileName, lastModifiedTime, externalFileAttributes, uncompressedSize, compression, compressionLevel, encryption, zip64, inputStream);
        setDataDescriptorAvailable(() -> true);
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    /** It's able to check compressed size only for {@link Compression#STORE} */
    @Override
    public void checkCompressedSize(long actual) {
        if (compression != Compression.STORE)
            return;

        long expected = encryption.getExpectedCompressedSizeFunc().apply(uncompressedSize);

        if (expected != actual)
            throw new Zip4jException("CompressedSize is not matched: " + getFileName());
    }

}
