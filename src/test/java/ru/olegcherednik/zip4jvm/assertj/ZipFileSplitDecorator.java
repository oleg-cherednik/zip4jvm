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

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipSplitReadOnlySeekableByteChannel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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

            if (password == null)
                extractFileByCommonsCompress(entry.getName(), tmp);
            else
                extractFileByZip4j(entry.getName(), tmp);

            return Files.newInputStream(tmp);
        });
    }

    private void extractFileByCommonsCompress(String entryName, Path destPath) throws IOException {
        Path[] paths = getPaths(zip);

        try (SeekableByteChannel seekableByteChannel = ZipSplitReadOnlySeekableByteChannel.forPaths(paths);
             ZipFile zipFile = ZipFile.builder()
                                      .setSeekableByteChannel(seekableByteChannel)
                                      .get()) {

            Zip4jvmSuite.createDir(destPath.getParent());
            copy(zipFile, entryName, destPath);
        }
    }

    private static void copy(ZipFile zipFile, String entryName, Path destPath) throws IOException {
        try (InputStream in = zipFile.getInputStream(zipFile.getEntry(entryName));
             OutputStream out = Files.newOutputStream(destPath)) {
            IOUtils.copy(in, out);
        }
    }

    private void extractFileByZip4j(String entryName, Path destPath) throws IOException {
        try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zip.toFile(), password)) {
            zipFile.extractFile(entryName, destPath.getParent().toString(), destPath.getFileName().toString());
        }
    }

    private static Map<String, ZipArchiveEntry> entries(Path path) {
        Path[] paths = getPaths(path);

        try (SeekableByteChannel seekableByteChannel = ZipSplitReadOnlySeekableByteChannel.forPaths(paths);
             ZipFile zipFile = ZipFile.builder()
                                      .setSeekableByteChannel(seekableByteChannel)
                                      .get()) {
            return Collections.list(zipFile.getEntries()).stream()
                              .collect(Collectors.toMap(ZipArchiveEntry::getName, Function.identity()));
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Path[] getPaths(Path path) {
        return SrcZip.of(path).getDisks().stream()
                     .map(SrcZip.Disk::getPath)
                     .toArray(Path[]::new);
    }

}
