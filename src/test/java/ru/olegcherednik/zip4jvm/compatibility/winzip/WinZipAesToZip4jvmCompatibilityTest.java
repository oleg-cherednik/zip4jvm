/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESEncrypterJCA;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class WinZipAesToZip4jvmCompatibilityTest {

    private static final Path rootDir =
            Zip4jvmSuite.generateSubDirNameWithTime(WinZipAesToZip4jvmCompatibilityTest.class);

    public void winZipAesShouldBeReadableForZip4jvm() throws IOException {
        Path zip = zipItWithWinZipAes(Zip4jvmSuite.subDirNameAsMethodName(rootDir));
        Path dir = unzipItWithZip4jvm(zip);
        assertThatDirectory(dir).matches(rootAssert);
    }

    private static Path zipItWithWinZipAes(Path dir) throws IOException {
        Path zip = dir.resolve("src.zip");
        Files.createDirectories(zip.getParent());

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
    private static Path unzipItWithZip4jvm(Path zip) throws IOException {
        Path dstDir = zip.getParent().resolve("unzip");
        UnzipIt.zip(zip).dstDir(dstDir).password(password).extract();

        // WinZipAes does not support empty folders in zip
        Files.createDirectories(dstDir.resolve(dirNameEmpty));
        // WinZipAes uses 'iso-8859-1' for file names
        Files.copy(fileOlegCherednik, dstDir.resolve(fileNameOlegCherednik));
        return dstDir;
    }

    private static List<Path> getDirectoryEntries(Path dir) {
        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

}
