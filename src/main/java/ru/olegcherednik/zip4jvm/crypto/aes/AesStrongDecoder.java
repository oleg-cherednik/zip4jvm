package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.LittleEndianDataInput;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

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
    private boolean eof;

    public static AesStrongDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            in.mark(DECRYPTION_HEADER);
            Cipher cipher = new DecryptionHeaderDecoder().readAndCreateCipher(in, "1".toCharArray());
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
        if (eof || len == IOUtils.EOF)
            return IOUtils.EOF;
        if (len == 0)
            return 0;

        try {
            decryptedBytes += len;
            int len1 = cipher.update(buf, offs, len, buf, offs);

            if (decryptedBytes < compressedSize)
                return len1;

            eof = true;
            return getUnpadLength(buf, offs, len1);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    /*
     * If the block length is B then add N padding bytes of value N to make the input length up to the next exact
     * multiple of B. If the input length is already an exact multiple of B then add B bytes of value B. Thus padding
     * of length N between one and B bytes is always added in an unambiguous manner. After decrypting, check that the
     * last N bytes of the decrypted data all have value N with 1 < N ≤ B. If so, strip N bytes, otherwise throw a
     * decryption error.
     */
    private int getUnpadLength(byte[] buf, int offs, int len) {
        int n = buf[offs + len - 1];

        if (n <= 0 || n > cipher.getBlockSize())
            return len;

        for (int i = offs + len - n; i < offs + len; i++)
            if (buf[i] != n)
                return len;

        return len - n;
    }

    private int unpad(byte[] in, int off, int len) {
        if (in != null && len != 0) {
            int idx = Math.addExact(off, len);
            byte lastByte = in[idx - 1];
            int padValue = lastByte & 255;
            if (padValue >= 1 && padValue <= cipher.getBlockSize()) {
                int start = idx - padValue;
                if (start < off) {
                    return -1;
                } else {
                    for (int i = start; i < idx; ++i) {
                        if (in[i] != lastByte) {
                            return -1;
                        }
                    }

                    return start;
                }
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize - decryptionHeaderSize;
    }
}
