package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
public class EntryWithPrefixNotFoundException extends Zip4jvmException {

    private static final long serialVersionUID = -983822403067879321L;

    public EntryWithPrefixNotFoundException(String entryNamePrefix) {
        super("Entry with prefix not found: " + entryNamePrefix, ErrorCode.ENTRY_WITH_PREFIX_NOT_FOUND);
    }
}
