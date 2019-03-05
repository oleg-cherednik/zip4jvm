package net.lingala.zip4j.core;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
public final class Zip64EndCentralDirectoryLocatorReader {

    private final LittleEndianRandomAccessFile in;
    private final long offsEndCentralDirectory;

    public Zip64EndCentralDirectoryLocator read() throws IOException, ZipException {
        if (!findHead())
            return null;

        Zip64EndCentralDirectoryLocator locator = new Zip64EndCentralDirectoryLocator();
        locator.setNoOfDiskStartOfZip64EndOfCentralDirRec(in.readInt());
        locator.setOffsetZip64EndOfCentralDirRec(in.readLong());
        locator.setTotNumberOfDiscs(in.readInt());

        return locator;
    }

    private boolean findHead() throws IOException {
        if (offsEndCentralDirectory < 0)
            throw new IOException("EndCentralDirectory offs is unknown");

        in.seek(offsEndCentralDirectory - Zip64EndCentralDirectoryLocator.SIZE);
        return in.readInt() == InternalZipConstants.ZIP64_ENDSIG_LOC;
    }
}
