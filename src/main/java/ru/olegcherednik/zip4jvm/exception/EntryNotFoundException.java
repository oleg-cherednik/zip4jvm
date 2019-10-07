package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
public class EntryNotFoundException extends Zip4jvmException {

    private static final long serialVersionUID = 5715303930552608939L;

    public EntryNotFoundException(String entryName) {
        super("Entry not found: " + entryName, ErrorCode.ENTRY_NOT_FOUND);
    }
}
