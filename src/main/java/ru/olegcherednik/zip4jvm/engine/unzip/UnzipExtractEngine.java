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

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.engine.zip.ZipSymlinkEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.ConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SolidConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SplitConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 22.12.2024
 */
@RequiredArgsConstructor
public class UnzipExtractEngine {

    protected final PasswordProvider passwordProvider;
    protected final ZipModel zipModel;

    public void extract(Path dstDir, Collection<String> fileNames) {
        Map<String, String> map = null;

        if (CollectionUtils.isNotEmpty(fileNames))
            map = getEntriesByPrefix(new HashSet<>(fileNames));

        extractEntry(dstDir, map);
    }

    public ZipFile.Entry extract(String fileName) {
        ZipEntry zipEntry = zipModel.getZipEntryByFileName(ZipUtils.normalizeFileName(fileName));
        zipEntry.setPassword(passwordProvider.getFilePassword(zipEntry.getFileName()));
        return zipEntry.createImmutableEntry();
    }

    protected Map<String, String> getEntriesByPrefix(Set<String> fileNames) {
        Map<String, String> map = new HashMap<>();

        for (String fileName : fileNames) {
            String entryName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

            if (zipModel.hasEntry(entryName)) {
                ZipEntry zipEntry = zipModel.getZipEntryByFileName(entryName);
                map.put(entryName, FilenameUtils.getName(zipEntry.getFileName()));
            }

            for (ZipEntry zipEntry : getEntriesByPrefix(entryName + '/'))
                map.put(zipEntry.getFileName(), StringUtils.substring(zipEntry.getFileName(), fileName.length() + 1));
        }

        return map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }

    protected List<ZipEntry> getEntriesByPrefix(String prefix) {
        return zipModel.getZipEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(prefix))
                       .collect(Collectors.toList());
    }

    // ----------

    protected void extractEntry(Path dstDir, Map<String, String> map) {
        try (ConsecutiveAccessDataInput in = createConsecutiveDataInput(zipModel.getSrcZip())) {
            Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();

                if (map == null || map.containsKey(zipEntry.getFileName())) {
                    in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());

                    String fileName = Optional.ofNullable(map)
                                              .map(m -> m.get(zipEntry.getFileName()))
                                              .orElse(zipEntry.getFileName());
                    Path file = dstDir.resolve(fileName);
                    extractEntry(file, zipEntry, in);
                }
            }
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    protected void extractEntry(Path file, ZipEntry zipEntry, DataInput in) throws IOException {
        if (zipEntry.isSymlink())
            extractSymlink(file, zipEntry, in);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry, in);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privileges)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    protected static void extractSymlink(Path symlink, ZipEntry zipEntry, DataInput in) throws IOException {
        String target = IOUtils.toString(zipEntry.createInputStream(in), Charsets.UTF_8);

        if (target.startsWith("/"))
            ZipSymlinkEngine.createAbsoluteSymlink(symlink, Paths.get(target));
        else if (target.contains(":"))
            // TODO absolute windows symlink
            throw new Zip4jvmException("windows absolute symlink is not supported");
        else
            ZipSymlinkEngine.createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    protected static void extractEmptyDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    protected void extractRegularFile(Path file, ZipEntry zipEntry, DataInput in) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));
        ZipUtils.copyLarge(zipEntry.createInputStream(in), getOutputStream(file));
    }

    protected static void setFileAttributes(Path path, ZipEntry zipEntry) throws IOException {
        if (zipEntry.getExternalFileAttributes() != null)
            zipEntry.getExternalFileAttributes().apply(path);
    }

    private static void setFileLastModifiedTime(Path path, ZipEntry zipEntry) throws IOException {
        long lastModifiedTime = DosTimestampConverterUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
        Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
    }

    protected static OutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(parent))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);
        return Files.newOutputStream(file);
    }

    // ---------- static ----------

    public static ConsecutiveAccessDataInput createConsecutiveDataInput(SrcZip srcZip) throws IOException {
        return srcZip.isSolid() ? new SolidConsecutiveAccessDataInput(srcZip)
                                : new SplitConsecutiveAccessDataInput(srcZip);

    }

}
