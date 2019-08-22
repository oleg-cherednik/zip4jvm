package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
@RequiredArgsConstructor
final class Zip64Reader {

    private final long offs;

    public Zip64 read(@NonNull DataInput in) throws IOException {
        Zip64.EndCentralDirectoryLocator locator = new Zip64Reader.EndCentralDirectoryLocator(offs).read(in);
        Zip64.EndCentralDirectory dir = locator == null ? null : new Zip64Reader.EndCentralDirectory(locator.getOffs()).read(in);
        return Zip64.of(locator, dir);
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectoryLocator {

        private final long offs;

        @NonNull
        public Zip64.EndCentralDirectoryLocator read(@NonNull DataInput in) throws IOException {
            if (!findHead(in))
                return null;

            Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
            locator.setStartDisk(in.readDword());
            locator.setOffs(in.readQword());
            locator.setTotalDisks(in.readDword());

            ZipUtils.requirePositive(locator.getOffs(), "Zip64.EndCentralDirectory");

            return locator;
        }

        private boolean findHead(DataInput in) throws IOException {
            if (offs < 0)
                throw new Zip4jException("EndCentralDirectory offs is unknown");

            long offs = this.offs - Zip64.EndCentralDirectoryLocator.SIZE;

            if (offs < 0)
                return false;

            in.seek(offs);
            return in.readSignature() == Zip64.EndCentralDirectoryLocator.SIGNATURE;
        }
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectory {

        private final long offs;

        @NonNull
        public Zip64.EndCentralDirectory read(@NonNull DataInput in) throws IOException {
            findHead(in);

            Zip64.EndCentralDirectory dir = new Zip64.EndCentralDirectory();
            dir.setSizeEndCentralDirectory(in.readQword());
            dir.setVersionMadeBy(in.readWord());
            dir.setVersionNeededToExtract(in.readWord());
            dir.setDisk(in.readDword());
            dir.setStartDisk(in.readDword());
            dir.setDiskEntries(in.readQword());
            dir.setTotalEntries(in.readQword());
            dir.setSize(in.readQword());
            dir.setOffs(in.readQword());
            dir.setExtensibleDataSector(in.readBytes((int)(dir.getSizeEndCentralDirectory() - Zip64.EndCentralDirectory.SIZE)));

            ZipUtils.requirePositive(dir.getOffs(), "offsCentralDirectory");
            ZipUtils.requirePositive(dir.getTotalEntries(), "totalEntries");

            return dir;
        }

        private void findHead(DataInput in) throws IOException {
            in.seek(offs);

            if (in.readSignature() != Zip64.EndCentralDirectory.SIGNATURE)
                throw new Zip4jException("invalid zip64 end of central directory");
        }
    }

    @RequiredArgsConstructor
    static final class ExtendedInfo {

        private final int signature;
        private final boolean uncompressedSize;
        private final boolean compressedSize;
        private final boolean offs;
        private final boolean diskNumber;

        @NonNull
        public Zip64.ExtendedInfo read(@NonNull DataInput in) throws IOException {
            if (signature != Zip64.ExtendedInfo.SIGNATURE)
                return Zip64.ExtendedInfo.NULL;

            return Zip64.ExtendedInfo.builder()
                                     .size(in.readWord())
                                     .uncompressedSize(uncompressedSize ? in.readQword() : ExtraField.NO_DATA)
                                     .compressedSize(compressedSize ? in.readQword() : ExtraField.NO_DATA)
                                     .offsLocalHeaderRelative(offs ? in.readQword() : ExtraField.NO_DATA)
                                     .diskNumber(diskNumber ? in.readDword() : ExtraField.NO_DATA)
                                     .build();
        }

    }

}
