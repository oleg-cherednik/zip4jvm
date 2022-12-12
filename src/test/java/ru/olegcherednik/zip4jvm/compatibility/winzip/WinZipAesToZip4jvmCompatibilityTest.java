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

import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESEncrypterJCA;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
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

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(WinZipAesToZip4jvmCompatibilityTest.class);

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

        for (Path file : getDirectoryEntries(dirSrc)) {
            if (Files.isDirectory(file))
                continue;
            if ("Oleg Cherednik.txt".equals(file.getFileName().toString()))
                continue;

            String pathForEntry = dirSrc.relativize(file).toString();
            encrypter.add(file.toFile(), pathForEntry, passwordStr);
        }

        encrypter.close();

        return zip;
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static Path unzipItWithZip4jvm(Path zip) throws IOException {
        Path destDir = zip.getParent().resolve("unzip");
        UnzipIt.zip(zip).destDir(destDir).password(password).extract();

        // WinZipAes does not support empty folders in zip
        Files.createDirectories(destDir.resolve("empty_dir"));
        // WinZipAes uses 'iso-8859-1' for file names
        Files.copy(dirSrc.resolve("Oleg Cherednik.txt"), destDir.resolve("Oleg Cherednik.txt"));
        return destDir;
    }

    private static List<Path> getDirectoryEntries(Path dir) {
        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            return Collections.emptyList();
        }
    }

}
