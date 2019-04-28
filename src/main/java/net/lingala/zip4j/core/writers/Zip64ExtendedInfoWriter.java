package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.ExtraField;
import net.lingala.zip4j.model.Zip64;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoWriter {

    @NonNull
    private final Zip64.ExtendedInfo info;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (info == Zip64.ExtendedInfo.NULL)
            return;

        out.writeWord(Zip64.ExtendedInfo.SIGNATURE);
        out.writeWord((short)info.getSize());

        if (info.getUncompressedSize() != ExtraField.NO_DATA)
            out.writeQword(info.getUncompressedSize());
        if (info.getCompressedSize() != ExtraField.NO_DATA)
            out.writeQword(info.getCompressedSize());
        if (info.getOffsLocalHeaderRelative() != ExtraField.NO_DATA)
            out.writeQword(info.getOffsLocalHeaderRelative());
        if (info.getDiskNumber() != ExtraField.NO_DATA)
            out.writeDword(info.getDiskNumber());
    }

}
