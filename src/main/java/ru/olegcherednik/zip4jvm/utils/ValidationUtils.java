package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;
import ru.olegcherednik.zip4jvm.exception.RealBigZip64NotSupportedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtils {

    public static void requireLessOrEqual(long value, long max, String type) {
        if (value > max)
            throw new IllegalArgumentException(String.format("Parameter should be less or equal to %d: %s", max, type));
    }

    public static void requireZeroOrPositive(long value, String type) {
        if (value < 0)
            throw new IllegalArgumentException("Parameter should be positive: " + type);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static void realBigZip64(long value, String type) {
        if (value < 0)
            throw new RealBigZip64NotSupportedException(value, type);
    }

    public static <T> T requireNotNull(T obj, String name) {
        return Optional.ofNullable(obj).orElseThrow(() -> new IllegalArgumentException("Parameter should not be null: " + name));
    }

    public static void requireExists(Path path) {
        if (!Files.exists(path))
            throw new PathNotExistsException(path);
    }

    public static void requireRegularFile(Path path, String name) {
        if (Files.exists(path) && !Files.isRegularFile(path))
            throw new IllegalArgumentException("Path should be a regular file: " + name);
    }

    public static void requireDirectory(Path path, String name) {
        if (Files.exists(path) && !Files.isDirectory(path))
            throw new IllegalArgumentException("Path should be a directory: " + name);
    }

    public static void requireNotEmpty(char[] arr, String name) {
        if (ArrayUtils.isEmpty(arr))
            throw new IllegalArgumentException("Parameter should be not empty: " + name);
    }

    public static String requireNotBlank(String str, String name) {
        if (StringUtils.isBlank(str))
            throw new IllegalArgumentException("Parameter should be not blank: " + name);
        return str;
    }

    public static <T> List<T> requireNotEmpty(List<T> obj, String name) {
        if (CollectionUtils.isEmpty(obj))
            throw new IllegalArgumentException("Collection should be not empty: " + name);
        return obj;
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> obj, String name) {
        if (CollectionUtils.isEmpty(obj))
            throw new IllegalArgumentException("Collection should be not empty: " + name);
        return obj;
    }

    public static void requireMaxSizeComment(String str, int maxLength) {
        if (StringUtils.length(str) > maxLength)
            throw new IllegalArgumentException("File comment should be " + maxLength + " characters maximum");
    }

}
