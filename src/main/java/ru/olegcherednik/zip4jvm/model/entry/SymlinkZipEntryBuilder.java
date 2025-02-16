package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamFunction;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 16.02.2025
 */
@Builder
class SymlinkZipEntryBuilder {

    private final Path symlinkTarget;
    private final String symlinkTargetRelativePath;
    private final String symlinkName;
    private final ZipEntrySettings entrySettings;

    public ZipEntry build() {
        int dosLastModifiedTime = DosTimestampConverterUtils.javaToDosTime(System.currentTimeMillis());
        byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);
        ZipEntryInputStreamFunction inputStreamFunction = (zipEntry, in) -> new ByteArrayInputStream(buf);
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.symlink(symlinkTarget);
        CompressionMethod compressionMethod = CompressionMethod.STORE;
        EncryptionMethod encryptionMethod = EncryptionMethod.OFF;
        DataDescriptorEnum dataDescriptorAvailability = entrySettings.getDataDescriptor();
        boolean dataDescriptorAvailable = dataDescriptorAvailability == DataDescriptorEnum.AUTO
                || dataDescriptorAvailability.isIncludeDataDescriptor(compressionMethod, encryptionMethod);
        AesVersion aesVersion = entrySettings.getAesVersion().getVersion();

        ZipEntry zipEntry = new RegularFileZipEntry(symlinkName,
                                                    dosLastModifiedTime,
                                                    externalFileAttributes,
                                                    aesVersion,
                                                    compressionMethod,
                                                    CompressionLevel.NORMAL,
                                                    encryptionMethod,
                                                    inputStreamFunction);

        zipEntry.setDataDescriptorAvailable(dataDescriptorAvailable);
        zipEntry.setComment(entrySettings.getComment());
        zipEntry.setUtf8(entrySettings.isUtf8());
        zipEntry.setUncompressedSize(buf.length);

        return zipEntry;
    }

}
