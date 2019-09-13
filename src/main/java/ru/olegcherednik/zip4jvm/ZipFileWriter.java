package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;
import ru.olegcherednik.zip4jvm.tasks.TaskEngine;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.FileNotFoundException;
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

    private final ZipEntrySettings defEntrySettings;
    private final ZipModel zipModel;
    private final TaskEngine engine;

    public ZipFileWriter(@NonNull Path zip, @NonNull ZipFileWriterSettings zipFileSettings) throws IOException {
        defEntrySettings = zipFileSettings.getEntrySettings();
        zipModel = createZipModel(zip, zipFileSettings);
        engine = new TaskEngine(zipModel);
    }

    private static ZipModel createZipModel(Path zip, ZipFileWriterSettings zipFileSettings) throws IOException {
        if (Files.exists(zip)) {
            ZipModel zipModel = ZipModelBuilder.read(zip);

            if (zipModel.isSplit())
                zipModel.setSplitSize(zipFileSettings.getSplitSize());
            if (zipFileSettings.getComment() != null)
                zipModel.setComment(zipFileSettings.getComment());

            return zipModel;
        }

        return ZipModelBuilder.create(zip, zipFileSettings);
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
    public void remove(@NonNull String entryName) throws FileNotFoundException {
        engine.removeEntry(entryName);
    }

    @Override
    public void setComment(String comment) {
        zipModel.setComment(comment);
    }

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings entrySettings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), entrySettings))
                           .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        engine.accept();
    }

}
