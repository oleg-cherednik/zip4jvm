package com.cop.zip4j.io.readers;

import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
    public Zip64.ExtendedInfo read(@NonNull DataInput in) throws IOException {
        if (signature != Zip64.ExtendedInfo.SIGNATURE)
            return Zip64.ExtendedInfo.NULL;

        return Zip64.ExtendedInfo.builder()
                                 .size(in.readWord())
                                 .uncompressedSize(uncompressedSize ? in.readQword() : ExtraField.NO_DATA)
                                 .compressedSize(compressedSize ? in.readQword() : ExtraField.NO_DATA)
                                 .offsLocalHeaderRelative(offs ? in.readQword() : ExtraField.NO_DATA)
                                 .diskNumber(diskNumber ? in.readDword() : ExtraField.NO_DATA)
                                 .build();
    }

}
