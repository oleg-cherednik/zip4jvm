package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
public interface ReadBuffer {

    int read(byte[] buf, int offs, int len) throws IOException;

}
