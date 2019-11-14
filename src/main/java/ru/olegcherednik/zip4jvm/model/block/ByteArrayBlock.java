package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
@Getter
@Setter
public class ByteArrayBlock extends Block {

    private byte[] data;

    @Override
    public <T> T calc(DataInput in, LocalSupplier<T> task) throws IOException {
        long offs = in.getOffs();
        in.cleanBuffer();
        T res = super.calc(in, task);
        data = in.getLastBytes((int)(in.getOffs() - offs));
        return res;
    }
}
