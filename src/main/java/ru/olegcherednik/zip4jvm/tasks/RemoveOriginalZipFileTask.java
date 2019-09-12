package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
public class RemoveOriginalZipFileTask implements Task {

    @Override
    public void accept(ZipModelContext context) throws IOException {
        Path file = context.getZipModel().getFile();
        Files.deleteIfExists(file);
    }
}
