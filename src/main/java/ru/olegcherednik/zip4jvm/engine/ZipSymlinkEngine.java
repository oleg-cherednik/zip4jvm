package ru.olegcherednik.zip4jvm.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.NamedPath;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;

    // @NotNull
    public List<NamedPath> getRelativeContent(Path path) {
        return getRelativeContent(path, PathUtils.getName(path));
    }

    // @NotNull
    public List<NamedPath> getRelativeContent(Path path, String pathName) {
        if (!Files.exists(path))
            return Collections.emptyList();

        boolean symlink = Files.isSymbolicLink(path);

        if (symlink && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return Collections.emptyList();

        if (symlink)
            return getSymlinkRelativeContent(path, pathName);
        if (Files.isRegularFile(path))
            return getRegularFileRelativeContent(path, pathName);
        if (Files.isDirectory(path))
            return getDirectoryRelativeContent(path, pathName);

        return Collections.emptyList();
    }

    private List<NamedPath> getSymlinkRelativeContent(Path symlink, String symlinkName) {
        assert Files.exists(symlink);
        assert Files.isSymbolicLink(symlink);

        Path symlinkTarget = getSymlinkTarget(symlink);
        return getRelativeContent(symlinkTarget, symlinkName);
    }

    private static List<NamedPath> getRegularFileRelativeContent(Path file, String fileName) {
        assert Files.exists(file);
        assert Files.isRegularFile(file);

        if (PathUtils.DS_STORE.equalsIgnoreCase(fileName)
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(file)))
            return Collections.emptyList();

        return Collections.singletonList(new NamedPath(file, ZipUtils.normalizeFileName(fileName)));
    }

    private List<NamedPath> getDirectoryRelativeContent(Path dir, String dirName) {
        assert Files.exists(dir);
        assert Files.isDirectory(dir);

        Map<Path, String> map = new TreeMap<>();

        for (Path child : PathUtils.getDirectoryContent(dir))
            getRelativeContent(child).forEach(namedPath -> map.put(namedPath.getPath(),
                                                                   dirName + '/' + namedPath.getName()));

        if (map.isEmpty())
            return Collections.singletonList(new NamedPath(dir, dirName));

        return map.entrySet().stream()
                  .map(entry -> new NamedPath(entry.getKey(), entry.getValue()))
                  .collect(Collectors.toList());
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
