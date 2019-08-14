package com.cop.zip4j.compatibility;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.UnzipIt;
import com.cop.zip4j.Zip4jSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.cop.zip4j.Zip4jSuite.winRarDeflateSolidAesZip;
import static com.cop.zip4j.Zip4jSuite.winRarDeflateSolidPkwareZip;
import static com.cop.zip4j.Zip4jSuite.winRarDeflateSolidZip;
import static com.cop.zip4j.Zip4jSuite.winRarStoreSolidAesZip;
import static com.cop.zip4j.Zip4jSuite.winRarStoreSolidPkwareZip;
import static com.cop.zip4j.Zip4jSuite.winRarStoreSolidZip;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;

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
            Files.createDirectories(dstDir);

            UnzipIt unzip = UnzipIt.builder()
                                   .zipFile(zip4jFile)
                                   .password(Zip4jSuite.password).build();

            unzip.extract(dstDir);

            assertThatDirectory(dstDir).matches(TestUtils.starWarsDirAssert);
        }
    }


}
