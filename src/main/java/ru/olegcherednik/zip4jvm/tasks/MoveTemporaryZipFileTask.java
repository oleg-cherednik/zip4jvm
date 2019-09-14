package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@RequiredArgsConstructor
public class MoveTemporaryZipFileTask implements Task {

    private final ZipModel destZipModel;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        for (long i = 0; i <= destZipModel.getTotalDisks(); i++) {
            Path src = destZipModel.getStreamPartFile(i);
            Path dest = destZipModel.getPartFile(i);
            Files.move(src, dest);
        }

        Files.deleteIfExists(destZipModel.getStreamFile().getParent());
    }
}
