/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * @author Oleg Cherednik
 * @since 14.09.2025
 */
@RequiredArgsConstructor
final class RecursiveEngine implements BiConsumer<Path, ZipEntry>, Iterator<SrcZip> {

    private final int recursiveLevel;
    private final Queue<SrcZip> zipQueue = new LinkedList<>();
    @Setter
    private Path rootPath;

    // ---------- Iterator ----------

    @Override
    public boolean hasNext() {
        return !zipQueue.isEmpty();
    }

    @Override
    public SrcZip next() {
        if (!hasNext())
            throw new NoSuchElementException("RecursiveEngine.zipQueue");
        return zipQueue.remove();
    }

    // ---------- static ----------

    private static int getRecursiveLevel(Path path) {
        int level = 1;
        String str = path.toString();

        // start from '1' to exclude '/' at the beginning
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (ch == PathUtils.SLASH || ch == PathUtils.BACK_SLASH)
                level++;
        }

        return level;
    }

    // ---------- Consumer ----------

    @Override
    public void accept(Path dstDir, ZipEntry zipEntry) {
        if (recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_OFF)
            return;
        if (!zipEntry.isRegularFile())
            return;
        if (!zipEntry.getFileName().endsWith(".zip"))
            return;

        Path zip = dstDir.resolve(zipEntry.getFileName());
        int level = getRecursiveLevel(rootPath.relativize(zip));

        if (recursiveLevel == UnzipSettings.RECURSIVE_LEVEL_MAX || level <= recursiveLevel)
            zipQueue.add(SrcZip.of(zip));
    }

}
