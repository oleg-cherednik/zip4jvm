package com.cop.zip4j.core.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryWriter {

    private final Zip64.EndCentralDirectory dir;

    public void write(@NonNull DataOutput out) throws IOException {
        if (dir == null)
            return;

        out.writeDword(Zip64.EndCentralDirectory.SIGNATURE);
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
