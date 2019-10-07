package ru.olegcherednik.zip4jvm.model;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class LocalFileHeaderTest {

    public void shouldUseSettersGettersCorrectly() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        ExtraField extraField = ExtraField.builder().addRecord(Zip64.ExtendedInfo.builder().uncompressedSize(4).build()).build();

        assertThat(extraField).isNotSameAs(ExtraField.NULL);

        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionToExtract(2);
        localFileHeader.setGeneralPurposeFlag(generalPurposeFlag);
        localFileHeader.setCompressionMethod(CompressionMethod.AES);
        localFileHeader.setLastModifiedTime(3);
        localFileHeader.setCrc32(4);
        localFileHeader.setCompressedSize(5);
        localFileHeader.setUncompressedSize(6);
        localFileHeader.setFileName("fileName");
        localFileHeader.setExtraField(extraField);

        assertThat(localFileHeader.getVersionToExtract()).isEqualTo(2);
        assertThat(localFileHeader.getGeneralPurposeFlag()).isSameAs(generalPurposeFlag);
        assertThat(localFileHeader.getCompressionMethod()).isSameAs(CompressionMethod.AES);
        assertThat(localFileHeader.getLastModifiedTime()).isEqualTo(3);
        assertThat(localFileHeader.getCrc32()).isEqualTo(4);
        assertThat(localFileHeader.getCompressedSize()).isEqualTo(5);
        assertThat(localFileHeader.getUncompressedSize()).isEqualTo(6);
        assertThat(localFileHeader.getExtraField().getExtendedInfo()).isNotNull();
        assertThat(localFileHeader.getFileName()).isEqualTo("fileName");
    }

    public void shouldRetrieveFileNameWhenToString() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        assertThat(localFileHeader.toString()).isNull();

        localFileHeader.setFileName("zip4jvm");
        assertThat(localFileHeader.toString()).isEqualTo("zip4jvm");
    }

    public void shouldRetrieveNotNullFileName() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        assertThat(localFileHeader.getFileName()).isNull();
        assertThat(localFileHeader.getFileName(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        localFileHeader.setFileName("zip4jvm");
        assertThat(localFileHeader.getFileName(Charsets.UTF_8)).isEqualTo("zip4jvm".getBytes(Charsets.UTF_8));
    }

}
