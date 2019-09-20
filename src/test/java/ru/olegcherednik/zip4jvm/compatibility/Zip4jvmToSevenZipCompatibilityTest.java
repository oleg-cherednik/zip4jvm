package ru.olegcherednik.zip4jvm.compatibility;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.TestData.deflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidZip;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 05.04.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention", "LocalVariableNamingConvention" })
public class Zip4jToSevenZipCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(Zip4jToSevenZipCompatibilityTest.class);

    public void checkCompatibilityWithSevenZip() throws IOException {
        String password = new String(Zip4jvmSuite.password);
        Path parentDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        for (Path zip4jFile : Arrays.asList(storeSolidZip, deflateSolidZip, deflateSolidPkwareZip)) {
            Path dstDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(parentDir, zip4jFile);

            try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(zip4jFile.toFile(), "r"));
                 IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

                for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems()) {
                    Path path = dstDir.resolve(item.getPath());

                    if (item.isFolder())
                        Files.createDirectories(path);
                    else {
                        Files.createDirectories(path.getParent());

                        if (item.getSize() == 0)
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
                            }, password);

                            if (res != ExtractOperationResult.OK)
                                throw new RuntimeException("Cannot extract zip entry");
                        }
                    }
                }
            }

            assertThatDirectory(dstDir).matches(TestDataAssert.dirAssert);
        }

    }

}
