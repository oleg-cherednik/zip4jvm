package com.cop.zip4j;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.ZipParameters;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ModifyCommentTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ModifyCommentTest.class);
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
//    @Ignore("it's not working under gradle build")
    public void shouldCreateNewZipWithComment() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();

        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .comment("Oleg Cherednik - Олег Чередник").build();

        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.carsDir, parameters);

        assertThatDirectory(rootDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
        assertThatZipFile(zipFile).exists();
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
//    @Ignore("it's not working under gradle build")
    public void shouldAddCommentToExistedNoSplitZip() {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");

        misc.setComment("this is new comment - ноый комментарий");
        assertThat(misc.getComment()).isEqualTo("this is new comment - ноый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
//    @Ignore("it's not working under gradle build")
    public void shouldClearCommentForExistedZip() {
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNotBlank();

        misc.clearComment();
        assertThat(misc.getComment()).isNull();
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
//    @Ignore("it's not working under gradle build")
    public void shouldAddCommentToEncryptedZip() throws Zip4jException, IOException {
        Files.deleteIfExists(zipFile);
        Files.copy(Zip4jSuite.deflateSolidPkwareZip, zipFile);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
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
        assertThatZipFile(zipFile).hasCommentSize(EndCentralDirectory.MAX_COMMENT_LENGTH);
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
