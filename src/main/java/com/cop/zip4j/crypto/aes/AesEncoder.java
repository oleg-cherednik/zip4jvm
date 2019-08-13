package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.security.SecureRandom;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;

@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor
public final class AesEncoder implements Encoder {

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] salt;
    private final byte[] passwordChecksum;

    public static AesEncoder create(@NonNull AesStrength strength, @NonNull char[] password) {
        try {
            byte[] salt = generateSalt(strength);
            byte[] key = AesEngine.createKey(password, salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);

            return new AesEncoder(cipher, mac, salt, passwordChecksum);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void writeHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        try {
            cypherUpdate(buf, offs, len);
            mac.update(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private final byte[] iv = new byte[AES_BLOCK_SIZE];
    private final byte[] counter = new byte[AES_BLOCK_SIZE];
    private int nonce = AES_BLOCK_SIZE;

    /**
     * Custom implementation (com.sun.crypto.provider.CounterMode) of 'AES/CTR/NoPadding' is not compatible with WinZip specification.
     * Have to implement custom one.
     */
    private void cypherUpdate(byte[] buf, int offs, int len) throws ShortBufferException {
        for (int i = 0; i < len; i++) {
            if (nonce == iv.length) {
                ivUpdate();
                cipher.update(iv, 0, iv.length, counter);
                nonce = 0;
            }

            buf[offs + i] ^= counter[nonce++];
        }
    }

    private void ivUpdate() {
        for (int i = 0; i < iv.length; i++)
            if (++iv[i] != 0)
                break;
    }

    @Override
    public void close(DataOutput out) throws IOException {
        out.write(mac.doFinal(), 0, AES_AUTH_LENGTH);
    }

    private static byte[] generateSalt(AesStrength strength) {
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[strength.getSaltLength()];
        random.nextBytes(buf);
        return buf;
    }
}
