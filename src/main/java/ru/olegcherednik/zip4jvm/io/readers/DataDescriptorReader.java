package ru.olegcherednik.zip4jvm.io.readers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataDescriptorReader implements Reader<DataDescriptor> {

    public static DataDescriptorReader get(boolean zip64) {
        return zip64 ? new Zip64() : new Standard();
    }

    private static final class Standard extends DataDescriptorReader {

        @Override
        public DataDescriptor read(DataInput in) throws IOException {
            long offs = in.getOffs();

            if (in.readSignature() != DataDescriptor.SIGNATURE)
                throw new Zip4jvmException("DataDescriptor signature expected at offs=" + offs);

            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(in.readDword());
            dataDescriptor.setCompressedSize(in.readDword());
            dataDescriptor.setUncompressedSize(in.readDword());

            return dataDescriptor;
        }
    }

    private static final class Zip64 extends DataDescriptorReader {

        @Override
        public DataDescriptor read(DataInput in) throws IOException {
            long offs = in.getOffs();

            if (in.readSignature() != DataDescriptor.SIGNATURE)
                throw new Zip4jvmException("DataDescriptor signature expected at offs=" + offs);

            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setCrc32(in.readDword());
            dataDescriptor.setCompressedSize(in.readQword());
            dataDescriptor.setUncompressedSize(in.readQword());

            return dataDescriptor;
        }
    }

}
