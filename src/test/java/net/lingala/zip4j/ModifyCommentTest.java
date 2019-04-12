package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatZipFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ModifyCommentTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ModifyCommentTest.class.getSimpleName());
    private static final Path zipFile = rootDir.resolve("src.zip");

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
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .comment("Oleg Cherednik - Олег Чередник").build();

        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.carsDir, parameters);

        assertThatDirectory(rootDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
        assertThatZipFile(zipFile).exists();
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
    public void shouldAddCommentToExistedNoSplitZip() {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");

        misc.setComment("this is new comment - ноый комментарий");
        assertThat(misc.getComment()).isEqualTo("this is new comment - ноый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNotBlank();

        misc.clearComment();
        assertThat(misc.getComment()).isNull();
    }

    @Test
    public void shouldSetCommentWithMaxLength() throws IOException {
        Path zipFile = rootDir.resolve("src_" + System.currentTimeMillis() + ".zip");
        Files.copy(Zip4jSuite.noSplitZip, zipFile);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNull();

        misc.setComment(StringUtils.repeat("_", 25191));
        assertThat(misc.getComment()).hasSize(EndCentralDirectory.MAX_COMMENT_LENGTH);
    }

    @Test(dependsOnMethods = "shouldAddCommentWithMaxLength")
    public void shouldAddCommentToEncryptedZip() throws ZipException, IOException {
        Files.deleteIfExists(zipFile);
        Files.copy(Zip4jSuite.noSplitAesZip, zipFile);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isEncrypted()).isTrue();
        assertThat(misc.getComment()).isNull();

        misc.setComment("Oleg Cherednik - Олег Чередник");
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
    }

}
