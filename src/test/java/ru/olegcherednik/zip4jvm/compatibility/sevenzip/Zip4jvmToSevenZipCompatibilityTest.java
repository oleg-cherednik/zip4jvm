/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.compatibility.sevenzip;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
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
@SuppressWarnings("NewClassNamingConvention")
public class Zip4jvmToSevenZipCompatibilityTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @Test(dataProvider = "zipFiles")
    @SuppressWarnings("PMD.CognitiveComplexity")
    public void checkCompatibilityWithSevenZip(Path zipFile) throws IOException {
        Path parentDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        Path dstDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(parentDir, zipFile);

        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(zipFile.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

            for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems()) {
                Path path = dstDir.resolve(item.getPath());

                if (item.isFolder())
                    Zip4jvmSuite.createDir(path);
                else {
                    Zip4jvmSuite.createDir(path.getParent());

                    if (item.getSize() == 0)
                        Files.createFile(path);
                    else {
                        if (!Files.exists(path))
                            Files.createFile(path);

                        ExtractOperationResult res = item.extractSlow(data -> {
                            try {
                                PathUtils.copyByteArray(path, data, StandardOpenOption.APPEND);
                                return ArrayUtils.getLength(data);
                            } catch (Exception e) {
                                assertThat(e);
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

    @DataProvider(name = "zipFiles")
    public static Object[][] zipFiles() {
        return new Object[][] {
                { zipStoreSolid },
                { zipDeflateSolid },
                { zipDeflateSolidPkware } };
    }

}
