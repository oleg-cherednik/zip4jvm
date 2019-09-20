package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmIncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesDecoder implements Decoder {

    private final int saltLength;
    private final AesEngine engine;

    public static AesDecoder create(@NonNull ZipEntry entry, @NonNull DataInput in) {
        try {
            AesStrength strength = AesEngine.getStrength(entry.getEncryption());
            byte[] salt = getSalt(entry, in);
            byte[] key = AesEngine.createKey(entry.getPassword(), salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);

            checkPasswordChecksum(passwordChecksum, entry, in);

            return new AesDecoder(cipher, mac, salt.length);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private AesDecoder(Cipher cipher, Mac mac, int saltLength) {
        this.saltLength = saltLength;
        engine = new AesEngine(cipher, mac);
    }

    @Override
    public void decrypt(@NonNull byte[] buf, int offs, int len) {
        try {
            engine.updateMac(buf, offs, len);
            engine.cypherUpdate(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public long getCompressedSize(@NonNull ZipEntry entry) {
        return entry.getCompressedSize() - saltLength - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

    @Override
    public void close(@NonNull DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    private static byte[] getSalt(ZipEntry entry, DataInput in) throws IOException {
        int saltLength = entry.getStrength().saltLength();
        return in.readBytes(saltLength);
    }

    private static void checkPasswordChecksum(byte[] actual, ZipEntry entry, DataInput in) throws IOException {
        byte[] expected = in.readBytes(PASSWORD_CHECKSUM_SIZE);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jvmIncorrectPasswordException(entry.getFileName());
    }

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAC_SIZE);
        byte[] actual = ArrayUtils.subarray(engine.getMac(), 0, MAC_SIZE);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is incorrect");
    }

}
