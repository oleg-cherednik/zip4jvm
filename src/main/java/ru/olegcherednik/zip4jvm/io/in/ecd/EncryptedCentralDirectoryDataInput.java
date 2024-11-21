package ru.olegcherednik.zip4jvm.io.in.ecd;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.11.2024
 */
public class EncryptedCentralDirectoryDataInput extends BaseDataInput implements RandomAccessDataInput {

    protected final CentralDirectoryDecoder decoder;
    protected long available;

    public static EncryptedCentralDirectoryDataInput create(DecryptionHeader decryptionHeader,
                                                            CentralDirectoryDecoder decoder,
                                                            DataInput in,
                                                            long encryptedSize) {

        return new BlockRead(decoder.getCipher().getBlockSize(), decoder, in, encryptedSize);
    }

    protected EncryptedCentralDirectoryDataInput(CentralDirectoryDecoder centralDirectoryDecoder,
                                                 DataInput in,
                                                 long encryptedSize) {
        super(in);
        this.decoder = centralDirectoryDecoder;
        available = encryptedSize;
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

    @Override
    public int readByte() throws IOException {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return getByteOrder().readQword(this);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = in.read(buf, offs, len);
        return decoder.decrypt(buf, offs, readNow);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void seek(long absOffs) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void seek(String id) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        throw new NotImplementedException();
    }

    @Override
    public long availableLong() throws IOException {
        throw new NotImplementedException();
    }

    protected static class BlockRead extends EncryptedCentralDirectoryDataInput {

        private final byte[] localBuf;

        private int lo;
        private int hi;

        public BlockRead(int blockSize, CentralDirectoryDecoder decoder, DataInput in, long encryptedSize) {
            super(decoder, in, encryptedSize);
            localBuf = new byte[blockSize];
        }

        private int readIn(byte[] buf, int offs, int len) throws IOException {
            int readNow = in.read(buf, offs, localBuf.length * (len / localBuf.length));

            if (readNow > 0) {
                available -= readNow;
                return decoder.decrypt(buf, offs, readNow);
            }

            return 0;
        }

        private int readLocalBuffer(byte[] buf, int offs, int len) {
            if (lo == hi || len <= 0)
                return 0;

            int res = 0;

            for (; lo < hi && len > 0; available--, res++, len--)
                buf[offs++] = localBuf[lo++];

            return res;
        }

        private void fillLocalBuffer() throws IOException {
            lo = 0;
            int res = in.read(localBuf, lo, (int) Math.min(available, localBuf.length));

            if (res > 0)
                hi = decoder.decrypt(localBuf, lo, res);
        }

        // ---------- ReadBuffer ----------

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            if (available == 0)
                return IOUtils.EOF;

            int readNow = readLocalBuffer(buf, offs, len);

            if (len - readNow > localBuf.length)
                readNow += readIn(buf, offs + readNow, len - readNow);

            if (len > readNow && available > 0) {
                fillLocalBuffer();
                readNow += readLocalBuffer(buf, offs + readNow, len - readNow);
            }

            return readNow;
        }

    }
}
