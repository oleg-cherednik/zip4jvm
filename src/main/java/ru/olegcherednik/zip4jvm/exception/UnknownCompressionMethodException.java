package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
public class UnknownCompressionMethodException extends Zip4jvmException {

    private static final long serialVersionUID = 2257593848156926925L;

    public UnknownCompressionMethodException(int code) {
        super(String.format("Unknown compression method code=%d", code));
    }
}
