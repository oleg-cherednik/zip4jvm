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
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.util.zip.ZipException;

@RequiredArgsConstructor
public final class HeaderWriter {

    public static final int ZIP64_EXTRA_BUF = 50;

    @NonNull
    private final ZipModel zipModel;

    // TODO do we really need validate flag?
    public void finalizeZipFile(@NonNull OutputStreamDecorator out, boolean validate) throws IOException {
        long s = 0;

        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            s += fileHeader.getFileName().getBytes(zipModel.getCharset()).length + fileHeader.getExtraFieldLength();

        s += zipModel.getEndCentralDirectory().getComment(zipModel.getCharset()).length;

        if(s > 65_535)
            throw new ZipException("----------------");



        if (validate)
            processHeaderData(out);

        final long offs = zipModel.getEndCentralDirectory().getOffs();
        long off = out.getOffs();
        new CentralDirectoryWriter(out, zipModel).write();
        final int size = (int)(out.getOffs() - off);
        zipModel.getEndCentralDirectory().setSize(size);

        if (zipModel.isZip64()) {
            Zip64EndCentralDirectory dir = zipModel.getZip64().getEndCentralDirectory();
            dir.setSizeOfCentralDir(Zip64EndCentralDirectory.SIZE + ArrayUtils.getLength(dir.getExtensibleDataSector()));
            dir.setVersionMadeBy(zipModel.getVersionMadeBy());
            dir.setVersionNeededToExtract(zipModel.getVersionToExtract());
            dir.setNoOfThisDisk(zipModel.getEndCentralDirectory().getDiskNumber());
            dir.setNoOfThisDiskStartOfCentralDir(zipModel.getEndCentralDirectory().getStartDiskNumber());
            dir.setTotNoOfEntriesInCentralDirOnThisDisk(countNumberOfFileHeaderEntriesOnDisk());
            dir.setTotalEntries(zipModel.getFileHeaders().size());
            dir.setSizeOfCentralDir(size);
            dir.setOffsetStartCenDirWRTStartDiskNo(offs);
        }

        if (zipModel.isZip64() && validate)
            zipModel.getZip64().setNoOfDiskStartOfZip64EndOfCentralDirRec(out.getCurrSplitFileCounter());

        if (zipModel.isZip64())
            zipModel.getZip64().setOffsetZip64EndOfCentralDirRec(offs + size);

        if (zipModel.isZip64() && validate)
            zipModel.getZip64().setTotNumberOfDiscs(out.getCurrSplitFileCounter() + 1);

        new Zip64EndCentralDirectoryWriter(out).write(zipModel.isZip64() ? zipModel.getZip64().getEndCentralDirectory() : null);
        new Zip64EndCentralDirectoryLocatorWriter(out).write(zipModel.isZip64() ? zipModel.getZip64().getEndCentralDirectoryLocator() : null);
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

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplitArchive())
            return zipModel.getFileHeaders().size();

        int numOfDisk = zipModel.getEndCentralDirectory().getDiskNumber();

        return (int)zipModel.getFileHeaders().stream()
                            .filter(fileHeader -> fileHeader.getDiskNumber() == numOfDisk)
                            .count();
    }

}
