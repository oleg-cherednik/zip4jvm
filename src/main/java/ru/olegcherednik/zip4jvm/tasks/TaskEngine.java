package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 11.09.2019
 */
public class TaskEngine {

    private final Set<String> entryNames = new HashSet<>();
    private final Map<String, Task> fileNameTask = new LinkedHashMap<>();

    public TaskEngine(Set<String> entryNames) {
        this.entryNames.addAll(entryNames);
    }

    public void addEntry(ZipEntry entry) {
        String entryName = entry.getFileName();

        if (entryNames.contains(entryName))
            throw new Zip4jException("File name duplication");

        fileNameTask.put(entry.getFileName(), new AddEntryTask(entry));
    }

    public void removeEntry(String entryName) {
        entryNames.remove(entryName);
        fileNameTask.remove(entryName);
    }

    public void accept(ZipModelContext context) throws IOException {
        for (Task task : fileNameTask.values())
            task.accept(context);
    }

}
