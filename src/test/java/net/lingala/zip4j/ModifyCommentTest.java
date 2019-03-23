package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ModifyCommentTest {

    private static final Path root = Zip4jSuite.root.resolve(ModifyCommentTest.class.getSimpleName());

    private final Path zipFile = root.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(root);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(root);
    }

    @Test
    public void shouldCreateNewZipWithComment() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir.resolve("cars"), parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
        TestUtils.checkDirectory(root, 0, 1);
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

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
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
