package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import tangible.cpp.seven_zip.crypto.NCrypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
 * ZipStrong.cpp:100
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
        int _remSize = (int)size;
        in.mark(MARKER);

        decryptionHeader.setFormat(in.readWord()); // +0
        decryptionHeader.setEncryptionAlgorithm(in.readWord()); // +2
        decryptionHeader.setBitLength(in.readWord()); // +4
        decryptionHeader.setFlags(Flags.parseCode(in.readWord())); // +6
        boolean passwordKey = decryptionHeader.getFlags() == Flags.PASSWORD_KEY;
        int encryptedRandomDataSize = in.readWord(); // +8
        int rdSize = encryptedRandomDataSize;
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

            in.seek(MARKER);
            byte[] _bufAligned = new byte[_remSize];
            in.read(_bufAligned, 0, _bufAligned.length);

            byte[] p = new byte[rdSize];
            System.arraycopy(_bufAligned, 10, p, 0, rdSize);


//            final int kPadSize = 16;

//            if (rdSize < kPadSize)
//                return null;
//            if ((rdSize & (kPadSize - 1)) != 0)
//                return null;

//            int validSize = passwordValidationDataSize;

//            if ((validSize & 0xF) != 0/* || validOffset + validSize != _remSize*/)
//                return null;

//            NCrypto.NZipStrong.CDecoder coder = new NCrypto.NZipStrong.CDecoder(AesStrength.S128.keyLength());
//            coder.CryptoSetPassword("1".getBytes(), "1".getBytes().length);
//            coder._key.KeySize = AesStrength.S128.keyLength();    // 16 + (algId - kAES128) * 2

//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            SecretKeySpec secretKeySpec = new SecretKeySpec(coder._key.MasterKey, "AES");
//            AlgorithmParameterSpec paramSpec = new IvParameterSpec(decryptionHeader.getIv());
//            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
//
//            cipher.update(_bufAligned1, 0, rdSize);
//            byte[] fi = cipher.doFinal();

//            rdSize -= kPadSize;

//            for (int i = 0; i < kPadSize; i++)
//                if (p[rdSize + i] != kPadSize)
//                    return null; // passwOK = false;

//            IvParameterSpec ivParameterSpec = new IvParameterSpec(decryptionHeader.getIv());
//            byte[] randomData = decryptionHeader.getEncryptedRandomData();//in.readBytes(strength.saltLength());
//            byte[] salt = Arrays.copyOfRange(randomData, 0, strength.saltLength());
//
//            byte[] key = AesEngine.createKey1("1".toCharArray(), salt, strength);
//
//
//            Cipher cipher = AesEngine.createCipher1(strength.createSecretKeyForCipher(key), ivParameterSpec);
//            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
//            byte[] passwordChecksum = strength.createPasswordChecksum(key);

//            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
//            SecretKeySpec skeySpec = new SecretKeySpec(coder._key.MasterKey, "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
//            byte[] original = cipher.doFinal(p);
//
//            int a = 0;
//            a++;
//
//            coder.SetKey(coder._key.MasterKey, coder._key.KeySize);
//            coder.SetInitVector(decryptionHeader.getIv(), 16);
//            coder.Init();
//            coder.Filter(p, rdSize);
//            rdSize -= kPadSize;
//
//            for (int i = 0; i < kPadSize; i++)
//                if (p[rdSize + i] != kPadSize)
//                    return null; // passwOK = false;
//
//
//            byte[] masterKey = setPassword("1");
//
//            MessageDigest sha = MessageDigest.getInstance("SHA-1");
//            sha.update(decryptionHeader.getIv());
//            sha.update(decryptionHeader.getEncryptedRandomData(), 0, encryptedRandomDataSize - 16);
//
//            byte[] fileKey = DeriveKey(sha);    // 32
//
//
//            Checksum checksum = new CRC32();
//            checksum.update(decryptionHeader.getPasswordValidationData(), 0, decryptionHeader.getPasswordValidationData().length);
//            long crc = checksum.getValue();
//            // get the current checksum value
//
//            long checksumValue = checksum.getValue();
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }

        if (in.getOffs() - in.getMark(MARKER) != size)
            throw new Zip4jvmException("DecryptionHeader size is incorrect");

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

        for (int j = 0; j < one.length; j++)
            key[i++] = one[j];
        for (int j = 0; j < two.length; j++)
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
