package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
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

    private final boolean zip64;

    @NonNull
    public DataDescriptor read(@NonNull DataInput in) throws IOException {
        long offs = in.getOffs();

        if (in.readDword() != DataDescriptor.SIGNATURE)
            throw new Zip4jException("DataDescriptor signature expected at offs=" + offs);

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(in.readDwordLong());
        dataDescriptor.setCompressedSize(/*zip64 ? in.readQword() :*/ in.readDwordLong());
        dataDescriptor.setUncompressedSize(/*zip64 ? in.readQword() :*/ in.readDwordLong());

        return dataDescriptor;
    }

}
