package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 11.09.2019
 */
@RequiredArgsConstructor
public final class CreateTemporaryZipFileTask implements Task {

    private final ZipModel destZipModel;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        context.setOut(createDataOutput());
    }

    private DataOutput createDataOutput() throws IOException {
        Path file = destZipModel.getFile();
        Path parent = file.getParent().resolve("tmp");

        Files.createDirectories(parent);

        file = parent.resolve(file.getFileName());
        destZipModel.setStreamFile(file);

        return destZipModel.isSplit() ? SplitZipOutputStream.create(destZipModel) : SingleZipOutputStream.create(file, destZipModel);
    }
}
