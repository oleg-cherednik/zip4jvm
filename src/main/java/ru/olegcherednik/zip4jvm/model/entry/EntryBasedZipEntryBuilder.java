package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.AesVersionEnum;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamFunction;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class EntryBasedZipEntryBuilder {

    private final ZipFile.Entry entry;
    private final ZipEntrySettings entrySettings;

    public ZipEntry build() {
        ZipEntry zipEntry = createZipEntry();
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        return zipEntry;
    }

    private ZipEntry createZipEntry() {
        if (entry.isSymlink())
            return createSymlinkEntry();
        if (entry.isDir())
            return createEmptyDirectoryEntry();
        return createRegularFileEntry();
    }

    private ZipEntry createSymlinkEntry() {
        String symlinkName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
        ZipEntryInputStreamFunction inputStreamSup = (zipEntry, in) -> entry.getInputStream();

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    lastModifiedTime,
                                                    externalFileAttributes,
                                                    AesVersionEnum.AUTO.getVersion(),
                                                    CompressionMethod.STORE,
                                                    CompressionLevel.NORMAL,
                                                    EncryptionMethod.OFF,
                                                    inputStreamSup);

        zipEntry.setDataDescriptorAvailable(true);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());

        return zipEntry;
    }

    private ZipEntry createEmptyDirectoryEntry() {
        String dirName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();
        return new EmptyDirectoryZipEntry(dirName, lastModifiedTime, externalFileAttributes);
    }

    private ZipEntry createRegularFileEntry() {
        String fileName = ZipUtils.getFileName(entry);
        int lastModifiedTime = DosTimestampConverterUtils.javaToDosTime(entry.getLastModifiedTime());
        ExternalFileAttributes externalFileAttributes = entry.getExternalFileAttributes();

        CompressionMethod compressionMethod =
                entry.getUncompressedSize() == 0 ? CompressionMethod.STORE
                                                 : entrySettings.getCompression().getMethod();
        CompressionLevel compressionLevel = entrySettings.getCompressionLevel();
        EncryptionMethod encryptionMethod = entrySettings.getEncryption().getMethod();
        ZipEntryInputStreamFunction inputStreamFunction = (zipEntry, in) -> entry.getInputStream();
        boolean dataDescriptorAvailable =
                entrySettings.getDataDescriptor().isIncludeDataDescriptor(compressionMethod, encryptionMethod);
        AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

        RegularFileZipEntry zipEntry = new RegularFileZipEntry(fileName,
                                                               lastModifiedTime,
                                                               externalFileAttributes,
                                                               aesVersion,
                                                               compressionMethod,
                                                               compressionLevel,
                                                               encryptionMethod,
                                                               inputStreamFunction);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setZip64(entrySettings.isZip64());
        zipEntry.setPassword(entrySettings.getPassword());
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(entry.getUncompressedSize());

        return zipEntry;
    }

}
