package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
public class Zip4jEmptyPasswordException extends Zip4jException {

    private static final long serialVersionUID = -2871641113917950093L;

    public Zip4jEmptyPasswordException() {
        super("Empty password for enabled encryption", ErrorCode.EMPTY_PASSWORD);
    }
}
