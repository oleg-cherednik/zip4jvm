package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

/**
 * This class was designed only to cover one decompose test split+ecd.
 * It should be removed and real problem should be fixed.
 *
 * @author Oleg Cherednik
 * @since 08.01.2023
 */
@Deprecated
public final class DiskByteArrayDataInput extends ByteArrayDataInput {

    @Getter
    private final SrcZip.Disk disk;

    public DiskByteArrayDataInput(byte[] buf, Endianness endianness, SrcZip.Disk disk) {
        super(buf, endianness);
        this.disk = disk;
    }


}
