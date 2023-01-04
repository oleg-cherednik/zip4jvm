package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 30.12.2022
 */
@RequiredArgsConstructor
public class ByteArrayReader implements Reader<byte[]> {

    private final int size;

    @Override
    public byte[] read(DataInput in) {
        return in.readBytes(size);
    }
}
