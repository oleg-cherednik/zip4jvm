package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
@RequiredArgsConstructor
@SuppressWarnings("MethodCanBeVariableArityMethod")
public final class AesStrongDecoder implements Decoder {

    private static final String DECRYPTION_HEADER = AesStrongDecoder.class.getSimpleName() + ".decryptionHeader";

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    private final Cipher cipher;
    private final int decryptionHeaderSize;
    private boolean eof;

    public static Decoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            in.mark(DECRYPTION_HEADER);

            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            final int decryptionHeaderSize = (int)(in.getAbsoluteOffs() - in.getMark(DECRYPTION_HEADER));

            Cipher cipher = createCipher(decryptionHeader, zipEntry.getPassword());
            byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

            long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
            long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData);

            if (expected != actual)
                throw new IncorrectPasswordException(zipEntry.getFileName());

            return new AesStrongDecoder(cipher, decryptionHeaderSize);
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(BadPaddingException e) {
            throw new IncorrectPasswordException(zipEntry.getFileName());
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Cipher createCipher(DecryptionHeader decryptionHeader, char[] password) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
        byte[] randomData = decryptRandomData(decryptionHeader, password, iv);
        byte[] fileKey = getFileKey(decryptionHeader, randomData);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(fileKey, "AES"), iv);

        return cipher;
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader, char[] password, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getMasterKey(password), "AES"), iv);
        return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
    }

    private static byte[] getMasterKey(char[] password) {
        byte[] data = new String(password).getBytes(StandardCharsets.UTF_8);
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

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            if (eof)
                return IOUtils.EOF;

            cipher.update(buf, offs, len, buf, offs);
            int unpadLength = getUnpadLength(buf, offs, len);

            if (unpadLength != len)
                eof = true;

            return unpadLength;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    /*
     * If the block length is B then add N padding bytes of value N to make the input length up to the next exact
     * multiple of B. If the input length is already an exact multiple of B then add B bytes of value B. Thus padding
     * of length N between one and B bytes is always added in an unambiguous manner. After decrypting, check that the
     * last N bytes of the decrypted data all have value N with 1 < N ≤ B. If so, strip N bytes, otherwise throw a
     * decryption error.
     */
    private int getUnpadLength(byte[] buf, int offs, int len) {
        int n = buf[offs + len - 1];

        if (n <= 0 || n > cipher.getBlockSize())
            return len;

        for (int i = offs + len - n; i < offs + len; i++)
            if (buf[i] != n)
                return len;

        return len - n;
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize - decryptionHeaderSize;
    }
}
