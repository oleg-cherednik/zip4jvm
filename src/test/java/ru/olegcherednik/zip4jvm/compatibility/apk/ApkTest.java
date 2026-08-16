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
package ru.olegcherednik.zip4jvm.compatibility.apk;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 04.01.2023
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class ApkTest extends BaseTest {

    public void shouldExtractApk() {
        Path dstDir = subDirNameAsRelativePathToRoot(Paths.get("src/test/resources/apk/app.apk"));

        UnzipIt.zip(Zip4jvmSuite.getResourcePath("zip/app.apk")).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).hasOnlyDirectoriesRegularFiles(3, 10);
        assertThatDirectory(dstDir).directory("kotlin").hasOnlyDirectoriesRegularFiles(6, 1);
        assertThatDirectory(dstDir).directory("kotlin/annotation").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("kotlin/collections").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("kotlin/coroutines").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("kotlin/internal").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("kotlin/ranges").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("kotlin/reflect").hasOnlyRegularFiles(1);

        assertThatDirectory(dstDir).directory("META-INF").hasOnlyDirectoriesRegularFiles(2, 39);
        assertThatDirectory(dstDir).directory("META-INF/com").hasOnlyDirectories(1);
        assertThatDirectory(dstDir).directory("META-INF/com/android").hasOnlyDirectories(1);
        assertThatDirectory(dstDir).directory("META-INF/com/android/build").hasOnlyDirectories(1);
        assertThatDirectory(dstDir).directory("META-INF/com/android/build/gradle").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("META-INF/services").hasOnlyRegularFiles(2);

        assertThatDirectory(dstDir).directory("res").hasOnlyDirectories(42);
        assertThatDirectory(dstDir).directory("res/anim").hasOnlyRegularFiles(27);
        assertThatDirectory(dstDir).directory("res/anim-v21").hasOnlyRegularFiles(4);
        assertThatDirectory(dstDir).directory("res/animator").hasOnlyRegularFiles(19);
        assertThatDirectory(dstDir).directory("res/animator-v21").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/color").hasOnlyRegularFiles(92);
        assertThatDirectory(dstDir).directory("res/color-night-v8").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/color-v21").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/color-v23").hasOnlyRegularFiles(9);
        assertThatDirectory(dstDir).directory("res/drawable").hasOnlyRegularFiles(85);
        assertThatDirectory(dstDir).directory("res/drawable-anydpi-v24").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/drawable-hdpi-v4").hasOnlyRegularFiles(52);
        assertThatDirectory(dstDir).directory("res/drawable-ldrtl-hdpi-v17").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/drawable-ldrtl-mdpi-v17").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/drawable-ldrtl-xhdpi-v17").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/drawable-ldrtl-xxhdpi-v17").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/drawable-ldrtl-xxxhdpi-v17").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/drawable-mdpi-v4").hasOnlyRegularFiles(52);
        assertThatDirectory(dstDir).directory("res/drawable-v21").hasOnlyRegularFiles(8);
        assertThatDirectory(dstDir).directory("res/drawable-v23").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/drawable-watch-v20").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/drawable-xhdpi-v4").hasOnlyRegularFiles(52);
        assertThatDirectory(dstDir).directory("res/drawable-xxhdpi-v4").hasOnlyRegularFiles(47);
        assertThatDirectory(dstDir).directory("res/drawable-xxxhdpi-v4").hasOnlyRegularFiles(26);
        assertThatDirectory(dstDir).directory("res/interpolator").hasOnlyRegularFiles(8);
        assertThatDirectory(dstDir).directory("res/interpolator-v21").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/layout").hasOnlyRegularFiles(106);
        assertThatDirectory(dstDir).directory("res/layout-land").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/layout-ldrtl-v17").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/layout-sw600dp-v13").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/layout-v21").hasOnlyRegularFiles(4);
        assertThatDirectory(dstDir).directory("res/layout-v22").hasOnlyRegularFiles(3);
        assertThatDirectory(dstDir).directory("res/layout-v26").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/layout-w936dp-v13").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/layout-w1240dp-v13").hasOnlyRegularFiles(1);
        assertThatDirectory(dstDir).directory("res/layout-watch-v20").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-anydpi-v26").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-hdpi-v4").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-mdpi-v4").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-xhdpi-v4").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-xxhdpi-v4").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/mipmap-xxxhdpi-v4").hasOnlyRegularFiles(2);
        assertThatDirectory(dstDir).directory("res/xml").hasOnlyRegularFiles(5);
    }

}
