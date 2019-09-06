package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
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

    private final ZipEntrySettings defEntrySettings;

    public ZipFile(@NonNull Path file) throws IOException {
        this(file, ZipFileSettings.builder().build());
    }

    public ZipFile(@NonNull Path file, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        zipModel = ZipModelBuilder.readOrCreate(file, zipFileSettings);
        defEntrySettings = zipFileSettings.getDefEntrySettings();
        out = ZipEngine.createDataOutput(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    public void add(@NonNull Path path) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(Collections.singleton(path), defEntrySettings);
    }

    public void add(@NonNull Path path, @NonNull ZipEntrySettings settings) throws IOException {
        add(Collections.singleton(path), settings);
    }

    public void add(@NonNull Collection<Path> paths) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(paths, defEntrySettings);
    }

    public void add(@NonNull Collection<Path> paths, @NonNull ZipEntrySettings settings) throws IOException {
        PathUtils.requireExistedPaths(paths);
        List<ZipEntry> entries = createEntries(PathUtils.getRelativeContentMap(paths), settings);

        // TODO throw exception if duplication found
        entries.forEach(entry -> ZipEngine.writeEntry(entry, out, zipModel));
    }

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings settings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), settings))
                           .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        // TODO check for zip64
        out.close();
    }
}
