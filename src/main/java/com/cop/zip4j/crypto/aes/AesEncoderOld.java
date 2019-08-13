package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.spec.KeySpec;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;
import static com.cop.zip4j.crypto.aes.AesEngine.PASSWORD_VERIFIER_LENGTH;

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
public final class AesEncoderOld implements Encoder {

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] salt;
    private final byte[] derivedPasswordVerifier;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static AesEncoderOld create(@NonNull AesStrength strength, char[] password) {
        try {
            byte[] salt = generateSalt(strength);

            // TODO temporary
            int length = strength.getKeyLength() + strength.getMacLength() + PASSWORD_VERIFIER_LENGTH;
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 1000, length * 8);
            byte[] tmp = secretKeyFactory.generateSecret(spec).getEncoded();

            byte[] macKey = new byte[strength.getMacLength()];
            byte[] derivedPasswordVerifier = new byte[PASSWORD_VERIFIER_LENGTH];

            System.arraycopy(tmp, strength.getKeyLength(), macKey, 0, macKey.length);
            System.arraycopy(tmp, strength.getKeyLength() + macKey.length, derivedPasswordVerifier, 0, PASSWORD_VERIFIER_LENGTH);

            // ----

            KeySpec keySpec = new PBEKeySpec(password, salt, 1000, strength.getSize());
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            byte[] keyBytes = secretKey.getEncoded();
//            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(macKey, "HmacSHA1"));
            return new AesEncoderOld(cipher, mac, salt, derivedPasswordVerifier);


//            KeySpec keySpec = new PBEKeySpec(password, salt, 1000, strength.getSize());
//            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
//            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
//
//            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));
//
//            Mac mac = Mac.getInstance("HmacSHA1");
//            mac.init(new SecretKeySpec(macKey, "HmacSHA1"));
//            return new AesEncoder(cipher, mac, salt, derivedPasswordVerifier);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private static byte[] generateSalt(AesStrength strength) {
        return new byte[] {
                (byte)0x3, (byte)0x58, (byte)0xC6, (byte)0x44, (byte)0x26,
                (byte)0x6, (byte)0x30, (byte)0xD2, (byte)0xEF, (byte)0x2B,
                (byte)0x2D, (byte)0x83, (byte)0x7B, (byte)0x5F, (byte)0xAC, (byte)0xCB };
//        SecureRandom random = new SecureRandom();
//        byte[] buf = new byte[strength.getSaltLength()];
//        random.nextBytes(buf);
//        return buf;
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        try {
            cipher.update(buf, offs, len, buf);
            mac.update(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private final byte[] aesBuf = new byte[AES_BLOCK_SIZE];
    private int aesOffs;

    @Override
    public void _write(byte[] buf, int offs, int len, DataOutput out) throws IOException {
//        if (aesOffs != 0) {
//            if (len >= (AES_BLOCK_SIZE - aesOffs)) {
//                System.arraycopy(buf, offs, aesBuf, aesOffs, aesBuf.length - aesOffs);
//                encryptAndWrite(aesBuf, 0, aesBuf.length, out);
//                offs = AES_BLOCK_SIZE - aesOffs;
//                len -= offs;
//                aesOffs = 0;
//            } else {
//                System.arraycopy(buf, offs, aesBuf, aesOffs, len);
//                aesOffs += len;
//                len = 0;
//            }
//        }
//
//        int tail = len % aesBuf.length;
//
//        if (tail != 0) {
//            System.arraycopy(buf, len + offs - tail, aesBuf, 0, tail);
//            aesOffs = tail;
//            len -= aesOffs;
//        }

        encryptAndWrite(buf, offs, len, out);
    }

    @Override
    public void writeHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(derivedPasswordVerifier);
    }

    @Override
    public void close(DataOutput out) throws IOException {
//        if (aesOffs != 0)
//            encryptAndWrite(aesBuf, 0, aesOffs, out);

        out.write(mac.doFinal(), 0, AES_AUTH_LENGTH);
    }

}
