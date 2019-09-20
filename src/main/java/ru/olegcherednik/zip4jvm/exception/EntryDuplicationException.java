package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 19.09.2019
 */
public class EntryDuplicationException extends Zip4jvmException {

    private static final long serialVersionUID = 2517559146270672014L;

    public EntryDuplicationException(String fileName) {
        super("Entry name duplication: '" + fileName + '\'', ErrorCode.ENTRY_DUPLICATION);
    }
}
