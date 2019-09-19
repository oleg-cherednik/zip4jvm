package ru.olegcherednik.zip4jvm.model.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jEmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.EntryInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.v2.ZipEntryMeta;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
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

    public static ZipEntry create(@NonNull Path path, @NonNull String fileName, @NonNull ZipEntrySettings entrySettings) {
        try {
            if (Files.isDirectory(path))
                return createDirectoryEntry(path, fileName, entrySettings);
            return createRegularFileEntry(path, fileName, entrySettings);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private static ZipEntry createDirectoryEntry(Path dir, String fileName, ZipEntrySettings entrySettings) throws IOException {
        fileName = ZipUtils.normalizeFileName(FilenameUtils.concat(entrySettings.getBasePath(), fileName) + '/');
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(dir).toMillis());
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(dir);

        DirectoryZipEntry entry = new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
        entry.setPassword(entrySettings.getPassword().apply(fileName));
        entry.setComment(entrySettings.getComment().apply(fileName));
        entry.setUtf8(entrySettings.isUtf8());

        if (entry.isEncrypted() && ArrayUtils.isEmpty(entry.getPassword()))
            throw new Zip4jEmptyPasswordException();

        return entry;
    }

    public static ZipEntry createRegularFileEntry(Path file, String fileName, ZipEntrySettings entrySettings) throws IOException {
        fileName = ZipUtils.normalizeFileName(FilenameUtils.concat(entrySettings.getBasePath(), fileName));
        int lastModifiedTime = ZipUtils.javaToDosTime(Files.getLastModifiedTime(file).toMillis());
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(file);

        Compression compression = entrySettings.getCompression();
        CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
        Encryption encryption = entrySettings.getEncryption();

        IOSupplier<InputStream> inputStream = entry -> new FileInputStream(file.toFile());

        RegularFileZipEntry entry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel,
                encryption, inputStream);
        entry.setZip64(entrySettings.isZip64());
        entry.setPassword(entrySettings.getPassword().apply(fileName));
        entry.setComment(entrySettings.getComment().apply(fileName));
        entry.setUtf8(entrySettings.isUtf8());

        if (entry.isEncrypted() && ArrayUtils.isEmpty(entry.getPassword()))
            throw new Zip4jEmptyPasswordException();

        return entry;
    }

    public static ZipEntry createRegularFileEntry(ZipEntryMeta src, ZipEntrySettings entrySettings) throws IOException {
        String fileName = ZipUtils.normalizeFileName(FilenameUtils.concat(entrySettings.getBasePath(), src.getFileName()));
        int lastModifiedTime = ZipUtils.javaToDosTime(src.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = src.getExternalFileAttributes();

        Compression compression = entrySettings.getCompression();
        CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
        Encryption encryption = entrySettings.getEncryption();

        IOSupplier<InputStream> inputStream = entry -> src.getInputStream().get();

        RegularFileZipEntry entry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel,
                encryption, inputStream);

        entry.setZip64(entrySettings.isZip64());
        entry.setPassword(entrySettings.getPassword().apply(fileName));
        entry.setComment(entrySettings.getComment().apply(fileName));
        entry.setUtf8(entrySettings.isUtf8());

        if (entry.isEncrypted() && ArrayUtils.isEmpty(entry.getPassword()))
            throw new Zip4jEmptyPasswordException();

        return entry;
    }

    public static ZipEntry create(@NonNull CentralDirectory.FileHeader fileHeader, @NonNull ZipModel zipModel) {
        boolean dir = ZipUtils.isDirectory(fileHeader.getFileName());
        ZipEntry zipEntry = dir ? createDirectoryEntry(fileHeader) : createRegularFileEntry(fileHeader, zipModel);
        zipEntry.setChecksum(fileHeader.getCrc32());
        zipEntry.setUncompressedSize(getUncompressedSize(fileHeader));
        zipEntry.setCompressedSize(getCompressedSize(fileHeader));
        zipEntry.setDisk(getDisk(fileHeader));
        zipEntry.setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());
        return zipEntry;
    }

    private static ZipEntry createDirectoryEntry(CentralDirectory.FileHeader fileHeader) {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        return new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
    }

    private static ZipEntry createRegularFileEntry(CentralDirectory.FileHeader fileHeader, ZipModel zipModel) {
        String fileName = ZipUtils.normalizeFileName(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        Compression compression = fileHeader.getCompression();
        CompressionLevel compressionLevel = fileHeader.getGeneralPurposeFlag().getCompressionLevel();
        Encryption encryption = fileHeader.getEncryption();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();
        IOSupplier<InputStream> inputStream = entry -> {
            DataInput in = zipModel.isSplit() ? SplitZipInputStream.create(zipModel, entry.getDisk()) : SingleZipInputStream.create(zipModel);
            return EntryInputStream.create(entry, in);
        };
        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel,
                encryption, inputStream);
        zipEntry.setZip64(fileHeader.isZip64());
        zipEntry.setComment(fileHeader.getComment());
        zipEntry.setUtf8(fileHeader.getGeneralPurposeFlag().isUtf8());
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
