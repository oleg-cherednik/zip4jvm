package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SingleZipOutputStream extends BaseDataOutput {

    public SingleZipOutputStream(ZipModel zipModel) throws FileNotFoundException {
        super(zipModel);
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
