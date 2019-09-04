package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.Setter;

import static com.cop.zip4j.model.ZipModel.MAX_ENTRY_SIZE;
import static com.cop.zip4j.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 28.08.2019
 */
@Getter
@Setter
public class FileHeaderPathZipEntry extends PathZipEntry {

    private final long checksum;
    private final boolean dir;

    private long compressedSize;

    public static PathZipEntry create(CentralDirectory.FileHeader fileHeader) {
        return new FileHeaderPathZipEntry(fileHeader);
    }

    private FileHeaderPathZipEntry(CentralDirectory.FileHeader fileHeader) {
        super(ZipUtils.normalizeFileName.apply(fileHeader.getFileName()), getUncompressedSize(fileHeader), fileHeader.getLastModifiedTime(),
                fileHeader.getCompression(), fileHeader.getGeneralPurposeFlag().getCompressionLevel(), fileHeader.getEncryption(),
                fileHeader.isZip64(), fileHeader.getExternalFileAttributes());
        checksum = fileHeader.getCrc32();
        compressedSize = getCompressedSize(fileHeader);
        dir = ZipUtils.isDirectory(fileHeader.getFileName());
        setDataDescriptorAvailable(() -> !dir);

        setDisk(getDisk(fileHeader));
        setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());
    }

    @Override
    public boolean isRegularFile() {
        return !dir;
    }

    @Override
    public boolean isDirectory() {
        return dir;
    }

    @Override
    public long getChecksum() {
        return checksum;
    }

    private static long getDisk(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getDisk() == MAX_TOTAL_DISKS)
            return fileHeader.getExtraField().getExtendedInfo().getDisk();
        return fileHeader.getDisk();
    }

    private static long getCompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getCompressedSize() == MAX_ENTRY_SIZE)
            return fileHeader.getExtraField().getExtendedInfo().getCompressedSize();
        return fileHeader.getCompressedSize();
    }

    private static long getUncompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getUncompressedSize() == MAX_ENTRY_SIZE)
            return fileHeader.getExtraField().getExtendedInfo().getUncompressedSize();
        return fileHeader.getUncompressedSize();
    }
}
