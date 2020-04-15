package ru.olegcherednik.zip4jvm.compatibility.securezip;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipBzip2SolidZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipLzmaSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSplitZip;
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
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipBzip2SolidZip).destDir(destDir).password(password).extract();
        assertThatDirectory(destDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenPkwareEncryption() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipBzip2SolidPkwareZip).destDir(destDir).password(password).extract();
        assertThatDirectory(destDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenAesEncryption() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipStoreSolidAesZip).destDir(destDir).password(password).extract();
        assertThatDirectory(destDir).matches(dirBikesAssert);
    }

    public void shouldUnzipWhenSplit() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.zip(secureZipStoreSplitZip).destDir(destDir).extract();
        assertThatDirectory(destDir).matches(rootAssert);
    }

}
