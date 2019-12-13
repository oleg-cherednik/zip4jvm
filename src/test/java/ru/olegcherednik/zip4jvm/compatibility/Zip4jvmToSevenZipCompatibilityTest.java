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
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 05.04.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention", "LocalVariableNamingConvention" })
public class Zip4jvmToSevenZipCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(Zip4jvmToSevenZipCompatibilityTest.class);

    public void checkCompatibilityWithSevenZip() throws IOException {
        Path parentDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        for (Path zip4jFile : Arrays.asList(zipStoreSolid, zipDeflateSolid, zipDeflateSolidPkware)) {
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
                            }, passwordStr);

                            if (res != ExtractOperationResult.OK)
                                throw new Zip4jvmException("Cannot extract zip entry");
                        }
                    }
                }
            }

            assertThatDirectory(dstDir).matches(rootAssert);
        }
    }

}
