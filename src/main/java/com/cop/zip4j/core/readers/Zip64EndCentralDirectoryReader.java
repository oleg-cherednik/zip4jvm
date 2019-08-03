package com.cop.zip4j.core.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryReader {

    private final long offs;

    @NonNull
    public Zip64.EndCentralDirectory read(@NonNull DataInput in) throws IOException {
        findHead(in);

        Zip64.EndCentralDirectory dir = new Zip64.EndCentralDirectory();
        dir.setSizeOfZip64EndCentralDirRec(in.readQword());
        dir.setVersionMadeBy(in.readWord());
        dir.setVersionNeededToExtract(in.readWord());
        dir.setDiskNumber(in.readDword());
        dir.setStartDiskNumber(in.readDword());
        dir.setDiskEntries(in.readQword());
        dir.setTotalEntries(in.readQword());
        dir.setSize(in.readQword());
        dir.setOffs(in.readQword());
        dir.setExtensibleDataSector(in.readBytes((int)(dir.getSizeOfZip64EndCentralDirRec() - Zip64.EndCentralDirectory.SIZE)));

        return dir;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(offs);

        if (in.readDword() == Zip64.EndCentralDirectory.SIGNATURE)
            return;

        throw new Zip4jException("invalid zip64 end of central directory");
    }
}
