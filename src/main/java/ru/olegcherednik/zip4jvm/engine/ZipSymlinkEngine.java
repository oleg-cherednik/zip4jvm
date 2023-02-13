package ru.olegcherednik.zip4jvm.engine;

import com.sun.org.apache.bcel.internal.generic.ARETURN;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;

    // @NotNull
    public List<NamedPath> list(Path path) {
        return Files.exists(path) ? dfs(new NamedPath(path)) : Collections.emptyList();
    }

    // @NotNull
    public List<NamedPath> list(NamedPath namedPath) {
        return namedPath.isExist() ? dfs(namedPath) : Collections.emptyList();
    }

    private List<NamedPath> dfs(NamedPath root) {
        assert root.isExist();

        Queue<NamedPath> queue = new LinkedList<>();
        queue.add(root);

        List<NamedPath> res = new ArrayList<>();

        while (!queue.isEmpty()) {
            NamedPath namedPath = queue.remove();

            if (!namedPath.isExist())
                continue;

            if (namedPath.isSymlink())
                listSymlink(namedPath, queue);
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

    private void listSymlink(NamedPath namedPath, Queue<NamedPath> queue) {
        assert namedPath.isExist();
        assert namedPath.isSymlink();

        if (zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return;
        if (zipSymlink == ZipSymlink.REPLACE_SYMLINK_WITH_TARGET) {
            Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
            queue.add(new NamedPath(symlinkTarget, namedPath.getName()));
        } else
            throw new Zip4jvmException(String.format("zipSymlink '%s' not supported", zipSymlink));
    }

    private static void listRegularFile(NamedPath namedPath, List<NamedPath> res) {
        assert namedPath.isExist();
        assert namedPath.isRegularFile();

        if (PathUtils.DS_STORE.equalsIgnoreCase(namedPath.getName())
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(namedPath.getPath())))
            return;

        res.add(new NamedPath(namedPath.getPath(), ZipUtils.normalizeFileName(namedPath.getName())));
    }

    private static void listDirectory(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isExist();
        assert namedPath.isDirectory();

        boolean empty = true;

        for (Path path : PathUtils.list(namedPath.getPath())) {
            empty = false;
            String name = namedPath.getName() + '/' + PathUtils.getName(path);
            queue.add(new NamedPath(path, name));
        }

        if (empty)
            res.add(namedPath);
    }

    // @NotNull
    public static Path getSymlinkTarget(Path symlink) {
        assert Files.exists(symlink);
        assert Files.isSymbolicLink(symlink);

        try {
            while (Files.isSymbolicLink(symlink)) {
                Path symlinkTarget = Files.readSymbolicLink(symlink);
                symlink = symlinkTarget.isAbsolute() ? Files.readSymbolicLink(symlink)
                                                     : symlink.getParent().resolve(symlinkTarget);
            }

            return symlink;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
