package ru.olegcherednik.zip4jvm.encryption;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
//@Test
@SuppressWarnings("FieldNamingConvention")
public class Encryption3DesTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(Encryption3DesTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipWhenStoreSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipSettings settings = UnzipSettings.builder().password(fileName -> password).build();

        UnzipIt.zip(Paths.get("d:/zip4jvm/3des/3des_lk.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes128.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes192.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes256.zip")).destDir(destDir).settings(settings).extract();
//        assertThatDirectory(destDir).matches(rootAssert);
    }

    public static void main(String[] args) throws IOException {
        Path zip = Paths.get("d:/zip4jvm/3des/3des_store_168.zip");
        Path destDir = Paths.get("d:/zip4jvm/3des/3des_store_168");
//        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();

        InputStream inputStream = null;
        try {
            Path filePath = zip;
            inputStream = Files.newInputStream(filePath);
            ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
            ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
            ArchiveEntry archiveEntry = null;
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                Path path = Paths.get(destDir.toString(), archiveEntry.getName());
                File file = path.toFile();
                if (archiveEntry.isDirectory()) {
                    if (!file.isDirectory()) {
                        file.mkdirs();
                    }
                } else {
                    File parent = file.getParentFile();
                    if (!parent.isDirectory()) {
                        parent.mkdirs();
                    }
                    try (OutputStream outputStream = Files.newOutputStream(path)) {
                        IOUtils.copy(archiveInputStream, outputStream);
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ArchiveException e) {
            e.printStackTrace();
        }
    }


}
