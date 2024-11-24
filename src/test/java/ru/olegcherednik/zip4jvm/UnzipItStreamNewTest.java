package ru.olegcherednik.zip4jvm;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItStreamNewTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirName(UnzipItStreamNewTest.class);

    public void foo() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        UnzipIt.zip(zipStoreSolid).destDir(destDir).extract();
        assertThatZipFile(zipStoreSolid).exists().root().matches(rootAssert);
    }

}
