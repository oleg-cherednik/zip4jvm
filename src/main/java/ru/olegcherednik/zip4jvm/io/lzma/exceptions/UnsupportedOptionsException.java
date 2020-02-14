package ru.olegcherednik.zip4jvm.io.lzma.exceptions;

import java.io.IOException;

/** Thrown when compression options not supported by this implementation are detected. Some other implementation might support those options. */
public class UnsupportedOptionsException extends IOException {

    private static final long serialVersionUID = 3L;

    /**
     * Creates a new UnsupportedOptionsException with null
     * as its error detail message.
     */
    public UnsupportedOptionsException() {
    }

    /**
     * Creates a new UnsupportedOptionsException with the given
     * error detail message.
     *
     * @param s error detail message
     */
    public UnsupportedOptionsException(String s) {
        super(s);
    }
}
