package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoWriter {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final CentralDirectory.FileHeader fileHeader;
//    private final Zip64ExtendedInfo info;
    private final boolean writeZip64FileSize;
    private final boolean writeZip64OffsetLocalHeader;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (!zipModel.isZip64())
            return;

        //Zip64 header
        out.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);

        //Zip64 extra data record size
        short dataSize = 0;

        if (writeZip64FileSize)
            dataSize += 16;
        if (writeZip64OffsetLocalHeader)
            dataSize += 8;

        out.writeWord(dataSize);

        if (writeZip64FileSize) {
            out.writeDword(fileHeader.getUncompressedSize());

            out.writeDword(fileHeader.getCompressedSize());
        }

        if (writeZip64OffsetLocalHeader)
            out.writeDword(fileHeader.getOffsLocalFileHeader());
    }

}
