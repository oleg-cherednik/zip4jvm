package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.in.data.CommonBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class StoreBufferedDataInput extends CommonBaseDataInput {

    public StoreBufferedDataInput(DataInput in) {
        super(in);
    }
}
