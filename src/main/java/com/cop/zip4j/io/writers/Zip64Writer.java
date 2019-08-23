package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@RequiredArgsConstructor
final class Zip64Writer {

    @NonNull
    private final Zip64 zip64;

    public void write(@NonNull DataOutput out) throws IOException {
        new EndCentralDirectory(zip64.getEndCentralDirectory()).write(out);
        new EndCentralDirectoryLocator(zip64.getEndCentralDirectoryLocator()).write(out);
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectory {

        private final Zip64.EndCentralDirectory dir;

        public void write(@NonNull DataOutput out) throws IOException {
            if (dir == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectory.SIGNATURE);
            out.writeQword(dir.getSizeEndCentralDirectory());
            out.writeWord(dir.getVersionMadeBy());
            out.writeWord(dir.getVersionNeededToExtract());
            out.writeDword(dir.getDisk());
            out.writeDword(dir.getStartDisk());
            out.writeQword(dir.getDiskEntries());
            out.writeQword(dir.getTotalEntries());
            out.writeQword(dir.getSize());
            out.writeQword(dir.getOffs());
            out.writeBytes(dir.getExtensibleDataSector());
        }
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectoryLocator {

        private final Zip64.EndCentralDirectoryLocator locator;

        public void write(@NonNull DataOutput out) throws IOException {
            if (locator == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectoryLocator.SIGNATURE);
            out.writeDword(locator.getStartDisk());
            out.writeQword(locator.getOffs());
            out.writeDword(locator.getTotalDisks());
        }
    }

    @RequiredArgsConstructor
    static final class ExtendedInfo {

        @NonNull
        private final Zip64.ExtendedInfo info;

        public void write(@NonNull DataOutput out) throws IOException {
            if (info == Zip64.ExtendedInfo.NULL)
                return;

            out.writeWordSignature(Zip64.ExtendedInfo.SIGNATURE);
            out.writeWord(info.getDataSize());

            if (info.getUncompressedSize() != ExtraField.NO_DATA)
                out.writeQword(info.getUncompressedSize());
            if (info.getCompressedSize() != ExtraField.NO_DATA)
                out.writeQword(info.getCompressedSize());
            if (info.getOffsLocalHeaderRelative() != ExtraField.NO_DATA)
                out.writeQword(info.getOffsLocalHeaderRelative());
            if (info.getDiskNumber() != ExtraField.NO_DATA)
                out.writeDword(info.getDiskNumber());
        }

    }

}
