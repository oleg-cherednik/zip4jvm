package ru.olegcherednik.zip4jvm;

import lombok.Builder;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Builder
public final class ZipMisc {

    @NonNull
    private final Path zipFile;
    private final char[] password;

    public static void setComment(@NonNull Path zip, String comment) throws IOException {
        try (ZipFile.Writer zipFile = ZipFile.write(zip)) {
            zipFile.setComment(comment);
        }
    }

    public static String getComment(@NonNull Path zip) throws IOException {
        return ZipFile.read(zip).getComment();
    }

    public static Set<String> getEntryNames(@NonNull Path zip) throws IOException {
        return ZipFile.read(zip).getEntryNames();
    }

    public static void removeEntry(@NonNull Path zip, @NonNull String entryName) throws IOException {
        removeEntry(zip, Collections.singleton(entryName));
    }

    public static void removeEntry(@NonNull Path zip, @NonNull Collection<String> entryNames) throws IOException {
        try (ZipFile.Writer zipFile = ZipFile.write(zip)) {
            zipFile.remove(entryNames);
        }
    }

    public static boolean isSplit(@NonNull Path zip) throws IOException {
        return ZipFile.read(zip).isSplit();
    }

    public static void merge(@NonNull Path src, @NonNull Path dest) throws IOException {
        ZipFile.Reader reader = ZipFile.read(src);

        ZipFileWriterSettings settings = ZipFileWriterSettings.builder()
                                                              .comment(reader.getComment())
                                                              .zip64(reader.isZip64()).build();

        try (ZipFile.Writer zipFile = ZipFile.write(dest, settings)) {
            zipFile.copy(src);
        }
    }

}
