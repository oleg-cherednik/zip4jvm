package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnzipIt {

    public static void extract(@NonNull Path zip, @NonNull Path destDir) throws IOException {
        ZipFile.read(zip).extract(destDir);
    }

    public static void extract(@NonNull Path zip, @NonNull Path destDir, @NonNull String fileName) throws IOException {
        ZipFile.read(zip).extract(destDir, fileName);
    }

    public static void extract(@NonNull Path zip, @NonNull Path destDir, @NonNull Collection<String> fileNames) throws IOException {
        ZipFile.read(zip).extract(destDir, fileNames);
    }

    public static void extract(@NonNull Path zip, @NonNull Path destDir, @NonNull Function<String, char[]> createPassword) throws IOException {
        ZipFile.read(zip, createPassword).extract(destDir);
    }

    public static void extract(@NonNull Path zip, @NonNull Path destDir, @NonNull String fileName, @NonNull Function<String, char[]> createPassword)
            throws IOException {
        ZipFile.read(zip, createPassword).extract(destDir, fileName);
    }

    public static void extract(@NonNull Path zip, @NonNull Path destDir, @NonNull Collection<String> fileNames,
            @NonNull Function<String, char[]> createPassword) throws IOException {
        ZipFile.read(zip, createPassword).extract(destDir, fileNames);
    }

}
