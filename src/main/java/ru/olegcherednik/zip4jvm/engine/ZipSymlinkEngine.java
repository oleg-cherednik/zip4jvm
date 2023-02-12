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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;
    private final Map<Path, String> pathFileName = new HashMap<>();

    public Map<Path, String> getRelativeContent(Path path) throws IOException {
        if (path == null || !Files.exists(path))
            return Collections.emptyMap();

        boolean symlink = Files.isSymbolicLink(path);

        if (symlink && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
            return Collections.emptyMap();

        if (symlink)
            return getSymlinkRelativeContent(path, PathUtils.getFileName(path));
        if (Files.isRegularFile(path))
            return getRegularFileRelativeContent(path, PathUtils.getFileName(path));
        if (Files.isDirectory(path))
            return getDirectoryRelativeContent(path);

        return Collections.emptyMap();
    }

    private Map<Path, String> getSymlinkRelativeContent(Path symlink, String symlinkName) throws IOException {
        assert Files.isSymbolicLink(symlink);

        Path symlinkTarget = PathUtils.getSymbolicLinkTarget(symlink);

        if (Files.isRegularFile(symlinkTarget))
            return getRegularFileRelativeContent(symlinkTarget, symlinkName);
        if (Files.isDirectory(symlinkTarget)) {
            Map<Path, String> pathFileName = new HashMap<>();

            for (Path child : PathUtils.getDirectoryContent(symlinkTarget)) {
                Map<Path, String> map = new TreeMap<>(getRelativeContent(child));

                for (Path key : map.keySet())
                    map.put(key, symlinkName + '/' + map.get(key));

                pathFileName.putAll(map);
            }

            return Collections.unmodifiableMap(pathFileName);
        }

        log.warn("not supported symlink type: " + symlink);
        return Collections.emptyMap();
    }

    private Map<Path, String> getRegularFileRelativeContent(Path path, String fileName) {
        if (PathUtils.DS_STORE.equalsIgnoreCase(fileName)
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getFileName(path)))
            return Collections.emptyMap();

//        if (pathFileName.put(path.toAbsolutePath(), fileName) != null)
//            throw new Zip4jvmException("Duplicate path");

        return Collections.singletonMap(path, ZipUtils.normalizeFileName(fileName));
    }

    private static Map<Path, String> getDirectoryRelativeContent(Path path) throws IOException {
        if (PathUtils.isEmptyDirectory(path))
            return Collections.singletonMap(path, PathUtils.getFileName(path));

        Map<Path, String> map = new TreeMap<>();

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(p -> Files.isRegularFile(p) || PathUtils.isEmptyDirectory(p))
                 .forEach(p -> map.computeIfAbsent(p, other -> PathUtils.getNormalizeRelativePath(path, other)));
        }

        return Collections.unmodifiableMap(map);
    }

}
