package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEngineUtils {

    public static Map<Path, String> getRelativeContent(Path path) throws IOException {
        if (path == null || !Files.exists(path))
            return Collections.emptyMap();

        if (Files.isSymbolicLink(path))
            return getSymlinkRelativeContent(path, PathUtils.getFileName(path));
        if (Files.isRegularFile(path))
            return getRegularFileRelativeContent(path, PathUtils.getFileName(path));
        if (Files.isDirectory(path))
            return getDirectoryRelativeContent(path);

        return Collections.emptyMap();
    }

    private static Map<Path, String> getSymlinkRelativeContent(Path symlink, String symlinkName) throws IOException {
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

    private static Map<Path, String> getRegularFileRelativeContent(Path path, String fileName) {
        if (PathUtils.DS_STORE.equalsIgnoreCase(fileName)
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getFileName(path)))
            return Collections.emptyMap();

        return Collections.singletonMap(path, ZipUtils.normalizeFileName(fileName));
    }

    private static Map<Path, String> getDirectoryRelativeContent(Path path) throws IOException {
        if (PathUtils.isEmptyDirectory(path))
            return Collections.singletonMap(path, path.getFileName().toString());

        Map<Path, String> map = new TreeMap<>();

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(p -> Files.isRegularFile(p) || PathUtils.isEmptyDirectory(p))
                 .forEach(p -> map.computeIfAbsent(p, other -> PathUtils.getNormalizeRelativePath(path, other)));
        }

        return Collections.unmodifiableMap(map);
    }

}
