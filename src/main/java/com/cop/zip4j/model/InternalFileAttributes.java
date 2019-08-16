package com.cop.zip4j.model;

import com.cop.zip4j.io.in.DataInput;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
@RequiredArgsConstructor
public class InternalFileAttributes implements Supplier<byte[]>, Consumer<Path> {

    private static final int SIZE = 2;
    public static final InternalFileAttributes NULL = new InternalFileAttributes(new byte[SIZE]);

    private final byte[] data;

    public static InternalFileAttributes of(@NonNull Path path) throws IOException {
        return new InternalFileAttributes(new byte[SIZE]);
    }

    public static InternalFileAttributes read(@NonNull DataInput in) throws IOException {
        return new InternalFileAttributes(in.readBytes(SIZE));
    }

    @Override
    public void accept(Path path) {
    }

    @Override
    public byte[] get() {
        return ArrayUtils.clone(data);
    }
}
