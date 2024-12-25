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
package ru.olegcherednik.zip4jvm.compatibility.apk;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.assertj.DirectoryAssert;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.appApk;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 04.01.2023
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class ApkTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ApkTest.class);

    public void shouldExtractApk() throws IOException {
        Path subDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);
        Path dstDir = Zip4jvmSuite.subDirNameAsRelativePathToRoot(subDir, appApk);

        UnzipIt.zip(Zip4jvmSuite.getResourcePath("zip/app.apk")).dstDir(dstDir).extract();

        assertThatDirectory(dstDir).matches(dir -> {
            dir.exists().hasDirectories(3).hasRegularFiles(10);

            DirectoryAssert dirKotlin = (DirectoryAssert) dir.directory("kotlin");
            dirKotlin.exists().hasDirectories(6).hasRegularFiles(1);
            dirKotlin.directory("annotation").exists().hasDirectories(0).hasRegularFiles(1);
            dirKotlin.directory("collections").exists().hasDirectories(0).hasRegularFiles(1);
            dirKotlin.directory("coroutines").exists().hasDirectories(0).hasRegularFiles(1);
            dirKotlin.directory("internal").exists().hasDirectories(0).hasRegularFiles(1);
            dirKotlin.directory("ranges").exists().hasDirectories(0).hasRegularFiles(1);
            dirKotlin.directory("reflect").exists().hasDirectories(0).hasRegularFiles(1);

            DirectoryAssert dirMetaInf = (DirectoryAssert) dir.directory("META-INF");
            dirMetaInf.exists().hasDirectories(2).hasRegularFiles(39);
            dirMetaInf.directory("com").exists().hasDirectories(1).hasRegularFiles(0);
            dirMetaInf.directory("com/android").exists().hasDirectories(1).hasRegularFiles(0);
            dirMetaInf.directory("com/android/build").exists().hasDirectories(1).hasRegularFiles(0);
            dirMetaInf.directory("com/android/build/gradle").exists().hasDirectories(0).hasRegularFiles(1);
            dirMetaInf.directory("services").exists().hasDirectories(0).hasRegularFiles(2);

            DirectoryAssert dirRes = (DirectoryAssert) dir.directory("res");
            dirRes.exists().hasDirectories(42).hasRegularFiles(0);
            dirRes.directory("anim").exists().hasDirectories(0).hasRegularFiles(27);
            dirRes.directory("anim-v21").exists().hasDirectories(0).hasRegularFiles(4);
            dirRes.directory("animator").exists().hasDirectories(0).hasRegularFiles(19);
            dirRes.directory("animator-v21").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("color").exists().hasDirectories(0).hasRegularFiles(92);
            dirRes.directory("color-night-v8").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("color-v21").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("color-v23").exists().hasDirectories(0).hasRegularFiles(9);
            dirRes.directory("drawable").exists().hasDirectories(0).hasRegularFiles(85);
            dirRes.directory("drawable-anydpi-v24").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("drawable-hdpi-v4").exists().hasDirectories(0).hasRegularFiles(52);
            dirRes.directory("drawable-ldrtl-hdpi-v17").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("drawable-ldrtl-mdpi-v17").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("drawable-ldrtl-xhdpi-v17").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("drawable-ldrtl-xxhdpi-v17").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("drawable-ldrtl-xxxhdpi-v17").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("drawable-mdpi-v4").exists().hasDirectories(0).hasRegularFiles(52);
            dirRes.directory("drawable-v21").exists().hasDirectories(0).hasRegularFiles(8);
            dirRes.directory("drawable-v23").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("drawable-watch-v20").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("drawable-xhdpi-v4").exists().hasDirectories(0).hasRegularFiles(52);
            dirRes.directory("drawable-xxhdpi-v4").exists().hasDirectories(0).hasRegularFiles(47);
            dirRes.directory("drawable-xxxhdpi-v4").exists().hasDirectories(0).hasRegularFiles(26);
            dirRes.directory("interpolator").exists().hasDirectories(0).hasRegularFiles(8);
            dirRes.directory("interpolator-v21").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("layout").exists().hasDirectories(0).hasRegularFiles(106);
            dirRes.directory("layout-land").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("layout-ldrtl-v17").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("layout-sw600dp-v13").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("layout-v21").exists().hasDirectories(0).hasRegularFiles(4);
            dirRes.directory("layout-v22").exists().hasDirectories(0).hasRegularFiles(3);
            dirRes.directory("layout-v26").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("layout-w936dp-v13").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("layout-w1240dp-v13").exists().hasDirectories(0).hasRegularFiles(1);
            dirRes.directory("layout-watch-v20").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-anydpi-v26").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-hdpi-v4").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-mdpi-v4").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-xhdpi-v4").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-xxhdpi-v4").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("mipmap-xxxhdpi-v4").exists().hasDirectories(0).hasRegularFiles(2);
            dirRes.directory("xml").exists().hasDirectories(0).hasRegularFiles(5);
        });
    }

}
