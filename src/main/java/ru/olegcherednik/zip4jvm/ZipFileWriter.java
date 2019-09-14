package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;
import ru.olegcherednik.zip4jvm.tasks.AddEntryTask;
import ru.olegcherednik.zip4jvm.tasks.CloseZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.CopyExistedEntryTask;
import ru.olegcherednik.zip4jvm.tasks.CreateTemporaryZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.MoveTemporaryZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.RemoveOriginalZipFileTask;
import ru.olegcherednik.zip4jvm.tasks.Task;
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
import java.util.LinkedHashSet;
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

    private final ZipEntrySettings defEntrySettings;
    private final ZipModel zipModel;
    private final Set<String> existedEntryNames;
    private final Map<String, ZipEntry> addEntries = new LinkedHashMap<>();

    public ZipFileWriter(@NonNull Path zip, @NonNull ZipFileWriterSettings zipFileSettings) throws IOException {
        defEntrySettings = zipFileSettings.getEntrySettings();
        zipModel = createZipModel(zip, zipFileSettings);
        existedEntryNames = new LinkedHashSet<>(zipModel.getEntryNames());
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
        createEntries(PathUtils.getRelativeContent(paths), entrySettings).forEach(entry -> {
            String entryName = entry.getFileName();

            if (addEntries.put(entryName, entry) != null)
                throw new Zip4jException("File name duplication");
        });
    }

    private static List<ZipEntry> createEntries(Map<Path, String> pathFileName, ZipEntrySettings entrySettings) {
        return pathFileName.entrySet().parallelStream()
                           .map(entry -> ZipEntryBuilder.create(entry.getKey(), entry.getValue(), entrySettings))
                           .collect(Collectors.toList());
    }

    @Override
    public void remove(@NonNull String prefixEntryName) throws FileNotFoundException {
        String normalizedPrefixEntryName = ZipUtils.normalizeFileName(prefixEntryName);

        Set<String> entryNames = zipModel.getEntryNames().stream()
                                         .filter(entryName -> entryName.startsWith(normalizedPrefixEntryName))
                                         .collect(Collectors.toSet());

        if (entryNames.isEmpty())
            throw new FileNotFoundException(prefixEntryName);

        entryNames.forEach(entryName -> {
            addEntries.remove(entryName);
            existedEntryNames.remove(entryName);
            zipModel.removeEntry(entryName);
        });
    }

    @Override
    public void setComment(String comment) {
        zipModel.setComment(comment);
    }

    @Override
    public void close() throws IOException {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new CreateTemporaryZipFileTask());

        existedEntryNames.forEach(entryName -> tasks.add(new CopyExistedEntryTask(entryName)));
        addEntries.forEach((entryName, entry) -> tasks.add(new AddEntryTask(entry)));
        tasks.add(new CloseZipFileTask());
        tasks.add(new RemoveOriginalZipFileTask());
        tasks.add(new MoveTemporaryZipFileTask());

        ZipModelContext context = ZipModelContext.builder()
                                                 .zipModel(zipModel)
                                                 .build();

        for (Task task : tasks) {
            try {
                int a = 0;
                task.accept(context);
                a++;
            } catch(IOException e) {
                throw e;
            }
        }
    }

}
