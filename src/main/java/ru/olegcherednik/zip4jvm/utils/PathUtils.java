package ru.olegcherednik.zip4jvm.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.olegcherednik.zip4jvm.exception.Zip4jPathNotExistsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@UtilityClass
public class PathUtils {

    private static final Comparator<Path> SORT_DIR_FIRST = Comparator.comparingInt((ToIntFunction<Path>)path -> Files.isDirectory(path) ? 0 : 1)
                                                                     .thenComparing(Path::compareTo);

    @NonNull
    public static Map<Path, String> getRelativeContent(@NonNull Collection<Path> paths) throws IOException {
        Map<Path, String> pathFileName = new TreeMap<>(SORT_DIR_FIRST);

        for (Path path : paths) {
            if (Files.isRegularFile(path))
                pathFileName.put(path, path.getFileName().toString());
            else if (Files.isDirectory(path)) {
                if (isEmptyDirectory(path))
                    pathFileName.put(path, path.getFileName().toString());
                else {
                    Files.walk(path)
                         .filter(p -> Files.isRegularFile(p) || isEmptyDirectory(p))
                         .forEach(p -> pathFileName.putIfAbsent(p, path.getParent().relativize(p).toString()));
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

    public static void requireExistedPaths(@NonNull Collection<Path> paths) {
        paths.forEach(PathUtils::requireExistedPath);
    }

    public static void requireExistedPath(@NonNull Path path) {
        if (!Files.exists(path) || !Files.isReadable(path))
            throw new Zip4jPathNotExistsException(path);
    }
}
