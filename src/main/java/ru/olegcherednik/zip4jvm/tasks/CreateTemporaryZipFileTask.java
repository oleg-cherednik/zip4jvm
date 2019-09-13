package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 11.09.2019
 */
public final class CreateTemporaryZipFileTask implements Task {

    @Override
    public void accept(ZipModelContext context) throws IOException {
//        zipModel = ZipModelBuilder.readOrCreate(zip, zipFileSettings);
//        defEntrySettings = zipFileSettings.getEntrySettings();
        context.setOut(createDataOutput(context));
//        out.seek(zipModel.getCentralDirectoryOffs());
    }

    private static DataOutput createDataOutput(ZipModelContext context) throws IOException {
        ZipModel zipModel = context.getZipModel();
        Path file = zipModel.getFile();
        Path parent = file.getParent().resolve("tmp");

        Files.createDirectories(parent);

        file = parent.resolve(file.getFileName());
        zipModel.setStreamFile(file);

        context.setTmpFile(file);

        return zipModel.isSplit() ? SplitZipOutputStream.create(zipModel) : SingleZipOutputStream.create(file, zipModel);
    }
}
