package ru.olegcherednik.zip4jvm.io.zstd;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private final ZstdDecompressor decompressor;

    public ZstdInputStream(DataInput in) throws IOException {
        decompressor = new ZstdDecompressor();
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

}
