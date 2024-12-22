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
package ru.olegcherednik.zip4jvm.engine.zip;

import ru.olegcherednik.zip4jvm.engine.np.NamedPath;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ZipSymlinkEngine {

    private static final char SLASH = '/';

    protected final ZipSymlink zipSymlink;
    protected final Map<Path, NamedPath> map = new LinkedHashMap<>();

    // @NotNull
    public List<NamedPath> list(List<NamedPath> namedPaths) {
        return namedPaths.stream()
                         .filter(namedPath -> Files.exists(namedPath.getPath()))
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

            assert Files.exists(namedPath.getPath());

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

    protected void listSymlink(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isSymlink();

        if (zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET) {
            Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
            queue.add(NamedPath.create(symlinkTarget, namedPath.getName()));
            return;
        }

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_UNIQUE_TARGET) {
            Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
            NamedPath symlinkTargetNamedPath = map.get(symlinkTarget);

            if (symlinkTargetNamedPath == null)
                queue.add(NamedPath.create(symlinkTarget, namedPath.getName()));
            else {
                int depth = getDepth(namedPath.getName());
                String symlinkTargetRelativePath = repeat("../", depth) + symlinkTargetNamedPath.getName();
                res.add(NamedPath.symlink(symlinkTarget, symlinkTargetRelativePath, namedPath.getName()));
            }

            return;
        }

        assert false : "not implemented";
    }

    protected static int getDepth(String name) {
        int res = 0;

        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) == SLASH)
                res++;

        return res;
    }

    protected static String repeat(String str, int total) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < total; i++)
            buf.append(str);

        return buf.toString();
    }

    protected void listRegularFile(NamedPath namedPath, List<NamedPath> res) {
        assert namedPath.isRegularFile() : namedPath;

        if (PathUtils.DS_STORE.equalsIgnoreCase(namedPath.getName())
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(namedPath.getPath())))
            return;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_UNIQUE_TARGET && map.containsKey(namedPath.getPath())) {
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

    protected void listDirectory(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isDirectory() : namedPath;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_UNIQUE_TARGET && map.containsKey(namedPath.getPath())) {
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

    // @NotNull
    public static Path getSymlinkTarget(Path symlink) {
        assert Files.exists(symlink) : symlink;
        assert Files.isSymbolicLink(symlink) : symlink;

        return Quietly.doRuntime(() -> {
            Path resSymlink = symlink;

            while (Files.isSymbolicLink(resSymlink)) {
                Path symlinkTarget = Files.readSymbolicLink(resSymlink);
                resSymlink = symlinkTarget.isAbsolute() ? Files.readSymbolicLink(resSymlink)
                                                        : resSymlink.getParent().resolve(symlinkTarget);
            }

            return resSymlink.toRealPath();
        });
    }

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
            Path target1 = getSymlinkTarget(one.getPath());
            Path target2 = getSymlinkTarget(two.getPath());
            return target1.compareTo(target2);
        }

        return 0;
    };

    public static void createRelativeSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, symlink.getParent().relativize(target));
    }

    public static void createAbsoluteSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, target);
    }

    protected static final Comparator<NamedPath> SORT_PATH = Comparator.comparing(NamedPath::getName);
    protected static final Comparator<NamedPath> SORT_PATHS = SORT_SYMLINK.reversed()
                                                                          .thenComparing(SORT_DIR)
                                                                          .thenComparing(SORT_SYMLINK_TARGET)
                                                                          .thenComparing(SORT_PATH);

}
