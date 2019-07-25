package com.cop.zip4j.core.readers;

import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.DataDescriptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@RequiredArgsConstructor
public final class DataDescriptorReader {

    // TODO should be used to check data after reading LocalFileHeader
    @NonNull
    public DataDescriptor read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        DataDescriptor dataDescriptor = new DataDescriptor();

        dataDescriptor.setCrc32(in.readDword());
        dataDescriptor.setCompressedSize(in.readDwordLong());
        dataDescriptor.setUncompressedSize(in.readDwordLong());

        return dataDescriptor;
    }

}
