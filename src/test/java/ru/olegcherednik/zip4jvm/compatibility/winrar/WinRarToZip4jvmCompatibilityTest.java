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
package ru.olegcherednik.zip4jvm.compatibility.winrar;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

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
@SuppressWarnings("NewClassNamingConvention")
public class WinRarToZip4jvmCompatibilityTest extends BaseTest {

    public void checkCompatibilityWithWinRar() {
        Path dir = getTestRoot();

        for (Path zip : Arrays.asList(winRarStoreSolidZip,
                                      winRarStoreSolidPkwareZip,
                                      winRarStoreSolidAesZip,
                                      winRarDeflateSolidZip,
                                      winRarDeflateSolidPkwareZip,
                                      winRarDeflateSolidAesZip)) {
            Path dstDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(dir, zip);
            UnzipIt.zip(zip).dstDir(dstDir).password(password).extract();
            assertThatDirectory(dstDir).matches(dirBikesAssert);
        }
    }

}
