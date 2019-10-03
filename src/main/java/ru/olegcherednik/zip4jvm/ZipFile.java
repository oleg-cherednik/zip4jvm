package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStream;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipFile {

    public static ZipIt zip(Path zip) {
        return new ZipIt(zip);
    }

    public static UnzipIt unzip(Path zip) {
        return new UnzipIt(zip);
    }

    public static Writer writer(Path zip, ZipFileSettings settings) throws IOException {
        return new ZipEngine(zip, settings);
    }

    public static Reader reader(Path zip, Function<String, char[]> passwordProvider) throws IOException {
        return new UnzipEngine(zip, passwordProvider);
    }

    @Getter
    public static final class Entry {

        @NonNull
        @Getter(AccessLevel.NONE)
        private final InputStreamSupplier inputStreamSup;
        @NonNull
        private final String fileName;
        private final long lastModifiedTime;
        @NonNull
        private final ExternalFileAttributes externalFileAttributes;
        private final boolean regularFile;

        public static Entry of(@NonNull Path path, @NonNull String fileName) throws IOException {
            return builder()
                    .inputStreamSup(Files.isRegularFile(path) ? () -> new FileInputStream(path.toFile()) : EmptyInputStreamSupplier.INSTANCE)
                    .fileName(fileName)
                    .lastModifiedTime(Files.getLastModifiedTime(path).toMillis())
                    .externalFileAttributes(ExternalFileAttributes.build(PROP_OS_NAME).readFrom(path))
                    .regularFile(Files.isRegularFile(path)).build();
        }

        public static Entry.Builder builder() {
            return new Entry.Builder();
        }

        private Entry(Entry.Builder builder) {
            fileName = ZipUtils.normalizeFileName(builder.fileName);
            inputStreamSup = builder.regularFile ? builder.inputStreamSup : () -> EmptyInputStream.INSTANCE;
            lastModifiedTime = builder.lastModifiedTime;
            externalFileAttributes = builder.externalFileAttributes;
            regularFile = builder.regularFile;
        }

        public InputStream getInputStream() throws IOException {
            return inputStreamSup.get();
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {

            private InputStreamSupplier inputStreamSup = EmptyInputStreamSupplier.INSTANCE;
            private String fileName;
            private long lastModifiedTime = System.currentTimeMillis();
            private ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
            private boolean regularFile = true;

            public Entry build() {
                return new Entry(this);
            }

            public Entry.Builder inputStreamSup(InputStreamSupplier inputStreamSup) {
                this.inputStreamSup = Optional.ofNullable(inputStreamSup).orElse(EmptyInputStreamSupplier.INSTANCE);
                return this;
            }

            public Entry.Builder fileName(@NonNull String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Entry.Builder lastModifiedTime(long lastModifiedTime) {
                this.lastModifiedTime = lastModifiedTime;
                return this;
            }

            public Entry.Builder externalFileAttributes(@NonNull ExternalFileAttributes externalFileAttributes) {
                this.externalFileAttributes = externalFileAttributes;
                return this;
            }

            public Entry.Builder regularFile(boolean regularFile) {
                this.regularFile = regularFile;
                return this;
            }

        }

    }

    public interface Writer extends Closeable {

        default void add(@NonNull Path path) throws IOException {
            add(Collections.singleton(path));
        }

        default void add(@NonNull Collection<Path> paths) throws IOException {
            for (Map.Entry<Path, String> entry : PathUtils.getRelativeContent(paths).entrySet())
                addEntry(Entry.of(entry.getKey(), entry.getValue()));
        }

        default void addEntry(@NonNull Collection<Entry> entries) {
            entries.forEach(this::addEntry);
        }

        void addEntry(@NonNull Entry entry);

        void remove(@NonNull String prefixEntryName) throws FileNotFoundException;

        default void remove(@NonNull Collection<String> prefixEntryNames) throws FileNotFoundException {
            for (String prefixEntryName : prefixEntryNames)
                remove(prefixEntryName);
        }

        void copy(@NonNull Path zip) throws IOException;

        void setComment(String comment);

    }

    public interface Reader extends Iterable<ZipFile.Entry> {

        void extract(@NonNull Path destDir) throws IOException;

        void extract(@NonNull Path destDir, @NonNull String fileName) throws IOException;

        default void extract(@NonNull Path destDir, @NonNull Collection<String> fileNames) throws IOException {
            for (String fileName : fileNames)
                extract(destDir, fileName);
        }

        default Stream<ZipFile.Entry> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        @NonNull
        ZipFile.Entry extract(@NonNull String fileName) throws IOException;

        String getComment();

        @NonNull
        Set<String> getEntryNames();

        boolean isSplit();

        boolean isZip64();
    }

}
