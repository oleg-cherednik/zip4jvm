package ru.olegcherednik.zip4jvm.model;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jSuite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class FileHeaderTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(FileHeaderTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldUseSettersCorrectly() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        InternalFileAttributes internalFileAttributes = InternalFileAttributes.createDataBasedDelegate(new byte[] { 1, 2 });
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.createOperationBasedDelegate(Zip4jSuite.fileBentleyContinental);

        assertThat(internalFileAttributes).isNotSameAs(InternalFileAttributes.NULL);
        assertThat(externalFileAttributes).isNotSameAs(ExternalFileAttributes.NULL);

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
        fileHeader.setOffsLocalFileHeader(9);
        fileHeader.setFileName("fileName");
//        fileHeader.setExtraField(extraField);

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
        assertThat(fileHeader.getOffsLocalFileHeader()).isEqualTo(9);
        assertThat(fileHeader.getFileName()).isEqualTo("fileName");
    }

    public void shouldRetrieveNotNullFileName() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getFileName()).isNull();
        assertThat(fileHeader.getFileName(StandardCharsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setFileName("zip4jvm");
        assertThat(fileHeader.getFileName(StandardCharsets.UTF_8)).isEqualTo("zip4jvm".getBytes(StandardCharsets.UTF_8));
    }

    public void shouldRetrieveNotNullComment() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getComment()).isNull();
        assertThat(fileHeader.getComment(StandardCharsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setComment("zip4jvm");
        assertThat(fileHeader.getComment(StandardCharsets.UTF_8)).isEqualTo("zip4jvm".getBytes(StandardCharsets.UTF_8));
    }

    public void shouldRetrieveFileNameWhenToString() {
        assertThat(new CentralDirectory.FileHeader().toString()).isNull();
        assertThat(new CentralDirectory.FileHeader("zip4jvm").toString()).isEqualTo("zip4jvm");
    }

    public void shouldRetrieveTrueWhenIsDirectoryForFileNameEndsWithDirectoryMarker() {
        assertThat(new CentralDirectory.FileHeader().isDirectory()).isFalse();
        assertThat(new CentralDirectory.FileHeader("zip4jvm").isDirectory()).isFalse();
        assertThat(new CentralDirectory.FileHeader("zip4jvm/").isDirectory()).isTrue();
        assertThat(new CentralDirectory.FileHeader("zip4jvm\\").isDirectory()).isTrue();
    }

    public void shouldThrowNullPointerExceptionWhenNull() {
        assertThatThrownBy(() -> new CentralDirectory.FileHeader().getFileName(null)).isExactlyInstanceOf(NullPointerException.class);
    }

}