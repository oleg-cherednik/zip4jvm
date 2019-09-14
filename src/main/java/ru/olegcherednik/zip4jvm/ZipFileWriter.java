package ru.olegcherednik.zip4jvm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
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
final class ZipFileWriter implements ZipFile.Writer, ZipModelContext {

    private final ZipEntrySettings defEntrySettings;
    @Getter
    private final ZipModel zipModel;
    private final Set<String> existedEntryNames;
    private final Map<String, ZipModelEntry> addEntryNameZipModelEntry = new LinkedHashMap<>();
    private final Map<String, ZipModelEntry> copyEntryNameZipModelEntry = new LinkedHashMap<>();

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

            if (copyEntryNameZipModelEntry.containsKey(entryName))
                throw new Zip4jException("File name duplication");

            if (addEntryNameZipModelEntry.put(entryName, new ZipModelEntry(zipModel, entry)) != null)
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

        // TODO it's not working, check it in test
        if (entryNames.isEmpty())
            throw new FileNotFoundException(prefixEntryName);

        entryNames.forEach(entryName -> {
            addEntryNameZipModelEntry.remove(entryName);
            copyEntryNameZipModelEntry.remove(entryName);
            existedEntryNames.remove(entryName);
            zipModel.removeEntry(entryName);
        });
    }

    @Override
    public void copy(@NonNull Path zip) throws IOException {
        ZipModel zipModel = ZipModelBuilder.read(zip);

        for (ZipEntry entry : zipModel.getEntries()) {
            if (addEntryNameZipModelEntry.containsKey(entry.getFileName()))
                throw new Zip4jException("File name duplication");
            if (copyEntryNameZipModelEntry.put(entry.getFileName(), new ZipModelEntry(zipModel, entry)) != null)
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

        existedEntryNames.forEach(entryName -> tasks.add(new CopyExistedEntryTask(zipModel, entryName)));
        addEntryNameZipModelEntry.forEach((entryName, data) -> tasks.add(new AddEntryTask(data.getZipModel(), data.getZipEntry())));
        copyEntryNameZipModelEntry.forEach(
                (entryName, data) -> tasks.add(new CopyExistedEntryTask(data.getZipModel(), data.getZipEntry().getFileName())));
        tasks.add(new CloseZipFileTask());
        tasks.add(new RemoveOriginalZipFileTask());
        tasks.add(new MoveTemporaryZipFileTask());

        for (Task task : tasks)
            task.accept(this);
    }

    @Getter
    @RequiredArgsConstructor
    private static final class ZipModelEntry {

        private final ZipModel zipModel;
        private final ZipEntry zipEntry;

        @Override
        public String toString() {
            return zipModel.getFile().toString() + " ->" + zipEntry.getFileName();
        }
    }

    @Getter
    @Setter
    private DataOutput out;

}
