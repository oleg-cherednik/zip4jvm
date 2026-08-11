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
package ru.olegcherednik.zip4jvm.encryption;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;

/**
 * @author Oleg Cherednik
 * @since 19.09.2024
 */
@Test
public class EncryptedCentralDirectoryTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipWhenStoreSolidAes() {
        Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/store_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/deflate_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/lzma_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/bzip2_aes256bit.zip");

        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/3des168bit.zip");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipSettings settings = UnzipSettings.builder().password(password).build();
        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();
        //        ZipInfo.zip(zip).password(password).decompose(dstDir.resolve("-----"));
        //        assertThatDirectory(dstDir).matches(rootAssert);
    }

    @Test(enabled = false)
    @SuppressWarnings("NewMethodNamingConvention")
    public void shouldThrowIncorrectCentralDirectoryPasswordExceptionWhenNotCorrectPasswordForCentralDirectory() {
        Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/aes128bit.zip");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipSettings settings = UnzipSettings.builder()
                                              .passwordProvider(new PasswordProvider() {
                                                  @Override
                                                  public char[] getFilePassword(String fileName) {
                                                      return password;
                                                  }

                                                  @Override
                                                  public char[] getCentralDirectoryPassword() {
                                                      return "unknown".toCharArray();
                                                  }
                                              }).build();

        assertThatThrownBy(() -> UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(IncorrectCentralDirectoryPasswordException.class)
                .hasMessageContaining("central directory");
    }

}
