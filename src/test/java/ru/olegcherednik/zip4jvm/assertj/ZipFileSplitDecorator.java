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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.temporaryFile;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
class ZipFileSplitDecorator extends ZipFileDecorator {

    private final char[] password;

    ZipFileSplitDecorator(Path zip) {
        this(zip, null);
    }

    ZipFileSplitDecorator(Path zip, char[] password) {
        super(zip, entries(zip));
        this.password = ArrayUtils.clone(password);
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) {
        return Quietly.doRuntime(() -> {
            Path tmp = temporaryFile(FilenameUtils.getExtension(entry.getName()));
            extractFile(entry, tmp);
            return Files.newInputStream(tmp);
        });
    }

    private void extractFile(ZipEntry entry, Path destPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip.toFile(), password)) {
            zipFile.extractFile(entry.getName(), destPath.getParent().toString(), destPath.getFileName().toString());
        }
    }

    private static Map<String, ZipArchiveEntry> entries(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            return zipFile.getFileHeaders().stream()
                          .map(fileHeader -> {
                              ZipArchiveEntry entry = new ZipArchiveEntry(fileHeader.getFileName());
                              entry.setSize(fileHeader.getUncompressedSize());
                              entry.setCompressedSize(fileHeader.getCompressedSize());
                              entry.setCrc(fileHeader.getCrc());
                              return entry;
                          })
                          .collect(Collectors.toMap(ZipEntry::getName, Function.identity()));
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
