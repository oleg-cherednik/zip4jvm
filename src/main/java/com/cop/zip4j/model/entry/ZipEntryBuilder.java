package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.ZipUtils;
import com.cop.zip4j.utils.function.IOSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.cop.zip4j.model.ZipModel.MAX_ENTRY_SIZE;
import static com.cop.zip4j.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntryBuilder {

    public static ZipEntry create(@NonNull Path path, @NonNull ZipParameters parameters) {
        try {
            return Files.isDirectory(path) ? createDirectoryEntry(path, parameters) : createRegularFileEntry(path, parameters);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    public static ZipEntry create(@NonNull CentralDirectory.FileHeader fileHeader) {
        ZipEntry zipEntry = ZipUtils.isDirectory(fileHeader.getFileName()) ? createDirectoryEntry(fileHeader) : createRegularFileEntry(fileHeader);
        zipEntry.setChecksum(fileHeader.getCrc32());
        zipEntry.setCompressedSize(getCompressedSize(fileHeader));
        zipEntry.setDisk(getDisk(fileHeader));
        zipEntry.setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());
        return zipEntry;
    }

    private static ZipEntry createDirectoryEntry(Path dir, ZipParameters parameters) throws IOException {
        String fileName = parameters.getRelativeEntryName(dir);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(dir).toMillis());
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
        attributes.readFrom(dir);

        DirectoryZipEntry zipEntry = new DirectoryZipEntry(fileName, lastModifiedTime, attributes);
        zipEntry.setPassword(parameters.getPassword());

        return zipEntry;
    }

    private static ZipEntry createDirectoryEntry(CentralDirectory.FileHeader fileHeader) {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        return new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
    }

    private static ZipEntry createRegularFileEntry(Path file, ZipParameters parameters) throws IOException {
        String fileName = parameters.getRelativeEntryName(file);
        long uncompressedSize = Files.size(file);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(file).toMillis());
        Compression compression = parameters.getCompression();
        CompressionLevel compressionLevel = parameters.getCompressionLevel();
        Encryption encryption = parameters.getEncryption();
        boolean zip64 = parameters.isZip64() || uncompressedSize > MAX_ENTRY_SIZE;
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate();
        attributes.readFrom(file);
        IOSupplier<InputStream> inputStream = () -> new FileInputStream(file.toFile());

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel,
                encryption, zip64, attributes, inputStream);
        zipEntry.setPassword(parameters.getPassword());

        return zipEntry;
    }

    private static ZipEntry createRegularFileEntry(CentralDirectory.FileHeader fileHeader) {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        long uncompressedSize = getUncompressedSize(fileHeader);
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        Compression compression = fileHeader.getCompression();
        CompressionLevel compressionLevel = fileHeader.getGeneralPurposeFlag().getCompressionLevel();
        Encryption encryption = fileHeader.getEncryption();
        boolean zip64 = fileHeader.isZip64();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        IOSupplier<InputStream> inputStream = () -> NullInputStream.INSTANCE;
        return new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                externalFileAttributes, inputStream);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NullInputStream extends InputStream {

        public static final NullInputStream INSTANCE = new NullInputStream();

        @Override
        public int read() throws IOException {
            return IOUtils.EOF;
        }
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
