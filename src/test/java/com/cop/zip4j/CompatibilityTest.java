package com.cop.zip4j;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 05.04.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompatibilityTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirName(CompatibilityTest.class);

    public void zip4jShouldBeReadableForZipFile() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);

        try (ZipFile zipFile = new ZipFile(Zip4jSuite.noSplitZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = destDir.resolve(entry.getName());

                if (entry.isDirectory())
                    Files.createDirectories(path);
                else
                    TestUtils.copyLarge(zipFile.getInputStream(entry), path);
            }
        }

        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

//    public void zip4jShouldBeReadableForZipInputStream() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        byte[] buf = new byte[1024];
//
//        try (ZipInputStream in = new ZipInputStream(new FileInputStream(Zip4jSuite.noSplitZip.toFile()))) {
//            ZipEntry entry;
//
//            while ((entry = in.getNextEntry()) != null) {
//                Path path = destDir.resolve(entry.getName());
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
//        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
//    }

    public void zip4jPkwareEncryptionShouldBeReadableForSevenZipTool() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);

        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(Zip4jSuite.noSplitPkwareZip.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

            for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems()) {
                Path path = destDir.resolve(item.getPath());

                if (item.isFolder())
                    Files.createDirectories(path);
                else if (item.getSize() == 0)
                    Files.createFile(path);
                else {
                    if (!Files.exists(path))
                        Files.createFile(path);

                    ExtractOperationResult res = item.extractSlow(data -> {
                        try {
                            Files.write(path, data, StandardOpenOption.APPEND);
                            return ArrayUtils.getLength(data);
                        } catch(IOException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }, new String(Zip4jSuite.password));

                    if (res != ExtractOperationResult.OK)
                        throw new RuntimeException("Cannot extract zip entry");
                }
            }
        }

        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    public void winRarPkwareEncryptionZipShouldBeReadableForZip4j() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(destDir);

        assertThat(Files.exists(Zip4jSuite.winRarPkwareZip)).isTrue();

        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(Zip4jSuite.winRarPkwareZip)
                               .password(Zip4jSuite.password).build();

        unzip.extract(destDir);

        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

}
