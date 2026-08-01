package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@RequiredArgsConstructor
public class DecryptionHeaderWriter implements Writer {

    private static final int VERSION_SIZE = ByteUtils.WORD_SIZE;
    private static final int ENCRYPTION_ALGORITHM_SIZE = ByteUtils.WORD_SIZE;
    private static final int BIT_LENGTH_SIZE = ByteUtils.WORD_SIZE;
    private static final int FLAGS_SIZE = ByteUtils.WORD_SIZE;
    private static final int ENCRYPTED_RANDOM_DATA_SIZE = ByteUtils.WORD_SIZE;
    private static final int RECIPIENT_COUNT_SIZE = ByteUtils.DWORD_SIZE;
    private static final int HASH_ALGORITHM_SIZE = ByteUtils.WORD_SIZE;
    private static final int PASSWORD_VALIDATION_DATA_SIZE = ByteUtils.WORD_SIZE;

    private static final String MARKER_VERSION = "DecryptionHeaderWriter.MARKER_VERSION";

    private final DecryptionHeader decryptionHeader;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) {
        int size = VERSION_SIZE + ENCRYPTION_ALGORITHM_SIZE
                + BIT_LENGTH_SIZE + FLAGS_SIZE + ENCRYPTED_RANDOM_DATA_SIZE
                + decryptionHeader.getEncryptedRandomData().length
                + RECIPIENT_COUNT_SIZE + PASSWORD_VALIDATION_DATA_SIZE
                + decryptionHeader.getPasswordValidationData().length;

        if (!decryptionHeader.isPasswordKey())
            size += HASH_ALGORITHM_SIZE;

        out.writeWord(decryptionHeader.getIv().length);
        out.writeBytes(decryptionHeader.getIv());
        out.writeDword(size);

        out.mark(MARKER_VERSION);

        out.writeWord(decryptionHeader.getVersion());
        out.writeWord(decryptionHeader.getEncryptionAlgorithmCode());
        out.writeWord(decryptionHeader.getBitLength());
        out.writeWord(decryptionHeader.getFlags().getCode());
        out.writeWord(decryptionHeader.getEncryptedRandomData().length);
        out.writeBytes(decryptionHeader.getEncryptedRandomData());
        out.writeDword(decryptionHeader.getRecipients().size());
        assert decryptionHeader.getRecipients().isEmpty();

        if (!decryptionHeader.isPasswordKey())
            out.writeWord(decryptionHeader.getHashAlgorithmCode());

        // write recipients
        out.writeWord(decryptionHeader.getPasswordValidationData().length);
        out.writeBytes(decryptionHeader.getPasswordValidationData());

        if (out.getMarkSize(MARKER_VERSION) != size)
            throw new Zip4jvmException("DecryptionHeader size is incorrect");
    }

}
