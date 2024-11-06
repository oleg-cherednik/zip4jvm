package ru.olegcherednik.zip4jvm.io.out.data;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
public class SizeCalcDataOutput extends BaseDataOutput {

    private final LongConsumer saveSize;
    private long size;

    public SizeCalcDataOutput(LongConsumer saveSize, DataOutput out) {
        super(out);
        this.saveSize = saveSize;
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        size++;
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        saveSize.accept(size);
        delegate.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return super.toString() + ", size: " + size;
    }

}
