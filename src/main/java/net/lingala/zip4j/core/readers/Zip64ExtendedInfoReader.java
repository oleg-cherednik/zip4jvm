package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class Zip64ExtendedInfoReader {

    private final short header;
    @NonNull
    private final CentralDirectory.FileHeader fileHeader;

    public Zip64ExtendedInfo read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (header != InternalZipConstants.EXTRAFIELDZIP64LENGTH)
            return null;

        Zip64ExtendedInfo res = new Zip64ExtendedInfo();
        res.setSize(in.readWord());
        res.setUnCompressedSize((fileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setCompressedSize((fileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setOffsLocalHeaderRelative((fileHeader.getOffsLocalFileHeader() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setDiskNumberStart((fileHeader.getDiskNumber() & 0xFFFF) == 0xFFFF ? in.readInt() : -1);
        return res;
    }

}
