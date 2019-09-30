package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
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

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static InternalFileAttributes build(byte[] data) {
        if (ArrayUtils.getLength(data) == SIZE && (data[0] != 0 || data[1] != 0))
            return new InternalFileAttributes(data);
        return NULL;
    }

    @Override
    public void accept(Path path) {
        /* nothing to accept */
    }

    @Override
    public byte[] get() {
        return ArrayUtils.clone(data);
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : "internal";
    }
}
