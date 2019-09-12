package ru.olegcherednik.zip4jvm.tasks;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Oleg Cherednik
 * @since 11.09.2019
 */
@Getter
public class TaskEngine {

    private final ZipModel zipModel;
    private final Set<String> existedEntryNames;
    private final Map<String, Task> fileNameTask = new LinkedHashMap<>();
    private final Map<String, ZipEntry> addEntries = new LinkedHashMap<>();

    public TaskEngine(ZipModel zipModel) {
        this.zipModel = zipModel;
        existedEntryNames = new LinkedHashSet<>(zipModel.getEntryNames());
    }

    public void addEntry(ZipEntry entry) {
        String entryName = entry.getFileName();

        if (existedEntryNames.contains(entryName))
            throw new Zip4jException("File name duplication");

        addEntries.put(entryName, entry);
        fileNameTask.put(entry.getFileName(), new AddEntryTask(entry));
    }

    public void removeEntry(String entryName) {
        addEntries.remove(entryName);
        existedEntryNames.remove(entryName);
        fileNameTask.remove(entryName);
    }

    public void accept() throws IOException {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new CreateTemporaryZipFileTask());

        existedEntryNames.forEach(entryName -> tasks.add(new CopyExistedEntryTask(entryName)));
        addEntries.forEach((entryName, entry) -> tasks.add(new AddEntryTask(entry)));
        tasks.add(new CloseZipFileTask());
        tasks.add(new RemoveOriginalZipFileTask());
        tasks.add(new RenameTemporaryZipFileTask());

        ZipModelContext context = ZipModelContext.builder()
                                                 .zipModel(zipModel)
                                                 .build();

        for (Task task : tasks)
            task.accept(context);

//        for (Task task : fileNameTask.values())
//            task.accept(context);
    }

    public void addTask(Task task) {
        fileNameTask.put(UUID.randomUUID().toString(), task);
    }

}
