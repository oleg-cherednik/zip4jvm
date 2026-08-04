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
package ru.olegcherednik.zip4jvm.engine.symlink.strategy;

import ru.olegcherednik.zip4jvm.engine.np.NamedPath;
import ru.olegcherednik.zip4jvm.engine.symlink.ZipSymlinkEngine;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Oleg Cherednik
 * @since 04.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReplaceWithUniqueTargetZipSymlinkStrategy extends ZipSymlinkStrategy {

    public static final ReplaceWithUniqueTargetZipSymlinkStrategy INSTANCE = new ReplaceWithUniqueTargetZipSymlinkStrategy();

    private final Map<Path, NamedPath> map = new LinkedHashMap<>();

    // ---------- ZipSymlinkStrategy ----------

    @Override
    protected void listSymlink(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isSymlink();

        Path symlinkTarget = ZipSymlinkEngine.getSymlinkTarget(namedPath.getPath());
        NamedPath symlinkTargetNamedPath = map.get(symlinkTarget);

        if (symlinkTargetNamedPath == null)
            queue.add(NamedPath.create(symlinkTarget, namedPath.getName()));
        else {
            int depth = getDepth(namedPath.getName());
            String symlinkTargetRelativePath = repeat("../", depth) + symlinkTargetNamedPath.getName();
            res.add(NamedPath.symlink(symlinkTarget, symlinkTargetRelativePath, namedPath.getName()));
        }
    }

    @Override
    protected void listRegularFile(NamedPath namedPath, List<NamedPath> res) {
        assert namedPath.isRegularFile() : namedPath;

        if (isDsStoreRegularFile(namedPath))
            return;

        if (map.containsKey(namedPath.getPath())) {
            NamedPath np = map.get(namedPath.getPath());
            int depth = getDepth(namedPath.getName());
            String symlinkTargetRelativePath = repeat("../", depth) + np.getName();
            res.add(NamedPath.symlink(np.getPath(), symlinkTargetRelativePath, namedPath.getName()));
        } else {
            NamedPath np = NamedPath.create(namedPath.getPath(), ZipUtils.normalizeFileName(namedPath.getName()));
            res.add(np);

            map.put(np.getPath(), np);
        }
    }

    @Override
    protected void listDirectory(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isDirectory() : namedPath;

        if (map.containsKey(namedPath.getPath())) {
            NamedPath np = map.get(namedPath.getPath());
            int depth = Math.max(0, getDepth(namedPath.getName()) - 1);
            String symlinkTargetRelativePath = repeat("../", depth) + np.getName();
            res.add(NamedPath.symlink(np.getPath(), symlinkTargetRelativePath, namedPath.getName()));
        } else {
            boolean empty = true;

            for (Path path : PathUtils.list(namedPath.getPath())) {
                empty = false;
                String name = namedPath.getName() + PathUtils.getName(path);
                queue.add(NamedPath.create(path, name));
            }

            if (empty)
                res.add(namedPath);

            map.put(namedPath.getPath(), namedPath);
        }
    }

}
