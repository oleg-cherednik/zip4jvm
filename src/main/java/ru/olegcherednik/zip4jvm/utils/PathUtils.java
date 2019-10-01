package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtils {

    public static Map<Path, String> getRelativeContent(Collection<Path> paths) throws IOException {
        requireExistedPaths(paths);

        Map<Path, String> pathFileName = new LinkedHashMap<>();

        for (Path path : paths) {
            if (Files.isRegularFile(path))
                pathFileName.put(path, ZipUtils.normalizeFileName(path.getFileName().toString()));
            else if (Files.isDirectory(path)) {
                if (isEmptyDirectory(path))
                    pathFileName.put(path, path.getFileName().toString());
                else {
                    Files.walk(path)
                         .filter(p -> Files.isRegularFile(p) || isEmptyDirectory(p))
                         .forEach(p -> pathFileName.putIfAbsent(p, ZipUtils.normalizeFileName(path.getParent().relativize(p).toString())));
                }
            }
        }

        return pathFileName;
    }

    private static boolean isEmptyDirectory(Path path) {
        try {
            return Files.isDirectory(path) && Files.list(path).count() == 0;
        } catch(IOException e) {
            return false;
        }
    }

    public static void requireExistedPaths(Collection<Path> paths) {
        paths.forEach(PathUtils::requireExistedPath);
    }

    public static void requireExistedPath(Path path) {
        if (path == null || !Files.exists(path) || !Files.isReadable(path))
            throw new PathNotExistsException(path);
    }
}
