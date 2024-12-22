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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ZipInfo {

    private final SrcZip srcZip;
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private ZipInfoSettings settings = ZipInfoSettings.DEFAULT;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        return new ZipInfo(SrcZip.of(zip));
    }

    public ZipInfo settings(ZipInfoSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(ZipInfoSettings.DEFAULT);
        return this;
    }

    public ZipInfo password(char[] password) {
        requireNotEmpty(password, "UnzipIt.password");
        settings = settings.toBuilder().password(password).build();
        return this;
    }

    public void printShortInfo() throws IOException {
        printShortInfo(System.out);
    }

    public void printShortInfo(PrintStream out) throws IOException {
        ZipFile.info(srcZip, settings).printTextInfo(out);
    }

    public void decompose(Path dstDir) throws IOException {
        ZipFile.info(srcZip, settings).decompose(dstDir);
    }

    public CentralDirectory.FileHeader getFileHeader(String entryName) throws IOException {
        return ZipFile.info(srcZip, settings).getFileHeader(entryName);
    }

}
