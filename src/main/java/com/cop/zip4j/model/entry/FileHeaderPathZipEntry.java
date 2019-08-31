package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.utils.ZipUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Paths;

import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 28.08.2019
 */
public class FileHeaderPathZipEntry extends PathZipEntry {

    private final long compressedSize;
    private final long uncompressedSize;
    private final long checksum;
    private final boolean dir;

    public FileHeaderPathZipEntry(CentralDirectory.FileHeader fileHeader) {
        super(Paths.get(fileHeader.getFileName()), fileHeader.getLastModifiedTime());
        compressedSize = getCompressedSize(fileHeader);
        uncompressedSize = getUncompressedSize(fileHeader);
        checksum = fileHeader.getCrc32();
        dir = ZipUtils.isDirectory(fileHeader.getFileName());

        setEncryption(fileHeader.getEncryption());
        setCompression(fileHeader.getCompression());
        setCompressionLevel(fileHeader.getGeneralPurposeFlag().getCompressionLevel());
        setStrength(fileHeader.getExtraField().getAesExtraDataRecord().getStrength());

        setCompressedSizeWithEncryptionHeader(compressedSize);
        setDisc(fileHeader.getDiskNumber());
        setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());

        setName(fileHeader.getFileName());
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
    public long checksum() {
        return checksum;
    }

    @Override
    public long size() {
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
    public void setName(String name) {
        if (dir && StringUtils.isNotBlank(name) && !ZipUtils.isDirectory(name))
            name += '/';
        super.setName(name);
    }

    public boolean isDataDescriptorAvailable() {
        if (dataDescriptorAvailable != null)
            return dataDescriptorAvailable;
        return !dir;
    }
}
