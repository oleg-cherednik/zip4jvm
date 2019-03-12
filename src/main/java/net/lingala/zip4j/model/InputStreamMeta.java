package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class InputStreamMeta implements Closeable {

    @NonNull
    private final InputStream in;
    @NonNull
    private final String relativePath;

    public boolean isRegularFile() {
        return !Zip4jUtil.isDirectory(relativePath);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
