package com.cop.zip4j.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip64 {

    public static final Zip64 NULL = new Zip64(null, null);
    public static final long LIMIT = 0xFFFF_FFFFL;

    private final EndCentralDirectoryLocator endCentralDirectoryLocator;
    private final EndCentralDirectory endCentralDirectory;

    public static Zip64 of(EndCentralDirectoryLocator locator, EndCentralDirectory dir) {
        return locator == null || dir == null ? NULL : new Zip64(locator, dir);
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : super.toString();
    }

    /** see 4.3.15 */
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

    }

    /** see 4.3.14 */
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

        public void incTotalEntries() {
            totalEntries++;
        }

    }

    /** see 4.5.3 */
    @Getter
    @Builder
    public static final class ExtendedInfo {

        public static final ExtendedInfo NULL = builder().build();

        public static final int SIGNATURE = 0x1;
        public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

        // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
        // size:2 - size of this "extra" block
        // size:8 - original uncompressed file size
        @Builder.Default
        private final long uncompressedSize = ExtraField.NO_DATA;
        // size:8 - size of compressed data
        @Builder.Default
        private final long compressedSize = ExtraField.NO_DATA;
        // size:8 - offset of local header record
        @Builder.Default
        private final long offsLocalHeaderRelative = ExtraField.NO_DATA;
        // size:4 - number of the disk on which  this file starts
        @Builder.Default
        private final long diskNumber = ExtraField.NO_DATA;

        @SuppressWarnings("ConstantConditions")
        public int getDataSize() {
            int size = 0;

            size += uncompressedSize == ExtraField.NO_DATA ? 0 : 8;
            size += compressedSize == ExtraField.NO_DATA ? 0 : 8;
            size += offsLocalHeaderRelative == ExtraField.NO_DATA ? 0 : 8;

            return size;
        }

        public int getBlockSize() {
            int size = getDataSize();
            return size == 0 ? 0 : size + SIZE_FIELD;
        }

        @Override
        public String toString() {
            return this == NULL ? "<null>" : super.toString();
        }
    }

}
