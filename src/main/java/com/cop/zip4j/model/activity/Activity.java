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
public interface Activity {

    LongSupplier getCrc32LocalFileHeader(LongSupplier originalCompressedSize);

    LongSupplier getCompressedSizeLocalFileHeader(LongSupplier originalCompressedSize);

    LongSupplier getUncompressedSizeLocalFileHeader(LongSupplier originalUncompressedSize);

    Supplier<Zip64.ExtendedInfo> getExtendedInfoLocalFileHeader(CentralDirectory.FileHeader fileHeader);

    void writeValueDataDescriptor(long value, @NonNull DataOutput out) throws IOException;

}
