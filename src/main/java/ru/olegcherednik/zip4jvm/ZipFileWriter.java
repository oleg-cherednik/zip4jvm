package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;
import ru.olegcherednik.zip4jvm.tasks.TaskEngine;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
final class ZipFileWriter implements ZipFile.Writer {

    private final ZipModel zipModel;
    private final DataOutput out;
    private final ZipEntrySettings defEntrySettings;
    private final TaskEngine engine;

    public ZipFileWriter(@NonNull Path zip, @NonNull ZipFileWriterSettings zipFileSettings) throws IOException {
        zipModel = ZipModelBuilder.readOrCreate(zip, zipFileSettings);
        defEntrySettings = zipFileSettings.getEntrySettings();
        out = createDataOutput(zipModel);
        engine = new TaskEngine(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    private static DataOutput createDataOutput(ZipModel zipModel) throws IOException {
        Path parent = zipModel.getZip().getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplit() ? SplitZipOutputStream.create(zipModel) : SingleZipOutputStream.create(zipModel);
    }

    @Override
    public void add(@NonNull Path path) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(Collections.singleton(path), defEntrySettings);
    }

    @Override
    public void add(@NonNull Path path, @NonNull ZipEntrySettings entrySettings) throws IOException {
        add(Collections.singleton(path), entrySettings);
    }

    @Override
    public void add(@NonNull Collection<Path> paths) throws IOException {
        Objects.requireNonNull(defEntrySettings);
        add(paths, defEntrySettings);
    }

    @Override
    public void add(@NonNull Collection<Path> paths, @NonNull ZipEntrySettings entrySettings) throws IOException {
        PathUtils.requireExistedPaths(paths);
        createEntries(PathUtils.getRelativeContent(paths), entrySettings).forEach(engine::addEntry);
    }

    @Override
    public void remove(@NonNull String entryName) {
        engine.removeEntry(entryName);
    }

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings entrySettings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), entrySettings))
                           .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        ZipModelContext context = ZipModelContext.builder().zipModel(zipModel).out(out).build();
        engine.accept(context);
        out.close();
    }

}
