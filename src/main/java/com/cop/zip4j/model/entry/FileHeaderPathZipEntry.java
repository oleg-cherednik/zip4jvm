package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.io.OutputStream;

import static com.cop.zip4j.model.ZipModel.MAX_ENTRY_SIZE;
import static com.cop.zip4j.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 28.08.2019
 */
@Getter
@Setter
public class FileHeaderPathZipEntry extends PathZipEntry {

    private final long uncompressedSize;
    private final long checksum;
    private final ExternalFileAttributes externalFileAttributes;
    private final boolean dir;

    private long compressedSize;

    public FileHeaderPathZipEntry(CentralDirectory.FileHeader fileHeader) {
        super(null, fileHeader.getLastModifiedTime());
        uncompressedSize = getUncompressedSize(fileHeader);
        checksum = fileHeader.getCrc32();
        compressedSize = getCompressedSize(fileHeader);
        externalFileAttributes = fileHeader.getExternalFileAttributes();
        dir = ZipUtils.isDirectory(fileHeader.getFileName());

        setZip64(fileHeader.isZip64());
        setEncryption(fileHeader.getEncryption());
        setCompression(fileHeader.getCompression());
        setCompressionLevel(fileHeader.getGeneralPurposeFlag().getCompressionLevel());

        setDisk(getDisk(fileHeader));
        setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());

        super.setFileName(fileHeader.getFileName());
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
        throw new NotImplementedException();
    }

    @Override
    public boolean isDataDescriptorAvailable() {
        if (dataDescriptorAvailable != null)
            return dataDescriptorAvailable;
        return !dir;
    }

    @Override
    public long write(OutputStream out) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return getFileName();
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
