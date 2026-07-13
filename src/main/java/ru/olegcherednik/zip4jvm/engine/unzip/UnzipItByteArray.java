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
package ru.olegcherednik.zip4jvm.engine.unzip;

import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireDirectory;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * This class encapsulates logic of an archive unzipping given as {@code byte[]}. It does not expect a huge byte array.
 *
 * @author Oleg Cherednik
 * @since 12.06.2026
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UnzipItByteArray {

    private final byte[] zip;
    private Path dstDir;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static UnzipItByteArray create(byte[] zip) {
        requireNotEmpty(zip, "UnzipByteArray.zip");
        return new UnzipItByteArray(Arrays.copyOf(zip, zip.length));
    }

    public UnzipItByteArray dstDir(Path dstDir) {
        requireNotNull(dstDir, "UnzipByteArray.dstDir");
        requireDirectory(dstDir, "UnzipByteArray.dstDir");

        this.dstDir = dstDir;
        return this;
    }

    // @NotNull
    public String readString(String fileName) {
        return new String(readByteArray(fileName), StandardCharsets.UTF_8);
    }

    // @NotNull
    public char[] readCharArray(String fileName) {
        return readString(fileName).toCharArray();
    }

    // @NotNull
    public byte[] readByteArray(String fileName) {
        requireNotBlank(fileName, "UnzipByteArray.fileName");

        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;

            while ((entry = in.getNextEntry()) != null) {
                if (entry.getName().equals(fileName))
                    return IOUtils.toByteArray(in);

                in.closeEntry();
            }
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }

        throw new EntryNotFoundException(fileName);
    }

    @SuppressWarnings("NestedTryStatement")
    public void extract(String dirName) {
        requireNotBlank(dirName, "UnzipByteArray.dirName");

        String dirNameWithSlash = dirName.endsWith("/") ? dirName : dirName + '/';

        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;

            while ((entry = in.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (!entryName.equals(dirNameWithSlash) && entryName.startsWith(dirNameWithSlash)) {
                    Path file = dstDir.resolve(FilenameUtils.getName(entryName));
                    Files.deleteIfExists(file);
                    Files.createDirectories(file.getParent());

                    try (OutputStream out = Files.newOutputStream(file.toFile().toPath())) {
                        IOUtils.copy(in, out);
                    }
                }

                in.closeEntry();
            }
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
