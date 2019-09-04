package ru.olegcherednik.zip4jvm.compatibility;

import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class Zip4jToWinZipAesCompatibilityTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(Zip4jToWinZipAesCompatibilityTest.class);

    public void checkCompatibilityWithWinZipAes() throws IOException, DataFormatException {
        String password = new String(Zip4jSuite.password);
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        AesZipFileDecrypter decrypter = new AesZipFileDecrypter(Zip4jSuite.deflateSolidAesZip.toFile(), new AESDecrypterJCA());

        for (ExtZipEntry zipEntry : decrypter.getEntryList()) {
            Path path = dstDir.resolve(zipEntry.getName());

            if (zipEntry.isDirectory())
                Files.createDirectories(path);
            else {
                Files.createDirectories(path.getParent());

                if (zipEntry.getSize() == 0)
                    Files.createFile(path);
                else {
                    if (!Files.exists(path))
                        Files.createFile(path);

                    decrypter.extractEntry(zipEntry, path.toFile(), password);
                }
            }
        }

        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

}
