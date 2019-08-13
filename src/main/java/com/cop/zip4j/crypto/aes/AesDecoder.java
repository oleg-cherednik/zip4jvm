package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.exception.Zip4jIncorrectPasswordException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import java.io.IOException;

import static com.cop.zip4j.crypto.aes.AesEngine.AES_AUTH_LENGTH;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_PASSWORD_VERIFIER_LENGTH;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
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
            AesStrength strength = localFileHeader.getExtraField().getAesExtraDataRecord().getStrength();
            byte[] salt = getSalt(in, localFileHeader);
            byte[] key = AesEngine.createKey(password, salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);

            checkPasswordChecksum(passwordChecksum, in, localFileHeader);
            in.seek(localFileHeader.getOffs() + strength.getSaltLength() + AES_PASSWORD_VERIFIER_LENGTH);

            return new AesDecoder(cipher, mac, salt.length);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        try {
            mac.update(buf, offs, len);
            cypherUpdate(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private final byte[] iv = new byte[AES_BLOCK_SIZE];
    private final byte[] counter = new byte[iv.length];
    private int nonce = iv.length;

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
    public int getLen(long bytesRead, int len, long length) {
        return bytesRead + len < length && len % 16 != 0 ? len - len % 16 : len;
    }

    @Override
    public long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize() - getSaltLength() - AES_PASSWORD_VERIFIER_LENGTH - 10;
    }

    @Override
    public long getOffs(@NonNull LocalFileHeader localFileHeader) {
        // TODO why don;t have MAC SIZE
        return localFileHeader.getOffs() + getSaltLength() + AES_PASSWORD_VERIFIER_LENGTH; // + MAC SIZE
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
        byte[] expected = in.readBytes(AES_PASSWORD_VERIFIER_LENGTH);

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
