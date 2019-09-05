package ru.olegcherednik.zip4jvm.model.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipParameters;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.IOSupplier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntryBuilder {

    public static ZipEntry create(@NonNull Path path, @NonNull ZipEntrySettings settings) {
        try {
            return Files.isDirectory(path) ? createDirectoryEntry(path, settings) : createRegularFileEntry(path, settings);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private static ZipEntry createDirectoryEntry(Path dir, ZipEntrySettings settings) throws IOException {
        String fileName = settings.getRelativeFileName(dir);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(dir).toMillis());
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(dir);

        DirectoryZipEntry zipEntry = new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
        zipEntry.setPassword(settings.getPassword());
        zipEntry.setComment(settings.getComment());
        zipEntry.setUtf8(settings.isUtf8());

        return zipEntry;
    }

    private static ZipEntry createRegularFileEntry(Path file, ZipEntrySettings settings) throws IOException {
        String fileName = settings.getRelativeFileName(file);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(file).toMillis());
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(file);

        long uncompressedSize = Files.size(file);
        Compression compression = settings.getCompression();
        CompressionLevel compressionLevel = settings.getCompressionLevel();
        Encryption encryption = settings.getEncryption();
        boolean zip64 = settings.isZip64() || uncompressedSize > MAX_ENTRY_SIZE;

        IOSupplier<InputStream> inputStream = () -> new FileInputStream(file.toFile());

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, uncompressedSize, compression,
                compressionLevel, encryption, zip64, inputStream);
        zipEntry.setPassword(settings.getPassword());
        zipEntry.setComment(settings.getComment());
        zipEntry.setUtf8(settings.isUtf8());

        return zipEntry;
    }

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
        String fileName = parameters.getRelativeFileName(dir);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(dir).toMillis());
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate(dir);
        DirectoryZipEntry zipEntry = new DirectoryZipEntry(fileName, lastModifiedTime, attributes);
        zipEntry.setPassword(parameters.getPassword());
        zipEntry.setUtf8(parameters.isUtf8());

        return zipEntry;
    }

    private static ZipEntry createDirectoryEntry(CentralDirectory.FileHeader fileHeader) {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        return new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
    }

    private static ZipEntry createRegularFileEntry(Path file, ZipParameters parameters) throws IOException {
        String fileName = parameters.getRelativeFileName(file);
        long uncompressedSize = Files.size(file);
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(file).toMillis());
        Compression compression = parameters.getCompression();
        CompressionLevel compressionLevel = parameters.getCompressionLevel();
        Encryption encryption = parameters.getEncryption();
        boolean zip64 = parameters.isZip64() || uncompressedSize > MAX_ENTRY_SIZE;
        ExternalFileAttributes attributes = ExternalFileAttributes.createOperationBasedDelegate(file);
        IOSupplier<InputStream> inputStream = () -> new FileInputStream(file.toFile());

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, attributes, uncompressedSize, compression,
                compressionLevel, encryption, zip64, inputStream);
        zipEntry.setPassword(parameters.getPassword());
        zipEntry.setUtf8(parameters.isUtf8());

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
        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, uncompressedSize, compression,
                compressionLevel,
                encryption, zip64, inputStream);
        zipEntry.setComment(fileHeader.getComment());
        zipEntry.setUtf8(fileHeader.getGeneralPurposeFlag().isUtf8());
        return zipEntry;
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
