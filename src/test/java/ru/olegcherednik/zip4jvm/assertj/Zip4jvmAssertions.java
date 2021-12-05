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
package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip4jvmAssertions extends Assertions {

    public static ZipFileAssert assertThatZipFile(Path zip) throws IOException {
        return new ZipFileAssert(isSplit(zip) ? new ZipFileSplitDecorator(zip) : new ZipFileSolidNoEncryptedDecorator(zip));
    }

    public static ZipFileAssert assertThatZipFile(Path zip, char[] password) throws IOException {
        return new ZipFileAssert(isSplit(zip) ? new ZipFileSplitDecorator(zip, password) : new ZipFileEncryptedDecoder(zip, password));
    }

    public static DirectoryAssert assertThatDirectory(Path path) {
        return new DirectoryAssert(path);
    }

    public static FileAssert assertThatFile(Path path) {
        return new FileAssert(path);
    }

    public static StringLineAssert assertThatStringLine(Path path, int pos, String str) {
        return new StringLineAssert(path, pos, str);
    }

    private static boolean isSplit(Path zip) {
        return Files.exists(SrcZip.getDiskPath(zip, 1));
    }

}
