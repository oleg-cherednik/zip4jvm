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
import ru.olegcherednik.zip4jvm.io.in.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SolidRandomAccessDataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInputFile;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void extract(Path dstDir) throws IOException {
        for (ZipEntry zipEntry : zipModel.getZipEntries())
            extractEntry(dstDir, zipEntry, ZipEntry::getFileName);
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void extract(Path dstDir, String fileName) throws IOException {
        fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

        if (zipModel.hasEntry(fileName))
            extractEntry(dstDir,
                         zipModel.getZipEntryByFileName(fileName),
                         e -> FilenameUtils.getName(e.getFileName()));
        else {
            List<ZipEntry> subEntries = getEntriesWithFileNamePrefix(fileName + '/');

            if (subEntries.isEmpty())
                throw new Zip4jvmException("Zip entry not found: " + fileName);

            for (ZipEntry zipEntry : subEntries)
                extractEntry(dstDir, zipEntry, ZipEntry::getFileName);
        }
    }

    private List<ZipEntry> getEntriesWithFileNamePrefix(String fileNamePrefix) {
        return zipModel.getZipEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
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

    public static RandomAccessDataInput createDataInput(SrcZip srcZip) throws FileNotFoundException {
        return srcZip.isSolid() ? new SolidRandomAccessDataInputFile(srcZip)
                                : new SplitRandomAccessDataInputFile(srcZip);
    }

}
