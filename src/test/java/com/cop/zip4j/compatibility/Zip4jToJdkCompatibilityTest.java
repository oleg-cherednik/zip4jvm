package com.cop.zip4j.compatibility;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.Zip4jSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.cop.zip4j.Zip4jSuite.deflateSolidZip;
import static com.cop.zip4j.Zip4jSuite.storeSolidZip;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention", "LocalVariableNamingConvention" })
public class Zip4jToJdkCompatibilityTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(Zip4jToJdkCompatibilityTest.class);

    public void checkCompatibilityWithJdk() throws IOException {
        Path parentDir = Zip4jSuite.subDirNameAsMethodName(rootDir);

        for (Path zip4jFile : Arrays.asList(storeSolidZip, deflateSolidZip)) {
            Path dstDir = Zip4jSuite.subDirNameAsRelativePathToRoot(parentDir, zip4jFile);

            try (ZipFile zipFile = new ZipFile(zip4jFile.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    Path path = dstDir.resolve(entry.getName());

                    if (entry.isDirectory())
                        Files.createDirectories(path);
                    else
                        TestUtils.copyLarge(zipFile.getInputStream(entry), path);
                }
            }

            assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
        }

    }

}
