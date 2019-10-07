package ru.olegcherednik.zip4jvm.model;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.WIN;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class FileHeaderTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(FileHeaderTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUseSettersGettersCorrectly() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        InternalFileAttributes internalFileAttributes = InternalFileAttributes.build(new byte[] { 1, 2 });
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN).readFrom(fileBentley);
        ExtraField extraField = ExtraField.builder().addRecord(Zip64.ExtendedInfo.builder().uncompressedSize(4).build()).build();

        assertThat(internalFileAttributes).isNotSameAs(InternalFileAttributes.NULL);
        assertThat(externalFileAttributes).isNotSameAs(ExternalFileAttributes.NULL);
        assertThat(extraField).isNotSameAs(ExtraField.NULL);

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        fileHeader.setVersionMadeBy(1);
        fileHeader.setVersionToExtract(2);
        fileHeader.setGeneralPurposeFlag(generalPurposeFlag);
        fileHeader.setCompressionMethod(CompressionMethod.AES);
        fileHeader.setLastModifiedTime(3);
        fileHeader.setCrc32(4);
        fileHeader.setCompressedSize(5);
        fileHeader.setUncompressedSize(6);
        fileHeader.setCommentLength(7);
        fileHeader.setDisk(8);
        fileHeader.setInternalFileAttributes(internalFileAttributes);
        fileHeader.setExternalFileAttributes(externalFileAttributes);
        fileHeader.setLocalFileHeaderOffs(9);
        fileHeader.setFileName("fileName");
        fileHeader.setExtraField(extraField);

        assertThat(fileHeader.getVersionMadeBy()).isEqualTo(1);
        assertThat(fileHeader.getVersionToExtract()).isEqualTo(2);
        assertThat(fileHeader.getGeneralPurposeFlag()).isSameAs(generalPurposeFlag);
        assertThat(fileHeader.getCompressionMethod()).isSameAs(CompressionMethod.AES);
        assertThat(fileHeader.getLastModifiedTime()).isEqualTo(3);
        assertThat(fileHeader.getCrc32()).isEqualTo(4);
        assertThat(fileHeader.getCompressedSize()).isEqualTo(5);
        assertThat(fileHeader.getUncompressedSize()).isEqualTo(6);
        assertThat(fileHeader.getCommentLength()).isEqualTo(7);
        assertThat(fileHeader.getDisk()).isEqualTo(8);
        assertThat(fileHeader.getInternalFileAttributes()).isSameAs(internalFileAttributes);
        assertThat(fileHeader.getExternalFileAttributes()).isSameAs(externalFileAttributes);
        assertThat(fileHeader.getLocalFileHeaderOffs()).isEqualTo(9);
        assertThat(fileHeader.getExtraField().getExtendedInfo()).isNotNull();
        assertThat(fileHeader.getFileName()).isEqualTo("fileName");
    }

    public void shouldRetrieveNotNullFileName() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getFileName()).isNull();
        assertThat(fileHeader.getFileName(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setFileName("zip4jvm");
        assertThat(fileHeader.getFileName(Charsets.UTF_8)).isEqualTo("zip4jvm".getBytes(Charsets.UTF_8));
    }

    public void shouldRetrieveNotNullComment() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getComment()).isNull();
        assertThat(fileHeader.getComment(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setComment("zip4jvm");
        assertThat(fileHeader.getComment(Charsets.UTF_8)).isEqualTo("zip4jvm".getBytes(Charsets.UTF_8));
    }

    public void shouldRetrieveFileNameWhenToString() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.toString()).isNull();

        fileHeader.setFileName("zip4jvm");
        assertThat(fileHeader.toString()).isEqualTo("zip4jvm");
    }

    public void shouldRetrieveIsZip64TrueWhenZip64ExtendedInfoIsNotNull() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getExtraField().getExtendedInfo()).isSameAs(Zip64.ExtendedInfo.NULL);
        assertThat(fileHeader.isZip64()).isFalse();

        Zip64.ExtendedInfo extendedInfo = Zip64.ExtendedInfo.builder().uncompressedSize(1).compressedSize(2).localFileHeaderOffs(3)
                                                            .disk(4).build();

        fileHeader.setExtraField(ExtraField.builder().addRecord(extendedInfo).build());
        assertThat(fileHeader.isZip64()).isTrue();
    }

    public void shouldWriteZip64WhenLocalFileHeaderOffsIsOverLimit() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setLocalFileHeaderOffs(Zip64.LIMIT_DWORD);
        assertThat(fileHeader.isWriteZip64OffsetLocalHeader()).isFalse();

        fileHeader.setLocalFileHeaderOffs(Zip64.LIMIT_DWORD + 1);
        assertThat(fileHeader.isWriteZip64OffsetLocalHeader()).isTrue();
    }

}
