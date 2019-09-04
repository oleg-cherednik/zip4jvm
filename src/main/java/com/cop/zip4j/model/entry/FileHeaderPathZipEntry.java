package com.cop.zip4j.model.entry;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import com.cop.zip4j.utils.function.IOSupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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
        String fileName = ZipUtils.normalizeFileName.apply(fileHeader.getFileName());
        int lastModifiedTime = fileHeader.getLastModifiedTime();
        ExternalFileAttributes externalFileAttributes = fileHeader.getExternalFileAttributes();

        if (ZipUtils.isDirectory(fileName))
            return apply(new DirectoryZipEntry(fileName, lastModifiedTime, externalFileAttributes), fileHeader);

        long uncompressedSize = getUncompressedSize(fileHeader);
        Compression compression = fileHeader.getCompression();
        CompressionLevel compressionLevel = fileHeader.getGeneralPurposeFlag().getCompressionLevel();
        Encryption encryption = fileHeader.getEncryption();
        boolean zip64 = fileHeader.isZip64();
        IOSupplier<InputStream> inputStream = () -> NullInputStream.INSTANCE;
        return apply(new RegularFileZipEntry(fileName, uncompressedSize, lastModifiedTime, compression, compressionLevel, encryption, zip64,
                externalFileAttributes, inputStream), fileHeader);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NullInputStream extends InputStream {

        public static final NullInputStream INSTANCE = new NullInputStream();

        @Override
        public int read() throws IOException {
            return IOUtils.EOF;
        }
    }

    private static PathZipEntry apply(PathZipEntry zipEntry, CentralDirectory.FileHeader fileHeader) {
        zipEntry.setChecksum(fileHeader.getCrc32());
        zipEntry.setCompressedSize(getCompressedSize(fileHeader));
        zipEntry.setDisk(getDisk(fileHeader));
        zipEntry.setLocalFileHeaderOffs(fileHeader.getOffsLocalFileHeader());
        return zipEntry;
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
