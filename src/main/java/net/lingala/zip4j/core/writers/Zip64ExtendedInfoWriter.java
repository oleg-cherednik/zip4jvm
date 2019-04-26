package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.Zip64ExtendedInfo;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoWriter {

    private final Zip64ExtendedInfo info;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (info == null)
            return;

        out.writeWord(Zip64ExtendedInfo.SIGNATURE);
        out.writeWord((short)info.getSize());

        if (info.getUncompressedSize() != Zip64ExtendedInfo.NO_DATA)
            out.writeLong(info.getUncompressedSize());
        if (info.getCompressedSize() != Zip64ExtendedInfo.NO_DATA)
            out.writeLong(info.getCompressedSize());
        if (info.getOffsLocalHeaderRelative() != Zip64ExtendedInfo.NO_DATA)
            out.writeLong(info.getOffsLocalHeaderRelative());
        if (info.getDiskNumber() != Zip64ExtendedInfo.NO_DATA)
            out.writeDword(info.getDiskNumber());
    }

}
