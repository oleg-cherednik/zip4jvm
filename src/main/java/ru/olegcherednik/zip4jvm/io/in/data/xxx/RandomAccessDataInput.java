package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface RandomAccessDataInput extends XxxDataInput {

    void seek(int diskNo, long relativeOffs) throws IOException;

    void seek(long absOffs) throws IOException;

    void seek(String id) throws IOException;

    // TODO not sure this method belongs to random access
    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    long availableLong() throws IOException;

    default boolean isDwordSignature(int expected) throws IOException {
        long offs = getAbsOffs();
        int actual = readDwordSignature();
        backward((int) (getAbsOffs() - offs));
        return actual == expected;
    }

    default void backward(int bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "backward.bytes");
        seek(getAbsOffs() - bytes);
    }

}
