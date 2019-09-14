package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
public class MoveTemporaryZipFileTask implements Task {

    @Override
    public void accept(ZipModelContext context) throws IOException {
        ZipModel zipModel = context.getZipModel();

        for (long i = 0; i <= zipModel.getTotalDisks(); i++) {
            Path src = zipModel.getStreamPartFile(i);
            Path dest = zipModel.getPartFile(i);
            Files.move(src, dest);
        }

        Files.deleteIfExists(zipModel.getStreamFile().getParent());
    }
}
