package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
@RequiredArgsConstructor
public final class AesStrongDecoder implements Decoder {

    private static final String DECRYPTION_HEADER = AesStrongDecoder.class.getSimpleName() + ".decryptionHeader";

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    //    private final Cipher cipher;
    private final MyAes aes;
    private final int decryptionHeaderSize;

    public static Decoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            in.mark(DECRYPTION_HEADER);

            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            final int decryptionHeaderSize = (int)(in.getAbsoluteOffs() - in.getMark(DECRYPTION_HEADER));

            byte[] passwordValidationData = decryptPasswordValidationData(decryptionHeader, zipEntry.getPassword());
            long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
            long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData);

            if (expected != actual)
                throw new IncorrectPasswordException(zipEntry.getFileName());

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] masterKey = getMasterKey(zipEntry.getPassword());
            byte[] randomData = decryptRandomData(decryptionHeader, zipEntry.getPassword());
            byte[] fileKey1 = getFileKey(decryptionHeader, randomData);

            SecretKey secretKey = new SecretKeySpec(masterKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(decryptionHeader.getIv()));


            MyAes aes = new MyAes();
            aes.init(false, 0);
            aes.SetKey(getMasterKey(zipEntry.getPassword()));
            aes.SetInitVector(decryptionHeader.getIv());

            byte[] decrypted1 = aes.filter(decryptionHeader.getEncryptedRandomData());

            int kPadSize = MyAes.AES_BLOCK_SIZE;
            int rdSize = decryptionHeader.getEncryptedRandomData().length - kPadSize;


            MessageDigest md = DigestUtils.getSha1Digest();
            md.update(decryptionHeader.getIv());
            md.update(decrypted1, 0, rdSize);
            byte[] fileKey = deriveKey(md.digest());

            aes.SetKey(fileKey);
            aes.SetInitVector(decryptionHeader.getIv());

            byte[] pwd = decryptionHeader.getPasswordValidationData();
            byte[] decrypted2 = aes.filter(pwd);

            CRC32 crc = new CRC32();
            crc.update(decrypted2, 0, pwd.length - 4);

            if (MyAes.GetUi32(decrypted2, decrypted2.length - 4) != crc.getValue())
                throw new RuntimeException();

            return new AesStrongDecoder(aes, decryptionHeaderSize);
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(BadPaddingException e) {
            throw new IncorrectPasswordException(zipEntry.getFileName());
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static byte[] decryptRandomData(DecryptionHeader decryptionHeader, char[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(getMasterKey(password), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(decryptionHeader.getIv()));
        return cipher.doFinal(decryptionHeader.getEncryptedRandomData());
    }

    private static byte[] decryptPasswordValidationData(DecryptionHeader decryptionHeader, byte[] fileKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(fileKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(decryptionHeader.getIv()));
        return cipher.doFinal(decryptionHeader.getPasswordValidationData());
    }

    private static byte[] decryptPasswordValidationData(DecryptionHeader decryptionHeader, char[] password) throws Exception {
        byte[] randomData = decryptRandomData(decryptionHeader, password);
        byte[] fileKey = getFileKey(decryptionHeader, randomData);
        return decryptPasswordValidationData(decryptionHeader, fileKey);
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

    private static void deriveKey(byte[] digest, byte c, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, c);

        for (int i = 0; i < SHA1_DIGEST_SIZE; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            byte[] dec = aes.filter(buf, offs, len);
            System.arraycopy(dec, 0, buf, offs, dec.length);
            return len;
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
    @Override
    public int getDecodedDataSize(byte[] buf, int offs, int len) {
        return len - buf[offs + len - 1];
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize - decryptionHeaderSize;
    }
}
