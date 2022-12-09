package ru.olegcherednik.zip4jvm.crypto.strong;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrongDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
public final class DecryptionHeaderDecoder {

    public static final String MARKER = DecryptionHeaderReader.MARKER;

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    public Cipher readAndCreateCipher(DataInput in, char[] password) throws Exception {
        DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
        AesStrength strength = AesEngine.getStrength(decryptionHeader.getEncryptionAlgorithm().getEncryptionMethod());
        Cipher cipher = createCipher(decryptionHeader, strength, password);
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData);

        if (expected != actual)
            throw new IncorrectPasswordException("");

        return cipher;
    }

    private static Cipher createCipher(DecryptionHeader decryptionHeader, AesStrength strength, char[] password) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
        byte[] randomData = decryptRandomData(decryptionHeader, strength, password, iv);
        byte[] fileKey = getFileKey(decryptionHeader, randomData);
        Key key = strength.createSecretKeyForCipher(fileKey);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        return cipher;
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader, AesStrength strength, char[] password, IvParameterSpec iv)
            throws Exception {
        byte[] masterKey = getMasterKey(password);
        Key key = strength.createSecretKeyForCipher(masterKey);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
    }

    private static byte[] getMasterKey(char[] password) {
        byte[] data = ArrayUtils.isEmpty(password) ? ArrayUtils.EMPTY_BYTE_ARRAY : new String(password).getBytes(StandardCharsets.UTF_8);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1);
    }

    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest());
    }

    private static byte[] deriveKey(byte[] digest) {
        byte[] buf = new byte[SHA1_DIGEST_SIZE * 2];
        deriveKey(digest, (byte)0x36, buf, 0);
        deriveKey(digest, (byte)0x5C, buf, SHA1_DIGEST_SIZE);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void deriveKey(byte[] digest, byte b, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < SHA1_DIGEST_SIZE; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }
}
