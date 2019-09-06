package ru.olegcherednik.zip4jvm.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static Map<Path, String> getRelativeContentMap(Collection<Path> paths) throws IOException {
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

    public static List<Path> getRelativeContent(Collection<Path> paths) throws IOException {
        List<Path> res = new LinkedList<>();
        Set<Path> unique = new HashSet<>();

        for (Path path : paths) {
            unique.add(path);

            if (Files.isRegularFile(path))
                res.add(path.getFileName());
            else if (Files.isDirectory(path)) {
                if (isEmptyDirectory(path))
                    res.add(path);
                else {
                    Files.walk(path)
                         .filter(p -> Files.isRegularFile(p) || isEmptyDirectory(p))
                         .forEach(p -> {
                             if (unique.contains(p))
                                 return;
                             res.add(path.getParent().relativize(p));
                             unique.add(p);
                         });
                }
            }
        }

        unique.clear();

        return res.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(res);
    }

    private static boolean isEmptyDirectory(Path path) {
        try {
            return Files.isDirectory(path) && Files.list(path).count() == 0;
        } catch(IOException e) {
            return false;
        }
    }

    public static void requireExistedPaths(@NonNull Collection<Path> paths) {
        if (paths.stream().anyMatch(path -> !Files.exists(path) || !Files.isReadable(path)))
            throw new Zip4jException("Some files are not exists or not readable");
    }
}
