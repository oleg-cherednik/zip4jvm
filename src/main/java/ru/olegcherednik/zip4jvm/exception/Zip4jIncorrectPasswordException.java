package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 10.08.2019
 */
public class Zip4jIncorrectPasswordException extends Zip4jException {
    private static final long serialVersionUID = 6396926502843613353L;

    public Zip4jIncorrectPasswordException(String fileName) {
        super("Incorrect password for filename '" + fileName + '\'', ErrorCode.INCORRECT_PASSWORD);
    }
}
