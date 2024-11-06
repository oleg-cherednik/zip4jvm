package ru.olegcherednik.zip4jvm.io.out.data;

import org.apache.commons.codec.digest.PureJavaCrc32;

import java.io.IOException;
import java.util.function.LongConsumer;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
public class ChecksumCalcDataOutput extends BaseDataOutput {

    private final LongConsumer saveSize;
    private final Checksum checksum = new PureJavaCrc32();

    public ChecksumCalcDataOutput(LongConsumer saveSize, DataOutput out) {
        super(out);
        this.saveSize = saveSize;
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        checksum.update(b);
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        saveSize.accept(checksum.getValue());
        delegate.close();
    }

}
