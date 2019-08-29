package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.IOException;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
public interface Activity {

    // LocalFileHeader

    LongSupplier getCrc32LocalFileHeader(LongSupplier originalCompressedSize);

    LongSupplier getCompressedSizeLocalFileHeader(LongSupplier originalCompressedSize);

    LongSupplier getUncompressedSizeLocalFileHeader(LongSupplier originalUncompressedSize);

    Supplier<Zip64.ExtendedInfo> getExtendedInfoLocalFileHeader(CentralDirectory.FileHeader fileHeader);

    // DataDescriptor

    void writeValueDataDescriptor(long value, @NonNull DataOutput out) throws IOException;

    // FileHeader

    LongSupplier getCompressedSizeFileHeader(LongSupplier originalCompressedSize);

    LongSupplier getUncompressedSizeFileHeader(LongSupplier originalUncompressedSize);

    Supplier<Zip64.ExtendedInfo> getExtendedInfoFileHeader(CentralDirectory.FileHeader fileHeader);

    // ZipModel

//    long getCentralDirectoryOffs(ZipModel zipModel);

    long getTotalEntries(ZipModel zipModel);

    void incTotalEntries(ZipModel zipModel);

    // EndCentralDirectory

    int getTotalEntriesECD(ZipModel zipModel);

}
