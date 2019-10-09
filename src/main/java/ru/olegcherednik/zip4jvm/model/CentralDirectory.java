package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * see 4.3.12
 *
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@Getter
@Setter
public class CentralDirectory {

    private List<FileHeader> fileHeaders = Collections.emptyList();
    private DigitalSignature digitalSignature;

    /** see 4.3.12 */
    @Getter
    @Setter
    public static class FileHeader {

        public static final int SIGNATURE = 0x02014B50;
        public static final int VERSION = 62;

        // size:4 - signature (0x02014b50)
        // size:2 - version made by
        private int versionMadeBy = VERSION;
        // size:2 - version needed to extractEntries
        private int versionToExtract = VERSION;
        // size:2 - general purpose bit flag
        private GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        // size:2 - compression method
        private CompressionMethod compressionMethod = CompressionMethod.STORE;
        // size:2 - last mod file time
        // size:2 - last mod file date
        private int lastModifiedTime;
        // size:4 - checksum
        private long crc32;
        // size:4 - compressed size
        private long compressedSize;
        // size:4 - uncompressed size
        private long uncompressedSize;
        // size:2 - file name length (n)
        // size:2 - extra field length (m)
        // size:2 - comment length (k)
        private int commentLength;
        // size:2 - disk number start
        private int disk;
        // size:2 - internal file attributes
        private InternalFileAttributes internalFileAttributes = InternalFileAttributes.NULL;
        // size:4 - external file attributes
        private ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        // size:4 - relative offset of local header
        private long localFileHeaderOffs;
        // size:n - file name
        private String fileName;
        // size:m - extra field
        private ExtraField extraField = ExtraField.NULL;
        // size:k - comment
        private String comment;

        public byte[] getFileName(Charset charset) {
            return fileName == null ? ArrayUtils.EMPTY_BYTE_ARRAY : fileName.getBytes(charset);
        }

        public byte[] getComment(Charset charset) {
            return comment == null ? ArrayUtils.EMPTY_BYTE_ARRAY : comment.getBytes(charset);
        }

        public Compression getCompression() {
            if (compressionMethod == CompressionMethod.AES)
                return Compression.parseCompressionMethod(extraField.getAesExtraDataRecord().getCompressionMethod());
            return Compression.parseCompressionMethod(compressionMethod);
        }

        public boolean isZip64() {
            return extraField.getExtendedInfo() != Zip64.ExtendedInfo.NULL;
        }

        public void setExtraField(ExtraField extraField) {
            this.extraField = ExtraField.builder().addRecord(extraField).build();
            generalPurposeFlag.setEncrypted(isEncrypted());
        }

        public void setGeneralPurposeFlagData(int data) {
            generalPurposeFlag.read(data);
            generalPurposeFlag.setEncrypted(isEncrypted());
        }

        public boolean isEncrypted() {
            return getEncryption() != Encryption.OFF;
        }

        public Encryption getEncryption() {
            return Encryption.get(extraField, generalPurposeFlag);
        }

        public boolean isWriteZip64OffsetLocalHeader() {
            return localFileHeaderOffs > Zip64.LIMIT_DWORD;
        }

        @Override
        public String toString() {
            return fileName;
        }

    }

    @Getter
    @Setter
    public static class DigitalSignature {

        public static final int SIGNATURE = 0x05054B50;

        // size:4 - header signature (0x06054b50)
        // size:2 - size of data (n)
        // size:n - signature data
        private byte[] signatureData;

    }

    @Getter
    @Setter
    public static class StrongEncryption implements ExtraField.Record {


        public static final int SIGNATURE = 0x0017;

        // size:2 - tag for this "extra" block type (0x0017)
        // size:2 - the data format identifier
        private int format;
        // size:8 - size of compressed data
        private long compressedSize;
        // size:8 - offset of local header record
        private long localFileHeaderOffs;
        // size:4 - number of the disk on which  this file starts
        private long disk;


        @Override
        public int getSignature() {
            return SIGNATURE;
        }

        @Override
        public int getBlockSize() {
            return 0;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public void write(DataOutput out) throws IOException {

        }
    }

}
