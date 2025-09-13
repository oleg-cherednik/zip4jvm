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

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public final class UnzipEngine implements ZipFile.Reader {

    private final SrcZip srcZip;
    private final UnzipSettings settings;

    private ZipModel zipModel;
    private UnzipExtractEngine unzipExtractEngine;
    private RecursiveSupport recursiveSupport = new RecursiveSupport();

    private static UnzipExtractEngine createUnzipExtractEngine(UnzipSettings settings,
                                                               ZipModel zipModel,
                                                               BiConsumer<Path, ZipEntry> onZipEntry) {
        PasswordProvider passwordProvider = settings.getPasswordProvider();

        if (settings.getAsyncThreads() == UnzipSettings.ASYNC_THREADS_OFF)
            return new UnzipExtractEngine(passwordProvider, zipModel, onZipEntry);

        int totalThreads = settings.getAsyncThreads();
        return new UnzipExtractAsyncEngine(passwordProvider, zipModel, totalThreads, onZipEntry);
    }

    private void init() {
        CharsetProvider charsetProvider = settings.getCharsetProvider();
        PasswordProvider passwordProvider = settings.getPasswordProvider();
        zipModel = ZipModelBuilder.read(srcZip, charsetProvider, passwordProvider);
        unzipExtractEngine = createUnzipExtractEngine(settings, zipModel, recursiveSupport);
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
    public void extract(Path dstDir, Collection<String> fileNames) {
        init();
        unzipExtractEngine.extract(dstDir, fileNames);

        while (!recursiveSupport.isEmpty()) {
            SrcZip srcZip1 = recursiveSupport.next();
            String dirName = FilenameUtils.getBaseName(srcZip1.getPath().getFileName().toString());
            new UnzipEngine(srcZip1, settings).extract(srcZip1.getPath().getParent().resolve(dirName), fileNames);
            srcZip1.getDisks().forEach(disk -> PathUtils.deleteIfExists(disk.getPath()));
        }
    }

    @Override
    public ZipFile.Entry extract(String fileName) {
        init();
        return unzipExtractEngine.extract(fileName);
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
