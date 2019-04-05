package net.lingala.zip4j;

import org.testng.annotations.Test;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 05.04.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompatibilityTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(CompatibilityTest.class.getSimpleName());

    public void zip4jShouldBeValidForZipFile() throws IOException {
        Path dest = rootDir.resolve("zipFile");

        try (ZipFile zipFile = new ZipFile(Zip4jSuite.noSplitZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = dest.resolve(entry.getName());

                if (entry.isDirectory())
                    Files.createDirectories(path);
                else
                    TestUtils.copyLarge(zipFile.getInputStream(entry), path);
            }
        }

        assertThatDirectory(dest).matches(TestUtils.dirAssert);
    }

    public void zip4jShouldBeValidForZipInputStream() throws IOException {
        Path dest = rootDir.resolve("zipInputStream");
        byte[] buf = new byte[1024];

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(Zip4jSuite.noSplitZip.toFile()))) {
            ZipEntry entry;

            while ((entry = in.getNextEntry()) != null) {
                Path path = dest.resolve(entry.getName());

                if (entry.isDirectory())
                    Files.createDirectories(path);
                else {
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }

                in.closeEntry();
            }
        }

        assertThatDirectory(dest).matches(TestUtils.dirAssert);
    }

}
