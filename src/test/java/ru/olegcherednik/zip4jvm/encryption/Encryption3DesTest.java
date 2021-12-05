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
package ru.olegcherednik.zip4jvm.encryption;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

//        UnzipIt.zip(Paths.get("d:/zip4jvm/3des/3des_lk.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes128.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes192.zip")).destDir(destDir).settings(settings).extract();
        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes256.zip")).destDir(destDir).settings(settings).extract();
        assertThatDirectory(destDir).matches(rootAssert);
    }

}
