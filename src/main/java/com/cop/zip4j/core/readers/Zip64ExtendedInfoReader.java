package com.cop.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.Zip64;

import java.io.IOException;

/**
 * see 4.4.8, 4.4.9, 4.4.16, 4.5.3
 *
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoReader {

    private final int signature;
    private final boolean uncompressedSize;
    private final boolean compressedSize;
    private final boolean offs;
    private final boolean diskNumber;

    @NonNull
    public Zip64.ExtendedInfo read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (signature != Zip64.ExtendedInfo.SIGNATURE)
            return Zip64.ExtendedInfo.NULL;

        Zip64.ExtendedInfo res = new Zip64.ExtendedInfo();
        res.setSize(in.readWord());
        res.setUncompressedSize(uncompressedSize ? in.readQword() : ExtraField.NO_DATA);
        res.setCompressedSize(compressedSize ? in.readQword() : ExtraField.NO_DATA);
        res.setOffsLocalHeaderRelative(offs ? in.readQword() : ExtraField.NO_DATA);
        res.setDiskNumber(diskNumber ? in.readDword() : ExtraField.NO_DATA);

        return res;
    }

}
