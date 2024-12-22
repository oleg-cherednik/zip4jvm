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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;

/**
 * @author Oleg Cherednik
 * @since 22.12.2024
 */
@RequiredArgsConstructor
public class UnzipExtractEngine {

    protected final PasswordProvider passwordProvider;
    protected final ZipModel zipModel;

    public void extract(Path dstDir, Collection<String> fileNames) throws IOException {
        Map<String, Function<ZipEntry, String>> map =
                CollectionUtils.isEmpty(fileNames) ? null : getEntryNamesByPrefix(new HashSet<>(fileNames));
        extractEntry(dstDir, map);
    }

    public ZipFile.Entry extract(String fileName) throws IOException {
        requireNotBlank(fileName, "UnzipIt.fileName");

        ZipEntry zipEntry = zipModel.getZipEntryByFileName(ZipUtils.normalizeFileName(fileName));

        if (zipEntry == null)
            throw new FileNotFoundException("Entry '" + fileName + "' was not found");

        zipEntry.setPassword(passwordProvider.getFilePassword(zipEntry.getFileName()));
        return zipEntry.createImmutableEntry();
    }

    protected Map<String, Function<ZipEntry, String>> getEntryNamesByPrefix(Set<String> fileNames) {
        Map<String, Function<ZipEntry, String>> map = new HashMap<>();

        for (String fileName : fileNames) {
            fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

            if (zipModel.hasEntry(fileName))
                map.put(fileName, e -> FilenameUtils.getName(e.getFileName()));
            else
                for (ZipEntry zipEntry : getEntriesNamesByPrefix(fileName + '/'))
                    map.put(zipEntry.getFileName(), ZipEntry::getFileName);
        }

        return map.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }

    protected List<ZipEntry> getEntriesNamesByPrefix(String fileNamePrefix) {
        return zipModel.getZipEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
    }

    // ----------

    protected void extractEntry(Path dstDir, Map<String, Function<ZipEntry, String>> map) throws IOException {
        try (ConsecutiveAccessDataInput in = createConsecutiveDataInput(zipModel.getSrcZip())) {
            Iterator<ZipEntry> it = zipModel.offsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();

                if (map == null || map.containsKey(zipEntry.getFileName())) {
                    in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());


                    extractEntry(dstDir, zipEntry, in, map == null ? ZipEntry::getFileName
                                                                   : map.get(zipEntry.getFileName()));
                }
            }
        }
    }

    protected void extractEntry(Path dstDir,
                                ZipEntry zipEntry,
                                DataInput in,
                                Function<ZipEntry, String> getFileName) throws IOException {
        Path file = dstDir.resolve(getFileName.apply(zipEntry));

        if (zipEntry.isSymlink())
            extractSymlink(file, zipEntry, in);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry, in);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privelegies)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    protected static void extractSymlink(Path symlink, ZipEntry zipEntry, DataInput in) throws IOException {
        String target = IOUtils.toString(zipEntry.createInputStream(), Charsets.UTF_8);

        if (target.startsWith("/"))
            ZipSymlinkEngine.createAbsoluteSymlink(symlink, Paths.get(target));
        else if (target.contains(":"))
            // TODO absolute windows symlink
            throw new Zip4jvmException("windows absolute symlink not supported");
        else
            ZipSymlinkEngine.createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    protected static void extractEmptyDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    @SuppressWarnings("PMD.CloseResource")
    protected void extractRegularFile(Path file, ZipEntry zipEntry, DataInput di) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));

        InputStream in = zipEntry.createInputStream(di);

//        if (zipEntry.getAesVersion() != AesVersion.AE_2) {
//            in = ChecksumInputStream.builder()
//                                    .setExpectedChecksumValue(zipEntry.getChecksum())
//                                    .setChecksum(new PureJavaCrc32())
//                                    .setInputStream(in)
//                                    .get();
//        }

        ZipUtils.copyLarge(in, getOutputStream(file));
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
