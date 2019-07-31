package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.pbkdf2.MacBasedPRF;
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
    private final MacBasedPRF mac1;
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

            byte[] macKey = new byte[strength.getMacLength()];
            byte[] derivedPasswordVerifier = new byte[AesNewDecoder.PASSWORD_VERIFIER_LENGTH];

            System.arraycopy(tmp, strength.getKeyLength(), macKey, 0, macKey.length);
            System.arraycopy(tmp, strength.getKeyLength() + macKey.length, derivedPasswordVerifier, 0, AesNewDecoder.PASSWORD_VERIFIER_LENGTH);

            // --

            spec = new PBEKeySpec(password, salt, 1000, strength.getSize());
            SecretKey secretKey = secretKeyFactory.generateSecret(spec);
            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            // TODO temporary

            MacBasedPRF mac1 = new MacBasedPRF("HmacSHA1");
            mac1.init(macKey);

            return new AesNewEncoder(cipher, mac, mac1, salt, derivedPasswordVerifier);
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
            byte[] tmp = cipher.doFinal(buf, offs, len);
            System.arraycopy(tmp, 0, buf, offs, tmp.length);
            mac.update(buf, offs, len);
            mac1.update(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void writeHeader(SplitOutputStream out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(derivedPasswordVerifier);
    }

    @Override
    public void close(SplitOutputStream out) throws IOException {
        byte[] buf = mac.doFinal();
//        out.writeBytes(macBytes);
        out.writeBytes(getFinalMac());
    }

    public byte[] getFinalMac() {
        byte[] rawMacBytes = mac1.doFinal();
        byte[] macBytes = new byte[10];
        System.arraycopy(rawMacBytes, 0, macBytes, 0, 10);
        return macBytes;
    }
}
