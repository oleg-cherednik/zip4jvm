package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import ru.olegcherednik.zip4jvm.crypto.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 31.08.2023
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesCentralDirectoryDecoder implements CentralDirectoryDecoder {

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    private final Cipher cipher;

    public static AesCentralDirectoryDecoder create(char[] password, Endianness endianness, DecryptionHeader decryptionHeader) {
        AesStrength strength = AesEngine.getStrength(decryptionHeader.getEncryptionAlgorithm().getEncryptionMethod());
        Cipher cipher = createCipher(password, decryptionHeader, strength);
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, endianness);

        if (expected != actual)
            throw new IncorrectPasswordException("Central Directory");

        return new AesCentralDirectoryDecoder(cipher);
    }

    // ---------- CentralDirectoryCipher ----------

    @Override
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public byte[] decrypt(byte[] buf) {
        return cipher.update(buf);
    }

    // ---------- static ----------

    private static Cipher createCipher(char[] password, DecryptionHeader decryptionHeader, AesStrength strength) {
        return Quietly.doQuietly(() -> {
            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
            byte[] randomData = decryptRandomData(password, decryptionHeader.getEncryptedRandomData(), strength, iv);
            byte[] fileKey = getFileKey(decryptionHeader, randomData);
            Key key = strength.createSecretKeyForCipher(fileKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return cipher;
        });
    }

    private static byte[] decryptRandomData(char[] password,
                                            byte[] encryptedRandomData,
                                            AesStrength strength,
                                            IvParameterSpec iv) throws Exception {
        byte[] masterKey = getMasterKey(password);
        Key key = strength.createSecretKeyForCipher(masterKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(encryptedRandomData);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] getMasterKey(char[] password) {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] toByteArray(char[] arr) {
        byte[] res = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
            res[i] = (byte)((int)arr[i] & 0xFF);

        return res;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest());
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
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
