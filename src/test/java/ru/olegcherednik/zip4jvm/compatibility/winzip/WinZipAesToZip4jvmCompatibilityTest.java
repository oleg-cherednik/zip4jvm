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
package ru.olegcherednik.zip4jvm.compatibility.winzip;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESEncrypterJCA;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcData;
import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class WinZipAesToZip4jvmCompatibilityTest extends BaseTest {

    public void winZipAesShouldBeReadableForZip4jvm() throws IOException {
        Path zip = zipItWithWinZipAes(getTestRoot());
        Path dir = unzipItWithZip4jvm(zip);
        assertThatDirectory(dir).matches(rootAssert);
    }

    private static Path zipItWithWinZipAes(Path dir) throws IOException {
        Path zip = dir.resolve("src.zip");
        Zip4jvmSuite.createDir(zip.getParent());

        AesZipFileEncrypter encrypter = new AesZipFileEncrypter(zip.toFile(), new AESEncrypterJCA());
        encrypter.setComment("password: " + passwordStr);

        for (Path path : getDirectoryEntries(dirSrcData)) {
            if (!Files.isRegularFile(path) || Files.isSymbolicLink(path))
                continue;

            String fileName = path.getFileName().toString();

            if (fileNameOlegCherednik.equals(fileName) || PathUtils.DS_STORE.equalsIgnoreCase(fileName))
                continue;

            String pathForEntry = dirSrcData.relativize(path).toString();
            encrypter.add(path.toFile(), pathForEntry, passwordStr);
        }

        encrypter.close();

        return zip;
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static Path unzipItWithZip4jvm(Path zip) {
        Path dstDir = zip.getParent().resolve("unzip");
        UnzipIt.zip(zip).dstDir(dstDir).password(password).extract();

        // WinZipAes does not support empty folders in zip
        Zip4jvmSuite.createDir(dstDir.resolve(dirNameEmpty));
        // WinZipAes uses 'iso-8859-1' for file names
        Zip4jvmSuite.copyFile(fileOlegCherednik, dstDir.resolve(fileNameOlegCherednik));
        return dstDir;
    }

    private static List<Path> getDirectoryEntries(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                    .collect(Collectors.toList());
        }
    }

}
