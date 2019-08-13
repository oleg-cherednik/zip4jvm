package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.exception.Zip4jIncorrectPasswordException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.spec.KeySpec;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;
import static com.cop.zip4j.crypto.aes.AesEngine.PASSWORD_VERIFIER_LENGTH;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesDecoder implements Decoder {

    private final Cipher cipher;
    private final Mac mac;
    private final int saltLength;

    @Setter
    private byte[] macKey;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static AesDecoder create(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, @NonNull char[] password) {
        try {
            AesExtraDataRecord aesExtraDataRecord = localFileHeader.getExtraField().getAesExtraDataRecord();
            AesStrength strength = aesExtraDataRecord.getStrength();

            byte[] salt = getSalt(in, localFileHeader);

            // TODO temporary
            int length = strength.getKeyLength() + strength.getMacLength() + PASSWORD_VERIFIER_LENGTH;
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 1000, length * 8);
            byte[] tmp = secretKeyFactory.generateSecret(spec).getEncoded();

            byte[] macKey = new byte[strength.getMacLength()];
            byte[] derivedPasswordVerifier = new byte[PASSWORD_VERIFIER_LENGTH];

            System.arraycopy(tmp, strength.getKeyLength(), macKey, 0, macKey.length);
            System.arraycopy(tmp, strength.getKeyLength() + macKey.length, derivedPasswordVerifier, 0, PASSWORD_VERIFIER_LENGTH);

            checkPasswordChecksum(derivedPasswordVerifier, in, localFileHeader);

            in.seek(localFileHeader.getOffs() + strength.getSaltLength() + PASSWORD_VERIFIER_LENGTH);

            // --

            spec = new PBEKeySpec(password, salt, 1000, strength.getSize());
            SecretKey secretKey = secretKeyFactory.generateSecret(spec);
            byte[] iv = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new IvParameterSpec(iv));

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(macKey, "HmacSHA1"));

            return new AesDecoder(cipher, mac, salt.length);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        try {
            mac.update(buf, offs, len);
            byte[] tmp = cipher.doFinal(buf, offs, len);
            System.arraycopy(tmp, 0, buf, offs, tmp.length);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public int getLen(long bytesRead, int len, long length) {
        return bytesRead + len < length && len % 16 != 0 ? len - len % 16 : len;
    }

    @Override
    public long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize() - getSaltLength() - PASSWORD_VERIFIER_LENGTH - 10;
    }

    @Override
    public long getOffs(@NonNull LocalFileHeader localFileHeader) {
        // TODO why don;t have MAC SIZE
        return localFileHeader.getOffs() + getSaltLength() + PASSWORD_VERIFIER_LENGTH; // + MAC SIZE
    }

    @Override
    public void close(DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    private static byte[] getSalt(DataInput in, LocalFileHeader localFileHeader) throws IOException {
        int saltLength = localFileHeader.getExtraField().getAesExtraDataRecord().getStrength().getSaltLength();
        in.seek(localFileHeader.getOffs());
        return in.readBytes(saltLength);
    }

    private static void checkPasswordChecksum(byte[] actual, DataInput in, LocalFileHeader localFileHeader) throws IOException {
        int saltLength = localFileHeader.getExtraField().getAesExtraDataRecord().getStrength().getSaltLength();
        in.seek(localFileHeader.getOffs() + saltLength);
        byte[] expected = in.readBytes(PASSWORD_VERIFIER_LENGTH);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jIncorrectPasswordException(localFileHeader.getFileName());
    }

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(AES_AUTH_LENGTH);
        byte[] actual = ArrayUtils.subarray(mac.doFinal(), 0, AES_AUTH_LENGTH);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jException("Message Authentication Code (MAC) is incorrect");
    }

}
