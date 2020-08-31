package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public class SolidZipOutputStream extends BaseZipDataOutput {

    public SolidZipOutputStream(ZipModel zipModel) throws IOException {
        super(zipModel);
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

}
