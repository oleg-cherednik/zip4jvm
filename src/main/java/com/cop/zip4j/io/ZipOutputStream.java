package com.cop.zip4j.io;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public class ZipOutputStream implements AutoCloseable {

    @NonNull
    public final SplitOutputStream out;
    @NonNull
    public final ZipModel zipModel;

    @Override
    public void close() throws IOException {
        out.close();
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }

}
