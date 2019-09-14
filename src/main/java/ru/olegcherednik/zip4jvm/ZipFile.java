package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReaderSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * ZipFile real-time implementation.
 * <br>
 * When create new instance of this class:
 * <ul>
 * <li><i>zip file exists</i> - open zip archive</li>
 * <li><i>zip file not exists</i> - create new empty zip archive</li>
 * </ul>
 * <p>
 * To close zip archive correctly, do call {@link ZipFile.Writer#close()} method.
 * <pre>
 * try (ZipFile zipFile = new ZipFile(Paths.get("~/src.zip"))) {
 *     zipFile.addEntry(...);
 * }
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@UtilityClass
public final class ZipFile {

    public ZipFile.Reader read(@NonNull Path zip) throws IOException {
        return read(zip, ZipFileReaderSettings.builder().build());
    }

    public ZipFile.Reader read(@NonNull Path zip, ZipFileReaderSettings settings) throws IOException {
        return new ZipFileReader(zip, settings);
    }

    public ZipFile.Writer write(@NonNull Path zip) throws IOException {
        return write(zip, ZipFileWriterSettings.builder().build());
    }

    public ZipFile.Writer write(@NonNull Path zip, @NonNull ZipFileWriterSettings zipFileSettings) throws IOException {
        return new ZipFileWriter(zip, zipFileSettings);
    }

    public interface Reader {

        void extract(@NonNull Path destDir) throws IOException;

        void extract(@NonNull Path destDir, @NonNull Collection<String> fileNames) throws IOException;

        void extract(@NonNull Path destDir, @NonNull String fileName) throws IOException;

        @NonNull
        InputStream extract(@NonNull String fileName) throws IOException;

        String getComment();

        @NonNull
        Set<String> getEntryNames();

        boolean isSplit();
    }

    public interface Writer extends Closeable {

        void add(@NonNull Path path) throws IOException;

        void add(@NonNull Path path, @NonNull ZipEntrySettings entrySettings) throws IOException;

        void add(@NonNull Collection<Path> paths) throws IOException;

        void add(@NonNull Collection<Path> paths, @NonNull ZipEntrySettings entrySettings) throws IOException;

        void remove(@NonNull String prefixEntryName) throws FileNotFoundException;

        default void remove(@NonNull Collection<String> prefixEntryNames) throws FileNotFoundException {
            for (String prefixEntryName : prefixEntryNames)
                remove(prefixEntryName);
        }

        void setComment(String comment);
    }

}
