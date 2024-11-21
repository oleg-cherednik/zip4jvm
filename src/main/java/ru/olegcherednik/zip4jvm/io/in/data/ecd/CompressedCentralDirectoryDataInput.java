package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
public abstract class CompressedCentralDirectoryDataInput extends BaseDataInput {

    public static CompressedCentralDirectoryDataInput create(Zip64.ExtensibleDataSector extensibleDataSector,
                                                             DataInput in) {
        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
        int uncompressedSize = (int) extensibleDataSector.getUncompressedSize();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreCentralDirectoryDataInput(in);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new InflateCentralDirectoryDataInput(in);
//        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
//            return new EnhancedDeflateDataInput(new ByteArrayDataInput(compressed, byteOrder, 0, 0), uncompressedSize);
//        if (compressionMethod == CompressionMethod.BZIP2)
//            return new Bzip2DataInput(new ByteArrayDataInput(compressed, byteOrder, 0, 0), uncompressedSize);

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

}
