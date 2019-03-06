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

package net.lingala.zip4j.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class HeaderReader {

    private final RandomAccessFile zip4jRaf;

    /**
     * Reads extra data record and saves it in the {@link LocalFileHeader}
     *
     * @param localFileHeader
     * @throws ZipException
     */
    private void readAndSaveExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {

        if (zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read extra data record");
        }

        if (localFileHeader == null) {
            throw new ZipException("file header is null");
        }

        int extraFieldLength = localFileHeader.getExtraFieldLength();
        if (extraFieldLength <= 0) {
            return;
        }

        localFileHeader.setExtraDataRecords(readExtraDataRecords(extraFieldLength));

    }

    /**
     * Reads extra data records
     *
     * @param extraFieldLength
     * @return ArrayList of {@link ExtraDataRecord}
     * @throws ZipException
     */
    private ArrayList readExtraDataRecords(int extraFieldLength) throws ZipException {

        if (extraFieldLength <= 0) {
            return null;
        }

        try {
            byte[] extraFieldBuf = new byte[extraFieldLength];
            zip4jRaf.read(extraFieldBuf);

            int counter = 0;
            ArrayList extraDataList = new ArrayList();
            while (counter < extraFieldLength) {
                ExtraDataRecord extraDataRecord = new ExtraDataRecord();
                short header = (short)Raw.readShortLittleEndian(extraFieldBuf, counter);
                extraDataRecord.setHeader(header);
                counter = counter + 2;
                int sizeOfRec = Raw.readShortLittleEndian(extraFieldBuf, counter);

                if ((2 + sizeOfRec) > extraFieldLength) {
                    sizeOfRec = Raw.readShortBigEndian(extraFieldBuf, counter);
                    if ((2 + sizeOfRec) > extraFieldLength) {
                        //If this is the case, then extra data record is corrupt
                        //skip reading any further extra data records
                        break;
                    }
                }

                extraDataRecord.setSizeOfData(sizeOfRec);
                counter = counter + 2;

                if (sizeOfRec > 0) {
                    byte[] data = new byte[sizeOfRec];
                    System.arraycopy(extraFieldBuf, counter, data, 0, sizeOfRec);
                    extraDataRecord.setData(data);
                }
                counter = counter + sizeOfRec;
                extraDataList.add(extraDataRecord);
            }
            if (extraDataList.size() > 0) {
                return extraDataList;
            } else {
                return null;
            }
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    /**
     * Reads Zip64 Extended Info and saves it in the {@link LocalFileHeader}
     *
     * @param localFileHeader
     * @throws ZipException
     */
    private void readAndSaveZip64ExtendedInfo(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        }

        if (CollectionUtils.isEmpty(localFileHeader.getExtraDataRecords()))
            return;

        Zip64ExtendedInfo zip64ExtendedInfo = readZip64ExtendedInfo(
                localFileHeader.getExtraDataRecords(),
                localFileHeader.getUncompressedSize(),
                localFileHeader.getCompressedSize(),
                -1, -1);

        if (zip64ExtendedInfo != null) {
            localFileHeader.setZip64ExtendedInfo(zip64ExtendedInfo);

            if (zip64ExtendedInfo.getUnCompressedSize() != -1)
                localFileHeader.setUncompressedSize(zip64ExtendedInfo.getUnCompressedSize());

            if (zip64ExtendedInfo.getCompressedSize() != -1)
                localFileHeader.setCompressedSize(zip64ExtendedInfo.getCompressedSize());
        }
    }

    /**
     * Reads Zip64 Extended Info
     *
     * @param extraDataRecords
     * @param unCompressedSize
     * @param compressedSize
     * @param offsetLocalHeader
     * @param diskNumberStart
     * @return {@link Zip64ExtendedInfo}
     * @throws ZipException
     */
    private Zip64ExtendedInfo readZip64ExtendedInfo(
            List<ExtraDataRecord> extraDataRecords,
            long unCompressedSize,
            long compressedSize,
            long offsetLocalHeader,
            int diskNumberStart) throws ZipException {

        for (int i = 0; i < extraDataRecords.size(); i++) {
            ExtraDataRecord extraDataRecord = extraDataRecords.get(i);
            if (extraDataRecord == null) {
                continue;
            }

            if (extraDataRecord.getHeader() == 0x0001) {

                Zip64ExtendedInfo zip64ExtendedInfo = new Zip64ExtendedInfo();

                byte[] byteBuff = extraDataRecord.getData();

                if (extraDataRecord.getSizeOfData() <= 0) {
                    break;
                }
                byte[] longByteBuff = new byte[8];
                byte[] intByteBuff = new byte[4];
                int counter = 0;
                boolean valueAdded = false;

                if (((unCompressedSize & 0xFFFF) == 0xFFFF) && counter < extraDataRecord.getSizeOfData()) {
                    System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                    long val = Raw.readLongLittleEndian(longByteBuff, 0);
                    zip64ExtendedInfo.setUnCompressedSize(val);
                    counter += 8;
                    valueAdded = true;
                }

                if (((compressedSize & 0xFFFF) == 0xFFFF) && counter < extraDataRecord.getSizeOfData()) {
                    System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                    long val = Raw.readLongLittleEndian(longByteBuff, 0);
                    zip64ExtendedInfo.setCompressedSize(val);
                    counter += 8;
                    valueAdded = true;
                }

                if (((offsetLocalHeader & 0xFFFF) == 0xFFFF) && counter < extraDataRecord.getSizeOfData()) {
                    System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                    long val = Raw.readLongLittleEndian(longByteBuff, 0);
                    zip64ExtendedInfo.setOffsLocalHeaderRelative(val);
                    counter += 8;
                    valueAdded = true;
                }

                if (((diskNumberStart & 0xFFFF) == 0xFFFF) && counter < extraDataRecord.getSizeOfData()) {
                    System.arraycopy(byteBuff, counter, intByteBuff, 0, 4);
                    int val = Raw.readIntLittleEndian(intByteBuff, 0);
                    zip64ExtendedInfo.setDiskNumberStart(val);
                    counter += 8;
                    valueAdded = true;
                }

                if (valueAdded) {
                    return zip64ExtendedInfo;
                }

                break;
            }
        }
        return null;
    }

    /**
     * Sets the current random access file pointer at the start of signature
     * of the zip64 end of central directory record
     *
     * @throws ZipException
     */
    private void setFilePointerToReadZip64EndCentralDirLoc() throws ZipException {
        try {
            byte[] ebs = new byte[4];
            long pos = zip4jRaf.length() - EndCentralDirectory.MIN_SIZE;

            do {
                zip4jRaf.seek(pos--);
            } while (Raw.readLeInt(zip4jRaf, ebs) != InternalZipConstants.ENDSIG);

            // Now the file pointer is at the end of signature of Central Dir Rec
            // Seek back with the following values
            // 4 -> end of central dir signature
            // 4 -> total number of disks
            // 8 -> relative offset of the zip64 end of central directory record
            // 4 -> number of the disk with the start of the zip64 end of central directory
            // 4 -> zip64 end of central dir locator signature
            // Refer to Appnote for more information
            //TODO: Donot harcorde these values. Make use of ZipConstants
            zip4jRaf.seek(zip4jRaf.getFilePointer() - 4 - 4 - 8 - 4 - 4);
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    /**
     * Reads local file header for the given file header
     *
     * @param fileHeader
     * @return {@link LocalFileHeader}
     * @throws ZipException
     */
    public LocalFileHeader readLocalFileHeader(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null || zip4jRaf == null) {
            throw new ZipException("invalid read parameters for local header");
        }

        long locHdrOffset = fileHeader.getOffLocalHeaderRelative();

        if (fileHeader.getZip64ExtendedInfo() != null) {
            Zip64ExtendedInfo zip64ExtendedInfo = fileHeader.getZip64ExtendedInfo();
            if (zip64ExtendedInfo.getOffsLocalHeaderRelative() > 0) {
                locHdrOffset = fileHeader.getOffLocalHeaderRelative();
            }
        }

        if (locHdrOffset < 0) {
            throw new ZipException("invalid local header offset");
        }

        try {
            zip4jRaf.seek(locHdrOffset);

            int length = 0;
            LocalFileHeader localFileHeader = new LocalFileHeader();

            byte[] shortBuff = new byte[2];
            byte[] intBuff = new byte[4];
            byte[] longBuff = new byte[8];

            //signature
            readIntoBuff(zip4jRaf, intBuff);
            int sig = Raw.readIntLittleEndian(intBuff, 0);
            if (sig != InternalZipConstants.LOCSIG) {
                throw new ZipException("invalid local header signature for file: " + fileHeader.getFileName());
            }
            localFileHeader.setSignature(sig);
            length += 4;

            //version needed to extract
            readIntoBuff(zip4jRaf, shortBuff);
            localFileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
            length += 2;

            //general purpose bit flag
            readIntoBuff(zip4jRaf, shortBuff);
            localFileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & InternalZipConstants.UFT8_NAMES_FLAG) != 0);
            int firstByte = shortBuff[0];
            int result = firstByte & 1;
            if (result != 0) {
                localFileHeader.setEncrypted(true);
            }
            localFileHeader.setGeneralPurposeFlag(shortBuff);
            length += 2;

            //Check if data descriptor exists for local file header
            String binary = Integer.toBinaryString(firstByte);
            if (binary.length() >= 4)
                localFileHeader.setDataDescriptorExists(binary.charAt(3) == '1');

            //compression method
            readIntoBuff(zip4jRaf, shortBuff);
            localFileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
            length += 2;

            //last mod file time
            readIntoBuff(zip4jRaf, intBuff);
            localFileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
            length += 4;

            //crc-32
            readIntoBuff(zip4jRaf, intBuff);
            localFileHeader.setCrc32(Raw.readIntLittleEndian(intBuff, 0));
            localFileHeader.setCrcBuff((byte[])intBuff.clone());
            length += 4;

            //compressed size
            readIntoBuff(zip4jRaf, intBuff);
            longBuff = getLongByteFromIntByte(intBuff);
            localFileHeader.setCompressedSize(Raw.readLongLittleEndian(longBuff, 0));
            length += 4;

            //uncompressed size
            readIntoBuff(zip4jRaf, intBuff);
            longBuff = getLongByteFromIntByte(intBuff);
            localFileHeader.setUncompressedSize(Raw.readLongLittleEndian(longBuff, 0));
            length += 4;

            //file name length
            readIntoBuff(zip4jRaf, shortBuff);
            int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
            localFileHeader.setFileNameLength(fileNameLength);
            length += 2;

            //extra field length
            readIntoBuff(zip4jRaf, shortBuff);
            int extraFieldLength = Raw.readShortLittleEndian(shortBuff, 0);
            localFileHeader.setExtraFieldLength(extraFieldLength);
            length += 2;

            //file name
            if (fileNameLength > 0) {
                byte[] fileNameBuf = new byte[fileNameLength];
                readIntoBuff(zip4jRaf, fileNameBuf);
                // Modified after user reported an issue http://www.lingala.net/zip4j/forum/index.php?topic=2.0
//				String fileName = new String(fileNameBuf, "Cp850");
//				String fileName = Zip4jUtil.getCp850EncodedString(fileNameBuf);
                String fileName = Zip4jUtil.decodeFileName(fileNameBuf, localFileHeader.isFileNameUTF8Encoded());

                if (fileName == null) {
                    throw new ZipException("file name is null, cannot assign file name to local file header");
                }

                if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
                    fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
                }

                localFileHeader.setFileName(fileName);
                length += fileNameLength;
            } else {
                localFileHeader.setFileName(null);
            }

            //extra field
            readAndSaveExtraDataRecord(localFileHeader);
            length += extraFieldLength;

            localFileHeader.setOffsetStartOfData(locHdrOffset + length);

            //Copy password from fileHeader to localFileHeader
            localFileHeader.setPassword(fileHeader.getPassword());

            readAndSaveZip64ExtendedInfo(localFileHeader);

            readAndSaveAESExtraDataRecord(localFileHeader);

            if (localFileHeader.isEncrypted()) {

                if (localFileHeader.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_AES) {
                    //Do nothing
                } else {
                    if ((firstByte & 64) == 64) {
                        //hardcoded for now
                        localFileHeader.setEncryptionMethod(1);
                    } else {
                        localFileHeader.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
//						localFileHeader.setCompressedSize(localFileHeader.getCompressedSize()
//								- ZipConstants.STD_DEC_HDR_SIZE);
                    }
                }

            }

            if (localFileHeader.getCrc32() <= 0) {
                localFileHeader.setCrc32(fileHeader.getCrc32());
                localFileHeader.setCrcBuff(fileHeader.getCrcBuff());
            }

            if (localFileHeader.getCompressedSize() <= 0) {
                localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
            }

            if (localFileHeader.getUncompressedSize() <= 0) {
                localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
            }

            return localFileHeader;
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    /**
     * Reads AES Extra Data Record and saves it in the {@link LocalFileHeader}
     *
     * @param localFileHeader
     * @throws ZipException
     */
    private void readAndSaveAESExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        }

        if (localFileHeader.getExtraDataRecords() == null || localFileHeader.getExtraDataRecords().size() <= 0) {
            return;
        }

        AESExtraDataRecord aesExtraDataRecord = readAESExtraDataRecord(localFileHeader.getExtraDataRecords());
        if (aesExtraDataRecord != null) {
            localFileHeader.setAesExtraDataRecord(aesExtraDataRecord);
            localFileHeader.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        }
    }

    /**
     * Reads AES Extra Data Record
     *
     * @param extraDataRecords
     * @return {@link AESExtraDataRecord}
     * @throws ZipException
     */
    private AESExtraDataRecord readAESExtraDataRecord(List<ExtraDataRecord> extraDataRecords) throws ZipException {

        if (extraDataRecords == null) {
            return null;
        }

        for (int i = 0; i < extraDataRecords.size(); i++) {
            ExtraDataRecord extraDataRecord = extraDataRecords.get(i);
            if (extraDataRecord == null) {
                continue;
            }

            if (extraDataRecord.getHeader() == InternalZipConstants.AESSIG) {

                if (extraDataRecord.getData() == null) {
                    throw new ZipException("corruput AES extra data records");
                }

                AESExtraDataRecord aesExtraDataRecord = new AESExtraDataRecord();

                aesExtraDataRecord.setDataSize(extraDataRecord.getSizeOfData());

                byte[] aesData = extraDataRecord.getData();
                aesExtraDataRecord.setVersionNumber(Raw.readShortLittleEndian(aesData, 0));
                byte[] vendorIDBytes = new byte[2];
                System.arraycopy(aesData, 2, vendorIDBytes, 0, 2);
                aesExtraDataRecord.setVendorID(new String(vendorIDBytes));
                aesExtraDataRecord.setAesStrength((int)(aesData[4] & 0xFF));
                aesExtraDataRecord.setCompressionMethod(Raw.readShortLittleEndian(aesData, 5));

                return aesExtraDataRecord;
            }
        }

        return null;
    }

    /**
     * Reads buf length of bytes from the input stream to buf
     *
     * @param zip4jRaf
     * @param buf
     * @return byte array
     * @throws ZipException
     */
    static byte[] readIntoBuff(RandomAccessFile zip4jRaf, byte[] buf) throws ZipException {
        try {
            if (zip4jRaf.read(buf, 0, buf.length) != -1) {
                return buf;
            } else {
                throw new ZipException("unexpected end of file when reading short buff");
            }
        } catch(IOException e) {
            throw new ZipException("IOException when reading short buff", e);
        }
    }

    /**
     * Returns a long byte from an int byte by appending last 4 bytes as 0's
     *
     * @param intByte
     * @return byte array
     * @throws ZipException
     */
    static byte[] getLongByteFromIntByte(byte[] intByte) throws ZipException {
        if (intByte == null) {
            throw new ZipException("input parameter is null, cannot expand to 8 bytes");
        }

        if (intByte.length != 4) {
            throw new ZipException("invalid byte length, cannot expand to 8 bytes");
        }

        byte[] longBuff = { intByte[0], intByte[1], intByte[2], intByte[3], 0, 0, 0, 0 };
        return longBuff;
    }
}
