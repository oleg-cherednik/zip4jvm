package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 22.09.2024
 */
public class IncorrectCentralDirectoryPasswordException extends IncorrectPasswordException {

    private static final long serialVersionUID = 3259098878115692516L;

    public IncorrectCentralDirectoryPasswordException() {
        super("Incorrect password for central directory", ErrorCode.CENTRAL_DIRECTORY_INCORRECT_PASSWORD);
    }

}
