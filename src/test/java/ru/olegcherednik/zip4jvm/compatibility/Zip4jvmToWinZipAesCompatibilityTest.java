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
package ru.olegcherednik.zip4jvm.compatibility;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class Zip4jvmToWinZipAesCompatibilityTest extends BaseTest {

    public void checkCompatibilityWithWinZipAes() throws IOException, DataFormatException {
        Path dstDir = getTestRoot();
        AesZipFileDecrypter decrypter = new AesZipFileDecrypter(zipDeflateSolidAes.toFile(), new AESDecrypterJCA());
        AesZipFileDecrypter.charset = Charsets.UTF_8.name();

        for (ExtZipEntry zipEntry : decrypter.getEntryList()) {
            Path path = dstDir.resolve(zipEntry.getName());

            if (zipEntry.isDirectory())
                Zip4jvmSuite.createDir(path);
            else {
                Zip4jvmSuite.createDir(path.getParent());

                if (zipEntry.getSize() == 0)
                    Files.createFile(path);
                else {
                    if (!Files.exists(path))
                        Files.createFile(path);

                    decrypter.extractEntry(zipEntry, path.toFile(), zipEntry.getName());
                }
            }
        }

        assertThatDirectory(dstDir).matches(rootAssert);
    }

}
