package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
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

        private final Zip64.EndCentralDirectory endCentralDirectory;

        public void write(@NonNull DataOutput out) throws IOException {
            if (endCentralDirectory == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectory.SIGNATURE);
            out.writeQword(endCentralDirectory.getEndCentralDirectorySize());
            out.writeWord(endCentralDirectory.getVersionMadeBy());
            out.writeWord(endCentralDirectory.getVersionNeededToExtract());
            out.writeDword(endCentralDirectory.getDisk());
            out.writeDword(endCentralDirectory.getMainDisk());
            out.writeQword(endCentralDirectory.getDiskEntries());
            out.writeQword(endCentralDirectory.getTotalEntries());
            out.writeQword(endCentralDirectory.getSize());
            out.writeQword(endCentralDirectory.getCentralDirectoryOffs());
            out.writeBytes(endCentralDirectory.getExtensibleDataSector());
        }
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectoryLocator {

        private final Zip64.EndCentralDirectoryLocator locator;

        public void write(@NonNull DataOutput out) throws IOException {
            if (locator == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectoryLocator.SIGNATURE);
            out.writeDword(locator.getMainDisk());
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
            if (info.getDisk() != ExtraField.NO_DATA)
                out.writeDword(info.getDisk());
        }

    }

    @RequiredArgsConstructor
    public static final class DataDescriptor {

        @NonNull
        private final ru.olegcherednik.zip4jvm.model.DataDescriptor dataDescriptor;

        public void write(@NonNull DataOutput out) throws IOException {
            out.writeDwordSignature(ru.olegcherednik.zip4jvm.model.DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeQword(dataDescriptor.getCompressedSize());
            out.writeQword(dataDescriptor.getUncompressedSize());
        }

    }

}
