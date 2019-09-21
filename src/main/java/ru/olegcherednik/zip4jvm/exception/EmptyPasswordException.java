package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
public class EmptyPasswordException extends Zip4jvmException {

    private static final long serialVersionUID = -2871641113917950093L;

    public EmptyPasswordException() {
        super("Empty password for enabled encryption", ErrorCode.EMPTY_PASSWORD);
    }
}
