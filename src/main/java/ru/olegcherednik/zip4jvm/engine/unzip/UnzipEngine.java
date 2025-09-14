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
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SolidRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public final class UnzipEngine implements ZipFile.Reader {

    private final UnzipSettings settings;
    private final ZipModel zipModel;
    private final RecursiveEngine recursiveEngine;

    public UnzipEngine(SrcZip srcZip, UnzipSettings settings) {
        this.settings = settings;
        // TODO here we need a tiny part of zip (comment, slip, zip64)
        zipModel = createZipModel(srcZip, settings);
        recursiveEngine = new RecursiveEngine(settings.getRecursiveLevel());
    }

    private static ZipModel createZipModel(SrcZip srcZip, UnzipSettings settings) {
        CharsetProvider charsetProvider = settings.getCharsetProvider();
        PasswordProvider passwordProvider = settings.getPasswordProvider();
        return ZipModelBuilder.read(srcZip, charsetProvider, passwordProvider);
    }

    private static UnzipExtractEngine createUnzipExtractEngine(UnzipSettings settings,
                                                               ZipModel zipModel,
                                                               BiConsumer<Path, ZipEntry> onZipEntry) {
        PasswordProvider passwordProvider = settings.getPasswordProvider();

        if (settings.getAsyncThreads() == UnzipSettings.ASYNC_THREADS_OFF)
            return new UnzipExtractEngine(passwordProvider, zipModel, onZipEntry);

        int totalThreads = settings.getAsyncThreads();
        return new UnzipExtractAsyncEngine(passwordProvider, zipModel, totalThreads, onZipEntry);
    }

    // ---------- ZipFile.Reader ----------

    @Override
    public void extract(Path dstDir) {
        extract(dstDir, Collections.emptySet());
    }

    @Override
    public void extract(Path dstDir, String fileName) {
        extract(dstDir, Collections.singleton(fileName));
    }

    @Override
    public void extract(Path dstDir, Collection<String> fileNamePrefixes) {
        recursiveEngine.setRootPath(dstDir);

        UnzipExtractEngine unzipExtractEngine = createUnzipExtractEngine(settings, zipModel, recursiveEngine);
        unzipExtractEngine.extractByFileNamePrefix(dstDir, fileNamePrefixes);

        while (recursiveEngine.hasNext()) {
            SrcZip srcZip = recursiveEngine.next();
            Path path = srcZip.getPath();
            String dirName = FilenameUtils.getBaseName(path.getFileName().toString());
            unzipExtractEngine = createUnzipExtractEngine(settings, createZipModel(srcZip, settings), recursiveEngine);
            unzipExtractEngine.extractByFileNamePrefix(path.getParent().resolve(dirName), fileNamePrefixes);
            PathUtils.deleteIfExists(srcZip);
        }
    }

    @Override
    public ZipFile.Entry extract(String fileName) {
        UnzipExtractEngine unzipExtractEngine = createUnzipExtractEngine(settings, zipModel, recursiveEngine);
        return unzipExtractEngine.extractByFileNameMatch(fileName);
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
            private final Iterator<ZipEntry> it = zipModel.absOffsAscIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ZipFile.Entry next() {
                return it.next().createImmutableEntry();
            }
        };
    }

    public static RandomAccessDataInput createRandomAccessDataInput(SrcZip srcZip) {
        return Quietly.doRuntime(() -> srcZip.isSolid() ? new SolidRandomAccessDataInput(srcZip)
                                                        : new SplitRandomAccessDataInput(srcZip));
    }

}
