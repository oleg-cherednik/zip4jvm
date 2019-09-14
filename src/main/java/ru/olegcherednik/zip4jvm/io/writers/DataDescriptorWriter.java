package ru.olegcherednik.zip4jvm.io.writers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataDescriptorWriter implements Writer {

    public static DataDescriptorWriter get(boolean zip64, DataDescriptor dataDescriptor) {
        return zip64 ? new Zip64(dataDescriptor) : new Standard(dataDescriptor);
    }

    @RequiredArgsConstructor
    private static final class Standard extends DataDescriptorWriter {

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
    private static final class Zip64 extends DataDescriptorWriter {

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

