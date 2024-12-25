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
package ru.olegcherednik.zip4jvm.compatibility;

import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "LocalVariableNamingConvention" })
public class Zip4jvmToJdkCompatibilityTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(Zip4jvmToJdkCompatibilityTest.class);

    public void checkCompatibilityWithJdk() throws IOException {
        Path parentDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        for (Path zip4jFile : Arrays.asList(zipStoreSolid, zipDeflateSolid)) {
            Path dstDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(parentDir, zip4jFile);
            withZipFile(zip4jFile, dstDir);
            assertThatDirectory(dstDir).matches(rootAssert);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    private static void withZipFile(Path zip4jFile, Path dstDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip4jFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path path = dstDir.resolve(entry.getName());

                if (entry.isDirectory())
                    Files.createDirectories(path);
                else {
                    Files.createDirectories(path.getParent());
                    TestDataAssert.copyLarge(zipFile.getInputStream(entry), path);
                }
            }
        }
    }

}
