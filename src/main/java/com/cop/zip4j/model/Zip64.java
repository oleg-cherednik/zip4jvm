package com.cop.zip4j.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@RequiredArgsConstructor
public class Zip64 {

    public static final Zip64 NULL = new Zip64(null, null);

    private final EndCentralDirectoryLocator endCentralDirectoryLocator;
    private final EndCentralDirectory endCentralDirectory;

    @Override
    public String toString() {
        return this == NULL ? "<null>" : super.toString();
    }

    /**
     * see 4.3.15
     */
    @Getter
    @Setter
    public static class EndCentralDirectoryLocator {

        public static final int SIGNATURE = 0x07064B50;

        // size (20) with comment length = 0
        public static final int SIZE = 4 + 4 + 8 + 4;

        // size:4 - signature (0x06054b50)
        // size:4 - number of the disk with the start of the zip64 end of central directory
        private long startDisk;
        // size:8 - relative offset of the Zip64.EndCentralDirectory
        private long offs;
        // size:4 - total number of disks
        private long totalDisks;

        public void updateOffsetZip64EndOfCentralDirRec(long delta) {
            offs += delta;
        }

    }

    /**
     * see 4.3.14
     */
    @Getter
    @Setter
    public static class EndCentralDirectory {

        public static final int SIGNATURE = 0x06064B50;
        // size (44) with extensibleDataSector length = 0
        public static final int SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

        // size:4 - signature (0x06064b50)
        // size:8 - directory record (n)
        private long sizeEndCentralDirectory;
        // size:2 - version made by
        private int versionMadeBy;
        // size:2 - version needed to extractEntries
        private int versionNeededToExtract;
        // size:4 - number of this disk
        private long disk;
        // size:4 - number of the disk with the start of the central directory
        private long startDisk;
        // size:8 - total number of entries in the central directory on this disk
        private long diskEntries;
        // size:8 - total number of entries in the central directory
        private long totalEntries;
        // size:8 - size of the central directory
        private long size;
        // size:8 - offs of CentralDirectory in startDiskNumber
        private long offs;
        // size:n-44 - extensible data sector
        private byte[] extensibleDataSector;

        public void updateOffsetStartCenDirWRTStartDiskNo(long delta) {
            offs += delta;
        }

    }

    @Getter
    @Builder(toBuilder = true)
    public static class ExtendedInfo {

        public static final int SIGNATURE = 0x1;
        public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

        // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
        // size:2 - size of this "extra" block
        private final int size;
        // size:8 - original uncompressed file size
        private final long uncompressedSize;
        // size:8 - size of compressed data
        private final long compressedSize;
        // size:8 - offset of local header record
        private final long offsLocalHeaderRelative;
        // size:4 - number of the disk on which  this file starts
        private final long diskNumber;

        public int getLength() {
            int length = 0;

            length += uncompressedSize != ExtraField.NO_DATA ? 8 : 0;
            length += compressedSize != ExtraField.NO_DATA ? 8 : 0;
            length += offsLocalHeaderRelative != ExtraField.NO_DATA ? 8 : 0;

            return length != 0 ? length + SIZE_FIELD : 0;
        }

        public static final ExtendedInfo NULL = new ExtendedInfo(0, ExtraField.NO_DATA, ExtraField.NO_DATA, ExtraField.NO_DATA, ExtraField.NO_DATA) {

            @Override
            public String toString() {
                return "<null>";
            }
        };
    }

}
