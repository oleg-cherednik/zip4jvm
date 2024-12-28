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
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
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
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 22.12.2024
 */
@RequiredArgsConstructor
public class UnzipExtractEngine {

    private static final char SLASH = '/';

    protected final PasswordProvider passwordProvider;
    protected final ZipModel zipModel;

    public void extract(Path dstDir, Collection<String> fileNames) {
        if (zipModel.isEmpty())
            return;

        if (CollectionUtils.isEmpty(fileNames))
            extractAllEntries(dstDir);
        else
            extractEntryByPrefix(dstDir, fileNames.stream()
                                                  .map(ZipUtils::getFileNameNoDirectoryMarker)
                                                  .collect(Collectors.toSet()));
    }

    public ZipFile.Entry extract(String fileName) {
        ZipEntry zipEntry = zipModel.getZipEntryByFileName(ZipUtils.normalizeFileName(fileName));
        zipEntry.setPassword(passwordProvider.getFilePassword(zipEntry.getFileName()));
        return zipEntry.createImmutableEntry();
    }

    protected void extractAllEntries(Path dstDir) {
        try (ConsecutiveAccessDataInput in = createConsecutiveDataInput()) {
            Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                Path file = dstDir.resolve(zipEntry.getFileName());
                extractEntry(file, zipEntry, in);
            }
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    protected void extractEntryByPrefix(Path dstDir, Set<String> prefixes) {
        assert CollectionUtils.isNotEmpty(prefixes);

        try (ConsecutiveAccessDataInput in = createConsecutiveDataInput()) {
            Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                String fileName = getFileName(zipEntry, prefixes);

                if (fileName != null) {
                    Path file = dstDir.resolve(fileName);
                    extractEntry(file, zipEntry, in);
                }
            }
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    protected String getFileName(ZipEntry zipEntry, Set<String> prefixes) {
        assert CollectionUtils.isNotEmpty(prefixes);

        String fileName = zipEntry.getFileName();

        if (prefixes.contains(fileName))
            return FilenameUtils.getName(fileName);

        for (String prefix : prefixes) {
            if (fileName.equals(prefix + '/'))
                return null;
            if (fileName.startsWith(prefix + '/'))
                return StringUtils.substring(fileName, prefix.length() + 1);
        }

        return null;
    }

    protected void extractEntry(Path file, ZipEntry zipEntry, ConsecutiveAccessDataInput in) throws IOException {
        in.seekForward(zipEntry.getLocalFileHeaderAbsOffs());

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

    protected void extractSymlink(Path symlink, ZipEntry zipEntry, DataInput in) throws IOException {
        String target = IOUtils.toString(zipEntry.createInputStream(in), Charsets.UTF_8);

        if (target.charAt(0) == SLASH)
            ZipSymlinkEngine.createAbsoluteSymlink(symlink, Paths.get(target));
        else if (target.contains(":"))
            // TODO absolute windows symlink
            throw new Zip4jvmException("windows absolute symlink is not supported");
        else
            ZipSymlinkEngine.createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    protected void extractEmptyDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    protected void extractRegularFile(Path file, ZipEntry zipEntry, DataInput in) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));
        ZipUtils.copyLarge(zipEntry.createInputStream(in), getOutputStream(file));
    }

    public ConsecutiveAccessDataInput createConsecutiveDataInput() {
        SrcZip srcZip = zipModel.getSrcZip();

        return Quietly.doRuntime(() -> srcZip.isSolid() ? new SolidConsecutiveAccessDataInput(srcZip)
                                                        : new SplitConsecutiveAccessDataInput(srcZip));
    }

    protected void setFileAttributes(Path path, ZipEntry zipEntry) throws IOException {
        if (zipEntry.getExternalFileAttributes() != null)
            zipEntry.getExternalFileAttributes().apply(path);
    }

    protected void setFileLastModifiedTime(Path path, ZipEntry zipEntry) throws IOException {
        long lastModifiedTime = DosTimestampConverterUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
        Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
    }

    protected OutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(parent))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);
        return Files.newOutputStream(file);
    }

}
