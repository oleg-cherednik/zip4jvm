package com.cop.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.cop.zip4j.utils.ZipUtils;

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
        return !ZipUtils.isDirectory(relativePath);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
