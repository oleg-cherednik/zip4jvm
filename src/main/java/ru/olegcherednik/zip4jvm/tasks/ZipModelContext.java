package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ZipModel;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
public interface ZipModelContext {

    ZipModel getZipModel();

    DataOutput getOut();

    void setOut(DataOutput out);

}
