package ru.olegcherednik.zip4jvm.model.diagnostic;

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
    @Setter
    private long offs;
    @Getter
    private long size;

    public void setEndOffs(long offs) {
        size = offs - this.offs;
    }

    public static <T> T foo(DataInput in, Block block, LocalSupplier<T> task)
            throws IOException {
        try {
            block.setOffs(in.getOffs());
            return task.get();
        } finally {
            block.setEndOffs(in.getOffs());
        }
    }

}
