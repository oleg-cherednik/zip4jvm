package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Function;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
@RequiredArgsConstructor
final class SecureCentralDirectoryReader implements Reader<CentralDirectory> {

    private final long offs;
    private final long totalEntries;
    private final Function<Charset, Charset> charsetCustomizer;
    private final Zip64.ExtensibleDataSector extensibleDataSector;

    private int nonce;
    private byte[] counter;

    @Override
    public CentralDirectory read(DataInput in) throws IOException {
        try {
            findHead(in);

            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);

            AesStrength strength = AesStrength.S128;
//            byte[] salt = Arrays.copyOfRange(decryptionHeader.getEncryptedRandomData(), 0, 16);
            byte[] salt = decryptionHeader.getEncryptedRandomData();
            byte[] key = createKey("1".toCharArray(), salt, strength);

            nonce = decryptionHeader.getIvSize();
            counter = new byte[decryptionHeader.getIvSize()];

            Cipher cipher = createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);
//            byte[] expected = in.readBytes(PASSWORD_CHECKSUM_SIZE);

//            byte[] buf = new byte[1024 * 4];

            byte[] buf = in.readBytes(100);
            Inflater inflater = new Inflater(true);
            cypherUpdate(buf, 0, buf.length, cipher, decryptionHeader);

            inflater.inflate(buf);
            cipher.doFinal(buf);
            inflater.setInput(buf);


            int a = 0;
            a++;


//        Decoder decoder = getEncryption().getCreateDecoderCentral().apply(extensibleDataSector, in);


//        try (DataInput inn = new BaseDataInput() {
//            @Override
//            public int read(byte[] buf, int offs, int len) throws IOException {
//                return 0;
//            }
//        }) {
//            CentralDirectory dir = new CentralDirectoryReader(inn.getOffs(), totalEntries, charsetCustomizer).read(inn);
//            return dir;
//        }
//        try (InputStream is = new CentralDirectoryInflateInputStream()) {
//
//        }
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }

        return null;

    }

    public void cypherUpdate(byte[] buf, int offs, int len, Cipher cipher, DecryptionHeader decryptionHeader) throws ShortBufferException {
        for (int i = 0; i < len; i++) {
            if (nonce == decryptionHeader.getIv().length) {
                ivUpdate(decryptionHeader);
                cipher.update(decryptionHeader.getIv(), 0, decryptionHeader.getIv().length, counter);
                nonce = 0;
            }

            buf[offs + i] ^= counter[nonce++];
        }
    }

    private void ivUpdate(DecryptionHeader decryptionHeader) {
        for (int i = 0; i < decryptionHeader.getIv().length; i++)
            if (++decryptionHeader.getIv()[i] != 0)
                break;
    }

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int keyLength = strength.getSize() * 2 + 16;
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, 1000, keyLength);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
    }

    public static Cipher createCipher(SecretKeySpec secretKeySpec) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        // use custom AES implementation, so no worry for DECRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher;
    }

    public static Mac createMac(SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKeySpec);
        return mac;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(offs);
    }

    private Encryption getEncryption() {
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();

        if (encryptionAlgorithm == EncryptionAlgorithm.AES_128)
            return Encryption.AES_128;
        if (encryptionAlgorithm == EncryptionAlgorithm.AES_192)
            return Encryption.AES_192;
        if (encryptionAlgorithm == EncryptionAlgorithm.AES_256)
            return Encryption.AES_256;

        throw new Zip4jvmException("Encryption algorithm is not supported: " + encryptionAlgorithm);
    }
}
