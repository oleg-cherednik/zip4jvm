package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.Zip64;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryWriter {

    private final Zip64.EndCentralDirectory dir;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (dir == null)
            return;

        out.writeSignature(Zip64.EndCentralDirectory.SIGNATURE);
        out.writeQword(dir.getSizeOfZip64EndCentralDirRec());
        out.writeWord(dir.getVersionMadeBy());
        out.writeWord(dir.getVersionNeededToExtract());
        out.writeDword(dir.getDiskNumber());
        out.writeDword(dir.getStartDiskNumber());
        out.writeQword(dir.getDiskEntries());
        out.writeQword(dir.getTotalEntries());
        out.writeQword(dir.getSize());
        out.writeQword(dir.getOffs());
        out.writeBytes(dir.getExtensibleDataSector());
    }
}
