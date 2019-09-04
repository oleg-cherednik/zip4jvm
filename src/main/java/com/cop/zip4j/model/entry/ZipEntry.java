package com.cop.zip4j.model.entry;

import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.aes.AesStrength;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.ZipUtils;
import com.cop.zip4j.utils.function.IOSupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;

import static com.cop.zip4j.model.ZipModel.MAX_ENTRY_SIZE;
import static com.cop.zip4j.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class ZipEntry {

    protected final String fileName;
    protected final long uncompressedSize;
    private final int lastModifiedTime;
    // TODO set from ZipModel
    protected final Charset charset = StandardCharsets.UTF_8;
    protected final Compression compression;
    private final CompressionLevel compressionLevel;
    protected final Encryption encryption;
    /**
     * {@literal true} only if section {@link Zip64.ExtendedInfo} exists in {@link LocalFileHeader} and {@link CentralDirectory.FileHeader}.
     * In other words, do set this to {@code true}, to write given entry in ZIP64 format.
     */
    private final boolean zip64;
    private final ExternalFileAttributes externalFileAttributes;

    // zip: set it in constructor
    // unzip: set it before unzip
    private char[] password;

    private long disk;
    private long localFileHeaderOffs;
    @Getter(AccessLevel.NONE)
    private BooleanSupplier dataDescriptorAvailable = () -> false;

    public boolean isRegularFile() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public abstract void setCompressedSize(long compressedSize);

    public abstract long getCompressedSize();

    public boolean isRoot() {
        return "/".equals(fileName) || "\\".equals(fileName);
    }

    public AesStrength getStrength() {
        return AesEngine.getStrength(encryption);
    }

    public boolean isEncrypted() {
        return getEncryption() != Encryption.OFF;
    }

    public long write(@NonNull OutputStream out) throws IOException {
        return 0;
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

    // --------------------

    // TODO should be ZipEntry
    public static ZipEntry of(Path path, ZipParameters parameters) {
        if (Files.isDirectory(path)) {
            try {
                String fileName = parameters.getRelativeEntryName(path);
                int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(path).toMillis());
                ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
                attributes.readFrom(path);
                return apply(new DirectoryZipEntry(fileName, lastModifiedTime, attributes), parameters, path);
            } catch(IOException e) {
                throw new Zip4jException(e);
            }
        }

        if (Files.isRegularFile(path)) {
            try {
                String fileName = parameters.getRelativeEntryName(path);
                long uncompressedSize = Files.size(path);
                int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(path).toMillis());
                Compression compression = parameters.getCompression();
                CompressionLevel compressionLevel = parameters.getCompressionLevel();
                Encryption encryption = parameters.getEncryption();
                boolean zip64 = parameters.isZip64() || uncompressedSize > ZipModel.MAX_ENTRY_SIZE;
                ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
                attributes.readFrom(path);
                IOSupplier<InputStream> inputStream = () -> new FileInputStream(path.toFile());
                return apply(new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                        attributes, inputStream), parameters, path);
            } catch(IOException e) {
                throw new Zip4jException(e);
            }
        }

        throw new Zip4jException("Cannot add neither directory nor regular file to zip");
    }

    private static ZipEntry apply(ZipEntry zipEntry, ZipParameters parameters, Path path) {
        zipEntry.setPassword(parameters.getPassword());
        return zipEntry;
    }

    public static ZipEntry create(CentralDirectory.FileHeader fileHeader) {
        String fileName = ZipUtils.normalizeFileName.apply(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();

        if (ZipUtils.isDirectory(fileName))
            return apply(new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes), fileHeader);

        long uncompressedSize = getUncompressedSize(fileHeader);
        Compression compression = fileHeader.getCompression();
        CompressionLevel compressionLevel = fileHeader.getGeneralPurposeFlag().getCompressionLevel();
        Encryption encryption = fileHeader.getEncryption();
        boolean zip64 = fileHeader.isZip64();
        IOSupplier<InputStream> inputStream = () -> NullInputStream.INSTANCE;
        return apply(new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                externalFileAttributes, inputStream), fileHeader);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NullInputStream extends InputStream {

        public static final NullInputStream INSTANCE = new NullInputStream();

        @Override
        public int read() throws IOException {
            return IOUtils.EOF;
        }
    }

    private static ZipEntry apply(ZipEntry zipEntry, CentralDirectory.FileHeader fileHeader) {
        zipEntry.setChecksum(fileHeader.getCrc32());
        zipEntry.setCompressedSize(getCompressedSize(fileHeader));
        zipEntry.setDisk(getDisk(fileHeader));
        zipEntry.setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());
        return zipEntry;
    }

    private static long getDisk(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getDisk() == MAX_TOTAL_DISKS)
            return fileHeader.getExtraField().getExtendedInfo().getDisk();
        return fileHeader.getDisk();
    }

    private static long getCompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getCompressedSize() == MAX_ENTRY_SIZE)
            return fileHeader.getExtraField().getExtendedInfo().getCompressedSize();
        return fileHeader.getCompressedSize();
    }

    private static long getUncompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getUncompressedSize() == MAX_ENTRY_SIZE)
            return fileHeader.getExtraField().getExtendedInfo().getUncompressedSize();
        return fileHeader.getUncompressedSize();
    }

}
