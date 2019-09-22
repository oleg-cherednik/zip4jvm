package ru.olegcherednik.zip4jvm.compatibility;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidZip;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention", "LocalVariableNamingConvention" })
public class WinRarToZip4jvmCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(WinRarToZip4jvmCompatibilityTest.class);

//    public void zip4jShouldBeReadableForZipInputStream() throws IOException {
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        byte[] buf = new byte[1024];
//
//        try (ZipInputStream in = new ZipInputStream(new FileInputStream(Zip4jSuite.noSplitZip.toFile()))) {
//            ZipEntry entry;
//
//            while ((entry = in.getNextEntry()) != null) {
//                Path path = dstDir.resolve(entry.getName());
//
//                if (entry.isDirectory())
//                    Files.createDirectories(path);
//                else {
//                    //noinspection NestedTryStatement
//                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
//                        int len;
//
//                        while ((len = in.read(buf)) > 0) {
//                            out.write(buf, 0, len);
//                        }
//                    }
//                }
//
//                in.close();
//            }
//        }
//
//        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
//    }

    public void checkCompatibilityWithWinRar() throws IOException {
        Path parentDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        for (Path zip4jFile : Arrays.asList(winRarStoreSolidZip, winRarStoreSolidPkwareZip, winRarStoreSolidAesZip, winRarDeflateSolidZip,
                winRarDeflateSolidPkwareZip, winRarDeflateSolidAesZip)) {
            Path destDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(parentDir, zip4jFile);
            UnzipIt.extract(zip4jFile, destDir, fileName -> Zip4jvmSuite.password);
//            assertThatDirectory(destDir).matches(TestDataAssert.starWarsDirAssert);
        }
    }


}
