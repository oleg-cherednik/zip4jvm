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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public final class ZipSymlinkEngine {

    private final ZipSymlink zipSymlink;
    private final Map<Path, String> pathFileName = new HashMap<>();

    public Map<Path, String> getRelativeContent(Path path) {
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

    private Map<Path, String> getSymlinkRelativeContent(Path symlink, String symlinkName) {
        assert Files.exists(symlink);
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

    private static Map<Path, String> getRegularFileRelativeContent(Path file, String fileName) {
        assert Files.exists(file);
        assert Files.isRegularFile(file);

        if (PathUtils.DS_STORE.equalsIgnoreCase(fileName)
                || PathUtils.DS_STORE.equalsIgnoreCase(PathUtils.getFileName(file)))
            return Collections.emptyMap();

//        if (pathFileName.put(path.toAbsolutePath(), fileName) != null)
//            throw new Zip4jvmException("Duplicate path");

        return Collections.singletonMap(file, ZipUtils.normalizeFileName(fileName));
    }

    private Map<Path, String> getDirectoryRelativeContent(Path path) {
        assert Files.exists(path);
        assert Files.isDirectory(path);

        if (PathUtils.isEmptyDirectory(path))
            return Collections.singletonMap(path, PathUtils.getFileName(path));

        Map<Path, String> map1 = new TreeMap<>();

        for (Path child : PathUtils.getDirectoryContent(path)) {
            Map<Path, String> map = new TreeMap<>(getRelativeContent(child));

            for (Path other : map.keySet()) {
                map.put(other, path.getFileName().toString() + '/' + map.get(other));
            }

            pathFileName.putAll(map);
            map1.putAll(map);
        }

        return Collections.unmodifiableMap(map1);
    }

    private static List<Path> foo(Path path) {
        try {
            return Files.walk(path)
                        .filter(Files::exists)
                        .collect(Collectors.toList());
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/Users/o.cherednik/Documents/zip4jvm/foo/src/symlink/");
        foo(path).forEach(System.out::println);

    }

}
