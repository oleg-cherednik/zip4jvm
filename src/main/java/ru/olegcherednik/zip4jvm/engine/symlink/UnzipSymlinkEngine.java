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
package ru.olegcherednik.zip4jvm.engine.symlink;

import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates a symlink on the file system while extracting a symlink entry from a zip archive.
 *
 * @author Oleg Cherednik
 * @since 04.08.2026
 */
@Slf4j
@RequiredArgsConstructor
public class UnzipSymlinkEngine {

    private final boolean ignoreSymlink;

    public void extractSymlink(Path symlink, ZipEntry zipEntry) {
        if (ignoreSymlink) {
            log.debug("'ignoreSymlink = true' => not symlink unizp");
            return;
        }

        String target = readSymlinkTarget(zipEntry);

        if (target.contains(":"))
            // TODO absolute windows symlink
            log.debug("windows absolute symlink is not supported");
        else if (target.charAt(0) == PathUtils.SLASH)
            createAbsoluteSymlink(symlink, Paths.get(target));
        else
            createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    // ---------- static ----------

    protected static String readSymlinkTarget(ZipEntry zipEntry) {
        return Quietly.doRuntime(() -> IOUtils.toString(zipEntry.createInputStream(), Charsets.UTF_8));
    }

    protected static void createRelativeSymlink(Path symlink, Path target) {
        if (PathUtils.isUnder(symlink.getParent(), target))
            Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, symlink.getParent().relativize(target)));
    }

    protected static void createAbsoluteSymlink(Path symlink, Path target) {
        if (PathUtils.isUnder(symlink.getParent(), target))
            Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, target));
    }

}
