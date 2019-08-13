package com.cop.zip4j.core.readers;

import com.cop.zip4j.io.in.DataInput;
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

    @NonNull
    public DataDescriptor read(@NonNull DataInput in) throws IOException {
        DataDescriptor dataDescriptor = new DataDescriptor();

        dataDescriptor.setCrc32(in.readDword());
        dataDescriptor.setCompressedSize(in.readDwordLong());
        dataDescriptor.setUncompressedSize(in.readDwordLong());

        return dataDescriptor;
    }

}
