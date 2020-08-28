package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
public class DecryptionHeaderReader implements Reader<DecryptionHeader> {

    private static final String MARKER = "DECRYPTION_HEADER";

    @Override
    public DecryptionHeader read(DataInput in) throws IOException {
        DecryptionHeader decryptionHeader = new DecryptionHeader();

        int ivSize = in.readWord();
        decryptionHeader.setIv(in.readBytes(ivSize));

        long size = in.readDword();
        in.mark(MARKER);

        decryptionHeader.setVersion(in.readWord());
        decryptionHeader.setEncryptionAlgorithm(in.readWord());
        decryptionHeader.setBitLength(in.readWord());
        decryptionHeader.setFlags(Flags.parseCode(in.readWord()));
        boolean passwordKey = decryptionHeader.getFlags() == Flags.PASSWORD_KEY;
        int encryptedRandomDataSize = in.readWord();
        decryptionHeader.setEncryptedRandomData(in.readBytes(encryptedRandomDataSize));
        int recipientCount = (int)in.readDword();

        realBigZip64(recipientCount, "recipientCount");

        decryptionHeader.setHashAlgorithm(passwordKey ? 0 : in.readWord());
        int hashSize = passwordKey ? 0x0 : in.readWord();
        decryptionHeader.setRecipients(readRecipients(recipientCount, hashSize, in));
        int passwordValidationDataSize = in.readWord();
        decryptionHeader.setPasswordValidationData(in.readBytes(passwordValidationDataSize - 4));
        decryptionHeader.setCrc32(in.readDword());

        if (in.getAbsoluteOffs() - in.getMark(MARKER) != size)
            throw new Zip4jvmException("DecryptionHeader size is incorrect");

        return decryptionHeader;
    }

    protected List<Recipient> readRecipients(int total, int hashSize, DataInput in) throws IOException {
        return new Recipients(total, hashSize).read(in);
    }

    @RequiredArgsConstructor
    private static final class Recipients implements Reader<List<Recipient>> {

        private final int total;
        private final int hashSize;

        @Override
        public List<Recipient> read(DataInput in) throws IOException {
            List<Recipient> recipients = new LinkedList<>();

            for (int i = 0; i < total; i++) {
                Recipient recipient = new Recipient();
                recipient.setSize(in.readWord());
                recipient.setHash(in.readBytes(hashSize));
                recipient.setSimpleKeyBlob(in.readBytes(recipient.getSize() - hashSize));
                recipients.add(recipient);
            }

            return recipients;
        }
    }

}
