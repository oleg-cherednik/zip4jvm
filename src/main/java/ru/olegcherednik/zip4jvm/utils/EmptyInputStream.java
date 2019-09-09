package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmptyInputStream extends InputStream {

    public static final EmptyInputStream INSTANCE = new EmptyInputStream();

    @Override
    public int read() throws IOException {
        return IOUtils.EOF;
    }
}
