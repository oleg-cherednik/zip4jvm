package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;

import java.io.IOException;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
public class Zip64Activity implements Activity {

    // LocalFileHeader

    public LongSupplier getCrc32LocalFileHeader(LongSupplier originalCrc32) {
        return () -> LOOK_IN_EXTRA_FIELD;
    }

    public LongSupplier getCompressedSizeLocalFileHeader(LongSupplier originalCompressedSize) {
        return () -> LOOK_IN_EXTRA_FIELD;
    }

    public LongSupplier getUncompressedSizeLocalFileHeader(LongSupplier originalUncompressedSize) {
        return () -> LOOK_IN_EXTRA_FIELD;
    }

    public Supplier<Zip64.ExtendedInfo> getExtendedInfoLocalFileHeader(CentralDirectory.FileHeader fileHeader) {
        return () -> Zip64.ExtendedInfo.builder()
                                       .compressedSize(fileHeader.getOriginalCompressedSize())
                                       .uncompressedSize(fileHeader.getOriginalUncompressedSize())
                                       .build();
    }

    // DataDescriptor

    public void writeValueDataDescriptor(long value, @NonNull DataOutput out) throws IOException {
        out.writeQword(value);
    }

}
