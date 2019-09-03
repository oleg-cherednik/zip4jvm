package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Paths;

import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 28.08.2019
 */
@Getter
@Setter
public class FileHeaderPathZipEntry extends PathZipEntry {

    private final long uncompressedSize;
    private final boolean dir;

    private long compressedSize;
    private long checksum;

    public FileHeaderPathZipEntry(CentralDirectory.FileHeader fileHeader) {
        super(Paths.get(fileHeader.getFileName()), fileHeader.getLastModifiedTime());
        compressedSize = getCompressedSize(fileHeader);
        uncompressedSize = getUncompressedSize(fileHeader);
        checksum = fileHeader.getCrc32();
        dir = ZipUtils.isDirectory(fileHeader.getFileName());

        setZip64(fileHeader.isZip64());
        setEncryption(fileHeader.getEncryption());
        setCompression(fileHeader.getCompression());
        setCompressionLevel(fileHeader.getGeneralPurposeFlag().getCompressionLevel());
        setStrength(fileHeader.getExtraField().getAesExtraDataRecord().getStrength());

        setCompressedSize(compressedSize);
        setDisk(getDisk(fileHeader));
        setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());

        setFileName(fileHeader.getFileName());
    }

    private static long getDisk(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getDisk() == ZipModel.MAX_TOTAL_DISKS)
            return fileHeader.getExtraField().getExtendedInfo().getDisk();
        return fileHeader.getDisk();
    }

    private static long getCompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD)
            return fileHeader.getExtraField().getExtendedInfo().getCompressedSize();
        return fileHeader.getCompressedSize();
    }

    private static long getUncompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD)
            return fileHeader.getExtraField().getExtendedInfo().getUncompressedSize();
        return fileHeader.getUncompressedSize();
    }

    @Override
    public boolean isDirectory() {
        return dir;
    }

    @Override
    public long getChecksum() {
        return checksum;
    }

    @Override
    public long getUncompressedSize() {
        return uncompressedSize;
    }

    @Override
    public long getExpectedCompressedSize() {
        return compressedSize;
    }

    @Override
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    @Override
    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    @Override
    public void setFileName(String fileName) {
        if (dir && StringUtils.isNotBlank(fileName) && !ZipUtils.isDirectory(fileName))
            fileName += '/';
        super.setFileName(fileName);
    }

    @Override
    public boolean isDataDescriptorAvailable() {
        if (dataDescriptorAvailable != null)
            return dataDescriptorAvailable;
        return !dir;
    }
}
