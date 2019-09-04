package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.exception.Zip4jIncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAX_SIZE;
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
            throw new Zip4jException(e);
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
            throw new Zip4jException(e);
        }
    }

    @Override
    public long getCompressedSize(@NonNull ZipEntry entry) {
        return entry.getCompressedSize() - saltLength - PASSWORD_CHECKSUM_SIZE - MAX_SIZE;
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
            throw new Zip4jIncorrectPasswordException(entry.getFileName());
    }

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAX_SIZE);
        byte[] actual = ArrayUtils.subarray(engine.getMac(), 0, MAX_SIZE);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jException("Message Authentication Code (MAC) is incorrect");
    }

}
