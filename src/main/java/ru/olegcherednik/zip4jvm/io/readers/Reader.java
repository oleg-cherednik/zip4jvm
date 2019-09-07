package ru.olegcherednik.zip4jvm.io.readers;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.io.in.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
interface Reader<T> {

    @NonNull
    T read(@NonNull DataInput in) throws IOException;

}
