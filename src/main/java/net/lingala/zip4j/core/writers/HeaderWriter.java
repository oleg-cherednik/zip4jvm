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

package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.IOException;
import java.nio.charset.Charset;

public final class HeaderWriter {

    public static final int ZIP64_EXTRA_BUF = 50;

    public void finalizeZipFile(@NonNull ZipModel zipModel, @NonNull OutputStreamDecorator out) throws IOException {
        processHeaderData(zipModel, out);

        LittleEndianBuffer bytes = new LittleEndianBuffer();
        long offsetCentralDir = zipModel.getEndCentralDirectory().getOffs();
        int sizeOfCentralDir = writeCentralDirectory(zipModel, bytes);

        if (zipModel.isZip64()) {
            zipModel.getZip64().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());
            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
            zipModel.getZip64().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);

            writeZip64EndOfCentralDirectoryRecord(zipModel, sizeOfCentralDir, offsetCentralDir, bytes);
            writeZip64EndOfCentralDirectoryLocator(zipModel, bytes);
        }

        writeEndOfCentralDirectoryRecord(zipModel, sizeOfCentralDir, offsetCentralDir, bytes);
        writeZipHeaderBytes(zipModel, out, bytes.byteArrayListToByteArray());
    }

    /**
     * Processes zip header data and writes this data to the zip file without any validations.
     * This process is not intended to use for normal operations (adding, deleting, etc) of a zip file.
     * This method is used when certain validations need to be skipped (ex: Merging split zip files,
     * adding comment to a zip file, etc)
     *
     * @param zipModel
     * @param out
     * @throws ZipException
     */
    public void finalizeZipFileWithoutValidations(@NonNull ZipModel zipModel, @NonNull OutputStreamDecorator out) throws IOException {
        LittleEndianBuffer bytes = new LittleEndianBuffer();
        long offsetCentralDir = zipModel.getEndCentralDirectory().getOffs();
        int sizeOfCentralDir = writeCentralDirectory(zipModel, bytes);

        if (zipModel.isZip64()) {
            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
            writeZip64EndOfCentralDirectoryRecord(zipModel, sizeOfCentralDir, offsetCentralDir, bytes);
            writeZip64EndOfCentralDirectoryLocator(zipModel, bytes);
        }

        writeEndOfCentralDirectoryRecord(zipModel, sizeOfCentralDir, offsetCentralDir, bytes);
        writeZipHeaderBytes(zipModel, out, bytes.byteArrayListToByteArray());
    }

    /**
     * Writes the zip header data to the zip file
     *
     * @param out
     * @param buf
     * @throws ZipException
     */
    private void writeZipHeaderBytes(ZipModel zipModel, OutputStreamDecorator out, byte[] buf) throws IOException {
        if (out.getDelegate() instanceof SplitOutputStream) {
            if (((SplitOutputStream)out.getDelegate()).checkBuffSizeAndStartNextSplitFile(buf.length)) {
                finalizeZipFile(zipModel, out);
                return;
            }
        }

        out.writeBytes(buf);
    }

    private static void processHeaderData(ZipModel zipModel, OutputStreamDecorator out) throws IOException {
        int currSplitFileCounter = out.getCurrSplitFileCounter();

        if (out.getDelegate() instanceof SplitOutputStream)
            zipModel.getEndCentralDirectory().setOffs(out.getFilePointer());

        if (zipModel.isZip64()) {
            zipModel.getZip64().getEndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(currSplitFileCounter);
            zipModel.getZip64().getEndCentralDirectoryLocator().setTotNumberOfDiscs(currSplitFileCounter + 1);
        }
        zipModel.getEndCentralDirectory().setDiskNumber(currSplitFileCounter);
        zipModel.getEndCentralDirectory().setStartDiskNumber(currSplitFileCounter);
    }

    private static int writeCentralDirectory(ZipModel zipModel, LittleEndianBuffer bytes) {
        if (zipModel.isEmpty())
            return 0;

        CentralDirectoryWriter writer = new CentralDirectoryWriter();
        int sizeOfCentralDir = 0;

        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            sizeOfCentralDir += writer.write(zipModel, fileHeader, bytes);

        return sizeOfCentralDir;
    }

    private static void writeZip64EndOfCentralDirectoryRecord(ZipModel zipModel, int sizeOfCentralDir, long offsetCentralDir,
            LittleEndianBuffer bytes) {
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

                Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getFileHeaders().get(0).getVersionToExtract());
                bytes.copyByteArrayToArrayList(shortByte);
            } else {
                bytes.copyByteArrayToArrayList(emptyShortByte);
                bytes.copyByteArrayToArrayList(emptyShortByte);
            }

            //number of this disk
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getDiskNumber());
            bytes.copyByteArrayToArrayList(intByte);

            //number of the disk with start of central directory
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getStartDiskNumber());
            bytes.copyByteArrayToArrayList(intByte);

            //total number of entries in the central directory on this disk
            int numEntries = zipModel.getFileHeaders().size();
            int numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk(zipModel);

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

    private static void writeZip64EndOfCentralDirectoryLocator(@NonNull ZipModel zipModel, LittleEndianBuffer bytes) {
        try {
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];

            //zip64 end of central dir locator  signature
            Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64_ENDSIG_LOC);
            bytes.copyByteArrayToArrayList(intByte);

            //number of the disk with the start of the zip64 end of central directory
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64().getEndCentralDirectoryLocator().getNoOfDiskStartOfZip64EndOfCentralDirRec());
            bytes.copyByteArrayToArrayList(intByte);

            //relative offset of the zip64 end of central directory record
            Raw.writeLongLittleEndian(longByte, 0, zipModel.getZip64().getEndCentralDirectoryLocator().getOffsetZip64EndOfCentralDirRec());
            bytes.copyByteArrayToArrayList(longByte);

            //total number of disks
            Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64().getEndCentralDirectoryLocator().getTotNumberOfDiscs());
            bytes.copyByteArrayToArrayList(intByte);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static void writeEndOfCentralDirectoryRecord(@NonNull ZipModel zipModel,
            int sizeOfCentralDir,
            long offsetCentralDir,
            LittleEndianBuffer bytes) {
        byte[] shortByte = new byte[2];
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];

        //End of central directory signature
        Raw.writeIntLittleEndian(intByte, 0, (int)zipModel.getEndCentralDirectory().getSignature());
        bytes.copyByteArrayToArrayList(intByte);

        //number of this disk
        Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getEndCentralDirectory().getDiskNumber());
        bytes.copyByteArrayToArrayList(shortByte);

        //number of the disk with start of central directory
        Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getEndCentralDirectory().getStartDiskNumber());
        bytes.copyByteArrayToArrayList(shortByte);

        //Total number of entries in central directory on this disk
        int numEntries = zipModel.getFileHeaders().size();
        int numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk(zipModel);

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
    }

    private static int countNumberOfFileHeaderEntriesOnDisk(ZipModel zipModel) {
        if (zipModel.isSplitArchive())
            return zipModel.getFileHeaders().size();

        int numOfDisk = zipModel.getEndCentralDirectory().getDiskNumber();

        return (int)zipModel.getFileHeaders().stream()
                            .filter(fileHeader -> fileHeader.getDiskNumber() == numOfDisk)
                            .count();
    }

}
