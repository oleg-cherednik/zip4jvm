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
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@RequiredArgsConstructor
final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;

    // @NotNull
    public Map<Path, String> getRelativeContent(Path path) {
        return getRelativeContent(path, PathUtils.getName(path));
    }

    // @NotNull
    public Map<Path, String> getRelativeContent(Path path, String pathName) {
        if (!Files.exists(path))
            return Collections.emptyMap();

        boolean symlink = Files.isSymbolicLink(path);

        if (symlink && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return Collections.emptyMap();

        if (symlink)
            return getSymlinkRelativeContent(path, pathName);
        if (Files.isRegularFile(path))
            return getRegularFileRelativeContent(path, pathName);
        if (Files.isDirectory(path))
            return getDirectoryRelativeContent(path, pathName);

        return Collections.emptyMap();
    }

    private Map<Path, String> getSymlinkRelativeContent(Path symlink, String symlinkName) {
        assert Files.exists(symlink);
        assert Files.isSymbolicLink(symlink);

        Path symlinkTarget = getSymlinkTarget(symlink);
        return getRelativeContent(symlinkTarget, symlinkName);
    }

    private static Map<Path, String> getRegularFileRelativeContent(Path file, String fileName) {
        assert Files.exists(file);
        assert Files.isRegularFile(file);

        if (PathUtils.DS_STORE.equalsIgnoreCase(fileName)
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getName(file)))
            return Collections.emptyMap();

        return Collections.singletonMap(file, ZipUtils.normalizeFileName(fileName));
    }

    private Map<Path, String> getDirectoryRelativeContent(Path dir, String dirName) {
        assert Files.exists(dir);
        assert Files.isDirectory(dir);

        Map<Path, String> map = new TreeMap<>();

        for (Path child : PathUtils.getDirectoryContent(dir))
            getRelativeContent(child).forEach((p, fileName) -> map.put(p, dirName + '/' + fileName));

        return map.isEmpty() ? Collections.singletonMap(dir, dirName) : Collections.unmodifiableMap(map);
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
