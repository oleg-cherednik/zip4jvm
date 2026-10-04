package io.airlift.compress.zstd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 04.10.2026
 */
public class InputStreamForRead extends ReadByteArrayWithOffs {

    private final InputStream in;

    public InputStreamForRead(byte[] buf) {
        super(buf);
        in = new ByteArrayInputStream(buf);
    }

}
