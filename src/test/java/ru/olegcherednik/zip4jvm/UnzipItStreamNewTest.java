package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.Zip64;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Test(dataProvider = "zips")
    public void foo(Path zip) throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        // Path zip = Paths.get("d:/zip4jvm/span/foo.zip");
        UnzipIt.zip(zip).destDir(destDir).extract();
        assertThatZipFile(zipStoreSolid).exists().root().matches(rootAssert);
    }

    @DataProvider(name = "zips")
    public static Object[][] zips() {
        return new Object[][] {
                { zipStoreSolid } };


    }

}
