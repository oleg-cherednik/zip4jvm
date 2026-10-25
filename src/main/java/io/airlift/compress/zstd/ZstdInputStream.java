package io.airlift.compress.zstd;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 25.10.2026
 */
@RequiredArgsConstructor
public class ZstdInputStream extends InputStream {

    private final InputStream in;

    @Override
    public int read() throws IOException {
        return in.read();
    }

}
