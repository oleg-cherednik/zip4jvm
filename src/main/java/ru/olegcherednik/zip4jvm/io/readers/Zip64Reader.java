package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
final class Zip64Reader implements Reader<Zip64> {

    @Override
    public Zip64 read(DataInput in) throws IOException {
        Zip64.EndCentralDirectoryLocator locator = new Zip64Reader.EndCentralDirectoryLocator().read(in);
        Zip64.EndCentralDirectory dir = locator == null ? null : new Zip64Reader.EndCentralDirectory(locator.getOffs()).read(in);
        return Zip64.of(locator, dir);
    }

    private static final class EndCentralDirectoryLocator {

        public Zip64.EndCentralDirectoryLocator read(DataInput in) throws IOException {
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

        public Zip64.EndCentralDirectory read(DataInput in) throws IOException {
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
                throw new Zip4jvmException("invalid zip64 end of central directory");
        }
    }

    @RequiredArgsConstructor
    static final class ExtendedInfo implements Reader<Zip64.ExtendedInfo> {

        private final boolean uncompressedSizeExists;
        private final boolean compressedSizeExists;
        private final boolean offsLocalHeaderRelativeExists;
        private final boolean diskExists;

        @Override
        public Zip64.ExtendedInfo read(DataInput in) throws IOException {
            long offs1 = in.getOffs();
            int size = in.readWord();
            long offs2 = in.getOffs();

            Zip64.ExtendedInfo extendedInfo = readExtendedInfo(in);

            if (in.getOffs() - offs2 != size) {
                // section exists, but not need to read it; all data in FileHeader
                extendedInfo = Zip64.ExtendedInfo.NULL;
                in.seek(offs1);
            }

            return extendedInfo;
        }

        private Zip64.ExtendedInfo readExtendedInfo(DataInput in) throws IOException {
            long uncompressedSize = uncompressedSizeExists ? in.readQword() : ExtraField.NO_DATA;
            long compressedSize = compressedSizeExists ? in.readQword() : ExtraField.NO_DATA;
            long offsLocalHeaderRelative = offsLocalHeaderRelativeExists ? in.readQword() : ExtraField.NO_DATA;
            long disk = diskExists ? in.readDword() : ExtraField.NO_DATA;

            return Zip64.ExtendedInfo.builder()
                                     .uncompressedSize(uncompressedSize)
                                     .compressedSize(compressedSize)
                                     .offsLocalHeaderRelative(offsLocalHeaderRelative)
                                     .disk(disk).build();
        }

    }

}
