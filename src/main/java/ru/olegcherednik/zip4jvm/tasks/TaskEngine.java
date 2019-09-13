package ru.olegcherednik.zip4jvm.tasks;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 11.09.2019
 */
@Getter
public class TaskEngine {

    private final ZipModel zipModel;
    private final Set<String> existedEntryNames;
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
    }

    public void removeEntry(String prefixEntryName) throws FileNotFoundException {
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

    public void accept() throws IOException {
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

        for (Task task : tasks)
            task.accept(context);
    }

}
