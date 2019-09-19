package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.v2.ZipEntryMeta;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipFile {

    public static ZipFile.Reader read(@NonNull Path zip) throws IOException {
        return read(zip, fileName -> null);
    }

    public static ZipFile.Reader read(@NonNull Path zip, @NonNull Function<String, char[]> createPassword) throws IOException {
        return new ZipFileReader(zip, createPassword);
    }

    public static ZipFile.Writer write(@NonNull Path zip) throws IOException {
        return write(zip, ZipFileSettings.builder().build());
    }

    public static ZipFile.Writer write(@NonNull Path zip, @NonNull Function<String, ZipEntrySettings> entrySettingsProvider) throws IOException {
        return write(zip, ZipFileSettings.builder().entrySettingsProvider(entrySettingsProvider).build());
    }

    public static ZipFile.Writer write(@NonNull Path zip, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        return new ZipFileWriter(zip, zipFileSettings);
    }

    public interface Reader extends Iterable<ZipEntry> {

        void extract(@NonNull Path destDir) throws IOException;

        void extract(@NonNull Path destDir, @NonNull String fileName) throws IOException;

        default void extract(@NonNull Path destDir, @NonNull Collection<String> fileNames) throws IOException {
            for (String fileName : fileNames)
                extract(destDir, fileName);
        }

        default Stream<ZipEntry> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        @NonNull
        InputStream extract(@NonNull String fileName) throws IOException;

        String getComment();

        @NonNull
        Set<String> getEntryNames();

        boolean isSplit();

        boolean isZip64();
    }

    public interface Writer extends Closeable {

        default void add(@NonNull Path path) throws IOException {
            add(Collections.singleton(path));
        }

        default void add(@NonNull Collection<Path> paths) throws IOException {
            for (Map.Entry<Path, String> entry : PathUtils.getRelativeContent(paths).entrySet())
                addMeta(ZipEntryMeta.of(entry.getKey(), entry.getValue()));
        }

        void addMeta(@NonNull ZipEntryMeta meta);

        default void addMeta(@NonNull Collection<ZipEntryMeta> metas) {
            metas.forEach(this::addMeta);
        }

        void remove(@NonNull String prefixEntryName) throws FileNotFoundException;

        default void remove(@NonNull Collection<String> prefixEntryNames) throws FileNotFoundException {
            for (String prefixEntryName : prefixEntryNames)
                remove(prefixEntryName);
        }

        void copy(@NonNull Path zip) throws IOException;

        void setComment(String comment);

    }

}
