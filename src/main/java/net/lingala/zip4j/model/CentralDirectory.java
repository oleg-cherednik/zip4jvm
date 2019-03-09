/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.unzip.Unzip;
import net.lingala.zip4j.util.BitUtils;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.lingala.zip4j.util.BitUtils.BIT0;
import static net.lingala.zip4j.util.BitUtils.BIT3;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@Getter
@Setter
public class CentralDirectory {

    @NonNull
    private List<FileHeader> fileHeaders = Collections.emptyList();
    private DigitalSignature digitalSignature;

    public void addFileHeader(FileHeader fileHeader) {
        fileHeaders = fileHeaders.isEmpty() ? new ArrayList<>() : fileHeaders;
        fileHeaders.add(fileHeader);
    }

    @Getter
    @Setter
    public static class FileHeader {

        // size:4 - signature (0x02014b50)
        private final int signature = InternalZipConstants.CENSIG;
        // size:2 - version made by
        private int versionMadeBy;
        // size:2 - version needed to extract
        private int versionNeededToExtract;
        // size:2 - general purpose bit flag
        private byte[] generalPurposeFlag;
        // size:2 - compression method
        @NonNull
        private CompressionMethod compressionMethod = CompressionMethod.STORE;
        // size:2 - last mod file time
        // size:2 - last mod file date
        private int lastModFileTime;
        // size:4 - crc-32
        private long crc32;
        @Deprecated
        private byte[] crcBuff;
        // size:4 - compressed size
        private long compressedSize;
        // size:4 - uncompressed size
        private long uncompressedSize;
        // size:2 - file name length (n)
        private int fileNameLength;
        // size:2 - extra field length (m)
        private int extraFieldLength;
        // size:2 - file comment length
        private int fileCommentLength;
        // size:2 - disk number start
        private int diskNumberStart;
        // size:2 - internal file attributes
        private byte[] internalFileAttr;
        // size:4 - external file attributes
        private byte[] externalFileAttr;
        // size:4 - relative offset of local header
        private long offLocalHeaderRelative;
        // size:n - file name
        private String fileName;
        // size:m - extra field
        @NonNull
        private List<ExtraDataRecord> extraDataRecords = Collections.emptyList();
        // size:k - extra field
        private String fileComment;
        @NonNull
        private Encryption encryption = Encryption.OFF;
        private char[] password;
        private boolean dataDescriptorExists;
        private Zip64ExtendedInfo zip64ExtendedInfo;
        private AESExtraDataRecord aesExtraDataRecord;

        // TODO should be true, when file name charset is UTF8
        private boolean fileNameUTF8Encoded;

        // TODO this is data class, this logic should be moved out

        /**
         * Extracts file to the specified directory
         *
         * @param zipModel
         * @param outPath
         * @throws ZipException
         */
        public void extractFile(ZipModel zipModel, String outPath, boolean runInThread) throws ZipException {
            extractFile(zipModel, outPath, null, runInThread);
        }

        /**
         * Extracts file to the specified directory using any
         * user defined parameters in UnzipParameters
         *
         * @param zipModel
         * @param outPath
         * @param unzipParameters
         * @throws ZipException
         */
        public void extractFile(ZipModel zipModel, String outPath,
                UnzipParameters unzipParameters, boolean runInThread) throws ZipException {
            extractFile(zipModel, outPath, unzipParameters, null, runInThread);
        }

        /**
         * Extracts file to the specified directory using any
         * user defined parameters in UnzipParameters. Output file name
         * will be overwritten with the value in newFileName. If this
         * parameter is null, then file name will be the same as in
         * FileHeader.getFileName
         *
         * @param zipModel
         * @param outPath
         * @param unzipParameters
         * @throws ZipException
         */
        public void extractFile(ZipModel zipModel, String outPath,
                UnzipParameters unzipParameters, String newFileName,
                boolean runInThread) throws ZipException {
            if (zipModel == null) {
                throw new ZipException("input zipModel is null");
            }

            if (!Zip4jUtil.checkOutputFolder(outPath)) {
                throw new ZipException("Invalid output path");
            }

            if (this == null) {
                throw new ZipException("invalid file header");
            }
            Unzip unzip = new Unzip(zipModel);
            unzip.extractFile(this, outPath, unzipParameters, newFileName, runInThread);
        }


        // -----------

        public void setGeneralPurposeFlag(byte[] generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            fileNameUTF8Encoded = generalPurposeFlag != null && BitUtils.isBitSet(generalPurposeFlag[1], BIT3);
            dataDescriptorExists = generalPurposeFlag != null && BitUtils.isBitSet(generalPurposeFlag[0], BIT3);
        }

        public boolean isDirectory() {
            return Zip4jUtil.isDirectory(fileName);
        }

        public boolean isEncrypted() {
            return generalPurposeFlag != null && BitUtils.isBitSet(generalPurposeFlag[0], BIT0);
        }

        public void setEncrypted(boolean val) {
            generalPurposeFlag = generalPurposeFlag != null ? generalPurposeFlag : new byte[2];

            if (val)
                BitUtils.setBits(generalPurposeFlag[0], BIT0);
            else
                BitUtils.clearBits(generalPurposeFlag[0], BIT0);
        }

        public void setZip64ExtendedInfo(Zip64ExtendedInfo info) {
            zip64ExtendedInfo = info;

            if (info != null) {
                uncompressedSize = info.getUnCompressedSize() != -1 ? info.getUnCompressedSize() : uncompressedSize;
                compressedSize = info.getCompressedSize() != -1 ? info.getCompressedSize() : uncompressedSize;
                offLocalHeaderRelative = info.getOffsLocalHeaderRelative() != -1 ? info.getOffsLocalHeaderRelative() : offLocalHeaderRelative;
                diskNumberStart = info.getDiskNumberStart() != -1 ? info.getDiskNumberStart() : diskNumberStart;
            }
        }

        public void setAesExtraDataRecord(AESExtraDataRecord record) {
            aesExtraDataRecord = record;
            encryption = record != null ? Encryption.AES : Encryption.OFF;
        }
    }

    @Getter
    @Setter
    public static class DigitalSignature {

        // size:4 - header signature (0x06054b50)
        private final int signature = InternalZipConstants.DIGSIG;
        // size:2 - size of data (n)
        private int sizeOfData;
        // size:n - signature data
        private byte[] signatureData;

    }

}
