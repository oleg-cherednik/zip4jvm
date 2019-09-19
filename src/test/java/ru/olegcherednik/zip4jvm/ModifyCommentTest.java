package ru.olegcherednik.zip4jvm;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
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

    public void shouldCreateNewZipWithComment() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.add(zip, Zip4jSuite.fileSrcOlegCherednik, settings);
        assertThatZipFile(zip).exists().hasComment("Oleg Cherednik - Олег Чередник");
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
    public void shouldAddCommentToExistedNoSplitZip() throws IOException {
        ZipMisc.setComment(zip, "this is new comment - новый комментарий");
        assertThatZipFile(zip).exists().hasComment("this is new comment - новый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() throws IOException {
        ZipMisc.setComment(zip, null);
        assertThatZipFile(zip).exists().hasCommentSize(0);
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
    public void shouldAddCommentToEncryptedZip() throws IOException {
        Files.deleteIfExists(zip);

        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jSuite.password).build())
                                                  .build();
        ZipIt.add(zip, Collections.emptyList(), settings);
        assertThatZipFile(zip, Zip4jSuite.password).hasCommentSize(0);

        ZipMisc.setComment(zip, "this is new comment");
        assertThatZipFile(zip, Zip4jSuite.password).hasComment("this is new comment");
    }

    public void shouldSetCommentWithMaxLength() throws IOException {
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(Zip4jSuite.deflateSolidZip, zip);

        ZipMisc.setComment(zip, StringUtils.repeat("_", EndCentralDirectory.MAX_COMMENT_LENGTH));
        assertThatZipFile(zip).hasCommentSize(EndCentralDirectory.MAX_COMMENT_LENGTH);
    }

    @Test
    public void shouldThrowExceptionWhenCommentIsOverMaxLength() throws IOException {
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(Zip4jSuite.deflateSolidZip, zip);

        assertThatThrownBy(() -> ZipMisc.setComment(zip, StringUtils.repeat("_", EndCentralDirectory.MAX_COMMENT_LENGTH + 1)))
                .isInstanceOf(Zip4jException.class);
    }

}
