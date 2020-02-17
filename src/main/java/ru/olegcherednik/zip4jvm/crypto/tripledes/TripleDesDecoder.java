package ru.olegcherednik.zip4jvm.crypto.tripledes;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
public final class TripleDesDecoder implements Decoder {

    public static TripleDesDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(decryptionHeader.getIv());
            //168
            int bitLength = decryptionHeader.getDecryptionInfo().getBitLength();
            byte[] salt = decryptionHeader.getDecryptionInfo().getEncryptedRandomData();

            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            final byte[] digestOfPassword = md.digest("1".getBytes(StandardCharsets.UTF_8));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                keyBytes[k++] = keyBytes[j++];
            }
            final SecretKey key = new SecretKeySpec(keyBytes, "DESede");

            KeySpec keySpec = new DESedeKeySpec(digestOfPassword);

//            SecretKeySpec secretKeySpec = new SecretKeySpec(key, 0, keyLength(), "AES")

//            String sha1 = DigestUtils.sha1Hex("1");
            Cipher decipherDES1 = Cipher.getInstance("DESede/CBC/PKCS5Padding");
//            decipherDES1.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            TripleDesStrength strength = TripleDesEngine.getStrength(zipEntry.getEncryptionMethod());


            String myEncryptionKey = "1";
            String myEncryptionScheme = "DESede";
            byte[] arrayBytes = myEncryptionKey.getBytes(Charsets.UTF_8);
            KeySpec ks = new DESedeKeySpec(arrayBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(myEncryptionScheme);
            Cipher cipher = Cipher.getInstance(myEncryptionScheme);
//            SecretKey key = skf.generateSecret(ks);

//            cipher.init(Cipher.DECRYPT_MODE, key);
            return new TripleDesDecoder(cipher);
        } catch(Exception e) {
            throw new IOException(e);
        }
    }

    private final TripleDesEngine engine;

    private TripleDesDecoder(Cipher cipher) {
        engine = new TripleDesEngine(cipher);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        engine.cypherUpdate(buf, offs, len);
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return 0;
    }
}
