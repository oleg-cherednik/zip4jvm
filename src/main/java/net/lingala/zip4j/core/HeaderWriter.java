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

import lombok.NonNull;
import net.lingala.zip4j.core.writers.CentralDirectoryWriter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public class HeaderWriter {

    public static final int ZIP64_EXTRA_BUF = 50;

    /**
     * Processes zip header data and writes this data to the zip file
     *
     * @param zipModel
     * @param outputStream
     * @throws ZipException
     */
    public void finalizeZipFile(ZipModel zipModel,
            OutputStream outputStream) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot finalize zip file");
        }

        try {
            processHeaderData(zipModel, outputStream);

            long offsetCentralDir = zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir();

            LittleEndianBuffer bytes = new LittleEndianBuffer();

            int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, bytes);

            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirectory() == null) {
                    zipModel.setZip64EndCentralDirectory(new Zip64EndCentralDirectory());
                }
                if (zipModel.getZip64EndCentralDirectoryLocator() == null) {
                    zipModel.setZip64EndCentralDirectoryLocator(new Zip64EndCentralDirectoryLocator());
                }

                zipModel.getZip64EndCentralDirectoryLocator().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
                if (outputStream instanceof SplitOutputStream) {
                    zipModel.getZip64EndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(
                            ((SplitOutputStream)outputStream).getCurrSplitFileCounter());
                    zipModel.getZip64EndCentralDirectoryLocator().setTotNumberOfDiscs(
                            ((SplitOutputStream)outputStream).getCurrSplitFileCounter() + 1);
                } else {
                    zipModel.getZip64EndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
                    zipModel.getZip64EndCentralDirectoryLocator().setTotNumberOfDiscs(1);
                }

                writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

                writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, bytes);
            }

            writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

            writeZipHeaderBytes(zipModel, outputStream, bytes.byteArrayListToByteArray());
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    /**
     * Processes zip header data and writes this data to the zip file without any validations.
     * This process is not intended to use for normal operations (adding, deleting, etc) of a zip file.
     * This method is used when certain validations need to be skipped (ex: Merging split zip files,
     * adding comment to a zip file, etc)
     *
     * @param zipModel
     * @param outputStream
     * @throws ZipException
     */
    public void finalizeZipFileWithoutValidations(ZipModel zipModel, OutputStream outputStream) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("input parameters is null, cannot finalize zip file without validations");
        }

        try {

            LittleEndianBuffer bytes = new LittleEndianBuffer();

            long offsetCentralDir = zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir();

            int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, bytes);

            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirectory() == null) {
                    zipModel.setZip64EndCentralDirectory(new Zip64EndCentralDirectory());
                }
                if (zipModel.getZip64EndCentralDirectoryLocator() == null) {
                    zipModel.setZip64EndCentralDirectoryLocator(new Zip64EndCentralDirectoryLocator());
                }

                zipModel.getZip64EndCentralDirectoryLocator().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);

                writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);
                writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, bytes);
            }

            writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

            writeZipHeaderBytes(zipModel, outputStream, bytes.byteArrayListToByteArray());
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    /**
     * Writes the zip header data to the zip file
     *
     * @param outputStream
     * @param buff
     * @throws ZipException
     */
    private void writeZipHeaderBytes(ZipModel zipModel, OutputStream outputStream, byte[] buff) throws ZipException {
        if (buff == null) {
            throw new ZipException("invalid buff to write as zip headers");
        }

        try {
            if (outputStream instanceof SplitOutputStream) {
                if (((SplitOutputStream)outputStream).checkBuffSizeAndStartNextSplitFile(buff.length)) {
                    finalizeZipFile(zipModel, outputStream);
                    return;
                }
            }

            outputStream.write(buff);
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    /**
     * Fills the header data in the zip model
     *
     * @param zipModel
     * @param outputStream
     * @throws ZipException
     */
    private void processHeaderData(ZipModel zipModel, OutputStream outputStream) throws ZipException {
        try {
            int currSplitFileCounter = 0;
            if (outputStream instanceof SplitOutputStream) {
                zipModel.getEndCentralDirectory().setOffOfStartOfCentralDir(
                        ((SplitOutputStream)outputStream).getFilePointer());
                currSplitFileCounter = ((SplitOutputStream)outputStream).getCurrSplitFileCounter();

            }

            if (zipModel.isZip64Format()) {
                if (zipModel.getZip64EndCentralDirectory() == null) {
                    zipModel.setZip64EndCentralDirectory(new Zip64EndCentralDirectory());
                }
                if (zipModel.getZip64EndCentralDirectoryLocator() == null) {
                    zipModel.setZip64EndCentralDirectoryLocator(new Zip64EndCentralDirectoryLocator());
                }

                zipModel.getZip64EndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(currSplitFileCounter);
                zipModel.getZip64EndCentralDirectoryLocator().setTotNumberOfDiscs(currSplitFileCounter + 1);
            }
            zipModel.getEndCentralDirectory().setNoOfDisk(currSplitFileCounter);
            zipModel.getEndCentralDirectory().setNoOfDiskStartCentralDir(currSplitFileCounter);
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private int writeCentralDirectory(@NonNull ZipModel zipModel, @NonNull OutputStream outputStream, @NonNull LittleEndianBuffer bytes)
            throws ZipException {
        if (zipModel.isEmpty())
            return 0;

        CentralDirectoryWriter writer = new CentralDirectoryWriter();

        int sizeOfCentralDir = 0;

        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            sizeOfCentralDir += writer.write(zipModel, fileHeader, outputStream, bytes);

        return sizeOfCentralDir;
    }

    private void writeZip64EndOfCentralDirectoryRecord(ZipModel zipModel,
            OutputStream outputStream, int sizeOfCentralDir,
            long offsetCentralDir, LittleEndianBuffer bytes) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory record");
        }

        try {

            byte[] shortByte = new byte[2];
            byte[] emptyShortByte = { 0, 0 };
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];

            //zip64 end of central dir signature
            Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64_ENDSIG);
            bytes.copyByteArrayToArrayList(intByte);

            //size of zip64 end of central directory record
            Raw.writeLongLittleEndian(longByte, 0, (long)44);
            bytes.copyByteArrayToArrayList(longByte);

            //version made by
            //version needed to extractEntries
            if (!zipModel.isEmpty()) {
                Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getFileHeaders().get(0).getVersionMadeBy());
                bytes.copyByteArrayToArrayList(shortByte);

                Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getFileHeaders().get(0).getVersionNeededToExtract());
                bytes.copyByteArrayToArrayList(shortByte);
            } else {
                bytes.copyByteArrayToArrayList(emptyShortByte);
                bytes.copyByteArrayToArrayList(emptyShortByte);
            }

            //number of this disk
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getNoOfDisk());
            bytes.copyByteArrayToArrayList(intByte);

            //number of the disk with start of central directory
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getNoOfDiskStartCentralDir());
            bytes.copyByteArrayToArrayList(intByte);

            //total number of entries in the central directory on this disk
            int numEntries = 0;
            int numEntriesOnThisDisk = 0;
            numEntries = zipModel.getFileHeaders().size();

            if (zipModel.isSplitArchive())
                countNumberOfFileHeaderEntriesOnDisk(zipModel.getFileHeaders(), zipModel.getEndCentralDirectory().getNoOfDisk());
            else
                numEntriesOnThisDisk = numEntries;

            Raw.writeLongLittleEndian(longByte, 0, numEntriesOnThisDisk);
            bytes.copyByteArrayToArrayList(longByte);

            //Total number of entries in central directory
            Raw.writeLongLittleEndian(longByte, 0, numEntries);
            bytes.copyByteArrayToArrayList(longByte);

            //Size of central directory
            Raw.writeLongLittleEndian(longByte, 0, sizeOfCentralDir);
            bytes.copyByteArrayToArrayList(longByte);

            //offset of start of central directory with respect to the starting disk number
            Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
            bytes.copyByteArrayToArrayList(longByte);

        } catch(ZipException zipException) {
            throw zipException;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static void writeZip64EndOfCentralDirectoryLocator(ZipModel zipModel,
            OutputStream outputStream, LittleEndianBuffer bytes) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory locator");
        }

        try {

            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];

            //zip64 end of central dir locator  signature
            Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64_ENDSIG_LOC);
            bytes.copyByteArrayToArrayList(intByte);

            //number of the disk with the start of the zip64 end of central directory
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirectoryLocator().getNoOfDiskStartOfZip64EndOfCentralDirRec());
            bytes.copyByteArrayToArrayList(intByte);

            //relative offset of the zip64 end of central directory record
            Raw.writeLongLittleEndian(longByte, 0, zipModel.getZip64EndCentralDirectoryLocator().getOffsetZip64EndOfCentralDirRec());
            bytes.copyByteArrayToArrayList(longByte);

            //total number of disks
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirectoryLocator().getTotNumberOfDiscs());
            bytes.copyByteArrayToArrayList(intByte);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private void writeEndOfCentralDirectoryRecord(ZipModel zipModel,
            OutputStream outputStream,
            int sizeOfCentralDir,
            long offsetCentralDir,
            LittleEndianBuffer bytes) throws ZipException {
        if (zipModel == null || outputStream == null) {
            throw new ZipException("zip model or output stream is null, cannot write end of central directory record");
        }

        try {

            byte[] shortByte = new byte[2];
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];

            //End of central directory signature
            Raw.writeIntLittleEndian(intByte, 0, (int)zipModel.getEndCentralDirectory().getSignature());
            bytes.copyByteArrayToArrayList(intByte);

            //number of this disk
            Raw.writeShortLittleEndian(shortByte, 0, (short)(zipModel.getEndCentralDirectory().getNoOfDisk()));
            bytes.copyByteArrayToArrayList(shortByte);

            //number of the disk with start of central directory
            Raw.writeShortLittleEndian(shortByte, 0, (short)(zipModel.getEndCentralDirectory().getNoOfDiskStartCentralDir()));
            bytes.copyByteArrayToArrayList(shortByte);

            //Total number of entries in central directory on this disk
            int numEntries = 0;
            int numEntriesOnThisDisk = 0;

            numEntries = zipModel.getFileHeaders().size();
            if (zipModel.isSplitArchive())
                numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk(zipModel.getFileHeaders(),
                        zipModel.getEndCentralDirectory().getNoOfDisk());
            else
                numEntriesOnThisDisk = numEntries;

            Raw.writeShortLittleEndian(shortByte, 0, (short)numEntriesOnThisDisk);
            bytes.copyByteArrayToArrayList(shortByte);

            //Total number of entries in central directory
            Raw.writeShortLittleEndian(shortByte, 0, (short)numEntries);
            bytes.copyByteArrayToArrayList(shortByte);

            //Size of central directory
            Raw.writeIntLittleEndian(intByte, 0, sizeOfCentralDir);
            bytes.copyByteArrayToArrayList(intByte);

            //Offset central directory
            if (offsetCentralDir > InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);
                bytes.copyByteArrayToArrayList(intByte);
            } else {
                Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
                System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)offsetCentralDir);
                bytes.copyByteArrayToArrayList(intByte);
            }

            //Zip File comment length
            int commentLength = 0;
            if (zipModel.getEndCentralDirectory().getComment() != null) {
                commentLength = zipModel.getEndCentralDirectory().getCommentLength();
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short)commentLength);
            bytes.copyByteArrayToArrayList(shortByte);

            //Comment
            if (commentLength > 0) {
                Charset charset = Charset.forName(System.getProperty("sun.jnu.encoding", zipModel.getCharset().name()));
                bytes.copyByteArrayToArrayList(zipModel.getEndCentralDirectory().getComment().getBytes(charset));
            }

        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    public void updateLocalFileHeader(LocalFileHeader localFileHeader, long offset,
            int toUpdate, ZipModel zipModel, byte[] bytesToWrite, int noOfDisk, SplitOutputStream outputStream) throws ZipException {
        if (localFileHeader == null || offset < 0 || zipModel == null) {
            throw new ZipException("invalid input parameters, cannot update local file header");
        }

        try {
            boolean closeFlag = false;
            SplitOutputStream currOutputStream = null;

            if (noOfDisk != outputStream.getCurrSplitFileCounter()) {
                Path zipFile = zipModel.getZipFile();
                Path fileName = ZipModel.getSplitFilePath(zipFile, noOfDisk + 1);
                currOutputStream = new NoSplitOutputStream(fileName);
                closeFlag = true;
            } else {
                currOutputStream = outputStream;
            }

            long currOffset = currOutputStream.getFilePointer();

            switch (toUpdate) {
                case InternalZipConstants.UPDATE_LFH_CRC:
                    currOutputStream.seek(offset + toUpdate);
                    currOutputStream.write(bytesToWrite);
                    break;
                case InternalZipConstants.UPDATE_LFH_COMP_SIZE:
                case InternalZipConstants.UPDATE_LFH_UNCOMP_SIZE:
                    updateCompressedSizeInLocalFileHeader(currOutputStream, localFileHeader,
                            offset, toUpdate, bytesToWrite, zipModel.isZip64Format());
                    break;
                default:
                    break;
            }
            if (closeFlag) {
                currOutputStream.close();
            } else {
                outputStream.seek(currOffset);
            }

        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private void updateCompressedSizeInLocalFileHeader(SplitOutputStream outputStream, LocalFileHeader localFileHeader,
            long offset, long toUpdate, byte[] bytesToWrite, boolean isZip64Format) throws ZipException {

        if (outputStream == null) {
            throw new ZipException("invalid output stream, cannot update compressed size for local file header");
        }

        try {
            if (localFileHeader.isWriteComprSizeInZip64ExtraRecord()) {
                if (bytesToWrite.length != 8) {
                    throw new ZipException("attempting to write a non 8-byte compressed size block for a zip64 file");
                }

                //4 - compressed size
                //4 - uncomprssed size
                //2 - file name length
                //2 - extra field length
                //file name length
                //2 - Zip64 signature
                //2 - size of zip64 data
                //8 - crc
                //8 - compressed size
                //8 - uncompressed size
                long zip64CompressedSizeOffset = offset + toUpdate + 4 + 4 + 2 + 2 + localFileHeader.getFileNameLength() + 2 + 2 + 8;
                if (toUpdate == InternalZipConstants.UPDATE_LFH_UNCOMP_SIZE) {
                    zip64CompressedSizeOffset += 8;
                }
                outputStream.seek(zip64CompressedSizeOffset);
                outputStream.write(bytesToWrite);
            } else {
                outputStream.seek(offset + toUpdate);
                outputStream.write(bytesToWrite);
            }
        } catch(IOException e) {
            throw new ZipException(e);
        }

    }

    private int countNumberOfFileHeaderEntriesOnDisk(List<CentralDirectory.FileHeader> fileHeaders,
            int numOfDisk) throws ZipException {
        if (fileHeaders == null) {
            throw new ZipException("file headers are null, cannot calculate number of entries on this disk");
        }

        int noEntries = 0;
        for (int i = 0; i < fileHeaders.size(); i++) {
            CentralDirectory.FileHeader fileHeader = fileHeaders.get(i);
            if (fileHeader.getDiskNumberStart() == numOfDisk) {
                noEntries++;
            }
        }
        return noEntries;
    }

}
