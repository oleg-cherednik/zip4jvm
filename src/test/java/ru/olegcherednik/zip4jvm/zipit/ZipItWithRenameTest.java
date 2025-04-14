package ru.olegcherednik.zip4jvm.zipit;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.04.2025
 */
@Test
public class ZipItWithRenameTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipItWithRenameTest.class);

    public void shouldAddFileAndRenameToName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).addWithRename(fileBentley, "foo.jpg");
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).addWithRename(fileBentley, "sub/foo.jpg");
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("sub").exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("sub/foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameWithDot() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).addWithRename(fileBentley, "sub/..foo.jpg");
        assertThatZipFile(zip).root().hasOnlyDirectories(1);
        assertThatZipFile(zip).directory("sub").exists().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("sub/..foo.jpg").exists().matches(fileBentleyAssert);
    }

    public void shouldAddFileAndRenameToDirAndNameSimilarWithDirName() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).addWithRename(fileBentley, "dir_name");
        assertThatZipFile(zip).root().hasOnlyRegularFiles(1);
        assertThatZipFile(zip).regularFile("dir_name").exists().matches(fileBentleyAssert);
    }

    public void shouldThrowIllegalArgumentExceptionWhenRenameToRelativeDir() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.zip(zip).addWithRename(fileBentley, "../foo.jpg"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
