package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * see 4.4.8, 4.4.9, 4.4.16, 4.5.3
 *
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoReader {

    private final short header;
    private final boolean uncompressedSize;
    private final boolean compressedSize;
    private final boolean offs;
    private final boolean diskNumber;

    public Zip64ExtendedInfo read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (header != Zip64ExtendedInfo.SIGNATURE)
            return null;

        Zip64ExtendedInfo res = new Zip64ExtendedInfo();
        res.setSize(in.readWord());
        res.setUncompressedSize(uncompressedSize ? in.readLong() : Zip64ExtendedInfo.NO_DATA);
        res.setCompressedSize(compressedSize ? in.readLong() : Zip64ExtendedInfo.NO_DATA);
        res.setOffsLocalHeaderRelative(offs ? in.readLong() : Zip64ExtendedInfo.NO_DATA);
        res.setDiskNumber(diskNumber ? in.readInt() : Zip64ExtendedInfo.NO_DATA);

        return res;
    }

}
