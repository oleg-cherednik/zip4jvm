package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
public interface Writer {

    void write(@NonNull DataOutput out) throws IOException;

}
