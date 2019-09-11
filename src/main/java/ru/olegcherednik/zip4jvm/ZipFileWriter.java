package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
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
import ru.olegcherednik.zip4jvm.tasks.AddEntryTask;
import ru.olegcherednik.zip4jvm.tasks.Task;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
final class ZipFileWriter implements ZipFile.Writer {

    private final ZipModel zipModel;
    private final DataOutput out;
    private final ZipEntrySettings defEntrySettings;
    private final List<Task> tasks = new ArrayList<>();

    public ZipFileWriter(@NonNull Path zip, @NonNull ZipFileWriterSettings zipFileSettings) throws IOException {
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

        List<ZipEntry> entries = createEntries(PathUtils.getRelativeContent(paths), entrySettings);
        requireNoDuplicates(entries);

        for (ZipEntry entry : entries)
            tasks.add(new AddEntryTask(entry));

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
        ZipModelContext context = ZipModelContext.builder().zipModel(zipModel).out(out).build();

        for (Task task : tasks)
            task.accept(context);

        out.close();
    }

}
