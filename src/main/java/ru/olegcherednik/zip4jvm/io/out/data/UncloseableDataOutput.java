package ru.olegcherednik.zip4jvm.io.out.data;

import java.io.IOException;

/**
 * This decorator block closing the delegate {@link BaseDataOutput#delegate}.
 *
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
public class UncloseableDataOutput extends BaseDataOutput {

    public UncloseableDataOutput(DataOutput delegate) {
        super(delegate);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        /* nothing to close */
    }

}
