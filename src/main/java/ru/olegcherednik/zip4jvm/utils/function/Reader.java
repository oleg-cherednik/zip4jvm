package ru.olegcherednik.zip4jvm.utils.function;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.io.in.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@FunctionalInterface
public interface Reader<T> {

    @NonNull
    T read(@NonNull DataInput in) throws IOException;

}
