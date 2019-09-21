package ru.olegcherednik.zip4jvm.model.entry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
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
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamSupplier;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 04.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntryBuilder {

    public static ZipEntry create(@NonNull ZipFile.Entry entry, @NonNull ZipEntrySettings entrySettings) {
        try {
            return entry.isRegularFile() ? createRegularFileEntry(entry, entrySettings) : createDirectoryEntry(entry, entrySettings);
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static ZipEntry createDirectoryEntry(ZipFile.Entry entry, ZipEntrySettings entrySettings) throws IOException {
        String fileName = ZipUtils.getFileName(entry.getFileName(), false);
        int lastModifiedTime = ZipUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();

        DirectoryZipEntry zipEntry = new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());

        return zipEntry;
    }

    private static ZipEntry createRegularFileEntry(ZipFile.Entry entry, ZipEntrySettings entrySettings) throws IOException {
        String fileName = ZipUtils.getFileName(entry.getFileName(), true);
        int lastModifiedTime = ZipUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();

        Compression compression = entrySettings.getCompression();
        CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
        Encryption encryption = entrySettings.getEncryption();

        ZipEntryInputStreamSupplier inputStreamSup = zipEntry -> entry.getInputStreamSup().get();

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel,
                encryption, inputStreamSup);

        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());

        if (zipEntry.isEncrypted() && ArrayUtils.isEmpty(zipEntry.getPassword()))
            throw new EmptyPasswordException();

        return zipEntry;
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

        ZipEntryInputStreamSupplier inputStreamSup = entry -> {
            DataInput in = zipModel.isSplit() ? SplitZipInputStream.create(zipModel, entry.getDisk()) : SingleZipInputStream.create(zipModel);
            return EntryInputStream.create(entry, in);
        };

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName, lastModifiedTime, externalFileAttributes, compression, compressionLevel,
                encryption, inputStreamSup);
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
