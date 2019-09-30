package ru.olegcherednik.zip4jvm;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ModifyCommentTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ModifyCommentTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithComment() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.add(zip, fileOlegCherednik, settings);
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
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .build();
        ZipIt.add(zip, Collections.emptyList(), settings);
        assertThatZipFile(zip, Zip4jvmSuite.password).hasCommentSize(0);

        ZipMisc.setComment(zip, "this is new comment");
        assertThatZipFile(zip, Zip4jvmSuite.password).hasComment("this is new comment");
    }

    public void shouldSetCommentWithMaxLength() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipDeflateSolid, zip);

        ZipMisc.setComment(zip, StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE));
        assertThatZipFile(zip).hasCommentSize(ZipModel.MAX_COMMENT_SIZE);
    }

    public void shouldThrowExceptionWhenCommentIsOverMaxLength() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipDeflateSolid, zip);

        assertThatThrownBy(() -> ZipMisc.setComment(zip, StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
