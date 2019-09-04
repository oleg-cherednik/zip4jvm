package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 18.08.2019
 */
public class Zip4jRealBigZip64NotSupportedException extends Zip4jException {

    private static final long serialVersionUID = -1743872676747843680L;

    public Zip4jRealBigZip64NotSupportedException(long value, String type) {
        super("Real big Zip64 format is not supported: " + type + " = " + Long.toUnsignedString(value));
    }
}
