package com.cop.zip4j.model.entry;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.ZipUtils;
import com.cop.zip4j.utils.function.IOSupplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
 * @since 26.07.2019
 */
public abstract class ZipEntry {

    // TODO should be ZipEntry
    public static PathZipEntry of(Path path, ZipParameters parameters) {
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

    private static PathZipEntry apply(PathZipEntry zipEntry, ZipParameters parameters, Path path) {
        zipEntry.setPassword(parameters.getPassword());
        return zipEntry;
    }

    public static PathZipEntry create(CentralDirectory.FileHeader fileHeader) {
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

    private static PathZipEntry apply(PathZipEntry zipEntry, CentralDirectory.FileHeader fileHeader) {
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
