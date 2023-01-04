package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

/**
 * Represents {@link DataInput} based on the array with additional metadata {@link DataInputLocation}
 *
 * @author Oleg Cherednik
 * @since 24.12.2022
 */
@Getter
public class MetadataByteArrayDataInput extends ByteArrayDataInput implements DataInputLocation {

    private final DataInputLocation dataInputLocation;

    public MetadataByteArrayDataInput(byte[] buf, Endianness endianness, DataInputLocation dataInputLocation) {
        super(buf, endianness);
        this.dataInputLocation = dataInputLocation;
    }

    @Override
    public long getDiskRelativeOffs() {
        return dataInputLocation.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return dataInputLocation.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return dataInputLocation.getDisk();
    }
}
