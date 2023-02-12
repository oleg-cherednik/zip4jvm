package ru.olegcherednik.zip4jvm.engine;

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
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;

    // @NotNull
    public List<NamedPath> list(Path path) {
        return Files.exists(path) ? list(new NamedPath(path)) : Collections.emptyList();
    }

    // @NotNull
    public List<NamedPath> list(NamedPath namedPath) {
        if (!namedPath.isExist())
            return Collections.emptyList();

        if (namedPath.isSymlink() && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return Collections.emptyList();

        Queue<NamedPath> queue = new LinkedList<>();
        queue.add(namedPath);

        List<NamedPath> res = new ArrayList<>();

        while (!queue.isEmpty()) {
            namedPath = queue.remove();

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

        return res.stream().sorted(NamedPath.SORT_BY_NAME_ASC).collect(Collectors.toList());
    }

    private static void listSymlink(NamedPath namedPath, Queue<NamedPath> queue) {
        assert namedPath.isExist();
        assert namedPath.isSymlink();

        Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
        queue.add(new NamedPath(symlinkTarget, namedPath.getName()));
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
        assert Files.isSymbolicLink(symlink);
        assert Files.exists(symlink) : "Symlink target should be real";

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
