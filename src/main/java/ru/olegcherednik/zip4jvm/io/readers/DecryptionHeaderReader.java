package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * ZipHandler.cpp:1150
 * ZipString.cpp:100
 *
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

        try {

            byte[] masterKey = setPassword("1");

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(decryptionHeader.getIv());
            sha.update(decryptionHeader.getEncryptedRandomData(), 0, encryptedRandomDataSize - 16);

            byte[] fileKey = DeriveKey(sha);    // 32


            Checksum checksum = new CRC32();
            checksum.update(decryptionHeader.getPasswordValidationData(), 0, decryptionHeader.getPasswordValidationData().length);
            long crc = checksum.getValue();
            // get the current checksum value

            long checksumValue = checksum.getValue();

            if (in.getOffs() - in.getMark(MARKER) != size)
                throw new Zip4jvmException("DecryptionHeader size is incorrect");
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }

        return decryptionHeader;
    }

    public static byte[] setPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        sha.update(password.getBytes());
        return DeriveKey(sha);
    }

    private static final int SHA1_NUM_BLOCK_WORDS = 16;
    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_BLOCK_SIZE = SHA1_NUM_BLOCK_WORDS * 4;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    public static byte[] DeriveKey(MessageDigest sha) throws NoSuchAlgorithmException {
        byte[] digest = sha.digest();//new byte[SHA1_DIGEST_SIZE];
        byte[] one = DeriveKey2(digest, (byte)0x36);
        byte[] two = DeriveKey2(digest, (byte)0x36);
        byte[] key = new byte[one.length + two.length];

        int i = 0;

        for(int j = 0; j < one.length; j++)
            key[i++] = one[j];
        for(int j = 0; j < two.length; j++)
            key[i++] = two[j];

        return key;
    }

    public static byte[] DeriveKey2(byte[] digest, byte c) throws NoSuchAlgorithmException {
        byte[] buf = new byte[64];
        Arrays.fill(buf, c);

        for (int i = 0; i < digest.length; i++)
            buf[i] ^= digest[i];

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        sha.reset();
        sha.update(buf);
        return sha.digest();
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
