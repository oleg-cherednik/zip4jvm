package ru.olegcherednik.zip4jvm.model.diagnostic;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;
import java.util.function.Function;

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

    public static <T> T foo(DataInput in, Function<Diagnostic, Block> getBlock, LocalSupplier<T> task)
            throws IOException {
        try {
            getBlock.apply(Diagnostic.getInstance()).setOffs(in.getOffs());
            return task.get();
        } finally {
            getBlock.apply(Diagnostic.getInstance()).setEndOffs(in.getOffs());
        }
    }

}
