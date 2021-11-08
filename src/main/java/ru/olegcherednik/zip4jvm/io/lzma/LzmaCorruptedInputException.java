package ru.olegcherednik.zip4jvm.io.lzma;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

/**
 * Thrown when the compressed input data is corrupt. However, it is possible that some or all of the data already read from the input stream was
 * corrupt too.
 *
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
public final class LzmaCorruptedInputException extends Zip4jvmException {

    private static final long serialVersionUID = 9196697492303828564L;

    public LzmaCorruptedInputException() {
        super("LZMA compressed data is corrupt");
    }

}
