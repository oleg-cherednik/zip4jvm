package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Version;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 08.11.2019
 */
@Test
public class VersionViewTest {

    public void shouldRetrieveVersionMadeByOnlyAndVersionToExtractWhenBothVersionsSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(VersionView.builder()
                                                         .versionMadeBy(Version.of(0x12))
                                                         .versionToExtract(Version.of(0x134))
                                                         .columnWidth(52).build());

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo("version made by operating system (00):              MS-DOS, OS/2, NT FAT");
        assertThat(lines[1]).isEqualTo("version made by zip software (18):                  1.8");
        assertThat(lines[2]).isEqualTo("operat. system version needed to extract (01):      Amiga");
        assertThat(lines[3]).isEqualTo("unzip software version needed to extract (52):      5.2");
    }

    public void shouldRetrieveVersionMadeByOnlyWhenOnlyItsSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(VersionView.builder()
                                                         .versionMadeBy(Version.of(0x12))
                                                         .columnWidth(52).build());

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("version made by operating system (00):              MS-DOS, OS/2, NT FAT");
        assertThat(lines[1]).isEqualTo("version made by zip software (18):                  1.8");
    }

    public void shouldRetrieveVersionToExtractOnlyWhenOnlyItsSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(VersionView.builder()
                                                         .versionToExtract(Version.of(0x134))
                                                         .columnWidth(52).build());

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("operat. system version needed to extract (01):      Amiga");
        assertThat(lines[1]).isEqualTo("unzip software version needed to extract (52):      5.2");
    }

    public void shouldRetrieveVersionToExtractOnlyWhenOnlyItsSet1() throws IOException {
        String[] lines = Zip4jvmSuite.execute(VersionView.builder().columnWidth(52).build());

        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEmpty();
    }
}
