package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryWriter {

    private final Zip64EndCentralDirectory dir;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (dir == null)
            return;

        out.writeDword(InternalZipConstants.ZIP64_ENDSIG);
        out.writeLong(dir.getSizeOfZip64EndCentralDirRec());
        out.writeWord(dir.getVersionMadeBy());
        out.writeWord(dir.getVersionNeededToExtract());
        out.writeDword(dir.getDiskNumber());
        out.writeDword(dir.getStartDiskNumber());
        out.writeLong(dir.getDiskEntries());
        out.writeLong(dir.getTotalEntries());
        out.writeLong(dir.getSize());
        out.writeLong(dir.getOffs());
        out.writeBytes(dir.getExtensibleDataSector());
    }
}
