package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReadSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ZipFile real-time implementation.
 * <br>
 * When create new instance of this class:
 * <ul>
 * <li><i>zip file exists</i> - open zip archive</li>
 * <li><i>zip file not exists</i> - create new empty zip archive</li>
 * </ul>
 * <p>
 * To close zip archive correctly, do call {@link ZipFile#close()} method.
 * <pre>
 * try (ZipFile zipFile = new ZipFile(Paths.get("~/src.zip"))) {
 *     zipFile.addEntry(...);
 * }
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
public class ZipFile implements Closeable {

    private final ZipModel zipModel;
    private final DataOutput out;
    private final ZipEntrySettings defEntrySettings;

    public static ZipFile write(@NonNull Path zip, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        return new ZipFile(zip, zipFileSettings);
    }

    public ZipFile(@NonNull Path zip) throws IOException {
        this(zip, ZipFileSettings.builder().build());
    }

    public ZipFile(@NonNull Path zip, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        zipModel = ZipModelBuilder.readOrCreate(zip, zipFileSettings);
        defEntrySettings = zipFileSettings.getEntrySettings();
        out = createDataOutput(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    private static DataOutput createDataOutput(ZipModel zipModel) throws IOException {
        Path parent = zipModel.getZip().getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplit() ? SplitZipOutputStream.create(zipModel) : SingleZipOutputStream.create(zipModel);
    }

    public void add(@NonNull Path path) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(Collections.singleton(path), defEntrySettings);
    }

    public void add(@NonNull Path path, @NonNull ZipEntrySettings entrySettings) throws IOException {
        add(Collections.singleton(path), entrySettings);
    }

    public void add(@NonNull Collection<Path> paths) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(paths, defEntrySettings);
    }

    public void add(@NonNull Collection<Path> paths, @NonNull ZipEntrySettings entrySettings) throws IOException {
        PathUtils.requireExistedPaths(paths);

        List<ZipEntry> entries = createEntries(PathUtils.getRelativeContent(paths), entrySettings);
        requireNoDuplicates(entries);

        entries.forEach(entry -> writeEntry(entry, out));
    }

    private void writeEntry(ZipEntry entry, DataOutput out) {
        try (OutputStream os = EntryOutputStream.create(entry, zipModel, out)) {
            entry.write(os);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings entrySettings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), entrySettings))
                           .collect(Collectors.toList());
    }

    private void requireNoDuplicates(List<ZipEntry> entries) {
        Set<String> entryNames = zipModel.getEntryNames();

        String duplicateEntryName = entries.stream()
                                           .map(ZipEntry::getFileName)
                                           .filter(entryNames::contains)
                                           .findFirst().orElse(null);

        if (duplicateEntryName != null)
            throw new Zip4jException("Entry with given name already exists: " + duplicateEntryName);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public static final class Read {

        private final ZipModel zipModel;
        private final ZipFileReadSettings settings;

        public Read(Path zip, ZipFileReadSettings settings) throws IOException {
            zipModel = ZipModelBuilder.read(zip);
            this.settings = settings;
        }

        public void extract(@NonNull Path destDir) {
            zipModel.getEntries().forEach(entry -> entry.setPassword(settings.getPassword()));
            new UnzipEngine(zipModel, settings.getPassword()).extractEntries(destDir, zipModel.getEntryNames());
        }
    }
}
