package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
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

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AesNewDecoder implements Decoder {

    public static final int PASSWORD_VERIFIER_LENGTH = 2;

    private final Cipher cipher;
    private final Mac mac;
    private final int saltLength;

    private byte[] macKey;


    public byte[] getStoredMac() {
        return macKey;
    }

    public void setStoredMac(byte[] storedMac) {
        this.macKey = storedMac;
    }

    public int getSaltLength() {
        return saltLength;
    }

    public int getPasswordVerifierLength() {
        return PASSWORD_VERIFIER_LENGTH;
    }

    public byte[] getCalculatedAuthenticationBytes() {
        return mac.doFinal();
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static AesNewDecoder create(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password) {
        try {
            AesExtraDataRecord aesExtraDataRecord = localFileHeader.getExtraField().getAesExtraDataRecord();
            AesStrength strength = aesExtraDataRecord.getStrength();

            byte[] salt = getSalt(in, localFileHeader.getOffs(), strength);
            byte[] passwordVerifier = getPasswordVerifier(in);

            KeySpec spec = new PBEKeySpec(password, salt, 1000, strength.getSize());
            SecretKey secretKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec);
            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);

            return new AesNewDecoder(cipher, mac, salt.length);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            byte[] tmp = cipher.doFinal(buf, offs, len);
            System.arraycopy(tmp, 0, buf, offs, tmp.length);
            return len;
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private static byte[] getSalt(LittleEndianRandomAccessFile in, long offs, AesStrength strength) throws IOException {
        in.seek(offs);
        return in.readBytes(strength.getSaltLength());
    }

    private static byte[] getPasswordVerifier(LittleEndianRandomAccessFile in) throws IOException {
        return in.readBytes(2);
    }

}
