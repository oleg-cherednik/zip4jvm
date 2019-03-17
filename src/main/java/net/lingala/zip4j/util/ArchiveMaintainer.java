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

package net.lingala.zip4j.util;

import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArchiveMaintainer {

    public void removeZipFile(ZipModel zipModel, @NonNull String entryName) {
        CentralDirectory.FileHeader fileHeader = zipModel.getFileHeader(entryName);

        if (fileHeader == null)
            return;

        entryName = fileHeader.getFileName();

//            if (indexOfFileHeader < 0)
//                throw new ZipException("file header not found in zip model, cannot remove file");
//            if (zipModel.isSplitArchive())
//                throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");

        Path tmpZipFile = createTempFile(zipModel);

        try (OutputStream out = new FileOutputStream(tmpZipFile.toFile())) {
            writeFileHeaders(zipModel, entryName, out);
            new HeaderWriter().finalizeZipFile(zipModel, out);
        } catch(IOException e) {
            throw new ZipException(e);
        }

        restoreFileName(zipModel.getZipFile(), tmpZipFile);
    }

    private static Path createTempFile(ZipModel zipModel) {
        try {
            return Files.createTempFile(zipModel.getZipFile().getParent(), null, ".zip");
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

    private static void writeFileHeaders(ZipModel zipModel, String entryName, OutputStream out) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>();
        CentralDirectory.FileHeader prv = null;

        long offsIn = 0;
        long offsOut = 0;
        long skip = 0;

        try (InputStream in = new FileInputStream(zipModel.getZipFile().toFile())) {
            for (CentralDirectory.FileHeader header : zipModel.getFileHeaders()) {
                if (prv != null) {
                    long curOffs = offsOut;
                    long length = header.getOffsLocalFileHeader() - prv.getOffsLocalFileHeader();
                    offsIn += skip + IOUtils.copyLarge(in, out, skip, length);
                    offsOut += length;
                    fileHeaders.add(prv);
                    prv.setOffsLocalFileHeader(curOffs);
                    skip = 0;

                    // TODO fix offs for zip64

                    //                long offsetLocalHdr = fileHeader.getOffsLocalFileHeader();
//                if (fileHeader.getZip64ExtendedInfo() != null &&
//                        fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
//                    offsetLocalHdr = fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative();
//                }
//
//                fileHeader.setOffsLocalFileHeader(offsetLocalHdr - (offs - offsetLocalFileHeader) - 1);
                }

                prv = entryName.equalsIgnoreCase(header.getFileName()) ? null : header;
                skip += header.getOffsLocalFileHeader() - offsIn;
            }

            if (prv != null) {
                long curOffs = offsOut;
                long length = zipModel.getOffsCentralDirectory() - prv.getOffsLocalFileHeader();
                IOUtils.copyLarge(in, out, skip, length);
                offsOut += length;
                fileHeaders.add(prv);
                prv.setOffsLocalFileHeader(curOffs);
            }
        }

        zipModel.setFileHeaders(fileHeaders);
        zipModel.getEndCentralDirectory().setOffsCentralDirectory(offsOut);
    }

    private static void restoreFileName(Path zipFile, Path tmpZipFileName) {
        try {
            if (tmpZipFileName == null)
                return;
            if (Files.deleteIfExists(zipFile))
                Files.move(tmpZipFileName, zipFile);
            else
                throw new ZipException("cannot delete old zip file");
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }

}
