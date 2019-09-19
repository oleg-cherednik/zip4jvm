package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
public class Zip4jEntryDuplicationException extends Zip4jException {

    private static final long serialVersionUID = 2517559146270672014L;

    public Zip4jEntryDuplicationException(String fileName) {
        super("Entry name duplication: '" + fileName + '\'', ErrorCode.ENTRY_DUPLICATION);
    }
}
