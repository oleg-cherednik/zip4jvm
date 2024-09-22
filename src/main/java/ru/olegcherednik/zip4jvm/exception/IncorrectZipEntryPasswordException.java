package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 22.09.2024
 */
public class IncorrectZipEntryPasswordException extends IncorrectPasswordException {

    private static final long serialVersionUID = 5314752891955389553L;

    public IncorrectZipEntryPasswordException(String fileName) {
        super(String.format("Incorrect password for zip entry '%s'", fileName),
              ErrorCode.ZIP_ENTRY_INCORRECT_PASSWORD);
    }

}
