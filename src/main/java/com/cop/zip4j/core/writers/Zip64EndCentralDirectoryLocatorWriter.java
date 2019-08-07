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
final class Zip64EndCentralDirectoryLocatorWriter {

    private final Zip64.EndCentralDirectoryLocator locator;

    public void write(@NonNull DataOutput out) throws IOException {
        if (locator == null)
            return;

        out.writeDwordSignature(Zip64.EndCentralDirectoryLocator.SIGNATURE);
        out.writeDword(locator.getNoOfDiskStartOfZip64EndOfCentralDirRec());
        out.writeQword(locator.getOffs());
        out.writeDword(locator.getTotNumberOfDiscs());
    }
}
