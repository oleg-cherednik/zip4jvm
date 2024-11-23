package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@Getter
public abstract class CompressedCentralDirectoryDataInput extends BaseDataInput {

    protected long absOffs;

    public static DataInput create(Zip64.ExtensibleDataSector extensibleDataSector, DataInput in) {
        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreDataInput(in);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new InflateDataInput(in);
        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
            return new EnhancedDeflateCentralDirectoryDataInput(in);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2CentralDirectoryDataInput(in);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    protected CompressedCentralDirectoryDataInput(DataInput in) {
        super(in);
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        int total = 0;

        for (long i = 0; i < bytes; i++, total++)
            readByte();

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
        assert buf == null;
        assert offs == IOUtils.EOF;

        if (len == IOUtils.EOF || len == 0)
            return IOUtils.EOF;

        absOffs += len;
        return len;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        long offs = getAbsOffs();
        return String.format("offs: %s (0x%s)", offs, Long.toHexString(offs));
    }
}
