package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.spec.KeySpec;

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
public class AesNewEncoder implements Encoder {

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] salt;
    private final byte[] derivedPasswordVerifier;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static AesNewEncoder create(@NonNull AesStrength strength, char[] password) {
        try {
            byte[] salt = generateSalt(strength);
            // TODO temporary
            int length = strength.getKeyLength() + strength.getMacLength() + AesNewDecoder.PASSWORD_VERIFIER_LENGTH;
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 1000, length * 8);
            byte[] tmp = secretKeyFactory.generateSecret(spec).getEncoded();
            byte[] derivedPasswordVerifier = { tmp[tmp.length - 2], tmp[tmp.length - 1] };
            // --

            spec = new PBEKeySpec(password, salt, 1000, strength.getSize());
            SecretKey secretKey = secretKeyFactory.generateSecret(spec);
            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            return new AesNewEncoder(cipher, mac, salt, derivedPasswordVerifier);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private static byte[] generateSalt(AesStrength strength) {
        return new byte[] { (byte)-5, (byte)76, (byte)-57, (byte)-47, (byte)-20,
                (byte)-120, (byte)54, (byte)69, (byte)102, (byte)-87,
                (byte)92, (byte)-5, (byte)48, (byte)57, (byte)-49, (byte)-88 };
//        SecureRandom random = new SecureRandom();
//        byte[] buf = new byte[strength.getSaltLength()];
//        random.nextBytes(buf);
//        return buf;
    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        try {
            byte[] tmp = cipher.doFinal(buf, offs, len);
            System.arraycopy(tmp, 0, buf, offs, tmp.length);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void writeHeader(SplitOutputStream out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(derivedPasswordVerifier);
    }
}
