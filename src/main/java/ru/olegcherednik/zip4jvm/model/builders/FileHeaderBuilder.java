package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class FileHeaderBuilder {

    private final ZipEntry entry;

    public CentralDirectory.FileHeader create() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        fileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        fileHeader.setGeneralPurposeFlag(createGeneralPurposeFlag());
        fileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod().apply(entry.getCompression()));
        fileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        fileHeader.setCrc32(entry.getEncryption().getChecksum().apply(entry));
        fileHeader.setCompressedSize(getSize(entry.getCompressedSize()));
        fileHeader.setUncompressedSize(getSize(entry.getUncompressedSize()));
        fileHeader.setCommentLength(0);
        fileHeader.setDisk(getDisk());
        fileHeader.setInternalFileAttributes(entry.getInternalFileAttributes());
        fileHeader.setExternalFileAttributes(entry.getExternalFileAttributes());
        fileHeader.setOffsLocalFileHeader(entry.getLocalFileHeaderOffs());
        fileHeader.setFileName(entry.getFileName());
        fileHeader.setExtraField(createExtraField());
        fileHeader.setComment(entry.getComment());

        return fileHeader;
    }

    private GeneralPurposeFlag createGeneralPurposeFlag() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorAvailable(entry.isDataDescriptorAvailable());
        generalPurposeFlag.setUtf8(entry.isUtf8());
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);

        return generalPurposeFlag;
    }

    private ExtraField createExtraField() {
        return ExtraField.builder()
                         .addRecord(createExtendedInfo())
                         .addRecord(new AesExtraDataRecordBuilder(entry).create()).build();
    }

    private Zip64.ExtendedInfo createExtendedInfo() {
        if (entry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(entry.getCompressedSize())
                                     .uncompressedSize(entry.getUncompressedSize())
                                     .disk(entry.getDisk())
//                                     .offsLocalHeaderRelative(entry.getLocalFileHeaderOffs())
                                     .build();
        return Zip64.ExtendedInfo.NULL;
    }

    private int getDisk() {
        return entry.isZip64() ? ZipModel.MAX_TOTAL_DISKS : (int)entry.getDisk();
    }

    private long getSize(long size) {
        return entry.isZip64() ? LOOK_IN_EXTRA_FIELD : size;
    }

}
