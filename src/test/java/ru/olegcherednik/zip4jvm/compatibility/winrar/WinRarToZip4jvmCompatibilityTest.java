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
package ru.olegcherednik.zip4jvm.compatibility.winrar;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarDeflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.winRarStoreSolidZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class WinRarToZip4jvmCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(WinRarToZip4jvmCompatibilityTest.class);

    public void checkCompatibilityWithWinRar() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        for (Path zip : Arrays.asList(winRarStoreSolidZip,
                                      winRarStoreSolidPkwareZip,
                                      winRarStoreSolidAesZip,
                                      winRarDeflateSolidZip,
                                      winRarDeflateSolidPkwareZip,
                                      winRarDeflateSolidAesZip)) {
            Path destDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(dir, zip);
            UnzipIt.zip(zip).destDir(destDir).password(password).extract();
            assertThatDirectory(destDir).matches(dirBikesAssert);
        }
    }

}
