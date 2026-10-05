package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.ByteArrayDataInput;

/**
 * @author Oleg Cherednik
 * @since 04.10.2026
 */
public class InputStreamForRead extends ReadByteArrayWithOffs {

    private final DataInput in;

    public InputStreamForRead(byte[] buf) {
        super(buf);
        in = new ByteArrayDataInput(buf);
    }

}
