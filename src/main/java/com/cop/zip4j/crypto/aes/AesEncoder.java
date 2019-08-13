package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;
import java.security.SecureRandom;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesEncoder implements Encoder {

    private final byte[] salt;
    private final byte[] passwordChecksum;
    private final AesEngine engine;

    public static AesEncoder create(@NonNull PathZipEntry entry) {
        try {
            AesStrength strength = entry.getStrength();
            byte[] salt = generateSalt(strength);
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
    public void writeHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        try {
            engine.cypherUpdate(buf, offs, len);
            engine.updateMac(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void close(DataOutput out) throws IOException {
        out.write(engine.getMac(), 0, AES_AUTH_LENGTH);
    }

    private static byte[] generateSalt(AesStrength strength) {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[strength.getSaltLength()];
        random.nextBytes(buf);
        return buf;
    }
}
