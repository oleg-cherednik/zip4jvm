package ru.olegcherednik.zip4jvm.crypto.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.Engine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AesEcdEngine implements Engine {

    private final Cipher cipher;

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return Quietly.doQuietly(() -> cipher.update(buf, offs, len, buf, offs));
    }

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        throw new NotImplementedException("AesEcdEngine.encrypt(byte)");
    }

    // ---------- static

    public static Cipher createCipher(DecryptionHeader decryptionHeader, char[] password, AesStrength strength) {
        return Quietly.doQuietly(() -> {
            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
            byte[] randomData = decryptRandomData(decryptionHeader, password, strength, iv);
            byte[] fileKey = getFileKey(decryptionHeader, randomData);
            Key key = strength.createSecretKeyForCipher(fileKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return cipher;
        });
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader,
                                            char[] password,
                                            AesStrength strength,
                                            IvParameterSpec iv) throws Exception {
        try {
            byte[] masterKey = getMasterKey(password);
            Key key = strength.createSecretKeyForCipher(masterKey);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
        } catch (BadPaddingException e) {
            throw new IncorrectPasswordException();
        }
    }

    private static byte[] getMasterKey(char[] password) {
        byte[] data = toByteArray(password);
        byte[] sha1 = DigestUtils.sha1(data);
        return deriveKey(sha1);
    }

    private static byte[] toByteArray(char[] arr) {
        byte[] res = new byte[arr.length];

        for (int i = 0; i < arr.length; i++)
            res[i] = (byte) (arr[i] & 0xFF);

        return res;
    }

    private static byte[] getFileKey(DecryptionHeader decryptionHeader, byte[] randomData) {
        MessageDigest md = DigestUtils.getSha1Digest();
        md.update(decryptionHeader.getIv());
        md.update(randomData);
        return deriveKey(md.digest());
    }

    private static byte[] deriveKey(byte[] digest) {
        byte[] buf = new byte[digest.length * 2];
        deriveKey(digest, (byte) 0x36, buf, 0);
        deriveKey(digest, (byte) 0x5C, buf, digest.length);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void deriveKey(byte[] digest, byte b, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, b);

        for (int i = 0; i < digest.length; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }
}
