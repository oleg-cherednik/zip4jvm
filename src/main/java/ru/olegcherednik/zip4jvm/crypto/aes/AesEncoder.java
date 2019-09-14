package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.NonNull;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesEncoder implements Encoder {

    private final byte[] salt;
    private final byte[] passwordChecksum;
    private final AesEngine engine;

    public static AesEncoder create(@NonNull ZipEntry entry) {
        try {
            AesStrength strength = entry.getStrength();
            byte[] salt = strength.generateSalt();
            byte[] key = AesEngine.createKey(entry.getPassword(), salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);

            return new AesEncoder(cipher, mac, salt, passwordChecksum);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @SuppressWarnings({ "AssignmentOrReturnOfFieldWithMutableType", "MethodCanBeVariableArityMethod" })
    private AesEncoder(Cipher cipher, Mac mac, byte[] salt, byte[] passwordChecksum) {
        this.salt = salt;
        this.passwordChecksum = passwordChecksum;
        engine = new AesEngine(cipher, mac);
    }

    @Override
    public void writeEncryptionHeader(@NonNull DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        try {
            engine.cypherUpdate(buf, offs, len);
            engine.updateMac(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void close(@NonNull DataOutput out) throws IOException {
        out.write(engine.getMac(), 0, MAC_SIZE);
    }

}
