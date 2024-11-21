package ru.olegcherednik.zip4jvm.io.in.ecd;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
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

    protected final Decoder decoder;
    protected long available;

    public static EncryptedCentralDirectoryDataInput create(Decoder decoder, long compressedSize, DataInput in) {
        int batchSize = Math.max(0, decoder.getBlockSize());
        long encryptedSize = decoder == Decoder.NULL ? compressedSize : decoder.getCompressedSize();

        return batchSize == 0 ? new EncryptedCentralDirectoryDataInput(decoder, in, encryptedSize)
                              : new BatchRead(batchSize, encryptedSize, decoder, in);
    }

    protected EncryptedCentralDirectoryDataInput(Decoder decoder, DataInput in, long encryptedSize) {
        super(in);
        this.decoder = decoder;
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

    protected static class BatchRead extends EncryptedCentralDirectoryDataInput {

        private final byte[] batch;

        private int lo;
        private int hi;

        public BatchRead(int batchSize, long encryptedSize, Decoder decoder, DataInput in) {
            super(decoder, in, encryptedSize);
            batch = new byte[batchSize];
        }

        private int readIn(byte[] buf, int offs, int len) throws IOException {
            int readNow = in.read(buf, offs, batch.length * (len / batch.length));

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
                buf[offs++] = batch[lo++];

            return res;
        }

        private void fillLocalBuffer() throws IOException {
            lo = 0;
            int readNow = in.read(batch, lo, (int) Math.min(available, batch.length));

            if (readNow > 0)
                hi = decoder.decrypt(batch, lo, readNow);
        }

        @Override
        public long getAbsOffs() {
            return in.getAbsOffs() + lo - hi;
        }

        // ---------- ReadBuffer ----------

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            if (available == 0)
                return IOUtils.EOF;

            int readNow = readLocalBuffer(buf, offs, len);

            if (len - readNow > batch.length)
                readNow += readIn(buf, offs + readNow, len - readNow);

            if (len > readNow && available > 0) {
                fillLocalBuffer();
                readNow += readLocalBuffer(buf, offs + readNow, len - readNow);
            }

            return readNow;
        }

        // ---------- Object ----------


        @Override
        public String toString() {
            long offs = getAbsOffs();
            return String.format("offs: %s (0x%s)", offs, Long.toHexString(offs));
        }
    }
}
