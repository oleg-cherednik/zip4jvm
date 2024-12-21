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
package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SolidRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
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

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public final class UnzipEngine extends BaseUnzipEngine implements ZipFile.Reader {

    private final ZipModel zipModel;

    public UnzipEngine(SrcZip srcZip, UnzipSettings settings) {
        super(settings.getPasswordProvider());
        zipModel = ZipModelBuilder.read(srcZip, settings.getCharsetCustomizer(), passwordProvider);
    }

    private Map<String, Function<ZipEntry, String>> getEntryNamesByPrefix(Set<String> fileNames) {
        Map<String, Function<ZipEntry, String>> map = new HashMap<>();

        for (String fileName : fileNames) {
            fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

            if (zipModel.hasEntry(fileName))
                map.put(fileName, e -> FilenameUtils.getName(e.getFileName()));
            else
                for (ZipEntry zipEntry : getEntriesWithFileNamePrefix(fileName + '/'))
                    map.put(fileName, ZipEntry::getFileName);
        }

        return map;
    }

    private Set<String> getEntriesNamesByPrefix(String prefix) {
        return zipModel.getEntryNames().stream()
                       .filter(entryName -> entryName.startsWith(prefix))
                       .collect(Collectors.toSet());
    }

    private List<ZipEntry> getEntriesWithFileNamePrefix(String fileNamePrefix) {
        return zipModel.getZipEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
    }

    // ---------- ZipFile.Reader ----------

    //    @Override
    //    public void extract(Path dstDir) throws IOException {
    //        for (ZipEntry zipEntry : zipModel.getZipEntries())
    //            extractEntry(dstDir, zipEntry, ZipEntry::getFileName);
    //    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void extract(Path dstDir, String fileName) throws IOException {
        fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

        if (zipModel.hasEntry(fileName))
            extractEntry(dstDir,
                         zipModel.getZipEntryByFileName(fileName),
                         e -> FilenameUtils.getName(e.getFileName()));
        else
            for (ZipEntry zipEntry : getEntriesWithFileNamePrefix(fileName + '/'))
                extractEntry(dstDir, zipEntry, ZipEntry::getFileName);
    }

    @Override
    public void extract(Path dstDir, Collection<String> fileNames) throws IOException {
        requireNotNull(fileNames, "UnzipEngine.fileNames");

        Map<String, Function<ZipEntry, String>> map = getEntryNamesByPrefix(new HashSet<>(fileNames));
        new UnzipStreamEngine(zipModel, passwordProvider).extract(dstDir, map);
    }

    @Override
    public ZipFile.Entry extract(String fileName) {
        return Quietly.doQuietly(() -> {
            ZipEntry zipEntry = zipModel.getZipEntryByFileName(ZipUtils.normalizeFileName(fileName));

            if (zipEntry == null)
                throw new FileNotFoundException("Entry '" + fileName + "' was not found");

            zipEntry.setPassword(passwordProvider.getFilePassword(zipEntry.getFileName()));
            return zipEntry.createImmutableEntry();
        });
    }

    @Override
    public String getComment() {
        return zipModel.getComment();
    }

    @Override
    public boolean isSplit() {
        return zipModel.isSplit();
    }

    @Override
    public boolean isZip64() {
        return zipModel.isZip64();
    }

    @Override
    @SuppressWarnings("PMD.UseDiamondOperator")
    public Iterator<ZipFile.Entry> iterator() {
        return new Iterator<ZipFile.Entry>() {
            private final Iterator<String> it = zipModel.getEntryNames().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ZipFile.Entry next() {
                return zipModel.getZipEntryByFileName(it.next()).createImmutableEntry();
            }
        };
    }

    public static RandomAccessDataInput createDataInput(SrcZip srcZip) throws IOException {
        return srcZip.isSolid() ? new SolidRandomAccessDataInput(srcZip)
                                : new SplitRandomAccessDataInput(srcZip);
    }

    @Getter
    @RequiredArgsConstructor
    public static final class En {

        private final String fileName;
        private final String outFileName;

    }

}
