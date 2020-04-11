package ru.olegcherednik.zip4jvm.exception;

import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
public class EncryptionNotSupportedException extends Zip4jvmException {

    public EncryptionNotSupportedException(EncryptionMethod encryptionMethod) {
        super(String.format("Encryption '%s' is not supported", encryptionMethod));
    }
}
