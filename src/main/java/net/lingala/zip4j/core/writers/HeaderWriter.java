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
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.IOException;
import java.nio.charset.Charset;

@RequiredArgsConstructor
public final class HeaderWriter {

    public static final int ZIP64_EXTRA_BUF = 50;

    @NonNull
    private final ZipModel zipModel;

    public void finalizeZipFile(@NonNull OutputStreamDecorator out) throws IOException {
        processHeaderData(out);

        LittleEndianBuffer bytes = new LittleEndianBuffer();
        long offsetCentralDir = zipModel.getEndCentralDirectory().getOffs();
        int sizeOfCentralDir = writeCentralDirectory(bytes);

        if (zipModel.isZip64()) {
            zipModel.getZip64().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());
            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
            zipModel.getZip64().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);

            writeZip64EndOfCentralDirectoryRecord(sizeOfCentralDir, offsetCentralDir, bytes);
            writeZip64EndOfCentralDirectoryLocator(bytes);
        }

        writeEndOfCentralDirectoryRecord(sizeOfCentralDir, offsetCentralDir, bytes);
        writeZipHeaderBytes(out, bytes.byteArrayListToByteArray());
    }

    /**
     * Processes zip header data and writes this data to the zip file without any validations.
     * This process is not intended to use for normal operations (adding, deleting, etc) of a zip file.
     * This method is used when certain validations need to be skipped (ex: Merging split zip files,
     * adding comment to a zip file, etc)
     *
     * @param out
     * @throws ZipException
     */
    public void finalizeZipFileWithoutValidations(@NonNull OutputStreamDecorator out) throws IOException {
        LittleEndianBuffer bytes = new LittleEndianBuffer();
        long offsetCentralDir = zipModel.getEndCentralDirectory().getOffs();
        int sizeOfCentralDir = writeCentralDirectory(bytes);

        if (zipModel.isZip64()) {
            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
            writeZip64EndOfCentralDirectoryRecord(sizeOfCentralDir, offsetCentralDir, bytes);
            writeZip64EndOfCentralDirectoryLocator(bytes);
        }

        writeEndOfCentralDirectoryRecord(sizeOfCentralDir, offsetCentralDir, bytes);
        writeZipHeaderBytes(out, bytes.byteArrayListToByteArray());
    }

    /**
     * Writes the zip header data to the zip file
     *
     * @param out
     * @param buf
     * @throws ZipException
     */
    private void writeZipHeaderBytes(OutputStreamDecorator out, byte[] buf) throws IOException {
        if (out.getDelegate().checkBuffSizeAndStartNextSplitFile(buf.length)) {
            finalizeZipFile(out);
            return;
        }

        out.writeBytes(buf);
    }

    private void processHeaderData(OutputStreamDecorator out) throws IOException {
        zipModel.getEndCentralDirectory().setOffs(out.getFilePointer());

        if (zipModel.isZip64()) {
            zipModel.getZip64().getEndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());
            zipModel.getZip64().getEndCentralDirectoryLocator().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);
        }

        zipModel.getEndCentralDirectory().setDiskNumber(out.getCurrSplitFileCounter());
        zipModel.getEndCentralDirectory().setStartDiskNumber(out.getCurrSplitFileCounter());
    }

    private int writeCentralDirectory(LittleEndianBuffer bytes) {
        if (zipModel.isEmpty())
            return 0;

        CentralDirectoryWriter writer = new CentralDirectoryWriter();
        int sizeOfCentralDir = 0;

        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            sizeOfCentralDir += writer.write(zipModel, fileHeader, bytes);

        return sizeOfCentralDir;
    }

    private void writeZip64EndOfCentralDirectoryRecord(int sizeOfCentralDir, long offsetCentralDir, LittleEndianBuffer bytes) {
        byte[] shortByte = new byte[2];
        byte[] emptyShortByte = { 0, 0 };
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];

        //zip64 end of central dir signature
        Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64_ENDSIG);
        bytes.copyByteArrayToArrayList(intByte);

        //size of zip64 end of central directory record
        Raw.writeLongLittleEndian(longByte, 0, 44L);
        bytes.copyByteArrayToArrayList(longByte);

        //version made by
        //version needed to extractEntries
        if (zipModel.isEmpty()) {
            bytes.copyByteArrayToArrayList(emptyShortByte);
            bytes.copyByteArrayToArrayList(emptyShortByte);
        } else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getFileHeaders().get(0).getVersionMadeBy());
            bytes.copyByteArrayToArrayList(shortByte);

            Raw.writeShortLittleEndian(shortByte, 0, (short)zipModel.getFileHeaders().get(0).getVersionToExtract());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        //number of this disk
        Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getDiskNumber());
        bytes.copyByteArrayToArrayList(intByte);

        //number of the disk with start of central directory
        Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirectory().getStartDiskNumber());
        bytes.copyByteArrayToArrayList(intByte);

        //total number of entries in the central directory on this disk
        int numEntries = zipModel.getFileHeaders().size();
        int numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk();

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
    }

    private void writeZip64EndOfCentralDirectoryLocator(LittleEndianBuffer bytes) {
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
    }

    private void writeEndOfCentralDirectoryRecord(int sizeOfCentralDir, long offsetCentralDir, LittleEndianBuffer bytes) {
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
        int numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk();

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

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplitArchive())
            return zipModel.getFileHeaders().size();

        int numOfDisk = zipModel.getEndCentralDirectory().getDiskNumber();

        return (int)zipModel.getFileHeaders().stream()
                            .filter(fileHeader -> fileHeader.getDiskNumber() == numOfDisk)
                            .count();
    }

}
