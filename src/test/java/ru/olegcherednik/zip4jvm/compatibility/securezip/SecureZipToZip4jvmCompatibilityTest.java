package ru.olegcherednik.zip4jvm.compatibility.securezip;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SplitZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipLzmaSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class SecureZipToZip4jvmCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(SecureZipToZip4jvmCompatibilityTest.class);

    public void shouldUnzipWhenLzmaSolid() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipLzmaSolidZip).destDir(destDir).extract();
        assertThatDirectory(destDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenBzip2Solid() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        for (Path zip : Arrays.asList(/*secureZipBzip2SolidZip,*/winRarDeflateSolidPkwareZip,  secureZipBzip2SolidPkwareZip)) {
            Path destDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(dir, zip);
            UnzipIt.zip(zip).destDir(destDir).password(password).extract();
            assertThatDirectory(destDir).matches(dirBikesAssert);
        }
    }

    public void shouldUnzipWhenBzip2Split() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipBzip2SplitZip).destDir(destDir).extract();
        assertThatDirectory(destDir).matches(rootAssert);
    }

}
