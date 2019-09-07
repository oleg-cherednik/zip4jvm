package ru.olegcherednik.zip4jvm.model.entry;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
final class RegularFileZipEntry extends ZipEntry {

    private static final long SIZE_2GB = 2_147_483_648L;

    private final IOSupplier<InputStream> inputStream;

    private long checksum;

    public RegularFileZipEntry(String fileName, int lastModifiedTime, ExternalFileAttributes externalFileAttributes, long uncompressedSize,
            Compression compression, CompressionLevel compressionLevel, Encryption encryption, boolean zip64, IOSupplier<InputStream> inputStream) {
        super(fileName, lastModifiedTime, externalFileAttributes, uncompressedSize, compression, compressionLevel, encryption, zip64);
        setDataDescriptorAvailable(() -> true);
        this.inputStream = inputStream;
    }

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public long write(@NonNull OutputStream out) throws IOException {
        try (InputStream in = inputStream.get(this)) {
            return uncompressedSize > SIZE_2GB ? IOUtils.copyLarge(in, out) : IOUtils.copy(in, out);
        }
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

    @Override
    public boolean isRoot() {
        return false;
    }

}
