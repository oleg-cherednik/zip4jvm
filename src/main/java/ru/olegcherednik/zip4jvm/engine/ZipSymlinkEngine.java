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
        return Files.exists(path) ? getRelativeContent(new NamedPath(path))
                                  : Collections.emptyList();
    }

    // @NotNull
    public List<NamedPath> getRelativeContent(NamedPath namedPath) {
        if (!namedPath.isExist())
            return Collections.emptyList();

        if (namedPath.isSymlink() && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return Collections.emptyList();

        if (namedPath.isSymlink())
            return getSymlinkRelativeContent(namedPath);
        if (namedPath.isRegularFile())
            return getRegularFileRelativeContent(namedPath);
        if (namedPath.isDirectory())
            return getDirectoryRelativeContent(namedPath);

        return Collections.emptyList();
    }

    private List<NamedPath> getSymlinkRelativeContent(NamedPath namedPath) {
        assert namedPath.isExist();
        assert namedPath.isSymlink();

        Path symlinkTarget = getSymlinkTarget(namedPath.getPath());
        return getRelativeContent(new NamedPath(symlinkTarget, namedPath.getName()));
    }

    private static List<NamedPath> getRegularFileRelativeContent(NamedPath namedPath) {
        assert namedPath.isExist();
        assert namedPath.isRegularFile();

        if (PathUtils.DS_STORE.equalsIgnoreCase(namedPath.getName())
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(namedPath.getPath())))
            return Collections.emptyList();

        return Collections.singletonList(new NamedPath(namedPath.getPath(),
                                                       ZipUtils.normalizeFileName(namedPath.getName())));
    }

    private List<NamedPath> getDirectoryRelativeContent(NamedPath namedPath) {
        assert namedPath.isExist();
        assert namedPath.isDirectory();

        Map<Path, String> map = new TreeMap<>();

        for (Path child : PathUtils.getDirectoryContent(namedPath.getPath()))
            getRelativeContent(child).forEach(np -> map.put(np.getPath(), namedPath.getName() + '/' + np.getName()));

        if (map.isEmpty())
            return Collections.singletonList(namedPath);

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
