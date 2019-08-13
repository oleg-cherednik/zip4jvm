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
    private final byte[] passwordVerifier;

    public static AesEncoder create(@NonNull AesStrength strength, char[] password) {
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

    private final byte[] iv = new byte[AES_BLOCK_SIZE];
    private final byte[] counter = new byte[AES_BLOCK_SIZE];
    private int next = AES_BLOCK_SIZE;

    /**
     * Custom implementation of 'AES/CTR/NoPadding' is not compatible with WinZip specification. Have to implement custom one.
     *
     * @see com.sun.crypto.provider.CounterMode
     */
    private void cryptUpdate(byte[] buf, int offs, int len) {
        try {
            for (int i = 0; i < len; i++) {
                if (next == AES_BLOCK_SIZE) {
                    for (int j = 0; j < AES_BLOCK_SIZE; j++)
                        if (++iv[j] != 0)
                            break;

                    cipher.update(iv, 0, AES_BLOCK_SIZE, counter);
                    next = 0;
                }

                buf[i] ^= counter[next++];
            }
        } catch(ShortBufferException e) {
            throw new Zip4jException(e);
        }
    }

    private final byte[] aesBuf = new byte[AES_BLOCK_SIZE];
    private int aesOffs;

    @Override
    public void _write(byte[] buf, int offs, int len, DataOutput out) throws IOException {
        if (aesOffs != 0) {
            if (len >= (AES_BLOCK_SIZE - aesOffs)) {
                System.arraycopy(buf, offs, aesBuf, aesOffs, aesBuf.length - aesOffs);
                encryptAndWrite(aesBuf, 0, aesBuf.length, out);
                offs = AES_BLOCK_SIZE - aesOffs;
                len -= offs;
                aesOffs = 0;
            } else {
                System.arraycopy(buf, offs, aesBuf, aesOffs, len);
                aesOffs += len;
                len = 0;
            }
        }

        int tail = len % aesBuf.length;

        if (tail != 0) {
            System.arraycopy(buf, len + offs - tail, aesBuf, 0, tail);
            aesOffs = tail;
            len -= aesOffs;
        }

        encryptAndWrite(buf, offs, len, out);
    }

    @Override
    public void writeHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordVerifier);
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        cryptUpdate(buf, offs, len);
        mac.update(buf, offs, len);
    }

    @Override
    public void close(DataOutput out) throws IOException {
        if (aesOffs != 0)
            encryptAndWrite(aesBuf, 0, aesOffs, out);

        out.write(mac.doFinal(), 0, AES_AUTH_LENGTH);
    }

    private static byte[] generateSalt(AesStrength strength) {
//        return new byte[] {
//                (byte)0x3, (byte)0x58, (byte)0xC6, (byte)0x44, (byte)0x26,
//                (byte)0x6, (byte)0x30, (byte)0xD2, (byte)0xEF, (byte)0x2B,
//                (byte)0x2D, (byte)0x83, (byte)0x7B, (byte)0x5F, (byte)0xAC, (byte)0xCB };
        SecureRandom random = new SecureRandom();
        byte[] buf = new byte[strength.getSaltLength()];
        random.nextBytes(buf);
        return buf;
    }
}
