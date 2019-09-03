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
final class Zip64Reader {

    public Zip64 read(@NonNull DataInput in) throws IOException {
        Zip64.EndCentralDirectoryLocator locator = new Zip64Reader.EndCentralDirectoryLocator().read(in);
        Zip64.EndCentralDirectory dir = locator == null ? null : new Zip64Reader.EndCentralDirectory(locator.getOffs()).read(in);
        return Zip64.of(locator, dir);
    }

    private static final class EndCentralDirectoryLocator {

        @NonNull
        public Zip64.EndCentralDirectoryLocator read(@NonNull DataInput in) throws IOException {
            if (!findHead(in))
                return null;

            Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
            locator.setMainDisk(in.readDword());
            locator.setOffs(in.readQword());
            locator.setTotalDisks(in.readDword());

            ZipUtils.requirePositive(locator.getOffs(), "Zip64.EndCentralDirectory");

            return locator;
        }

        private static boolean findHead(DataInput in) throws IOException {
            long offs = in.getOffs() - Zip64.EndCentralDirectoryLocator.SIZE;

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
            long endCentralDirectorySize = in.readQword();
            dir.setEndCentralDirectorySize(endCentralDirectorySize);
            dir.setVersionMadeBy(in.readWord());
            dir.setVersionNeededToExtract(in.readWord());
            dir.setDisk(in.readDword());
            dir.setMainDisk(in.readDword());
            dir.setDiskEntries(in.readQword());
            dir.setTotalEntries(in.readQword());
            dir.setSize(in.readQword());
            dir.setCentralDirectoryOffs(in.readQword());
            dir.setExtensibleDataSector(in.readBytes((int)endCentralDirectorySize - Zip64.EndCentralDirectory.SIZE));

            ZipUtils.requirePositive(dir.getCentralDirectoryOffs(), "offsCentralDirectory");
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
        private final boolean disk;

        @NonNull
        public Zip64.ExtendedInfo read(@NonNull DataInput in) throws IOException {
            if (signature != Zip64.ExtendedInfo.SIGNATURE)
                return Zip64.ExtendedInfo.NULL;

            int size = in.readWord();
            long offs = in.getOffs();

            Zip64.ExtendedInfo extendedInfo = Zip64.ExtendedInfo.builder()
                                                                .uncompressedSize(uncompressedSize ? in.readQword() : ExtraField.NO_DATA)
                                                                .compressedSize(compressedSize ? in.readQword() : ExtraField.NO_DATA)
                                                                .offsLocalHeaderRelative(this.offs ? in.readQword() : ExtraField.NO_DATA)
                                                                .disk(disk ? in.readDword() : ExtraField.NO_DATA)
                                                                .build();

            if (in.getOffs() - offs != size)
                throw new Zip4jException("Illegal number of read bytes");

            return extendedInfo;
        }

    }

}
