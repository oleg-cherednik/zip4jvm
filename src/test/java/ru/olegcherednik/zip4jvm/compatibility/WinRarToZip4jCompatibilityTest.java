package ru.olegcherednik.zip4jvm.compatibility;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarDeflateSolidAesZip;
import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarDeflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarDeflateSolidZip;
import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarStoreSolidAesZip;
import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarStoreSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.Zip4jSuite.winRarStoreSolidZip;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention", "LocalVariableNamingConvention" })
public class WinRarToZip4jCompatibilityTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(WinRarToZip4jCompatibilityTest.class);

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
        Path parentDir = Zip4jSuite.subDirNameAsMethodName(rootDir);

        for (Path zip4jFile : Arrays.asList(winRarStoreSolidZip, winRarStoreSolidPkwareZip, winRarStoreSolidAesZip, winRarDeflateSolidZip,
                winRarDeflateSolidPkwareZip, winRarDeflateSolidAesZip)) {
            Path dstDir = Zip4jSuite.subDirNameAsRelativePathToRoot(parentDir, zip4jFile);
            ZipFile.Reader zipFile = ZipFile.read(zip4jFile, fileName -> Zip4jSuite.password);
            zipFile.extract(dstDir);
            assertThatDirectory(dstDir).matches(TestUtils.starWarsDirAssert);
        }
    }


}
