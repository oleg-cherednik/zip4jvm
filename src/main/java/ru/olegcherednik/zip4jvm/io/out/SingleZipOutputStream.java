package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SingleZipOutputStream extends BaseDataOutput {

    @NonNull
    public static SingleZipOutputStream create(@NonNull ZipModel zipModel) throws IOException {
        return new SingleZipOutputStream(zipModel.getFile(), zipModel);
    }

    @NonNull
    public static SingleZipOutputStream create(@NonNull Path zip, @NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipOutputStream(zip, zipModel);
    }

    private SingleZipOutputStream(Path zip, ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
        createFile(zip);
    }

    @Override
    public long getDisk() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
