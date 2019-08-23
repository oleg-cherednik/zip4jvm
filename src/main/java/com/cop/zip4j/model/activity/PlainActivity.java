package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;

import java.io.IOException;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
public class PlainActivity implements Activity {

    public LongSupplier getCrc32LocalFileHeader(LongSupplier originalCrc32) {
        return originalCrc32;
    }

    public LongSupplier getCompressedSizeLocalFileHeader(LongSupplier originalCompressedSize) {
        return originalCompressedSize;
    }

    public LongSupplier getUncompressedSizeLocalFileHeader(LongSupplier originalUncompressedSize) {
        return originalUncompressedSize;
    }

    public Supplier<Zip64.ExtendedInfo> getExtendedInfoLocalFileHeader(CentralDirectory.FileHeader fileHeader) {
        return () -> Zip64.ExtendedInfo.NULL;
    }

    public void writeValueDataDescriptor(long value, @NonNull DataOutput out) throws IOException {
        out.writeDword(value);
    }

}
