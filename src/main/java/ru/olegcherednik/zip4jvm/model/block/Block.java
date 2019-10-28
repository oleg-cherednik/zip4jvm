package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public class Block {

    public static final Block NULL = new Block();

    @Getter
    private long size;
    @Getter
    @Setter
    private long offs;

    public <T> T calc(DataInput in, LocalSupplier<T> task) throws IOException {
        try {
            offs = in.getOffs();
            return task.get();
        } finally {
            calc(in.getOffs());
        }
    }

    public void calc(long offs) {
        size = offs - this.offs;
    }

}
