package net.lingala.zip4j.core;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
public final class Zip64EndCentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;
    private final long offs;

    public Zip64EndCentralDirectory read() throws IOException, ZipException {
        findHead();

        Zip64EndCentralDirectory dir = new Zip64EndCentralDirectory();
        dir.setSizeOfZip64EndCentralDirRec(in.readLong());
        dir.setVersionMadeBy(in.readShort());
        dir.setVersionNeededToExtract(in.readShort());
        dir.setNoOfThisDisk(in.readInt());
        dir.setNoOfThisDiskStartOfCentralDir(in.readInt());
        dir.setTotNoOfEntriesInCentralDirOnThisDisk(in.readLong());
        dir.setTotNoOfEntriesInCentralDir(in.readLong());
        dir.setSizeOfCentralDir(in.readLong());
        dir.setOffsetStartCenDirWRTStartDiskNo(in.readLong());
        dir.setExtensibleDataSector(in.readBytes((int)(dir.getSizeOfZip64EndCentralDirRec() - Zip64EndCentralDirectory.SIZE)));

        return dir;
    }

    private void findHead() throws IOException {
        in.seek(offs);

        if (in.readInt() == InternalZipConstants.ZIP64_ENDSIG)
            return;

        throw new IOException("invalid zip64 end of central directory");
    }
}
