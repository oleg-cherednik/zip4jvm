package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.Closeable;
import java.io.IOException;
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
public final class ZipFile implements Closeable {

    private final ZipModel zipModel;
    private final DataOutput out;
    private final ZipEntrySettings defSettings;

    public ZipFile(@NonNull Path zip) throws IOException {
        this(zip, ZipFileSettings.builder().build());
    }

    public ZipFile(@NonNull Path zip, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        zipModel = ZipModelBuilder.readOrCreate(zip, zipFileSettings);
        defSettings = zipFileSettings.getEntrySettings();
        out = ZipEngine.createDataOutput(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    public void add(@NonNull Path path) throws IOException {
        Objects.requireNonNull(defSettings);
        add(Collections.singleton(path), defSettings);
    }

    public void add(@NonNull Path path, @NonNull ZipEntrySettings entrySettings) throws IOException {
        add(Collections.singleton(path), entrySettings);
    }

    public void add(@NonNull Collection<Path> paths) throws IOException {
        Objects.requireNonNull(defSettings);
        add(paths, defSettings);
    }

    public void add(@NonNull Collection<Path> paths, @NonNull ZipEntrySettings entrySettings) throws IOException {
        PathUtils.requireExistedPaths(paths);

        List<ZipEntry> entries = createEntries(PathUtils.getRelativeContent(paths), entrySettings);
        requireNoDuplicates(entries);

        entries.forEach(entry -> ZipEngine.writeEntry(entry, out, zipModel));
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

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings entrySettings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), entrySettings))
                           .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        // TODO check for zip64
        out.close();
    }
}
