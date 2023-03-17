package ru.olegcherednik.zip4jvm.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.olegcherednik.zip4jvm.engine.np.NamedPath;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

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
public final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;
    private final Map<Path, NamedPath> map = new LinkedHashMap<>();

    // @NotNull
    public List<NamedPath> list(List<NamedPath> namedPaths) {
        return namedPaths.stream()
                         .filter(namedPath -> Files.exists(namedPath.getPath()))
                         .sorted(SORT_PATHS)
                         .map(this::dfs)
                         .flatMap(List::stream)
                         .collect(Collectors.toList());
    }

    private List<NamedPath> dfs(NamedPath root) {
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

    private void listSymlink(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isSymlink();

        if (zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET) {
            Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
            queue.add(NamedPath.create(symlinkTarget, namedPath.getName()));
            return;
        }

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET_NO_DUPLICATES) {
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

    private static int getDepth(String name) {
        int res = 0;

        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) == '/')
                res++;

        return res;
    }

    private static String repeat(String str, int total) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < total; i++)
            buf.append(str);

        return buf.toString();
    }

    private void listRegularFile(NamedPath namedPath, List<NamedPath> res) {
        assert namedPath.isRegularFile() : namedPath;

        if (PathUtils.DS_STORE.equalsIgnoreCase(namedPath.getName())
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(namedPath.getPath())))
            return;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET_NO_DUPLICATES && map.containsKey(namedPath.getPath())) {
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

    private void listDirectory(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isDirectory() : namedPath;

        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET_NO_DUPLICATES && map.containsKey(namedPath.getPath())) {
            NamedPath np = map.get(namedPath.getPath());
            int depth = getDepth(namedPath.getName());
            String symlinkTargetRelativePath = repeat("../", depth) + np.getName();
            res.add(NamedPath.symlink(np.getPath(), symlinkTargetRelativePath, namedPath.getName()));
        } else {
            boolean empty = true;

            for (Path path : PathUtils.list(namedPath.getPath())) {
                empty = false;
                String name = namedPath.getName() + '/' + PathUtils.getName(path);
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

        try {
            while (Files.isSymbolicLink(symlink)) {
                Path symlinkTarget = Files.readSymbolicLink(symlink);
                symlink = symlinkTarget.isAbsolute() ? Files.readSymbolicLink(symlink)
                                                     : symlink.getParent().resolve(symlinkTarget);
            }

            return symlink.toRealPath();
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static final Comparator<NamedPath> SORT_SYMLINK = (one, two) -> {
        if (one.isSymlink() ^ two.isSymlink())
            return one.isSymlink() ? -1 : 1;

        return 0;
    };

    private static final Comparator<NamedPath> SORT_DIR = (one, two) -> {
        if (one.isDirectory() ^ two.isDirectory())
            return one.isDirectory() ? -1 : 1;

        return 0;
    };

    private static final Comparator<NamedPath> SORT_SYMLINK_TARGET = (one, two) -> {
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

    private static final Comparator<NamedPath> SORT_PATH = Comparator.comparing(NamedPath::getName);
    private static final Comparator<NamedPath> SORT_PATHS = SORT_SYMLINK.reversed()
                                                                        .thenComparing(SORT_DIR)
                                                                        .thenComparing(SORT_SYMLINK_TARGET)
                                                                        .thenComparing(SORT_PATH);

}
