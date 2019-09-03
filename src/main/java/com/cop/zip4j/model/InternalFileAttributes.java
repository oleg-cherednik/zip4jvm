package com.cop.zip4j.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InternalFileAttributes implements Supplier<byte[]>, Consumer<Path> {

    public static final int SIZE = 2;
    public static final InternalFileAttributes NULL = new InternalFileAttributes(new byte[SIZE]);

    private final byte[] data;

    public static InternalFileAttributes create() {
        return new InternalFileAttributes(new byte[SIZE]);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static InternalFileAttributes create(@NonNull byte[] data) {
        return new InternalFileAttributes(ArrayUtils.clone(data));
    }

    @Override
    public void accept(Path path) {
    }

    @Override
    public byte[] get() {
        return ArrayUtils.clone(data);
    }
}
