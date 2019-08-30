package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.DataDescriptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@SuppressWarnings("ClassMayBeInterface")
public abstract class DataDescriptorWriter {

    public abstract void write(@NonNull DataOutput out) throws IOException;

    @RequiredArgsConstructor
    public static final class Plain extends DataDescriptorWriter {

        @NonNull
        private final DataDescriptor dataDescriptor;

        @Override
        public void write(@NonNull DataOutput out) throws IOException {
            out.writeDwordSignature(DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeDword(dataDescriptor.getCompressedSize());
            out.writeDword(dataDescriptor.getUncompressedSize());
        }
    }

    @RequiredArgsConstructor
    public static final class Zip64 extends DataDescriptorWriter {

        @NonNull
        private final DataDescriptor dataDescriptor;

        @Override
        public void write(@NonNull DataOutput out) throws IOException {
            out.writeDwordSignature(DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeQword(dataDescriptor.getCompressedSize());
            out.writeQword(dataDescriptor.getUncompressedSize());
        }
    }

}

