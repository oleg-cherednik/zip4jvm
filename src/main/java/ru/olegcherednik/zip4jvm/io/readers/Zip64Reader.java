package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.Diagnostic;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
@RequiredArgsConstructor
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

            realBigZip64(locator.getOffs(), "Zip64.EndCentralDirectory");

            Diagnostic.getInstance().getZip64().setEndCentralDirectoryLocatorEndOffs(in.getOffs());

            return locator;
        }

        private static boolean findHead(DataInput in) throws IOException {
            long offs = in.getOffs() - Zip64.EndCentralDirectoryLocator.SIZE;

            if (offs < 0)
                return false;

            in.seek(offs);

            boolean exists = in.readSignature() == Zip64.EndCentralDirectoryLocator.SIGNATURE;

            if (exists)
                Diagnostic.getInstance().createZip64().setEndCentralDirectoryLocatorOffs(offs);

            return exists;
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
            dir.setVersionMadeBy(Version.build(in.readWord()));
            dir.setVersionToExtract(Version.build(in.readWord()));
            dir.setTotalDisks(in.readDword());
            dir.setMainDisk(in.readDword());
            dir.setDiskEntries(in.readQword());
            dir.setTotalEntries(in.readQword());
            dir.setCentralDirectorySize(in.readQword());
            dir.setCentralDirectoryOffs(in.readQword());
            dir.setExtensibleDataSector(in.readBytes((int)endCentralDirectorySize - Zip64.EndCentralDirectory.SIZE));

            realBigZip64(dir.getCentralDirectoryOffs(), "offsCentralDirectory");
            realBigZip64(dir.getTotalEntries(), "totalEntries");

            Diagnostic.getInstance().getZip64().setEndCentralDirectoryEndOffs(in.getOffs());

            return dir;
        }

        private void findHead(DataInput in) throws IOException {
            in.seek(offs);

            if (in.readSignature() != Zip64.EndCentralDirectory.SIGNATURE)
                throw new Zip4jvmException("invalid zip64 end of central directory");

            Diagnostic.getInstance().getZip64().setEndCentralDirectoryEndOffs(offs);
        }

    }

    @RequiredArgsConstructor
    static final class ExtendedInfo implements Reader<Zip64.ExtendedInfo> {

        private final int size;
        private final boolean uncompressedSizeExists;
        private final boolean compressedSizeExists;
        private final boolean offsLocalHeaderRelativeExists;
        private final boolean diskExists;

        @Override
        public Zip64.ExtendedInfo read(DataInput in) throws IOException {
            long offs = in.getOffs();

            Zip64.ExtendedInfo extendedInfo = readExtendedInfo(in);

            if (in.getOffs() - offs != size) {
                // section exists, but not need to read it; all data is in FileHeader
                extendedInfo = Zip64.ExtendedInfo.NULL;
                in.seek(offs + size);
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
                                     .localFileHeaderOffs(offsLocalHeaderRelative)
                                     .disk(disk).build();
        }

        @Override
        public String toString() {
            return "ZIP64";
        }

    }

}
