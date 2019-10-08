package ru.olegcherednik.zip4jvm.model.entry;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class ZipEntryTest {

    public void shouldRetrieveFileNameWhenToString() throws IOException {
        ZipEntry file = ZipEntryBuilder.build(ZipFile.Entry.of(fileBentley, fileNameBentley), ZipEntrySettings.DEFAULT);
        ZipEntry dir = ZipEntryBuilder.build(ZipFile.Entry.of(dirBikes, dirNameBikes), ZipEntrySettings.DEFAULT);

        assertThat(file.toString()).isEqualTo(fileNameBentley);
        assertThat(dir.toString()).isEqualTo(zipDirNameBikes);
    }
}
