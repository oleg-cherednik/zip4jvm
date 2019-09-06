package ru.olegcherednik.zip4jvm;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ModifyCommentTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ModifyCommentTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithComment() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.add(zip, Zip4jSuite.carsDir, settings);

        // TODO check comment using assertion
        ZipMisc misc = ZipMisc.builder().zipFile(zip).build();

        Zip4jAssertions.assertThatDirectory(rootDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
        Zip4jAssertions.assertThatZipFile(zip).exists();
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
//    @Ignore("it's not working under gradle build")
    public void shouldAddCommentToExistedNoSplitZip() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(zip).build();
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");

        misc.setComment("this is new comment - новый комментарий");
        assertThat(misc.getComment()).isEqualTo("this is new comment - новый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
//    @Ignore("it's not working under gradle build")
    public void shouldClearCommentForExistedZip() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(zip).build();
        assertThat(misc.getComment()).isNotBlank();

        misc.clearComment();
        assertThat(misc.getComment()).isNull();
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
//    @Ignore("it's not working under gradle build")
    public void shouldAddCommentToEncryptedZip() throws Zip4jException, IOException {
        Files.deleteIfExists(zip);
        Files.copy(Zip4jSuite.deflateSolidPkwareZip, zip);

        ZipMisc misc = ZipMisc.builder().zipFile(zip).build();
        assertThat(misc.isEncrypted()).isTrue();
        assertThat(misc.getComment()).isEqualTo("password: " + new String(Zip4jSuite.password));

        misc.setComment("Oleg Cherednik - Олег Чередник");
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
    }

    @Test
//    @Ignore("it's not working under gradle build")
    public void shouldSetCommentWithMaxLength() throws IOException {
        Path zipFile = rootDir.resolve("src_" + System.currentTimeMillis() + ".zip");
        Files.copy(Zip4jSuite.deflateSolidZip, zipFile);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNull();

        misc.setComment(StringUtils.repeat("_", EndCentralDirectory.MAX_COMMENT_LENGTH));
        Zip4jAssertions.assertThatZipFile(zipFile).hasCommentSize(EndCentralDirectory.MAX_COMMENT_LENGTH);
    }

    @Test
//    @Ignore("it's not working under gradle build")
    public void shouldThrowExceptionWhenCommentIsOverMaxLength() throws IOException {
        Path zipFile = Zip4jSuite.copy(rootDir, Zip4jSuite.deflateSolidZip);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNull();

        assertThatThrownBy(() -> misc.setComment(StringUtils.repeat("_", EndCentralDirectory.MAX_COMMENT_LENGTH + 1)))
                .isInstanceOf(Zip4jException.class);
    }

}
