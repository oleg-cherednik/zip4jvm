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
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.IOException;

@RequiredArgsConstructor
public final class HeaderWriter {

    public static final int ZIP64_EXTRA_BUF = 50;

    @NonNull
    private final ZipModel zipModel;

    // TODO do we really need validate flag?
    public void finalizeZipFile(@NonNull OutputStreamDecorator out, boolean validate) throws IOException {
        if (validate)
            processHeaderData(out);

        LittleEndianBuffer bytes = new LittleEndianBuffer();
        final long offs = zipModel.getEndCentralDirectory().getOffs();

        long off = out.getOffs();
        new CentralDirectoryWriter(zipModel, out, bytes).write();
        final int size = bytes.size() + (int)(out.getOffs() - off);
        zipModel.getEndCentralDirectory().setSize(size);

        out.writeBytes(bytes.byteArrayListToByteArray());
        bytes = new LittleEndianBuffer();


        if (zipModel.isZip64()) {
            if (validate)
                zipModel.getZip64().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());

            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offs + size);

            if (validate)
                zipModel.getZip64().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);

            writeZip64EndOfCentralDirectoryRecord(size, offs, bytes);
            writeZip64EndOfCentralDirectoryLocator(bytes);
        }

        out.writeBytes(bytes.byteArrayListToByteArray());
        new EndCentralDirectoryWriter(out, zipModel.getCharset()).write(zipModel.getEndCentralDirectory());

    }

    private void processHeaderData(OutputStreamDecorator out) throws IOException {
        zipModel.getEndCentralDirectory().setOffs(out.getFilePointer());

        if (zipModel.isZip64()) {
            zipModel.getZip64().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());
            zipModel.getZip64().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);
        }

        zipModel.getEndCentralDirectory().setDiskNumber(out.getCurrSplitFileCounter());
        zipModel.getEndCentralDirectory().setStartDiskNumber(out.getCurrSplitFileCounter());
    }

    private void writeZip64EndOfCentralDirectoryRecord(int sizeOfCentralDir, long offsetCentralDir, LittleEndianBuffer bytes) {
        bytes.writeInt(InternalZipConstants.ZIP64_ENDSIG);
        bytes.writeLong(44L);   // size of zip64 end of central directory record

        if (zipModel.isEmpty()) {
            bytes.writeShort((short)0);
            bytes.writeShort((short)0);
        } else {
            bytes.writeShort((short)zipModel.getFileHeaders().get(0).getVersionMadeBy());
            bytes.writeShort((short)zipModel.getFileHeaders().get(0).getVersionToExtract());
        }

        bytes.writeInt(zipModel.getEndCentralDirectory().getDiskNumber());
        bytes.writeInt(zipModel.getEndCentralDirectory().getStartDiskNumber());
        bytes.writeLong(countNumberOfFileHeaderEntriesOnDisk());
        bytes.writeLong(zipModel.getFileHeaders().size());
        bytes.writeLong(sizeOfCentralDir);
        bytes.writeLong(offsetCentralDir);
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

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplitArchive())
            return zipModel.getFileHeaders().size();

        int numOfDisk = zipModel.getEndCentralDirectory().getDiskNumber();

        return (int)zipModel.getFileHeaders().stream()
                            .filter(fileHeader -> fileHeader.getDiskNumber() == numOfDisk)
                            .count();
    }

}
