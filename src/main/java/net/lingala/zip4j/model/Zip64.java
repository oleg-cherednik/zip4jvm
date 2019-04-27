package net.lingala.zip4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.utils.InternalZipConstants;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@RequiredArgsConstructor
public class Zip64 {

    private final EndCentralDirectoryLocator endCentralDirectoryLocator;
    private final EndCentralDirectory endCentralDirectory;

    @Getter
    @Setter
    public static class EndCentralDirectoryLocator {

        public static final int SIGNATURE = 0x07064B50;

        // size (20) with comment length = 0
        public static final int SIZE = 4 + 4 + 8 + 4;

        // size:4 - signature (0x06054b50)
        private final int signature = SIGNATURE;
        // size:4 - number of the disk with the start of the zip64 end of central directory
        private int noOfDiskStartOfZip64EndOfCentralDirRec;
        // size:8 - relative offset of the zip64 end of central directory record
        private long offs;
        // size:4 - total number of disks
        private int totNumberOfDiscs;

        public void updateOffsetZip64EndOfCentralDirRec(long delta) {
            offs += delta;
        }

    }

    @Getter
    @Setter
    public static class EndCentralDirectory {

        // size (44) with extensibleDataSector length = 0
        public static final int SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

        // size:4 - signature (0x06064b50)
        private final int signature = InternalZipConstants.ZIP64_ENDSIG;
        // size:8 - directory record (n)
        private long sizeOfZip64EndCentralDirRec;
        // size:2 - version made by
        private short versionMadeBy;
        // size:2 - version needed to extractEntries
        private short versionNeededToExtract;
        // size:4 - number of this disk
        private int diskNumber;
        // size:4 - number of the disk with the start of the central directory
        private int startDiskNumber;
        // size:8 - total number of entries in the central directory on this disk
        private long diskEntries;
        // size:8 - total number of entries in the central directory
        private long totalEntries;
        // size:8 - size of the central directory
        private long size;
        // size:8 - directory with respect to the starting disk number
        private long offs;
        // size:n-44 - extensible data sector
        private byte[] extensibleDataSector;

        public void updateOffsetStartCenDirWRTStartDiskNo(long delta) {
            offs += delta;
        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ExtendedInfo {

        public static final short SIGNATURE = 0x0001;
        public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

        // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
        private final short signature = SIGNATURE;
        // size:2 - size of this "extra" block
        private int size;
        // size:8 - original uncompressed file size
        @Builder.Default
        private long uncompressedSize = ExtraField.NO_DATA;
        // size:8 - size of compressed data
        @Builder.Default
        private long compressedSize = ExtraField.NO_DATA;
        // size:8 - offset of local header record
        @Builder.Default
        private long offsLocalHeaderRelative = ExtraField.NO_DATA;
        // size:4 - number of the disk on which  this file starts
        @Builder.Default
        private int diskNumber = ExtraField.NO_DATA;

        public int getLength() {
            int length = 0;

            length += uncompressedSize != ExtraField.NO_DATA ? 8 : 0;
            length += compressedSize != ExtraField.NO_DATA ? 8 : 0;
            length += offsLocalHeaderRelative != ExtraField.NO_DATA ? 8 : 0;

            return length != 0 ? length + SIZE_FIELD : 0;
        }

        public static final ExtendedInfo NULL = new ExtendedInfo() {

            private final NullPointerException exception = new NullPointerException("Null object modification: " + getClass().getSimpleName());

            @Override
            public void setSize(int size) {
                throw exception;
            }

            @Override
            public void setUncompressedSize(long uncompressedSize) {
                throw exception;
            }

            @Override
            public void setCompressedSize(long compressedSize) {
                throw exception;
            }

            @Override
            public void setOffsLocalHeaderRelative(long offsLocalHeaderRelative) {
                throw exception;
            }

            @Override
            public void setDiskNumber(int diskNumber) {
                throw exception;
            }

            @Override
            public int getLength() {
                return 0;
            }
        };
    }

}
