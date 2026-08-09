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
import ru.olegcherednik.zip4jvm.model.settings.ZipSymlinkEnum;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 04.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ZipSymlinkStrategy {

    public static ZipSymlinkStrategy of(ZipSymlinkEnum zipSymlink) {
        if (zipSymlink == ZipSymlinkEnum.REPLACE_SYMLINK_WITH_TARGET)
            return ReplaceWithTargetZipSymlinkStrategy.INSTANCE;
        //        if (zipSymlink == ZipSymlinkEnum.REPLACE_SYMLINK_WITH_UNIQUE_TARGET)
        //            return ReplaceWithUniqueTargetZipSymlinkStrategy.INSTANCE;

        // In case of unknown strategy, just ignore symlink
        return IgnoreZipSymlinkStrategy.INSTANCE;
    }

    public List<NamedPath> list(List<NamedPath> namedPaths) {
        return namedPaths.stream()
                         .filter(NamedPath::isExists)
                         .sorted(SORT_PATHS)
                         .map(this::dfs)
                         .flatMap(List::stream)
                         .collect(Collectors.toList());
    }

    protected List<NamedPath> dfs(NamedPath root) {
        Queue<NamedPath> queue = new LinkedList<>();
        queue.add(root);

        List<NamedPath> res = new ArrayList<>();

        while (!queue.isEmpty()) {
            NamedPath namedPath = queue.remove();

            if (!namedPath.isExists())
                continue;

            if (namedPath.isSymlink())
                listSymlink(namedPath, queue, res);
            else if (namedPath.isRegularFile())
                listRegularFile(namedPath, res);
            else if (namedPath.isDirectory())
                listDirectory(namedPath, queue, res);
        }

        if (res.isEmpty())
            return Collections.emptyList();

        res.sort(NamedPath.SORT_BY_NAME_ASC);
        return Collections.unmodifiableList(res);
    }

    protected abstract void listSymlink(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res);

    protected void listRegularFile(NamedPath namedPath, List<NamedPath> res) {
        assert namedPath.isRegularFile() : namedPath;

        if (!isDsStoreRegularFile(namedPath))
            res.add(namedPath);
    }

    protected void listDirectory(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isDirectory() : namedPath;

        boolean empty = true;

        for (Path path : PathUtils.list(namedPath.getPath())) {
            empty = false;
            String name = namedPath.getName() + PathUtils.getName(path);
            queue.add(NamedPath.create(path, name));
        }

        if (empty)
            res.add(namedPath);
    }

    // ---------- static ----------

    protected static final Comparator<NamedPath> SORT_SYMLINK = (one, two) -> {
        if (one.isSymlink() ^ two.isSymlink())
            return one.isSymlink() ? -1 : 1;

        return 0;
    };

    protected static final Comparator<NamedPath> SORT_DIR = (one, two) -> {
        if (one.isDirectory() ^ two.isDirectory())
            return one.isDirectory() ? -1 : 1;

        return 0;
    };

    protected static final Comparator<NamedPath> SORT_SYMLINK_TARGET = (one, two) -> {
        if (one.isSymlink() && two.isSymlink()) {
            Path target1 = ZipSymlinkEngine.getSymlinkTarget(one.getPath());
            Path target2 = ZipSymlinkEngine.getSymlinkTarget(two.getPath());
            return target1.compareTo(target2);
        }

        return 0;
    };

    protected static final Comparator<NamedPath> SORT_PATHS = SORT_SYMLINK.reversed()
                                                                          .thenComparing(SORT_DIR)
                                                                          .thenComparing(SORT_SYMLINK_TARGET);

    public static boolean isDsStoreRegularFile(NamedPath namedPath) {
        return PathUtils.DS_STORE.equalsIgnoreCase(namedPath.getName())
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(namedPath.getPath()));
    }

    protected static int getDepth(String name) {
        int res = 0;

        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) == PathUtils.SLASH)
                res++;

        return res;
    }

    protected static String repeat(String str, int total) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < total; i++)
            buf.append(str);

        return buf.toString();
    }

}
