package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
@RequiredArgsConstructor
public final class AesStrongDecoder implements Decoder {

    private static final String DECRYPTION_HEADER = "AesStrongDecoder.DECRYPTION_HEADER";

    private final Cipher cipher;
    private final long compressedSize;
    private final int decryptionHeaderSize;

    private long decryptedBytes;

    public static AesStrongDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            in.mark(DECRYPTION_HEADER);
            Cipher cipher = new DecryptionHeaderDecoder(zipEntry.getPassword()).readAndCreateCipher(in);
            int decryptionHeaderSize = (int)in.getMarkSize(DECRYPTION_HEADER);
            long compressedSize = zipEntry.getCompressedSize() - decryptionHeaderSize;
            return new AesStrongDecoder(cipher, compressedSize, decryptionHeaderSize);
        } catch(IncorrectPasswordException | BadPaddingException e) {
            throw new IncorrectPasswordException("Central Directory");
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            if (decryptedBytes >= compressedSize || len <= 0)
                return 0;

            decryptedBytes += len;
            len = cipher.update(buf, offs, len, buf, offs);
            return decryptedBytes < compressedSize ? len : unpad(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static int unpad(byte[] buf, int offs, int len) {
        int n = buf[offs + len - 1];

        for (int i = offs + len - n; i < offs + len; i++)
            if (buf[i] != n)
                return len;

        return len - n;
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize - decryptionHeaderSize;
    }
}
