package ru.olegcherednik.zip4jvm.crypto.strong.desede;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherUtils;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.codec.digest.PureJavaCrc32;

import java.security.Key;
import java.security.SecureRandom;
import java.util.zip.Checksum;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
public class StrongTripleDesEncoderFactory implements EncoderFactory {

    private static final int IV_SIZE = 16;
    private static final int RANDOM_DATA_SIZE = 128;
    /**
     * Password validation data ({@code VData}) followed by 4 bytes of {@code VCRC32}.
     */
    private static final int VALIDATION_DATA_SIZE = 128;

    public static final StrongTripleDesEncoderFactory S112 =
            new StrongTripleDesEncoderFactory(EncryptionAlgorithm.TRIPLE_DES_112);
    public static final StrongTripleDesEncoderFactory S168 =
            new StrongTripleDesEncoderFactory(EncryptionAlgorithm.TRIPLE_DES_168);

    private final EncryptionAlgorithm encryptionAlgorithm;
    private final TripleDesStrength strength;

    protected StrongTripleDesEncoderFactory(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        strength = TripleDesStrength.of(encryptionAlgorithm.getEncryption());
    }

    // ---------- EncoderFactory ----------

    @Override
    public Encoder createEncoder(ZipEntry zipEntry) {
        return Quietly.doRuntime(() -> {
            char[] password = zipEntry.getPassword();

            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_SIZE];
            byte[] randomData = new byte[RANDOM_DATA_SIZE];
            random.nextBytes(iv);
            random.nextBytes(randomData);

            IvParameterSpec ivParam = new IvParameterSpec(iv, 0, StrongTripleDesEncoder.BLOCK_SIZE);
            byte[] encryptedRandomData = StrongCipherUtils.encryptRandomData(password, randomData, strength,
                                                                             "DESede/CBC/PKCS5Padding", ivParam);

            byte[] fileKey = StrongCipherUtils.getFileKey(iv, randomData, strength.getKeySize());
            Key key = strength.createSecretKeyForCipher(fileKey);
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);

            byte[] passwordValidationData = createPasswordValidationData(cipher, random);

            return new StrongTripleDesEncoder(encryptionAlgorithm, strength.getSize(), iv,
                                              encryptedRandomData, passwordValidationData, cipher);
        });
    }

    // ---------- static ----------

    private static byte[] createPasswordValidationData(Cipher cipher, SecureRandom random) {
        byte[] buf = new byte[VALIDATION_DATA_SIZE];
        random.nextBytes(buf);

        Checksum crc = new PureJavaCrc32();
        crc.update(buf, 0, buf.length - 4);
        long value = crc.getValue();

        buf[buf.length - 4] = (byte) value;
        buf[buf.length - 3] = (byte) (value >> 8);
        buf[buf.length - 2] = (byte) (value >> 16);
        buf[buf.length - 1] = (byte) (value >> 24);

        return Quietly.doRuntime(() -> cipher.update(buf));
    }

}
