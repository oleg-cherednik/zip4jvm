package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
public class RemoveOriginalZipFileTask implements Task {

    @Override
    public void accept(ZipModelContext context) throws IOException {
        ZipModel zipModel = context.getZipModel();

        for (long i = 0; i <= zipModel.getTotalDisks(); i++)
            Files.deleteIfExists(zipModel.getPartFile(i));
    }
}
