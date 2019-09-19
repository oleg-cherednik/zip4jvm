package ru.olegcherednik.zip4jvm.model.entry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.function.BooleanSupplier;

/**
 * Represents one single entry in zip archive, i.e. one instance of {@link LocalFileHeader} and related {@link CentralDirectory.FileHeader}.
 *
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class ZipEntry {

    public static final Comparator<ZipEntry> SORT_BY_DISC_LOCAL_FILE_HEADER_OFFS =
            Comparator.comparingLong(ZipEntry::getDisk).thenComparing(ZipEntry::getLocalFileHeaderOffs);

    public static final long SIZE_2GB = 2_147_483_648L;

    private final String fileName;
    private final int lastModifiedTime;
    private final ExternalFileAttributes externalFileAttributes;

    protected final Compression compression;
    private final CompressionLevel compressionLevel;
    protected final Encryption encryption;
    private final IOSupplier<InputStream> inputStream;

    /**
     * {@literal true} only if section {@link Zip64.ExtendedInfo} exists in {@link LocalFileHeader} and {@link CentralDirectory.FileHeader}.
     * In other words, do set this to {@code true}, to write given entry in ZIP64 format.
     */
    private boolean zip64;

    private char[] password;
    private long disk;
    private long localFileHeaderOffs;
    @Getter(AccessLevel.NONE)
    private BooleanSupplier dataDescriptorAvailable = () -> false;
    private long uncompressedSize;
    private long compressedSize;

    private String comment;
    private boolean utf8;
    private long size;

    public boolean isRegularFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public final AesStrength getStrength() {
        return AesEngine.getStrength(encryption);
    }

    public final boolean isEncrypted() {
        return encryption != Encryption.OFF;
    }

    public InputStream getIn() throws IOException {
        return inputStream.get(this);
    }

    @Override
    public String toString() {
        return fileName;
    }

    public InternalFileAttributes getInternalFileAttributes() throws IOException {
        return InternalFileAttributes.NULL;
    }

    public long getChecksum() {
        return 0;
    }

    public void setChecksum(long checksum) {
    }

    public void checkCompressedSize(long actual) {
    }

    public final boolean isDataDescriptorAvailable() {
        return dataDescriptorAvailable.getAsBoolean();
    }

}
