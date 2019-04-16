package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryLocatorWriter {

    private final Zip64EndCentralDirectoryLocator locator;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (locator == null)
            return;

        out.writeDword(Zip64EndCentralDirectoryLocator.SIGNATURE);
        out.writeDword(locator.getNoOfDiskStartOfZip64EndOfCentralDirRec());
        out.writeLong(locator.getOffsetZip64EndOfCentralDirRec());
        out.writeDword(locator.getTotNumberOfDiscs());
    }
}
