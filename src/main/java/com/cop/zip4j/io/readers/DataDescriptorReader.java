package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.DataDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataDescriptorReader {

    public static DataDescriptorReader get(boolean zip64) {
        return zip64 ? new Zip64() : new Standard();
    }

    public abstract DataDescriptor read(@NonNull DataInput in) throws IOException;

    private static final class Standard extends DataDescriptorReader {

        @Override
        public DataDescriptor read(@NonNull DataInput in) throws IOException {
            long offs = in.getOffs();

            if (in.readSignature() != DataDescriptor.SIGNATURE)
                throw new Zip4jException("DataDescriptor signature expected at offs=" + offs);

            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(in.readDword());
            dataDescriptor.setCompressedSize(in.readDword());
            dataDescriptor.setUncompressedSize(in.readDword());

            return dataDescriptor;
        }
    }

    private static final class Zip64 extends DataDescriptorReader {

        @Override
        public DataDescriptor read(@NonNull DataInput in) throws IOException {
            long offs = in.getOffs();

            if (in.readSignature() != DataDescriptor.SIGNATURE)
                throw new Zip4jException("DataDescriptor signature expected at offs=" + offs);

            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(in.readDword());
            dataDescriptor.setCompressedSize(in.readQword());
            dataDescriptor.setUncompressedSize(in.readQword());

            return dataDescriptor;
        }
    }

}
