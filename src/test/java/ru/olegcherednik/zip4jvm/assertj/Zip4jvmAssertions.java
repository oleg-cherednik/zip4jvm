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
package ru.olegcherednik.zip4jvm.assertj;

import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip4jvmAssertions extends Assertions {

    public static ZipFileAssert assertThatZipFile(Path zip) {
        return assertThatZipFile(zip, null);
    }

    public static ZipFileAssert assertThatZipFile(Path zip, char[] password) {
        return new ZipFileAssert(createZipFileDecorator(zip, password));
    }

    public static ZipFileDecorator createZipFileDecorator(Path zip, char[] password) {
        if (isSplit(zip))
            return new ZipFileSplitDecorator(zip, password);
//        if (password == null)
//            return new ZipFileSolidNoEncryptedDecorator(zip);
        return new ZipFileSolidEncryptedDecorator(zip, password);
    }

    public static DirectoryAssert assertThatDirectory(Path path) {
        return new DirectoryAssert(path);
    }

    public static RegularFileAssert assertThatFile(Path path) {
        return new RegularFileAssert(path);
    }

    public static StringLineAssert assertThatStringLine(Path path, int pos, String str) {
        return new StringLineAssert(path, pos, str);
    }

    private static boolean isSplit(Path zip) {
        return Files.exists(SrcZip.getDiskPath(zip, 1));
    }

}
