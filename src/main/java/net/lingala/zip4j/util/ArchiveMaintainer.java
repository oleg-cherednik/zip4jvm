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
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArchiveMaintainer {

    public void removeZipFile(ZipModel zipModel, @NonNull String entryName) throws ZipException {
        SplitOutputStream out = null;
        Path zipFile = zipModel.getZipFile();
        RandomAccessFile in = null;
        boolean successFlag = false;
        Path tmp = null;

        try {
            CentralDirectory.FileHeader fileHeader = zipModel.getFileHeader(entryName);

            if (fileHeader == null)
                return;

            entryName = fileHeader.getFileName();

//            if (indexOfFileHeader < 0)
//                throw new ZipException("file header not found in zip model, cannot remove file");
//            if (zipModel.isSplitArchive())
//                throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");

            tmp = Files.createTempFile(zipModel.getZipFile().getParent(), null, null);
            out = new NoSplitOutputStream(tmp);

            in = createFileHandler(zipModel, InternalZipConstants.READ_MODE);

            List<CentralDirectory.FileHeader> newHeaders = new ArrayList<>();
            CentralDirectory.FileHeader prvHeader = null;

            for (CentralDirectory.FileHeader header : zipModel.getFileHeaders()) {
                if (prvHeader != null) {
                    long offs = out.getFilePointer();
                    copyFile(in, out, prvHeader.getOffsLocalFileHeader(), header.getOffsLocalFileHeader() - 1);
                    newHeaders.add(prvHeader);
                    prvHeader.setOffsLocalFileHeader(offs);

                    // TODO fix offs for zip64

                    //                long offsetLocalHdr = fileHeader.getOffsLocalFileHeader();
//                if (fileHeader.getZip64ExtendedInfo() != null &&
//                        fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
//                    offsetLocalHdr = fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative();
//                }
//
//                fileHeader.setOffsLocalFileHeader(offsetLocalHdr - (offs - offsetLocalFileHeader) - 1);


                }

                prvHeader = entryName.equals(header.getFileName()) ? null : header;
            }

            if (prvHeader != null) {
                long offs = out.getFilePointer();
                copyFile(in, out, prvHeader.getOffsLocalFileHeader(), zipModel.getOffsCentralDirectory() - 1);
                newHeaders.add(prvHeader);
                prvHeader.setOffsLocalFileHeader(offs);
            }

            zipModel.setFileHeaders(newHeaders);

            new HeaderWriter().finalizeZipFile(zipModel, out);

            successFlag = true;
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch(IOException e) {
                throw new ZipException("cannot close input stream or output stream when trying to delete a file from zip file");
            }

            if (successFlag)
                restoreFileName(zipFile, tmp);
            else if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch(IOException e) {
                    throw new ZipException(e);
                }
            }
        }
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

    public static void copyFile(RandomAccessFile inputStream, OutputStream outputStream, long start, long end) throws ZipException {
        System.out.println(start + " - " + end);
        if (inputStream == null || outputStream == null) {
            throw new ZipException("input or output stream is null, cannot copy file");
        }

        if (start < 0) {
            throw new ZipException("starting offset is negative, cannot copy file");
        }

        if (end < 0) {
            throw new ZipException("end offset is negative, cannot copy file");
        }

        if (start > end) {
            throw new ZipException("start offset is greater than end offset, cannot copy file");
        }

        if (start == end) {
            return;
        }

        try {
            inputStream.seek(start);

            int readLen = -2;
            byte[] buff;
            long bytesRead = 0;
            long bytesToRead = end - start;

            if ((end - start) < InternalZipConstants.BUFF_SIZE) {
                buff = new byte[(int)(end - start)];
            } else {
                buff = new byte[InternalZipConstants.BUFF_SIZE];
            }

            while ((readLen = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, readLen);

                bytesRead += readLen;

                if (bytesRead == bytesToRead) {
                    break;
                } else if (bytesRead + buff.length > bytesToRead) {
                    buff = new byte[(int)(bytesToRead - bytesRead)];
                }
            }

        } catch(IOException e) {
            throw new ZipException(e);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private static RandomAccessFile createFileHandler(ZipModel zipModel, String mode) throws ZipException {
        if (zipModel == null || zipModel.getZipFile() == null)
            throw new ZipException("input parameter is null in getFilePointer, cannot create file handler to remove file");

        try {
            return new RandomAccessFile(zipModel.getZipFile().toFile(), mode);
        } catch(FileNotFoundException e) {
            throw new ZipException(e);
        }
    }

}
