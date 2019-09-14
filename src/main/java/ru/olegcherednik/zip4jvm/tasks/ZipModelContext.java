package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
public interface ZipModelContext {

    DataOutput getOut();

    void setOut(DataOutput out);

}
