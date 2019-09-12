package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
final class ZipFileMisc implements ZipFile.Misc {

    private final ZipModel zipModel;
    private final DataOutput out;

    public ZipFileMisc(@NonNull Path zip) throws IOException {
        checkZipFile(zip);
        zipModel = ZipModelBuilder.read(zip);
        out = createDataOutput(zipModel);
        out.seek(zipModel.getCentralDirectoryOffs());
    }

    private static DataOutput createDataOutput(ZipModel zipModel) throws IOException {
        Path parent = zipModel.getFile().getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplit() ? SplitZipOutputStream.create(zipModel) : SingleZipOutputStream.create(zipModel);
    }

    @Override
    public void setComment(String comment) {
        // TODO should treat only empty comment as null
        comment = StringUtils.trimToNull(comment);
        zipModel.setComment(comment);
    }

    @Override
    public String getComment() {
        return zipModel.getComment();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private static void checkZipFile(Path zip) {
        if (!Files.exists(zip))
            throw new Zip4jException("ZipFile not exists: " + zip);
        if (!Files.isRegularFile(zip))
            throw new Zip4jException("ZipFile is not a regular file: " + zip);
    }
}
