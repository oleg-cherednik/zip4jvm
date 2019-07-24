package com.cop.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.Zip64;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryLocatorWriter {

    private final Zip64.EndCentralDirectoryLocator locator;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (locator == null)
            return;

        out.writeDword(Zip64.EndCentralDirectoryLocator.SIGNATURE);
        out.writeDword(locator.getNoOfDiskStartOfZip64EndOfCentralDirRec());
        out.writeQword(locator.getOffs());
        out.writeDword(locator.getTotNumberOfDiscs());
    }
}
