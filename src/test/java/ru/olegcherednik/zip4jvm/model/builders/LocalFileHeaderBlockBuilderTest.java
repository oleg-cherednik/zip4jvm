package ru.olegcherednik.zip4jvm.model.builders;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 29.09.2019
 */
@Test
public class LocalFileHeaderBlockBuilderTest {

    public void shouldCreateLocalFileHeaderWhenZip64Entry() throws IOException {
        ZipFile.Entry entry = ZipFile.Entry.of(fileDucati, fileNameDucati);
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().zip64(true).utf8(true).build();
        ZipEntry zipEntry = ZipEntryBuilder.build(entry, entrySettings);

        zipEntry.setDataDescriptorAvailable(() -> false);

        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
        assertThat(localFileHeader).isNotNull();

        assertThat(localFileHeader.getCompressedSize()).isEqualTo(LOOK_IN_EXTRA_FIELD);
        assertThat(localFileHeader.getUncompressedSize()).isEqualTo(LOOK_IN_EXTRA_FIELD);

        Zip64.ExtendedInfo extendedInfo = localFileHeader.getExtraField().getExtendedInfo();
        assertThat(extendedInfo).isNotSameAs(Zip64.ExtendedInfo.NULL);
        assertThat(extendedInfo.getUncompressedSize()).isEqualTo(0);
        assertThat(extendedInfo.getCompressedSize()).isEqualTo(0);
    }
}
