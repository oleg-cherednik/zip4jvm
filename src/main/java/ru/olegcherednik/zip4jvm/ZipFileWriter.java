package ru.olegcherednik.zip4jvm;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;
import ru.olegcherednik.zip4jvm.tasks.AddEntryTask;
import ru.olegcherednik.zip4jvm.tasks.CloseZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.CopyExistedEntryTask;
import ru.olegcherednik.zip4jvm.tasks.CreateTemporaryZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.MoveTemporaryZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.RemoveOriginalZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.Task;
import ru.olegcherednik.zip4jvm.tasks.ZipModelContext;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
final class ZipFileWriter implements ZipFile.Writer, ZipModelContext {

    private final ZipEntrySettings defEntrySettings;
    @Getter
    private final ZipModel zipModel;
    private final Map<String, Task> addEntryTasks = new LinkedHashMap<>();

    public ZipFileWriter(@NonNull Path zip, @NonNull ZipFileSettings zipFileSettings) throws IOException {
        defEntrySettings = zipFileSettings.getEntrySettings();
        zipModel = createZipModel(zip, zipFileSettings);
        addEntryTasks.putAll(createCopyExistedEntryTask(zipModel));
    }

    private static ZipModel createZipModel(Path zip, ZipFileSettings zipFileSettings) throws IOException {
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

    private static Map<String, Task> createCopyExistedEntryTask(ZipModel zipModel) {
        Map<String, Task> map = new LinkedHashMap<>();
        zipModel.getEntryNames().forEach(entryName -> map.put(entryName, new CopyExistedEntryTask(zipModel, entryName)));
        return map;
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

        for (Map.Entry<Path, String> entry : PathUtils.getRelativeContent(paths).entrySet()) {
            Path path = entry.getKey();
            String fileName = entry.getValue();

            if (addEntryTasks.put(fileName, new AddEntryTask(path, fileName, entrySettings)) != null)
                throw new Zip4jException("File name duplication");
        }
    }

    @Override
    public void remove(@NonNull String prefixEntryName) throws FileNotFoundException {
        String normalizedPrefixEntryName = ZipUtils.normalizeFileName(prefixEntryName);

        Set<String> entryNames = zipModel.getEntryNames().stream()
                                         .filter(entryName -> entryName.startsWith(normalizedPrefixEntryName))
                                         .collect(Collectors.toSet());

        // TODO it's not working, check it in test
        if (entryNames.isEmpty())
            throw new FileNotFoundException(prefixEntryName);

        entryNames.forEach(entryName -> {
            addEntryTasks.remove(entryName);
            zipModel.removeEntry(entryName);
        });
    }

    @Override
    public void copy(@NonNull Path zip) throws IOException {
        ZipModel zipModel = ZipModelBuilder.read(zip);

        for (String fileName : zipModel.getEntryNames()) {
            if (addEntryTasks.containsKey(fileName))
                throw new Zip4jException("File name duplication");
            if (addEntryTasks.put(fileName, new CopyExistedEntryTask(zipModel, fileName)) != null)
                throw new Zip4jException("File name duplication");
        }
    }

    @Override
    public void setComment(String comment) {
        zipModel.setComment(comment);
    }

    @Override
    public void close() throws IOException {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new CreateTemporaryZipFileTask());
        tasks.addAll(addEntryTasks.values());
        tasks.add(new CloseZipFileTask());
        tasks.add(new RemoveOriginalZipFileTask());
        tasks.add(new MoveTemporaryZipFileTask());

        for (Task task : tasks)
            task.accept(this);
    }

    @Getter
    @Setter
    private DataOutput out;

}
