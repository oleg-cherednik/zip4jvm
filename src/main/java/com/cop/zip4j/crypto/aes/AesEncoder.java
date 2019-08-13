package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;

/**
 * byte[] iv = new byte[128/8];
 * new SecureRandom().nextBytes(iv);
 * IvParameterSpec ivspec = new IvParameterSpec(iv);
 * <p>
 * KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
 * SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
 * <p>
 * Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
 */
@RequiredArgsConstructor
public final class AesEncoder implements Encoder {

    public static final int ITERATION_COUNT = 1000;
    public static final int BLOCK_SIZE = 16;

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] passwordVerifier;
    private final byte[] salt;

    /* State for implementing AES-CTR. */
    private final byte[] iv = new byte[BLOCK_SIZE];
    private final byte[] keystream = new byte[BLOCK_SIZE];
    private int next = BLOCK_SIZE;

    public static AesEncoder create(@NonNull AesStrength strength, char[] password) {
        return new AesEncoder(password, strength.getSize(), createSalt(strength.getSaltLength()));
    }

    private static final Random RANDOM = new SecureRandom();

    private static byte[] createSalt(int size) {
        byte[] salt = new byte[size];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public AesEncoder(char[] passwordChars, int keySize, byte[] salt) {
        if (keySize != 128 && keySize != 192 && keySize != 256)
            throw new IllegalArgumentException("Illegal keysize: " + keySize);

        try {
            this.salt = salt;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec keySpec = new PBEKeySpec(passwordChars, salt, ITERATION_COUNT, keySize * 2 + 16);
            SecretKey sk = skf.generateSecret(keySpec);
            byte[] keyBytes = sk.getEncoded();

            cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, 0, keySize / 8, "AES");

            //int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            //System.out.println( "maxKeyLen=" + maxKeyLen );

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, keySize / 8, keySize / 8, "HmacSHA1"));

            passwordVerifier = new byte[2];
            System.arraycopy(keyBytes, 2 * (keySize / 8), passwordVerifier, 0, 2);
        } catch(NoSuchAlgorithmException e) {
            /*
             * XXX(mdempsky): Could happen if the user's JRE doesn't support PBKDF2,
             * AES, and/or HMAC-SHA1.  Throw a better exception?
             */
            throw new Error();
        } catch(InvalidKeyException e) {
            e.printStackTrace();
            /* Shouldn't happen: our key specs match our algorithms. */
            throw new Error();
        } catch(InvalidKeySpecException e) {
            /* Shouldn't happen: our key specs match our algorithms. */
            throw new Error();
        } catch(NoSuchPaddingException e) {
            /* Shouldn't happen: we don't specify any padding schemes. */
            throw new Error();
        }
    }

    public void cryptUpdate(byte[] in, int length) {
        try {
            /*
             * We must implement CTR mode by hand, because WinZip's AES encryption
             * scheme is incompatible with Java's AES/CTR/NoPadding.
             */
            for (int i = 0; i < length; ++i) {
                /*
                 * If we've exhausted the current keystream block, we need to
                 * increment the iv and generate another one.
                 */
                if (next == BLOCK_SIZE) {
                    for (int j = 0; j < BLOCK_SIZE; ++j)
                        if (++iv[j] != 0)
                            break;
                    cipher.update(iv, 0, BLOCK_SIZE, keystream);
                    next = 0;
                }

                in[i] ^= keystream[next++];
            }
        } catch(ShortBufferException e) {
            /* Shouldn't happen: our output buffer is always appropriately sized. */
            throw new Error();
        }
    }

    public void authUpdate(byte[] in, int length) {
        mac.update(in, 0, length);
    }

    public byte[] getFinalAuthentifier() {
        byte[] auth = new byte[10];
        System.arraycopy(mac.doFinal(), 0, auth, 0, 10);
        return auth;
    }

    public byte[] getPasswordVerifier() {
        return passwordVerifier;
    }


    // ------------------------

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
        cryptUpdate(buf, len);
        authUpdate(buf, len);
    }

    @Override
    public void close(DataOutput out) throws IOException {
        if (aesOffs != 0)
            encryptAndWrite(aesBuf, 0, aesOffs, out);

        out.write(mac.doFinal(), 0, AES_AUTH_LENGTH);
    }
}
